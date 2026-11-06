package com.journeyflow.services;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import com.journeyflow.dto.Chat.ChatCreateRequestDto;
import com.journeyflow.dto.Chat.ChatResponseDto;
import com.journeyflow.dto.Chat.ChatUpdateRequestDto;
import com.journeyflow.dto.Common.PageResponseDto;
import com.journeyflow.models.ChatEntity;
import com.journeyflow.models.EmployeeEntity;
import com.journeyflow.models.AlertEntity;
import com.journeyflow.models.ManagerEntity;
import com.journeyflow.models.PillProgressId;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertUrgency;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;
import com.journeyflow.repositories.ChatRepository;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.AlertRepository;
import com.journeyflow.repositories.ManagerRepository;
import com.journeyflow.repositories.PillProgressRepository;
import com.journeyflow.repositories.PillsRepository;
import com.journeyflow.repositories.StoreRepository;
import com.journeyflow.services.PillProgressService;
import com.journeyflow.services.AnalysisResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    /** Nome gravado em "author" para mensagens enviadas pelo bot. */
    static final String BOT_AUTHOR = "JourneyFlow";
    private static final Set<String> PILL_SEARCH_STOP_WORDS = Set.of(
            "com", "das", "dos", "uma", "que", "para", "por", "sobre", "qual", "quais", "como",
            "mais", "sim", "nao", "não", "tem", "ter", "quero", "pode", "ajuda", "conteudo",
            "conteúdo", "pílula", "pilula", "revisar", "revisao", "revisão", "praticar");
    private static final Pattern PILL_SEARCH_NON_WORD = Pattern.compile("[^\\p{L}\\p{N}]+");
    private static final String HR_CONSENT_PROMPT = "Identifiquei que talvez seja importante oferecer um acompanhamento humano. Você autoriza que eu envie ao RH apenas um pedido de conversa privada e a prioridade do acompanhamento? O conteúdo do relato e nomes de outras pessoas não serão incluídos.";
    private static final String CONVERSATION_CLOSED = "Obrigado por compartilhar como está sua jornada. A conversa foi concluída; nenhum conteúdo do que você contou foi incluído em alerta.";
    private static final String ALERT_CONVERSATION_CLOSED = "Obrigado por compartilhar. A conversa foi concluída. O alerta registra apenas a prioridade e uma orientação para oferecer acompanhamento, sem incluir o conteúdo do seu relato.";
    private static final String HR_CONVERSATION_CLOSED = "Obrigado por confirmar. O RH recebeu apenas a prioridade e um pedido para conversar com você em particular, sem o conteúdo do seu relato.";
    private static final String DECLINED_CONVERSATION_CLOSED = "Entendido. Não encaminhei o pedido. Se mudar de ideia, procure diretamente o canal de RH da empresa.";

    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;
    private final ManagerRepository managerRepository;
    private final StoreRepository storeRepository;
    private final PillsRepository pillsRepository;
    private final PillProgressRepository pillProgressRepository;
    private final PillProgressService pillProgressService;
    private final AlertRepository alertRepository;
    private final GeminiService geminiService;
    private final PointsService pointsService;
    private final EmployeeService employeeService;
    private final ObjectMapper mapper;
    private final Clock clock;

    /**
     * O DTO de criação não recebe "author": ele é derivado aqui
     * (nome do colaborador para USER, BOT_AUTHOR para BOT), então o cliente não consegue forjá-lo.
     * dateSent é opcional e assume "agora".
     */
    public ChatResponseDto createChat(ChatCreateRequestDto dto, String actorEmail) {
        if (dto.sender() != MessageSender.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente mensagens do funcionário podem ser enviadas por este endpoint");
        }
        EmployeeEntity employee = findAuthorizedEmployee(dto.employeeId(), actorEmail);
        ChatEntity previousMessage = chatRepository.findFirstByEmployeeIdOrderByDateSentDesc(employee.getId()).orElse(null);
        Long pendingPillId = previousMessage != null && previousMessage.getSender() == MessageSender.BOT
                && previousMessage.getPillId() != null
                && !pillProgressRepository.existsById(new PillProgressId(employee.getId(), previousMessage.getPillId()))
                        ? previousMessage.getPillId()
                        : null;
        if (dto.pillId() != null && !dto.pillId().equals(pendingPillId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não há quiz pendente para essa pílula");
        }
        Long answeredPillId = dto.pillId() == null ? pendingPillId : dto.pillId();
        boolean waitingForHrConsent = previousMessage != null && previousMessage.getSender() == MessageSender.BOT
                && isHrConsentPrompt(previousMessage.getContent());
        ManagerEntity hrRecipient = waitingForHrConsent && isConsentGranted(dto.content())
                ? findAlertRecipient(employee, AlertDestination.HR)
                : null;

        ChatEntity chat = new ChatEntity();
        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(resolveAuthor(dto.sender(), employee));
        chat.setDateSent(dto.dateSent() == null ? Instant.now(clock) : dto.dateSent());
        chat.setPillId(answeredPillId);

        chatRepository.save(chat);
        employeeService.registerActivity(employee.getId());
        if (answeredPillId != null) {
            boolean correct = answerPillQuiz(employee, answeredPillId, dto.content());
            String result = correct
                    ? "Resposta correta! A pílula foi concluída e a pontuação correspondente foi registrada no seu saldo."
                    : "Essa resposta não está correta. Tente novamente escolhendo uma das alternativas.";
            return ChatResponseDto.from(createBotMessage(employee, result, correct ? null : answeredPillId));
        }

        if (waitingForHrConsent) {
            if (isConsentDeclined(dto.content())) {
                pointsService.awardConversationCompletion(employee.getId());
                return ChatResponseDto.from(createBotMessage(employee, DECLINED_CONVERSATION_CLOSED));
            }
            if (isConsentGranted(dto.content())) {
                createAlert(employee, AlertDestination.HR, AlertUrgency.FOLLOW_UP, hrRecipient);
                pointsService.awardConversationCompletion(employee.getId());
                return ChatResponseDto.from(createBotMessage(employee, HR_CONVERSATION_CLOSED));
            }
            return ChatResponseDto.from(createBotMessage(employee,
                    "Não entendi sua resposta. Você autoriza o RH a entrar em contato para uma conversa privada? Responda sim ou não."));
        }
        if (isExplicitHrContactRequest(dto.content())) {
            return ChatResponseDto.from(createBotMessage(employee,
                    "Entendi. Ao concluir a conversa, a plataforma avaliará o encaminhamento ao RH. Se encaminhado, o alerta pedirá que chamem você para uma conversa privada e não incluirá o relato."));
        }
        boolean acceptedPractice = isPracticeRequest(dto.content())
                || isPracticeAcceptance(dto.content()) && isPracticeInvitation(previousMessage);
        if (acceptedPractice) {
            List<ChatEntity> history = chatRepository.findByEmployeeIdOrderByDateSentAsc(employee.getId());
            PillsEntity pill = findPracticePill(employee, history);
            if (pill != null) {
                return ChatResponseDto.from(createPillQuizPrompt(employee, pill));
            }
            PillsEntity upcomingPill = findUpcomingPill(employee, history, true);
            if (upcomingPill != null) {
                return ChatResponseDto.from(createBotMessage(employee,
                        pillUnlockMessage(employee, upcomingPill)));
            }
            return ChatResponseDto.from(createBotMessage(employee,
                    "Ainda não encontrei uma pílula disponível com um quiz cadastrado para esse tema. Podemos continuar conversando sobre outra dúvida."));
        }
        return ChatResponseDto.from(replyWithGemini(employee, "", null));
    }

    /** Cria no chat um quiz baseado somente em conteúdo já cadastrado em pills. */
    @Transactional
    public ChatResponseDto startPillQuiz(UUID employeeId, String actorEmail) {
        EmployeeEntity employee = findAuthorizedEmployee(employeeId, actorEmail);
        ChatEntity latest = chatRepository.findFirstByEmployeeIdOrderByDateSentDesc(employeeId).orElse(null);
        if (latest != null && latest.getSender() == MessageSender.BOT && latest.getPillId() != null
                && !pillProgressRepository.existsById(new PillProgressId(employeeId, latest.getPillId()))) {
            return ChatResponseDto.from(latest);
        }
        for (PillsEntity pill : pillsRepository.findAll(Sort.by("dayFrom").ascending())) {
            if (!isPillAvailable(employee, pill)) continue;
            if (pillProgressRepository.existsById(new PillProgressId(employeeId, pill.getId()))) continue;
            List<String> options = quizOptions(pill);
            if (options.isEmpty() || pill.getQuiz_correct() == null
                    || pill.getQuiz_correct() < 0 || pill.getQuiz_correct() >= options.size()) continue;
            return ChatResponseDto.from(createPillQuizPrompt(employee, pill));
        }
        return ChatResponseDto.from(createBotMessage(employee,
                "Ainda não há pílulas disponíveis para um quiz. Você pode continuar compartilhando como está sua jornada."));
    }

    /** A chamada externa ao Gemini ocorre sem transação de banco aberta. */
    public ChatResponseDto finishConversation(UUID employeeId, String actorEmail) {
        EmployeeEntity employee = findAuthorizedEmployee(employeeId, actorEmail);
        List<ChatEntity> history = chatRepository.findByEmployeeIdOrderByDateSentAsc(employeeId);
        if (!history.isEmpty() && history.getLast().getSender() == MessageSender.BOT
                && isHrConsentPrompt(history.getLast().getContent())) {
            return ChatResponseDto.from(history.getLast());
        }
        if (!history.isEmpty() && isClosedMessage(history.getLast())) {
            return ChatResponseDto.from(history.getLast());
        }
        int activeStart = 0;
        for (int i = 0; i < history.size(); i++) {
            if (isClosedMessage(history.get(i))) activeStart = i + 1;
        }
        List<ChatEntity> activeHistory = history.subList(activeStart, history.size());
        List<ChatEntity> employeeMessages = activeHistory.stream()
                .filter(message -> message.getSender() == MessageSender.USER)
                .toList();
        String transcript = employeeMessages.stream()
                .skip(Math.max(0, employeeMessages.size() - 30L))
                .map(message -> "Funcionário: " + message.getContent())
                .reduce((left, right) -> left + "\n" + right).orElse("");
        if (transcript.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conversa ainda não tem mensagens");
        }

        if (hasExplicitHrConsent(activeHistory)) {
            ManagerEntity hrRecipient = findAlertRecipient(employee, AlertDestination.HR);
            createAlert(employee, AlertDestination.HR, AlertUrgency.FOLLOW_UP, hrRecipient);
            pointsService.awardConversationCompletion(employee.getId());
            return ChatResponseDto.from(createBotMessage(employee, HR_CONVERSATION_CLOSED));
        }

        String relevantTranscript = transcript.length() > 1800
                ? transcript.substring(transcript.length() - 1800)
                : transcript;
        var analysis = geminiService.analyze(relevantTranscript);
        if (analysis.isEmpty()) {
            return ChatResponseDto.from(createBotMessage(employee,
                    "Não consegui analisar a conversa agora. Suas mensagens continuam salvas; tente concluir novamente mais tarde."));
        }
        AnalysisResult result = analysis.get();
        if ("NEUTRAL".equals(result.kind()) || "INSUFFICIENT".equals(result.kind())) {
            pointsService.awardConversationCompletion(employee.getId());
            return ChatResponseDto.from(createBotMessage(employee, CONVERSATION_CLOSED));
        }
        if (result.destination() == AlertDestination.HR) {
            return ChatResponseDto.from(createBotMessage(employee, HR_CONSENT_PROMPT));
        }
        ManagerEntity recipient = findAlertRecipient(employee, result.destination());
        createAlert(employee, result.destination(), result.urgency(), recipient);
        pointsService.awardConversationCompletion(employee.getId());
        return ChatResponseDto.from(createBotMessage(employee, ALERT_CONVERSATION_CLOSED));
    }

    private static boolean isClosedMessage(ChatEntity message) {
        if (message.getSender() != MessageSender.BOT) return false;
        return CONVERSATION_CLOSED.equals(message.getContent())
                || ALERT_CONVERSATION_CLOSED.equals(message.getContent())
                || HR_CONVERSATION_CLOSED.equals(message.getContent())
                || DECLINED_CONVERSATION_CLOSED.equals(message.getContent());
    }

    private boolean answerPillQuiz(EmployeeEntity employee, Long pillId, String answer) {
        PillsEntity pill = pillsRepository.findById(pillId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pílula não encontrada"));
        List<String> options = quizOptions(pill);
        boolean correct = false;
        try {
            correct = Integer.parseInt(answer.trim()) == pill.getQuiz_correct();
        } catch (NumberFormatException ignored) {
            String normalizedAnswer = answer.trim().toLowerCase(java.util.Locale.ROOT);
            if (normalizedAnswer.matches("[a-z][).]?")) {
                int selectedIndex = normalizedAnswer.charAt(0) - 'a';
                correct = pill.getQuiz_correct() != null && selectedIndex == pill.getQuiz_correct();
            } else if (pill.getQuiz_correct() != null && pill.getQuiz_correct() >= 0
                    && pill.getQuiz_correct() < options.size()) {
                correct = options.get(pill.getQuiz_correct()).equalsIgnoreCase(answer.trim());
            }
        }
        if (correct) {
            pillProgressService.completeQuiz(employee.getId(), pill.getId());
            return true;
        }
        return false;
    }

    private ChatEntity replyWithGemini(EmployeeEntity employee, String backendContext, Long pendingPillId) {
        List<ChatEntity> history = chatRepository.findByEmployeeIdOrderByDateSentAsc(employee.getId());
        int activeStart = 0;
        for (int i = 0; i < history.size(); i++) {
            if (isClosedMessage(history.get(i))) activeStart = i + 1;
        }
        List<ChatEntity> activeHistory = history.subList(activeStart, history.size());
        List<PillsEntity> learningPills = findRelevantLearningPills(employee, activeHistory);
        if (learningPills.isEmpty()) {
            PillsEntity upcomingPill = findUpcomingPill(employee, activeHistory, false);
            if (upcomingPill != null) {
                return createBotMessage(employee, pillUnlockMessage(employee, upcomingPill), pendingPillId);
            }
        }
        if (pendingPillId != null) {
            pillsRepository.findById(pendingPillId).ifPresent(pill -> {
                if (learningPills.stream().noneMatch(item -> item.getId().equals(pill.getId()))) {
                    learningPills.add(pill);
                }
            });
        }
        String response = geminiService.respond(activeHistory, employee.getPosition(), learningPills, backendContext)
                .orElse("Recebi sua mensagem, mas não consegui responder agora. Tente novamente em instantes.");
        return createBotMessage(employee, response, pendingPillId);
    }

    private List<PillsEntity> findRelevantLearningPills(EmployeeEntity employee, List<ChatEntity> history) {
        Set<String> queryTerms = Set.of();
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatEntity message = history.get(i);
            if (message.getSender() != MessageSender.USER) continue;
            queryTerms = tokenizePillSearch(message.getContent());
            if (!queryTerms.isEmpty()) break;
        }
        if (queryTerms.isEmpty()) return List.of();
        Set<String> matchedTerms = queryTerms;

        return pillsRepository.findAll(Sort.by("dayFrom").ascending()).stream()
                .filter(pill -> isPillAvailable(employee, pill))
                .map(pill -> new java.util.AbstractMap.SimpleEntry<>(
                        pill, scorePill(pill, matchedTerms)))
                .filter(entry -> hasStrongPillMatch(entry.getKey(), matchedTerms, entry.getValue()))
                .sorted(java.util.Map.Entry.<PillsEntity, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().getDayFrom())
                        .thenComparing(entry -> entry.getKey().getId()))
                .limit(3)
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    private static int scorePill(PillsEntity pill, Set<String> queryTerms) {
        return scorePillField(pill.getTitle(), queryTerms, 5)
                + scorePillField(pill.getContent(), queryTerms, 1)
                + scorePillField(pill.getQuiz_question(), queryTerms, 2)
                + scorePillField(pill.getQuiz_options(), queryTerms, 1);
    }

    private static boolean hasStrongPillMatch(PillsEntity pill, Set<String> queryTerms, int totalScore) {
        if (queryTerms.isEmpty()) return true;
        return scorePillField(pill.getTitle(), queryTerms, 1) > 0
                || queryTerms.size() >= 2 && totalScore >= 2;
    }

    private static int scorePillField(String value, Set<String> queryTerms, int weight) {
        if (value == null || value.isBlank()) return 0;
        Set<String> fieldTerms = tokenizePillSearch(value);
        return (int) queryTerms.stream().filter(fieldTerms::contains).count() * weight;
    }

    private static Set<String> tokenizePillSearch(String value) {
        return java.util.Arrays.stream(PILL_SEARCH_NON_WORD.split(
                        java.text.Normalizer.normalize(value.toLowerCase(Locale.ROOT),
                                java.text.Normalizer.Form.NFD)
                                .replaceAll("\\p{M}", "")))
                .filter(term -> term.length() >= 3 && !PILL_SEARCH_STOP_WORDS.contains(term))
                .collect(java.util.stream.Collectors.toSet());
    }

    private static Set<String> practiceTopicTerms(String content) {
        Set<String> terms = new java.util.HashSet<>(tokenizePillSearch(content));
        terms.removeAll(Set.of(
                "quer", "quero", "vamos", "bora", "aceito", "claro", "praticar", "treinar",
                "treinamento", "estudar", "estudo", "quiz", "fazer", "teste", "testar",
                "conhecimento", "revisar", "revisao", "tema", "assunto"));
        return terms;
    }

    private static Set<String> practiceTopicTermsFromHistory(List<ChatEntity> history) {
        int latestUserIndex = -1;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).getSender() == MessageSender.USER) {
                latestUserIndex = i;
                break;
            }
        }
        if (latestUserIndex < 0) return Set.of();

        Set<String> queryTerms = practiceTopicTerms(history.get(latestUserIndex).getContent());
        if (!queryTerms.isEmpty()) return queryTerms;

        // Para um "sim", recupere primeiro a pergunta que gerou o convite de prática.
        for (int i = latestUserIndex - 1; i >= 0; i--) {
            ChatEntity invitation = history.get(i);
            if (invitation.getSender() != MessageSender.BOT || !isPracticeInvitation(invitation)) continue;

            for (int j = i - 1; j >= 0; j--) {
                ChatEntity previousUserMessage = history.get(j);
                if (previousUserMessage.getSender() != MessageSender.USER) continue;
                queryTerms = practiceTopicTerms(previousUserMessage.getContent());
                if (!queryTerms.isEmpty()) return queryTerms;
                break;
            }
            return practiceTopicTerms(invitation.getContent());
        }
        return Set.of();
    }

    private PillsEntity findPracticePill(EmployeeEntity employee, List<ChatEntity> history) {
        Set<String> matchedTerms = practiceTopicTermsFromHistory(history);

        return pillsRepository.findAll(Sort.by("dayFrom").ascending()).stream()
                .filter(pill -> isPillAvailable(employee, pill))
                .filter(pill -> !pillProgressRepository.existsById(
                        new PillProgressId(employee.getId(), pill.getId())))
                .filter(this::hasValidQuiz)
                .map(pill -> new java.util.AbstractMap.SimpleEntry<>(
                        pill, scorePill(pill, matchedTerms)))
                .filter(entry -> hasStrongPillMatch(entry.getKey(), matchedTerms, entry.getValue()))
                .sorted(java.util.Map.Entry.<PillsEntity, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().getDayFrom())
                        .thenComparing(entry -> entry.getKey().getId()))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private PillsEntity findUpcomingPill(EmployeeEntity employee, List<ChatEntity> history,
                                         boolean allowEarliestWhenNoTopic) {
        Set<String> queryTerms = practiceTopicTermsFromHistory(history);
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(
                employee.getStartDate(), LocalDate.now(clock)));

        return pillsRepository.findAll(Sort.by("dayFrom").ascending()).stream()
                .filter(pill -> pill.getDayFrom() != null && pill.getDayFrom() > elapsedDays)
                .map(pill -> new java.util.AbstractMap.SimpleEntry<>(
                        pill, scorePill(pill, queryTerms)))
                .filter(entry -> queryTerms.isEmpty()
                        ? allowEarliestWhenNoTopic
                        : hasStrongPillMatch(entry.getKey(), queryTerms, entry.getValue()))
                .sorted(java.util.Map.Entry.<PillsEntity, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> entry.getKey().getDayFrom())
                        .thenComparing(entry -> entry.getKey().getId()))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private String pillUnlockMessage(EmployeeEntity employee, PillsEntity pill) {
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(
                employee.getStartDate(), LocalDate.now(clock)));
        long daysRemaining = Math.max(1, pill.getDayFrom() - elapsedDays);
        String countdown = daysRemaining == 1
                ? "Falta 1 dia"
                : "Faltam " + daysRemaining + " dias";
        return "A pílula " + pill.getTitle() + " ainda não está liberada. " + countdown
                + " para ser liberada (no dia " + pill.getDayFrom()
                + " da sua jornada).";
    }

    private boolean hasValidQuiz(PillsEntity pill) {
        List<String> options = quizOptions(pill);
        return pill.getQuiz_correct() != null
                && pill.getQuiz_correct() >= 0
                && pill.getQuiz_correct() < options.size();
    }

    private ChatEntity createPillQuizPrompt(EmployeeEntity employee, PillsEntity pill) {
        List<String> options = quizOptions(pill);
        String choices = java.util.stream.IntStream.range(0, options.size())
                .mapToObj(index -> (char) ('A' + index) + ") " + options.get(index))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return createBotMessage(employee,
                "Vamos praticar " + pill.getTitle() + ".\n\n" + pill.getQuiz_question() + "\n" + choices,
                pill.getId());
    }

    private static boolean isPracticeRequest(String content) {
        String normalized = normalizeConsentAnswer(content);
        return normalized.contains("pratic")
                || normalized.contains("fazer um quiz")
                || normalized.contains("me teste")
                || normalized.contains("testar meu conhecimento");
    }

    private static boolean isPracticeAcceptance(String content) {
        String normalized = normalizeConsentAnswer(content).replaceAll("[.!?]+$", "").trim();
        return Set.of("sim", "claro", "quero", "aceito", "pode", "vamos", "bora", "com certeza")
                .contains(normalized);
    }

    private static boolean isPracticeInvitation(ChatEntity message) {
        if (message == null || message.getSender() != MessageSender.BOT) return false;
        String normalized = normalizeConsentAnswer(message.getContent());
        return normalized.contains("pratic")
                || normalized.contains("revisar")
                || normalized.contains("revisao")
                || normalized.contains("treinar")
                || normalized.contains("quer aprender");
    }

    private boolean isPillAvailable(EmployeeEntity employee, PillsEntity pill) {
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(employee.getStartDate(), LocalDate.now(clock)));
        return pill.getDayFrom() != null && elapsedDays >= pill.getDayFrom();
    }

    private List<String> quizOptions(PillsEntity pill) {
        try {
            return mapper.readValue(pill.getQuiz_options(), mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception ignored) {
            return java.util.Arrays.stream(pill.getQuiz_options().split("[;,\\n\\r]+"))
                    .map(String::trim).filter(option -> !option.isEmpty()).toList();
        }
    }

    private ChatEntity createBotMessage(EmployeeEntity employee, String content) {
        return createBotMessage(employee, content, null);
    }

    private ChatEntity createBotMessage(EmployeeEntity employee, String content, Long pillId) {
        ChatEntity bot = new ChatEntity();
        bot.setEmployee(employee);
        bot.setSender(MessageSender.BOT);
        bot.setContent(content);
        bot.setSource(ContentSource.TEXT);
        bot.setAuthor(BOT_AUTHOR);
        bot.setDateSent(Instant.now(clock));
        bot.setPillId(pillId);
        return chatRepository.save(bot);
    }

    private void createAlert(EmployeeEntity employee, AlertDestination destination,
                             AlertUrgency urgency, ManagerEntity recipient) {
        AlertEntity alert = new AlertEntity();
        alert.setEmployeeId(employee.getId());
        StoreEntity store = storeRepository.findById(employee.getStore().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade do funcionário não encontrada"));
        alert.setStore(store);
        alert.setAssignedTo(recipient);
        alert.setDestination(destination);
        alert.setUrgency(urgency);
        alert.setSuggestion(suggestionFor(destination, urgency));
        alert.setAlertDate(LocalDateTime.now(clock));
        alert.setAlertTitle("Acompanhamento de colaborador");
        alert.setSubtitle("Prioridade: " + urgencyLabel(urgency) + ". O conteúdo da conversa não está incluído.");
        alertRepository.save(alert);
    }

    private ManagerEntity findAlertRecipient(EmployeeEntity employee, AlertDestination destination) {
        Long storeId = employee.getStore().getId();
        String primaryPosition = destination == AlertDestination.HR ? "RH" : "MANAGER";
        String alternatePosition = destination == AlertDestination.HR ? "HR" : "GERENTE";
        return managerRepository.findFirstByStoreIdAndPositionIgnoreCaseOrderByNameAsc(storeId, primaryPosition)
                .or(() -> managerRepository.findFirstByStoreIdAndPositionIgnoreCaseOrderByNameAsc(
                        storeId, alternatePosition))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        destination == AlertDestination.HR
                                ? "Não há um responsável de RH cadastrado nesta unidade"
                                : "Não há um manager cadastrado nesta unidade"));
    }

    private static String suggestionFor(AlertDestination destination, AlertUrgency urgency) {
        if (destination == AlertDestination.HR) {
            return "Oferecer uma conversa privada e prioritária com o RH, sem solicitar detalhes pelo alerta.";
        }
        return switch (urgency) {
            case NORMAL -> "Manter o canal aberto e oferecer acompanhamento no próximo contato.";
            case ATTENTION -> "Convidar o colaborador para uma conversa breve e reservada.";
            case FOLLOW_UP -> "Priorizar um contato individual e oferecer apoio, sem investigar o relato.";
            case TODAY, THREE_DAYS -> "Convidar o colaborador para uma conversa reservada de acompanhamento.";
        };
    }

    private static String urgencyLabel(AlertUrgency urgency) {
        return switch (urgency) {
            case NORMAL -> "normal";
            case ATTENTION -> "atenção";
            case FOLLOW_UP -> "necessidade de acompanhamento";
            case TODAY -> "hoje";
            case THREE_DAYS -> "em até três dias";
        };
    }

    private static boolean isConsentGranted(String content) {
        String answer = normalizeConsentAnswer(content);
        return !isConsentDeclined(answer)
                && (answer.equals("sim") || answer.equals("autorizo") || answer.startsWith("sim ")
                    || answer.startsWith("autorizo ") || answer.startsWith("pode encaminhar")
                    || answer.startsWith("quero que o rh") || answer.startsWith("quero que rh")
                    || answer.startsWith("quero contato do rh") || answer.startsWith("aceito o contato"));
    }

    private static boolean isConsentDeclined(String content) {
        String answer = normalizeConsentAnswer(content);
        return answer.equals("nao") || answer.startsWith("nao ") || answer.startsWith("nao,")
                || answer.startsWith("nao autorizo") || answer.startsWith("prefiro que o rh nao")
                || answer.startsWith("nao quero");
    }

    private static boolean isExplicitHrContactRequest(String content) {
        String answer = normalizeConsentAnswer(content);
        if (isConsentDeclined(answer) || !answer.contains("rh")) return false;
        boolean asksContact = answer.contains("contato") || answer.contains("chame")
                || answer.contains("chamar") || answer.contains("encaminh")
                || answer.contains("alerta") || answer.contains("pedido");
        boolean authorizes = answer.contains("autoriz") || answer.startsWith("quero que")
                || answer.startsWith("pode encaminhar");
        return asksContact && authorizes;
    }

    private static String normalizeConsentAnswer(String content) {
        return java.text.Normalizer.normalize(content.trim().toLowerCase(java.util.Locale.ROOT),
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static boolean isHrConsentPrompt(String content) {
        String normalized = normalizeConsentAnswer(content);
        boolean asksPermission = normalized.contains("autoriza") || normalized.contains("consent")
                || normalized.contains("permissao") || normalized.contains("posso pedir")
                || normalized.contains("gostaria que eu") || normalized.contains("quer que eu");
        return normalized.contains("rh")
                && asksPermission
                && (normalized.contains("encaminh") || normalized.contains("contato")
                    || normalized.contains("conversa privada") || normalized.contains("pedido"))
                && (normalized.contains("?") || normalized.contains("responda sim ou nao"));
    }

    private static boolean hasExplicitHrConsent(List<ChatEntity> history) {
        boolean awaitingConsent = false;
        for (ChatEntity message : history) {
            if (message.getSender() == MessageSender.BOT) {
                if (isHrConsentPrompt(message.getContent())) awaitingConsent = true;
            } else if (awaitingConsent) {
                if (isConsentGranted(message.getContent())) return true;
                awaitingConsent = false;
            } else if (isExplicitHrContactRequest(message.getContent())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public ChatResponseDto updateChat(Long id, ChatUpdateRequestDto dto, String actorEmail) {
        ChatEntity chat = findOwnedChat(id, actorEmail);
        EmployeeEntity employee = findAuthorizedEmployee(dto.employeeId(), actorEmail);
        if (!chat.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A mensagem não pode ser transferida para outro funcionário");
        }

        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(dto.author().trim());
        chat.setDateSent(dto.dateSent());

        return ChatResponseDto.from(chat);
    }

    @Transactional
    public void deleteChat(Long id, String actorEmail) {
        chatRepository.delete(findOwnedChat(id, actorEmail));
    }

    /** Histórico do próprio funcionário (mais recentes primeiro). */
    @Transactional(readOnly = true)
    public PageResponseDto<ChatResponseDto> findChats(UUID employeeId, Pageable pageable, String actorEmail) {
        int number = pageable.getPageNumber();
        int size = pageable.getPageSize();
        EmployeeEntity employee = employeeId == null
                ? findActorEmployee(actorEmail)
                : findAuthorizedEmployee(employeeId, actorEmail);

        Page<ChatEntity> result = chatRepository.findByEmployeeIdOrderByDateSentDesc(
                employee.getId(), PageRequest.of(number, size));

        return PageResponseDto.from(result, ChatResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ChatResponseDto findChatById(Long id, String actorEmail) {
        return ChatResponseDto.from(findOwnedChat(id, actorEmail));
    }

    private ChatEntity findOwnedChat(Long id, String actorEmail) {
        EmployeeEntity employee = findActorEmployee(actorEmail);
        ChatEntity chat = findChat(id);
        if (!chat.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar as próprias conversas");
        }
        return chat;
    }

    private EmployeeEntity findAuthorizedEmployee(UUID employeeId, String actorEmail) {
        EmployeeEntity employee = findActorEmployee(actorEmail);
        if (!employee.getId().equals(employeeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode acessar as próprias conversas");
        }
        return employee;
    }

    private EmployeeEntity findActorEmployee(String actorEmail) {
        return employeeRepository.findByEmailIgnoreCase(actorEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Somente o funcionário dono da conversa pode acessá-la"));
    }

    private ChatEntity findChat(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
    }

    private static String resolveAuthor(MessageSender sender, EmployeeEntity employee) {
        return sender == MessageSender.BOT ? BOT_AUTHOR : employee.getName();
    }
}
