const roleLabels = {
  EMPLOYEE: "Funcionário",
  EMPLOY: "Funcionário",
  RH: "RH",
  HR: "RH",
  MANAGER: "Gestor da unidade"
};

export function normalizeRole(role) {
  return String(role ?? "").trim().toUpperCase();
}

export function formatRoleLabel(role) {
  const label = String(role ?? "").trim();
  return roleLabels[normalizeRole(label)] || label;
}

export function isEmployeeRole(role) {
  return ["EMPLOYEE", "EMPLOY"].includes(normalizeRole(role));
}

export function isManagerRole(role) {
  return ["MANAGER", "RH", "HR"].includes(normalizeRole(role));
}
