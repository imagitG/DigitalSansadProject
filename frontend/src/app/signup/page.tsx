"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { signup } from "@/services/authService";

export default function SignupPage() {
    const router = useRouter();

    const [form, setForm] = useState({
        name: "",
        designation: "",
        email: "",
        mobile: ""
    });

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await signup(form);
            localStorage.setItem("auth_email", form.email);
            router.push("/verify-otp");
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="min-h-screen relative">

            {/* Background */}
            <div
                className="absolute inset-0 bg-cover bg-center"
                style={{ backgroundImage: "url('/bg-sansad.png')" }}
            />
            <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/70 to-yellow-900/40" />

            {/* Navbar */}
            <nav className="relative z-10 h-16 flex items-center px-6">
                <div className="flex items-center gap-2">
                    <img src="/parliament-icon.png" className="h-8 w-8" />
                    <span className="heading-font text-lg text-[#0B0B0B]">
                        Digital Sansad
                    </span>
                </div>
            </nav>

            {/* Content */}
            <div className="relative z-10 flex items-center justify-center min-h-[calc(100vh-4rem)] px-4">
                <div className="w-full max-w-md bg-white/95 rounded-2xl shadow-xl p-8 space-y-6">

                    <div className="text-center space-y-1">
                        <p className="text-sm text-[#8A8A8A]">Welcome to</p>
                        <h1 className="heading-font text-3xl text-[#0B0B0B]">
                            Digital <span className="text-[#D4A017]">Sansad</span>
                        </h1>
                        <p className="text-sm text-[#5A5A5A]">
                            Integrated Application Platform
                        </p>
                    </div>

                    {error && (
                        <div className="text-red-700 bg-red-50 border border-red-200 text-sm p-3 rounded-lg">
                            {error}
                        </div>
                    )}

                    <form className="space-y-4" onSubmit={handleSubmit}>
                        {["name", "designation", "email", "mobile"].map((field) => (
                            <input
                                key={field}
                                name={field}
                                placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
                                onChange={handleChange}
                                className="
                  w-full rounded-lg border border-[#E5E5E5]
                  px-3 py-2 text-sm text-[#2E2E2E]
                  placeholder:text-[#8A8A8A]
                  focus:border-[#D4A017]
                  focus:ring-2 focus:ring-[rgba(212,160,23,0.4)]
                "
                            />
                        ))}

                        <button
                            className="
                w-full rounded-lg py-3 font-semibold
                bg-[#D4A017] text-[#0B0B0B]
                hover:bg-[#E6B325] active:bg-[#B88A0A]
                disabled:bg-[#F1E4B3] disabled:text-[#8A8A8A]
              "
                            disabled={loading}
                        >
                            {loading ? "Sending OTP..." : "Send OTP"}
                        </button>
                    </form>

                    <p className="text-center text-sm text-[#5A5A5A]">
                        Already registered?{" "}
                        <a href="/login" className="text-[#2563EB] font-medium hover:underline">
                            Login
                        </a>
                    </p>
                </div>
            </div>
        </main>
    );
}
