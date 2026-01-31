"use client";

import { useRouter } from "next/navigation";
import Image from "next/image";
import { logout } from "@/services/authService";
import { useEffect, useState } from "react";
import { getUserName } from "@/utils/api";
import { getUserRoles } from "@/utils/api";

export default function Navbar() {
  const router = useRouter();

  const [name, setName] = useState<string>("");
  const [roles, setRoles] = useState<string[]>([]);


  useEffect(() => {
    const n = getUserName();
    setName(n);
    const r = getUserRoles();
    setRoles(r);
  }, []);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  return (
    <header className="w-full bg-black border-b shadow-sm h-auto">
      <div className="max-w-8xl mx-auto px-6 py-4 flex items-center justify-between">
        {/* Left: Logo + Title */}
        <div
          className="flex items-center gap-3 cursor-pointer"
          onClick={() => router.push("/home")}
        >
          <Image
            src="/icon.jpg"
            alt="Digital Sansad"
            width={36}
            height={36}
          />
          <span className="text-xl font-semibold text-white">
            Digital Sansad
          </span>
        </div>

        {/* Right: Actions */}
        <div className="flex items-center gap-4">
          <button
            onClick={() => router.push("/home")}
            className="px-4 py-2 text-sm font-medium text-white hover:text-yellow-400 transition"
          >
            Home
          </button>

          <button
            onClick={() => router.push("/chatbot")}
            className="px-4 py-2 text-sm font-medium text-white hover:text-yellow-400 transition"
          >
            Chatbot
          </button>

          <button
            onClick={handleLogout}
            className="px-4 py-2 text-sm font-semibold text-white bg-blue-500 rounded-lg hover:bg-blue-600 transition"
          >
            Logout
          </button>

          <div className="flex flex-col">

            <span className="text-sm text-white">
              {name}
            </span>
            <span className="text-sm font-medium text-white">
              {roles.join(", \n")}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
}
