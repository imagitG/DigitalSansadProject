"use client";

import { useRouter } from "next/navigation";

export default function LandingPage() {
  const router = useRouter();

  return (
    <main className="relative min-h-screen w-full overflow-hidden">

      {/* Background Image */}
      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage:
            "url('/bg-sansad.png')" // put image in /public
        }}
      />

      {/* Gradient Overlay */}
      <div className="absolute inset-0 bg-gradient-to-r from-black/85 via-black/70 to-yellow-900/40" />

      {/* Content */}
      <div className="relative z-10 min-h-screen flex items-center">
        <div className="w-full max-w-6xl px-8 md:px-16">

          {/* LEFT HALF CONTENT */}
          <div className="max-w-2xl text-white">

            <h1
              className="text-4xl md:text-6xl font-semibold leading-tight mb-6"
              style={{ fontFamily: "'Playfair Display', serif" }}
            >
              Welcome to <br />
              <span className="text-yellow-300">Digital Sansad</span>
            </h1>

            <p className="text-lg md:text-xl text-slate-200 mb-10 leading-relaxed">
              A unified digital platform for secure, transparent and
              intelligent handling of parliamentary and institutional
              documents — enabling structured workflows and AI-powered
              querying.
            </p>

            {/* Buttons */}
            <div className="flex gap-6">
              <button
                onClick={() => router.push("/login")}
                className="px-8 py-3 rounded-md bg-yellow-600 hover:bg-yellow-500 text-black font-semibold transition shadow-lg"
              >
                Login
              </button>

              <button
                onClick={() => router.push("/signup")}
                className="px-8 py-3 rounded-md border border-yellow-400 text-yellow-300 hover:bg-yellow-400/10 font-semibold transition"
              >
                Sign Up
              </button>
            </div>

          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="absolute bottom-4 left-8 text-sm text-slate-300">
        © {new Date().getFullYear()} Digital Sansad • Government of India (Prototype)
      </footer>
    </main>
  );
}
