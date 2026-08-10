import type { Book } from "../types";

export function buildOutboundLinks(book: Pick<Book, "title" | "authors">) {
  const query = [book.title, ...(book.authors ?? [])].filter(Boolean).join(" ");
  const encoded = encodeURIComponent(query);

  return {
    audible: `https://www.audible.com/search?keywords=${encoded}`,
    kindle: `https://www.amazon.com/s?k=${encoded}&i=digital-text`,
    google: `https://www.google.com/search?q=${encoded}`,
  };
}

export function OutboundLinks({ book }: { book: Pick<Book, "title" | "authors"> }) {
  const links = buildOutboundLinks(book);

  return (
    <div style={{ display: "flex", gap: 12 }}>
      <a className="btn secondary" href={links.audible} target="_blank" rel="noreferrer">
        Audible
      </a>
      <a className="btn secondary" href={links.kindle} target="_blank" rel="noreferrer">
        Kindle Store
      </a>
      <a className="btn secondary" href={links.google} target="_blank" rel="noreferrer">
        Google
      </a>
    </div>
  );
}
