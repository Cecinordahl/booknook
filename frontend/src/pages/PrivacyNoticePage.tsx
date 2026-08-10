export function PrivacyNoticePage() {
  return (
    <div className="page" style={{ maxWidth: 680 }}>
      <h1>Privacy notice</h1>
      <p style={{ color: "var(--color-text-muted)" }}>
        Booknook is a small, invite-only app built for a group of friends — not a public product. This notice is a
        plain-language summary, not a legal document. See the project README for the full disclaimer.
      </p>

      <h3>What we collect</h3>
      <ul>
        <li>Your email address and display name, to sign you in and enforce the invite list.</li>
        <li>
          The books you add: title, author, cover image, reading status/progress, format, genre, personal
          rating, and any mood/theme tags you assign.
        </li>
        <li>The series you choose to follow, so we know when to send you a release reminder.</li>
        <li>A push notification subscription, only if you opt in to release reminders.</li>
        <li>
          Screenshot images used for the OCR "scan a book cover" feature are processed entirely in your
          browser and are never uploaded to our server or stored anywhere.
        </li>
      </ul>

      <h3>Why</h3>
      <p>
        All of the above exists to run the app's core features: tracking your reading, filtering your library, and
        reminding you about upcoming releases in series you follow.
      </p>

      <h3>Legal basis</h3>
      <p>
        Given this is a closed, invite-only app for friends, the legal basis for processing this data is your
        consent (given by choosing to sign in and use the app) and legitimate interest (running a small shared
        tool for a group you're part of).
      </p>

      <h3>Who can see your data</h3>
      <p>
        Your library is private to your account. Booknook does not have social features — other users cannot see
        your books, ratings, or reading activity.
      </p>

      <h3>Deleting your data</h3>
      <p>
        You can permanently delete your account and all associated data at any time from{" "}
        <a href="/settings">Settings</a>. This removes your books, series follows, push subscriptions, and account
        record, and revokes your invite — you'd need to be re-invited to sign in again.
      </p>
    </div>
  );
}
