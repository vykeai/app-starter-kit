export type NetworkContext = {
  requestId: string;
  environment: "mock";
  authMode: "magic_link" | "social";
};

export const currentNetworkContext: NetworkContext = {
  requestId: "web-mock-req-001",
  environment: "mock",
  authMode: "magic_link",
};
