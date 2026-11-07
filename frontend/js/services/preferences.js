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

// Nome/e-mail e notificações lidas pertencem a UMA conta. Antes eram gravados numa
// chave única e, ao trocar de usuário no mesmo navegador, a próxima conta herdava
// o nome e o e-mail da anterior. O tema continua sendo uma preferência do dispositivo.
function scopedKey(base, userKey) {
  return userKey ? `${base}.${userKey}` : base;
}

export function loadTheme() {
  try {
    const settings = readStoredJson(settingsKey, {});
    return settings?.theme === "dark" ? "dark" : "light";
  } catch {
    return "light";
  }
}

export function loadUserSettings(defaultUser, userKey) {
  const settings = readStoredJson(scopedKey(settingsKey, userKey), {});
  if (typeof settings !== "object" || settings === null || Array.isArray(settings)) {
    throw new Error("As configurações salvas do usuário são inválidas.");
  }

  const theme = settings.theme === "dark" ? "dark" : "light";

  return { name: defaultUser.name, email: defaultUser.email, theme };
}

export function saveUserSettings(settings) {
  window.localStorage.setItem(settingsKey, JSON.stringify({ theme: settings.theme }));
}

export function loadReadNotifications(userKey) {
  const readIds = readStoredJson(scopedKey(readNotificationsKey, userKey), []);
  if (!Array.isArray(readIds) || !readIds.every((id) => typeof id === "string")) {
    throw new Error("A lista de notificações lidas salva no navegador é inválida.");
  }
  return new Set(readIds);
}

export function saveReadNotifications(readIds, userKey) {
  window.localStorage.setItem(scopedKey(readNotificationsKey, userKey), JSON.stringify([...readIds]));
}
