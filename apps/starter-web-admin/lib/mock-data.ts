export const adminMetrics = [
  { label: "Active users", value: "12,480", delta: "+8.2%" },
  { label: "Pending reviews", value: "31", delta: "-12%" },
  { label: "Failed jobs", value: "2", delta: "stable" },
  { label: "Support SLA", value: "47m", delta: "green" }
];

export const adminQueues = [
  { name: "Email delivery", health: "Healthy", backlog: 18 },
  { name: "Media processing", health: "Watching", backlog: 42 },
  { name: "Push dispatch", health: "Healthy", backlog: 11 }
];
