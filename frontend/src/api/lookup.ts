import { api } from "./client";
import type { BookMetadataSuggestion } from "../types";

export const lookupApi = {
  byIsbn: (isbn: string) => api.get<BookMetadataSuggestion>(`/api/lookup/isbn/${encodeURIComponent(isbn)}`),
};
