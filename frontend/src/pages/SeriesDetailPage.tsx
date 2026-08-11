import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { seriesApi } from "../api/series";
import { colorForId } from "../utils/palette";
import type { HardcoverSeriesBook, SeriesFollowView } from "../types";

export function SeriesDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [series, setSeries] = useState<SeriesFollowView | null>(null);
  const [books, setBooks] = useState<HardcoverSeriesBook[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  function load() {
    if (!id) return;
    seriesApi
      .get(id)
      .then(setSeries)
      .catch((e) => setError(e instanceof Error ? e.message : "Could not load this series."));
    seriesApi
      .books(id)
      .then(setBooks)
      .catch(() => setBooks([]));
  }

  useEffect(load, [id]);

  async function toggleFollow() {
    if (!id || !series) return;
    setBusy(true);
    try {
      if (series.discarded) {
        await seriesApi.reactivate(id);
      } else {
        await seriesApi.unfollow(id);
      }
      load();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not update follow status.");
    } finally {
      setBusy(false);
    }
  }

  if (error) return <p className="error-text page">{error}</p>;
  if (!series) return <div className="page-loading">Loading…</div>;

  return (
    <div className="page" style={{ maxWidth: 720 }}>
      <h1>{series.seriesName}</h1>

      <div className="card" style={{ marginBottom: 20 }}>
        {series.nextReleaseDate ? (
          <p style={{ margin: 0 }}>
            Next: <strong>{series.nextReleaseTitle}</strong> ({series.nextReleaseDate})
          </p>
        ) : (
          <p style={{ margin: 0, color: "var(--color-text-muted)" }}>
            {series.isCompleted === true
              ? "This series is marked complete — nothing more planned."
              : series.isCompleted === false
                ? "Ongoing series, nothing dated yet."
                : "No release-status data available for this series."}
          </p>
        )}
        <button className="btn secondary" style={{ marginTop: 12 }} onClick={toggleFollow} disabled={busy}>
          {series.discarded ? "Re-follow" : "Unfollow"}
        </button>
      </div>

      <h3>Books in this series</h3>
      {books === null && <p>Loading books…</p>}
      {books !== null && books.length === 0 && <p>No book data available from Hardcover for this series.</p>}
      {books !== null && books.length > 0 && (
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {books.map((b, i) => (
            <div key={i} className="card" style={{ display: "flex", gap: 14, alignItems: "center" }}>
              <div
                style={{
                  width: 48,
                  height: 72,
                  flexShrink: 0,
                  borderRadius: 4,
                  background: b.coverImageUrl ? `center/cover url(${b.coverImageUrl})` : colorForId(b.title + i),
                }}
              />
              <div>
                <div>
                  {b.position != null && <span className="badge" style={{ marginRight: 8 }}>#{b.position}</span>}
                  <strong>{b.title}</strong>
                </div>
                {b.releaseDate && (
                  <div style={{ fontSize: "0.85rem", color: "var(--color-text-muted)" }}>{b.releaseDate}</div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
