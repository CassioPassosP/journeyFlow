import mockData from "../../data/mock-data.js";

const simulatedDelay = 120;

function waitForMockResponse() {
  return new Promise((resolve) => {
    window.setTimeout(resolve, simulatedDelay);
  });
}

export async function getDashboardData() {
  await waitForMockResponse();
  return mockData;
}

export async function getSummary() {
  const data = await getDashboardData();
  return data.summary;
}

export async function getRecentAlerts() {
  const data = await getDashboardData();
  return data.recentAlerts;
}

export async function getMonthlyStatus() {
  const data = await getDashboardData();
  return data.monthlyStatus;
}

export async function getTopUpdates() {
  const data = await getDashboardData();
  return data.topUpdates;
}
