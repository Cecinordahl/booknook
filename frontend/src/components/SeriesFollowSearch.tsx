import { useState } from "react";
import { seriesApi } from "../api/series";
import type { HardcoverSeriesMatch } from "../types";

export function SeriesFollowSearch({ onFollowed }: { onFollowed: () => void }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<HardcoverSeriesMatch[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [followingId, setFollowingId] = useState<string | null>(null);

  async function search() {
    setError(null);
    try {
      setResults(await seriesApi.search(query));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Series search failed.");
    }
  }

  async function follow(match: HardcoverSeriesMatch) {
    setFollowingId(match.hardcoverSeriesId);
    try {
      await seriesApi.follow(match.hardcoverSeriesId, match.name);
      setResults((r) => r.filter((m) => m.hardcoverSeriesId !== match.hardcoverSeriesId));
      onFollowed();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not follow that series.");
    } finally {
      setFollowingId(null);
    }
  }

  return (
    <div className="card">
      <h3 style={{ marginTop: 0 }}>Follow a series</h3>
      <div style={{ display: "flex", gap: 8 }}>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && search()}
          placeholder="Series name"
          style={{ flex: 1 }}
        />
        <button className="btn secondary" type="button" onClick={search}>
          Search
        </button>
      </div>
      {error && <p className="error-text">{error}</p>}
      {results.length > 0 && (
        <ul style={{ listStyle: "none", padding: 0, marginBottom: 0 }}>
          {results.map((m) => (
            <li key={m.hardcoverSeriesId} style={{ display: "flex", justifyContent: "space-between", padding: "6px 0" }}>
              <span>{m.name}</span>
              <button className="btn" type="button" disabled={followingId === m.hardcoverSeriesId} onClick={() => follow(m)}>
                Follow
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
