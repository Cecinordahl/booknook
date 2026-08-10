import { useEffect, useRef, useState } from "react";
import { lookupApi } from "../api/lookup";
import type { BookMetadataSuggestion } from "../types";

const MIN_QUERY_LENGTH = 3;
const DEBOUNCE_MS = 350;

interface TitleSearchProps {
  value: string;
  onChange: (title: string) => void;
  onSelect: (suggestion: BookMetadataSuggestion) => void;
}

/**
 * Title input with search-as-you-type. Typing always updates the title directly — the dropdown
 * of Google Books matches is a shortcut for auto-filling the rest of the form, not the only way
 * to set a title.
 */
export function TitleSearch({ value, onChange, onSelect }: TitleSearchProps) {
  const [results, setResults] = useState<BookMetadataSuggestion[]>([]);
  const [open, setOpen] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const requestId = useRef(0);

  useEffect(() => {
    if (value.trim().length < MIN_QUERY_LENGTH) {
      setResults([]);
      setSearchError(null);
      return;
    }

    const currentRequest = ++requestId.current;
    const timer = setTimeout(async () => {
      try {
        const matches = await lookupApi.search(value.trim());
        if (currentRequest === requestId.current) {
          setResults(matches);
          setSearchError(null);
          setOpen(matches.length > 0);
        }
      } catch (e) {
        if (currentRequest === requestId.current) {
          setResults([]);
          setSearchError(e instanceof Error ? e.message : "Title search is unavailable right now.");
        }
      }
    }, DEBOUNCE_MS);

    return () => clearTimeout(timer);
  }, [value]);

  return (
    <div style={{ position: "relative" }}>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onFocus={() => results.length > 0 && setOpen(true)}
        onBlur={() => setOpen(false)}
        onKeyDown={(e) => e.key === "Escape" && setOpen(false)}
        style={{ width: "100%" }}
        placeholder="Start typing a title…"
      />
      {searchError && (
        <p style={{ color: "var(--color-text-muted)", fontSize: "0.8rem", margin: "4px 0 0" }}>
          {searchError} You can still fill in the fields below manually.
        </p>
      )}
      {open && results.length > 0 && (
        <ul
          style={{
            position: "absolute",
            top: "100%",
            left: 0,
            right: 0,
            zIndex: 10,
            margin: 0,
            marginTop: 4,
            padding: 4,
            listStyle: "none",
            background: "var(--color-surface)",
            border: "1px solid var(--color-border)",
            borderRadius: 8,
            boxShadow: "var(--shadow)",
            maxHeight: 280,
            overflowY: "auto",
          }}
        >
          {results.map((suggestion, i) => (
            <li key={i}>
              {/* onMouseDown + preventDefault so the click registers before the input's onBlur closes the dropdown */}
              <button
                type="button"
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => {
                  onSelect(suggestion);
                  setOpen(false);
                }}
                style={{
                  display: "flex",
                  gap: 10,
                  alignItems: "center",
                  width: "100%",
                  padding: 6,
                  border: "none",
                  background: "transparent",
                  textAlign: "left",
                  cursor: "pointer",
                  borderRadius: 6,
                }}
              >
                <div
                  style={{
                    width: 32,
                    height: 44,
                    flexShrink: 0,
                    borderRadius: 3,
                    background: suggestion.coverImageUrl
                      ? `center/cover url(${suggestion.coverImageUrl})`
                      : "var(--color-accent-light)",
                  }}
                />
                <div style={{ minWidth: 0 }}>
                  <div style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                    {suggestion.title}
                  </div>
                  <div style={{ fontSize: "0.8rem", color: "var(--color-text-muted)" }}>
                    {suggestion.authors?.join(", ")}
                    {suggestion.publicationYear ? ` · ${suggestion.publicationYear}` : ""}
                  </div>
                </div>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
