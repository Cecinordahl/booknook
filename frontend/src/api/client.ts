import { auth } from "../firebase";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export class ApiError extends Error {
  constructor(
    public status: number,
    public code: string,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const user = auth.currentUser;
  const idToken = user ? await user.getIdToken() : null;
  // FormData bodies (file uploads) need the browser to set its own multipart boundary — setting
  // Content-Type ourselves would strip that and break the request.
  const isFormData = init?.body instanceof FormData;

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      ...(isFormData ? {} : { "Content-Type": "application/json" }),
      ...(idToken ? { Authorization: `Bearer ${idToken}` } : {}),
      ...init?.headers,
    },
  });

  if (!response.ok) {
    let code = "unknown_error";
    let message = response.statusText;
    try {
      const body = await response.json();
      code = body.error ?? code;
      message = body.message ?? message;
    } catch {
      // response had no JSON body — fall back to statusText above
    }
    throw new ApiError(response.status, code, message);
  }

  // Don't key off status 204 specifically — Spring returns 200 with an empty body for `void`
  // controller methods (follow/unfollow, push subscribe, account delete, ...), and calling
  // response.json() on an empty body throws. Read as text first and only parse if non-empty.
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body !== undefined ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "PUT", body: body !== undefined ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "DELETE", body: body !== undefined ? JSON.stringify(body) : undefined }),
  upload: <T>(path: string, formData: FormData) => request<T>(path, { method: "POST", body: formData }),
};
