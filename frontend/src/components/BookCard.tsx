import { Link } from "react-router-dom";
import type { Book } from "../types";
import { ProgressBar } from "./ProgressBar";
import { colorForId } from "../utils/palette";

const STATUS_LABELS: Record<Book["status"], string> = {
  NOT_STARTED: "Not started",
  READING: "Reading",
  FINISHED: "Finished",
  ON_HOLD: "On hold",
};

const FORMAT_LABELS: Record<Book["format"], string> = {
  PHYSICAL: "Physical",
  EBOOK: "Ebook",
  AUDIOBOOK: "Audiobook",
};

interface BookCardProps {
  book: Book;
  /** When provided, shows a delete (trash can) button — the caller owns the confirm step. */
  onDelete?: (book: Book) => void;
}

export function BookCard({ book, onDelete }: BookCardProps) {
  return (
    <Link
      to={`/books/${book.id}`}
      className="card"
      style={{ display: "flex", gap: 14, textDecoration: "none", color: "inherit" }}
    >
      <div
        style={{
          width: 64,
          height: 96,
          flexShrink: 0,
          borderRadius: 4,
          background: book.coverImageUrl ? `center/cover url(${book.coverImageUrl})` : colorForId(book.id),
        }}
      />
      <div style={{ display: "flex", flexDirection: "column", gap: 6, minWidth: 0, flex: 1 }}>
        <strong style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{book.title}</strong>
        <span style={{ color: "var(--color-text-muted)", fontSize: "0.9rem" }}>{book.authors?.join(", ")}</span>
        <div style={{ display: "flex", gap: 6 }}>
          <span className="badge">{STATUS_LABELS[book.status]}</span>
          <span className="badge badge--ink">{FORMAT_LABELS[book.format]}</span>
        </div>
        {book.status === "READING" && <ProgressBar current={book.currentPage} total={book.pageCount} />}
      </div>
      {onDelete && (
        <button
          type="button"
          aria-label={`Remove ${book.title} from your library`}
          title="Remove from library"
          onClick={(e) => {
            // This button lives inside the card's <Link> — stop the click from also
            // triggering navigation to the book detail page.
            e.preventDefault();
            e.stopPropagation();
            onDelete(book);
          }}
          style={{
            alignSelf: "flex-start",
            background: "transparent",
            border: "none",
            cursor: "pointer",
            color: "var(--color-text-muted)",
            padding: 6,
          }}
          className="book-card-delete"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            <line x1="10" y1="11" x2="10" y2="17" />
            <line x1="14" y1="11" x2="14" y2="17" />
          </svg>
        </button>
      )}
    </Link>
  );
}
