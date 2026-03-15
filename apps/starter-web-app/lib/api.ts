import { currentSession } from "./session";

export type NetworkContext = {
  requestId: string;
  correlationId: string;
  environment: "mock";
  authMode: "magic_link" | "social";
  clientPlatform: "web";
  refreshStrategy: "rotating_refresh_token";
};

export const currentNetworkContext: NetworkContext = {
  requestId: currentSession.requestId,
  correlationId: currentSession.correlationId,
  environment: currentSession.environment,
  authMode: currentSession.authMode,
  clientPlatform: "web",
  refreshStrategy: currentSession.refreshStrategy,
};
