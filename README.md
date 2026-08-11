# Booknook

A personal, invite-only book-tracking app for a small group of friends. Track what you're
reading, follow series for release reminders, and browse a visual "bookshelf" that fills up as
your library grows.

**Not legal advice.** The access-control and data-deletion approach here is a reasonable,
pragmatic starting point for a small friends-and-family project — not a substitute for legal
review. If this app ever grows beyond that scope, revisit the privacy/legal approach properly.

## Stack

- **Backend:** Java 21 / Spring Boot 3, REST API, Firestore via the Firebase Admin SDK
- **Frontend:** React + TypeScript, Vite, installable PWA (camera access, web push)
- **Database:** Firestore
- **External data:** Hardcover.app GraphQL API (series/release dates), Google Books + Open
  Library (book metadata), both cached in Firestore
- **Auth:** Firebase Authentication (Google Sign-In + email link), gated by a Firestore
  email allowlist

This guide assumes you're comfortable with Java/Spring and React/TypeScript, but not necessarily
with deploy tooling — every step below is spelled out.

---

## 1. Prerequisites

- **Java 21** and **Maven** (or use the bundled `./mvnw` wrapper — no local Maven install needed)
- **Node.js 20+** and npm
- A free **Firebase** account (https://console.firebase.google.com)
- A free **Hardcover.app** account (https://hardcover.app) for series/release data

## 2. Set up Firebase

1. Go to the [Firebase console](https://console.firebase.google.com) and **create a new project**
   (any name — e.g. "booknook").
2. **Enable Authentication providers:** Build > Authentication > Sign-in method > enable
   **Google** and **Email link (passwordless sign-in)**.
3. **Create a Firestore database:** Build > Firestore Database > Create database > start in
   **production mode** (the backend uses the Admin SDK, which bypasses security rules, so the
   default production-mode rules are fine — the backend is the only thing that talks to Firestore).
4. **Generate a service-account key for the backend:** Project settings (gear icon) > Service
   accounts > Generate new private key. This downloads a JSON file — save it somewhere outside
   the repo (e.g. `~/secrets/booknook-firebase.json`). This is the credential for
   `FIREBASE_CREDENTIALS_PATH`.
5. **Get the web app config for the frontend:** Project settings > General > Your apps > Add app
   > Web. Register an app (any nickname). Copy the `apiKey`, `authDomain`, `projectId`, and
   `appId` values shown — these go into the frontend's `.env.local`.

## 3. Invite yourself (and your friends)

There's no admin UI for the invite list in this version — entries are added directly in the
Firestore console, which is fine for a handful of people:

1. Firestore Database > Start collection > collection ID: `allowlist`.
2. Add a document per invited person: **document ID is their lowercased email address**
   (e.g. `friend@example.com`), with fields `addedBy` (string) and `addedAt` (timestamp).
3. Repeat for everyone you want to invite, including yourself.

Only emails with a document in this collection can sign in — everyone else gets a clear
"not invited" screen after authenticating with Firebase.

## 4. Get a Hardcover API key

Sign up at https://hardcover.app, then find your API key in your account settings
(https://hardcover.app/account/api). This powers series search and release-date lookups; without
it, the app still runs — series search just returns no results until you set it.

## 5. Get a Google Books API key (optional but recommended)

ISBN lookup and title search-as-you-type both use the Google Books API, which works without a
key — but the anonymous/no-key quota is small and shared across everyone using it unauthenticated,
so it 429s (rate-limits) easily even under light use. To fix that:

1. Go to https://console.cloud.google.com/apis/library/books.googleapis.com and select your
   Firebase project, then click **Enable**.
2. Go to https://console.cloud.google.com/apis/credentials (same project) > **Create Credentials
   > API key**.
3. Click the new key > under "API restrictions" choose **Restrict key** > select **Books API**
   only > Save.
4. This is `GOOGLE_BOOKS_API_KEY` in the backend's `.env`.

## 6. Generate VAPID keys for web push

VAPID keys authenticate your server to push services. Generate a pair with:

```bash
npx web-push generate-vapid-keys
```

This prints a public and private key. The public key goes to the frontend
(`VITE_VAPID_PUBLIC_KEY`), the private key (and the same public key) go to the backend
(`VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY`). Without these, the app still runs — push
notifications just stay disabled until configured.

## 7. Local development

### Backend

```bash
cd backend
cp .env.example .env   # then fill in the values from steps 2, 4, 5, 6 above
```

Load the `.env` values into your shell and run:

```bash
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

The API starts on http://localhost:8080. `FIREBASE_PROJECT_ID` and `FIREBASE_CREDENTIALS_PATH`
are required for the app to start at all; `HARDCOVER_API_KEY`, `GOOGLE_BOOKS_API_KEY`, and the
`VAPID_*` keys are optional at startup — only the features that need them are degraded without
them (a warning is logged).

### Frontend

```bash
cd frontend
cp .env.example .env.local   # fill in the values from step 2 (and 5, once you have it)
npm install
npm run dev
```

The app runs on http://localhost:5173. Sign in with an email you added to the allowlist in
step 3.

## 8. Deploy the backend (Render, free tier)

1. Push this repo to GitHub (Render deploys from a Git remote).
2. In the [Render dashboard](https://dashboard.render.com), click **New > Web Service** and
   connect the repo.
3. **Root Directory:** `backend`
4. **Runtime:** Docker is not required — pick **Java**; or if Render doesn't detect it
   automatically, set:
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/booknook-backend.jar`
5. **Environment variables:** add everything from `backend/.env.example`. For
   `FIREBASE_CREDENTIALS_PATH`, use Render's **Secret Files** feature (Environment > Secret
   Files) to upload the service-account JSON, and point the env var at the path Render gives it
   (typically `/etc/secrets/<filename>`).
6. Set `ALLOWED_ORIGIN` to your deployed frontend's URL once you have it (step 9) so CORS allows
   it — you'll need to redeploy after step 9 to update this.
7. Deploy. Render gives you a URL like `https://booknook-backend.onrender.com` — this is your
   `VITE_API_BASE_URL` for the frontend.

Free-tier Render services spin down after inactivity and take ~30–60s to wake up on the next
request — fine for a small friends app, just expect the first load after idle time to be slow.

## 9. Deploy the frontend (Vercel or Cloudflare Pages, free tier)

**Vercel:**

1. Go to https://vercel.com, **New Project**, import this repo.
2. **Root Directory:** `frontend`
3. Framework preset: Vite (should auto-detect). Build command `npm run build`, output directory
   `dist`.
4. Add the environment variables from `frontend/.env.example` (Project Settings >
   Environment Variables), using your deployed backend URL from step 8 for
   `VITE_API_BASE_URL`.
5. Deploy. Vercel gives you a URL — go back to step 8 and set the backend's `ALLOWED_ORIGIN` to
   this URL, then redeploy the backend.

**Cloudflare Pages** works the same way: connect the repo, set the root directory to `frontend`,
build command `npm run build`, output directory `dist`, and add the same environment variables.

Once both are deployed and pointed at each other, open the frontend URL, sign in with an
allowlisted email, and add your first book.

---

## Notes on what's scoped in vs. deferred

- **Email/password sign-in** was intentionally left out of this pass (Google Sign-In and email
  link are supported) — a reasonable follow-up if some invitees can't use either.
- **Audible/Kindle auto-sync** ("what am I currently reading/listening to") is explicitly *not*
  built — Amazon has no public API for this, and any working version would mean reverse-engineering
  private endpoints in violation of their Terms of Service. See
  `backend/src/main/java/com/booknook/backend/future/AudibleSyncStub.java` for the full rationale.
  The outbound Audible/Kindle links are the supported alternative.
- **Hardcover GraphQL queries** in `HardcoverClient.java` are verified against the live schema
  (confirmed via introspection and real requests, not just Hardcover's docs) — series search uses
  a Typesense-style `search.results.hits[].document` shape, and release dates are reached via
  `series_by_pk(id: ...)` → `book_series` → `book { release_date }`. If Hardcover changes their
  schema later, this is the one place to update.
- The frontend's production build reports one large JS chunk (~800KB, mostly Firebase +
  Tesseract.js + the barcode scanner). Fine for this app's scale; if it becomes a problem,
  code-splitting the OCR/scanner flows behind `import()` is the first thing to try.
- `npm audit` flags one moderate, dev-server-only advisory in esbuild/Vite (fixable via
  `npm audit fix --force`, which bumps Vite to a new major — verify `vite-plugin-pwa`
  compatibility before doing that). It doesn't affect the production build.
