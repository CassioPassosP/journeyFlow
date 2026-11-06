const SESSION_KEY = "journeyflow.api-session";
const API_PREFIX = "/__api";
const REQUEST_TIMEOUT_MS = 20000;

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export function getSession() {
  const value = window.sessionStorage.getItem(SESSION_KEY);
  if (!value) return null;

  try {
    return JSON.parse(value);
  } catch (error) {
    window.sessionStorage.removeItem(SESSION_KEY);
    throw new Error("A sessão salva está inválida. Entre novamente.", { cause: error });
  }
}

export function saveSession(session) {
  window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearSession() {
  window.sessionStorage.removeItem(SESSION_KEY);
}

export async function apiRequest(path, { method = "GET", body, authenticated = true } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";

  if (authenticated) {
    const session = getSession();
    if (!session?.token) throw new ApiError("Sua sessão expirou. Entre novamente.", 401);
    headers.Authorization = `Bearer ${session.token}`;
  }

  // Sem timeout, uma API parada deixa a tela "carregando" para sempre.
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    let response;
    try {
      response = await fetch(`${API_PREFIX}${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal
      });
    } catch (error) {
      if (error.name === "AbortError") {
        throw new ApiError("O servidor demorou demais para responder. Tente novamente.", 0);
      }
      throw new ApiError("Não foi possível conectar ao backend. Verifique se a API está em execução.", 0);
    }

    if (response.status === 204) return null;

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      if (response.status === 401 && authenticated) clearSession();
      const message = typeof payload === "object" && payload !== null
        ? payload.message || payload.detail || payload.error
        : payload;
      throw new ApiError(message || `Falha na API (${response.status}).`, response.status);
    }

    return payload;
  } finally {
    window.clearTimeout(timer);
  }
}

export async function login(email, password) {
  const session = await apiRequest("/auth/login", {
    method: "POST",
    body: { email, password },
    authenticated: false
  });
  saveSession(session);
  return session;
}