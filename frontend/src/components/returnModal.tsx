"use client";

import { useState } from "react";

interface ReturnModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (comment: string) => Promise<void>;
  isLoading?: boolean;
}

export default function ReturnModal({
  isOpen,
  onClose,
  onConfirm,
  isLoading = false,
}: ReturnModalProps) {
  const [comment, setComment] = useState("");
  const [error, setError] = useState<string | null>(null);

  const handleConfirm = async () => {
    if (!comment.trim()) {
      setError("Please enter a message");
      return;
    }

    try {
      setError(null);
      await onConfirm(comment);
      setComment("");
    } catch (err: any) {
      setError(err.message || "Failed to return document");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-lg font-semibold text-[#0B0B0B] mb-4">
          Return Document
        </h2>

        <p className="text-sm text-[#5A5A5A] mb-4">
          Please enter a message explaining why you are returning this document.
        </p>

        <textarea
          value={comment}
          onChange={(e) => {
            setComment(e.target.value);
            setError(null);
          }}
          placeholder="Enter your message here..."
          className="
            w-full border border-[#E5E5E5] rounded-lg
            px-3 py-2 text-sm text-[#2E2E2E]
            focus:border-[#D4A017] focus:ring-2
            focus:ring-[rgba(212,160,23,0.4)]
            resize-none h-24 mb-4
          "
          disabled={isLoading}
        />

        {error && (
          <div className="text-sm text-red-600 mb-4 p-2 bg-red-50 rounded">
            {error}
          </div>
        )}

        <div className="flex gap-3 justify-end">
          <button
            onClick={onClose}
            disabled={isLoading}
            className="
              px-4 py-2 rounded-lg text-sm
              border border-[#E5E5E5] text-[#2E2E2E]
              hover:bg-[#FFF8E1]
              disabled:opacity-50 disabled:cursor-not-allowed
            "
          >
            Cancel
          </button>

          <button
            onClick={handleConfirm}
            disabled={isLoading}
            className="
              px-4 py-2 rounded-lg text-sm font-medium
              bg-[#0B0B0B] text-white
              hover:bg-[#454545c1]
              disabled:opacity-50 disabled:cursor-not-allowed
            "
          >
            {isLoading ? "Returning..." : "Confirm Return"}
          </button>
        </div>
      </div>
    </div>
  );
}
