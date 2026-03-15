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

export function refreshSession(session: WebSession): WebSession {
  const suffix = Math.floor(Math.random() * 900 + 100);
  return {
    ...session,
    requestId: `web-mock-req-${suffix}`,
    correlationId: `web-corr-${suffix}`,
  };
}
