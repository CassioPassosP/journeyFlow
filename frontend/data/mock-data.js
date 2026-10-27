const mockData = {
  unit: { name: "Loja Silva so - 1020", updatedAt: "hoje 10:23" },
  user: { name: "Thiago Silva", email: "thiago.silva@journeyflow.com", role: "Gestor da unidade" },
  menu: [
    { id: "dashboard", label: "Dashboard", icon: "dashboard", active: true },
    { id: "employees", label: "Funcionários", icon: "users" },
    { id: "alerts", label: "Alertas", icon: "alerts" },
    { id: "settings", label: "Configurações", icon: "settings" }
  ],
  sidebarTip: "Visualize, atualize e acompanhe a situação de cada colaborador da sua equipe de forma centralizada.",
  greeting: {
    subtitle: "Acompanhe a integração da equipe e priorize conversas que fazem diferença"
  },
  summary: [
    { id: "total", title: "Funcionários totais", value: 25, caption: "contratos ativos", icon: "onUsers", tone: "neutral" },
    { id: "learning", title: "Funcionários na trilha de aprendizagem", value: 16, caption: "Aprendizes", icon: "calendarCard", tone: "success" },
    { id: "attention", title: "Funcionários em atenção", value: 3, caption: "Atualizações", icon: "attention", tone: "warning" },
    { id: "pending", title: "Alertas não confirmados", value: 3, caption: "Atualizações aguardando confirmação", icon: "alertNok", tone: "danger" }
  ],
  monthlyStatus: {
    totalFollowed: 25,
    items: [
      { id: "normal", label: "Normal", percent: 56, color: "success" },
      { id: "attention", label: "Atenção", percent: 25, color: "warning" },
      { id: "help", label: "Precisa de ajuda", percent: 19, color: "danger" }
    ]
  },
  topUpdates: [
    { name: "João", count: 6 },
    { name: "Luísa", count: 4 },
    { name: "Cássio", count: 3 },
    { name: "Luísa C", count: 3 },
    { name: "Felipe", count: 2 },
    { name: "Gustavo", count: 1 }
  ],
  recentAlerts: [
    { id: 1, when: "Hoje - 10:27", message: "João apresentou dificuldade no treinamento da cozinha", author: "João Pedro Amaral - Jovem Aprendiz", status: "attention" },
    { id: 2, when: "Ontem - 10:27", message: "Luísa apresentou indícios de desmotivação no dia de hoje", author: "Luísa Chagas - Atendente", status: "help" },
    { id: 3, when: "10/12/26 - 10:27", message: "Gustavo apresentou dificuldade no treinamento de atendimento", author: "Gustavo Amaral - Atendente", status: "attention" }
  ],
  employees: [
    { id: "joao-pedro-amaral", name: "João pedro amaral", role: "Jovem Aprendiz", days: 20, status: "attention", interaction: "Hoje, 9:42", whatsapp: "", unit: "Loja Silva so - 1020", hiredAt: "2026-11-06" },
    { id: "gustavo-amaral", name: "Gustavo Amaral", role: "Atendente", days: 16, status: "attention", interaction: "Ontem, 9:23", whatsapp: "", unit: "Loja Silva so - 1020", hiredAt: "2026-11-10" },
    { id: "luisa-chagas", name: "Luísa Chagas", role: "Atendente", days: 5, status: "attention", interaction: "Hoje, 7:46", whatsapp: "", unit: "Loja Silva so - 1020", hiredAt: "2026-11-21" }
  ],
  alerts: [
    { id: "alert-1", employeeId: "joao-pedro-amaral", message: "João apresentou dificuldade no treinamento da cozinha", author: "João pedro amaral - Jovem Aprendiz", status: "attention", when: "12/12/2026 - 9:42", confirmed: false },
    { id: "alert-2", employeeId: "gustavo-amaral", message: "Gustavo apresentou dificuldade no treinamento de atendimento", author: "Gustavo Amaral - Atendente", status: "attention", when: "10/12/2026 - 9:23", confirmed: false },
    { id: "alert-3", employeeId: "luisa-chagas", message: "Luísa apresentou indícios de desmotivação no dia de hoje", author: "Luísa Chagas - Atendente", status: "help", when: "11/12/2026 - 7:46", confirmed: true }
  ]
};

export default mockData;
