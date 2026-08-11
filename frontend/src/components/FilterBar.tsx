import type { BookFilter, BookFormat, BookStatus } from "../types";

interface FilterBarProps {
  filter: BookFilter;
  onChange: (filter: BookFilter) => void;
}

const STATUSES: BookStatus[] = ["NOT_STARTED", "READING", "FINISHED", "ON_HOLD"];
const FORMATS: BookFormat[] = ["PHYSICAL", "EBOOK", "AUDIOBOOK"];

export function FilterBar({ filter, onChange }: FilterBarProps) {
  function set<K extends keyof BookFilter>(key: K, value: BookFilter[K]) {
    onChange({ ...filter, [key]: value || undefined });
  }

  return (
    <div className="card filter-bar">
      <label>
        <div>Genre</div>
        <input value={filter.genre ?? ""} onChange={(e) => set("genre", e.target.value)} placeholder="Any genre" />
      </label>

      <label>
        <div>Source</div>
        <input value={filter.source ?? ""} onChange={(e) => set("source", e.target.value)} placeholder="Any source" />
      </label>

      <label>
        <div>Mood / theme tags</div>
        <input
          value={filter.moodTags?.join(", ") ?? ""}
          onChange={(e) =>
            set(
              "moodTags",
              e.target.value
                .split(",")
                .map((t) => t.trim())
                .filter(Boolean),
            )
          }
          placeholder="cozy, slow-burn, ..."
        />
      </label>

      <label>
        <div>Status</div>
        <select value={filter.status ?? ""} onChange={(e) => set("status", e.target.value as BookStatus)}>
          <option value="">Any</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {s.replace("_", " ")}
            </option>
          ))}
        </select>
      </label>

      <label>
        <div>Format</div>
        <select value={filter.format ?? ""} onChange={(e) => set("format", e.target.value as BookFormat)}>
          <option value="">Any</option>
          {FORMATS.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </select>
      </label>

      <label>
        <div>Pages</div>
        <div style={{ display: "flex", gap: 6 }}>
          <input
            type="number"
            style={{ width: 70 }}
            value={filter.minPageCount ?? ""}
            onChange={(e) => set("minPageCount", e.target.value ? Number(e.target.value) : undefined)}
            placeholder="min"
          />
          <input
            type="number"
            style={{ width: 70 }}
            value={filter.maxPageCount ?? ""}
            onChange={(e) => set("maxPageCount", e.target.value ? Number(e.target.value) : undefined)}
            placeholder="max"
          />
        </div>
      </label>

      <label>
        <div>Publication year</div>
        <div style={{ display: "flex", gap: 6 }}>
          <input
            type="number"
            style={{ width: 80 }}
            value={filter.minPublicationYear ?? ""}
            onChange={(e) => set("minPublicationYear", e.target.value ? Number(e.target.value) : undefined)}
            placeholder="from"
          />
          <input
            type="number"
            style={{ width: 80 }}
            value={filter.maxPublicationYear ?? ""}
            onChange={(e) => set("maxPublicationYear", e.target.value ? Number(e.target.value) : undefined)}
            placeholder="to"
          />
        </div>
      </label>

      <label>
        <div>Min rating</div>
        <input
          type="number"
          step="0.5"
          min="0"
          max="5"
          style={{ width: 70 }}
          value={filter.minRating ?? ""}
          onChange={(e) => set("minRating", e.target.value ? Number(e.target.value) : undefined)}
        />
      </label>

      <label>
        <div>Sort by</div>
        <select value={filter.sortBy ?? "addedAt"} onChange={(e) => set("sortBy", e.target.value)}>
          <option value="addedAt">Date added</option>
          <option value="title">Title</option>
          <option value="publicationYear">Publication year</option>
          <option value="personalRating">Rating</option>
          <option value="pageCount">Page count</option>
        </select>
      </label>

      <div className="filter-bar__actions">
        <button className="btn secondary" type="button" onClick={() => onChange({ sortBy: "addedAt", sortDescending: true })}>
          Reset filters
        </button>
      </div>
    </div>
  );
}
