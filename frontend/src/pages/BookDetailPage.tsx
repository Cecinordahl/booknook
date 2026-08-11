import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { booksApi } from "../api/books";
import { seriesApi } from "../api/series";
import { OutboundLinks } from "../components/OutboundLinks";
import { ProgressBar } from "../components/ProgressBar";
import type { Book, BookStatus } from "../types";

const STATUSES: BookStatus[] = ["NOT_STARTED", "READING", "FINISHED", "ON_HOLD"];

export function BookDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [book, setBook] = useState<Book | null>(null);
  const [seriesName, setSeriesName] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    booksApi
      .get(id)
      .then(setBook)
      .catch((e) => setError(e instanceof Error ? e.message : "Could not load this book."));
  }, [id]);

  useEffect(() => {
    if (!book?.seriesId) {
      setSeriesName(null);
      return;
    }
    seriesApi.get(book.seriesId).then((s) => setSeriesName(s.seriesName)).catch(() => setSeriesName(null));
  }, [book?.seriesId]);

  async function save(updates: Partial<Book>) {
    if (!book) return;
    setSaving(true);
    try {
      const updated = await booksApi.update(book.id, { ...book, ...updates });
      setBook(updated);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not save your changes.");
    } finally {
      setSaving(false);
    }
  }

  async function remove() {
    if (!book) return;
    if (!window.confirm(`Remove "${book.title}" from your library?`)) return;
    await booksApi.remove(book.id);
    navigate("/library");
  }

  if (error) return <p className="error-text page">{error}</p>;
  if (!book) return <div className="page-loading">Loading…</div>;

  return (
    <div className="page" style={{ maxWidth: 640 }}>
      <h1>{book.title}</h1>
      <p style={{ color: "var(--color-text-muted)" }}>{book.authors?.join(", ")}</p>
      {book.seriesId && seriesName && (
        <p>
          Part of <Link to={`/series/${book.seriesId}`}>{seriesName}</Link>
          {book.seriesPosition != null ? ` (#${book.seriesPosition})` : ""}
        </p>
      )}

      <div className="card" style={{ display: "flex", flexDirection: "column", gap: 14 }}>
        <label>
          <div>Status</div>
          <select value={book.status} onChange={(e) => save({ status: e.target.value as BookStatus })} disabled={saving}>
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {s.replace("_", " ")}
              </option>
            ))}
          </select>
        </label>

        {book.status === "READING" && (
          <label>
            <div>Current page (of {book.pageCount ?? "?"})</div>
            <input
              type="number"
              value={book.currentPage ?? ""}
              onChange={(e) => setBook({ ...book, currentPage: Number(e.target.value) })}
              onBlur={(e) => save({ currentPage: Number(e.target.value) })}
              disabled={saving}
            />
            <ProgressBar current={book.currentPage} total={book.pageCount} />
          </label>
        )}

        <label>
          <div>Personal rating (0–5)</div>
          <input
            type="number"
            step="0.5"
            min="0"
            max="5"
            value={book.personalRating ?? ""}
            onChange={(e) => setBook({ ...book, personalRating: Number(e.target.value) })}
            onBlur={(e) => save({ personalRating: Number(e.target.value) })}
            disabled={saving}
          />
        </label>
      </div>

      <h3>Find it elsewhere</h3>
      <OutboundLinks book={book} />

      <div style={{ marginTop: 32 }}>
        <button className="btn danger" onClick={remove}>
          Remove from library
        </button>
      </div>
    </div>
  );
}
