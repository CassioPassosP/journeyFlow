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
      <h1 id="greeting-title">${escapeHtml(data.greeting.title || "Bom dia")}, ${escapeHtml(firstName)}</h1>
      <p>${escapeHtml(data.greeting.subtitle)}</p>
    </section>
    <div class="dashboard-grid">
      <section id="summary-cards" class="summary-cards" aria-label="Resumo da equipe"></section>
      <section id="status-chart" class="dashboard-panel status-chart" aria-label="Status mensal da equipe"></section>
      <section id="updates-chart" class="dashboard-panel updates-chart" aria-label="Colaboradores com mais atualizações"></section>
      <section id="recent-alerts" class="dashboard-panel recent-alerts" aria-label="Alertas recentes"></section>
    </div>
  `;
}

export function renderEmployeesPage(container, employees, query = "", sort = "name") {
  const filtered = employees
    .filter((employee) => `${employee.name} ${employee.role}`.toLocaleLowerCase("pt-BR").includes(query.toLocaleLowerCase("pt-BR")))
    .sort((a, b) => sort === "days" ? a.days - b.days : a.name.localeCompare(b.name, "pt-BR"));
  const rows = filtered.map((employee) => `
    <tr>
      <td><strong>${escapeHtml(employee.name)}</strong><span>${escapeHtml(employee.role)}</span></td>
      <td>${escapeHtml(employee.days)} dias</td>
      <td>${statusBadge(employee.status)}</td>
      <td>${escapeHtml(employee.interaction)}</td>
      <td><a class="button button--muted" href="#employees/${escapeHtml(employee.id)}">Ver detalhes <span aria-hidden="true">↗</span></a></td>
    </tr>
  `).join("");

  container.innerHTML = `
    ${pageHeading("Funcionários", "Acompanhe o estado atual de todos os funcionários")}
    <section class="page-toolbar">
      <label class="search-field"><span class="visually-hidden">Buscar funcionário</span>
        <input data-search-employees type="search" placeholder="Buscar funcionário..." value="${escapeHtml(query)}">
      </label>
      <label class="visually-hidden" for="employee-sort">Ordenar funcionários</label>
      <select id="employee-sort" data-sort-employees><option value="name"${sort === "name" ? " selected" : ""}>Ordenar por nome</option><option value="days"${sort === "days" ? " selected" : ""}>Dias de contrato</option></select>
      <a class="button button--primary page-toolbar__action" href="#employees/new"><span aria-hidden="true">＋</span> Funcionário</a>
    </section>
    <section class="data-panel">
      <div class="data-panel__heading"><h2>${filtered.length} Funcionários</h2><span>encontrados com filtro</span></div>
      <div class="table-scroll"><table class="data-table">
        <thead><tr><th>Nome</th><th>Dias de contrato</th><th>Estado</th><th>Última interação</th><th>Ação</th></tr></thead>
        <tbody>${rows || '<tr><td class="empty-state" colspan="5">Nenhum funcionário encontrado.</td></tr>'}</tbody>
      </table></div>
    </section>
  `;
}

export function renderEmployeeForm(container) {
  container.innerHTML = `
    ${pageHeading("Cadastrar funcionário", "Preencha manualmente as informações para iniciar o acompanhamento dos primeiros 90 dias.", '<a href="#employees">Funcionários</a> &gt;')}
    <form class="employee-form" data-employee-form>
      <section class="form-section">
        <h2>Dados do funcionário</h2>
        <div class="form-grid form-grid--two">
          <label>Nome completo<input name="name" autocomplete="name" required></label>
          <label>WhatsApp<input name="whatsapp" type="tel" autocomplete="tel" placeholder="(11) 99999-9999"></label>
        </div>
      </section>
      <section class="form-section">
        <h2>Dados profissionais</h2>
        <div class="form-grid">
          <label>Cargo / funcao<input name="role" required></label>
          <label>Unidade<input name="unit" value="Loja Silva so - 1020" required></label>
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
      </fieldset>
      <div class="form-actions"><a class="button button--muted" href="#employees">Cancelar</a><button class="button button--primary" type="submit">Cadastrar funcionário</button></div>
    </form>
  `;
}

export function renderEmployeeDetails(container, employee, alerts) {
  const employeeAlerts = alerts.filter((alert) => alert.employeeId === employee.id);
  const recentAlerts = employeeAlerts.map((alert) => `
    <article class="detail-alert"><div>${statusBadge(alert.status)}<p>${escapeHtml(alert.message)}</p><small>${escapeHtml(alert.when)}</small></div></article>
  `).join("") || '<p class="empty-state">Nenhum alerta registrado para este funcionário.</p>';

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
      <article><span>Data de contratação</span><strong>${escapeHtml(employee.hiredAt ? new Date(`${employee.hiredAt}T00:00:00`).toLocaleDateString("pt-BR") : "Não informada")}</strong></article>
      <article><span>Atrasos</span><strong>5 <small>neste mês</small></strong></article>
      <article><span>Faltas</span><strong>3 <small>neste mês</small></strong></article>
    </div>
    <div class="employee-details-grid">
      <section class="data-panel"><div class="data-panel__heading"><h2>Alertas recentes</h2></div>${recentAlerts}</section>
      <section class="data-panel suggestions"><h2>Sugestões de acompanhamento</h2><p>Sugestões de ações para este funcionário.</p><div class="suggestion-card">Sugiro uma reunião privada para entender se o volume de trabalho está excessivo ou se há problemas pessoais.</div></section>
    </div>
  `;
}

export function renderAlertsPage(container, alerts, query = "", filters = {}) {
  const statuses = filters.statuses || [];
  let filtered = alerts.filter((alert) => {
    const searchText = `${alert.message} ${alert.author}`.toLocaleLowerCase("pt-BR");
    const matchesQuery = searchText.includes(query.toLocaleLowerCase("pt-BR"));
    const matchesStatus = statuses.length === 0 || statuses.includes(alert.status);
    const matchesConfirmation = filters.confirmation === undefined || alert.confirmed === filters.confirmation;
    return matchesQuery && matchesStatus && matchesConfirmation;
  });
  if (filters.sort) {
    const timestamp = (value) => {
      const match = value.match(/^(\d{2})\/(\d{2})\/(\d{4}) - (\d{1,2}):(\d{2})$/);
      if (!match) throw new Error(`Data de alerta invalida: ${value}`);
      return new Date(`${match[3]}-${match[2]}-${match[1]}T${match[4].padStart(2, "0")}:${match[5]}:00`).getTime();
    };
    filtered = [...filtered].sort((a, b) => {
      const dateA = timestamp(a.when);
      const dateB = timestamp(b.when);
      return filters.sort === "oldest" ? dateA - dateB : dateB - dateA;
    });
  }
  const rows = filtered.map((alert) => `
    <tr>
      <td><strong>${escapeHtml(alert.message)}</strong><span>${escapeHtml(alert.author)}</span></td>
      <td>${statusBadge(alert.status)}</td>
      <td>${escapeHtml(alert.when)}</td>
      <td>${alert.confirmed
        ? '<button class="button button--muted" type="button" disabled>Confirmado</button>'
        : `<button class="button button--success" type="button" data-confirm-alert="${escapeHtml(alert.id)}">Confirmar <span aria-hidden="true">⌄</span></button>`}</td>
    </tr>
  `).join("");
  const selectedStatus = statuses.map((status) => status === "attention" ? "Atenção" : "Precisa de ajuda").join(", ");

  container.innerHTML = `
    ${pageHeading("Alertas", "Acompanhe o histórico de alertas da unidade.")}
    <section class="page-toolbar page-toolbar--alerts">
      <label class="search-field"><span class="visually-hidden">Buscar funcionário</span><input data-search-alerts type="search" placeholder="Buscar funcionário..." value="${escapeHtml(query)}"></label>
      <button class="button button--muted" type="button" data-open-dialog="status-filter">Estado <span aria-hidden="true">⌄</span></button>
      <button class="button button--muted" type="button" data-open-dialog="date-filter">Data/Hora <span aria-hidden="true">⌄</span></button>
      <button class="button button--muted" type="button" data-open-dialog="confirmation-filter">Filtros</button>
      <button class="button button--muted" type="button" data-clear-filters>Limpar</button>
      ${statuses.length ? `<span class="filter-chip">${escapeHtml(selectedStatus)} <button type="button" data-clear-status aria-label="Remover filtro de estado">×</button></span>` : ""}
      ${filters.confirmation !== undefined ? `<span class="filter-chip">${filters.confirmation ? "Confirmados" : "Não confirmados"} <button type="button" data-clear-confirmation aria-label="Remover filtro de confirmação">×</button></span>` : ""}
    </section>
    <section class="data-panel">
      <div class="data-panel__heading"><h2>${filtered.length} Alertas</h2><span>encontrados com filtro</span></div>
      <div class="table-scroll"><table class="data-table alerts-table">
        <thead><tr><th>Alerta</th><th>Estado</th><th>Última interação</th><th>Ação</th></tr></thead>
        <tbody>${rows || '<tr><td class="empty-state" colspan="4">Nenhum alerta encontrado.</td></tr>'}</tbody>
      </table></div>
    </section>
    <dialog class="filter-dialog" data-filter-dialog="status-filter"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Estado</h2><label><input type="checkbox" data-status-filter value="attention"${statuses.includes("attention") ? " checked" : ""}> Atenção</label><label><input type="checkbox" data-status-filter value="help"${statuses.includes("help") ? " checked" : ""}> Precisa de ajuda</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
    <dialog class="filter-dialog" data-filter-dialog="confirmation-filter"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Filtros</h2><label><input type="radio" name="confirmation" data-confirmation-filter value="false"${filters.confirmation === false ? " checked" : ""}> Não confirmados</label><label><input type="radio" name="confirmation" data-confirmation-filter value="true"${filters.confirmation === true ? " checked" : ""}> Confirmados</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
    <dialog class="filter-dialog" data-filter-dialog="date-filter"><form method="dialog"><button class="dialog-close" aria-label="Fechar">×</button><h2>Ordenar por data</h2><label><input type="radio" name="date-sort" data-date-sort value="newest"${filters.sort !== "oldest" ? " checked" : ""}> Mais recentes</label><label><input type="radio" name="date-sort" data-date-sort value="oldest"${filters.sort === "oldest" ? " checked" : ""}> Mais antigas</label><button class="button button--primary" value="apply">Aplicar</button></form></dialog>
  `;
}
