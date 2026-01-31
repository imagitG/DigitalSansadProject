"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { login } from "@/services/authService";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(email);
      localStorage.setItem("auth_email", email);
      router.push("/verify-otp");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen relative">

      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{ backgroundImage: "url('/bg-sansad.png')" }}
      />
      <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/70 to-yellow-900/40" />

      <nav className="relative z-10 h-16 flex items-center px-6">
        <div className="flex items-center gap-2">
          <img src="/parliament-icon.png" className="h-8 w-8" />
          <span className="heading-font text-lg text-[#0B0B0B]">
            Digital Sansad
          </span>
        </div>
      </nav>

      <div className="relative z-10 flex items-center justify-center min-h-[calc(100vh-4rem)] px-4">
        <form
          className="w-full max-w-md bg-white/95 rounded-2xl shadow-xl p-8 space-y-6"
          onSubmit={handleSubmit}
        >
          <h1 className="heading-font text-2xl text-center text-[#0B0B0B]">
            Login
          </h1>

          {error && (
            <div className="text-red-700 bg-red-50 border border-red-200 text-sm p-3 rounded-lg">
              {error}
            </div>
          )}

          <input
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="
              w-full rounded-lg border border-[#E5E5E5]
              px-3 py-2 text-sm
              focus:border-[#D4A017]
              focus:ring-2 focus:ring-[rgba(212,160,23,0.4)]
            "
          />

          <button
            className="
              w-full rounded-lg py-3 font-semibold
              bg-[#D4A017] text-[#0B0B0B]
              hover:bg-[#E6B325]
              disabled:bg-[#F1E4B3]
            "
            disabled={loading}
          >
            {loading ? "Sending OTP..." : "Send OTP"}
          </button>

          <p className="text-center text-sm text-[#5A5A5A]">
            New user?{" "}
            <a href="/signup" className="text-[#2563EB] font-medium hover:underline">
              Create account
            </a>
          </p>
        </form>
      </div>
    </main>
  );
}
