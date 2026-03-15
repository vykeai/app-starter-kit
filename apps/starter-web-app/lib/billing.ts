export type WebEntitlement = {
  tier: "free" | "tracker";
  source: "starter" | "ios" | "android" | "web";
  features: string[];
  renewsAt: string | null;
};

export const currentEntitlement: WebEntitlement = {
  tier: "tracker",
  source: "starter",
  features: ["basic_logging", "exercise_library", "advanced_analytics", "export_data"],
  renewsAt: "2026-04-14T12:00:00Z",
};
