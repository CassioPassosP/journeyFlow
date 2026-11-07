export const ALERT_REMINDER_INTERVAL_MS = 24 * 60 * 60 * 1000;

export function isNotificationRead(notification, readAtById, now = Date.now()) {
  if (notification.kind === "alert" && notification.confirmed) return true;

  const id = String(notification.id);
  const readAt = readAtById instanceof Map
    ? readAtById.get(id)
    : readAtById?.has?.(id) ? now : undefined;
  if (!Number.isFinite(readAt)) return false;

  return notification.kind !== "alert" || now - readAt < ALERT_REMINDER_INTERVAL_MS;
}

function dateAtStartOfDay(value) {
  if (!value) return null;
  const date = new Date(`${value}T00:00:00`);
  return Number.isNaN(date.getTime()) ? null : date;
}

function daysBetween(start, end) {
  const startUtc = Date.UTC(start.getFullYear(), start.getMonth(), start.getDate());
  const endUtc = Date.UTC(end.getFullYear(), end.getMonth(), end.getDate());
  return Math.floor((endUtc - startUtc) / 86400000);
}

function formatDate(value) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short" }).format(value);
}

function formatCompletionDate(value) {
  if (!value) return "Conclusão registrada";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Conclusão registrada";
  return `Concluída em ${new Intl.DateTimeFormat("pt-BR", { dateStyle: "short" }).format(date)}`;
}

export function buildEmployeeNotifications(employee, pills = [], pillProgress = []) {
  const notifications = [];
  const today = new Date();
  const startDate = dateAtStartOfDay(employee?.startDate);
  const elapsedDays = startDate ? Math.max(0, daysBetween(startDate, today)) : null;
  const completedIds = new Set(pillProgress.map((progress) => String(progress.pillId)));
  const pillById = new Map(pills.map((pill) => [String(pill.id), pill]));

  if (elapsedDays !== null) {
    const scheduledPills = [...pills]
      .filter((pill) => Number.isFinite(Number(pill.dayFrom)))
      .sort((left, right) => Number(left.dayFrom) - Number(right.dayFrom));

    for (const pill of scheduledPills) {
      const pillId = String(pill.id);
      if (completedIds.has(pillId)) continue;

      const dayFrom = Number(pill.dayFrom);
      const releaseDate = new Date(startDate);
      releaseDate.setDate(releaseDate.getDate() + dayFrom);

      if (dayFrom <= elapsedDays) {
        notifications.push({
          id: `employee-pill-available:${pillId}`,
          kind: "employee",
          status: "success",
          message: `Pílula liberada para praticar com o bot: ${pill.title}`,
          author: "Trilha de aprendizagem",
          when: `Liberada em ${formatDate(releaseDate)}`,
          destination: "#employee/conversation",
          createdAt: releaseDate.getTime()
        });
      }
    }

    const nextPill = scheduledPills.find((pill) => (
      !completedIds.has(String(pill.id)) && Number(pill.dayFrom) > elapsedDays
    ));
    if (nextPill) {
      const daysUntilRelease = Number(nextPill.dayFrom) - elapsedDays;
      notifications.push({
        id: `employee-pill-upcoming:${nextPill.id}`,
        kind: "employee",
        status: "info",
        message: `${nextPill.title} será liberada em ${daysUntilRelease} ${daysUntilRelease === 1 ? "dia" : "dias"}`,
        author: "Próxima pílula",
        when: "Na sua trilha de aprendizagem",
        destination: "#employee/pills",
        createdAt: 0
      });
    }
  }

  pillProgress
    .filter((progress) => pillById.has(String(progress.pillId)))
    .sort((left, right) => new Date(right.completedAt || 0) - new Date(left.completedAt || 0))
    .slice(0, 3)
    .forEach((progress) => {
      const pill = pillById.get(String(progress.pillId));
      const completedAt = progress.completedAt ? new Date(progress.completedAt).getTime() : 0;
      notifications.push({
        id: `employee-pill-completed:${progress.pillId}`,
        kind: "employee",
        status: "success",
        message: `Pílula concluída: ${pill.title}`,
        author: "Seu progresso",
        when: formatCompletionDate(progress.completedAt),
        destination: "#employee/progress",
        createdAt: Number.isFinite(completedAt) ? completedAt : 0
      });
    });

  return notifications.sort((left, right) => right.createdAt - left.createdAt);
}
