import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { onAuthStateChanged, type User } from "firebase/auth";
import { auth } from "../firebase";
import { meApi } from "../api/me";
import { ApiError } from "../api/client";
import type { UserAccount } from "../types";

interface AuthState {
  loading: boolean;
  firebaseUser: User | null;
  account: UserAccount | null;
  notInvited: boolean;
  refreshAccount: () => Promise<void>;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [loading, setLoading] = useState(true);
  const [firebaseUser, setFirebaseUser] = useState<User | null>(null);
  const [account, setAccount] = useState<UserAccount | null>(null);
  const [notInvited, setNotInvited] = useState(false);

  async function loadAccount() {
    try {
      const me = await meApi.get();
      setAccount(me);
      setNotInvited(false);
    } catch (e) {
      if (e instanceof ApiError && e.status === 403) {
        setNotInvited(true);
        setAccount(null);
      } else {
        throw e;
      }
    }
  }

  useEffect(() => {
    return onAuthStateChanged(auth, async (user) => {
      setFirebaseUser(user);
      if (user) {
        await loadAccount();
      } else {
        setAccount(null);
        setNotInvited(false);
      }
      setLoading(false);
    });
  }, []);

  return (
    <AuthContext.Provider value={{ loading, firebaseUser, account, notInvited, refreshAccount: loadAccount }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
