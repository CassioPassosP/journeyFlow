import { escapeHtml, iconMarkup } from "../utils/dom.js";

export function renderSidebar(container, data) {
  const menuItems = data.menu.map((item) => {
    const currentPage = item.active ? ' aria-current="page"' : "";
    return `
      <a class="sidebar__link" href="#${escapeHtml(item.id)}"${currentPage}>
        ${iconMarkup(item.icon)}
        <span>${escapeHtml(item.label)}</span>
      </a>
    `;
  }).join("");

  container.innerHTML = `
    <div class="sidebar">
      <div class="sidebar__top">
        <a class="sidebar__brand" href="#dashboard" aria-label="JourneyFlow, início">
          <img class="sidebar__brand-logo" src="assets/icons/logo.svg" alt="" aria-hidden="true">
          <span>JourneyFlow</span>
        </a>
        <nav class="sidebar__nav" aria-label="Menu principal">
          ${menuItems}
        </nav>
      </div>
      <aside class="sidebar__tip" aria-label="Dica">
        ${iconMarkup("heart", "sidebar__tip-icon")}
        <p>${escapeHtml(data.sidebarTip)}</p>
      </aside>
    </div>
  `;
}
