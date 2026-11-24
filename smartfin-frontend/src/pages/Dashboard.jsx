
import { useEffect, useMemo, useState } from "react";
import api from "../services/api";

import {
    Wallet,
    CreditCard,
    TrendingUp,
    ArrowDownLeft,
    ArrowUpRight,
    ArrowLeftRight,
    Activity,
    RefreshCw,
    CalendarDays,
    Filter,
    X,
    Landmark
} from "lucide-react";

import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    PieChart,
    Pie,
    Cell,
    Legend,
    AreaChart,
    Area
} from "recharts";

const CHART_COLORS = [
    "#2563eb",
    "#16a34a",
    "#9333ea",
    "#f97316",
    "#0891b2"
];

const formatMoney = (amount) =>
    Number(amount || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

const formatDate = (date) => {
    if (!date) return "Date unavailable";

    return new Date(date).toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
};

const StatCard = ({ title, value, subtitle, icon: Icon, color }) => (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 md:p-6 shadow-sm hover:shadow-md transition-shadow">
        <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
                <p className="text-sm font-medium text-slate-500">
                    {title}
                </p>

                <h3 className="text-2xl font-bold text-slate-800 mt-3 break-words">
                    {value}
                </h3>

                <p className="text-xs text-slate-400 mt-2">
                    {subtitle}
                </p>
            </div>

            <div className={`p-3 rounded-xl shrink-0 ${color}`}>
                <Icon size={23} />
            </div>
        </div>
    </div>
);

function Dashboard() {

    // User profile
    const [userFullName, setUserFullName] = useState(
        sessionStorage.getItem("fullName") || "User"
    );

    const [userEmail, setUserEmail] = useState(
        sessionStorage.getItem("email") || ""
    );

    const currentHour = new Date().getHours();

    const greeting =
        currentHour < 12
            ? "Good morning"
            : currentHour < 17
                ? "Good afternoon"
                : "Good evening";

    // Dashboard states
    const [accounts, setAccounts] = useState([]);
    const [transactions, setTransactions] = useState([]);
    const [analytics, setAnalytics] = useState(null);

    const [loading, setLoading] = useState(true);
    const [transactionsLoading, setTransactionsLoading] = useState(true);

    const [error, setError] = useState("");
    const [transactionError, setTransactionError] = useState("");
    const [filterError, setFilterError] = useState("");

    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [appliedDates, setAppliedDates] = useState(null);

    // Fetch authenticated user profile
    useEffect(() => {
        let cancelled = false;

        const fetchUserProfile = async () => {
            try {
                const response = await api.get("/api/auth/profile");

                if (cancelled) return;

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
                if (!cancelled) {
                    console.error(
                        "Unable to fetch dashboard profile:",
                        error
                    );
                }
            }
        };

        fetchUserProfile();

        return () => {
            cancelled = true;
        };
    }, []);

    // Fetch bank accounts
    useEffect(() => {
        let cancelled = false;

        const fetchAccounts = async () => {
            try {
                const response = await api.get("/api/accounts");

                if (!cancelled) {
                    setAccounts(response.data);
                }

            } catch (err) {
                if (!cancelled) {
                    setError(
                        err.response?.data?.message ||
                        "Unable to load account details."
                    );
                }

            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        fetchAccounts();

        return () => {
            cancelled = true;
        };
    }, []);

    // Fetch complete analytics and recent transactions
    useEffect(() => {
        let cancelled = false;

        const fetchDashboardData = async () => {
            setTransactionsLoading(true);
            setTransactionError("");

            try {
                const params = new URLSearchParams();

                if (appliedDates) {
                    params.append("startDate", appliedDates.startDate);
                    params.append("endDate", appliedDates.endDate);
                }

                const analyticsUrl = params.toString()
                    ? `/api/transactions/analytics?${params.toString()}`
                    : "/api/transactions/analytics";

                const historyParams = new URLSearchParams({
                    page: "0",
                    size: "5"
                });

                let historyUrl =
                    `/api/transactions/history?${historyParams.toString()}`;

                if (appliedDates) {
                    const dateParams = new URLSearchParams({
                        startDate: appliedDates.startDate,
                        endDate: appliedDates.endDate,
                        page: "0",
                        size: "5"
                    });

                    historyUrl =
                        `/api/transactions/history/filter?${dateParams.toString()}`;
                }

                const [analyticsResponse, historyResponse] =
                    await Promise.all([
                        api.get(analyticsUrl),
                        api.get(historyUrl)
                    ]);

                if (cancelled) return;

                setAnalytics(analyticsResponse.data);
                setTransactions(historyResponse.data.content || []);

            } catch (err) {
                if (cancelled) return;

                setAnalytics(null);
                setTransactions([]);

                setTransactionError(
                    err.response?.data?.message ||
                    "Unable to load financial analytics."
                );

            } finally {
                if (!cancelled) {
                    setTransactionsLoading(false);
                }
            }
        };

        fetchDashboardData();

        return () => {
            cancelled = true;
        };
    }, [appliedDates]);

    // Account calculations
    const totalBalance = accounts.reduce(
        (total, account) =>
            total + Number(account.balance || 0),
        0
    );

    const activeAccounts = accounts.filter(
        account => account.status === "ACTIVE"
    ).length;

    // Apply date filter
    const handleApplyFilter = () => {
        setFilterError("");

        if (!startDate || !endDate) {
            setFilterError(
                "Please select both start date and end date."
            );
            return;
        }

        if (startDate > endDate) {
            setFilterError(
                "Start date cannot be after end date."
            );
            return;
        }

        setAppliedDates({
            startDate: `${startDate}T00:00:00+05:30`,
            endDate: `${endDate}T23:59:59+05:30`
        });
    };

    // Clear date filter
    const handleClearFilter = () => {
        setStartDate("");
        setEndDate("");
        setFilterError("");
        setAppliedDates(null);
    };

    // Transaction icons
    const getTransactionIcon = (type) => {
        switch (type?.toUpperCase()) {
            case "DEPOSIT":
                return <ArrowDownLeft size={20} />;
            case "WITHDRAWAL":
                return <ArrowUpRight size={20} />;
            case "TRANSFER":
                return <ArrowLeftRight size={20} />;
            default:
                return <Wallet size={20} />;
        }
    };

    const getTransactionStyle = (type) => {
        switch (type?.toUpperCase()) {
            case "DEPOSIT":
                return "bg-emerald-50 text-emerald-600";
            case "WITHDRAWAL":
                return "bg-red-50 text-red-600";
            case "TRANSFER":
                return "bg-blue-50 text-blue-600";
            default:
                return "bg-slate-100 text-slate-600";
        }
    };

    // Transaction comparison data
    const transactionTypeData = useMemo(() => {
        return (analytics?.transactionTypeBreakdown || []).map(item => ({
            name: item.transactionType,
            amount: Number(item.totalAmount || 0),
            count: Number(item.count || 0)
        }));
    }, [analytics]);

    // Daily activity data
    const activityData = useMemo(() => {
        return (analytics?.dailyActivity || []).map(item => ({
            date: new Date(`${item.date}T00:00:00`).toLocaleDateString(
                "en-IN",
                {
                    day: "2-digit",
                    month: "short"
                }
            ),
            count: Number(item.transactionCount || 0),
            amount: Number(item.totalAmount || 0)
        }));
    }, [analytics]);

    // Account balance distribution
    const accountBalanceData = useMemo(() => {
        return accounts
            .filter(account => Number(account.balance) > 0)
            .map((account, index) => ({
                name: `${account.accountType} (${String(account.accountNumber).slice(-4)})`,
                value: Number(account.balance),
                color: CHART_COLORS[index % CHART_COLORS.length]
            }));
    }, [accounts]);

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center min-h-80 gap-3 text-slate-500">
                <RefreshCw size={25} className="animate-spin text-blue-600" />
                <p className="font-medium">Loading your dashboard...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-red-700">
                {error}
            </div>
        );
    }

    return (
        <div className="space-y-8">

            {/* Welcome Section */}
            <section className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">

                <div>
                    <p className="text-sm font-semibold text-blue-600 uppercase tracking-wider mb-2">
                        SmartFin / Overview
                    </p>

                    <h1 className="text-2xl md:text-3xl font-bold text-slate-900">
                        {greeting}, {userFullName}!
                    </h1>

                    <p className="text-slate-500 mt-2">
                        Welcome back to SmartFin. Here's your financial overview.
                    </p>

                    <p className="text-sm text-slate-400 mt-1">
                        {userEmail}
                    </p>
                </div>

                <div className="flex items-center gap-2 text-sm font-medium text-emerald-700 bg-emerald-50 border border-emerald-100 rounded-full px-4 py-2 w-fit">
                    <span className="w-2 h-2 bg-emerald-500 rounded-full"></span>
                    Your account is active
                </div>

            </section>

            {/* Main Account Summary */}
            <section className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-5">

                <div className="relative overflow-hidden bg-gradient-to-br from-blue-700 via-blue-800 to-indigo-950 rounded-2xl p-6 md:p-7 text-white shadow-lg">
                    <div className="absolute -right-10 -top-10 w-44 h-44 rounded-full bg-white/5"></div>
                    <div className="absolute right-12 -bottom-20 w-52 h-52 rounded-full bg-white/5"></div>

                    <div className="relative">
                        <div className="flex items-center justify-between">
                            <p className="text-blue-100 text-sm font-medium">
                                Total Balance
                            </p>

                            <div className="bg-white/15 p-3 rounded-xl">
                                <Wallet size={24} />
                            </div>
                        </div>

                        <h2 className="text-3xl md:text-4xl font-bold mt-5 break-words">
                            ₹{formatMoney(totalBalance)}
                        </h2>

                        <p className="text-blue-100 text-sm mt-5">
                            Combined balance across all your accounts
                        </p>
                    </div>
                </div>

                <StatCard
                    title="Total Accounts"
                    value={accounts.length}
                    subtitle="Your registered bank accounts"
                    icon={CreditCard}
                    color="bg-blue-50 text-blue-600"
                />

                <StatCard
                    title="Active Accounts"
                    value={activeAccounts}
                    subtitle="Currently active accounts"
                    icon={TrendingUp}
                    color="bg-emerald-50 text-emerald-600"
                />

            </section>

            {/* Transaction Summary */}
            <section>
                <div className="mb-4">
                    <h2 className="text-lg font-bold text-slate-800">
                        Transaction Summary
                    </h2>
                    <p className="text-sm text-slate-500 mt-1">
                        Complete transaction totals for the selected period
                    </p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">

                    <StatCard
                        title="Total Transactions"
                        value={analytics?.totalTransactions ?? 0}
                        subtitle="All transaction types"
                        icon={Activity}
                        color="bg-purple-50 text-purple-600"
                    />

                    <StatCard
                        title="Total Deposits"
                        value={`₹${formatMoney(analytics?.totalDepositAmount)}`}
                        subtitle={`${analytics?.totalDeposits ?? 0} transactions`}
                        icon={ArrowDownLeft}
                        color="bg-emerald-50 text-emerald-600"
                    />

                    <StatCard
                        title="Total Withdrawals"
                        value={`₹${formatMoney(analytics?.totalWithdrawalAmount)}`}
                        subtitle={`${analytics?.totalWithdrawals ?? 0} transactions`}
                        icon={ArrowUpRight}
                        color="bg-red-50 text-red-600"
                    />

                    <StatCard
                        title="Total Transfers"
                        value={`₹${formatMoney(analytics?.totalTransferAmount)}`}
                        subtitle={`${analytics?.totalTransfers ?? 0} transactions`}
                        icon={ArrowLeftRight}
                        color="bg-blue-50 text-blue-600"
                    />

                </div>
            </section>

            {/* Date Filter */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-7">

                <div className="flex items-start gap-3 mb-6">
                    <div className="bg-blue-50 text-blue-600 p-3 rounded-xl">
                        <CalendarDays size={22} />
                    </div>

                    <div>
                        <h2 className="text-lg font-bold text-slate-800">
                            Financial Analytics Filter
                        </h2>

                        <p className="text-sm text-slate-500 mt-1">
                            Choose a date range to filter your transaction data.
                        </p>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2">
                            Start Date
                        </label>

                        <input
                            type="date"
                            value={startDate}
                            max={endDate || undefined}
                            onChange={(e) => setStartDate(e.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-3 bg-white text-slate-700 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 outline-none transition"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-2">
                            End Date
                        </label>

                        <input
                            type="date"
                            value={endDate}
                            min={startDate || undefined}
                            onChange={(e) => setEndDate(e.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-3 bg-white text-slate-700 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 outline-none transition"
                        />
                    </div>

                </div>

                {filterError && (
                    <p className="text-sm text-red-600 mt-3">
                        {filterError}
                    </p>
                )}

                <div className="flex flex-wrap gap-3 mt-5">

                    <button
                        onClick={handleApplyFilter}
                        disabled={transactionsLoading}
                        className="flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-5 py-3 rounded-xl font-semibold transition disabled:opacity-50"
                    >
                        <Filter size={17} />
                        Apply Filter
                    </button>

                    <button
                        onClick={handleClearFilter}
                        disabled={transactionsLoading && !appliedDates}
                        className="flex items-center justify-center gap-2 border border-slate-300 hover:bg-slate-50 text-slate-700 px-5 py-3 rounded-xl font-semibold transition disabled:opacity-50"
                    >
                        <X size={17} />
                        Clear Filter
                    </button>

                </div>

                {appliedDates && (
                    <p className="text-sm text-blue-600 mt-4">
                        Showing analytics for your selected date range.
                    </p>
                )}

            </section>

            {/* Analytics Charts */}
            <section>
                <div className="mb-5">
                    <h2 className="text-xl font-bold text-slate-800">
                        Financial Analytics
                    </h2>

                    <p className="text-sm text-slate-500 mt-1">
                        Visual insights into your transaction activity
                    </p>
                </div>

                {transactionsLoading ? (
                    <div className="bg-white rounded-2xl border border-slate-200 p-10 text-center text-slate-500">
                        <RefreshCw size={22} className="animate-spin mx-auto mb-3 text-blue-600" />
                        Loading analytics...
                    </div>
                ) : transactionError ? (
                    <div className="bg-red-50 border border-red-200 rounded-2xl p-5 text-red-700">
                        {transactionError}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">

                        {/* Transaction Comparison */}
                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-6 min-w-0">
                            <h3 className="font-bold text-slate-800">
                                Transaction Comparison
                            </h3>

                            <p className="text-sm text-slate-500 mt-1 mb-6">
                                Transaction amounts by type
                            </p>

                            {transactionTypeData.length === 0 ? (
                                <div className="h-64 flex items-center justify-center text-slate-400 text-sm">
                                    No transaction data available.
                                </div>
                            ) : (
                                <div className="h-72">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <BarChart
                                            data={transactionTypeData}
                                            margin={{ top: 10, right: 10, left: 5, bottom: 5 }}
                                        >
                                            <CartesianGrid
                                                strokeDasharray="3 3"
                                                vertical={false}
                                                stroke="#e2e8f0"
                                            />

                                            <XAxis
                                                dataKey="name"
                                                tick={{ fill: "#64748b", fontSize: 12 }}
                                                axisLine={false}
                                                tickLine={false}
                                            />

                                            <YAxis
                                                tickFormatter={(value) =>
                                                    `₹${value.toLocaleString("en-IN")}`
                                                }
                                                tick={{ fill: "#64748b", fontSize: 11 }}
                                                axisLine={false}
                                                tickLine={false}
                                                width={65}
                                            />

                                            <Tooltip
                                                contentStyle={{
                                                    borderRadius: "12px",
                                                    border: "1px solid #e2e8f0"
                                                }}
                                                formatter={(value) => [
                                                    `₹${formatMoney(value)}`,
                                                    "Amount"
                                                ]}
                                            />

                                            <Bar
                                                dataKey="amount"
                                                name="Amount"
                                                fill="#2563eb"
                                                radius={[7, 7, 0, 0]}
                                                maxBarSize={65}
                                            />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            )}
                        </div>

                        {/* Daily Activity */}
                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-6 min-w-0">
                            <h3 className="font-bold text-slate-800">
                                Transaction Activity
                            </h3>

                            <p className="text-sm text-slate-500 mt-1 mb-6">
                                Daily transaction count
                            </p>

                            {activityData.length === 0 ? (
                                <div className="h-64 flex items-center justify-center text-slate-400 text-sm">
                                    No activity data available.
                                </div>
                            ) : (
                                <div className="h-72">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <AreaChart
                                            data={activityData}
                                            margin={{ top: 10, right: 10, left: 0, bottom: 5 }}
                                        >
                                            <defs>
                                                <linearGradient
                                                    id="activityGradient"
                                                    x1="0"
                                                    y1="0"
                                                    x2="0"
                                                    y2="1"
                                                >
                                                    <stop
                                                        offset="0%"
                                                        stopColor="#8b5cf6"
                                                        stopOpacity={0.3}
                                                    />
                                                    <stop
                                                        offset="95%"
                                                        stopColor="#8b5cf6"
                                                        stopOpacity={0.02}
                                                    />
                                                </linearGradient>
                                            </defs>

                                            <CartesianGrid
                                                strokeDasharray="3 3"
                                                vertical={false}
                                                stroke="#e2e8f0"
                                            />

                                            <XAxis
                                                dataKey="date"
                                                tick={{ fill: "#64748b", fontSize: 12 }}
                                                axisLine={false}
                                                tickLine={false}
                                            />

                                            <YAxis
                                                allowDecimals={false}
                                                tick={{ fill: "#64748b", fontSize: 12 }}
                                                axisLine={false}
                                                tickLine={false}
                                            />

                                            <Tooltip
                                                contentStyle={{
                                                    borderRadius: "12px",
                                                    border: "1px solid #e2e8f0"
                                                }}
                                                formatter={(value) => [
                                                    value,
                                                    "Transactions"
                                                ]}
                                            />

                                            <Area
                                                type="monotone"
                                                dataKey="count"
                                                name="Transactions"
                                                stroke="#7c3aed"
                                                fill="url(#activityGradient)"
                                                strokeWidth={3}
                                                activeDot={{ r: 6 }}
                                            />
                                        </AreaChart>
                                    </ResponsiveContainer>
                                </div>
                            )}
                        </div>

                    </div>
                )}
            </section>

            {/* Account Balance Distribution */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-6">

                <div className="mb-5">
                    <h2 className="text-lg font-bold text-slate-800">
                        Account Balance Distribution
                    </h2>

                    <p className="text-sm text-slate-500 mt-1">
                        How your balance is distributed across accounts
                    </p>
                </div>

                {accountBalanceData.length === 0 ? (
                    <div className="h-52 flex items-center justify-center text-slate-400 text-sm">
                        No account balance data available.
                    </div>
                ) : (
                    <div className="h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={accountBalanceData}
                                    dataKey="value"
                                    nameKey="name"
                                    cx="50%"
                                    cy="50%"
                                    outerRadius={105}
                                    innerRadius={55}
                                    paddingAngle={3}
                                    label={({ percent }) =>
                                        `${(percent * 100).toFixed(1)}%`
                                    }
                                >
                                    {accountBalanceData.map((entry, index) => (
                                        <Cell
                                            key={`cell-${index}`}
                                            fill={entry.color}
                                        />
                                    ))}
                                </Pie>

                                <Tooltip
                                    formatter={(value) =>
                                        `₹${formatMoney(value)}`
                                    }
                                />

                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                )}

            </section>

            {/* My Bank Accounts */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-6">

                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="text-lg font-bold text-slate-800">
                            My Bank Accounts
                        </h2>

                        <p className="text-sm text-slate-500 mt-1">
                            Overview of your registered accounts
                        </p>
                    </div>

                    <div className="bg-blue-50 text-blue-600 p-3 rounded-xl">
                        <Landmark size={22} />
                    </div>
                </div>

                {accounts.length === 0 ? (
                    <div className="text-center py-10">
                        <Wallet
                            size={35}
                            className="mx-auto text-slate-300 mb-3"
                        />

                        <p className="text-slate-500">
                            No bank accounts found.
                        </p>

                        <p className="text-sm text-slate-400 mt-1">
                            Create your first account to get started.
                        </p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {accounts.map((account) => (
                            <div
                                key={account.accountNumber}
                                className="border border-slate-200 rounded-xl p-5 hover:border-blue-300 hover:shadow-md transition-all"
                            >
                                <div className="flex items-center justify-between gap-3">
                                    <div className="flex items-center gap-3 min-w-0">
                                        <div className="bg-blue-50 text-blue-600 p-3 rounded-xl shrink-0">
                                            <Wallet size={21} />
                                        </div>

                                        <div className="min-w-0">
                                            <h3 className="font-semibold text-slate-800">
                                                {account.accountType}
                                            </h3>

                                            <p className="text-xs text-slate-500 mt-1 break-all">
                                                {account.accountNumber}
                                            </p>
                                        </div>
                                    </div>

                                    <span className={`text-xs font-semibold px-3 py-1 rounded-full shrink-0 ${
                                        account.status === "ACTIVE"
                                            ? "bg-emerald-50 text-emerald-700"
                                            : "bg-slate-100 text-slate-600"
                                    }`}>
                                        {account.status}
                                    </span>
                                </div>

                                <div className="border-t border-slate-100 mt-5 pt-4">
                                    <p className="text-xs text-slate-500">
                                        Available Balance
                                    </p>

                                    <h3 className="text-xl font-bold text-slate-800 mt-1">
                                        {account.currency} {formatMoney(account.balance)}
                                    </h3>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

            </section>

            {/* Recent Transactions */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm p-5 md:p-6">

                <div className="flex items-center justify-between mb-6">
                    <div>
                        <h2 className="text-lg font-bold text-slate-800">
                            Recent Transactions
                        </h2>

                        <p className="text-sm text-slate-500 mt-1">
                            {appliedDates
                                ? "Latest transactions in selected date range"
                                : "Your latest financial activities"}
                        </p>
                    </div>

                    <div className="bg-purple-50 text-purple-600 p-3 rounded-xl">
                        <Activity size={22} />
                    </div>
                </div>

                {transactionsLoading ? (
                    <div className="py-8 text-center text-slate-500">
                        <RefreshCw size={20} className="animate-spin mx-auto mb-2" />
                        Loading transactions...
                    </div>
                ) : transactionError ? (
                    <p className="text-red-600 bg-red-50 rounded-xl p-4">
                        {transactionError}
                    </p>
                ) : transactions.length === 0 ? (
                    <div className="text-center py-10">
                        <Activity
                            size={35}
                            className="mx-auto text-slate-300 mb-3"
                        />

                        <p className="text-slate-500">
                            No transactions found.
                        </p>

                        <p className="text-sm text-slate-400 mt-1">
                            Try selecting another date range.
                        </p>
                    </div>
                ) : (
                    <div className="divide-y divide-slate-100">
                        {transactions.slice(0, 5).map((transaction) => (
                            <div
                                key={transaction.referenceNumber}
                                className="flex items-start gap-3 md:gap-4 py-4 first:pt-0 last:pb-0"
                            >
                                <div className={`p-3 rounded-xl shrink-0 ${getTransactionStyle(transaction.transactionType)}`}>
                                    {getTransactionIcon(transaction.transactionType)}
                                </div>

                                <div className="flex-1 min-w-0">
                                    <div className="flex flex-wrap items-center gap-2">
                                        <h3 className="font-semibold text-slate-800">
                                            {transaction.transactionType}
                                        </h3>

                                        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
                                            transaction.status === "SUCCESS"
                                                ? "bg-emerald-50 text-emerald-700"
                                                : "bg-slate-100 text-slate-600"
                                        }`}>
                                            {transaction.status}
                                        </span>
                                    </div>

                                    <p className="text-lg font-bold text-slate-800 mt-1">
                                        ₹{formatMoney(transaction.amount)}
                                    </p>

                                    <p className="text-sm text-slate-500 mt-1 break-words">
                                        {transaction.description || "No description"}
                                    </p>

                                    <p className="text-xs text-slate-400 mt-2 break-all">
                                        Ref: {transaction.referenceNumber}
                                    </p>

                                    <p className="text-xs text-slate-400 mt-1">
                                        {formatDate(transaction.createdAt)}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

            </section>

        </div>
    );
}

export default Dashboard;
