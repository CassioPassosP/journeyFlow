import { escapeHtml, iconMarkup } from "../utils/dom.js";

export function renderSummaryCards(container, data) {
  container.innerHTML = data.map((item) => `
    <article class="summary-card" aria-labelledby="summary-${escapeHtml(item.id)}">
      <div class="summary-card__top">
        <h2 class="summary-card__title" id="summary-${escapeHtml(item.id)}">${escapeHtml(item.title)}</h2>
        <span class="summary-card__icon summary-card__icon--${escapeHtml(item.tone)}">
          ${iconMarkup(item.icon)}
        </span>
      </div>
      <div class="summary-card__value-row">
        <span class="summary-card__value">${escapeHtml(item.value)}</span>
        <span class="summary-card__caption">${escapeHtml(item.caption)}</span>
      </div>
    </article>
  `).join("");
}
