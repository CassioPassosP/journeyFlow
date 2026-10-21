import { escapeHtml, getStatusConfig, getThemeColor } from "../utils/dom.js";

let chartInstance;

export function renderStatusChart(container, data) {
  chartInstance?.destroy();

  const rawTotal = data.items.reduce((sum, item) => sum + item.percent, 0);
  const segments = data.items.map((item) => ({
    ...item,
    percent: rawTotal > 0 ? (item.percent / rawTotal) * 100 : 0
  }));

  const legendMarkup = segments.map((item) => {
    const config = getStatusConfig(item.id);
    return `
      <li class="status-chart__legend-item">
        <span class="status-chart__dot status-chart__dot--${config.tone}" aria-hidden="true"></span>
        <span class="status-chart__legend-label">${escapeHtml(config.label)}</span>
        <span class="status-chart__legend-value">${Math.round(item.percent)}%</span>
      </li>
    `;
  }).join("");

  container.innerHTML = `
    <h2 class="dashboard-panel__title">Status mensal da equipe</h2>
    <div class="status-chart__content">
      <div class="status-chart__visual" role="img" aria-label="Distribuição do status dos colaboradores">
        <canvas class="status-chart__canvas"></canvas>
        <div class="status-chart__center">
          <span class="status-chart__total">${escapeHtml(data.totalFollowed)}</span>
          <span class="status-chart__total-label">colaboradores acompanhados</span>
        </div>
      </div>
      <ul class="status-chart__legend" aria-label="Legenda do status">
        ${legendMarkup}
      </ul>
    </div>
  `;

  const chartConstructor = window.Chart;

  if (!chartConstructor) {
    throw new Error("Chart.js não foi carregado.");
  }

  chartInstance = new chartConstructor(container.querySelector(".status-chart__canvas"), {
    type: "doughnut",
    data: {
      labels: segments.map((item) => getStatusConfig(item.id).label),
      datasets: [{
        data: segments.map((item) => item.percent),
        backgroundColor: segments.map((item) => getThemeColor(getStatusConfig(item.id).tone)),
        borderWidth: 0,
        hoverOffset: 4
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: "72%",
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => ` ${context.label}: ${Math.round(context.parsed)}%`
          }
        }
      }
    }
  });
}
