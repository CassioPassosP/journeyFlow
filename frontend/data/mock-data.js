const mockData = {
  unit: { name: "Loja Silva so - 1020", updatedAt: "hoje 10:23" },
  user: { name: "Thiago silva", role: "Gestor da unidade" },
  notifications: 3,
  menu: [
    { id: "dashboard", label: "Dashboard", icon: "dashboard", active: true },
    { id: "employees", label: "Funcionários", icon: "users" },
    { id: "alerts", label: "Alertas", icon: "alerts" }
  ],
  sidebarTip: "Visualize, atualize e acompanhe a situação de cada colaborador da sua equipe de forma centralizada.",
  greeting: {
    subtitle: "Acompanhe a integração da equipe e priorize conversas que fazem diferença"
  },
  summary: [
    { id: "total", title: "Funcionários totais", value: 25, caption: "contratos ativos", icon: "users", tone: "neutral" },
    { id: "learning", title: "Funcionários na trilha de aprendizagem", value: 16, caption: "Aprendizes", icon: "calendar", tone: "success" },
    { id: "attention", title: "Funcionários em atenção", value: 3, caption: "Atualizações", icon: "heart", tone: "warning" },
    { id: "pending", title: "Alertas não confirmados", value: 3, caption: "Atualizações aguardando confirmação", icon: "bell", tone: "danger" }
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
  ]
};

export default mockData;
