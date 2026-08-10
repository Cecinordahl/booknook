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

/** Lets the user opt in to release-reminder push notifications for series they follow. */
export function PushOptIn() {
  const [status, setStatus] = useState<"unknown" | "subscribed" | "unsubscribed" | "unsupported">("unknown");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
      setStatus("unsupported");
      return;
    }
    navigator.serviceWorker.ready.then(async (registration) => {
      const existing = await registration.pushManager.getSubscription();
      setStatus(existing ? "subscribed" : "unsubscribed");
    });
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
      setStatus("subscribed");
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
    setStatus("unsubscribed");
  }

  if (status === "unsupported") {
    return <p className="error-text">Push notifications aren't supported in this browser.</p>;
  }

  return (
    <div>
      {isIosNotStandalone() && (
        <p style={{ color: "var(--color-text-muted)", fontSize: "0.85rem", marginTop: 0 }}>
          On iPhone, notifications only work if Booknook is added to your Home Screen (Share →
          Add to Home Screen). Otherwise you'll only see them while this tab is open.
        </p>
      )}
      {status === "subscribed" ? (
        <button className="btn secondary" onClick={unsubscribe}>
          Disable release notifications
        </button>
      ) : (
        <button className="btn" onClick={subscribe}>
          Enable release notifications
        </button>
      )}
      {error && <p className="error-text">{error}</p>}
    </div>
  );
}
