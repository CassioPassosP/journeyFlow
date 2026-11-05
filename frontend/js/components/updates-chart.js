import { escapeHtml, getThemeColor, tooltipStyle } from "../utils/dom.js";

let chartInstance;

function withAlpha(hex, alpha) {
  const value = hex.replace("#", "");
  const [r, g, b] = [0, 2, 4].map((index) => parseInt(value.slice(index, index + 2), 16));
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Degradê vertical: coral cheio no topo, mais leve na base.
function barGradient(context) {
  const { chart } = context;
  const area = chart.chartArea;
  const base = getThemeColor("chart-bar");
  if (!area) return base;
  const gradient = chart.ctx.createLinearGradient(0, area.top, 0, area.bottom);
  gradient.addColorStop(0, base);
  gradient.addColorStop(1, withAlpha(base, 0.55));
  return gradient;
}

export function renderUpdatesChart(container, data) {
  chartInstance?.destroy();
  const peak = Math.max(...data.map((item) => item.count), 4);
  // Poucos degraus no eixo Y (no máximo ~4) em vez de um por unidade.
  const step = Math.max(1, Math.ceil(peak / 4));
  const maximum = Math.ceil(peak / step) * step;

  container.innerHTML = `
    <h2 class="dashboard-panel__title">Colaboradores com mais atualizações</h2>
    <p class="dashboard-panel__subtitle">Relatos enviados no período</p>
    <div class="updates-chart__chart">
      <canvas class="updates-chart__canvas" role="img" aria-label="Gráfico de atualizações por colaborador: ${data.map((item) => `${escapeHtml(item.name)}, ${escapeHtml(item.count)} atualizações`).join("; ")}"></canvas>
    </div>
    <ul class="visually-hidden" aria-label="Atualizações por colaborador">
      ${data.map((item) => `<li>${escapeHtml(item.name)}: ${escapeHtml(item.count)} atualizações</li>`).join("")}
    </ul>
  `;

  const chartConstructor = window.Chart;

  if (!chartConstructor) {
    // Sem Chart.js (CDN bloqueado/offline) o resto do dashboard continua funcionando.
    console.error("Chart.js não foi carregado.");
    container.querySelector("canvas")?.replaceWith(Object.assign(document.createElement("p"), {
      className: "chart-fallback",
      textContent: "Não foi possível carregar o gráfico."
    }));
    return;
  }

  chartInstance = new chartConstructor(container.querySelector(".updates-chart__canvas"), {
    type: "bar",
    data: {
      labels: data.map((item) => item.name),
      datasets: [{
        data: data.map((item) => item.count),
        backgroundColor: (context) => barGradient(context),
        hoverBackgroundColor: getThemeColor("primary"),
        borderRadius: { topLeft: 8, topRight: 8, bottomLeft: 0, bottomRight: 0 },
        borderSkipped: false,
        maxBarThickness: 30,
        categoryPercentage: 0.7,
        barPercentage: 0.9
      }]
    },
    options: {
      animation: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? false : { duration: 450 },
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          ...tooltipStyle(),
          callbacks: {
            label: (context) => ` ${context.parsed.y} ${context.parsed.y === 1 ? "atualização" : "atualizações"}`
          }
        }
      },
      scales: {
        x: {
          grid: { display: false },
          border: { display: false },
          ticks: {
            padding: 8,
            color: getThemeColor("text-muted"),
            font: { family: getComputedStyle(document.body).fontFamily, size: 11, weight: 500 }
          }
        },
        y: {
          beginAtZero: true,
          max: maximum,
          ticks: {
            stepSize: step,
            padding: 10,
            color: getThemeColor("text-muted"),
            font: { family: getComputedStyle(document.body).fontFamily, size: 11, weight: 500 }
          },
          grid: {
            color: getThemeColor("chart-grid"),
            drawTicks: false
          },
          border: { display: false }
        }
      }
    }
  });
}