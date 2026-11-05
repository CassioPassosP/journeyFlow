import { escapeHtml, getStatusConfig, getThemeColor, tooltipStyle } from "../utils/dom.js";

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
      <li class="status-chart__legend-item" style="--pct:${Math.round(item.percent)}">
        <span class="status-chart__dot status-chart__dot--${config.tone}" aria-hidden="true"></span>
        <span class="status-chart__legend-label">${escapeHtml(config.label)}</span>
        <span class="status-chart__legend-value">${Math.round(item.percent)}%</span>
      </li>
    `;
  }).join("");

  container.innerHTML = `
    <h2 class="dashboard-panel__title">Status mensal da equipe</h2>
    <p class="dashboard-panel__subtitle">Como cada colaborador reportou se sentir</p>
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
    // Sem Chart.js (CDN bloqueado/offline) o resto do dashboard continua funcionando.
    console.error("Chart.js não foi carregado.");
    // A legenda ao lado já traz os percentuais; só removemos o canvas vazio.
    container.querySelector("canvas")?.remove();
    return;
  }

  chartInstance = new chartConstructor(container.querySelector(".status-chart__canvas"), {
    type: "doughnut",
    data: {
      labels: segments.map((item) => getStatusConfig(item.id).label),
      datasets: [{
        data: segments.map((item) => item.percent),
        backgroundColor: segments.map((item) => getThemeColor(getStatusConfig(item.id).tone)),
        borderWidth: 0,
        borderRadius: 8,
        spacing: 3,
        hoverOffset: 3
      }]
    },
    options: {
      animation: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? false : { duration: 450 },
      responsive: true,
      maintainAspectRatio: false,
      cutout: "78%",
      plugins: {
        legend: { display: false },
        tooltip: {
          ...tooltipStyle(),
          callbacks: {
            label: (context) => ` ${context.label}: ${Math.round(context.parsed)}%`
          }
        }
      }
    }
  });
}