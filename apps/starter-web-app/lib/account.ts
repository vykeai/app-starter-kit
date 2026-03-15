export type WebUserProfile = {
  id: string;
  email: string;
  displayName: string;
  loginMethod: "magic_link" | "apple" | "google";
  preferences: {
    theme: string;
    enabledCategories: string[];
  };
};

export const currentUser: WebUserProfile = {
  id: "starter-user-1",
  email: "reader@onlystack.dev",
  displayName: "Elara North",
  loginMethod: "magic_link",
  preferences: {
    theme: "moss",
    enabledCategories: ["activity", "transactional"],
  },
};
