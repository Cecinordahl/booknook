import { signOut } from "../firebase";

export function NotInvitedPage() {
  return (
    <div className="page" style={{ maxWidth: 480 }}>
      <h1>Not invited yet</h1>
      <p>
        This account signed in successfully, but it isn't on Booknook's invite list. Ask the person who runs this
        Booknook instance to add your email address.
      </p>
      <button className="btn secondary" onClick={() => signOut()}>
        Sign out
      </button>
    </div>
  );
}
