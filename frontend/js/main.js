import { clearSession, getSession } from "./services/api-client.js";
import {
  completePill,
  confirmAlert,
  createEmployee,
  getDashboardData,
  loginAndLoadDashboard,
  redeemReward,
  sendEmployeeMessage
} from "./services/data-service.js";
import { renderSidebar } from "./components/sidebar.js";
import { renderHeader } from "./components/header.js";
import { renderSummaryCards } from "./components/summary-cards.js";
import { renderStatusChart } from "./components/status-chart.js";
import { renderUpdatesChart } from "./components/updates-chart.js";
import { renderRecentAlerts } from "./components/recent-alerts.js";
import { renderEmployeePage } from "./components/employee-pages.js";
import {
  loadReadNotifications,
  loadTheme,
  loadUserSettings,
  saveReadNotifications,
  saveUserSettings
} from "./services/preferences.js";
import {
  renderAlertsPage,
  renderDashboardPage,
  renderEmployeeDetails,
  renderEmployeeForm,
  renderEmployeesPage,
  renderSettingsPage,
  updateAlertsResults,
  updateEmployeesResults
} from "./components/pages.js";
import { getGreeting } from "./utils/dom.js";

const DEFAULT_EMPLOYEE_SORTS = { name: "asc", interaction: "desc", days: "asc" };
const DEFAULT_SORT_PRIORITY = ["interaction", "days", "name"];

let data;
let employeeQuery = "";
let employeeSorts = { ...DEFAULT_EMPLOYEE_SORTS };
let employeeSortPriority = [...DEFAULT_SORT_PRIORITY];
let employeeStatuses = null;
let alertQuery = "";
let alertFilters = { statuses: [] };
let readNotifications = new Set();
let theme = "light";
let notificationsOpen = false;
let notificationError = "";
let employeeNotice = "";
let lastRouteKey = "";
let busyCount = 0;
let noticeTimer;
let sendingMessage = false;

/* ------------------------------------------------------------------ */
/* Estado e utilitários                                                */
/* ------------------------------------------------------------------ */

// Ao sair (ou ao expirar a sessão) tudo o que pertence à conta anterior é descartado.
// Antes filtros, buscas, avisos e recompensas resgatadas "vazavam" para o próximo login.
function resetUiState() {
  employeeQuery = "";
  employeeSorts = { ...DEFAULT_EMPLOYEE_SORTS };
  employeeSortPriority = [...DEFAULT_SORT_PRIORITY];
  employeeStatuses = null;
  alertQuery = "";
  alertFilters = { statuses: [] };
  readNotifications = new Set();
  notificationsOpen = false;
  notificationError = "";
  employeeNotice = "";
  lastRouteKey = "";
  sendingMessage = false;
  window.clearTimeout(noticeTimer);
}

function userKey() {
  const session = getSession();
  return session ? `${session.role}.${session.userId}` : "";
}

function prefersReducedMotion() {
  return window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

// Preferências guardadas no navegador podem estar corrompidas; isso não pode impedir o login.
function withFallback(task, fallback, label) {
  try {
    return task();
  } catch (error) {
    console.error(label, error);
    return fallback;
  }
}

function applyUserSettings(nextData) {
  const settings = withFallback(
    () => loadUserSettings(nextData.user, userKey()),
    { name: nextData.user.name, email: nextData.user.email },
    "Não foi possível ler as configurações do usuário."
  );
  nextData.user.name = settings.name;
  nextData.user.email = settings.email;
  return nextData;
}

function applyTheme(nextTheme, { animate = true } = {}) {
  theme = nextTheme === "dark" ? "dark" : "light";
  const root = document.documentElement;
  if (animate && !prefersReducedMotion()) {
    root.classList.add("theme-transition");
    window.setTimeout(() => root.classList.remove("theme-transition"), 350);
  }
  root.dataset.theme = theme;
}

function setBusy(delta) {
  busyCount = Math.max(0, busyCount + delta);
  document.querySelector("#progress")?.classList.toggle("is-active", busyCount > 0);
  document.documentElement.toggleAttribute("aria-busy", busyCount > 0);
}

async function withBusy(task) {
  setBusy(1);
  try {
    return await task();
  } finally {
    setBusy(-1);
  }
}

function hideBoot() {
  const boot = document.querySelector("#boot");
  if (!boot) return;
  boot.classList.add("is-hidden");
  window.setTimeout(() => boot.remove(), 300);
}

/* ------------------------------------------------------------------ */
/* Rotas                                                               */
/* ------------------------------------------------------------------ */

function currentRoute() {
  const route = window.location.hash.slice(1).split("/").filter(Boolean);
  return { page: route[0] || "dashboard", detail: route[1] || "" };
}

function routeKey() {
  const { page, detail } = currentRoute();
  return `${page}/${detail}`;
}

// Devolve true quando precisou trocar a rota; o hashchange seguinte faz o render.
function setRouteForRole(role) {
  const isEmployeeRoute = window.location.hash.startsWith("#employee/");
  if (role === "EMPLOYEE" && !isEmployeeRoute) {
    window.location.hash = "#employee/home";
    return true;
  }
  if (role === "MANAGER" && isEmployeeRoute) {
    window.location.hash = "#dashboard";
    return true;
  }
  return false;
}

/* ------------------------------------------------------------------ */
/* Cabeçalho e notificações                                            */
/* ------------------------------------------------------------------ */

function isEmployeeRoute() {
  return currentRoute().page === "employee";
}

function headerOptions(overrides = {}) {
  return {
    alerts: data.alerts,
    readNotifications,
    notificationsOpen,
    notificationError,
    role: isEmployeeRoute() ? "employee" : "manager",
    ...overrides
  };
}

// Antes, abrir as notificações ou marcá-las como lidas recriava o cabeçalho sem informar o perfil,
// e o colaborador passava a ver o nome da unidade do gestor no lugar de "Minha jornada".
function refreshHeader(overrides = {}) {
  const headerData = isEmployeeRoute()
    ? { ...data, user: { ...data.user, role: data.employee.role } }
    : data;
  renderHeader(document.querySelector("#header"), headerData, headerOptions(overrides));
}

// Abrir/fechar apenas alterna o painel existente, sem recriar o cabeçalho.
function setNotificationsOpen(open, { restoreFocus = false } = {}) {
  notificationsOpen = open;
  const popover = document.querySelector("#notification-popover");
  const toggle = document.querySelector("[data-toggle-notifications]");
  if (!popover || !toggle) return;
  if (open) popover.removeAttribute("data-instant");
  popover.hidden = !open;
  toggle.setAttribute("aria-expanded", String(open));
  if (!open && restoreFocus) toggle.focus();
}

function persistReadNotifications(nextReadNotifications) {
  try {
    saveReadNotifications(nextReadNotifications, userKey());
    readNotifications = nextReadNotifications;
    notificationError = "";
    return true;
  } catch (error) {
    console.error("Não foi possível salvar o estado das notificações.", error);
    notificationError = "Não foi possível salvar. Verifique as permissões de armazenamento do navegador.";
    notificationsOpen = true;
    return false;
  }
}

/* ------------------------------------------------------------------ */
/* Renderização                                                        */
/* ------------------------------------------------------------------ */

function replayPageEnter(element) {
  if (prefersReducedMotion()) return;
  element.classList.remove("page-enter");
  void element.offsetWidth;
  element.classList.add("page-enter");
}

function scrollChatsToEnd() {
  document.querySelectorAll(".employee-chat-log").forEach((log) => {
    log.scrollTop = log.scrollHeight;
  });
}

// moveFocus: leva o foco ao conteúdo ao trocar de página (acessibilidade).
// preserveChatScroll: mantém a posição da janela ao redesenhar depois de uma ação no chat.
function renderPage({ moveFocus = false, preserveChatScroll = false } = {}) {
  const viewportX = window.scrollX;
  const viewportY = window.scrollY;
  const { page, detail } = currentRoute();
  const isEmployee = page === "employee";
  const employeePage = isEmployee ? detail || "home" : "";
  const activePage = isEmployee
    ? employeePage
    : page === "employees" ? "employees" : page === "alerts" ? "alerts" : page === "settings" ? "settings" : "dashboard";
  const role = isEmployee ? "employee" : "manager";

  lastRouteKey = routeKey();

  renderSidebar(document.querySelector("#sidebar"), data, activePage, role);
  refreshHeader({ instant: true });

  const mainContent = document.querySelector("#main-content");
  document.querySelector("#sidebar").classList.toggle("sidebar-host--employee", isEmployee);
  document.querySelector(".workspace").classList.toggle("workspace--employee", isEmployee);
  mainContent.classList.toggle("main-content--employee", isEmployee);
  mainContent.classList.toggle("main-content--dashboard", !isEmployee && (page === "dashboard" || !["employees", "alerts", "settings"].includes(page)));

  if (isEmployee) {
    if (employeePage === "settings") {
      renderSettingsPage(mainContent, data.user, theme);
    } else {
      renderEmployeePage(mainContent, data, employeePage, data.employee.chat, employeeNotice, data.employee.redeemedRewardIds || []);
      scrollChatsToEnd();
    }
  } else if (page === "settings") {
    renderSettingsPage(mainContent, data.user, theme);
  } else if (page === "employees" && detail === "new") {
    renderEmployeeForm(mainContent, data.stores, data.currentManager.storeId);
  } else if (page === "employees" && detail) {
    const employee = data.employees.find((item) => item.id === detail);
    if (employee) {
      renderEmployeeDetails(mainContent, employee, data.alerts);
    } else {
      mainContent.innerHTML = '<p class="dashboard-error" role="alert">Funcionário não encontrado. <a href="#employees">Voltar para funcionários</a></p>';
    }
  } else if (page === "employees") {
    renderEmployeesPage(mainContent, data.employees, employeeQuery, employeeSorts, employeeStatuses, employeeSortPriority);
  } else if (page === "alerts") {
    renderAlertsPage(mainContent, data.alerts, alertQuery, alertFilters);
  } else {
    renderDashboardPage(mainContent, data);
    const firstName = data.user.name.trim().split(/\s+/)[0];
    mainContent.querySelector("#greeting-title").textContent = `${getGreeting()}, ${firstName}`;
    renderSummaryCards(mainContent.querySelector("#summary-cards"), data.summary);
    renderStatusChart(mainContent.querySelector("#status-chart"), data.monthlyStatus);
    renderUpdatesChart(mainContent.querySelector("#updates-chart"), data.topUpdates);
    renderRecentAlerts(mainContent.querySelector("#recent-alerts"), data.recentAlerts);
  }

  if (moveFocus) {
    mainContent.focus({ preventScroll: true });
    replayPageEnter(mainContent);
  }

  const shouldShowLatestChat = isEmployee && (employeePage === "conversation" || employeePage === "home");
  if (preserveChatScroll || shouldShowLatestChat) {
    requestAnimationFrame(() => {
      const chatLog = document.querySelector(".employee-chat-log");
      if (chatLog) chatLog.scrollTop = chatLog.scrollHeight;
      if (preserveChatScroll) window.scrollTo(viewportX, viewportY);
    });
  }
}

function rerenderCurrentPage() {
  renderPage();
}

function showLogin(errorMessage = "") {
  document.querySelector(".app-layout").hidden = true;
  const loginScreen = document.querySelector("#login-screen");
  const error = loginScreen.querySelector("[data-login-error]");
  error.textContent = errorMessage;
  error.hidden = !errorMessage;
  loginScreen.hidden = false;
  loginScreen.querySelector('input[name="email"]')?.focus({ preventScroll: true });
}

function showApplication() {
  document.querySelector("#login-screen").hidden = true;
  document.querySelector(".app-layout").hidden = false;
}

function showPageError(message) {
  const mainContent = document.querySelector("#main-content");
  mainContent.querySelector("[data-api-error]")?.remove();
  const error = document.createElement("p");
  error.className = "dashboard-error";
  error.setAttribute("role", "alert");
  error.dataset.apiError = "";
  error.textContent = message;
  mainContent.prepend(error);
  error.scrollIntoView({ block: "nearest", behavior: prefersReducedMotion() ? "auto" : "smooth" });
}

function flashNotice(message) {
  employeeNotice = message;
  window.clearTimeout(noticeTimer);
  // O aviso some sozinho; antes ele ficava na tela até o próximo login.
  noticeTimer = window.setTimeout(() => {
    employeeNotice = "";
    document.querySelectorAll(".employee-notice").forEach((element) => {
      element.classList.add("is-leaving");
      window.setTimeout(() => element.remove(), 220);
    });
  }, 4500);
}

/* ------------------------------------------------------------------ */
/* Sessão e dados                                                      */
/* ------------------------------------------------------------------ */

function logout(message = "") {
  clearSession();
  data = undefined;
  resetUiState();
  document.querySelector("#sidebar")?.removeAttribute("data-signature");
  window.history.replaceState(null, "", `${window.location.pathname}${window.location.search}`);
  showLogin(message);
}

// Token expirado (401) leva de volta ao login em vez de deixar a tela com botões que falham.
function handleActionError(error, fallbackHandler = showPageError) {
  if (error?.status === 401) {
    logout("Sua sessão expirou. Entre novamente.");
    return;
  }
  fallbackHandler(error?.message || "Algo deu errado. Tente novamente.");
}

// Recarregar os dados não pode descartar o nome/e-mail que o usuário editou em Configurações
// (antes eles voltavam ao valor da API após confirmar um alerta ou enviar uma mensagem).
async function refreshData() {
  const fresh = await withBusy(() => getDashboardData(getSession()));
  data = applyUserSettings(fresh);
}

async function submitEmployeeMessage(content, { restoreInput } = {}) {
  if (sendingMessage) return;
  sendingMessage = true;
  try {
    await withBusy(() => sendEmployeeMessage(data.currentEmployee.id, content));
    await refreshData();
    flashNotice("Mensagem enviada.");
    renderPage({ preserveChatScroll: true });
  } catch (error) {
    restoreInput?.();
    handleActionError(error);
  } finally {
    sendingMessage = false;
  }
}

async function redeemRewardById(button) {
  const rewardId = button.dataset.redeemReward;
  const reward = (data.employee.rewards || []).find((item) => String(item.id) === String(rewardId));
  if (!reward) return;
  if ((data.employee.redeemedRewardIds || []).includes(reward.id)) return;
  if (data.employee.points < reward.points) {
    showPageError("Você não tem pontos suficientes para este resgate.");
    return;
  }

  button.disabled = true;
  try {
    await withBusy(() => redeemReward(data.currentEmployee.id, reward));
    await refreshData();
    flashNotice(`Recompensa resgatada: ${reward.name}.`);
    renderPage();
  } catch (error) {
    button.disabled = false;
    handleActionError(error);
  }
}

/* ------------------------------------------------------------------ */
/* Eventos                                                             */
/* ------------------------------------------------------------------ */

window.addEventListener("hashchange", () => {
  if (!data) return;
  // Se a rota era de outro perfil, o redirecionamento dispara um novo hashchange; evita renderizar a tela errada antes.
  if (setRouteForRole(getSession()?.role)) return;
  notificationsOpen = false;
  if (routeKey() === lastRouteKey) {
    setNotificationsOpen(false);
    return;
  }
  employeeNotice = "";
  renderPage({ moveFocus: true });
  window.scrollTo(0, 0);
});

document.addEventListener("keydown", (event) => {
  if (event.key === "Escape" && notificationsOpen) {
    setNotificationsOpen(false, { restoreFocus: true });
  }
});

document.addEventListener("input", (event) => {
  if (!data) return;
  if (event.target.matches("[data-search-employees]")) {
    employeeQuery = event.target.value;
    const main = document.querySelector("#main-content");
    updateEmployeesResults(main, data.employees, employeeQuery, employeeSorts, employeeStatuses, employeeSortPriority);
  }
  if (event.target.matches("[data-search-alerts]")) {
    alertQuery = event.target.value;
    const main = document.querySelector("#main-content");
    updateAlertsResults(main, data.alerts, alertQuery, alertFilters);
  }
});

document.addEventListener("click", async (event) => {
  const target = event.target;
  const closest = (selector) => target.closest(selector);

  if (closest("[data-logout]")) {
    logout();
    return;
  }
  if (closest("[data-skip-to-content]")) {
    event.preventDefault();
    document.querySelector("#main-content")?.focus();
    return;
  }
  if (closest("[data-retry-render]")) {
    if (data) renderPage();
    return;
  }

  // Clique no fundo escurecido fecha o diálogo de filtros.
  if (target instanceof HTMLDialogElement && target.open) {
    const rect = target.getBoundingClientRect();
    const inside = event.clientX >= rect.left && event.clientX <= rect.right
      && event.clientY >= rect.top && event.clientY <= rect.bottom;
    if (!inside) target.close();
    return;
  }

  // Sem dados carregados (tela de login) não há mais nada a tratar.
  if (!data) return;

  if (notificationsOpen && !closest(".notification-popover, [data-toggle-notifications]")) {
    setNotificationsOpen(false);
  }
  if (closest("[data-toggle-notifications]")) {
    setNotificationsOpen(!notificationsOpen);
    return;
  }

  const employeeAnswer = closest("[data-employee-answer]");
  const employeeAction = closest("[data-employee-action]");
  if (employeeAnswer || employeeAction) {
    const text = employeeAnswer?.dataset.employeeAnswer || employeeAction.dataset.employeeAction.replace(/^[^ ]+\s/, "");
    await submitEmployeeMessage(text);
    return;
  }

  const redeemButton = closest("[data-redeem-reward]");
  if (redeemButton) {
    await redeemRewardById(redeemButton);
    return;
  }

  if (closest("[data-mark-all-read]")) {
    persistReadNotifications(new Set(data.alerts.map((alert) => alert.id)));
    refreshHeader({ instant: true });
    return;
  }

  const notificationButton = closest("[data-open-notification]");
  if (notificationButton) {
    const nextReadNotifications = new Set(readNotifications);
    nextReadNotifications.add(notificationButton.dataset.openNotification);
    persistReadNotifications(nextReadNotifications);
    notificationsOpen = false;
    refreshHeader({ instant: true });
    window.location.hash = "#alerts";
    return;
  }

  const openButton = closest("[data-open-dialog]");
  if (openButton) {
    const dialog = document.querySelector(`[data-filter-dialog="${openButton.dataset.openDialog}"]`);
    if (dialog && !dialog.open) {
      dialog.showModal();
      openButton.setAttribute("aria-expanded", "true");
    }
    return;
  }
  if (closest("[data-close-dialog]")) {
    closest("dialog").close();
    return;
  }

  if (closest("[data-select-all-employee-status]")) {
    const dialog = closest("[data-filter-dialog='employee-status-filter']");
    dialog.querySelectorAll("[data-employee-status-filter]").forEach((input) => {
      input.checked = true;
    });
    return;
  }

  const applyEmployeeSort = closest("[data-apply-employee-sort]");
  if (applyEmployeeSort) {
    const dialog = applyEmployeeSort.closest("[data-filter-dialog]");
    const selected = dialog.querySelector("[data-employee-sort-direction]:checked");
    if (selected) {
      const sort = applyEmployeeSort.dataset.applyEmployeeSort;
      employeeSorts[sort] = selected.value;
      // O critério escolhido passa a ser o principal da ordenação.
      employeeSortPriority = [sort, ...employeeSortPriority.filter((item) => item !== sort)];
      dialog.close();
      updateEmployeesResults(document.querySelector("#main-content"), data.employees, employeeQuery, employeeSorts, employeeStatuses, employeeSortPriority);
    }
    return;
  }

  if (closest("[data-apply-employee-status]")) {
    const dialog = closest("[data-filter-dialog='employee-status-filter']");
    employeeStatuses = [...dialog.querySelectorAll("[data-employee-status-filter]:checked")].map((input) => input.value);
    dialog.close();
    rerenderCurrentPage();
    return;
  }

  const confirmButton = closest("[data-confirm-alert]");
  if (confirmButton) {
    confirmButton.disabled = true;
    confirmButton.classList.add("is-loading");
    try {
      await withBusy(() => confirmAlert(confirmButton.dataset.confirmAlert));
      await refreshData();
      // Só a lista e o contador de notificações mudam; os filtros e a busca continuam como estavam.
      if (!updateAlertsResults(document.querySelector("#main-content"), data.alerts, alertQuery, alertFilters)) {
        renderPage();
      }
      refreshHeader({ instant: true });
    } catch (error) {
      confirmButton.disabled = false;
      confirmButton.classList.remove("is-loading");
      handleActionError(error);
    }
    return;
  }

  if (closest("[data-clear-filters]")) {
    alertQuery = "";
    alertFilters = { statuses: [] };
    rerenderCurrentPage();
    return;
  }
  if (closest("[data-clear-status]")) {
    alertFilters.statuses = [];
    rerenderCurrentPage();
    return;
  }
  if (closest("[data-clear-confirmation]")) {
    delete alertFilters.confirmation;
    rerenderCurrentPage();
  }
});

document.addEventListener("close", (event) => {
  const dialog = event.target.closest?.("[data-filter-dialog]");
  if (dialog) {
    document.querySelector(`[data-open-dialog="${dialog.dataset.filterDialog}"]`)?.setAttribute("aria-expanded", "false");
  }
  if (!data || !dialog || event.target.returnValue !== "apply") return;
  if (dialog.dataset.filterDialog === "status-filter") {
    alertFilters.statuses = [...dialog.querySelectorAll("[data-status-filter]:checked")].map((input) => input.value);
  } else if (dialog.dataset.filterDialog === "confirmation-filter") {
    const selected = dialog.querySelector("[data-confirmation-filter]:checked");
    if (selected) alertFilters.confirmation = selected.value === "true";
  } else if (dialog.querySelector("[data-date-sort]")) {
    const selected = dialog.querySelector("[data-date-sort]:checked");
    if (selected) alertFilters.sort = selected.value;
  } else {
    // Outros diálogos (ex.: filtros de funcionários) aplicam o próprio estado nos botões acima.
    return;
  }
  rerenderCurrentPage();
}, true);

document.addEventListener("submit", async (event) => {
  const loginForm = event.target.closest("[data-login-form]");
  if (loginForm) {
    event.preventDefault();
    const button = loginForm.querySelector('button[type="submit"]');
    const idleLabel = button.textContent;
    const values = new FormData(loginForm);
    button.disabled = true;
    button.textContent = "Entrando…";
    try {
      resetUiState();
      const nextData = await withBusy(() => loginAndLoadDashboard(
        String(values.get("email")).trim(),
        String(values.get("password"))
      ));
      data = applyUserSettings(nextData);
      loginForm.reset();
      setRouteForRole(getSession().role);
      readNotifications = withFallback(
        () => loadReadNotifications(userKey()),
        new Set(),
        "Não foi possível ler as notificações lidas."
      );
      showApplication();
      renderPage();
    } catch (error) {
      clearSession();
      data = undefined;
      showLogin(error.status === 403
        ? "Credenciais inválidas. Confira o e-mail e a senha e tente novamente."
        : error.message);
    } finally {
      button.disabled = false;
      button.textContent = idleLabel;
    }
    return;
  }

  if (!data) return;

  const chatForm = event.target.closest("[data-employee-chat-form]");
  if (chatForm) {
    event.preventDefault();
    const input = chatForm.elements.message;
    const text = input.value.trim();
    if (!text || sendingMessage) return;
    input.value = "";
    await submitEmployeeMessage(text, {
      restoreInput: () => {
        const current = document.querySelector("#employee-message");
        if (current && !current.value) current.value = text;
      }
    });
    return;
  }

  const pillQuiz = event.target.closest("[data-pill-quiz]");
  if (pillQuiz) {
    event.preventDefault();
    const feedback = pillQuiz.querySelector("[data-pill-feedback]");
    const answer = new FormData(pillQuiz).get("answer");
    if (answer === null) {
      feedback.textContent = "Selecione uma resposta antes de continuar.";
      feedback.hidden = false;
      return;
    }
    if (Number(answer) !== Number(pillQuiz.dataset.correctAnswer)) {
      feedback.textContent = "Essa resposta não está correta. Revise o conteúdo e tente novamente.";
      feedback.hidden = false;
      return;
    }
    const button = pillQuiz.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
      await withBusy(() => completePill(data.currentEmployee.id, Number(pillQuiz.dataset.pillQuiz)));
      await refreshData();
      flashNotice("Resposta correta! Pílula concluída.");
      renderPage();
    } catch (error) {
      if (error.status === 401) {
        logout("Sua sessão expirou. Entre novamente.");
        return;
      }
      button.disabled = false;
      feedback.textContent = error.status === 409
        ? "Esta pílula já consta como concluída. Atualize a tela para ver seu progresso."
        : error.message;
      feedback.hidden = false;
    }
    return;
  }

  const profileForm = event.target.closest("[data-profile-form]");
  if (profileForm) {
    event.preventDefault();
    const values = new FormData(profileForm);
    const name = String(values.get("name")).trim();
    const email = String(values.get("email")).trim();
    const message = profileForm.querySelector("[data-profile-message]");
    try {
      saveUserSettings({ name, email, theme }, userKey());
      data.user.name = name;
      data.user.email = email;
      refreshHeader({ instant: true });
      message.textContent = "Perfil atualizado neste navegador.";
      message.classList.remove("settings-message--error");
    } catch (error) {
      console.error("Não foi possível salvar as configurações do perfil.", error);
      message.textContent = "Não foi possível salvar. Verifique as permissões de armazenamento do navegador.";
      message.classList.add("settings-message--error");
    }
    return;
  }

  const form = event.target.closest("[data-employee-form]");
  if (!form) return;
  event.preventDefault();
  const values = new FormData(form);
  const hiredAt = String(values.get("hiredAt"));
  const status = String(values.get("status") || "normal");
  const payload = {
    storeId: Number(values.get("storeId")),
    name: String(values.get("name")).trim(),
    email: String(values.get("email")).trim(),
    password: String(values.get("password")),
    position: String(values.get("position")).trim(),
    initiationDate: hiredAt,
    startDate: hiredAt,
    currentState: { normal: "NORMAL", attention: "ATTENTION", help: "HELP" }[status],
    inLearningPath: values.has("inLearningPath"),
    consent: values.has("consent")
  };
  const submitButton = form.querySelector('button[type="submit"]');
  const idleLabel = submitButton.textContent;
  submitButton.disabled = true;
  submitButton.textContent = "Cadastrando…";
  try {
    const createdEmployee = await withBusy(() => createEmployee(payload));
    await refreshData();
    // A navegação dispara o render da nova tela; antes a página era desenhada duas vezes.
    window.location.hash = `#employees/${createdEmployee.id}`;
  } catch (error) {
    submitButton.disabled = false;
    submitButton.textContent = idleLabel;
    if (error.status === 401) {
      logout("Sua sessão expirou. Entre novamente.");
      return;
    }
    let message = form.querySelector("[data-form-error]");
    if (!message) {
      message = document.createElement("p");
      message.className = "dashboard-error";
      message.setAttribute("role", "alert");
      message.dataset.formError = "";
      form.prepend(message);
    }
    message.textContent = error.message;
    message.scrollIntoView({ block: "nearest", behavior: prefersReducedMotion() ? "auto" : "smooth" });
  }
});

document.addEventListener("change", (event) => {
  if (!event.target.matches('input[name="theme"]')) return;
  if (!data) return;
  const selectedTheme = event.target.value;
  try {
    saveUserSettings({
      name: data.user.name,
      email: data.user.email,
      theme: selectedTheme
    }, userKey());
    applyTheme(selectedTheme);
    // Os gráficos leem as cores do tema ao serem criados, então o dashboard é redesenhado.
    if (currentRoute().page === "dashboard") renderPage();
  } catch (error) {
    console.error("Não foi possível salvar a preferência de tema.", error);
    const message = document.querySelector("[data-profile-message]");
    if (message) {
      message.textContent = "Não foi possível salvar a preferência de tema.";
      message.classList.add("settings-message--error");
    }
    event.target.checked = false;
    const previousOption = document.querySelector(`input[name="theme"][value="${theme}"]`);
    if (previousOption) previousOption.checked = true;
  }
});

/* ------------------------------------------------------------------ */
/* Inicialização                                                       */
/* ------------------------------------------------------------------ */

async function initialize() {
  applyTheme(withFallback(loadTheme, "light", "Não foi possível ler o tema."), { animate: false });

  let session;
  try {
    session = getSession();
  } catch (error) {
    showLogin(error.message);
    return;
  }
  if (!session) {
    showLogin();
    return;
  }

  try {
    data = applyUserSettings(await getDashboardData(session));
    setRouteForRole(session.role);
    readNotifications = withFallback(
      () => loadReadNotifications(userKey()),
      new Set(),
      "Não foi possível ler as notificações lidas."
    );
    showApplication();
    renderPage();
  } catch (error) {
    data = undefined;
    clearSession();
    showLogin(error.status === 401 ? "Sua sessão expirou. Entre novamente." : error.message);
  }
}

initialize()
  .catch((error) => {
    console.error("Não foi possível carregar o dashboard JourneyFlow.", error);
    showLogin(error.message);
  })
  .finally(hideBoot);