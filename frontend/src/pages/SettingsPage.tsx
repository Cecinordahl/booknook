import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { signOut } from "../firebase";
import { meApi } from "../api/me";
import { PushOptIn, type PushStatus } from "../components/PushOptIn";
import { NotificationIntervalsEditor } from "../components/NotificationIntervalsEditor";
import { useAuth } from "../auth/AuthProvider";

export function SettingsPage() {
  const { account } = useAuth();
  const navigate = useNavigate();
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pushStatus, setPushStatus] = useState<PushStatus>("unknown");

  async function deleteAccount() {
    const confirmed = window.confirm(
      "This permanently deletes your Booknook account and all your books, series follows, and notification " +
        "subscriptions. This can't be undone. Continue?",
    );
    if (!confirmed) return;

    setDeleting(true);
    setError(null);
    try {
      await meApi.deleteAccount();
      await signOut();
      navigate("/login");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not delete your account.");
      setDeleting(false);
    }
  }

  return (
    <div className="page" style={{ maxWidth: 480 }}>
      <h1>Settings</h1>

      <div className="card" style={{ marginBottom: 16 }}>
        <p style={{ marginTop: 0 }}>Signed in as {account?.email}</p>
        <button className="btn secondary" onClick={() => signOut()}>
          Sign out
        </button>
      </div>

      <div className="card" style={{ marginBottom: 16 }}>
        <h3 style={{ marginTop: 0 }}>Notifications</h3>
        <p style={{ color: "var(--color-text-muted)", fontSize: "0.9rem" }}>
          Get notified when upcoming releases from series you follow are just around the corner.
        </p>

        <div style={{ marginTop: 16 }}>
          <div style={{ fontWeight: 600, fontSize: "0.9rem", marginBottom: 4 }}>Push notifications</div>
          <p style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", marginTop: 0 }}>
            Turns notifications on or off for this device. Nothing arrives until this is enabled.
          </p>
          <PushOptIn onStatusChange={setPushStatus} />
        </div>

        {pushStatus === "subscribed" && (
          <div style={{ marginTop: 24 }}>
            <div style={{ fontWeight: 600, fontSize: "0.9rem", marginBottom: 4 }}>When to remind me</div>
            <p style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", marginTop: 0 }}>
              How far ahead of a release you want a heads-up (up to 3). Applies to every series you follow,
              on any device where notifications are enabled.
            </p>
            <NotificationIntervalsEditor account={account} />
          </div>
        )}
      </div>

      <div className="card" style={{ marginBottom: 16 }}>
        <Link to="/privacy">Privacy notice</Link>
      </div>

      <div className="card">
        <h3 style={{ marginTop: 0 }}>Delete account</h3>
        <p style={{ color: "var(--color-text-muted)", fontSize: "0.9rem" }}>
          Permanently deletes your account and all data associated with it.
        </p>
        <button className="btn danger" onClick={deleteAccount} disabled={deleting}>
          {deleting ? "Deleting…" : "Delete my account"}
        </button>
        {error && <p className="error-text">{error}</p>}
      </div>
    </div>
  );
}
