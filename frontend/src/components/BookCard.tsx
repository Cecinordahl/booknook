import { Link } from "react-router-dom";
import type { Book } from "../types";
import { ProgressBar } from "./ProgressBar";

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

export function BookCard({ book }: { book: Book }) {
  return (
    <Link to={`/books/${book.id}`} className="card" style={{ display: "flex", gap: 14, textDecoration: "none", color: "inherit" }}>
      <div
        style={{
          width: 64,
          height: 96,
          flexShrink: 0,
          borderRadius: 4,
          background: book.coverImageUrl ? `center/cover url(${book.coverImageUrl})` : "var(--color-accent-light)",
        }}
      />
      <div style={{ display: "flex", flexDirection: "column", gap: 6, minWidth: 0 }}>
        <strong style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{book.title}</strong>
        <span style={{ color: "var(--color-text-muted)", fontSize: "0.9rem" }}>{book.authors?.join(", ")}</span>
        <div style={{ display: "flex", gap: 6 }}>
          <span className="badge">{STATUS_LABELS[book.status]}</span>
          <span className="badge">{FORMAT_LABELS[book.format]}</span>
        </div>
        {book.status === "READING" && <ProgressBar current={book.currentPage} total={book.pageCount} />}
      </div>
    </Link>
  );
}
