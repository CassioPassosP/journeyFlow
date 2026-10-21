import { escapeHtml, getThemeColor } from "../utils/dom.js";

let chartInstance;

export function renderUpdatesChart(container, data) {
  chartInstance?.destroy();
  const maximum = Math.max(...data.map((item) => item.count), 6);

  container.innerHTML = `
    <h2 class="dashboard-panel__title">Colaboradores com mais atualizações</h2>
    <div class="updates-chart__chart">
      <canvas class="updates-chart__canvas" role="img" aria-label="Gráfico de atualizações por colaborador: ${data.map((item) => `${escapeHtml(item.name)}, ${escapeHtml(item.count)} atualizações`).join("; ")}"></canvas>
    </div>
    <ul class="visually-hidden" aria-label="Atualizações por colaborador">
      ${data.map((item) => `<li>${escapeHtml(item.name)}: ${escapeHtml(item.count)} atualizações</li>`).join("")}
    </ul>
  `;

  const chartConstructor = window.Chart;

  if (!chartConstructor) {
    throw new Error("Chart.js não foi carregado.");
  }

  chartInstance = new chartConstructor(container.querySelector(".updates-chart__canvas"), {
    type: "bar",
    data: {
      labels: data.map((item) => item.name),
      datasets: [{
        data: data.map((item) => item.count),
        backgroundColor: getThemeColor("warning"),
        borderRadius: 6,
        borderSkipped: "bottom",
        maxBarThickness: 26
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => ` ${context.parsed.y} atualizações`
          }
        }
      },
      scales: {
        x: {
          grid: { display: false },
          border: { display: false },
          ticks: {
            color: getThemeColor("text-muted"),
            font: { family: getComputedStyle(document.body).fontFamily, size: 10 }
          }
        },
        y: {
          beginAtZero: true,
          max: maximum,
          ticks: {
            stepSize: 1,
            color: getThemeColor("text-muted"),
            font: { family: getComputedStyle(document.body).fontFamily, size: 10 }
          },
          grid: {
            color: getThemeColor("border"),
            borderDash: [4, 4],
            drawTicks: false
          },
          border: { display: false }
        }
      }
    }
  });
}
