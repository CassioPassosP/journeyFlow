const settingsKey = "journeyflow.settings";
const readNotificationsKey = "journeyflow.read-notifications";

function readStoredJson(key, fallback) {
  const storedValue = window.localStorage.getItem(key);
  if (storedValue === null) return fallback;

  try {
    return JSON.parse(storedValue);
  } catch (error) {
    throw new Error(`Não foi possível ler a preferência "${key}" do navegador.`, { cause: error });
  }
}

export function loadUserSettings(defaultUser) {
  const settings = readStoredJson(settingsKey, {});
  if (typeof settings !== "object" || settings === null || Array.isArray(settings)) {
    throw new Error("As configurações salvas do usuário são inválidas.");
  }

  const name = typeof settings.name === "string" ? settings.name : defaultUser.name;
  const email = typeof settings.email === "string" ? settings.email : defaultUser.email;
  const theme = settings.theme === "dark" ? "dark" : "light";

  return { name, email, theme };
}

export function saveUserSettings(settings) {
  window.localStorage.setItem(settingsKey, JSON.stringify(settings));
}

export function loadReadNotifications() {
  const readIds = readStoredJson(readNotificationsKey, []);
  if (!Array.isArray(readIds) || !readIds.every((id) => typeof id === "string")) {
    throw new Error("A lista de notificações lidas salva no navegador é inválida.");
  }
  return new Set(readIds);
}

export function saveReadNotifications(readIds) {
  window.localStorage.setItem(readNotificationsKey, JSON.stringify([...readIds]));
}
