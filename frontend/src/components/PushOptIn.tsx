import { useEffect, useState } from "react";
import { pushApi } from "../api/push";

function isIosNotStandalone(): boolean {
  const isIos = /iPhone|iPad|iPod/.test(navigator.userAgent);
  const isStandalone =
    window.matchMedia("(display-mode: standalone)").matches ||
    // Safari's non-standard property for "launched from the Home Screen icon".
    (navigator as Navigator & { standalone?: boolean }).standalone === true;
  return isIos && !isStandalone;
}

function urlBase64ToUint8Array(base64: string): Uint8Array<ArrayBuffer> {
  const padding = "=".repeat((4 - (base64.length % 4)) % 4);
  const base64Safe = (base64 + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = window.atob(base64Safe);
  const bytes = new Uint8Array(new ArrayBuffer(rawData.length));
  for (let i = 0; i < rawData.length; i++) {
    bytes[i] = rawData.charCodeAt(i);
  }
  return bytes;
}

export type PushStatus = "unknown" | "subscribed" | "unsubscribed" | "unsupported";

/** Lets the user opt in to release-reminder push notifications for series they follow. */
export function PushOptIn({ onStatusChange }: { onStatusChange?: (status: PushStatus) => void }) {
  const [status, setStatus] = useState<PushStatus>("unknown");
  const [error, setError] = useState<string | null>(null);
  const [showIosInstructions, setShowIosInstructions] = useState(false);

  function updateStatus(next: PushStatus) {
    setStatus(next);
    onStatusChange?.(next);
  }

  useEffect(() => {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
      updateStatus("unsupported");
      return;
    }
    navigator.serviceWorker.ready.then(async (registration) => {
      const existing = await registration.pushManager.getSubscription();
      updateStatus(existing ? "subscribed" : "unsubscribed");
    });
    // Runs once on mount to read the existing subscription — onStatusChange intentionally
    // excluded so a new inline function identity from the parent doesn't re-trigger this.
  }, []);

  async function subscribe() {
    setError(null);
    try {
      const permission = await Notification.requestPermission();
      if (permission !== "granted") {
        setError("Notification permission was not granted.");
        return;
      }
      const registration = await navigator.serviceWorker.ready;
      const vapidPublicKey = import.meta.env.VITE_VAPID_PUBLIC_KEY;
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidPublicKey),
      });
      await pushApi.subscribe(subscription.toJSON() as PushSubscriptionJSON);
      updateStatus("subscribed");
    } catch (e) {
      setError(e instanceof Error ? e.message : "Could not enable notifications.");
    }
  }

  async function unsubscribe() {
    setError(null);
    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();
    if (subscription) {
      await pushApi.unsubscribe(subscription.endpoint);
      await subscription.unsubscribe();
    }
    updateStatus("unsubscribed");
  }

  if (status === "unsupported") {
    return <p className="error-text">Push notifications aren't supported in this browser.</p>;
  }

  return (
    <div>
      <div style={{ display: "flex", gap: 10, alignItems: "center", flexWrap: "wrap" }}>
        {status === "subscribed" ? (
          <button className="btn secondary" onClick={unsubscribe}>
            Disable release notifications
          </button>
        ) : (
          <button className="btn" onClick={subscribe}>
            Enable release notifications
          </button>
        )}
        {isIosNotStandalone() && (
          <button
            type="button"
            aria-label="Why do I need to add this to my Home Screen?"
            title="iPhone setup info"
            onClick={() => setShowIosInstructions((open) => !open)}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 22,
              height: 22,
              borderRadius: "50%",
              border: "1px solid var(--color-text-muted)",
              background: "transparent",
              color: "var(--color-text-muted)",
              cursor: "pointer",
              fontSize: "0.8rem",
              fontStyle: "italic",
              fontFamily: "Georgia, serif",
              padding: 0,
            }}
          >
            i
          </button>
        )}
      </div>

      {isIosNotStandalone() && showIosInstructions && (
        <ol style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", marginTop: 10, paddingLeft: 20 }}>
          <li>Tap the Share button in Safari.</li>
          <li>Scroll down and tap "Add to Home Screen".</li>
          <li>Open Booknook from the Home Screen icon you just created, then come back here to enable notifications.</li>
        </ol>
      )}

      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
