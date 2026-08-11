import { useState } from "react";
import { booksApi } from "../api/books";
import type { GoodreadsImportResult } from "../types";

export function GoodreadsImport({ onImported }: { onImported: () => void }) {
  const [file, setFile] = useState<File | null>(null);
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState<GoodreadsImportResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleImport() {
    if (!file) return;
    setImporting(true);
    setError(null);
    setResult(null);
    try {
      const res = await booksApi.importGoodreads(file);
      setResult(res);
      setFile(null);
      onImported();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not import that file.");
    } finally {
      setImporting(false);
    }
  }

  return (
    <div className="card" style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <p style={{ margin: 0, color: "var(--color-text-muted)", fontSize: "0.9rem" }}>
        On Goodreads: My Books → Import/Export (under Tools) → Export Library. Upload the CSV file
        it gives you below. Books already in your library (matched by ISBN or title + author) are
        skipped automatically, so it's safe to re-run this later.
      </p>

      <label>
        <div>Goodreads export (.csv)</div>
        <input
          type="file"
          accept=".csv,text/csv"
          onChange={(e) => {
            setFile(e.target.files?.[0] ?? null);
            setResult(null);
            setError(null);
          }}
        />
      </label>

      <button className="btn" onClick={handleImport} disabled={!file || importing}>
        {importing ? "Importing…" : "Import books"}
      </button>

      {error && <p className="error-text">{error}</p>}

      {result && (
        <div>
          <p style={{ margin: 0 }}>
            Imported <strong>{result.imported}</strong> book{result.imported === 1 ? "" : "s"}
            {result.skipped > 0 ? ` — skipped ${result.skipped} already in your library.` : "."}
          </p>
          {result.errors.length > 0 && (
            <details style={{ marginTop: 8 }}>
              <summary>
                {result.errors.length} row{result.errors.length === 1 ? "" : "s"} couldn't be read
              </summary>
              <ul style={{ margin: "8px 0 0", paddingLeft: 20 }}>
                {result.errors.map((e, i) => (
                  <li key={i} style={{ fontSize: "0.85rem", color: "var(--color-text-muted)" }}>
                    {e}
                  </li>
                ))}
              </ul>
            </details>
          )}
        </div>
      )}
    </div>
  );
}
