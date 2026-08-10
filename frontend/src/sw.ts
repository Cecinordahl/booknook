/// <reference lib="webworker" />
import { precacheAndRoute, type PrecacheEntry } from "workbox-precaching";
import { clientsClaim } from "workbox-core";

declare let self: ServiceWorkerGlobalScope & {
  __WB_MANIFEST: Array<PrecacheEntry | string>;
};

self.skipWaiting();
clientsClaim();

// Populated at build time by vite-plugin-pwa's injectManifest strategy.
precacheAndRoute(self.__WB_MANIFEST);

self.addEventListener("push", (event) => {
  if (!event.data) return;

  let payload: { title?: string; body?: string } = {};
  try {
    payload = event.data.json();
  } catch {
    payload = { title: "Booknook", body: event.data.text() };
  }

  event.waitUntil(
    self.registration.showNotification(payload.title ?? "Booknook", {
      body: payload.body ?? "",
      icon: "/icons/icon-192.png",
      badge: "/icons/icon-192.png",
    }),
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  event.waitUntil(self.clients.openWindow("/"));
});
