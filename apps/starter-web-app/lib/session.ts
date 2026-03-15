export type WebSession = {
  authMode: "magic_link" | "social";
  environment: "mock";
  requestId: string;
  accessTokenState: "mock-issued";
  refreshStrategy: "rotating_refresh_token";
  correlationId: string;
};

export const currentSession: WebSession = {
  authMode: "magic_link",
  environment: "mock",
  requestId: "web-mock-req-001",
  accessTokenState: "mock-issued",
  refreshStrategy: "rotating_refresh_token",
  correlationId: "web-corr-001",
};
