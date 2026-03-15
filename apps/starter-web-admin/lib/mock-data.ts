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

export const adminEntitlements = [
  { tier: "tracker", users: "8,402", source: "ios" },
  { tier: "free", users: "3,912", source: "starter" },
  { tier: "tracker", users: "166", source: "android" }
];

export const adminNotificationHealth = {
  optedIn: "71%",
  quietHoursEnabled: "18%",
  marketingMuted: "22%"
};

export const adminAuthHealth = {
  magicLinkSuccess: "94.2%",
  socialMix: [
    { label: "magic link", value: "63%" },
    { label: "apple", value: "21%" },
    { label: "google", value: "16%" }
  ],
  refreshFailures: "0.4%"
};
