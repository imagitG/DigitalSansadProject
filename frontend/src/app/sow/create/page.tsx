"use client";

import { useState } from "react";
import { createSow } from "@/services/sowService";
import { useRouter } from "next/navigation";
import Navbar from "@/components/navbar";


export default function CreateSowPage() {
  const router = useRouter();
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!file) {
      alert("Please upload a PDF document");
      return;
    }

    setLoading(true);

    const formData = new FormData();
    formData.append("file", file);
    formData.append("title", title);

    try {
      await createSow(formData);
      router.push("/home");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-white">

      <Navbar />
      <div className="mx-auto max-w-xl px-8 py-10 space-y-8">

        {/* Heading */}
        <h1 className="heading-font text-3xl text-[#0B0B0B]">
          Create <span className="text-[#D4A017]">Statement of Work</span>
        </h1>

        {/* Card */}
        <div className="rounded-xl border border-[#E5E5E5] bg-white p-6 shadow-sm space-y-6">

          {/* Title */}
          <div className="space-y-1">
            <label className="text-sm font-medium text-[#2E2E2E]">
              Document Title
            </label>
            <input
              placeholder="Enter document title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="
                w-full rounded-lg border border-[#E5E5E5]
                px-3 py-2 text-sm text-[#2E2E2E]
                placeholder:text-[#8A8A8A]
                focus:outline-none focus:ring-2
                focus:ring-[rgba(212,160,23,0.4)]
                focus:border-[#D4A017]
              "
            />
          </div>

          {/* File Upload */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-[#2E2E2E]">
              Upload PDF
            </label>

            <label
              htmlFor="file-upload"
              className="
                flex flex-col items-center justify-center h-40 w-full
                cursor-pointer rounded-xl border-2 border-dashed
                border-[#E5E5E5] bg-[#F9F9F9]
                hover:border-[#D4A017] hover:bg-[#FFF8E1]
                transition
              "
            >
              <div className="flex flex-col items-center gap-2 text-[#5A5A5A] text-sm">

                <svg
                  className="h-10 w-10 text-[#8A8A8A]"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.5"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M12 16v-8m0 0-3 3m3-3 3 3M4 16v1a3 3 0 0 0 3 3h10a3 3 0 0 0 3-3v-1"
                  />
                </svg>

                {!file ? (
                  <>
                    <p className="font-medium text-[#2E2E2E]">
                      Click to upload or drag & drop
                    </p>
                    <p className="text-xs text-[#8A8A8A]">
                      PDF files only
                    </p>
                  </>
                ) : (
                  <>
                    <p className="font-medium text-[#0B0B0B]">
                      {file.name}
                    </p>
                    <p className="text-xs text-[#8A8A8A]">
                      {(file.size / 1024 / 1024).toFixed(2)} MB
                    </p>
                  </>
                )}
              </div>

              <input
                id="file-upload"
                type="file"
                accept="application/pdf"
                className="hidden"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
              />
            </label>
          </div>

          {/* Submit */}
          <button
            onClick={handleSubmit}
            disabled={loading}
            className="
              w-full rounded-lg px-6 py-3
              bg-[#D4A017] text-[#0B0B0B]
              font-semibold text-sm
              hover:bg-[#E6B325]
              active:bg-[#B88A0A]
              disabled:bg-[#F1E4B3]
              disabled:text-[#8A8A8A]
              transition
            "
          >
            {loading ? "Uploading..." : "Upload PDF"}
          </button>
        </div>
      </div>
    </main>
  );
}
