"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { getUserRoles } from "@/utils/api";
import { fetchTasks } from "@/services/sowService";
import Navbar from "@/components/navbar";

/* ───────────── Types ───────────── */

type SowTask = {
  id: string;
  refNo: string;
  documentName: string;
  status: string;
  pendingAt: string;
};

/* ───────────── Tiles ───────────── */

function Tile({
  title,
  onClick,
}: {
  title: string;
  onClick: () => void;
}) {
  return (
    <div
      onClick={onClick}
      className="
        p-6 bg-white border border-[#E5E5E5] rounded-xl
        text-lg font-semibold text-[#0B0B0B]
        cursor-pointer transition
        hover:border-[#D4A017] hover:shadow-md
      "
    >
      {title}
    </div>
  );
}

/* ───────────── Filters ───────────── */

function TaskFilters({
  onApply,
}: {
  onApply: (filters: any) => void;
}) {
  const [filters, setFilters] = useState({
    refNo: "",
    status: "",
    pendingAt: "",
    createdBy: "",
    createdOn: "",
  });

  const pendingAtOptions = [
    "SOW_CREATOR",
    "SOW_REVIEWER",
    "SOW_APPROVER",
  ];

  const handleReset = () => {
    const resetFilters = {
      refNo: "",
      status: "",
      pendingAt: "",
      createdBy: "",
      createdOn: "",
    };
    setFilters(resetFilters);
    onApply({});
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-[#E5E5E5] shadow-sm mb-6">
      <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-4">

        {/* Ref No */}
        <div>
          <label className="block text-sm font-medium text-[#2E2E2E] mb-1">
            Ref No
          </label>
          <input
            placeholder="Search by Ref No"
            className="
              w-full border border-[#E5E5E5] rounded-lg
              px-3 py-2 text-sm text-[#2E2E2E]
              focus:border-[#D4A017] focus:ring-2
              focus:ring-[rgba(212,160,23,0.4)]
            "
            value={filters.refNo}
            onChange={(e) =>
              setFilters({ ...filters, refNo: e.target.value })
            }
          />
        </div>

        {/* Status */}
        <div>
          <label className="block text-sm font-medium text-[#2E2E2E] mb-1">
            Status
          </label>
          <select
            className="
              w-full border border-[#E5E5E5] rounded-lg
              px-3 py-2 text-sm text-[#2E2E2E]
              focus:border-[#D4A017]
            "
            value={filters.status}
            onChange={(e) =>
              setFilters({ ...filters, status: e.target.value })
            }
          >
            <option value="">All Status</option>
            <option value="DRAFT">Draft</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RETURNED">Returned</option>
            <option value="APPROVED">Approved</option>
          </select>
        </div>

        {/* Pending At */}
        <div>
          <label className="block text-sm font-medium text-[#2E2E2E] mb-1">
            Pending At
          </label>
          <select
            className="
              w-full border border-[#E5E5E5] rounded-lg
              px-3 py-2 text-sm text-[#2E2E2E]
            "
            value={filters.pendingAt}
            onChange={(e) =>
              setFilters({ ...filters, pendingAt: e.target.value })
            }
          >
            <option value="">All Roles</option>
            {pendingAtOptions.map((role) => (
              <option key={role} value={role}>
                {role.replace(/_/g, " ")}
              </option>
            ))}
          </select>
        </div>

        {/* Created By */}
        <div>
          <label className="block text-sm font-medium text-[#2E2E2E] mb-1">
            Created By (User ID)
          </label>
          <input
            placeholder="User ID"
            className="
              w-full border border-[#E5E5E5] rounded-lg
              px-3 py-2 text-sm text-[#2E2E2E]
            "
            value={filters.createdBy}
            onChange={(e) =>
              setFilters({ ...filters, createdBy: e.target.value })
            }
          />
        </div>

        {/* Created On */}
        <div>
          <label className="block text-sm font-medium text-[#2E2E2E] mb-1">
            Created On
          </label>
          <input
            type="date"
            className="
              w-full border border-[#E5E5E5] rounded-lg
              px-3 py-2 text-sm text-[#2E2E2E]
            "
            value={filters.createdOn}
            onChange={(e) =>
              setFilters({ ...filters, createdOn: e.target.value })
            }
          />
        </div>
      </div>

      {/* Buttons */}
      <div className="flex gap-3">
        <button
          className="
            px-4 py-2 rounded-lg text-sm font-medium
            bg-[#D4A017] text-[#0B0B0B]
            hover:bg-[#E6B325] active:bg-[#B88A0A]
          "
          onClick={() => onApply(filters)}
        >
          Apply
        </button>

        <button
          className="
            px-4 py-2 rounded-lg text-sm
            border border-[#E5E5E5] text-[#2E2E2E]
            hover:bg-[#FFF8E1]
          "
          onClick={handleReset}
        >
          Reset
        </button>
      </div>
    </div>
  );
}

/* ───────────── Task Table ───────────── */

function TaskTable({
  tasks,
  onView,
}: {
  tasks: SowTask[];
  onView: (id: string) => void;
}) {
  return (
    <div className="bg-white rounded-xl border border-[#E5E5E5] shadow-sm overflow-hidden">
      <table className="w-full text-sm">
        <thead className="bg-[#e7f3f8b9] text-[#2E2E2E]">
          <tr>
            <th className="p-4 text-left">Ref No</th>
            <th className="p-4 text-left">Document</th>
            <th className="p-4 text-left">Status</th>
            <th className="p-4 text-left">Pending At</th>
            <th className="p-4 text-left">Action</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map((t) => (
            <tr key={t.id} className="border-t hover:bg-[#FFF8E1]">
              <td className="p-4">{t.refNo}</td>
              <td className="p-4">{t.documentName}</td>
              <td className="p-4">
                <span className="px-2 py-1 rounded-full bg-blue-100 text-blue-800 text-xs font-medium">
                  {t.status}
                </span>
              </td>
              <td className="p-4">{t.pendingAt}</td>
              <td className="p-4">
                <button
                  className="
                    px-3 py-1 rounded text-sm
                    bg-[#0B0B0B] text-white
                    hover:bg-[#1A1A1A]
                  "
                  onClick={() => onView(t.id)}
                >
                  View
                </button>
              </td>
            </tr>
          ))}

          {tasks.length === 0 && (
            <tr>
              <td colSpan={5} className="p-6 text-center text-[#8A8A8A]">
                No documents found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

/* ───────────── Page ───────────── */

export default function HomePage() {
  const router = useRouter();
  const [roles, setRoles] = useState<string[]>([]);
  const [tasks, setTasks] = useState<SowTask[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const canCreateSow = roles.includes("SOW_CREATOR");

  const isAdmin = roles.includes("ADMIN");

  const canSeeTasks =
    roles.includes("SOW_CREATOR") ||
    roles.includes("SOW_REVIEWER") ||
    roles.includes("SOW_APPROVER");

  useEffect(() => {
    const r = getUserRoles();
    setRoles(r);
    if (canSeeTasks) loadTasks({});
  }, []);

  const loadTasks = async (filters: any) => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchTasks(filters);
      setTasks(data);
    } catch (err: any) {
      setError(err.message || "Failed to load tasks");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      <Navbar />

      <main className="max-w-7xl mx-auto p-8 space-y-10">
        <h1 className="heading-font text-3xl text-[#0B0B0B]">
          Dashboard
        </h1>

        {error && (
          <div className="p-4 bg-red-100 border border-red-300 text-red-700 rounded-lg">
            {error}
          </div>
        )}

        {/* Tiles */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
          {canCreateSow && (
            <Tile
              title="Create Statement of Work"
              onClick={() => router.push("/sow/create")}
            />
          )}
          {isAdmin && (
            <Tile
              title="User Management"
              onClick={() => router.push("/admin/user-management")}
            />
          )}
        </div>

        {/* Tasks */}
        {canSeeTasks && (
          <section className="space-y-4">
            <h2 className="heading-font text-2xl text-[#1A1A1A]">
              My Tasks
            </h2>

            <TaskFilters onApply={loadTasks} />

            {loading ? (
              <div className="text-[#5A5A5A]">Loading tasks...</div>
            ) : (
              <TaskTable
                tasks={tasks}
                onView={(id) => router.push(`/sow/${id}`)}
              />
            )}
          </section>
        )}
      </main>
    </div>
  );
}
