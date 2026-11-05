const iconFiles = {
  alerts: "alerts.svg",
  bell: "bell.svg",
  calendar: "calendar.svg",
  dashboard: "dashboard.svg",
  heart: "heart.svg",
  settings: "settings.svg",
  users: "users.svg"
};

const iconPaths = {
  alerts: '<path d="M10.3 4.3 2.8 17.2A2 2 0 0 0 4.5 20h15a2 2 0 0 0 1.7-2.8L13.7 4.3a2 2 0 0 0-3.4 0Z"/><path d="M12 9v4m0 3h.01"/>',
  bell: '<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/>',
  calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 11h18"/>',
  chat: '<path d="M21 11.5a8.5 8.5 0 0 1-12.5 7.5L3 21l1.5-5.5A8.5 8.5 0 1 1 21 11.5Z"/><path d="M8 11h8m-8 4h5"/>',
  dashboard: '<rect x="3" y="3" width="8" height="8" rx="1.5"/><rect x="13" y="3" width="8" height="5" rx="1.5"/><rect x="13" y="10" width="8" height="11" rx="1.5"/><rect x="3" y="13" width="8" height="8" rx="1.5"/>',
  gift: '<rect x="3" y="8" width="18" height="13" rx="2"/><path d="M12 8v13M3 12h18M12 8H7.5a2.5 2.5 0 1 1 2.4-3.2L12 8Zm0 0h4.5a2.5 2.5 0 1 0-2.4-3.2L12 8Z"/>',
  heart: '<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"/>',
  pills: '<path d="m10.5 20.5 9.9-9.9a5 5 0 0 0-7.1-7.1l-9.9 9.9a5 5 0 0 0 7.1 7.1Z"/><path d="m8 8 8 8"/>',
  progress: '<path d="M3 3v18h18"/><path d="m7 14 4-4 4 3 6-7"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1.1-1.56 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.56-1.1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33 1.65 1.65 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1.1 1.56 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82 1.65 1.65 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1.03Z"/>',
  users: '<path d="M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="10" cy="7" r="4"/><path d="M20 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>'
};

const iconAssets = {
  onUsers: "onUsers.svg",
  calendarCard: "calendar.svg",
  attention: "attention.svg",
  alertNok: "alertNok.svg",
  bellAttention: "bell-attention.svg",
  bellHelp: "bell-help.svg"
};

const statusConfig = {
  attention: { label: "Atenção", tone: "warning" },
  help: { label: "Precisa de ajuda", tone: "danger" },
  normal: { label: "Normal", tone: "success" }
};

export function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (character) => {
    const entities = {
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#39;"
    };
    return entities[character];
  });
}

export function iconMarkup(name, className = "") {
  const classes = ["icon", className].filter(Boolean).join(" ");
  const assetFile = iconAssets[name];

  if (assetFile) {
    return `<img class="${classes} icon--asset" src="assets/icons/${assetFile}" alt="" aria-hidden="true">`;
  }

  const pathMarkup = iconPaths[name];

  if ((!iconFiles[name] && !["chat", "gift", "pills", "progress"].includes(name)) || !pathMarkup) {
    throw new Error(`Ícone não cadastrado: ${name}`);
  }

  return `<svg class="${classes}" viewBox="0 0 24 24" aria-hidden="true" focusable="false">${pathMarkup}</svg>`;
}

export function getStatusConfig(status) {
  const config = statusConfig[status];

  if (!config) {
    throw new Error(`Status não cadastrado: ${status}`);
  }

  return config;
}

export function getThemeColor(name) {
  const color = getComputedStyle(document.documentElement)
    .getPropertyValue(`--color-${name}`)
    .trim();

  if (!color) {
    throw new Error(`Variável de cor não cadastrada: --color-${name}`);
  }

  return color;
}

export function getGreeting(hour = new Date().getHours()) {
  if (hour >= 5 && hour < 12) {
    return "Bom dia";
  }

  if (hour >= 12 && hour < 18) {
    return "Boa tarde";
  }

  return "Boa noite";
}

// Tooltip dos gráficos: pílula escura no tema claro e clara no escuro, sem caixinha de cor.
export function tooltipStyle() {
  return {
    backgroundColor: getThemeColor("text"),
    titleColor: getThemeColor("surface"),
    bodyColor: getThemeColor("surface"),
    titleFont: { weight: 600, size: 12 },
    bodyFont: { weight: 500, size: 12 },
    padding: { x: 12, y: 8 },
    cornerRadius: 10,
    displayColors: false,
    caretSize: 5,
    caretPadding: 6
  };
}