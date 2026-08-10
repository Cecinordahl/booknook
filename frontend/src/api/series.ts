import { api } from "./client";
import type { HardcoverSeriesMatch, SeriesFollowView } from "../types";

export const seriesApi = {
  search: (query: string) => api.get<HardcoverSeriesMatch[]>(`/api/series/search?q=${encodeURIComponent(query)}`),
  follow: (hardcoverSeriesId: string, seriesName: string) =>
    api.post<void>("/api/series/follow", { hardcoverSeriesId, seriesName }),
  unfollow: (seriesId: string) => api.delete<void>(`/api/series/${seriesId}/follow`),
  followed: () => api.get<SeriesFollowView[]>("/api/series/followed"),
};
