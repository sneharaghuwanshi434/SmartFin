
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
    Wallet,
    Mail,
    LockKeyhole,
    Eye,
    EyeOff,
    ArrowRight,
    ShieldCheck,
    LoaderCircle
} from "lucide-react";
import api from "../services/api";

function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e) => {
        e.preventDefault();

        setError("");
        setLoading(true);

        try {
            const response = await api.post("/api/auth/login", {
                email: email.trim(),
                password,
            });

            const token = response.data.accessToken;

            if (!token) {
                throw new Error("Access token not received");
            }

            sessionStorage.setItem("accessToken", token);
            sessionStorage.setItem("email", response.data.email);
            sessionStorage.setItem("role", response.data.role);

            navigate("/", { replace: true });
        } catch (error) {
            setError(
                error.response?.data?.error ||
                error.response?.data?.message ||
                error.message ||
                "Login failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4 relative overflow-hidden">

            <div className="absolute -top-32 -right-32 w-96 h-96 bg-blue-200/50 rounded-full blur-3xl" />
            <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-indigo-200/50 rounded-full blur-3xl" />

            <div className="relative w-full max-w-md">

                <div className="bg-white rounded-3xl shadow-xl border border-slate-200/70 p-8 sm:p-10">

                    {/* Logo */}
                    <div className="flex flex-col items-center text-center mb-9">

                        <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-200 mb-5">
                            <Wallet size={32} className="text-white" />
                        </div>

                        <h1 className="text-3xl font-bold tracking-tight text-slate-900">
                            SmartFin
                        </h1>

                        <p className="text-sm text-slate-500 mt-2">
                            Your finances, simplified.
                        </p>

                    </div>

                    {/* Heading */}
                    <div className="mb-7">
                        <h2 className="text-2xl font-bold text-slate-800">
                            Welcome back
                        </h2>

                        <p className="text-sm text-slate-500 mt-2">
                            Sign in to access your banking dashboard.
                        </p>
                    </div>

                    {/* Login Form */}
                    <form onSubmit={handleLogin} className="space-y-5">

                        {/* Email */}
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">
                                Email Address
                            </label>

                            <div className="relative">
                                <Mail
                                    size={19}
                                    className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                                />

                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="name@example.com"
                                    autoComplete="email"
                                    required
                                    className="w-full pl-12 pr-4 py-3.5 border border-slate-200 rounded-xl bg-slate-50 focus:bg-white outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                                />
                            </div>
                        </div>

                        {/* Password */}
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">
                                Password
                            </label>

                            <div className="relative">
                                <LockKeyhole
                                    size={19}
                                    className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                                />

                                <input
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Enter your password"
                                    autoComplete="current-password"
                                    required
                                    className="w-full pl-12 pr-12 py-3.5 border border-slate-200 rounded-xl bg-slate-50 focus:bg-white outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
                                />

                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-700"
                                    aria-label={showPassword ? "Hide password" : "Show password"}
                                >
                                    {showPassword ? (
                                        <EyeOff size={19} />
                                    ) : (
                                        <Eye size={19} />
                                    )}
                                </button>
                            </div>
                        </div>

                        {/* Error */}
                        {error && (
                            <div
                                role="alert"
                                className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl p-3"
                            >
                                {error}
                            </div>
                        )}

                        {/* Submit */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3.5 rounded-xl shadow-md shadow-blue-200 transition-all"
                        >
                            {loading ? (
                                <>
                                    <LoaderCircle size={19} className="animate-spin" />
                                    Signing in...
                                </>
                            ) : (
                                <>
                                    Sign In
                                    <ArrowRight size={19} />
                                </>
                            )}
                        </button>

                    </form>

                    {/* Footer */}
                    <div className="mt-8 pt-6 border-t border-slate-100">

                        <div className="flex items-center justify-center gap-2 text-xs text-slate-500">
                            <ShieldCheck size={16} className="text-emerald-600" />
                            Secure authentication powered by JWT
                        </div>

                        <p className="text-center text-xs text-slate-400 mt-4">
                            SmartFin is an educational banking application.
                        </p>

                    </div>

                </div>

                <p className="text-center text-sm text-slate-500 mt-6">
                    Don't have an account?{" "}
                    <Link
                        to="/register"
                        className="font-semibold text-blue-600 hover:text-blue-700"
                    >
                        Create account
                    </Link>
                </p>

            </div>
        </div>
    );
}

export default Login;
