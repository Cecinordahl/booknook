import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { booksApi } from "../api/books";
import { Bookshelf } from "../components/Bookshelf";
import { BookStackLoader } from "../components/BookStackLoader";
import type { Book } from "../types";

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
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline", marginBottom: 20 }}>
        <h1>Your shelf</h1>
        <Link className="btn" to="/add">
          Add a book
        </Link>
      </div>

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
