package com.journeyflow.services;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import com.journeyflow.models.PillProgressId;
import com.journeyflow.models.PillsEntity;
import com.journeyflow.models.ReportsEntity;
import com.journeyflow.models.StoreEntity;
import com.journeyflow.models.enums.AlertDestination;
import com.journeyflow.models.enums.AlertUrgency;
import com.journeyflow.models.enums.ReportKind;
import com.journeyflow.models.enums.ContentSource;
import com.journeyflow.models.enums.MessageSender;
import com.journeyflow.repositories.ChatRepository;
import com.journeyflow.repositories.EmployeeRepository;
import com.journeyflow.repositories.AlertRepository;
import com.journeyflow.repositories.PillProgressRepository;
import com.journeyflow.repositories.PillsRepository;
import com.journeyflow.repositories.ReportsRepository;
import com.journeyflow.repositories.StoreRepository;
import com.journeyflow.services.PillProgressService;
import com.journeyflow.services.AnalysisResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    /** Nome gravado em "author" para mensagens enviadas pelo bot. */
    static final String BOT_AUTHOR = "JourneyFlow";
    private static final String HR_CONSENT_PROMPT = "Identifiquei que talvez seja importante oferecer um acompanhamento humano. Você autoriza que eu peça ao RH para conversar com você em particular? O pedido não incluirá o relato nem nomes de outras pessoas.";
    private static final String HR_CONSENT_REPORT = "Solicitação de contato privado pelo RH, autorizada pelo colaborador.";
    private static final String CONVERSATION_CLOSED = "Obrigado por compartilhar como está sua jornada. A conversa foi concluída; você pode voltar quando quiser.";
    private static final String HR_CONVERSATION_CLOSED = "Obrigado por confirmar. O RH recebeu apenas um pedido para conversar com você em particular.";
    private static final String DECLINED_CONVERSATION_CLOSED = "Entendido. Não encaminhei o pedido. Se mudar de ideia, procure diretamente o canal de RH da empresa.";

    private final ChatRepository chatRepository;
    private final EmployeeRepository employeeRepository;
    private final PillsRepository pillsRepository;
    private final PillProgressRepository pillProgressRepository;
    private final PillProgressService pillProgressService;
    private final ReportsRepository reportsRepository;
    private final StoreRepository storeRepository;
    private final AlertRepository alertRepository;
    private final GeminiService geminiService;
    private final ObjectMapper mapper;
    private final Clock clock;

    /**
     * O DTO de criação não recebe "author": ele é derivado aqui
     * (nome do colaborador para USER, BOT_AUTHOR para BOT), então o cliente não consegue forjá-lo.
     * dateSent é opcional e assume "agora".
     */
    public ChatResponseDto createChat(ChatCreateRequestDto dto) {
        if (dto.sender() != MessageSender.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente mensagens do funcionário podem ser enviadas por este endpoint");
        }
        EmployeeEntity employee = findEmployee(dto.employeeId());
        ChatEntity previousMessage = chatRepository.findFirstByEmployeeIdOrderByDateSentDesc(employee.getId()).orElse(null);
        if (dto.pillId() != null && (previousMessage == null || previousMessage.getSender() != MessageSender.BOT
                || !dto.pillId().equals(previousMessage.getPillId()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não há quiz pendente para essa pílula");
        }

        ChatEntity chat = new ChatEntity();
        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(resolveAuthor(dto.sender(), employee));
        chat.setDateSent(dto.dateSent() == null ? Instant.now(clock) : dto.dateSent());
        chat.setPillId(dto.pillId());

        chatRepository.save(chat);
        if (dto.pillId() != null) {
            boolean correct = answerPillQuiz(employee, dto.pillId(), dto.content());
            String result = correct
                    ? "O backend validou a resposta como correta e concluiu a pílula. Reconheça o progresso sem prometer pontos adicionais."
                    : "O backend validou a resposta como incorreta. Explique com gentileza que ela não está correta e convide o funcionário a tentar novamente, sem revelar o gabarito.";
            return ChatResponseDto.from(replyWithGemini(employee, result, correct ? null : dto.pillId()));
        }

        if (previousMessage != null && previousMessage.getSender() == MessageSender.BOT
                && HR_CONSENT_PROMPT.equals(previousMessage.getContent())) {
            String answer = dto.content().trim().toLowerCase(java.util.Locale.ROOT);
            if (answer.equals("sim") || answer.startsWith("sim, autorizo") || answer.startsWith("sim, pode")
                    || answer.startsWith("sim, quero") || answer.equals("autorizo")) {
                createConfidentialReferral(employee);
                return ChatResponseDto.from(createBotMessage(employee, HR_CONVERSATION_CLOSED));
            }
            if (answer.equals("não") || answer.equals("nao") || answer.startsWith("não autorizo")
                    || answer.startsWith("nao autorizo") || answer.startsWith("não, obrigado")
                    || answer.startsWith("nao, obrigado")) {
                return ChatResponseDto.from(createBotMessage(employee, DECLINED_CONVERSATION_CLOSED));
            }
        }
        return ChatResponseDto.from(replyWithGemini(employee, "", null));
    }

    /** Cria no chat um quiz baseado somente em conteúdo já cadastrado em pills. */
    @Transactional
    public ChatResponseDto startPillQuiz(UUID employeeId) {
        EmployeeEntity employee = findEmployee(employeeId);
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
            return ChatResponseDto.from(createBotMessage(employee, pill.getQuiz_question(), pill.getId()));
        }
        return ChatResponseDto.from(createBotMessage(employee,
                "Ainda não há pílulas disponíveis para um quiz. Você pode continuar compartilhando como está sua jornada."));
    }

    /** A chamada externa ao Gemini ocorre sem transação de banco aberta. */
    public ChatResponseDto finishConversation(UUID employeeId) {
        EmployeeEntity employee = findEmployee(employeeId);
        List<ChatEntity> history = chatRepository.findByEmployeeIdOrderByDateSentAsc(employeeId);
        if (!history.isEmpty() && history.getLast().getSender() == MessageSender.BOT
                && HR_CONSENT_PROMPT.equals(history.getLast().getContent())) {
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
        String transcript = activeHistory.stream().skip(Math.max(0, activeHistory.size() - 30L))
                .map(message -> (message.getSender() == MessageSender.USER ? "Funcionário" : "JourneyFlow")
                        + ": " + message.getContent())
                .reduce((left, right) -> left + "\n" + right).orElse("");
        if (transcript.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conversa ainda não tem mensagens");
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
        if (ReportKind.SUPPORT_REQUEST.name().equals(result.kind())
                || ReportKind.NEGATIVE_NO_REQUEST.name().equals(result.kind())) {
            return ChatResponseDto.from(createBotMessage(employee, HR_CONSENT_PROMPT));
        }
        return ChatResponseDto.from(createBotMessage(employee, CONVERSATION_CLOSED));
    }

    private static boolean isClosedMessage(ChatEntity message) {
        if (message.getSender() != MessageSender.BOT) return false;
        return CONVERSATION_CLOSED.equals(message.getContent())
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
            if (pill.getQuiz_correct() != null && pill.getQuiz_correct() >= 0 && pill.getQuiz_correct() < options.size()) {
                correct = options.get(pill.getQuiz_correct()).equalsIgnoreCase(answer.trim());
            }
        }
        if (correct) {
            PillProgressId id = new PillProgressId(employee.getId(), pill.getId());
            if (!pillProgressRepository.existsById(id)) {
                pillProgressService.completeQuiz(employee.getId(), pill.getId());
            }
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
        List<PillsEntity> learningPills = new ArrayList<>(pillsRepository.findAll(Sort.by("dayFrom").ascending()).stream()
                .filter(pill -> isPillAvailable(employee, pill))
                .filter(pill -> !pillProgressRepository.existsById(new PillProgressId(employee.getId(), pill.getId())))
                .limit(5)
                .toList());
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

    private void createConfidentialReferral(EmployeeEntity employee) {
        if (reportsRepository.existsByEmployeeIdAndOriginalText(employee.getId(), HR_CONSENT_REPORT)) return;
        ReportsEntity report = new ReportsEntity();
        report.setEmployeeId(employee.getId());
        report.setOriginalText(HR_CONSENT_REPORT);
        report.setEditedText(HR_CONSENT_REPORT);
        report.setSummary("O colaborador autorizou um contato privado do RH.");
        report.setTopic("Acompanhamento confidencial");
        report.setKind(ReportKind.SUPPORT_REQUEST);
        report.setSource(ContentSource.TEXT);
        report.setHelpRequest(true);
        report = reportsRepository.save(report);

        AlertEntity alert = new AlertEntity();
        alert.setReport(report);
        StoreEntity store = storeRepository.findById(employee.getStore().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loja não encontrada"));
        alert.setStore(store);
        alert.setDestination(AlertDestination.HR);
        alert.setUrgency(AlertUrgency.TODAY);
        alert.setAlertDate(LocalDateTime.now(clock));
        alert.setAlertTitle("Pedido de contato privado do RH");
        alert.setSubtitle("O colaborador autorizou um contato privado. Não há detalhes do relato neste alerta.");
        alertRepository.save(alert);
    }

    @Transactional
    public ChatResponseDto updateChat(Long id, ChatUpdateRequestDto dto) {
        ChatEntity chat = findChat(id);
        EmployeeEntity employee = findEmployee(dto.employeeId());

        chat.setEmployee(employee);
        chat.setSender(dto.sender());
        chat.setContent(dto.content().trim());
        chat.setSource(dto.source() == null ? ContentSource.TEXT : dto.source());
        chat.setAuthor(dto.author().trim());
        chat.setDateSent(dto.dateSent());

        return ChatResponseDto.from(chat);
    }

    @Transactional
    public void deleteChat(Long id) {
        chatRepository.delete(findChat(id));
    }

    /** Com employeeId: histórico da conversa (mais recentes primeiro). Sem: todas as mensagens, também desc. */
    @Transactional(readOnly = true)
    public PageResponseDto<ChatResponseDto> findChats(UUID employeeId, Pageable pageable) {
        int number = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Page<ChatEntity> result = employeeId == null
                ? chatRepository.findAll(PageRequest.of(number, size, Sort.by(Sort.Direction.DESC, "dateSent")))
                : chatRepository.findByEmployeeIdOrderByDateSentDesc(employeeId, PageRequest.of(number, size));

        return PageResponseDto.from(result, ChatResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ChatResponseDto findChatById(Long id) {
        return ChatResponseDto.from(findChat(id));
    }

    private ChatEntity findChat(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensagem não encontrada"));
    }

    private EmployeeEntity findEmployee(UUID employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
    }

    private static String resolveAuthor(MessageSender sender, EmployeeEntity employee) {
        return sender == MessageSender.BOT ? BOT_AUTHOR : employee.getName();
    }
}
