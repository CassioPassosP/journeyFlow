import { escapeHtml, iconMarkup } from "../utils/dom.js";

export function renderHeader(container, data, { alerts, readNotifications, notificationsOpen = false, notificationError = "" }) {
  const firstName = data.user.name.trim().split(/\s+/)[0];
  const initials = data.user.name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((name) => name[0])
    .join("")
    .toLocaleUpperCase("pt-BR");
  const unreadCount = alerts.filter((alert) => !readNotifications.has(alert.id)).length;
  const notificationItems = alerts.map((alert) => `
    <button class="notification-item${readNotifications.has(alert.id) ? " notification-item--read" : ""}" type="button" data-open-notification="${escapeHtml(alert.id)}">
      <span class="notification-item__dot notification-item__dot--${escapeHtml(alert.status)}" aria-hidden="true"></span>
      <span class="notification-item__copy">
        <strong>${escapeHtml(alert.message)}</strong>
        <small>${escapeHtml(alert.author)} · ${escapeHtml(alert.when)}</small>
      </span>
      ${readNotifications.has(alert.id) ? "" : '<span class="notification-item__unread" aria-label="Não lida"></span>'}
    </button>
  `).join("");

  container.innerHTML = `
    <div class="header">
      <div class="header__unit-info">
        <p class="header__unit">${escapeHtml(data.unit.name)}</p>
        <p class="header__updated">Atualizado ${escapeHtml(data.unit.updatedAt)}</p>
      </div>
      <div class="header__account">
        <button class="header__notifications" type="button" data-toggle-notifications aria-label="Notificações${unreadCount ? `, ${unreadCount} não lidas` : ""}" aria-expanded="${notificationsOpen}" aria-controls="notification-popover">
          ${iconMarkup("bell")}
          ${unreadCount ? `<span class="header__badge" aria-hidden="true">${escapeHtml(unreadCount)}</span>` : ""}
        </button>
        <section id="notification-popover" class="notification-popover" aria-label="Notificações" ${notificationsOpen ? "" : "hidden"}>
          <div class="notification-popover__heading">
            <h2>Notificações</h2>
            <button type="button" data-mark-all-read${unreadCount ? "" : " disabled"}>Marcar todas como lidas</button>
          </div>
          ${notificationError ? `<p class="notification-popover__error" role="alert">${escapeHtml(notificationError)}</p>` : ""}
          <div class="notification-popover__list">${notificationItems || '<p class="notification-popover__empty">Você não tem notificações.</p>'}</div>
          <a class="notification-popover__footer" href="#alerts">Ver todos os alertas</a>
        </section>
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
