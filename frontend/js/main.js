import { getDashboardData } from "./services/data-service.js";
import { renderSidebar } from "./components/sidebar.js";
import { renderHeader } from "./components/header.js";
import { renderSummaryCards } from "./components/summary-cards.js";
import { renderStatusChart } from "./components/status-chart.js";
import { renderUpdatesChart } from "./components/updates-chart.js";
import { renderRecentAlerts } from "./components/recent-alerts.js";
import {
  renderAlertsPage,
  renderDashboardPage,
  renderEmployeeDetails,
  renderEmployeeForm,
  renderEmployeesPage
} from "./components/pages.js";
import { escapeHtml, getGreeting } from "./utils/dom.js";

let data;
let employeeQuery = "";
let employeeSort = "name";
let alertQuery = "";
let alertFilters = { statuses: [] };

function currentRoute() {
  const route = window.location.hash.slice(1).split("/").filter(Boolean);
  return { page: route[0] || "dashboard", detail: route[1] || "" };
}

function renderPage() {
  const { page, detail } = currentRoute();
  const activePage = page === "employees" ? "employees" : page === "alerts" ? "alerts" : "dashboard";
  renderSidebar(document.querySelector("#sidebar"), data, activePage);
  renderHeader(document.querySelector("#header"), data);
  const mainContent = document.querySelector("#main-content");

  if (page === "employees" && detail === "new") {
    renderEmployeeForm(mainContent);
  } else if (page === "employees" && detail) {
    const employee = data.employees.find((item) => item.id === detail);
    if (employee) {
      renderEmployeeDetails(mainContent, employee, data.alerts);
    } else {
      mainContent.innerHTML = '<p class="dashboard-error" role="alert">Funcionario nao encontrado. <a href="#employees">Voltar para funcionarios</a></p>';
    }
  } else if (page === "employees") {
    renderEmployeesPage(mainContent, data.employees, employeeQuery, employeeSort);
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

document.addEventListener("change", (event) => {
  if (event.target.matches("[data-sort-employees]")) {
    employeeSort = event.target.value;
    rerenderCurrentPage();
  }
});

document.addEventListener("click", (event) => {
  const openButton = event.target.closest("[data-open-dialog]");
  if (openButton) {
    document.querySelector(`[data-filter-dialog="${openButton.dataset.openDialog}"]`).showModal();
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

async function initialize() {
  data = await getDashboardData();
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
