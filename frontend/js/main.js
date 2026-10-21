import { getDashboardData } from "./services/data-service.js";
import { renderSidebar } from "./components/sidebar.js";
import { renderHeader } from "./components/header.js";
import { renderSummaryCards } from "./components/summary-cards.js";
import { renderStatusChart } from "./components/status-chart.js";
import { renderUpdatesChart } from "./components/updates-chart.js";
import { renderRecentAlerts } from "./components/recent-alerts.js";
import { escapeHtml, getGreeting } from "./utils/dom.js";

async function renderDashboard() {
  const data = await getDashboardData();
  const firstName = data.user.name.trim().split(/\s+/)[0];

  renderSidebar(document.querySelector("#sidebar"), data);
  renderHeader(document.querySelector("#header"), data);
  renderSummaryCards(document.querySelector("#summary-cards"), data.summary);
  renderStatusChart(document.querySelector("#status-chart"), data.monthlyStatus);
  renderUpdatesChart(document.querySelector("#updates-chart"), data.topUpdates);
  renderRecentAlerts(document.querySelector("#recent-alerts"), data.recentAlerts);

  document.querySelector("#greeting").innerHTML = `
    <h1 id="greeting-title">${getGreeting()}, ${escapeHtml(firstName)}</h1>
    <p>${escapeHtml(data.greeting.subtitle)}</p>
  `;
}

renderDashboard().catch((error) => {
  console.error("Não foi possível carregar o dashboard JourneyFlow.", error);
  const mainContent = document.querySelector("#main-content");
  mainContent.innerHTML = `
    <p class="dashboard-error" role="alert">
      Não foi possível carregar o dashboard. Atualize a página e tente novamente.
    </p>
  `;
});
