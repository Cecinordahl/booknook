interface MascotProps {
  size?: number;
  className?: string;
}

/**
 * Line-art dog mascot — single ink-blue stroke, no fill, used sparingly (hero, empty states).
 * Recreated as an inline SVG (rather than embedding a raster) so it stays crisp at any size and
 * always matches the current ink color token.
 */
export function Mascot({ size = 64, className }: MascotProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 64 64"
      fill="none"
      stroke="var(--color-text)"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      {/* ears */}
      <path d="M20 22c-4-6-2-13 3-14 4-1 6 4 5 9" />
      <path d="M44 22c4-6 2-13-3-14-4-1-6 4-5 9" />
      {/* head */}
      <circle cx="32" cy="26" r="13" />
      {/* eyes */}
      <circle cx="27.5" cy="25" r="1.4" fill="var(--color-text)" />
      <circle cx="36.5" cy="25" r="1.4" fill="var(--color-text)" />
      {/* snout + nose */}
      <path d="M28 31c1.5 2 5 2 6.5 0" />
      {/* body */}
      <path d="M20 40c0-6 5-10 12-10s12 4 12 10v6c0 4-3 7-7 7H27c-4 0-7-3-7-7z" />
      {/* tail */}
      <path d="M44 38c4-1 7 1 7 4" />
      {/* feet */}
      <path d="M25 53v3" />
      <path d="M31 53v3" />
      <path d="M37 53v3" />
    </svg>
  );
}
