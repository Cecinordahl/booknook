interface ProgressBarProps {
  current?: number;
  total?: number;
}

export function ProgressBar({ current, total }: ProgressBarProps) {
  if (!total || total <= 0) {
    return null;
  }
  const pct = Math.min(100, Math.round(((current ?? 0) / total) * 100));

  return (
    <div>
      <div className="progress-bar" role="progressbar" aria-valuenow={pct} aria-valuemin={0} aria-valuemax={100}>
        <div className="progress-bar__fill" style={{ width: `${pct}%` }} />
      </div>
      <div style={{ fontSize: "0.8rem", color: "var(--color-text-muted)", marginTop: 4 }}>
        {current ?? 0} / {total} pages ({pct}%)
      </div>
    </div>
  );
}
