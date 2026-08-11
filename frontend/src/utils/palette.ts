/** Accent colors used to give book covers/spines visual variety when there's no real cover image. */
export const ACCENT_PALETTE = [
  "#c1932b", // gold
  "#c3502a", // terracotta
  "#6e3e7a", // plum
  "#3733a8", // ink-mid
  "#8a6a1f", // deeper gold
  "#8f3c20", // deeper terracotta
];

export function hashString(value: string): number {
  let hash = 0;
  for (let i = 0; i < value.length; i++) {
    hash = (hash << 5) - hash + value.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function colorForId(id: string): string {
  return ACCENT_PALETTE[hashString(id) % ACCENT_PALETTE.length];
}
