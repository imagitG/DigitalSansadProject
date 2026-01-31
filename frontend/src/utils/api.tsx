import { jwtDecode } from "jwt-decode";

export const AUTH_SERVICE_API =
  process.env.NEXT_PUBLIC_AUTH_SERVICE_API!;

export const SOW_SERVICE_API =
  process.env.NEXT_PUBLIC_SOW_SERVICE_API!;

export const CHATBOT_SERVICE_API =
  process.env.NEXT_PUBLIC_CHATBOT_SERVICE_API!;

export class ApiError extends Error {
  status: number;
  code?: string;

  constructor(message: string, status: number, code?: string) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

export async function apiFetch(
  path: string,
  options: RequestInit = {},
  baseUrl: string
) {
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("auth_token")
      : null;

  // Don't force Content-Type for FormData (let browser set multipart/form-data)
  const headers: Record<string, string> = {};
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      ...headers,
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : {};

  if (!res.ok) {
    throw new ApiError(
      data.message || data.error || "Request failed",
      res.status,
      data.code
    );
  }

  return data;
}



export function getUserRoles(): string[] {
  if (typeof window === "undefined") {
    return []; // 👈 SSR safe
  }

  const token = localStorage.getItem("auth_token");
  if (!token) return [];

  try {
    const decoded: any = jwtDecode(token);
    return decoded.roles || [];
  } catch {
    return [];
  }
}

export function getUserName(): string {
  if (typeof window === "undefined") {
    return "";
  }

  const token = localStorage.getItem("auth_token");
  if (!token) return "";

  try {
    const decoded: any = jwtDecode(token);
    return decoded.name || "";
  } catch {
    return "";
  }
}

