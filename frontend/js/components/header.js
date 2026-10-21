import { escapeHtml, iconMarkup } from "../utils/dom.js";

export function renderHeader(container, data) {
  const firstName = data.user.name.trim().split(/\s+/)[0];
  const initials = data.user.name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((name) => name[0])
    .join("")
    .toLocaleUpperCase("pt-BR");

  container.innerHTML = `
    <div class="header">
      <div class="header__unit-info">
        <p class="header__unit">${escapeHtml(data.unit.name)}</p>
        <p class="header__updated">Atualizado ${escapeHtml(data.unit.updatedAt)}</p>
      </div>
      <div class="header__account">
        <button class="header__notifications" type="button" aria-label="${escapeHtml(data.notifications)} notificações não lidas">
          ${iconMarkup("bell")}
          <span class="header__badge" aria-hidden="true">${escapeHtml(data.notifications)}</span>
        </button>
        <div class="header__profile">
          <span class="header__avatar" aria-hidden="true">${escapeHtml(initials)}</span>
          <span class="header__profile-copy">
            <span class="header__name">${escapeHtml(firstName)}</span>
            <span class="header__role">${escapeHtml(data.user.role)}</span>
          </span>
        </div>
      </div>
    </div>
  `;
}
