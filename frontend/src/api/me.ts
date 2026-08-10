import { api } from "./client";
import type { UserAccount } from "../types";

export const meApi = {
  get: () => api.get<UserAccount>("/api/me"),
  deleteAccount: () => api.delete<void>("/api/me"),
};
