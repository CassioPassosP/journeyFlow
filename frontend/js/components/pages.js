import { escapeHtml, getStatusConfig } from "../utils/dom.js";

function statusBadge(status) {
  const config = getStatusConfig(status);
  return `<span class="status-badge status-badge--${config.tone}">${escapeHtml(config.label)}</span>`;
}

function pageHeading(title, subtitle, breadcrumb = "") {
  return `
    <div class="page-heading">
      ${breadcrumb ? `<p class="page-heading__breadcrumb">${breadcrumb}</p>` : ""}
      <h1>${title}</h1>
      <p>${subtitle}</p>
    </div>
  `;
}

export function renderDashboardPage(container, data) {
  const firstName = data.user.name.trim().split(/\s+/)[0];
  container.innerHTML = `
    <section class="greeting" aria-labelledby="greeting-title">
      <div class="greeting__copy">
        <span class="greeting__wave" aria-hidden="true">👋</span>
        <div><h1 id="greeting-title">Olá, ${escapeHtml(firstName)}</h1>
          <p>Acompanhe funcionários e alertas da sua unidade.</p></div>
      </div>
      <span class="greeting__art" aria-hidden="true">📊</span>
    </section>
    <div class="dashboard-grid">
      <section id="summary-cards" class="summary-cards" aria-label="Resumo da equipe"></section>
      <section id="status-chart" class="dashboard-panel status-chart" aria-label="Status mensal da equipe"></section>
      <section id="updates-chart" class="dashboard-panel updates-chart" aria-label="Colaboradores com mais atualizações"></section>
      <section id="recent-alerts" class="dashboard-panel recent-alerts" aria-label="Alertas recentes"></section>
    </div>
  `;
}

const EMPLOYEE_STATUS_OPTIONS = [
  { id: "normal", label: "Normal" },
  { id: "attention", label: "Atenção" },
  { id: "help", label: "Precisa de ajuda" }
];

const DEFAULT_SORTS = { name: "asc", interaction: "desc", days: "asc" };
const DEFAULT_SORT_PRIORITY = ["interaction", "days", "name"];

// Antes a ordenação sempre desempatava na ordem fixa interação > dias > nome, então
// escolher "Nome" não mudava nada na prática. Agora o último critério aplicado vira o principal.
function filterEmployees(employees, query, sorts, statuses, priority) {
  const normalizedQuery = query.trim().toLocaleLowerCase("pt-BR");
  const order = [...priority, ...DEFAULT_SORT_PRIORITY.filter((sort) => !priority.includes(sort))];
  const direction = (sort) => (sorts[sort] === "desc" ? -1 : 1);
  const comparisons = {
    // lastInteractionAt vem da API; quem nunca interagiu (0) fica no fim em "mais recentes".
    interaction: (a, b) => (a.lastInteractionAt - b.lastInteractionAt) * direction("interaction"),
    days: (a, b) => (a.days - b.days) * direction("days"),
    name: (a, b) => a.name.localeCompare(b.name, "pt-BR") * direction("name")
  };

  return employees
    .filter((employee) => `${employee.name} ${employee.role}`.toLocaleLowerCase("pt-BR").includes(normalizedQuery))
    .filter((employee) => statuses === null || statuses.includes(employee.status))
    .sort((a, b) => {
      for (const sort of order) {
        const result = comparisons[sort](a, b);
        if (result !== 0) return result;
      }
      return a.name.localeCompare(b.name, "pt-BR");
    });
}

function employeesResultsMarkup(filtered) {
  const rows = filtered.map((employee) => `
    <tr>
      <td><strong>${escapeHtml(employee.name)}</strong><span>${escapeHtml(employee.role)}</span></td>
      <td>${escapeHtml(employee.days)} dias</td>
      <td class="status-cell">${statusBadge(employee.status)}</td>
      <td>${escapeHtml(employee.interaction)}</td>
      <td><a class="button button--muted" href="#employees/${escapeHtml(employee.id)}">Ver detalhes <span aria-hidden="true">↗</span></a></td>
    </tr>
  `).join("");

  return `
    <div class="data-panel__heading"><h2 role="status">${filtered.length} ${filtered.length === 1 ? "Funcionário" : "Funcionários"}</h2><span>encontrados com filtro</span></div>
    <div class="table-scroll"><table class="data-table employees-table">
      <colgroup><col class="employees-table__name-column"><col class="employees-table__days-column"><col class="employees-table__status-column"><col class="employees-table__interaction-column"><col class="employees-table__action-column"></colgroup>
      <thead><tr><th>Nome</th><th>Dias de contrato</th><th>Estado</th><th>Última interação</th><th>Ação</th></tr></thead>
      <tbody>${rows || '<tr><td class="empty-state" colspan="5">Nenhum funcionário encontrado.</td></tr>'}</tbody>
    </table></div>
  `;
}

// Digitar na busca só troca a tabela. Antes a página inteira era recriada a cada tecla
// (campo, botões e diálogos), o que piscava e dependia de devolver o foco ao input na mão.
export function updateEmployeesResults(container, employees, query = "", sorts = DEFAULT_SORTS, statuses = null, priority = DEFAULT_SORT_PRIORITY) {
  const panel = container.querySelector("[data-employees-results]");
  if (!panel) return false;
  panel.innerHTML = employeesResultsMarkup(filterEmployees(employees, query, sorts, statuses, priority));
  return true;
}

export function renderEmployeesPage(container, employees, query = "", sorts = DEFAULT_SORTS, statuses = null, priority = DEFAULT_SORT_PRIORITY) {
  const filtered = filterEmployees(employees, query, sorts, statuses, priority);
  const sortTrigger = (id, label) => `
        <button class="button button--muted sort-trigger filter-trigger" type="button" data-open-dialog="${id}" aria-haspopup="dialog" aria-expanded="false">
          ${label}
          <svg class="filter-trigger__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false"><path d="m4 6 4 4 4-4"/></svg>
        </button>`;

  container.innerHTML = `
    ${pageHeading("Funcionários", "Acompanhe o estado atual de todos os funcionários")}
    <section class="page-toolbar">
      <label class="search-field"><span class="visually-hidden">Buscar funcionário</span>
        <input data-search-employees type="search" placeholder="Buscar funcionário..." autocomplete="off" value="${escapeHtml(query)}">
      </label>
      <div class="employee-sort-controls" role="group" aria-label="Ordenar funcionários por">
        <span class="employee-sort-controls__label">Ordenar por</span>
        ${sortTrigger("employee-name-sort", "Nome")}
        ${sortTrigger("employee-interaction-sort", "Interação")}
        ${sortTrigger("employee-days-sort", "Dias de contrato")}
        <button class="button button--muted sort-trigger filter-trigger${statuses !== null ? " is-active" : ""}" type="button" data-open-dialog="employee-status-filter" aria-haspopup="dialog" aria-expanded="false">
          Estado${statuses !== null ? ` (${statuses.length})` : ""}
          <svg class="filter-trigger__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false"><path d="m4 6 4 4 4-4"/></svg>
        </button>
      </div>
      <a class="button button--primary page-toolbar__action" href="#employees/new"><span aria-hidden="true">＋</span> Funcionário</a>
    </section>
    <section class="data-panel" data-employees-results>${employeesResultsMarkup(filtered)}</section>
    ${["name", "interaction", "days"].map((sort) => {
      const labels = {
        name: { title: "Ordenar por nome", asc: "Crescente (A a Z)", desc: "Decrescente (Z a A)" },
        interaction: { title: "Ordenar por interação", asc: "Crescente (mais antigas)", desc: "Decrescente (mais recentes)" },
        days: { title: "Ordenar por dias de contrato", asc: "Crescente (menor para maior)", desc: "Decrescente (maior para menor)" }
      }[sort];
      return `
        <dialog class="filter-dialog" data-filter-dialog="employee-${sort}-sort" aria-label="${labels.title}">
          <form method="dialog">
            <button class="dialog-close" aria-label="Fechar">×</button>
            <h2>${labels.title}</h2>
            <label><input type="radio" name="employee-${sort}-direction" data-employee-sort-direction value="asc"${sorts[sort] === "asc" ? " checked" : ""}> ${labels.asc}</label>
            <label><input type="radio" name="employee-${sort}-direction" data-employee-sort-direction value="desc"${sorts[sort] === "desc" ? " checked" : ""}> ${labels.desc}</label>
            <button class="button button--primary" type="button" data-apply-employee-sort="${sort}">Aplicar</button>
          </form>
        </dialog>
      `;
    }).join("")}
    <dialog class="filter-dialog" data-filter-dialog="employee-status-filter" aria-label="Filtrar por estado">
      <form method="dialog">
        <button class="dialog-close" aria-label="Fechar">×</button>
        <h2>Filtrar por estado</h2>
        <button class="button button--muted" type="button" data-select-all-employee-status>Selecionar todos</button>
        ${EMPLOYEE_STATUS_OPTIONS.map(({ id, label }) => `
          <label><input type="checkbox" data-employee-status-filter value="${id}"${statuses === null || statuses.includes(id) ? " checked" : ""}> ${label}</label>
        `).join("")}
        <button class="button button--primary" type="button" data-apply-employee-status>Aplicar</button>
      </form>
    </dialog>
  `;
}

export function renderEmployeeForm(container, stores = [], defaultStoreId = "") {
  container.innerHTML = `
    ${pageHeading("Cadastrar funcionário", "Cadastre os dados do funcionário e defina a senha que ele usará para acessar o JourneyFlow.", '<a href="#employees">Funcionários</a> &gt;')}
    <form class="employee-form" data-employee-form>
      <section class="form-section">
        <h2>Dados do funcionário</h2>
        <div class="form-grid form-grid--two">
          <label>Nome completo<input name="name" autocomplete="name" required></label>
          <label>E-mail<input name="email" type="email" autocomplete="email" required></label>
          <label>Senha inicial<input name="password" type="password" minlength="8" maxlength="72" autocomplete="new-password" required></label>
        </div>
      </section>
      <section class="form-section">
        <h2>Dados profissionais</h2>
        <div class="form-grid">
          <label>Cargo / função<input name="position" required></label>
          <label>Unidade<select name="storeId" required>${stores.map((store) => `
            <option value="${escapeHtml(store.id)}"${String(store.id) === String(defaultStoreId) ? " selected" : ""}>${escapeHtml(store.storeName)}</option>
          `).join("")}</select></label>
          <label>Data de contratação<input name="hiredAt" type="date" required></label>
        </div>
      </section>
      <fieldset class="form-section">
        <legend>Acompanhamento inicial</legend>
        <p>Estado inicial</p>
        <div class="status-options">
          <label><input type="radio" name="status" value="normal" required><span><i class="status-dot status-dot--success"></i>Normal<small>Acolhimento e rotina de integração</small></span></label>
          <label><input type="radio" name="status" value="attention"><span><i class="status-dot status-dot--warning"></i>Atenção<small>Um olhar mais próximo na adaptação</small></span></label>
          <label><input type="radio" name="status" value="help"><span><i class="status-dot status-dot--danger"></i>Necessidade de acompanhamento<small>Apoio individual mais frequente</small></span></label>
        </div>
        <label class="employee-form__checkbox"><input type="checkbox" name="inLearningPath" checked> Incluir na trilha de aprendizagem</label>
        <label class="employee-form__checkbox"><input type="checkbox" name="consent"> Consentimento registrado para tratamento dos dados</label>
      </fieldset>
      <div class="form-actions"><a class="button button--muted" href="#employees">Cancelar</a><button class="button button--primary" type="submit">Cadastrar funcionário</button></div>
    </form>
  `;
}

export function renderEmployeeDetails(container, employee, alerts) {
  const employeeAlerts = alerts.filter((alert) => alert.employeeId === employee.id);
  const pendingCount = employeeAlerts.filter((alert) => !alert.confirmed).length;
  const recentAlerts = employeeAlerts.map((alert) => `
    <article class="detail-alert"><div>${statusBadge(alert.status)}<p>${escapeHtml(alert.message)}</p><small>${escapeHtml(alert.when)}</small></div></article>
  `).join("") || '<p class="empty-state">Nenhum alerta registrado para este funcionário.</p>';
  // As sugestões vêm dos alertas da API. Antes a tela exibia um texto fixo para todo mundo.
  const suggestions = employeeAlerts
    .filter((alert) => alert.suggestion)
    .slice(0, 3)
    .map((alert) => `<div class="suggestion-card">${escapeHtml(alert.suggestion)}</div>`)
    .join("") || '<div class="suggestion-card">Ainda não há sugestões de acompanhamento para este funcionário.</div>';
  const hiredAt = employee.hiredAt ? new Date(`${employee.hiredAt}T00:00:00`).toLocaleDateString("pt-BR") : "Não informada";

  container.innerHTML = `
    ${pageHeading("Detalhes do funcionário", "Acompanhe o estado atual de todos os funcionários", `<a href="#employees">Funcionários</a> &gt; ${escapeHtml(employee.name)}`)}
    <section class="employee-summary">
      <span class="employee-summary__avatar">${escapeHtml(employee.name.split(/\s+/).map((part) => part[0]).slice(0, 2).join("").toLocaleUpperCase("pt-BR"))}</span>
      <div><h2>${escapeHtml(employee.name)}</h2><p>${escapeHtml(employee.role)}</p></div>
      <div class="employee-summary__days"><span>Dias de contrato</span><strong>${escapeHtml(employee.days)}</strong></div>
      ${statusBadge(employee.status)}
    </section>
    <div class="employee-metrics">
      <article><span>Unidade</span><strong>${escapeHtml(employee.unit || "Não informada")}</strong></article>
<<<<<<< HEAD
      <article><span>Data de contratação</span><strong>${escapeHtml(hiredAt)}</strong></article>
      <article><span>Alertas registrados</span><strong>${employeeAlerts.length} <small>${pendingCount} sem confirmar</small></strong></article>
      <article><span>Última interação</span><strong>${escapeHtml(employee.interaction)}</strong></article>
    </div>
    <div class="employee-details-grid">
      <section class="data-panel"><div class="data-panel__heading"><h2>Alertas recentes</h2></div>${recentAlerts}</section>
      <section class="data-panel suggestions"><h2>Sugestões de acompanhamento</h2><p>Sugestões de ações para este funcionário.</p>${suggestions}</section>
=======
      <article><span>Data de contratação</span><strong>${escapeHtml(employee.hiredAt ? new Date(`${employee.hiredAt}T00:00:00`).toLocaleDateString("pt-BR") : "Não informada")}</strong></article>
    </div>
    <div class="employee-details-grid">
      <section class="data-panel"><div class="data-panel__heading"><h2>Alertas recentes</h2></div>${recentAlerts}</section>
      <section class="data-panel suggestions"><h2>Sugestões de acompanhamento</h2><p>${employeeAlerts.length
        ? escapeHtml(employeeAlerts[0].message)
        : "Nenhuma sugestão de acompanhamento registrada."}</p></section>
>>>>>>> 92d01f6 (AAAAAAAAAAAAAAaaaAaAaAaAAAaaaaaaaaaAaAAAAAAAAAAaaaaaaaaaaaaaaAAAAAAAAaAAAAaA)
    </div>
  `;
}

function filterAlerts(alerts, query, filters) {
  const statuses = filters.statuses || [];
  const normalizedQuery = query.trim().toLocaleLowerCase("pt-BR");
  const filtered = alerts.filter((alert) => {
    const searchText = `${alert.message} ${alert.author}`.toLocaleLowerCase("pt-BR");
    const matchesQuery = searchText.includes(normalizedQuery);
    const matchesStatus = statuses.length === 0 || statuses.includes(alert.status);
    const matchesConfirmation = filters.confirmation === undefined || alert.confirmed === filters.confirmation;
    return matchesQuery && matchesStatus && matchesConfirmation;
  });
  if (!filters.sort) return filtered;
  // Antes a ordenação lia o texto "dd/mm/aaaa - hh:mm" e lançava erro (quebrando a página inteira)
  // quando a data vinha como "Data não informada". Agora usa o timestamp numérico.
  return [...filtered].sort((a, b) => (filters.sort === "oldest" ? a.timestamp - b.timestamp : b.timestamp - a.timestamp));
}

function alertsResultsMarkup(filtered) {
  const rows = filtered.map((alert) => `
    <tr>
      <td><strong>${escapeHtml(alert.message)}</strong><span>${escapeHtml(alert.author)}</span></td>
      <td class="status-cell">${statusBadge(alert.status)}</td>
      <td>${escapeHtml(alert.when)}</td>
      <td>${alert.confirmed
        ? '<button class="button button--muted" type="button" disabled>Confirmado</button>'
        : `<button class="button button--success" type="button" data-confirm-alert="${escapeHtml(alert.id)}"><svg class="button__check" viewBox="0 0 20 20" aria-hidden="true" focusable="false"><path d="m4 10 4 4 8-8"/></svg>Confirmar</button>`}</td>
    </tr>
  `).join("");

  return `
    <div class="data-panel__heading"><h2 role="status">${filtered.length} ${filtered.length === 1 ? "Alerta" : "Alertas"}</h2><span>encontrados com filtro</span></div>
    <div class="table-scroll"><table class="data-table alerts-table">
      <colgroup><col class="alerts-table__message-column"><col class="alerts-table__status-column"><col class="alerts-table__interaction-column"><col class="alerts-table__action-column"></colgroup>
      <thead><tr><th>Alerta</th><th class="status-cell">Estado</th><th>Última interação</th><th>Ação</th></tr></thead>
      <tbody>${rows || '<tr><td class="empty-state" colspan="4">Nenhum alerta encontrado.</td></tr>'}</tbody>
    </table></div>
  `;
}

export function updateAlertsResults(container, alerts, query = "", filters = {}) {
  const panel = container.querySelector("[data-alerts-results]");
  if (!panel) return false;
  panel.innerHTML = alertsResultsMarkup(filterAlerts(alerts, query, filters));
  return true;
}

export function renderAlertsPage(container, alerts, query = "", filters = {}) {
  const statuses = filters.statuses || [];
  const filtered = filterAlerts(alerts, query, filters);
  const statusLabels = { attention: "Atenção", help: "Precisa de ajuda" };
  const selectedStatus = statuses.map((status) => statusLabels[status] || status).join(", ");
  const chevron = '<svg class="filter-trigger__icon" viewBox="0 0 16 16" aria-hidden="true" focusable="false"><path d="m4 6 4 4 4-4"/></svg>';

  container.innerHTML = `
    ${pageHeading("Alertas", "Acompanhe o histórico de alertas da unidade.")}
    <section class="page-toolbar page-toolbar--alerts">
      <label class="search-field"><span class="visually-hidden">Buscar funcionário</span><input data-search-alerts type="search" placeholder="Buscar funcionário..." autocomplete="off" value="${escapeHtml(query)}"></label>
      <button class="button button--muted filter-trigger${statuses.length ? " is-active" : ""}" type="button" data-open-dialog="status-filter" aria-haspopup="dialog" aria-expanded="false">Estado ${chevron}</button>
      <button class="button button--muted filter-trigger${filters.sort ? " is-active" : ""}" type="button" data-open-dialog="date-filter" aria-haspopup="dialog" aria-expanded="false">Data/Hora ${chevron}</button>
      <button class="button button--muted filter-trigger${filters.confirmation !== undefined ? " is-active" : ""}" type="button" data-open-dialog="confirmation-filter" aria-haspopup="dialog" aria-expanded="false">Filtros</button>
      <button class="button button--muted" type="button" data-clear-filters>Limpar</button>
      ${statuses.length ? `<span class="filter-chip">${escapeHtml(selectedStatus)} <button type="button" data-clear-status aria-label="Remover filtro de estado">×</button></span>` : ""}
      ${filters.confirmation !== undefined ? `<span class="filter-chip">${filters.confirmation ? "Confirmados" : "Não confirmados"} <button type="button" data-clear-confirmation aria-label="Remover filtro de confirmação">×</button></span>` : ""}
    </section>
    <section class="data-panel" data-alerts-results>${alertsResultsMarkup(filtered)}</section>
    <dialog class="filter-dialog" data-filter-dialog="status-filter" aria-label="Estado"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Estado</h2><label><input type="checkbox" data-status-filter value="attention"${statuses.includes("attention") ? " checked" : ""}> Atenção</label><label><input type="checkbox" data-status-filter value="help"${statuses.includes("help") ? " checked" : ""}> Precisa de ajuda</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
    <dialog class="filter-dialog" data-filter-dialog="confirmation-filter" aria-label="Filtros"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Filtros</h2><label><input type="radio" name="confirmation" data-confirmation-filter value="false"${filters.confirmation === false ? " checked" : ""}> Não confirmados</label><label><input type="radio" name="confirmation" data-confirmation-filter value="true"${filters.confirmation === true ? " checked" : ""}> Confirmados</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
    <dialog class="filter-dialog" data-filter-dialog="date-filter" aria-label="Ordenar por data"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Ordenar por data</h2><label><input type="radio" name="date-sort" data-date-sort value="newest"${filters.sort !== "oldest" ? " checked" : ""}> Mais recentes</label><label><input type="radio" name="date-sort" data-date-sort value="oldest"${filters.sort === "oldest" ? " checked" : ""}> Mais antigas</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
  `;
}

export function renderSettingsPage(container, user, theme) {
  container.innerHTML = `
    ${pageHeading("Configurações", "Gerencie suas preferências e os dados da sua conta.")}
    <div class="settings-grid">
      <form class="settings-panel" data-profile-form>
        <h2>Dados do perfil</h2>
        <p>Atualize o nome e o e-mail salvos na sua conta.</p>
        <label>Nome completo<input name="name" autocomplete="name" value="${escapeHtml(user.name)}" required></label>
        <label>E-mail<input name="email" type="email" autocomplete="email" value="${escapeHtml(user.email)}" required></label>
        <div class="settings-panel__footer">
          <span class="settings-message" data-profile-message role="status" aria-live="polite"></span>
          <button class="button button--primary" type="submit">Salvar alterações</button>
        </div>
      </form>
      <section class="settings-panel" aria-labelledby="appearance-heading">
        <h2 id="appearance-heading">Aparência</h2>
        <p>Escolha como o JourneyFlow aparece neste dispositivo.</p>
        <fieldset class="theme-options">
          <legend>Tema</legend>
          <label><input type="radio" name="theme" value="light"${theme === "light" ? " checked" : ""}><span>Claro</span></label>
          <label><input type="radio" name="theme" value="dark"${theme === "dark" ? " checked" : ""}><span>Escuro</span></label>
        </fieldset>
      </section>
      <section class="settings-panel settings-panel--password">
        <h2>Redefinir senha</h2>
        <p>A alteração de senha ficará disponível quando a autenticação segura do backend estiver integrada. Este protótipo não solicita nem armazena senhas.</p>
        <label>Senha atual<input type="password" autocomplete="off" disabled></label>
        <label>Nova senha<input type="password" autocomplete="off" disabled></label>
        <label>Confirmar nova senha<input type="password" autocomplete="off" disabled></label>
        <button class="button button--muted" type="button" disabled>Indisponível neste protótipo</button>
      </section>
    </div>
  `;
}