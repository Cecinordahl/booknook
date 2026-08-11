import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";

// Uses the "injectManifest" strategy (a hand-written service worker at src/sw.ts) rather than
// the default generated one, because push notifications need custom `push`/`notificationclick`
// event handlers that the auto-generated Workbox service worker doesn't provide.
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      strategies: "injectManifest",
      srcDir: "src",
      filename: "sw.ts",
      registerType: "autoUpdate",
      injectRegister: "auto",
      manifest: {
        name: "Booknook",
        short_name: "Booknook",
        description: "A personal, invite-only book tracker for a small group of friends.",
        theme_color: "#1C1A63",
        background_color: "#F3ECDA",
        display: "standalone",
        start_url: "/",
        icons: [
          { src: "/icons/icon-192.png", sizes: "192x192", type: "image/png" },
          { src: "/icons/icon-512.png", sizes: "512x512", type: "image/png" },
          { src: "/icons/icon-512-maskable.png", sizes: "512x512", type: "image/png", purpose: "maskable" }
        ]
      },
      injectManifest: {
        // Small app shell — no need to precache large third-party assets like the Tesseract.js
        // worker/wasm, which are fetched on demand only when the OCR flow is actually used.
        globPatterns: ["**/*.{js,css,html,svg,png,ico}"]
      },
      devOptions: {
        enabled: true,
        type: "module"
      }
    })
  ],
  server: {
    port: 5173
  }
});
