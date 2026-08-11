import { Link } from "react-router-dom";
import { seriesApi } from "../api/series";
import type { SeriesFollowView } from "../types";

/**
 * Splits followed series into: has a dated upcoming release (shown up front), confirmed
 * completed (Hardcover's is_completed is true), confirmed ongoing with nothing dated yet
 * (is_completed is false), unknown status (is_completed is null), and discarded (explicitly
 * unfollowed — kept visible so a book from that series later doesn't silently re-follow it, and
 * so it's easy to change your mind).
 */
export function FollowedSeriesPanel({ followed, onChanged }: { followed: SeriesFollowView[]; onChanged: () => void }) {
  const active = followed.filter((f) => !f.discarded);
  const discardedList = followed.filter((f) => f.discarded);

  const upcoming = [...active.filter((f) => f.nextReleaseDate)].sort((a, b) =>
    a.nextReleaseDate! < b.nextReleaseDate! ? -1 : 1,
  );
  const withoutDate = active.filter((f) => !f.nextReleaseDate);
  const completed = withoutDate.filter((f) => f.isCompleted === true);
  const ongoingTbd = withoutDate.filter((f) => f.isCompleted === false);
  const unknownStatus = withoutDate.filter((f) => f.isCompleted == null);

  async function unfollow(seriesId: string) {
    await seriesApi.unfollow(seriesId);
    onChanged();
  }

  async function reactivate(seriesId: string) {
    await seriesApi.reactivate(seriesId);
    onChanged();
  }

  function SeriesRow({ series, extra }: { series: SeriesFollowView; extra?: string }) {
    return (
      <li style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "4px 0" }}>
        <span>
          <Link to={`/series/${series.seriesId}`}>{series.seriesName}</Link>
          {extra}
        </span>
        <button className="btn secondary" type="button" onClick={() => unfollow(series.seriesId)}>
          Unfollow
        </button>
      </li>
    );
  }

  if (followed.length === 0) {
    return null;
  }

  return (
    <div className="card card--dark">
      <h3 style={{ marginTop: 0 }}>Next in series you follow</h3>
      {upcoming.length > 0 ? (
        <ul style={{ margin: 0, paddingLeft: 0, listStyle: "none" }}>
          {upcoming.map((s) => (
            <SeriesRow key={s.seriesId} series={s} extra={` — ${s.nextReleaseTitle} (${s.nextReleaseDate})`} />
          ))}
        </ul>
      ) : (
        <p style={{ color: "var(--color-gold-soft)", margin: 0 }}>Nothing dated yet.</p>
      )}

      {completed.length > 0 && (
        <details style={{ marginTop: 12 }}>
          <summary>Completed series ({completed.length})</summary>
          <ul style={{ margin: "8px 0 0", paddingLeft: 0, listStyle: "none" }}>
            {completed.map((s) => (
              <SeriesRow key={s.seriesId} series={s} />
            ))}
          </ul>
        </details>
      )}

      {ongoingTbd.length > 0 && (
        <details style={{ marginTop: 12 }}>
          <summary>Nothing planned yet ({ongoingTbd.length})</summary>
          <ul style={{ margin: "8px 0 0", paddingLeft: 0, listStyle: "none" }}>
            {ongoingTbd.map((s) => (
              <SeriesRow key={s.seriesId} series={s} />
            ))}
          </ul>
        </details>
      )}

      {unknownStatus.length > 0 && (
        <details style={{ marginTop: 12 }}>
          <summary>Status unknown ({unknownStatus.length})</summary>
          <ul style={{ margin: "8px 0 0", paddingLeft: 0, listStyle: "none" }}>
            {unknownStatus.map((s) => (
              <SeriesRow key={s.seriesId} series={s} />
            ))}
          </ul>
        </details>
      )}

      {discardedList.length > 0 && (
        <details style={{ marginTop: 12 }}>
          <summary>Discarded ({discardedList.length})</summary>
          <ul style={{ margin: "8px 0 0", paddingLeft: 20, listStyle: "none" }}>
            {discardedList.map((s) => (
              <li key={s.seriesId} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "4px 0" }}>
                <Link to={`/series/${s.seriesId}`}>{s.seriesName}</Link>
                <button className="btn secondary" type="button" onClick={() => reactivate(s.seriesId)}>
                  Re-follow
                </button>
              </li>
            ))}
          </ul>
        </details>
      )}
    </div>
  );
}
