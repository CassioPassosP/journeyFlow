import mockData from "../../data/mock-data.js";
import { apiRequest, login } from "./api-client.js";

const PAGE_SIZE = 100;
const MAX_PAGES = 100;

async function getAllPages(path) {
  const items = [];
  for (let page = 0; page < MAX_PAGES; page += 1) {
    const result = await apiRequest(`${path}${path.includes("?") ? "&" : "?"}page=${page}&size=${PAGE_SIZE}`);
    items.push(...result.content);
    if (result.last || result.content.length === 0) return items;
  }
  throw new Error("A consulta excedeu o limite de páginas permitido.");
}

function statusKey(value, report) {
  const normalized = String(value || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
  if (report?.helpRequest || report?.kind === "SUPPORT_REQUEST" || /help|ajuda|suporte/.test(normalized)) {
    return "help";
  }
  if (report?.kind === "NEGATIVE_NO_REQUEST" || /attention|atencao|alerta/.test(normalized)) {
    return "attention";
  }
  return "normal";
}

function formatDateTime(value, fallback = "Sem interação") {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(date);
}

function formatUpdatedAt() {
  const time = new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" }).format(new Date());
  return `hoje às ${time}`;
}

function formatAlertDate(alert) {
  // new Date(null) vira 1970; sem data na API, mostramos que ela não foi informada.
  if (!alert.alertDate && !alert.createdAt) return "Data não informada";
  const date = new Date(alert.alertDate || alert.createdAt);
  if (Number.isNaN(date.getTime())) return "Data não informada";
  const datePart = new Intl.DateTimeFormat("pt-BR").format(date);
  const timePart = new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" }).format(date);
  return `${datePart} - ${timePart}`;
}

function mapEmployee(employee, store) {
  const hiredAt = employee.startDate;
  const hiredAtDate = hiredAt ? new Date(`${hiredAt}T00:00:00`) : null;
  const days = hiredAtDate && !Number.isNaN(hiredAtDate.getTime())
    ? Math.max(0, Math.floor((Date.now() - hiredAtDate.getTime()) / 86400000))
    : 0;

  return {
    id: employee.id,
    name: employee.name,
    role: employee.position,
    unit: store.storeName,
    storeId: employee.storeId,
    hiredAt,
    days,
    status: statusKey(employee.currentState),
    currentState: employee.currentState,
    interaction: formatDateTime(employee.lastInteraction),
    lastInteraction: employee.lastInteraction,
    // Valor numérico para ordenar sem precisar interpretar o texto formatado.
    lastInteractionAt: employee.lastInteraction ? new Date(employee.lastInteraction).getTime() || 0 : 0
  };
}

function alertTimestamp(alert) {
  if (!alert.alertDate && !alert.createdAt) return 0;
  const time = new Date(alert.alertDate || alert.createdAt).getTime();
  return Number.isNaN(time) ? 0 : time;
}

function mapAlert(alert, reportById, employeeById) {
  const report = reportById.get(alert.reportId);
  const employee = report ? employeeById.get(report.employeeId) : null;
  const detail = alert.subtitle?.trim();
  const message = [alert.alertTitle, detail].filter(Boolean).join(" — ");

  return {
    id: String(alert.id),
    backendId: alert.id,
    employeeId: report?.employeeId || "",
    message: message || report?.summary || "Alerta sem descrição",
    author: employee
      ? `${employee.name} - ${employee.role}`
      : report?.employeeId || "Funcionário",
    status: statusKey("", report),
    when: formatAlertDate(alert),
    timestamp: alertTimestamp(alert),
    confirmed: alert.confirmed,
    suggestion: alert.suggestion?.trim() || "",
    reportId: alert.reportId
  };
}

export async function loginAndLoadDashboard(email, password) {
  const session = await login(email, password);
  return getDashboardData(session);
}

export async function getDashboardData(session) {
  if (session?.role === "EMPLOYEE") return getEmployeeDashboardData(session);
  if (session?.role !== "MANAGER") throw new Error("Tipo de conta não reconhecido. Entre novamente.");

  // Duas rodadas de requisições em paralelo em vez de três em sequência.
  const [manager, reports] = await Promise.all([
    apiRequest(`/manager/${encodeURIComponent(session.userId)}`),
    getAllPages("/reports")
  ]);
  const [store, employeeRecords, alertRecords] = await Promise.all([
    apiRequest(`/store/${encodeURIComponent(manager.storeId)}`),
    getAllPages(`/employee?storeId=${encodeURIComponent(manager.storeId)}`),
    getAllPages(`/alert?storeId=${encodeURIComponent(manager.storeId)}`)
  ]);

  const employees = employeeRecords.map((employee) => mapEmployee(employee, store));
  const employeeById = new Map(employeeRecords.map((employee, index) => [employee.id, employees[index]]));
  const reportById = new Map(reports.map((report) => [report.id, report]));
  const alerts = alertRecords
    .map((alert) => mapAlert(alert, reportById, employeeById))
    .sort((a, b) => new Date(reportById.get(b.reportId)?.createdAt || 0) - new Date(reportById.get(a.reportId)?.createdAt || 0));

  const counts = employees.reduce((result, employee) => {
    result[employee.status] += 1;
    return result;
  }, { normal: 0, attention: 0, help: 0 });

  const reportsByEmployee = new Map();
  for (const report of reports) {
    reportsByEmployee.set(report.employeeId, (reportsByEmployee.get(report.employeeId) || 0) + 1);
  }
  const topUpdates = employees
    .map((employee) => ({ name: employee.name.split(/\s+/)[0], count: reportsByEmployee.get(employee.id) || 0 }))
    .filter((employee) => employee.count > 0)
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);
  const totalEmployees = employees.length;
  const percent = (count) => totalEmployees ? Math.round((count / totalEmployees) * 100) : 0;

  return {
    unit: { name: store.storeName, updatedAt: formatUpdatedAt() },
    user: { name: manager.name, email: manager.email, role: "Gestor da unidade" },
    employee: mockData.employee,
    menu: mockData.menu,
    sidebarTip: mockData.sidebarTip,
    greeting: mockData.greeting,
    employees,
    alerts,
    recentAlerts: alerts.slice(0, 5),
    summary: [
      { id: "total", title: "Funcionários totais", value: totalEmployees, caption: "colaboradores cadastrados", icon: "onUsers", tone: "neutral" },
      { id: "learning", title: "Na trilha de aprendizagem", value: employeeRecords.filter((employee) => employee.inLearningPath).length, caption: "Aprendizes", icon: "calendarCard", tone: "success" },
      { id: "attention", title: "Funcionários em atenção", value: counts.attention + counts.help, caption: "Atenção ou precisam de ajuda", icon: "attention", tone: "warning" },
      { id: "pending", title: "Alertas não confirmados", value: alerts.filter((alert) => !alert.confirmed).length, caption: "Aguardando confirmação", icon: "alertNok", tone: "danger" }
    ],
    monthlyStatus: {
      totalFollowed: totalEmployees,
      items: [
        { id: "normal", label: "Normal", percent: percent(counts.normal), color: "success" },
        { id: "attention", label: "Atenção", percent: percent(counts.attention), color: "warning" },
        { id: "help", label: "Precisa de ajuda", percent: percent(counts.help), color: "danger" }
      ]
    },
    topUpdates,
    stores: [store],
    currentManager: manager
  };
}

function redemptionReason(reward) {
  return `Resgate: ${reward.name}`;
}

async function getEmployeeDashboardData(session) {
  const employeeId = encodeURIComponent(session.userId);
  const employee = await apiRequest(`/employee/${employeeId}`);
  const [store, balance, pointEntries, chatEntries, pills, pillProgress] = await Promise.all([
    apiRequest(`/store/${encodeURIComponent(employee.storeId)}`),
    apiRequest(`/points/employee/${employeeId}/balance`),
    getAllPages(`/points?employeeId=${employeeId}`),
    getAllPages(`/chat?employeeId=${employeeId}`),
    apiRequest("/pills"),
    apiRequest(`/pillprogress?employeeId=${employeeId}`)
  ]);
  const history = pointEntries.map((entry) => ({
    id: String(entry.id),
    emoji: entry.points < 0 ? "🎁" : "⭐",
    label: entry.reason,
    date: formatDateTime(entry.createdAt),
    points: entry.points
  }));
  const rewards = mockData.employee.rewards;
  const redeemedRewardIds = rewards
    .filter((reward) => pointEntries.some((entry) => entry.points < 0 && entry.reason === redemptionReason(reward)))
    .map((reward) => reward.id);
  const chat = [...chatEntries]
    .sort((a, b) => new Date(a.dateSent || a.createdAt) - new Date(b.dateSent || b.createdAt))
    .map((entry) => ({
      author: entry.sender === "USER" ? "employee" : "bot",
      text: entry.content,
      pillId: entry.pillId,
      time: formatDateTime(entry.dateSent || entry.createdAt)
    }));

  return {
    unit: { name: store.storeName, updatedAt: formatUpdatedAt() },
    user: { name: employee.name, email: employee.email, role: "Colaborador" },
    employee: {
      ...mockData.employee,
      role: employee.position,
      points: balance.balance,
      history,
      chat,
      redeemedRewardIds
    },
    menu: mockData.menu,
    sidebarTip: mockData.sidebarTip,
    greeting: mockData.greeting,
    employees: [],
    alerts: [],
    recentAlerts: [],
    pills,
    pillProgress,
    stores: [store],
    currentEmployee: employee
  };
}

export async function createEmployee(payload) {
  return apiRequest("/employee", { method: "POST", body: payload });
}

export async function sendEmployeeMessage(employeeId, content, pillId = null) {
  return apiRequest("/chat", {
    method: "POST",
    body: { employeeId, sender: "USER", content, source: "TEXT", pillId }
  });
}

// O resgate era só uma conta feita no navegador e sumia ao recarregar a página.
// A API de pontos aceita lançamentos negativos, então o resgate passa a ser gravado.
export async function redeemReward(employeeId, reward) {
  return apiRequest("/points", {
    method: "POST",
    body: { employeeId, reason: redemptionReason(reward), points: -Math.abs(reward.points) }
  });
}

export async function startEmployeePillQuiz(employeeId) {
  return apiRequest(`/chat/quiz/${encodeURIComponent(employeeId)}`, { method: "POST" });
}

export async function finishEmployeeConversation(employeeId) {
  return apiRequest(`/chat/finish/${encodeURIComponent(employeeId)}`, { method: "POST" });
}

export async function confirmAlert(alertId) {
  return apiRequest(`/alert/${encodeURIComponent(alertId)}/confirmed`, {
    method: "PATCH",
    body: { confirmed: true }
  });
}