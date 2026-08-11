import { useEffect, useState } from "react";
import { booksApi } from "../api/books";
import { seriesApi } from "../api/series";
import { FilterBar } from "../components/FilterBar";
import { BookCard } from "../components/BookCard";
import { FollowedSeriesPanel } from "../components/FollowedSeriesPanel";
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

  function reloadFollowed() {
    seriesApi.followed().then(setFollowed).catch(() => setFollowed([]));
  }

  useEffect(() => {
    reloadFollowed();
  }, []);

  async function handleDelete(book: Book) {
    if (!window.confirm(`Remove "${book.title}" from your library? This can't be undone.`)) {
      return;
    }
    try {
      await booksApi.remove(book.id);
      setBooks((current) => current.filter((b) => b.id !== book.id));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not remove this book.");
    }
  }

  return (
    <div className="page">
      <h1>Library</h1>

      <div style={{ marginBottom: 20 }}>
        <FollowedSeriesPanel followed={followed} onChanged={reloadFollowed} />
      </div>

      <div style={{ marginBottom: 20 }}>
        <FilterBar filter={filter} onChange={setFilter} />
      </div>

      {error && <p className="error-text">{error}</p>}
      {loading && <p>Loading…</p>}

      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {books.map((book) => (
          <BookCard key={book.id} book={book} onDelete={handleDelete} />
        ))}
        {!loading && books.length === 0 && <p>No books match these filters.</p>}
      </div>
    </div>
  );
}
