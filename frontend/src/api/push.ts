import { api } from "./client";

export const pushApi = {
  subscribe: (subscription: PushSubscriptionJSON) =>
    api.post<void>("/api/push/subscribe", {
      endpoint: subscription.endpoint,
      keys: subscription.keys,
    }),
  unsubscribe: (endpoint: string) => api.post<void>("/api/push/unsubscribe", { endpoint }),
};
