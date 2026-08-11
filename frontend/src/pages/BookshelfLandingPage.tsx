import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { booksApi } from "../api/books";
import { Bookshelf } from "../components/Bookshelf";
import { BookStackLoader } from "../components/BookStackLoader";
import type { Book } from "../types";

function ShelfHero() {
  return (
    <div className="shelf-hero">
      <div className="shelf-hero__text">
        <h1 style={{ fontSize: "2.1rem", lineHeight: 1.15, margin: "0 0 12px" }}>
          Every book you've loved, in one nook.
        </h1>
        <p style={{ color: "var(--color-text-muted)", margin: "0 0 20px" }}>
          Track what you're reading, what's next, and every series you follow — all in one calm little shelf.
        </p>
        <Link className="btn" to="/add">
          Add a book →
        </Link>
      </div>

      <img
        src="/illustrations/shelf-hero.png"
        alt="A line-art illustration of bookshelves with a dog reaching for a book"
        className="shelf-hero__image"
      />
    </div>
  );
}

export function BookshelfLandingPage() {
  const [books, setBooks] = useState<Book[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    booksApi
      .list({ sortBy: "addedAt", sortDescending: true })
      .then(setBooks)
      .catch((e) => setError(e instanceof Error ? e.message : "Could not load your books."));
  }, []);

  return (
    <div className="page">
      <ShelfHero />

      {error && <p className="error-text">{error}</p>}

      {books === null && !error && <BookStackLoader />}

      {books !== null && books.length === 0 && (
        <div className="card">
          <p>Your shelf is empty. Add your first book to start filling it up.</p>
          <Link className="btn" to="/add">
            Add a book
          </Link>
        </div>
      )}

      {books !== null && books.length > 0 && <Bookshelf books={books} />}
    </div>
  );
}
