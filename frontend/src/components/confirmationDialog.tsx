"use client";

interface ConfirmationDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
  isLoading?: boolean;
  confirmText?: string;
  cancelText?: string;
}

export default function ConfirmationDialog({
  isOpen,
  title,
  message,
  onConfirm,
  onCancel,
  isLoading = false,
  confirmText = "Confirm",
  cancelText = "Cancel",
}: ConfirmationDialogProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full mx-4">
        <h2 className="text-lg font-semibold text-[#0B0B0B] mb-2">
          {title}
        </h2>

        <p className="text-sm text-[#5A5A5A] mb-6">
          {message}
        </p>

        <div className="flex gap-3 justify-end">
          <button
            onClick={onCancel}
            disabled={isLoading}
            className="
              px-4 py-2 rounded-lg text-sm
              border border-[#E5E5E5] text-[#2E2E2E]
              hover:bg-[#FFF8E1]
              disabled:opacity-50 disabled:cursor-not-allowed
            "
          >
            {cancelText}
          </button>

          <button
            onClick={onConfirm}
            disabled={isLoading}
            className="
              px-4 py-2 rounded-lg text-sm font-medium
              bg-[#0B0B0B] text-white
              hover:bg-[#454545c1]
              disabled:opacity-50 disabled:cursor-not-allowed
            "
          >
            {isLoading ? "Processing..." : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}
