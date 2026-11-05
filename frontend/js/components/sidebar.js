import { escapeHtml, iconMarkup } from "../utils/dom.js";

const employeeMenu = [
  { id: "home", label: "Início", icon: "dashboard" },
  { id: "conversation", label: "Conversa", icon: "chat" },
  { id: "pills", label: "Pílulas de conhecimento", icon: "pills" },
  { id: "rewards", label: "Minhas recompensas", icon: "gift" },
  { id: "progress", label: "Meu progresso", icon: "progress" },
  { id: "settings", label: "Configurações", icon: "settings" }
];

function isCurrent(item, activePage) {
  return activePage === item.id
    || (activePage.startsWith("employees") && item.id === "employees")
    || (activePage.startsWith("alerts") && item.id === "alerts");
}

export function renderSidebar(container, data, activePage = "dashboard", role = "manager") {
  const menu = role === "employee" ? employeeMenu : data.menu;

  // O menu só muda quando o perfil muda. Nas demais navegações apenas o item ativo é
  // atualizado, o que evita piscar e preserva o foco do teclado nos links.
  const signature = `${role}:${menu.map((item) => item.id).join(",")}`;
  if (container.dataset.signature === signature) {
    container.querySelectorAll(".sidebar__link").forEach((link, index) => {
      if (isCurrent(menu[index], activePage)) {
        link.setAttribute("aria-current", "page");
      } else {
        link.removeAttribute("aria-current");
      }
    });
    return;
  }
  container.dataset.signature = signature;

  const menuItems = menu.map((item) => {
    const currentPage = isCurrent(item, activePage) ? ' aria-current="page"' : "";
    const href = role === "employee" ? `#employee/${item.id}` : `#${item.id}`;
    return `
      <a class="sidebar__link" href="${escapeHtml(href)}"${currentPage}>
        ${iconMarkup(item.icon)}
        <span>${escapeHtml(item.label)}</span>
      </a>
    `;
  }).join("");

  container.innerHTML = `
    <div class="sidebar">
      <div class="sidebar__top">
        <a class="sidebar__brand" href="${role === "employee" ? "#employee/home" : "#dashboard"}" aria-label="JourneyFlow, início">
          <img class="sidebar__brand-logo" src="assets/icons/logo.svg" alt="" aria-hidden="true">
          <span>JourneyFlow</span>
        </a>
        <nav class="sidebar__nav" aria-label="Menu principal">
          ${menuItems}
        </nav>
      </div>
      <aside class="sidebar__tip" aria-label="${role === "employee" ? "Ajuda" : "Dica"}">
        ${iconMarkup("heart", "sidebar__tip-icon")}
        <p>${escapeHtml(role === "employee"
          ? "Precisa de ajuda? Nossa equipe está aqui para apoiar você sempre que precisar."
          : data.sidebarTip)}</p>
        ${role === "employee" ? '<a class="employee-sidebar__support" href="mailto:suporte@journeyflow.com">Falar com o suporte</a>' : ""}
      </aside>
    </div>
  `;
}