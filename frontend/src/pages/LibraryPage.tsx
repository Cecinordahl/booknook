import { useEffect, useState } from "react";
import { booksApi } from "../api/books";
import { seriesApi } from "../api/series";
import { FilterBar } from "../components/FilterBar";
import { BookCard } from "../components/BookCard";
import { SeriesFollowSearch } from "../components/SeriesFollowSearch";
import type { Book, BookFilter, SeriesFollowView } from "../types";

export function LibraryPage() {
  const [filter, setFilter] = useState<BookFilter>({ sortBy: "addedAt", sortDescending: true });
  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [followed, setFollowed] = useState<SeriesFollowView[]>([]);

  useEffect(() => {
    setLoading(true);
    booksApi
      .list(filter)
      .then(setBooks)
      .catch((e) => setError(e instanceof Error ? e.message : "Could not load your library."))
      .finally(() => setLoading(false));
  }, [filter]);

  useEffect(() => {
    seriesApi.followed().then(setFollowed).catch(() => setFollowed([]));
  }, []);

  function reloadFollowed() {
    seriesApi.followed().then(setFollowed).catch(() => setFollowed([]));
  }

  const upcoming = followed.filter((f) => f.nextReleaseDate).sort((a, b) => (a.nextReleaseDate! < b.nextReleaseDate! ? -1 : 1));

  return (
    <div className="page">
      <h1>Library</h1>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginBottom: 20 }}>
        {upcoming.length > 0 && (
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Next in series you follow</h3>
            <ul style={{ margin: 0, paddingLeft: 20 }}>
              {upcoming.map((s) => (
                <li key={s.seriesId}>
                  <strong>{s.seriesName}</strong> — {s.nextReleaseTitle} ({s.nextReleaseDate})
                </li>
              ))}
            </ul>
          </div>
        )}
        <SeriesFollowSearch onFollowed={reloadFollowed} />
      </div>

      <div style={{ marginBottom: 20 }}>
        <FilterBar filter={filter} onChange={setFilter} />
      </div>

      {error && <p className="error-text">{error}</p>}
      {loading && <p>Loading…</p>}

      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
        {!loading && books.length === 0 && <p>No books match these filters.</p>}
      </div>
    </div>
  );
}
