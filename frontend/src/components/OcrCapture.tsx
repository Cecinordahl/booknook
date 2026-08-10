import { useState } from "react";
import { createWorker } from "tesseract.js";

interface OcrCaptureProps {
  onConfirm: (data: { title: string; authors: string[] }) => void;
}

/**
 * Client-side OCR for a book-cover screenshot. Nothing here is ever saved automatically — the
 * extracted text only pre-fills an editable form that the user must explicitly confirm, per the
 * privacy/data-minimization requirement (transient OCR, no server upload, best-effort parse).
 */
export function OcrCapture({ onConfirm }: OcrCaptureProps) {
  const [running, setRunning] = useState(false);
  const [rawText, setRawText] = useState("");
  const [title, setTitle] = useState("");
  const [author, setAuthor] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function handleFile(file: File) {
    setRunning(true);
    setError(null);
    try {
      const worker = await createWorker("eng");
      const {
        data: { text },
      } = await worker.recognize(file);
      await worker.terminate();

      setRawText(text);
      const lines = text.split("\n").map((l) => l.trim()).filter(Boolean);
      // Best-effort guess only — this is why the fields below are editable, not auto-saved.
      setTitle(lines[0] ?? "");
      setAuthor(lines[1] ?? "");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not read text from that image.");
    } finally {
      setRunning(false);
    }
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <input
        type="file"
        accept="image/*"
        capture="environment"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFile(file);
        }}
      />

      {running && <p>Reading text from the image…</p>}
      {error && <p className="error-text">{error}</p>}

      {rawText && !running && (
        <div className="card" style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <p style={{ margin: 0, color: "var(--color-text-muted)", fontSize: "0.85rem" }}>
            Best guess from the image — check and correct before saving.
          </p>
          <label>
            <div>Title</div>
            <input value={title} onChange={(e) => setTitle(e.target.value)} />
          </label>
          <label>
            <div>Author</div>
            <input value={author} onChange={(e) => setAuthor(e.target.value)} />
          </label>
          <details>
            <summary>Raw extracted text</summary>
            <pre style={{ whiteSpace: "pre-wrap", fontSize: "0.8rem" }}>{rawText}</pre>
          </details>
          <button
            className="btn"
            type="button"
            disabled={!title.trim()}
            onClick={() => onConfirm({ title: title.trim(), authors: author.trim() ? [author.trim()] : [] })}
          >
            Use this
          </button>
        </div>
      )}
    </div>
  );
}
