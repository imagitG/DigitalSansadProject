"use client";

import { useEffect, useState } from "react";
import {
  getUsers,
  getRoles,
  updateUserRoles,
  removeUserRole,
} from "@/services/adminService";
import { getUserRoles } from "@/utils/api";
import { useRouter } from "next/navigation";
import { logout } from "@/services/authService";
import Navbar from "@/components/navbar";


type User = {
  id: string;
  name: string;
  email: string;
  mobile: string;
  roles: string[];
};

export default function AdminPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [allRoles, setAllRoles] = useState<string[]>([]);
  const [selectedRole, setSelectedRole] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    const roles = getUserRoles();
    if (!roles.includes("ADMIN")) {
      router.push("/home");
      return;
    }
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setError(null);
      const [usersData, rolesData] = await Promise.all([
        getUsers(),
        getRoles(),
      ]);
      setUsers(usersData);
      setAllRoles(rolesData);
    } catch (e: any) {
      setError(e.message || "Failed to load users and roles");
    } finally {
      setLoading(false);
    }
  };

  const assignRole = async (user: User) => {
    const role = selectedRole[user.id];
    if (!role) return;

    try {
      setError(null);
      const updatedRoles = [...new Set([...user.roles, role])];
      await updateUserRoles(user.id, updatedRoles);

      setUsers(prev =>
        prev.map(u =>
          u.id === user.id ? { ...u, roles: updatedRoles } : u
        )
      );
    } catch (err: any) {
      setError(err.message || "Failed to assign role");
    }
  };

  const handleRemoveRole = async (user: User, role: string) => {
    if (!window.confirm(`Remove "${role}" role from ${user.name}?`)) return;

    try {
      setError(null);
      await removeUserRole(user.id, role);

      setUsers(prev =>
        prev.map(u =>
          u.id === user.id
            ? { ...u, roles: u.roles.filter(r => r !== role) }
            : u
        )
      );
    } catch (err: any) {
      setError(err.message || "Failed to remove role");
    }
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  if (loading) {
    return <div className="p-6 text-[#5A5A5A]">Loading...</div>;
  }

  return (
    <main className="relative mx-auto bg-white">

      <Navbar />
      {/* Logout
      <div className="absolute top-4 right-4">
        <button
          onClick={handleLogout}
          className="
            px-4 py-2 rounded-lg text-sm font-semibold
            bg-[#0B0B0B] text-white
            hover:bg-[#1A1A1A]
          "
        >
          Logout
        </button>
      </div> */}

      <div className="max-w-7xl mx-auto p-20">
        {/* Heading */}
        <h1 className="heading-font text-2xl text-[#0B0B0B] mb-6">
          Admin – User Management
        </h1>

        {/* Error */}
        {error && (
          <div className="p-4 mb-6 bg-red-50 border border-red-300 text-red-700 rounded-lg flex justify-between items-center">
            <span>{error}</span>
            <button
              onClick={() => setError(null)}
              className="font-bold text-lg hover:text-red-900"
            >
              ×
            </button>
          </div>
        )}

        {/* Table */}
        <div className="overflow-hidden rounded-xl border border-[#E5E5E5]">
          <table className="w-full text-sm">
            <thead className="bg-[#F9F9F9] text-[#2E2E2E]">
              <tr>
                <th className="p-3 text-left">Name</th>
                <th className="p-3 text-left">Email</th>
                <th className="p-3 text-left">Roles</th>
                <th className="p-3 text-left">Assign Role</th>
              </tr>
            </thead>

            <tbody>
              {users.map(user => (
                <tr key={user.id} className="border-t hover:bg-[#FFF8E1]">
                  <td className="p-3 text-[#2E2E2E]">{user.name}</td>
                  <td className="p-3 text-[#2E2E2E]">{user.email}</td>

                  {/* Existing Roles */}
                  <td className="p-3 space-x-2">
                    {user.roles.map(r => (
                      <span
                        key={r}
                        className="
                        inline-flex items-center gap-2
                        px-2 py-1 rounded-full text-xs font-medium
                        bg-gray-100 text-[#2E2E2E]
                      "
                      >
                        {r}
                        <button
                          onClick={() => handleRemoveRole(user, r)}
                          className="text-red-600 hover:text-red-800 font-bold"
                          title={`Remove ${r} role`}
                        >
                          ×
                        </button>
                      </span>
                    ))}
                  </td>

                  {/* Assign New Role */}
                  <td className="p-3 flex gap-2">
                    <select
                      className="
                      border border-[#E5E5E5] rounded-lg
                      px-2 py-1 text-sm text-[#2E2E2E]
                      focus:border-[#D4A017]
                    "
                      value={selectedRole[user.id] || ""}
                      onChange={e =>
                        setSelectedRole(prev => ({
                          ...prev,
                          [user.id]: e.target.value,
                        }))
                      }
                    >
                      <option value="">Select role</option>
                      {allRoles
                        .filter(r => !user.roles.includes(r))
                        .map(role => (
                          <option key={role} value={role}>
                            {role}
                          </option>
                        ))}
                    </select>

                    <button
                      onClick={() => assignRole(user)}
                      className="
                      px-3 py-1 rounded-lg text-sm font-medium
                      bg-[#D4A017] text-[#0B0B0B]
                      hover:bg-[#E6B325]
                    "
                    >
                      Assign
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </main>
  );
}
