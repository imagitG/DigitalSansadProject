import { apiFetch, AUTH_SERVICE_API } from "@/utils/api";

export const signup = (data: {
  name: string;
  designation: string;
  email: string;
  mobile: string;
}) =>
  apiFetch(
    "/auth/signup",
    {
      method: "POST",
      body: JSON.stringify(data),
    },
    AUTH_SERVICE_API
  );

export const login = (email: string) =>
  apiFetch(
    "/auth/login",
    {
      method: "POST",
      body: JSON.stringify({ email }),
    },
    AUTH_SERVICE_API
  );

export const verifyOtp = (email: string, otp: string) =>
  apiFetch(
    "/auth/verify-otp",
    {
      method: "POST",
      body: JSON.stringify({ email, otp }),
    },
    AUTH_SERVICE_API
  );

export const logout = () => {
  // Call the logout endpoint
  apiFetch("/auth/logout", {
    method: "POST",
  }, AUTH_SERVICE_API
  ).catch(err => {
    // Even if the request fails, clear the token locally
    console.error("Logout error:", err);
  }).finally(() => {
    // Clear the token from localStorage
    if (typeof window !== "undefined") {
      localStorage.removeItem("auth_token");
    }
  });
};
