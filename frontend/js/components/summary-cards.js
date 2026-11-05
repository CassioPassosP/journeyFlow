import { escapeHtml, iconMarkup } from "../utils/dom.js";

// Os ícones de raster antigos traziam fundo próprio e não acompanhavam o tema escuro.
// Em vez deles usamos os ícones vetoriais, que herdam a cor do tom do card.
const vectorIcons = {
  onUsers: "users",
  calendarCard: "calendar",
  attention: "heart",
  alertNok: "alerts"
};

export function renderSummaryCards(container, data) {
  container.innerHTML = data.map((item) => `
    <article class="summary-card" aria-labelledby="summary-${escapeHtml(item.id)}">
      <div class="summary-card__top">
        <h2 class="summary-card__title" id="summary-${escapeHtml(item.id)}">${escapeHtml(item.title)}</h2>
        <span class="summary-card__icon summary-card__icon--${escapeHtml(item.tone)}">
          ${iconMarkup(vectorIcons[item.icon] || item.icon)}
        </span>
      </div>
      <div class="summary-card__value-row">
        <span class="summary-card__value">${escapeHtml(item.value)}</span>
        <span class="summary-card__caption">${escapeHtml(item.caption)}</span>
      </div>
    </article>
  `).join("");
}