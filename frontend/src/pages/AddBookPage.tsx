import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { booksApi } from "../api/books";
import { lookupApi } from "../api/lookup";
import { BarcodeScanner } from "../components/BarcodeScanner";
import { OcrCapture } from "../components/OcrCapture";
import { TitleSearch } from "../components/TitleSearch";
import type { Book, BookFormat } from "../types";

type Tab = "manual" | "scan" | "screenshot";

const EMPTY_DRAFT: Partial<Book> = {
  title: "",
  authors: [],
  format: "PHYSICAL",
  status: "NOT_STARTED",
};

export function AddBookPage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<Tab>("manual");
  const [draft, setDraft] = useState<Partial<Book>>(EMPTY_DRAFT);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [existingGenres, setExistingGenres] = useState<string[]>([]);

  useEffect(() => {
    booksApi.genres().then(setExistingGenres).catch(() => setExistingGenres([]));
  }, []);

  async function lookupIsbn(isbn: string) {
    setError(null);
    try {
      const suggestion = await lookupApi.byIsbn(isbn);
      setDraft((d) => ({
        ...d,
        title: suggestion.title ?? d.title,
        authors: suggestion.authors ?? d.authors,
        isbn: suggestion.isbn ?? isbn,
        coverImageUrl: suggestion.coverImageUrl,
        pageCount: suggestion.pageCount,
        publicationYear: suggestion.publicationYear,
        genre: suggestion.genre ?? d.genre,
      }));
      setTab("manual");
    } catch {
      setDraft((d) => ({ ...d, isbn }));
      setError("No metadata match for that ISBN — fill in the details manually below.");
      setTab("manual");
    }
  }

  async function save() {
    if (!draft.title?.trim()) {
      setError("Title is required.");
      return;
    }
    setSaving(true);
    setError(null);
    try {
      const created = await booksApi.create(draft);
      navigate(`/books/${created.id}`);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not save this book.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="page" style={{ maxWidth: 560 }}>
      <h1>Add a book</h1>

      <div style={{ display: "flex", gap: 10, marginBottom: 20 }}>
        <button className={`btn ${tab === "manual" ? "" : "secondary"}`} onClick={() => setTab("manual")}>
          Manual
        </button>
        <button className={`btn ${tab === "scan" ? "" : "secondary"}`} onClick={() => setTab("scan")}>
          Scan barcode
        </button>
        <button className={`btn ${tab === "screenshot" ? "" : "secondary"}`} onClick={() => setTab("screenshot")}>
          Screenshot (OCR)
        </button>
      </div>

      {error && <p className="error-text">{error}</p>}

      {tab === "scan" && <BarcodeScanner onScan={lookupIsbn} />}

      {tab === "screenshot" && (
        <OcrCapture
          onConfirm={({ title, authors }) => {
            setDraft((d) => ({ ...d, title, authors }));
            setTab("manual");
          }}
        />
      )}

      {tab === "manual" && (
        <div className="card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <label>
            <div>Title</div>
            <TitleSearch
              value={draft.title ?? ""}
              onChange={(title) => setDraft((d) => ({ ...d, title }))}
              onSelect={(suggestion) =>
                setDraft((d) => ({
                  ...d,
                  title: suggestion.title ?? d.title,
                  authors: suggestion.authors ?? d.authors,
                  isbn: suggestion.isbn ?? d.isbn,
                  coverImageUrl: suggestion.coverImageUrl,
                  pageCount: suggestion.pageCount,
                  publicationYear: suggestion.publicationYear,
                  genre: suggestion.genre ?? d.genre,
                }))
              }
            />
          </label>
          <label>
            <div>Author(s), comma-separated</div>
            <input
              value={draft.authors?.join(", ") ?? ""}
              onChange={(e) =>
                setDraft({ ...draft, authors: e.target.value.split(",").map((a) => a.trim()).filter(Boolean) })
              }
            />
          </label>
          <label>
            <div>ISBN</div>
            <input value={draft.isbn ?? ""} onChange={(e) => setDraft({ ...draft, isbn: e.target.value })} />
          </label>
          <label>
            <div>Page count</div>
            <input
              type="number"
              value={draft.pageCount ?? ""}
              onChange={(e) => setDraft({ ...draft, pageCount: Number(e.target.value) })}
            />
          </label>
          <label>
            <div>Format</div>
            <select value={draft.format} onChange={(e) => setDraft({ ...draft, format: e.target.value as BookFormat })}>
              <option value="PHYSICAL">Physical</option>
              <option value="EBOOK">Ebook</option>
              <option value="AUDIOBOOK">Audiobook</option>
            </select>
          </label>
          <label>
            <div>Genre</div>
            <input
              value={draft.genre ?? ""}
              onChange={(e) => setDraft({ ...draft, genre: e.target.value })}
              list="genre-options"
              placeholder="Start typing or pick a genre you've used before…"
            />
            {/* Browser-native autocomplete against genres already used in this library — no
                custom dropdown needed, and it stays in sync as existingGenres grows over time. */}
            <datalist id="genre-options">
              {existingGenres.map((genre) => (
                <option key={genre} value={genre} />
              ))}
            </datalist>
          </label>

          <button className="btn" onClick={save} disabled={saving}>
            {saving ? "Saving…" : "Add to library"}
          </button>
        </div>
      )}
    </div>
  );
}
