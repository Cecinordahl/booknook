import { useState, type FormEvent } from "react";
import { Navigate } from "react-router-dom";
import { signInWithGoogle, sendLoginLink } from "../firebase";
import { useAuth } from "../auth/AuthProvider";

export function LoginPage() {
  const { firebaseUser, loading } = useAuth();
  const [email, setEmail] = useState("");
  const [linkSent, setLinkSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!loading && firebaseUser) {
    return <Navigate to="/" replace />;
  }

  async function handleGoogle() {
    setError(null);
    try {
      await signInWithGoogle();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Google sign-in failed.");
    }
  }

  async function handleEmailLink(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await sendLoginLink(email);
      setLinkSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send the sign-in link.");
    }
  }

  return (
    <div className="page" style={{ maxWidth: 420 }}>
      <h1>Booknook</h1>
      <p style={{ color: "var(--color-text-muted)" }}>Invite-only. Sign in with an invited email address.</p>

      <button className="btn" onClick={handleGoogle} style={{ width: "100%", justifyContent: "center" }}>
        Sign in with Google
      </button>

      <div style={{ margin: "20px 0", color: "var(--color-text-muted)", textAlign: "center" }}>or</div>

      {linkSent ? (
        <p>Check your inbox at <strong>{email}</strong> for a sign-in link.</p>
      ) : (
        <form onSubmit={handleEmailLink} style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <label>
            <div>Email</div>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              style={{ width: "100%" }}
            />
          </label>
          <button className="btn secondary" type="submit">
            Email me a sign-in link
          </button>
        </form>
      )}

      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
