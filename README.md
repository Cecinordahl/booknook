# Booknook

A personal, invite-only book-tracking app for a small group of friends. Track what you're
reading, follow series for release reminders, and browse a visual "bookshelf" that fills up as
your library grows.

## Stack

- **Backend:** Java 21 / Spring Boot 3, REST API, Firestore via the Firebase Admin SDK
- **Frontend:** React + TypeScript, Vite, installable PWA (camera access, web push)
- **Database:** Firestore
- **External data:** Hardcover.app GraphQL API (series/release dates), Google Books + Open
  Library (book metadata), both cached in Firestore
- **Auth:** Firebase Authentication (Google Sign-In + email link), gated by a Firestore
  email allowlist

## Prerequisites

- **Java 21** and the bundled `./mvnw` wrapper (no local Maven install needed)
- **Node.js 20+** and npm

## Local development

### Backend

```bash
cd backend
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

Runs on http://localhost:8080.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on http://localhost:5173. Sign in with an email that's on the allowlist (see below).

## Inviting new people

There's no admin UI for this — add new people directly in the Firestore console:

1. Firestore Database > `allowlist` collection.
2. Add a document: **document ID is their lowercased email address**, with fields `addedBy`
   (string) and `addedAt` (timestamp).

Only emails with a document in this collection can sign in.

## Deploy

- **Backend (Render):** root directory `backend`, build `./mvnw clean package -DskipTests`,
  start `java -jar target/booknook-backend.jar`. Env vars from `backend/.env.example`;
  `FIREBASE_CREDENTIALS_PATH` is uploaded via Render's Secret Files and pointed at the path
  Render gives it.
- **Frontend (Vercel):** root directory `frontend`, build `npm run build`, output `dist`. Env
  vars from `frontend/.env.example`, with `VITE_API_BASE_URL` set to the deployed backend URL.
- Set the backend's `ALLOWED_ORIGIN` to the deployed frontend URL so CORS allows it.

Render's free tier spins down after inactivity — expect the first request after idle time to be
slow (~30–60s).

## Later

- **Kindle import.** Amazon has no library export, so there's no Goodreads-style bulk import for
  Kindle books. If it's worth doing later: either a "download a CSV template" button (blank file
  with the exact column headers the Goodreads importer expects, so a hand-built list doesn't need
  to guess the schema), or a looser "generic CSV" mode that accepts a simpler header set
  (`Title, Author, ISBN, Format, Status`) instead of requiring Goodreads' exact columns.
