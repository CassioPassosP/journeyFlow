import { escapeHtml, getStatusConfig, iconMarkup } from "../utils/dom.js";

export function renderRecentAlerts(container, data) {
  const alertsMarkup = data.map((alert) => {
    const status = getStatusConfig(alert.status);
    const icon = alert.status === "help" ? "alerts" : "bell";
    return `
      <article class="recent-alert">
        <span class="recent-alert__icon recent-alert__icon--${status.tone}">
          ${iconMarkup(icon)}
        </span>
        <div class="recent-alert__details">
          <p class="recent-alert__message" title="${escapeHtml(alert.message)}">${escapeHtml(alert.message)}</p>
          <span class="recent-alert__author">${escapeHtml(alert.author)} · <time class="recent-alert__when" style="display:inline">${escapeHtml(alert.when)}</time></span>
        </div>
        <span class="status-badge status-badge--${status.tone}">${escapeHtml(status.label)}</span>
      </article>
    `;
  }).join("");

  container.innerHTML = `
    <div class="recent-alerts__heading">
      <h2 class="dashboard-panel__title">Alertas recentes</h2>
      <span class="recent-alerts__count">${data.length} ${data.length === 1 ? "atualização" : "atualizações"}</span>
    </div>
    <div class="recent-alerts__list">${alertsMarkup}</div>
  `;
}