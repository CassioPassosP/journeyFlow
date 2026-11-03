import {
  confirmAlert,
  completePill,
  createEmployee,
  getDashboardData,
  loginAndLoadDashboard,
  sendEmployeeMessage
} from "./services/data-service.js";
import { clearSession, getSession } from "./services/api-client.js";
import { renderSidebar } from "./components/sidebar.js";
import { renderHeader } from "./components/header.js";
import { renderSummaryCards } from "./components/summary-cards.js";
import { renderStatusChart } from "./components/status-chart.js";
import { renderUpdatesChart } from "./components/updates-chart.js";
import { renderRecentAlerts } from "./components/recent-alerts.js";
import { renderEmployeePage } from "./components/employee-pages.js";
import {
  loadReadNotifications,
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
  renderSettingsPage
} from "./components/pages.js";
import { escapeHtml, getGreeting } from "./utils/dom.js";

let data;
let employeeQuery = "";
const employeeSorts = { name: "asc", interaction: "desc", days: "asc" };
let employeeStatuses = null;
let alertQuery = "";
let alertFilters = { statuses: [] };
let readNotifications = new Set();
let theme = "light";
let notificationsOpen = false;
let notificationError = "";
let employeeNotice = "";
const redeemedRewards = new Set();

function persistReadNotifications(nextReadNotifications) {
  try {
    saveReadNotifications(nextReadNotifications);
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

function currentRoute() {
  const route = window.location.hash.slice(1).split("/").filter(Boolean);
  return { page: route[0] || "dashboard", detail: route[1] || "" };
}

function renderPage() {
  const { page, detail } = currentRoute();
  const isEmployee = page === "employee";
  const employeePage = isEmployee ? detail || "home" : "";
  const activePage = isEmployee
    ? employeePage
    : page === "employees" ? "employees" : page === "alerts" ? "alerts" : page === "settings" ? "settings" : "dashboard";
  const headerData = isEmployee
    ? { ...data, user: { ...data.user, role: data.employee.role } }
    : data;
  renderSidebar(document.querySelector("#sidebar"), data, activePage, isEmployee ? "employee" : "manager");
  renderHeader(document.querySelector("#header"), headerData, {
    alerts: data.alerts,
    readNotifications,
    notificationsOpen,
    notificationError,
    role: isEmployee ? "employee" : "manager"
  });
  const mainContent = document.querySelector("#main-content");
  document.querySelector("#sidebar").classList.toggle("sidebar-host--employee", isEmployee);
  document.querySelector(".workspace").classList.toggle("workspace--employee", isEmployee);
  mainContent.classList.toggle("main-content--employee", isEmployee);
  mainContent.classList.toggle("main-content--dashboard", !isEmployee && (page === "dashboard" || !["employees", "alerts", "settings"].includes(page)));

  if (isEmployee) {
    if (employeePage === "settings") {
      renderSettingsPage(mainContent, data.user, theme);
    } else {
      renderEmployeePage(mainContent, data, employeePage, data.employee.chat, employeeNotice, [...redeemedRewards]);
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
      mainContent.innerHTML = '<p class="dashboard-error" role="alert">Funcionario nao encontrado. <a href="#employees">Voltar para funcionarios</a></p>';
    }
  } else if (page === "employees") {
    renderEmployeesPage(mainContent, data.employees, employeeQuery, employeeSorts, employeeStatuses);
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
}

async function reloadDashboard() {
  data = await getDashboardData(getSession());
  renderPage();
}

function showApplication() {
  document.querySelector("#login-screen").hidden = true;
  document.querySelector(".app-layout").hidden = false;
}

function setRouteForRole(role) {
  const isEmployeeRoute = window.location.hash.startsWith("#employee/");
  if (role === "EMPLOYEE" && !isEmployeeRoute) {
    window.location.hash = "#employee/home";
  } else if (role === "MANAGER" && isEmployeeRoute) {
    window.location.hash = "#dashboard";
  }
}

async function submitEmployeeMessage(content) {
  try {
    await sendEmployeeMessage(data.currentEmployee.id, content);
    data = await getDashboardData(getSession());
    employeeNotice = "Mensagem enviada.";
    renderPage();
  } catch (error) {
    showPageError(error.message);
  }
}

window.addEventListener("hashchange", () => {
  if (!data) return;
  setRouteForRole(getSession()?.role);
  notificationsOpen = false;
  renderPage();
  window.scrollTo(0, 0);
});

document.addEventListener("input", (event) => {
  if (event.target.matches("[data-search-employees]")) {
    employeeQuery = event.target.value;
    renderPage();
    const input = document.querySelector("[data-search-employees]");
    input.focus();
    input.setSelectionRange(employeeQuery.length, employeeQuery.length);
  }
  if (event.target.matches("[data-search-alerts]")) {
    alertQuery = event.target.value;
    renderPage();
    const input = document.querySelector("[data-search-alerts]");
    input.focus();
    input.setSelectionRange(alertQuery.length, alertQuery.length);
  }
});

document.addEventListener("click", async (event) => {
  if (event.target.closest("[data-logout]")) {
    clearSession();
    data = undefined;
    showLogin();
    return;
  }
  if (notificationsOpen && !event.target.closest(".notification-popover, [data-toggle-notifications]")) {
    notificationsOpen = false;
    renderHeader(document.querySelector("#header"), data, {
      alerts: data.alerts,
      readNotifications,
      notificationsOpen,
      notificationError
    });
  }
  if (event.target.closest("[data-toggle-notifications]")) {
    notificationsOpen = !notificationsOpen;
    renderHeader(document.querySelector("#header"), data, {
      alerts: data.alerts,
      readNotifications,
      notificationsOpen,
      notificationError
    });
  }
  const employeeAnswer = event.target.closest("[data-employee-answer]");
  const employeeAction = event.target.closest("[data-employee-action]");
  if (employeeAnswer || employeeAction) {
    const text = employeeAnswer?.dataset.employeeAnswer || employeeAction.dataset.employeeAction.replace(/^[^ ]+\s/, "");
    await submitEmployeeMessage(text);
  }
  const redeemButton = event.target.closest("[data-redeem-reward]");
  if (redeemButton) {
    const reward = data.employee.rewards.find((item) => item.id === redeemButton.dataset.redeemReward);
    if (reward && !redeemedRewards.has(reward.id) && data.employee.points >= reward.points) {
      data.employee.points -= reward.points;
      redeemedRewards.add(reward.id);
      employeeNotice = `${reward.name} resgatado! Aproveite sua recompensa.`;
      rerenderCurrentPage();
    }
  }
  if (event.target.closest("[data-mark-all-read]")) {
    const nextReadNotifications = new Set(data.alerts.map((alert) => alert.id));
    persistReadNotifications(nextReadNotifications);
    renderHeader(document.querySelector("#header"), data, {
      alerts: data.alerts,
      readNotifications,
      notificationsOpen,
      notificationError
    });
  }
  const notificationButton = event.target.closest("[data-open-notification]");
  if (notificationButton) {
    const nextReadNotifications = new Set(readNotifications);
    nextReadNotifications.add(notificationButton.dataset.openNotification);
    persistReadNotifications(nextReadNotifications);
    notificationsOpen = false;
    renderHeader(document.querySelector("#header"), data, {
      alerts: data.alerts,
      readNotifications,
      notificationsOpen,
      notificationError
    });
    window.location.hash = "#alerts";
  }
  const openButton = event.target.closest("[data-open-dialog]");
  if (openButton) {
    document.querySelector(`[data-filter-dialog="${openButton.dataset.openDialog}"]`).showModal();
    openButton.setAttribute("aria-expanded", "true");
  }
  if (event.target.closest("[data-close-dialog]")) {
    event.target.closest("dialog").close();
  }
  if (event.target.closest("[data-select-all-employee-status]")) {
    const dialog = event.target.closest("[data-filter-dialog='employee-status-filter']");
    dialog.querySelectorAll("[data-employee-status-filter]").forEach((input) => {
      input.checked = true;
    });
  }
  const applyEmployeeSort = event.target.closest("[data-apply-employee-sort]");
  if (applyEmployeeSort) {
    const dialog = applyEmployeeSort.closest("[data-filter-dialog]");
    const selected = dialog.querySelector("[data-employee-sort-direction]:checked");
    if (selected) {
      employeeSorts[applyEmployeeSort.dataset.applyEmployeeSort] = selected.value;
      dialog.close();
      rerenderCurrentPage();
    }
  }
  if (event.target.closest("[data-apply-employee-status]")) {
    const dialog = event.target.closest("[data-filter-dialog='employee-status-filter']");
    employeeStatuses = [...dialog.querySelectorAll("[data-employee-status-filter]:checked")].map((input) => input.value);
    dialog.close();
    rerenderCurrentPage();
  }
  const confirmButton = event.target.closest("[data-confirm-alert]");
  if (confirmButton) {
    confirmButton.disabled = true;
    try {
      await confirmAlert(confirmButton.dataset.confirmAlert);
      await reloadDashboard();
    } catch (error) {
      confirmButton.disabled = false;
      showPageError(error.message);
    }
  }
  if (event.target.closest("[data-clear-filters]")) {
    alertQuery = "";
    alertFilters = { statuses: [] };
    rerenderCurrentPage();
  }
  if (event.target.closest("[data-clear-status]")) {
    alertFilters.statuses = [];
    rerenderCurrentPage();
  }
  if (event.target.closest("[data-clear-confirmation]")) {
    delete alertFilters.confirmation;
    rerenderCurrentPage();
  }
});

document.addEventListener("close", (event) => {
  const dialog = event.target.closest("[data-filter-dialog]");
  if (dialog) {
    document.querySelector(`[data-open-dialog="${dialog.dataset.filterDialog}"]`)?.setAttribute("aria-expanded", "false");
  }
  if (!dialog || event.target.returnValue !== "apply") return;
  if (dialog.dataset.filterDialog === "status-filter") {
    alertFilters.statuses = [...dialog.querySelectorAll("[data-status-filter]:checked")].map((input) => input.value);
  } else if (dialog.dataset.filterDialog === "confirmation-filter") {
    const selected = dialog.querySelector("[data-confirmation-filter]:checked");
    if (selected) alertFilters.confirmation = selected.value === "true";
  } else {
    const selected = dialog.querySelector("[data-date-sort]:checked");
    if (selected) alertFilters.sort = selected.value;
  }
  rerenderCurrentPage();
}, true);

document.addEventListener("submit", async (event) => {
  const loginForm = event.target.closest("[data-login-form]");
  if (loginForm) {
    event.preventDefault();
    const button = loginForm.querySelector('button[type="submit"]');
    const values = new FormData(loginForm);
    button.disabled = true;
    try {
      data = await loginAndLoadDashboard(
        String(values.get("email")).trim(),
        String(values.get("password"))
      );
      setRouteForRole(getSession().role);
      const settings = loadUserSettings(data.user);
      data.user.name = settings.name;
      data.user.email = settings.email;
      theme = settings.theme;
      document.documentElement.dataset.theme = theme;
      readNotifications = loadReadNotifications();
      showApplication();
      renderPage();
    } catch (error) {
      clearSession();
      showLogin(error.status === 403
        ? "Credenciais inválidas. Confira o e-mail e a senha e tente novamente."
        : error.message);
    } finally {
      button.disabled = false;
    }
    return;
  }

  const chatForm = event.target.closest("[data-employee-chat-form]");
  if (chatForm) {
    event.preventDefault();
    const input = chatForm.elements.message;
    const text = input.value.trim();
    if (!text) return;
    await submitEmployeeMessage(text);
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
      await completePill(data.currentEmployee.id, Number(pillQuiz.dataset.pillQuiz));
      data = await getDashboardData(getSession());
      employeeNotice = "Resposta correta! Pílula concluída.";
      renderPage();
    } catch (error) {
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
      saveUserSettings({ name, email, theme });
      data.user.name = name;
      data.user.email = email;
      const isEmployee = currentRoute().page === "employee";
      const headerData = isEmployee
        ? { ...data, user: { ...data.user, role: data.employee.role } }
        : data;
      renderHeader(document.querySelector("#header"), headerData, {
        alerts: data.alerts,
        readNotifications,
        notificationsOpen,
        role: isEmployee ? "employee" : "manager"
      });
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
  submitButton.disabled = true;
  try {
    const createdEmployee = await createEmployee(payload);
    await reloadDashboard();
    window.location.hash = `#employees/${createdEmployee.id}`;
  } catch (error) {
    submitButton.disabled = false;
    let message = form.querySelector("[data-form-error]");
    if (!message) {
      message = document.createElement("p");
      message.className = "dashboard-error";
      message.setAttribute("role", "alert");
      message.dataset.formError = "";
      form.prepend(message);
    }
    message.textContent = error.message;
  }
});

document.addEventListener("change", (event) => {
  if (!event.target.matches('input[name="theme"]')) return;
  const selectedTheme = event.target.value;
  try {
    saveUserSettings({
      name: data.user.name,
      email: data.user.email,
      theme: selectedTheme
    });
    theme = selectedTheme;
    document.documentElement.dataset.theme = theme;
    if (currentRoute().page === "dashboard") renderPage();
  } catch (error) {
    console.error("Não foi possível salvar a preferência de tema.", error);
    const message = document.querySelector("[data-profile-message]");
    if (message) {
      message.textContent = "Não foi possível salvar a preferência de tema.";
      message.classList.add("settings-message--error");
    }
    event.target.checked = false;
    const previousTheme = theme;
    const previousOption = document.querySelector(`input[name="theme"][value="${previousTheme}"]`);
    if (previousOption) previousOption.checked = true;
  }
});

async function initialize() {
  const session = getSession();
  if (!session) {
    showLogin();
    return;
  }

  try {
    data = await getDashboardData(session);
    setRouteForRole(session.role);
    const settings = loadUserSettings(data.user);
    data.user.name = settings.name;
    data.user.email = settings.email;
    theme = settings.theme;
    document.documentElement.dataset.theme = theme;
    readNotifications = loadReadNotifications();
    showApplication();
    renderPage();
  } catch (error) {
    clearSession();
    showLogin(error.message);
  }
}

initialize().catch((error) => {
  console.error("Não foi possível carregar o dashboard JourneyFlow.", error);
  showLogin(error.message);
});
