
import {
  BrowserRouter,
  Routes,
  Route,
  NavLink,
  Navigate,
  useLocation,
  useNavigate
} from "react-router-dom";

import {
  LayoutDashboard,
  Wallet,
  ArrowLeftRight,
  History,
  LogOut,
  Menu,
  X,
  Landmark,
  ShieldCheck,
  ChevronRight
} from "lucide-react";

import { useEffect, useState } from "react";
import api from "./services/api";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard.jsx";
import Accounts from "./pages/Accounts.jsx";
import Transfers from "./pages/Transfers.jsx";
import Transactions from "./pages/Transactions.jsx";

const navigation = [
  { name: "Dashboard", path: "/", icon: LayoutDashboard },
  { name: "My Accounts", path: "/accounts", icon: Wallet },
  { name: "Transfers", path: "/transfers", icon: ArrowLeftRight },
  { name: "Transactions", path: "/transactions", icon: History }
];

// Protected Route
function ProtectedRoute({ children }) {
  const token = sessionStorage.getItem("accessToken");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

// Navigation Items
function NavItems({ mobile = false, onNavigate, onLogout }) {
  return (
      <nav className="space-y-2">
        {navigation.map((item) => {
          const Icon = item.icon;

          return (
              <NavLink
                  key={item.path}
                  to={item.path}
                  end={item.path === "/"}
                  onClick={onNavigate}
                  className={({ isActive }) =>
                      `group flex items-center justify-between px-4 py-3.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                          isActive
                              ? "bg-blue-600 text-white shadow-lg shadow-blue-950/30"
                              : "text-slate-400 hover:bg-slate-800 hover:text-white"
                      }`
                  }
              >
                        <span className="flex items-center gap-3">
                            <Icon size={19} />
                          {item.name}
                        </span>

                <ChevronRight
                    size={16}
                    className="opacity-0 group-hover:opacity-70 transition"
                />
              </NavLink>
          );
        })}

        {mobile && (
            <button
                type="button"
                onClick={onLogout}
                className="w-full flex items-center gap-3 px-4 py-3.5 rounded-xl text-sm font-medium text-red-400 hover:bg-red-500/10 transition"
            >
              <LogOut size={19} />
              Logout
            </button>
        )}
      </nav>
  );
}

// Application Layout
function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const [userFullName, setUserFullName] = useState(
      sessionStorage.getItem("fullName") || ""
  );

  const [userEmail, setUserEmail] = useState(
      sessionStorage.getItem("email") || ""
  );

  const [profileLoading, setProfileLoading] = useState(true);

  // Fetch authenticated user profile
  useEffect(() => {
    let isMounted = true;

    const fetchProfile = async () => {
      try {
        const response = await api.get("/api/auth/profile");

        if (!isMounted) return;

        const fullName = response.data.fullName;
        const email = response.data.email;

        if (fullName) {
          setUserFullName(fullName);
          sessionStorage.setItem("fullName", fullName);
        }

        if (email) {
          setUserEmail(email);
          sessionStorage.setItem("email", email);
        }
      } catch (error) {
        console.error("Unable to fetch user profile:", error);
      } finally {
        if (isMounted) {
          setProfileLoading(false);
        }
      }
    };

    fetchProfile();

    return () => {
      isMounted = false;
    };
  }, []);

  // Close mobile menu whenever route changes
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  // Prevent background scrolling while mobile menu is open
  useEffect(() => {
    document.body.style.overflow = mobileMenuOpen ? "hidden" : "";

    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileMenuOpen]);

  const displayName = userFullName || (profileLoading ? "Loading..." : "User");

  const userInitial =
      userFullName.trim().charAt(0).toUpperCase() || "U";

  const currentPage =
      navigation.find((item) => item.path === location.pathname)?.name ||
      "Dashboard";

  // Common logout handler for desktop and mobile
  const handleLogout = () => {
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("email");
    sessionStorage.removeItem("fullName");
    sessionStorage.removeItem("role");

    setMobileMenuOpen(false);

    navigate("/login", { replace: true });
  };

  return (
      <div className="min-h-screen bg-slate-100 flex">

        {/* Desktop Sidebar */}
        <aside className="hidden md:flex w-64 bg-slate-950 text-white flex-col fixed inset-y-0 left-0 z-40">

          {/* Brand */}
          <div className="px-6 py-7 border-b border-slate-800">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-xl bg-blue-600 flex items-center justify-center shadow-lg shadow-blue-900/30">
                <Landmark size={24} className="text-white" />
              </div>

              <div>
                <h1 className="text-2xl font-bold tracking-wide">
                  SmartFin
                </h1>

                <p className="text-xs text-slate-400 mt-0.5">
                  Banking Platform
                </p>
              </div>
            </div>
          </div>

          {/* Navigation */}
          <div className="px-4 py-7 flex-1">
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider px-4 mb-4">
              Main Menu
            </p>

            <NavItems />
          </div>

          {/* Security Status */}
          <div className="px-5 py-3 border-t border-slate-800">
            <div className="flex items-center gap-2 text-xs text-emerald-400">
              <ShieldCheck size={16} />
              Secure Session
            </div>
          </div>

          {/* User Profile */}
          <div className="p-4 border-t border-slate-800">
            <div className="flex items-center gap-3 px-3 py-3 mb-3">
              <div className="w-10 h-10 shrink-0 rounded-full bg-blue-600 flex items-center justify-center font-bold">
                {userInitial}
              </div>

              <div className="min-w-0">
                <p
                    className="text-sm font-semibold text-white truncate"
                    title={userFullName}
                >
                  {displayName}
                </p>

                <p
                    className="text-xs text-slate-400 truncate"
                    title={userEmail}
                >
                  {userEmail || "Account"}
                </p>
              </div>
            </div>

            <button
                type="button"
                onClick={handleLogout}
                className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition"
            >
              <LogOut size={19} />
              Logout
            </button>
          </div>
        </aside>

        {/* Main Area */}
        <div className="flex-1 min-w-0 md:ml-64">

          {/* Top Header */}
          <header className="sticky top-0 z-30 bg-white/95 backdrop-blur-md border-b border-slate-200 px-4 sm:px-5 md:px-8 py-4">
            <div className="flex items-center justify-between">

              <div className="flex items-center gap-3">

                {/* Mobile Menu Button */}
                <button
                    type="button"
                    onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    className="md:hidden p-2 rounded-lg hover:bg-slate-100 text-slate-700 transition"
                    aria-label="Toggle navigation"
                    aria-expanded={mobileMenuOpen}
                >
                  {mobileMenuOpen ? (
                      <X size={22} />
                  ) : (
                      <Menu size={22} />
                  )}
                </button>

                <div>
                  <h2 className="text-xl md:text-2xl font-bold text-slate-800">
                    {currentPage}
                  </h2>

                  <p className="text-xs md:text-sm text-slate-500 mt-1">
                    Welcome to your SmartFin banking dashboard
                  </p>
                </div>
              </div>

              {/* Header Right */}
              <div className="flex items-center gap-3 md:gap-5">

                <div className="hidden sm:flex items-center gap-2 text-xs font-medium text-emerald-600 bg-emerald-50 px-3 py-2 rounded-full">
                  <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  System Online
                </div>

                <div className="hidden sm:block max-w-48 text-right">
                  <p className="text-xs font-semibold text-slate-700 truncate">
                    {displayName}
                  </p>

                  <p className="text-xs text-slate-400 truncate">
                    {userEmail}
                  </p>
                </div>

                <div className="w-10 h-10 rounded-full bg-blue-50 text-blue-700 flex items-center justify-center border border-blue-100 font-semibold">
                  {userInitial}
                </div>
              </div>
            </div>
          </header>

          {/* Mobile Navigation Overlay */}
          {mobileMenuOpen && (
              <div
                  className="md:hidden fixed inset-0 z-50 bg-slate-950/50"
                  onClick={() => setMobileMenuOpen(false)}
              >
                <aside
                    className="w-[min(290px,85vw)] h-full bg-slate-950 text-white p-5 shadow-2xl animate-in slide-in-from-left duration-200"
                    onClick={(event) => event.stopPropagation()}
                >

                  <div className="flex items-center justify-between mb-8">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center">
                        <Landmark size={22} />
                      </div>

                      <h1 className="text-xl font-bold">
                        SmartFin
                      </h1>
                    </div>

                    <button
                        type="button"
                        onClick={() => setMobileMenuOpen(false)}
                        className="p-2 text-slate-300 hover:text-white"
                        aria-label="Close navigation"
                    >
                      <X size={23} />
                    </button>
                  </div>

                  {/* Mobile User Profile */}
                  <div className="flex items-center gap-3 px-3 py-4 mb-6 border-b border-slate-800">
                    <div className="w-10 h-10 shrink-0 rounded-full bg-blue-600 flex items-center justify-center font-bold">
                      {userInitial}
                    </div>

                    <div className="min-w-0">
                      <p className="text-sm font-semibold truncate">
                        {displayName}
                      </p>

                      <p className="text-xs text-slate-400 truncate">
                        {userEmail}
                      </p>
                    </div>
                  </div>

                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
                    Main Menu
                  </p>

                  <NavItems
                      mobile
                      onNavigate={() => setMobileMenuOpen(false)}
                      onLogout={handleLogout}
                  />
                </aside>
              </div>
          )}

          {/* Page Content */}
          <main className="p-4 md:p-8 max-w-[1600px] mx-auto">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/accounts" element={<Accounts />} />
              <Route path="/transfers" element={<Transfers />} />
              <Route path="/transactions" element={<Transactions />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>

          {/* Footer */}
          <footer className="px-5 md:px-8 py-5 border-t border-slate-200 bg-white">
            <div className="max-w-[1600px] mx-auto flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-slate-400">
              <p>
                © {new Date().getFullYear()} SmartFin. All rights reserved.
              </p>

              <p>
                Educational Banking Simulation Platform
              </p>
            </div>
          </footer>
        </div>
      </div>
  );
}

// Main App
function App() {
  return (
      <BrowserRouter>
        <Routes>

          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes */}
          <Route
              path="/*"
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
          />

        </Routes>
      </BrowserRouter>
  );
}

export default App;
