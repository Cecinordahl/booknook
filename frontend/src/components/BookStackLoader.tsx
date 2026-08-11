import "./BookStackLoader.css";

/** Playful loading state for the shelf page: a little stack of books builds itself, holds, clears, and repeats. */
export function BookStackLoader({ text = "Building your shelf…" }: { text?: string }) {
  return (
    <div className="book-stack-loader">
      <div className="book-stack-loader__stack">
        <div className="book-stack-loader__book book-stack-loader__book--1" />
        <div className="book-stack-loader__book book-stack-loader__book--2" />
        <div className="book-stack-loader__book book-stack-loader__book--3" />
        <div className="book-stack-loader__book book-stack-loader__book--4" />
        <div className="book-stack-loader__book book-stack-loader__book--5" />
        <div className="book-stack-loader__shelf" />
      </div>
      <p className="book-stack-loader__text">{text}</p>
    </div>
  );
}
