"use client";

import { useEffect, useState, useRef } from "react";
import { use } from "react";
import { useRouter } from "next/navigation";
import {
  getSowMetadata,
  downloadSowPdf,
  returnSow,
  updateSowFile,
  submitSow,
} from "@/services/sowService";
import { getUserRoles } from "@/utils/api";
import Navbar from "@/components/navbar";
import ReturnModal from "@/components/returnModal";
import ConfirmationDialog from "@/components/confirmationDialog";


type SowMetadata = {
  id: string;
  title: string;
  refNo: string;
  status: string;
  createdAt: string;
  approvedAt?: string;
  currentOwnerRole: string;
};

export default function ViewSowPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [metadata, setMetadata] = useState<SowMetadata | null>(null);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [isReturnModalOpen, setIsReturnModalOpen] = useState(false);
  const [isReturning, setIsReturning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploadingFile, setIsUploadingFile] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [showFileConfirmation, setShowFileConfirmation] = useState(false);

  useEffect(() => {
    let objectUrl: string;

    const load = async () => {
      const [meta, blob, userRoles] = await Promise.all([
        getSowMetadata(id),
        downloadSowPdf(id),
        getUserRoles(),
      ]);

      setMetadata(meta);
      setRoles(userRoles);

      objectUrl = URL.createObjectURL(blob);
      setPdfUrl(objectUrl);
    };

    load();

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [id]);

  if (!metadata || !pdfUrl) {
    return <p className="p-8 text-[#5A5A5A]">Loading SOW…</p>;
  }

  /* ───────── Role checks ───────── */
  //const canRecall = roles.includes("SOW_REVIEWER") || roles.includes("SOW_CREATOR");
  const canReturn =
    roles.includes("SOW_REVIEWER") || roles.includes("SOW_APPROVER");
  const canApprove = roles.includes("SOW_APPROVER");

  // Check if user can edit (user role matches current owner role)
  const userRole = roles.find(r => ["SOW_CREATOR", "SOW_REVIEWER", "SOW_APPROVER"].includes(r));
  const canEdit = userRole && metadata?.currentOwnerRole === userRole && !canApprove;

  const handleReturnSow = async (comment: string) => {
    try {
      setIsReturning(true);
      setError(null);
      await returnSow(id, comment);
      setIsReturnModalOpen(false);
      setTimeout(() => {
        router.push("/home");
      }, 2000);
    } catch (err: any) {
      setError(err.message || "Failed to return document");
      throw err;
    } finally {
      setIsReturning(false);
    }
  };

  const handleFileInputChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    setSelectedFile(e.target.files[0]);
    // Reset input
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleConfirmFileUpload = async () => {
    if (!selectedFile) return;

    try {
      setIsUploadingFile(true);
      setError(null);
      await updateSowFile(id, selectedFile);
      setSelectedFile(null);
      setShowFileConfirmation(false);
      // Reload metadata and PDF to show updated file
      const [meta, blob] = await Promise.all([
        getSowMetadata(id),
        downloadSowPdf(id),
      ]);
      setMetadata(meta);
      if (pdfUrl) URL.revokeObjectURL(pdfUrl);
      const newObjectUrl = URL.createObjectURL(blob);
      setPdfUrl(newObjectUrl);
    } catch (err: any) {
      setError(err.message || "Failed to upload file");
    } finally {
      setIsUploadingFile(false);
    }
  };

  const handleSubmitSow = async () => {
    try {
      setIsSubmitting(true);
      setError(null);
      await submitSow(id);
      setIsEditing(false);
      setShowSuccessMessage(true);
      // Redirect to home after 2 seconds
      // setTimeout(() => {
      //   router.push("/home");
      // }, 2000);
      router.push("/home");
    } catch (err: any) {
      setError(err.message || "Failed to submit document");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="bg-white">

      <Navbar />

      <div className="mx-auto max-w-7xl px-8 py-10 space-y-8">
        {error && (
          <div className="p-4 rounded-lg bg-red-50 border border-red-200 text-red-800 text-sm">
            {error}
          </div>
        )}

        {showSuccessMessage && (
          <div className="p-4 rounded-lg bg-green-50 border border-green-200 text-green-800 text-sm">
            Submitted successfully! Redirecting to home...
          </div>
        )}

        {/* ───────── Metadata ───────── */}
        <section className="border border-[#E5E5E5] rounded-lg p-4 grid grid-cols-2 gap-4 text-sm">
          <div className="text-[#2E2E2E]">
            <span className="font-medium">Title:</span> {metadata.title}
          </div>
          <div className="text-[#2E2E2E]">
            <span className="font-medium">Ref No:</span> {metadata.refNo}
          </div>
          <div className="text-[#2E2E2E]">
            <span className="font-medium">Status:</span>{" "}
            <span className="inline-flex px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
              {metadata.status}
            </span>
          </div>

          <div className="text-[#2E2E2E]">
            <span className="font-medium">Created At:</span>{" "}
            {new Date(metadata.createdAt).toLocaleString()}
          </div>

          <div className="text-[#2E2E2E]">
            <span className="font-medium">Approved At:</span>{" "}
            {metadata.approvedAt
              ? new Date(metadata.approvedAt).toLocaleString()
              : <span className="text-[#8A8A8A]">—</span>}
          </div>
        </section>

        {/* ───────── PDF Viewer ───────── */}
        <section className="border border-[#E5E5E5] rounded-lg h-[75vh] overflow-hidden">
          <iframe
            src={pdfUrl}
            className="w-full h-full"
            title="SOW PDF Viewer"
          />
        </section>

        {/* ───────── File Upload (Edit Mode) ───────── */}
        {isEditing && (
          <section className="border border-[#E5E5E5] rounded-lg p-6 bg-[#FFF8E1]">
            <h3 className="text-lg font-semibold text-[#0B0B0B] mb-4">
              Add new file
            </h3>
            <p className="text-sm text-[#5A5A5A] mb-4">
              Upload a new PDF file to replace the current one.
            </p>

            <div className="flex items-center gap-3">
              <input
                ref={fileInputRef}
                type="file"
                accept=".pdf"
                onChange={handleFileInputChange}
                disabled={isUploadingFile}
                style={{ display: 'none' }}
              />

              <button
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploadingFile || selectedFile !== null}
                className="
                  px-4 py-2 rounded-lg text-sm
                  border-2 border-[#D4A017] text-[#D4A017]
                  hover:bg-[#FFF8E1]
                  disabled:opacity-50 disabled:cursor-not-allowed
                  font-medium
                "
              >
                {isUploadingFile ? "Uploading..." : "Choose File"}
              </button>

              {selectedFile && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-[#2E2E2E]">
                    {selectedFile.name}
                  </span>
                  <button
                    onClick={() => {
                      setShowFileConfirmation(true);
                    }}
                    disabled={isUploadingFile}
                    className="
                      px-3 py-1 rounded text-xs font-medium
                      bg-[#D4A017] text-[#0B0B0B]
                      hover:bg-[#E6B325]
                      disabled:opacity-50
                    "
                  >
                    Upload
                  </button>
                </div>
              )}
            </div>
          </section>
        )}

        {/* ───────── Actions ───────── */}
        <section className="flex justify-between items-center">

          {/* Back */}
          <button
            onClick={() => {
              if (isEditing) {
                setIsEditing(false);
                setSelectedFile(null);
              } else {
                router.back();
              }
            }}
            className="
            px-4 py-2 rounded-lg text-sm
            border border-[#E5E5E5] text-[#2E2E2E]
            hover:bg-[#FFF8E1]
          "
          >
            {isEditing ? "Cancel Edit" : "Back"}
          </button>

          {/* Role Actions */}
          <div className="space-x-3">

            {canEdit && !isEditing && (
              <button
                onClick={() => setIsEditing(true)}
                className="
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#D4A017] text-[#0B0B0B]
                hover:bg-[#E6B325]
              "
              >
                Edit
              </button>
            )}

            {userRole !== "SOW_APPROVER" && (
              <button
                onClick={handleSubmitSow}
                disabled={isSubmitting}
                className="
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#166534] text-white
                hover:bg-[#14532D]
                disabled:opacity-50 disabled:cursor-not-allowed
              "
              >
                {isSubmitting ? "Submitting..." : "Submit"}
              </button>
            )}

            {/* {canRecall && !isEditing && (
              <button
                onClick={() => router.push(`/app/sow/recall?id=${id}`)}
                className="
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#D4A017] text-[#0B0B0B]
                hover:bg-[#E6B325]
              "
              >
                Recall
              </button>
            )} */}

            {canReturn && !isEditing && (
              <button
                onClick={() => setIsReturnModalOpen(true)}
                className="
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#0B0B0B] text-white
                hover:bg-[#454545c1]
              "
              >
                Return
              </button>
            )}

            {canApprove && !isEditing && (
              <button
                onClick={() => router.push(`/app/sow/approve?id=${id}`)}
                className="
                px-4 py-2 rounded-lg text-sm font-medium
                bg-[#166534] text-white
                hover:bg-[#14532D]
              "
              >
                Approve
              </button>
            )}
          </div>
        </section>
      </div>

      <ReturnModal
        isOpen={isReturnModalOpen}
        onClose={() => setIsReturnModalOpen(false)}
        onConfirm={handleReturnSow}
        isLoading={isReturning}
      />

      <ConfirmationDialog
        isOpen={showFileConfirmation}
        title="Replace File"
        message="You are about to replace the old file with a new one. Continue?"
        onConfirm={handleConfirmFileUpload}
        onCancel={() => {
          setShowFileConfirmation(false);
          setSelectedFile(null);
        }}
        isLoading={isUploadingFile}
        confirmText="Replace"
        cancelText="Cancel"
      />
    </main>
  );
}
