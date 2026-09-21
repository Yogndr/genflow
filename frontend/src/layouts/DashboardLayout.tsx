import { useState } from "react";
import {
  NavLink,
  Outlet,
  useNavigate,
} from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function DashboardLayout() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [sidebarOpen, setSidebarOpen] =
    useState(false);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const closeSidebar = () => {
    setSidebarOpen(false);
  };

  const navItemClass = ({
    isActive,
  }: {
    isActive: boolean;
  }) =>
    `block rounded-lg px-4 py-3 text-sm font-medium transition ${
      isActive
        ? "bg-indigo-600 text-white"
        : "text-gray-600 hover:bg-gray-100 hover:text-gray-900"
    }`;

  const navigation = (
    <>
      <NavLink
        to="/dashboard"
        onClick={closeSidebar}
        className={navItemClass}
      >
        Dashboard
      </NavLink>

      <NavLink
        to="/generate-content"
        onClick={closeSidebar}
        className={navItemClass}
      >
        Generate Content
      </NavLink>

      <NavLink
        to="/generate-image"
        onClick={closeSidebar}
        className={navItemClass}
      >
        Generate Image
      </NavLink>

      <NavLink
        to="/content-history"
        onClick={closeSidebar}
        className={navItemClass}
      >
        Content History
      </NavLink>

      <NavLink
        to="/image-history"
        onClick={closeSidebar}
        className={navItemClass}
      >
        Image History
      </NavLink>
    </>
  );

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Mobile header */}
      <header className="flex items-center justify-between border-b border-gray-200 bg-white px-4 py-4 md:hidden">
        <div>
          <h1 className="text-xl font-bold text-indigo-600">
            GenFlow
          </h1>

          <p className="text-xs text-gray-400">
            AI Creation Platform
          </p>
        </div>

        <button
          type="button"
          onClick={() => setSidebarOpen(true)}
          className="rounded-lg border border-gray-300 px-3 py-2 text-gray-700"
          aria-label="Open navigation"
        >
          ☰
        </button>
      </header>

      <div className="flex min-h-screen">

        {/* Desktop sidebar */}
        <aside className="hidden w-64 shrink-0 border-r border-gray-200 bg-white p-5 md:block">
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-indigo-600">
              GenFlow
            </h1>

            <p className="mt-1 text-xs text-gray-500">
              AI Creation Platform
            </p>
          </div>

          <nav className="space-y-2">
            {navigation}
          </nav>

          <div className="mt-8 border-t border-gray-200 pt-5">
            <button
              type="button"
              onClick={handleLogout}
              className="w-full rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-100"
            >
              Logout
            </button>
          </div>
        </aside>

        {/* Mobile overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-40 bg-black/40 md:hidden"
            onClick={closeSidebar}
          />
        )}

        {/* Mobile sidebar */}
        <aside
          className={`fixed inset-y-0 left-0 z-50 w-72 transform bg-white p-5 shadow-xl transition-transform duration-300 md:hidden ${
            sidebarOpen
              ? "translate-x-0"
              : "-translate-x-full"
          }`}
        >
          <div className="mb-8 flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-indigo-600">
                GenFlow
              </h1>

              <p className="mt-1 text-xs text-gray-500">
                AI Creation Platform
              </p>
            </div>

            <button
              type="button"
              onClick={closeSidebar}
              className="rounded-lg px-3 py-2 text-xl text-gray-500 hover:bg-gray-100"
              aria-label="Close navigation"
            >
              ×
            </button>
          </div>

          <nav className="space-y-2">
            {navigation}
          </nav>

          <div className="mt-8 border-t border-gray-200 pt-5">
            <button
              type="button"
              onClick={handleLogout}
              className="w-full rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100"
            >
              Logout
            </button>
          </div>
        </aside>

        {/* Page content */}
        <main className="min-w-0 flex-1">
          <div className="mx-auto max-w-7xl p-4 sm:p-6 lg:p-8">
            <Outlet />
          </div>
        </main>

      </div>
    </div>
  );
}

export default DashboardLayout;