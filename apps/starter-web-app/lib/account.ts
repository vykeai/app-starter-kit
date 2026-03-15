export type WebUserProfile = {
  id: string;
  email: string;
  displayName: string;
  preferences: {
    theme: string;
    enabledCategories: string[];
  };
};

export const currentUser: WebUserProfile = {
  id: "starter-user-1",
  email: "reader@onlystack.dev",
  displayName: "Elara North",
  preferences: {
    theme: "moss",
    enabledCategories: ["activity", "transactional"],
  },
};
