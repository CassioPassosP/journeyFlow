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

function statusKey(value) {
  const normalized = String(value || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
  if (value === "FOLLOW_UP" || value === "TODAY" || /help|ajuda|suporte/.test(normalized)) {
    return "help";
  }
  if (value === "ATTENTION" || value === "THREE_DAYS" || /attention|atencao|alerta/.test(normalized)) {
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
  const value = alert.alertDate || alert.createdAt;
  if (!value) return 0;
  const timestamp = new Date(value).getTime();
  return Number.isNaN(timestamp) ? 0 : timestamp;
}

function mapAlert(alert, employeeById) {
  const employee = employeeById.get(alert.employeeId);
  const detail = alert.subtitle?.trim();
  const suggestion = alert.suggestion?.trim();
  const message = [alert.alertTitle, detail, suggestion].filter(Boolean).join(" — ");

  return {
    id: String(alert.id),
    backendId: alert.id,
    employeeId: alert.employeeId || "",
    message: message || "Alerta sem descrição",
    author: employee
      ? `${employee.name} - ${employee.role}`
      : alert.employeeId || "Funcionário",
    status: statusKey(alert.urgency),
    when: formatAlertDate(alert),
    timestamp: alertTimestamp(alert),
    confirmed: alert.confirmed,
    resolutionStatus: alert.status || "OPEN",
    reportId: alert.reportId,
    destination: alert.destination,
    urgency: alert.urgency,
    createdAt: alert.createdAt || alert.alertDate
  };
}

function applyActiveAlertStatuses(employees, alerts) {
  const priority = { normal: 0, attention: 1, help: 2 };
  const employeeById = new Map(employees.map((employee) => [String(employee.id), employee]));

  for (const alert of alerts) {
    if (alert.resolutionStatus === "DONE") continue;
    const employee = employeeById.get(String(alert.employeeId));
    if (!employee) continue;

    if ((priority[alert.status] ?? 0) > (priority[employee.status] ?? 0)) {
      employee.status = alert.status;
    }
  }
}
export async function loginAndLoadDashboard(email, password) {
  const session = await login(email, password);
  return getDashboardData(session);
}

export async function getDashboardData(session) {
  if (session?.role === "EMPLOYEE") return getEmployeeDashboardData(session);
  if (session?.role !== "MANAGER") throw new Error("Tipo de conta não reconhecido. Entre novamente.");

  const manager = await apiRequest(`/manager/${encodeURIComponent(session.userId)}`);
  const [stores, store, employeeRecords, alertRecords] = await Promise.all([
    apiRequest("/store"),
    apiRequest(`/store/${encodeURIComponent(manager.storeId)}`),
    getAllPages(`/employee?storeId=${encodeURIComponent(manager.storeId)}`),
    getAllPages(`/alert?storeId=${encodeURIComponent(manager.storeId)}`)
  ]);

  const employees = employeeRecords.map((employee) => mapEmployee(employee, store));
  const employeeById = new Map(employeeRecords.map((employee, index) => [employee.id, employees[index]]));
  const alerts = alertRecords
    .map((alert) => mapAlert(alert, employeeById))
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

  applyActiveAlertStatuses(employees, alerts);
  const counts = employees.reduce((result, employee) => {
    result[employee.status] += 1;
    return result;
  }, { normal: 0, attention: 0, help: 0 });

  const alertsByEmployee = new Map();
  for (const alert of alerts) {
    alertsByEmployee.set(alert.employeeId, (alertsByEmployee.get(alert.employeeId) || 0) + 1);
  }
  const topUpdates = employees
    .map((employee) => ({ name: employee.name.split(/\s+/)[0], count: alertsByEmployee.get(employee.id) || 0 }))
    .filter((employee) => employee.count > 0)
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);
  const totalEmployees = employees.length;
  const percent = (count) => totalEmployees ? Math.round((count / totalEmployees) * 100) : 0;

  return {
    unit: { name: store.storeName, updatedAt: "agora" },
    user: {
      name: manager.name,
      email: manager.email,
      role: ["RH", "HR"].includes(String(manager.position || "").toUpperCase())
        ? "RH"
        : "Gestor da unidade"
    },

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
    stores,
    currentManager: manager
  };
}

export async function refreshManagerAlerts(data) {
  const storeId = encodeURIComponent(data.currentManager.storeId);
  const [alertPage, employeePage] = await Promise.all([
    apiRequest(`/alert?storeId=${storeId}&page=0&size=100`),
    getAllPages(`/employee?storeId=${storeId}`)
  ]);
  const store = data.stores.find((item) => String(item.id) === String(data.currentManager.storeId));
  const refreshedEmployees = employeePage.map((employee) => mapEmployee(employee, store));
  const employeeById = new Map(refreshedEmployees.map((employee) => [employee.id, employee]));
  const refreshedAlerts = alertPage.content.map((alert) => mapAlert(alert, employeeById));
  const alertsById = new Map(data.alerts.map((alert) => [alert.id, alert]));
  const employeesChanged = refreshedEmployees.some((employee) => {
    const currentEmployee = data.employees.find((item) => item.id === employee.id);
    return !currentEmployee || !sameEmployee(currentEmployee, employee);
  });
  const alertsChanged = refreshedAlerts.some((alert) => {
    const currentAlert = alertsById.get(alert.id);
    return !currentAlert || !sameAlert(currentAlert, alert);
  });
  if (!employeesChanged && !alertsChanged) return data;

  for (const alert of refreshedAlerts) alertsById.set(alert.id, alert);

  const alerts = [...alertsById.values()]
    .map((alert) => {
      const employee = employeeById.get(alert.employeeId);
      return employee ? { ...alert, author: `${employee.name} - ${employee.role}` } : alert;
    })
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
  const employees = refreshedEmployees;
  applyActiveAlertStatuses(employees, alerts);

  const alertCounts = new Map();
  for (const alert of alerts) {
    alertCounts.set(alert.employeeId, (alertCounts.get(alert.employeeId) || 0) + 1);
  }
  const topUpdates = employees
    .map((employee) => ({
      name: employee.name.split(/\s+/)[0],
      count: alertCounts.get(employee.id) || 0
    }))
    .filter((employee) => employee.count > 0)
    .sort((a, b) => b.count - a.count)
    .slice(0, 6);

  const counts = employees.reduce((result, employee) => {
    result[employee.status] += 1;
    return result;
  }, { normal: 0, attention: 0, help: 0 });
  const totalEmployees = employees.length;
  const percent = (count) => totalEmployees ? Math.round((count / totalEmployees) * 100) : 0;
  const summary = data.summary.map((item) => {
    if (item.id === "attention") {
      return { ...item, value: counts.attention + counts.help };
    }
    if (item.id === "pending") {
      return { ...item, value: alerts.filter((alert) => !alert.confirmed).length };
    }
    return item;
  });
  const monthlyStatus = {
    totalFollowed: totalEmployees,
    items: [
      { id: "normal", label: "Normal", percent: percent(counts.normal), color: "success" },
      { id: "attention", label: "Atenção", percent: percent(counts.attention), color: "warning" },
      { id: "help", label: "Precisa de ajuda", percent: percent(counts.help), color: "danger" }
    ]
  };

  return {
    ...data,
    employees,
    alerts,
    recentAlerts: alerts.slice(0, 5),
    topUpdates,
    summary,
    monthlyStatus
  };
}

function sameAlert(left, right) {
  return left.message === right.message
    && left.author === right.author
    && left.status === right.status
    && left.when === right.when
    && left.confirmed === right.confirmed
    && left.resolutionStatus === right.resolutionStatus
    && left.reportId === right.reportId
    && left.destination === right.destination
    && left.urgency === right.urgency
    && left.createdAt === right.createdAt;
}

function sameEmployee(left, right) {
  return left.name === right.name
    && left.role === right.role
    && left.days === right.days
    && left.currentState === right.currentState
    && left.interaction === right.interaction
    && left.lastInteraction === right.lastInteraction;
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
      role: employee.position,
      points: balance.balance,
      history,
      chat,
    },
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

export async function updateOwnProfile(name, email) {
  return apiRequest("/auth/profile", {
    method: "PUT",
    body: { name, email }
  });
}

export async function sendEmployeeMessage(employeeId, content, pillId = null) {
  return apiRequest("/chat", {
    method: "POST",
    body: { employeeId, sender: "USER", content, source: "TEXT", pillId }
  });
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