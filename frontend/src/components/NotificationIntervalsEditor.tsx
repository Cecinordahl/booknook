import { useEffect, useState } from "react";
import { meApi } from "../api/me";
import { useAuth } from "../auth/AuthProvider";
import type { UserAccount } from "../types";

const PRESETS = [
  { days: 0, label: "Release day" },
  { days: 7, label: "1 week before" },
  { days: 14, label: "2 weeks before" },
  { days: 30, label: "1 month before" },
  { days: 60, label: "2 months before" },
  { days: 90, label: "3 months before" },
];

const MAX_INTERVALS = 3;
const DEFAULT_INTERVALS = [60, 0];

/** Changes save immediately (no separate Save step) — each row edit is one deliberate action already. */
export function NotificationIntervalsEditor({ account }: { account: UserAccount | null }) {
  const { refreshAccount } = useAuth();
  const [intervals, setIntervals] = useState<number[]>(account?.notificationIntervalDays ?? DEFAULT_INTERVALS);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setIntervals(account?.notificationIntervalDays ?? DEFAULT_INTERVALS);
  }, [account?.notificationIntervalDays]);

  async function persist(next: number[]) {
    setIntervals(next);
    setSaving(true);
    setError(null);
    try {
      await meApi.updateNotificationIntervals(next);
      await refreshAccount();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not save your notification settings.");
    } finally {
      setSaving(false);
    }
  }

  function updateAt(index: number, days: number) {
    persist(intervals.map((d, i) => (i === index ? days : d)));
  }

  function removeAt(index: number) {
    persist(intervals.filter((_, i) => i !== index));
  }

  function addInterval() {
    const unused = PRESETS.find((p) => !intervals.includes(p.days));
    persist([...intervals, unused ? unused.days : 0]);
  }

  return (
    <div>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {intervals.map((days, i) => (
          <div key={i} style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <select value={days} onChange={(e) => updateAt(i, Number(e.target.value))}>
              {PRESETS.map((p) => (
                <option key={p.days} value={p.days}>
                  {p.label}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="btn secondary"
              onClick={() => removeAt(i)}
              aria-label="Remove this reminder"
              style={{ padding: "6px 10px" }}
            >
              ×
            </button>
          </div>
        ))}
        {intervals.length === 0 && (
          <p style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", margin: 0 }}>
            No release reminders set — you won't get notified about upcoming releases.
          </p>
        )}
      </div>

      <div style={{ display: "flex", gap: 10, marginTop: 12, alignItems: "center" }}>
        {intervals.length < MAX_INTERVALS && (
          <button type="button" className="btn secondary" onClick={addInterval}>
            Add another reminder
          </button>
        )}
        {saving && <span style={{ color: "var(--color-text-muted)", fontSize: "0.85rem" }}>Saving…</span>}
      </div>
      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
