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
      <NavLink to="/settings">Settings</NavLink>
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
