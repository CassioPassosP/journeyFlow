import { getDashboardData } from "./services/data-service.js";
import { renderSidebar } from "./components/sidebar.js";
import { renderHeader } from "./components/header.js";
import { renderSummaryCards } from "./components/summary-cards.js";
import { renderStatusChart } from "./components/status-chart.js";
import { renderUpdatesChart } from "./components/updates-chart.js";
import { renderRecentAlerts } from "./components/recent-alerts.js";
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
  const activePage = page === "employees" ? "employees" : page === "alerts" ? "alerts" : page === "settings" ? "settings" : "dashboard";
  renderSidebar(document.querySelector("#sidebar"), data, activePage);
  renderHeader(document.querySelector("#header"), data, {
    alerts: data.alerts,
    readNotifications,
    notificationsOpen,
    notificationError
  });
  const mainContent = document.querySelector("#main-content");
  mainContent.classList.toggle("main-content--dashboard", page === "dashboard" || !["employees", "alerts", "settings"].includes(page));

  if (page === "settings") {
    renderSettingsPage(mainContent, data.user, theme);
  } else if (page === "employees" && detail === "new") {
    renderEmployeeForm(mainContent);
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

window.addEventListener("hashchange", () => {
  if (!data) return;
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

document.addEventListener("click", (event) => {
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
    const alert = data.alerts.find((item) => item.id === confirmButton.dataset.confirmAlert);
    if (alert) {
      alert.confirmed = true;
      rerenderCurrentPage();
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

document.addEventListener("submit", (event) => {
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
      renderHeader(document.querySelector("#header"), data, {
        alerts: data.alerts,
        readNotifications,
        notificationsOpen
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
  const name = String(values.get("name")).trim();
  const hiredAt = String(values.get("hiredAt"));
  const days = Math.max(0, Math.floor((Date.now() - new Date(`${hiredAt}T00:00:00`).getTime()) / 86400000));
  const id = name.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-|-$/g, "");
  const employeeId = `${id}-${Date.now().toString(36)}`;
  const employee = {
    id: employeeId,
    name,
    role: String(values.get("role")).trim(),
    whatsapp: String(values.get("whatsapp")).trim(),
    unit: String(values.get("unit")).trim(),
    hiredAt,
    days,
    status: String(values.get("status")),
    interaction: "Agora"
  };
  data.employees.push(employee);
  window.location.hash = `#employees/${employeeId}`;
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
  data = await getDashboardData();
  const settings = loadUserSettings(data.user);
  data.user.name = settings.name;
  data.user.email = settings.email;
  theme = settings.theme;
  document.documentElement.dataset.theme = theme;
  readNotifications = loadReadNotifications();
  renderPage();
}

initialize().catch((error) => {
  console.error("Não foi possível carregar o dashboard JourneyFlow.", error);
  const mainContent = document.querySelector("#main-content");
  mainContent.innerHTML = `
    <p class="dashboard-error" role="alert">
      Não foi possível carregar o dashboard. Atualize a página e tente novamente.
    </p>
  `;
});
