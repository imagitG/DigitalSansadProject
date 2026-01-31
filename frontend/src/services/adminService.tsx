import { apiFetch, AUTH_SERVICE_API } from "@/utils/api";

export const getUsers = () => apiFetch("/admin/users", undefined, AUTH_SERVICE_API);

export const getRoles = () => apiFetch("/admin/roles", undefined, AUTH_SERVICE_API);

export const updateUserRoles = (userId: string, roles: string[]) =>
  apiFetch(
    `/admin/users/${userId}/roles`,
    {
      method: "PUT",
      body: JSON.stringify(roles),
    },
    AUTH_SERVICE_API
  );

export const removeUserRole = (userId: string, role: string) =>
  apiFetch(`/admin/users/${userId}/roles/${role}`, {
    method: "DELETE",
  }, AUTH_SERVICE_API);
