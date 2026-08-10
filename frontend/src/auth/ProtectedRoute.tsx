import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "./AuthProvider";

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { loading, firebaseUser, notInvited } = useAuth();

  if (loading) {
    return <div className="page-loading">Loading…</div>;
  }
  if (!firebaseUser) {
    return <Navigate to="/login" replace />;
  }
  if (notInvited) {
    return <Navigate to="/not-invited" replace />;
  }
  return <>{children}</>;
}
