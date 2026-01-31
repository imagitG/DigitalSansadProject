import { apiFetch, SOW_SERVICE_API } from "@/utils/api";

/* Upload PDF */
export async function createSow(formData: FormData) {
  return apiFetch("/sow", {
    method: "POST",
    body: formData,
  }, SOW_SERVICE_API);
}

/* Fetch tasks */
export async function fetchTasks(filters: any) {
  return apiFetch(
    "/sow/tasks/search",
    {
      method: "POST",
      body: JSON.stringify(filters),
    },
    SOW_SERVICE_API
  );
}

/* Download PDF */
export async function downloadSowPdf(id: string): Promise<Blob> {
  const token = typeof window !== "undefined" ? localStorage.getItem("auth_token") : null;

  const res = await fetch(`${SOW_SERVICE_API}/sow/${id}/file`, {
    headers: {
      ...(token && { Authorization: `Bearer ${token}` }),
    },
  });

  if (!res.ok) {
    const text = await res.text();
    const errorData = text ? JSON.parse(text) : {};
    throw new Error(errorData.message || "Failed to download PDF");
  }

  return res.blob();
}

/* Get SOW Metadata */
export async function getSowMetadata(id: string) {
  return apiFetch(`/sow/${id}/metadata`, {}, SOW_SERVICE_API);
}

/* Return SOW */
export async function returnSow(id: string, comment: string) {
  return apiFetch(
    `/sow/${id}/return`,
    {
      method: "POST",
      body: JSON.stringify({ comment }),
    },
    SOW_SERVICE_API
  );
}

/* Update File */
export async function updateSowFile(id: string, file: File) {
  const formData = new FormData();
  formData.append("file", file);

  const token = typeof window !== "undefined" ? localStorage.getItem("auth_token") : null;

  const res = await fetch(`${SOW_SERVICE_API}/sow/${id}/update-file`, {
    method: "POST",
    headers: {
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    const errorData = text ? JSON.parse(text) : {};
    throw new Error(errorData.message || "Failed to update file");
  }

  return res.json();
}

/* Submit SOW */
export async function submitSow(id: string) {
  return apiFetch(
    `/sow/${id}/submit`,
    {
      method: "POST",
      body: JSON.stringify({}),
    },
    SOW_SERVICE_API
  );
}


