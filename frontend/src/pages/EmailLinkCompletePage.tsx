import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { completeEmailLinkSignIn, isEmailLinkUrl } from "../firebase";

export function EmailLinkCompletePage() {
  const [needsEmail, setNeedsEmail] = useState(false);
  const [email, setEmail] = useState("");
  const [done, setDone] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function attempt(url: string) {
    try {
      await completeEmailLinkSignIn(url);
      setDone(true);
    } catch (e) {
      if (e instanceof Error && e.message === "MISSING_EMAIL_FOR_SIGN_IN") {
        setNeedsEmail(true);
      } else {
        setError(e instanceof Error ? e.message : "That sign-in link is invalid or expired.");
      }
    }
  }

  useEffect(() => {
    const url = window.location.href;
    if (!isEmailLinkUrl(url)) {
      setError("This isn't a valid sign-in link.");
      return;
    }
    attempt(url);
  }, []);

  if (done) {
    return <Navigate to="/" replace />;
  }

  if (needsEmail) {
    return (
      <div className="page" style={{ maxWidth: 420 }}>
        <h1>Confirm your email</h1>
        <p>You opened this link on a different device or browser — enter the email you used to request it.</p>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            window.localStorage.setItem("booknook.emailForSignIn", email);
            attempt(window.location.href);
          }}
          style={{ display: "flex", gap: 10 }}
        >
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          <button className="btn" type="submit">
            Continue
          </button>
        </form>
        {error && <p className="error-text">{error}</p>}
      </div>
    );
  }

  return <div className="page-loading">{error ? <p className="error-text">{error}</p> : "Signing you in…"}</div>;
}
