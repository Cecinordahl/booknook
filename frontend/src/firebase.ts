import { initializeApp } from "firebase/app";
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  sendSignInLinkToEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  signOut as firebaseSignOut,
} from "firebase/auth";

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

export const firebaseApp = initializeApp(firebaseConfig);
export const auth = getAuth(firebaseApp);

const googleProvider = new GoogleAuthProvider();

export function signInWithGoogle() {
  return signInWithPopup(auth, googleProvider);
}

// Passwordless email-link sign-in. Sending the link and completing it are two separate app
// loads, so we stash the email in localStorage to avoid re-prompting for it (matches Firebase's
// own recommended pattern).
const EMAIL_LINK_STORAGE_KEY = "booknook.emailForSignIn";

export function sendLoginLink(email: string) {
  const actionCodeSettings = {
    url: `${window.location.origin}/finish-sign-in`,
    handleCodeInApp: true,
  };
  return sendSignInLinkToEmail(auth, email, actionCodeSettings).then(() => {
    window.localStorage.setItem(EMAIL_LINK_STORAGE_KEY, email);
  });
}

export function isEmailLinkUrl(url: string) {
  return isSignInWithEmailLink(auth, url);
}

export async function completeEmailLinkSignIn(url: string) {
  let email = window.localStorage.getItem(EMAIL_LINK_STORAGE_KEY);
  if (!email) {
    // Fallback for cross-device link opens: the browser that opened the link may not be the one
    // that requested it, so localStorage won't have the email. Caller should prompt for it.
    throw new Error("MISSING_EMAIL_FOR_SIGN_IN");
  }
  const result = await signInWithEmailLink(auth, email, url);
  window.localStorage.removeItem(EMAIL_LINK_STORAGE_KEY);
  return result;
}

export function signOut() {
  return firebaseSignOut(auth);
}
