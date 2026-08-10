import { Link } from "react-router-dom";
import type { Book } from "../types";
import "./Bookshelf.css";

const SHELF_CAPACITY = 9;
const SPINE_COLORS = ["#b5622f", "#5c4735", "#3f6e4a", "#8a4b6b", "#c98a2c", "#3a5a78", "#a5333a"];

function hashString(value: string): number {
  let hash = 0;
  for (let i = 0; i < value.length; i++) {
    hash = (hash << 5) - hash + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function spineHeight(pageCount?: number): number {
  if (!pageCount) return 90;
  return Math.min(140, Math.max(70, 60 + pageCount / 8));
}

function chunk<T>(items: T[], size: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < items.length; i += size) {
    chunks.push(items.slice(i, i + size));
  }
  return chunks;
}

/**
 * The app's signature visual: a bookshelf that fills up as books are added. Books group into
 * fixed-capacity rows ("shelves"); a partially-filled trailing shelf shows empty slots so the
 * "fills up as you add books" feel reads even with a small library.
 */
export function Bookshelf({ books }: { books: Book[] }) {
  // Always leave room to grow: if the last shelf is exactly full, add an empty trailing one.
  const rows = chunk(books, SHELF_CAPACITY);
  if (rows.length === 0) {
    rows.push([]);
  } else if (rows[rows.length - 1].length === SHELF_CAPACITY) {
    rows.push([]);
  }

  return (
    <div className="bookshelf">
      {rows.map((row, rowIndex) => (
        <div className="bookshelf__row" key={rowIndex}>
          <div className="bookshelf__spines">
            {row.map((book) => {
              const color = SPINE_COLORS[hashString(book.id) % SPINE_COLORS.length];
              const finished = book.status === "FINISHED";
              return (
                <Link
                  key={book.id}
                  to={`/books/${book.id}`}
                  className="bookshelf__spine"
                  style={{
                    height: spineHeight(book.pageCount),
                    background: color,
                    opacity: finished ? 1 : 0.85,
                  }}
                  title={book.title}
                >
                  <span>{book.title}</span>
                </Link>
              );
            })}
            {Array.from({ length: Math.max(0, SHELF_CAPACITY - row.length) }).map((_, i) => (
              <div className="bookshelf__spine bookshelf__spine--empty" key={`empty-${i}`} />
            ))}
          </div>
          <div className="bookshelf__ledge" />
        </div>
      ))}
    </div>
  );
}
