export type WebNotificationPreferences = {
  pushEnabled: boolean;
  emailEnabled: boolean;
  enabledCategories: string[];
  quietHoursEnabled: boolean;
  quietHoursStart: string | null;
  quietHoursEnd: string | null;
  urgentBreaksQuietHours: boolean;
  batchSoonNotifications: boolean;
};

export const currentNotificationPreferences: WebNotificationPreferences = {
  pushEnabled: true,
  emailEnabled: true,
  enabledCategories: ["activity", "transactional"],
  quietHoursEnabled: true,
  quietHoursStart: "22:00",
  quietHoursEnd: "07:00",
  urgentBreaksQuietHours: true,
  batchSoonNotifications: false,
};
