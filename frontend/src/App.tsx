import { NavLink, Routes, Route, Navigate } from "react-router-dom";
import { ProtectedRoute } from "./auth/ProtectedRoute";
import { useAuth } from "./auth/AuthProvider";
import { LoginPage } from "./pages/LoginPage";
import { EmailLinkCompletePage } from "./pages/EmailLinkCompletePage";
import { NotInvitedPage } from "./pages/NotInvitedPage";
import { BookshelfLandingPage } from "./pages/BookshelfLandingPage";
import { LibraryPage } from "./pages/LibraryPage";
import { BookDetailPage } from "./pages/BookDetailPage";
import { SeriesDetailPage } from "./pages/SeriesDetailPage";
import { AddBookPage } from "./pages/AddBookPage";
import { SettingsPage } from "./pages/SettingsPage";
import { PrivacyNoticePage } from "./pages/PrivacyNoticePage";

function Nav() {
  const { firebaseUser } = useAuth();
  if (!firebaseUser) return null;

  return (
    <nav className="app-nav">
      <span className="brand">Booknook</span>
      <NavLink to="/" end>
        Shelf
      </NavLink>
      <NavLink to="/library">Library</NavLink>
      <NavLink to="/add">Add book</NavLink>
      <NavLink to="/settings" aria-label="Profile" title="Profile" style={{ display: "flex", alignItems: "center" }}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      </NavLink>
    </nav>
  );
}

export default function App() {
  return (
    <>
      <Nav />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/finish-sign-in" element={<EmailLinkCompletePage />} />
        <Route path="/not-invited" element={<NotInvitedPage />} />
        <Route path="/privacy" element={<PrivacyNoticePage />} />

        <Route
          path="/"
          element={
            <ProtectedRoute>
              <BookshelfLandingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/library"
          element={
            <ProtectedRoute>
              <LibraryPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/books/:id"
          element={
            <ProtectedRoute>
              <BookDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/series/:id"
          element={
            <ProtectedRoute>
              <SeriesDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/add"
          element={
            <ProtectedRoute>
              <AddBookPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <SettingsPage />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
