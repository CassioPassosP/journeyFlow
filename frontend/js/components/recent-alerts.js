import { escapeHtml, getStatusConfig, iconMarkup } from "../utils/dom.js";

export function renderRecentAlerts(container, data) {
  const alertsMarkup = data.map((alert) => {
    const status = getStatusConfig(alert.status);
    const icon = alert.status === "attention"
      ? "bellAttention"
      : alert.status === "help"
        ? "bellHelp"
        : "bell";
    const assetIconClass = icon === "bell" ? "" : " recent-alert__icon--asset";
    return `
      <article class="recent-alert">
        <span class="recent-alert__icon recent-alert__icon--${status.tone}${assetIconClass}">
          ${iconMarkup(icon)}
        </span>
        <div class="recent-alert__details">
          <time class="recent-alert__when">${escapeHtml(alert.when)}</time>
          <p class="recent-alert__message">${escapeHtml(alert.message)}</p>
          <span class="recent-alert__author">${escapeHtml(alert.author)}</span>
        </div>
        <span class="status-badge status-badge--${status.tone}">${escapeHtml(status.label)}</span>
      </article>
    `;
  }).join("");

  container.innerHTML = `
    <div class="recent-alerts__heading">
      <h2 class="dashboard-panel__title">Alertas recentes</h2>
      <span class="recent-alerts__count">${data.length} atualizações</span>
    </div>
    <div class="recent-alerts__list">${alertsMarkup}</div>
  `;
}
