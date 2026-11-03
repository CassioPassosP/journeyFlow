import { escapeHtml } from "../utils/dom.js";

function points(value) {
  return `${Number(value).toLocaleString("pt-BR")} pontos`;
}

function formatCompletionDate(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "data não informada" : new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
}

function employeeCharacter(employee) {
  const gender = String(employee.gender || employee.sex || "").toLocaleLowerCase("pt-BR");
  return ["f", "female", "woman", "feminino", "mulher"].includes(gender)
    ? "assets/employee-girl.png"
    : "assets/employee-boy.png";
}

function pageHeading(title, subtitle) {
  return `
    <header class="employee-page-heading">
      <h1>${title}</h1>
      <span>${subtitle}</span>
    </header>
  `;
}

function historyItems(history, limit = history.length) {
  return history
    .filter((item) => !item.label.toLocaleLowerCase("pt-BR").includes("sugest"))
    .slice(0, limit)
    .map((item) => `
    <li class="employee-history-item">
      <span class="employee-history-item__icon" aria-hidden="true">${escapeHtml(item.emoji)}</span>
      <span class="employee-history-item__copy"><strong>${escapeHtml(item.label)}</strong><small>${escapeHtml(item.date)}</small></span>
      ${item.points > 0 ? `<strong class="employee-history-item__points">+${escapeHtml(item.points)}</strong>` : ""}
    </li>
    `).join("");
}

function rewardsList(rewards, balance, redeemedRewards, compact = false) {
  const items = compact ? rewards.slice(0, 3) : rewards;
  return items.map((reward) => {
    const redeemed = redeemedRewards.includes(reward.id);
    const available = balance >= reward.points;
    return `
      <article class="employee-reward-card">
        <span class="employee-reward-card__emoji" aria-hidden="true">${escapeHtml(reward.emoji)}</span>
        <h3>${escapeHtml(reward.name)}</h3>
        <p>🟡 ${points(reward.points)}</p>
        <button type="button" class="employee-button${available && !redeemed ? " employee-button--primary" : ""}" data-redeem-reward="${escapeHtml(reward.id)}"${available && !redeemed ? "" : " disabled"}>
          ${redeemed ? "Resgatado" : available ? "Resgatar" : "Junte mais pontos"}
        </button>
      </article>
    `;
  }).join("");
}

function conversation(messages, { compact = false } = {}) {
  const visibleMessages = compact ? messages.slice(-4) : messages;
  return `
    <section class="employee-conversation" aria-label="Conversa com JourneyFlow">
      ${compact ? "" : `
        <div class="employee-conversation__heading">
          <span class="employee-bot-avatar" aria-hidden="true">🤖</span>
          <div><h2>Converse com a JourneyFlow</h2><p>Responda às perguntas e ganhe pontos. Compartilhe suas ideias conosco.</p></div>
        </div>
      `}
      <div class="employee-chat-log" aria-live="polite">
        ${visibleMessages.map((message) => `
          <article class="employee-chat-message employee-chat-message--${message.author === "employee" ? "employee" : "bot"}">
            ${message.author === "bot" ? '<span class="employee-bot-avatar" aria-hidden="true">🤖</span>' : ""}
            <div class="employee-chat-message__body">
              <p>${escapeHtml(message.text)}</p>
              <time>${escapeHtml(message.time)}</time>
              ${message.answers ? `<div class="employee-chat-answers">${message.answers.map((answer) => `<button type="button" data-employee-answer="${escapeHtml(answer)}">${escapeHtml(answer)}</button>`).join("")}</div>` : ""}
              ${message.actions ? `<div class="employee-chat-actions">${message.actions.map((action) => `<button type="button" data-employee-action="${escapeHtml(action)}">${escapeHtml(action)}</button>`).join("")}</div>` : ""}
            </div>
          </article>
        `).join("")}
      </div>
      <form class="employee-chat-compose" data-employee-chat-form>
        <label class="visually-hidden" for="employee-message">Digite sua mensagem</label>
        <input id="employee-message" name="message" maxlength="500" placeholder="Digite sua mensagem..." autocomplete="off" required>
        <button type="submit" aria-label="Enviar mensagem">➤</button>
      </form>
    </section>
  `;
}

function renderHome(container, employee, messages, notice, redeemedRewards) {
  container.innerHTML = `
    <div class="employee-dashboard">
      <div class="employee-main-column">
        <section class="employee-welcome">
          <div><span class="employee-welcome__wave" aria-hidden="true">👋</span><h1>Olá, ${escapeHtml(employee.name.split(/\s+/)[0])}!</h1>
            <p>Responda às perguntas para ganhar pontos e trocar por recompensas no seu trabalho.</p></div>
          <div class="employee-welcome__art" aria-hidden="true"><img class="employee-welcome__character" src="${employeeCharacter(employee)}" alt=""></div>
        </section>
        ${conversation(messages, { compact: true })}
        <section class="employee-tips">
          <h2>👍 Como participar</h2>
          <div><article><span>🗨️</span><p><strong>Responda perguntas</strong><small>Ganhe pontos ao conversar</small></p></article>
            <article><span>⭐</span><p><strong>Conte sua experiência</strong><small>Sua opinião faz a diferença</small></p></article></div>
        </section>
      </div>
      <aside class="employee-side-column">
        <a class="employee-points-card" href="#employee/progress"><span aria-hidden="true">⭐</span><div><small>Meus pontos</small><strong>${escapeHtml(employee.points)} <i aria-hidden="true">›</i></strong></div></a>
        <section class="employee-side-card">
          <header><h2>🎁 Recompensas disponíveis</h2><a href="#employee/rewards">Ver todas <span aria-hidden="true">→</span></a></header>
          <div class="employee-rewards-grid">${rewardsList(employee.rewards, employee.points, redeemedRewards, true)}</div>
        </section>
        <a class="employee-continue" href="#employee/conversation"><span aria-hidden="true">🎯</span><span><strong>Continue interagindo!</strong><small>Responda às perguntas e ganhe pontos. Compartilhe suas ideias com a equipe.</small></span><i aria-hidden="true">›</i></a>
        <section class="employee-side-card employee-history-card">
          <header><h2>◷ Atividades recentes</h2></header>
          <ul>${historyItems(employee.history, 5)}</ul>
        </section>
        ${notice ? `<p class="employee-notice" role="status">${escapeHtml(notice)}</p>` : ""}
      </aside>
    </div>
  `;
}

function renderRewards(container, employee, notice, redeemedRewards) {
  container.innerHTML = `
    ${pageHeading("Minhas recompensas", "Troque seus pontos por recompensas especiais.")}
    <section class="employee-points-banner"><span aria-hidden="true">⭐</span><div><small>Seu saldo disponível</small><strong>${escapeHtml(points(employee.points))}</strong></div><a class="employee-button" href="#employee/progress">Como ganhar pontos</a></section>
    ${notice ? `<p class="employee-notice" role="status">${escapeHtml(notice)}</p>` : ""}
    <section class="employee-rewards-page"><h2>Recompensas disponíveis</h2><div class="employee-rewards-grid">${rewardsList(employee.rewards, employee.points, redeemedRewards)}</div></section>
  `;
}

function renderProgress(container, employee) {
  const totalEarned = employee.history.reduce((total, entry) => total + entry.points, 0);
  const nextReward = employee.rewards.find((reward) => reward.points > employee.points);
  const progress = nextReward ? Math.min(100, Math.round((employee.points / nextReward.points) * 100)) : 100;
  container.innerHTML = `
    ${pageHeading("Meu progresso", "Acompanhe seus pontos e continue avançando.")}
    <section class="employee-progress-summary"><article><span>⭐</span><small>Saldo atual</small><strong>${escapeHtml(points(employee.points))}</strong></article><article><span>🎉</span><small>Pontos conquistados</small><strong>${escapeHtml(points(totalEarned))}</strong></article><article><span>💬</span><small>Interações registradas</small><strong>${escapeHtml(employee.history.length)}</strong></article></section>
    <section class="employee-progress-panel"><h2>${nextReward ? `Sua próxima recompensa: ${escapeHtml(nextReward.name)}` : "Você desbloqueou todas as recompensas!"}</h2><div class="employee-progress-track" role="progressbar" aria-label="Progresso para a próxima recompensa" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100"><span style="width:${progress}%"></span></div><p>${escapeHtml(employee.points)} de ${escapeHtml(nextReward?.points || employee.points)} pontos</p></section>
    <section class="employee-progress-panel"><h2>Como ganhar mais pontos</h2><ul class="employee-ways-list"><li>🗨️ Responda às perguntas da conversa <strong>+10 pontos</strong></li><li>⭐ Conte como foi sua experiência <strong>+20 pontos</strong></li></ul></section>
  `;
}

function parseQuizOptions(value) {
  try {
    const options = JSON.parse(value);
    if (Array.isArray(options) && options.every((option) => typeof option === "string")) {
      return options;
    }
  } catch {
    return String(value || "").split(/\r?\n|[;,]/).map((option) => option.trim()).filter(Boolean);
  }
  throw new Error("As opções deste questionário estão em um formato inválido.");
}

function daysSince(startDate) {
  if (!startDate) return 0;
  const start = new Date(`${startDate}T00:00:00`);
  if (Number.isNaN(start.getTime())) return 0;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.max(0, Math.floor((today.getTime() - start.getTime()) / 86400000));
}

function renderPills(container, data, notice) {
  const completedPillIds = new Set(data.pillProgress.map((progress) => String(progress.pillId)));
  const elapsedDays = daysSince(data.currentEmployee.startDate);
  const pills = [...data.pills].sort((a, b) => a.dayFrom - b.dayFrom || a.id - b.id);
  const completedCount = pills.filter((pill) => completedPillIds.has(String(pill.id))).length;
  const cards = pills.map((pill) => {
    const completed = completedPillIds.has(String(pill.id));
    const available = Number(pill.dayFrom) <= elapsedDays;
    const options = parseQuizOptions(pill.quizOptions);
    const quizCorrect = Number(pill.quizCorrect);
    if (!Number.isInteger(quizCorrect) || quizCorrect < 0 || quizCorrect >= options.length) {
      throw new Error(`O gabarito da pílula "${pill.title}" está fora das opções disponíveis.`);
    }
    return `
      <article class="employee-pill-card${completed ? " employee-pill-card--completed" : ""}">
        <header class="employee-pill-card__heading">
          <span class="employee-pill-card__icon" aria-hidden="true">${completed ? "✓" : "💊"}</span>
          <div><p>Dia ${escapeHtml(pill.dayFrom)}</p><h2>${escapeHtml(pill.title)}</h2></div>
          <span class="employee-pill-card__status">${completed ? "Concluída" : available ? "Disponível" : "Bloqueada"}</span>
        </header>
        <p class="employee-pill-card__content">${escapeHtml(pill.content)}</p>
        ${completed
          ? `<p class="employee-pill-card__feedback">Concluída em ${escapeHtml(formatCompletionDate(data.pillProgress.find((progress) => String(progress.pillId) === String(pill.id))?.completedAt))}</p>`
          : available
            ? `<form class="employee-pill-quiz" data-pill-quiz="${escapeHtml(pill.id)}" data-correct-answer="${quizCorrect}">
                <fieldset><legend>${escapeHtml(pill.quizQuestion)}</legend>
                  ${options.map((option, index) => `<label><input type="radio" name="answer" value="${index}" required><span>${escapeHtml(option)}</span></label>`).join("")}
                </fieldset>
                <p class="employee-pill-quiz__feedback" data-pill-feedback role="status" hidden></p>
                <button class="employee-button employee-button--primary" type="submit">Responder e concluir</button>
              </form>`
            : `<p class="employee-pill-card__locked">Disponível a partir do dia ${escapeHtml(pill.dayFrom)} da sua jornada.</p>`}
      </article>
    `;
  }).join("");

  container.innerHTML = `
    ${pageHeading("Pílulas de conhecimento", "Aprenda no seu ritmo e acompanhe as pílulas concluídas.")}
    <section class="employee-pills-overview">
      <span aria-hidden="true">💊</span>
      <div><strong>${completedCount} de ${pills.length}</strong><small>pílulas concluídas</small></div>
      <div class="employee-pills-overview__track" role="progressbar" aria-label="Progresso das pílulas" aria-valuenow="${pills.length ? Math.round((completedCount / pills.length) * 100) : 0}" aria-valuemin="0" aria-valuemax="100">
        <span style="width:${pills.length ? (completedCount / pills.length) * 100 : 0}%"></span>
      </div>
    </section>
    ${notice ? `<p class="employee-notice" role="status">${escapeHtml(notice)}</p>` : ""}
    <section class="employee-pills-list" aria-label="Pílulas de conhecimento">
      ${cards || '<p class="employee-pill-empty">Ainda não há pílulas de conhecimento disponíveis.</p>'}
    </section>
  `;
}

export function renderEmployeePage(container, data, page, messages, notice, redeemedRewards) {
  const employee = { ...data.employee, ...data.user, role: data.employee.role, name: data.user.name, email: data.user.email };
  const employeeMessages = messages.map((message) => ({
    ...message,
    text: message.text.replaceAll("{role}", employee.role)
  }));
  if (page === "conversation") {
    container.innerHTML = `${pageHeading("Conversa", "Compartilhe como está sendo sua jornada de trabalho.")}${conversation(employeeMessages)}`;
  } else if (page === "rewards") {
    renderRewards(container, employee, notice, redeemedRewards);
  } else if (page === "progress") {
    renderProgress(container, employee);
  } else if (page === "pills") {
    renderPills(container, data, notice);
  } else {
    renderHome(container, employee, employeeMessages, notice, redeemedRewards);
  }
}
