import { api } from "./client";
import type { HardcoverSeriesBook, SeriesFollowView } from "../types";

export const seriesApi = {
  followed: () => api.get<SeriesFollowView[]>("/api/series/followed"),
  get: (seriesId: string) => api.get<SeriesFollowView>(`/api/series/${seriesId}`),
  books: (seriesId: string) => api.get<HardcoverSeriesBook[]>(`/api/series/${seriesId}/books`),
  unfollow: (seriesId: string) => api.delete<void>(`/api/series/${seriesId}/follow`),
  reactivate: (seriesId: string) => api.post<void>(`/api/series/${seriesId}/reactivate`),
};
