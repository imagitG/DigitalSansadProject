"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { verifyOtp, login } from "@/services/authService";
import { getUserRoles } from "@/utils/api";

export default function VerifyOtpPage() {
  const router = useRouter();
  const [otp, setOtp] = useState("");
  const [timeLeft, setTimeLeft] = useState(120);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const email =
    typeof window !== "undefined"
      ? localStorage.getItem("auth_email")
      : null;

  useEffect(() => {
    if (timeLeft === 0) return;
    const timer = setInterval(() => setTimeLeft(t => t - 1), 1000);
    return () => clearInterval(timer);
  }, [timeLeft]);

  const handleVerify = async () => {
    if (!email) return router.push("/login");

    setLoading(true);
    setError(null);

    try {
      const res = await verifyOtp(email, otp);
      localStorage.setItem("auth_token", res.token);
      getUserRoles();
      router.push("/home");
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) return;
    await login(email);
    setTimeLeft(120);
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
        <div className="w-full max-w-md bg-white/95 rounded-2xl shadow-xl p-8 space-y-6">

          <h1 className="heading-font text-2xl text-center text-[#0B0B0B]">
            Verify OTP
          </h1>

          {error && (
            <div className="text-red-700 bg-red-50 border border-red-200 text-sm p-3 rounded-lg">
              {error}
            </div>
          )}

          <input
            placeholder="Enter 6-digit OTP"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
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
            onClick={handleVerify}
            disabled={loading}
          >
            {loading ? "Verifying..." : "Verify & Continue"}
          </button>

          {timeLeft > 0 ? (
            <p className="text-sm text-center text-[#8A8A8A]">
              Resend OTP in {timeLeft}s
            </p>
          ) : (
            <button
              onClick={handleResend}
              className="text-[#2563EB] text-sm mx-auto block hover:underline"
            >
              Resend OTP
            </button>
          )}
        </div>
      </div>
    </main>
  );
}
