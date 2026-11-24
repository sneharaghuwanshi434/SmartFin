
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import {
    Wallet,
    User,
    Mail,
    LockKeyhole,
    Eye,
    EyeOff,
    ArrowRight,
    ShieldCheck,
    LoaderCircle,
    CheckCircle2
} from "lucide-react";

import api from "../services/api";

function Register() {
    const navigate = useNavigate();

    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRegister = async (e) => {
        e.preventDefault();

        if (loading || success) return;

        setError("");
        setSuccess("");

        const cleanName = fullName.trim();
        const cleanEmail = email.trim().toLowerCase();

        if (cleanName.length < 2) {
            setError("Please enter a valid full name.");
            return;
        }

        if (password.length < 8) {
            setError("Password must contain at least 8 characters.");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            await api.post("/api/auth/register", {
                fullName: cleanName,
                email: cleanEmail,
                password
            });

            setSuccess(
                "Account created successfully! Redirecting to login..."
            );

            setTimeout(() => {
                navigate("/login", { replace: true });
            }, 1500);

        } catch (error) {
            setError(
                error.response?.data?.error ||
                error.response?.data?.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    const inputClass =
        "w-full pl-12 pr-4 py-3.5 border border-slate-200 rounded-xl bg-slate-50 text-slate-800 placeholder:text-slate-400 outline-none focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition";

    const passwordInputClass =
        "w-full pl-12 pr-12 py-3.5 border border-slate-200 rounded-xl bg-slate-50 text-slate-800 placeholder:text-slate-400 outline-none focus:bg-white focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition";

    return (
        <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4 sm:p-6 relative overflow-hidden">

            {/* Background Decorations */}
            <div className="absolute -top-32 -right-32 w-96 h-96 bg-blue-200/50 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-indigo-200/50 rounded-full blur-3xl pointer-events-none" />

            <div className="relative w-full max-w-5xl">

                <div className="grid lg:grid-cols-2 bg-white rounded-3xl shadow-2xl shadow-slate-300/50 border border-slate-200/70 overflow-hidden">

                    {/* Left Branding Panel */}
                    <div className="hidden lg:flex flex-col justify-between bg-gradient-to-br from-slate-950 via-blue-950 to-indigo-950 text-white p-12 relative overflow-hidden">

                        <div className="absolute -right-24 -top-20 w-80 h-80 rounded-full border border-white/10" />
                        <div className="absolute -right-12 -top-8 w-56 h-56 rounded-full border border-white/10" />
                        <div className="absolute -left-24 -bottom-24 w-80 h-80 rounded-full bg-blue-500/10 blur-2xl" />

                        <div className="relative z-10">

                            <div className="flex items-center gap-3 mb-16">
                                <div className="w-12 h-12 rounded-xl bg-blue-600 flex items-center justify-center shadow-lg">
                                    <Wallet size={27} />
                                </div>

                                <div>
                                    <h1 className="text-2xl font-bold tracking-wide">
                                        SmartFin
                                    </h1>

                                    <p className="text-xs text-blue-200">
                                        Banking Platform
                                    </p>
                                </div>
                            </div>

                            <p className="text-blue-300 text-sm font-semibold uppercase tracking-[0.2em] mb-4">
                                Welcome to SmartFin
                            </p>

                            <h2 className="text-4xl font-bold leading-tight">
                                Your finances.
                                <br />
                                Your control.
                                <br />
                                Your future.
                            </h2>

                            <p className="text-slate-300 text-sm leading-7 mt-6 max-w-sm">
                                Create your account and explore a smarter way
                                to manage accounts, transfers and transaction
                                history.
                            </p>
                        </div>

                        <div className="relative z-10 space-y-5">

                            <div className="flex items-center gap-3">
                                <CheckCircle2 className="text-emerald-400" size={20} />
                                <span className="text-sm text-slate-200">
                                    Manage your banking accounts
                                </span>
                            </div>

                            <div className="flex items-center gap-3">
                                <CheckCircle2 className="text-emerald-400" size={20} />
                                <span className="text-sm text-slate-200">
                                    Track deposits and withdrawals
                                </span>
                            </div>

                            <div className="flex items-center gap-3">
                                <CheckCircle2 className="text-emerald-400" size={20} />
                                <span className="text-sm text-slate-200">
                                    View transaction analytics
                                </span>
                            </div>

                            <p className="text-xs text-slate-400 pt-6 border-t border-white/10">
                                Educational Banking Simulation Platform
                            </p>
                        </div>
                    </div>

                    {/* Registration Form */}
                    <div className="p-6 sm:p-10 lg:p-12">

                        {/* Mobile Logo */}
                        <div className="lg:hidden flex flex-col items-center text-center mb-8">

                            <div className="w-14 h-14 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-200 mb-3">
                                <Wallet size={28} className="text-white" />
                            </div>

                            <h1 className="text-2xl font-bold text-slate-900">
                                SmartFin
                            </h1>

                            <p className="text-xs text-slate-500 mt-1">
                                Banking Platform
                            </p>
                        </div>

                        {/* Heading */}
                        <div className="mb-8">

                            <p className="text-sm font-semibold text-blue-600 mb-2">
                                GET STARTED
                            </p>

                            <h2 className="text-3xl font-bold text-slate-900">
                                Create Account
                            </h2>

                            <p className="text-sm text-slate-500 mt-2">
                                Enter your details to create your SmartFin account.
                            </p>
                        </div>

                        <form onSubmit={handleRegister} className="space-y-5">

                            {/* Full Name */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-700 mb-2">
                                    Full Name
                                </label>

                                <div className="relative">
                                    <User
                                        size={19}
                                        className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                                    />

                                    <input
                                        type="text"
                                        value={fullName}
                                        onChange={(e) => setFullName(e.target.value)}
                                        placeholder="Enter your full name"
                                        autoComplete="name"
                                        maxLength={100}
                                        required
                                        className={inputClass}
                                    />
                                </div>
                            </div>

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
                                        className={inputClass}
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
                                        placeholder="Minimum 8 characters"
                                        autoComplete="new-password"
                                        minLength={8}
                                        required
                                        className={passwordInputClass}
                                    />

                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-700"
                                        aria-label={showPassword ? "Hide password" : "Show password"}
                                    >
                                        {showPassword
                                            ? <EyeOff size={19} />
                                            : <Eye size={19} />
                                        }
                                    </button>
                                </div>

                                <p className="text-xs text-slate-400 mt-2">
                                    Use at least 8 characters.
                                </p>
                            </div>

                            {/* Confirm Password */}
                            <div>
                                <label className="block text-sm font-semibold text-slate-700 mb-2">
                                    Confirm Password
                                </label>

                                <div className="relative">
                                    <LockKeyhole
                                        size={19}
                                        className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
                                    />

                                    <input
                                        type={showConfirmPassword ? "text" : "password"}
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        placeholder="Re-enter your password"
                                        autoComplete="new-password"
                                        required
                                        className={passwordInputClass}
                                    />

                                    <button
                                        type="button"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-700"
                                        aria-label={showConfirmPassword ? "Hide confirm password" : "Show confirm password"}
                                    >
                                        {showConfirmPassword
                                            ? <EyeOff size={19} />
                                            : <Eye size={19} />
                                        }
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

                            {/* Success */}
                            {success && (
                                <div
                                    role="status"
                                    className="bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl p-3"
                                >
                                    {success}
                                </div>
                            )}

                            {/* Submit */}
                            <button
                                type="submit"
                                disabled={loading || !!success}
                                className="w-full flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white font-semibold py-3.5 rounded-xl shadow-lg shadow-blue-200 transition-all"
                            >
                                {loading ? (
                                    <>
                                        <LoaderCircle size={19} className="animate-spin" />
                                        Creating Account...
                                    </>
                                ) : (
                                    <>
                                        Create Account
                                        <ArrowRight size={19} />
                                    </>
                                )}
                            </button>

                        </form>

                        {/* Security Footer */}
                        <div className="mt-8 pt-6 border-t border-slate-100">

                            <div className="flex items-center justify-center gap-2 text-xs text-slate-500">
                                <ShieldCheck size={16} className="text-emerald-600" />
                                Secure account registration
                            </div>

                            <p className="text-center text-xs text-slate-400 mt-3">
                                SmartFin is an educational banking application.
                            </p>
                        </div>

                        {/* Login Link */}
                        <p className="text-center text-sm text-slate-500 mt-7">
                            Already have an account?{" "}
                            <Link
                                to="/login"
                                className="font-semibold text-blue-600 hover:text-blue-700"
                            >
                                Sign In
                            </Link>
                        </p>

                    </div>
                </div>

                <p className="text-center text-xs text-slate-400 mt-5">
                    © {new Date().getFullYear()} SmartFin
                </p>

            </div>
        </div>
    );
}

export default Register;
