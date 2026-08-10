import { api } from "./client";
import type { Book, BookFilter } from "../types";

function toQueryString(filter: BookFilter): string {
  const params = new URLSearchParams();
  Object.entries(filter).forEach(([key, value]) => {
    if (value === undefined || value === null) return;
    if (Array.isArray(value)) {
      value.forEach((v) => params.append(key, String(v)));
    } else {
      params.set(key, String(value));
    }
  });
  const qs = params.toString();
  return qs ? `?${qs}` : "";
}

export const booksApi = {
  list: (filter: BookFilter = {}) => api.get<Book[]>(`/api/books${toQueryString(filter)}`),
  genres: () => api.get<string[]>("/api/books/genres"),
  get: (id: string) => api.get<Book>(`/api/books/${id}`),
  create: (book: Partial<Book>) => api.post<Book>("/api/books", book),
  update: (id: string, book: Partial<Book>) => api.put<Book>(`/api/books/${id}`, book),
  remove: (id: string) => api.delete<void>(`/api/books/${id}`),
};
