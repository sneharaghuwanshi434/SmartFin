
import { useEffect, useMemo, useRef, useState } from "react";
import {
    Wallet,
    Plus,
    RefreshCw,
    ArrowDownCircle,
    ArrowUpCircle,
    IndianRupee,
    CreditCard,
    CheckCircle2,
    AlertCircle,
    Building2
} from "lucide-react";
import api from "../services/api";

function Accounts() {
    const [accounts, setAccounts] = useState([]);
    const [accountType, setAccountType] = useState("SAVINGS");

    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [creating, setCreating] = useState(false);
    const [depositing, setDepositing] = useState(false);
    const [withdrawing, setWithdrawing] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [selectedAccountNumber, setSelectedAccountNumber] = useState("");
    const [depositAmount, setDepositAmount] = useState("");
    const [depositError, setDepositError] = useState("");
    const [depositSuccess, setDepositSuccess] = useState("");

    const [withdrawAmount, setWithdrawAmount] = useState("");
    const [withdrawError, setWithdrawError] = useState("");
    const [withdrawSuccess, setWithdrawSuccess] = useState("");

    const depositKey = useRef(null);
    const withdrawKey = useRef(null);

    const fetchAccounts = async (showRefresh = false) => {
        try {
            setError("");

            if (showRefresh) {
                setRefreshing(true);
            }

            const response = await api.get("/api/accounts");
            const accountList = response.data || [];

            setAccounts(accountList);

            setSelectedAccountNumber(current =>
                accountList.some(account => account.accountNumber === current)
                    ? current
                    : accountList[0]?.accountNumber || ""
            );
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load accounts."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    const totalBalance = useMemo(() => {
        return accounts.reduce(
            (total, account) => total + Number(account.balance || 0),
            0
        );
    }, [accounts]);

    const activeAccounts = accounts.filter(
        account => account.status === "ACTIVE"
    ).length;

    const selectedAccount = accounts.find(
        account => account.accountNumber === selectedAccountNumber
    );

    const formatBalance = (balance) =>
        Number(balance || 0).toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });

    const handleCreateAccount = async (e) => {
        e.preventDefault();

        setCreating(true);
        setError("");
        setSuccess("");

        try {
            const response = await api.post("/api/accounts", {
                accountType
            });

            setSuccess(
                response.data.message ||
                "Bank account created successfully!"
            );

            await fetchAccounts();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to create account."
            );
        } finally {
            setCreating(false);
        }
    };

    const handleDeposit = async (e) => {
        e.preventDefault();

        setDepositError("");
        setDepositSuccess("");

        const amount = Number(depositAmount);

        if (!selectedAccountNumber) {
            setDepositError("Please select a bank account.");
            return;
        }

        if (!Number.isFinite(amount) || amount <= 0) {
            setDepositError("Please enter a valid deposit amount.");
            return;
        }

        setDepositing(true);

        try {
            if (!depositKey.current) {
                depositKey.current = crypto.randomUUID();
            }

            const response = await api.post(
                "/api/transactions/deposit",
                {
                    accountNumber: selectedAccountNumber,
                    amount: depositAmount,
                    idempotencyKey: depositKey.current
                }
            );

            setDepositSuccess(
                `${response.data.message || "Deposit successful!"} Reference: ${response.data.referenceNumber}`
            );

            setDepositAmount("");
            depositKey.current = null;

            await fetchAccounts();
        } catch (err) {
            setDepositError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to complete deposit."
            );
        } finally {
            setDepositing(false);
        }
    };

    const handleWithdraw = async (e) => {
        e.preventDefault();

        setWithdrawError("");
        setWithdrawSuccess("");

        const amount = Number(withdrawAmount);

        if (!selectedAccountNumber) {
            setWithdrawError("Please select a bank account.");
            return;
        }

        if (!Number.isFinite(amount) || amount <= 0) {
            setWithdrawError("Please enter a valid withdrawal amount.");
            return;
        }

        if (selectedAccount && amount > Number(selectedAccount.balance)) {
            setWithdrawError("Insufficient account balance.");
            return;
        }

        setWithdrawing(true);

        try {
            if (!withdrawKey.current) {
                withdrawKey.current = crypto.randomUUID();
            }

            const response = await api.post(
                "/api/transactions/withdraw",
                {
                    accountNumber: selectedAccountNumber,
                    amount: withdrawAmount,
                    idempotencyKey: withdrawKey.current
                }
            );

            setWithdrawSuccess(
                `${response.data.message || "Withdrawal successful!"} Reference: ${response.data.referenceNumber}`
            );

            setWithdrawAmount("");
            withdrawKey.current = null;

            await fetchAccounts();
        } catch (err) {
            setWithdrawError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to complete withdrawal."
            );
        } finally {
            setWithdrawing(false);
        }
    };

    const inputClass =
        "w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-800 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-500/10";

    const labelClass =
        "mb-2 block text-sm font-semibold text-slate-700";

    const ActionMessage = ({ message, type }) => {
        if (!message) return null;

        const isSuccess = type === "success";

        return (
            <div className={`mt-4 flex items-start gap-2 rounded-xl p-3 text-sm ${
                isSuccess
                    ? "bg-emerald-50 text-emerald-700"
                    : "bg-red-50 text-red-700"
            }`}>
                {isSuccess
                    ? <CheckCircle2 size={18} className="mt-0.5 shrink-0" />
                    : <AlertCircle size={18} className="mt-0.5 shrink-0" />
                }
                <span className="break-words">{message}</span>
            </div>
        );
    };

    return (
        <div className="space-y-7">

            {/* Page Heading */}
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-800">
                        My Accounts
                    </h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Manage your bank accounts and everyday transactions.
                    </p>
                </div>

                <button
                    type="button"
                    onClick={() => fetchAccounts(true)}
                    disabled={refreshing}
                    className="flex w-fit items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 shadow-sm transition hover:bg-slate-50 disabled:opacity-60"
                >
                    <RefreshCw
                        size={16}
                        className={refreshing ? "animate-spin" : ""}
                    />
                    {refreshing ? "Refreshing..." : "Refresh Accounts"}
                </button>
            </div>

            {/* Error Message */}
            {error && (
                <ActionMessage message={error} type="error" />
            )}

            {/* Account Summary */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">

                <div className="rounded-2xl bg-gradient-to-br from-blue-700 to-indigo-800 p-6 text-white shadow-lg shadow-blue-900/10">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-blue-100">
                                Total Account Balance
                            </p>
                            <h2 className="mt-3 text-2xl font-bold tracking-tight">
                                ₹{formatBalance(totalBalance)}
                            </h2>
                        </div>

                        <div className="rounded-xl bg-white/15 p-3">
                            <IndianRupee size={25} />
                        </div>
                    </div>
                    <p className="mt-5 text-xs text-blue-100">
                        Combined balance across your accounts
                    </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-slate-500">
                                Total Accounts
                            </p>
                            <h2 className="mt-3 text-3xl font-bold text-slate-800">
                                {loading ? "—" : accounts.length}
                            </h2>
                        </div>

                        <div className="rounded-xl bg-blue-50 p-3 text-blue-600">
                            <Wallet size={25} />
                        </div>
                    </div>
                    <p className="mt-5 text-xs text-slate-400">
                        All registered bank accounts
                    </p>
                </div>

                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-slate-500">
                                Active Accounts
                            </p>
                            <h2 className="mt-3 text-3xl font-bold text-slate-800">
                                {loading ? "—" : activeAccounts}
                            </h2>
                        </div>

                        <div className="rounded-xl bg-emerald-50 p-3 text-emerald-600">
                            <CheckCircle2 size={25} />
                        </div>
                    </div>
                    <p className="mt-5 text-xs text-slate-400">
                        Accounts currently active
                    </p>
                </div>
            </div>

            {/* Account Actions */}
            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">

                {/* Create Account */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="mb-6 flex items-center gap-3">
                        <div className="rounded-xl bg-blue-50 p-3 text-blue-600">
                            <Plus size={22} />
                        </div>
                        <div>
                            <h2 className="font-bold text-slate-800">
                                Open New Account
                            </h2>
                            <p className="text-xs text-slate-500">
                                Create another bank account
                            </p>
                        </div>
                    </div>

                    <form onSubmit={handleCreateAccount} className="space-y-4">
                        <div>
                            <label className={labelClass}>
                                Account Type
                            </label>

                            <select
                                value={accountType}
                                onChange={(e) => setAccountType(e.target.value)}
                                className={inputClass}
                            >
                                <option value="SAVINGS">Savings Account</option>
                                <option value="CURRENT">Current Account</option>
                            </select>
                        </div>

                        <button
                            type="submit"
                            disabled={creating}
                            className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <Plus size={17} />
                            {creating ? "Creating..." : "Create Account"}
                        </button>
                    </form>

                    <ActionMessage message={success} type="success" />
                    <ActionMessage message={error} type="error" />
                </div>

                {/* Deposit */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="mb-6 flex items-center gap-3">
                        <div className="rounded-xl bg-emerald-50 p-3 text-emerald-600">
                            <ArrowDownCircle size={22} />
                        </div>
                        <div>
                            <h2 className="font-bold text-slate-800">
                                Deposit Money
                            </h2>
                            <p className="text-xs text-slate-500">
                                Add money to your account
                            </p>
                        </div>
                    </div>

                    <form onSubmit={handleDeposit} className="space-y-4">
                        <div>
                            <label className={labelClass}>
                                Select Account
                            </label>

                            <select
                                value={selectedAccountNumber}
                                onChange={(e) => setSelectedAccountNumber(e.target.value)}
                                required
                                disabled={accounts.length === 0}
                                className={inputClass}
                            >
                                <option value="">Select Bank Account</option>
                                {accounts.map(account => (
                                    <option
                                        key={account.accountNumber}
                                        value={account.accountNumber}
                                    >
                                        {account.accountType} - {account.accountNumber}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className={labelClass}>
                                Deposit Amount (INR)
                            </label>

                            <input
                                type="number"
                                min="0.01"
                                step="0.01"
                                required
                                value={depositAmount}
                                onChange={(e) => setDepositAmount(e.target.value)}
                                placeholder="Enter amount"
                                className={inputClass}
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={depositing || accounts.length === 0}
                            className="flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <ArrowDownCircle size={17} />
                            {depositing ? "Processing..." : "Deposit Money"}
                        </button>
                    </form>

                    <ActionMessage message={depositSuccess} type="success" />
                    <ActionMessage message={depositError} type="error" />
                </div>

                {/* Withdraw */}
                <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div className="mb-6 flex items-center gap-3">
                        <div className="rounded-xl bg-rose-50 p-3 text-rose-600">
                            <ArrowUpCircle size={22} />
                        </div>
                        <div>
                            <h2 className="font-bold text-slate-800">
                                Withdraw Money
                            </h2>
                            <p className="text-xs text-slate-500">
                                Withdraw from your account
                            </p>
                        </div>
                    </div>

                    <form onSubmit={handleWithdraw} className="space-y-4">
                        <div>
                            <label className={labelClass}>
                                Select Account
                            </label>

                            <select
                                value={selectedAccountNumber}
                                onChange={(e) => setSelectedAccountNumber(e.target.value)}
                                required
                                disabled={accounts.length === 0}
                                className={inputClass}
                            >
                                <option value="">Select Bank Account</option>
                                {accounts.map(account => (
                                    <option
                                        key={account.accountNumber}
                                        value={account.accountNumber}
                                    >
                                        {account.accountType} - {account.accountNumber}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className={labelClass}>
                                Withdrawal Amount (INR)
                            </label>

                            <input
                                type="number"
                                min="0.01"
                                step="0.01"
                                required
                                value={withdrawAmount}
                                onChange={(e) => setWithdrawAmount(e.target.value)}
                                placeholder="Enter amount"
                                className={inputClass}
                            />

                            {selectedAccount && (
                                <p className="mt-2 text-xs text-slate-500">
                                    Available: {selectedAccount.currency}{" "}
                                    {formatBalance(selectedAccount.balance)}
                                </p>
                            )}
                        </div>

                        <button
                            type="submit"
                            disabled={withdrawing || accounts.length === 0}
                            className="flex w-full items-center justify-center gap-2 rounded-xl bg-rose-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-rose-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <ArrowUpCircle size={17} />
                            {withdrawing ? "Processing..." : "Withdraw Money"}
                        </button>
                    </form>

                    <ActionMessage message={withdrawSuccess} type="success" />
                    <ActionMessage message={withdrawError} type="error" />
                </div>
            </div>

            {/* Bank Accounts */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">

                <div className="mb-6 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <h2 className="text-lg font-bold text-slate-800">
                            Your Bank Accounts
                        </h2>
                        <p className="mt-1 text-sm text-slate-500">
                            View your account details and balances.
                        </p>
                    </div>

                    <span className="w-fit rounded-full bg-blue-50 px-3 py-1.5 text-xs font-semibold text-blue-700">
                        {accounts.length} Account(s)
                    </span>
                </div>

                {loading ? (
                    <div className="py-12 text-center">
                        <RefreshCw className="mx-auto mb-3 animate-spin text-blue-600" size={28} />
                        <p className="text-sm text-slate-500">
                            Loading your accounts...
                        </p>
                    </div>
                ) : accounts.length === 0 ? (
                    <div className="rounded-xl border border-dashed border-slate-300 py-12 text-center">
                        <Building2 className="mx-auto mb-3 text-slate-400" size={35} />
                        <h3 className="font-semibold text-slate-700">
                            No bank accounts found
                        </h3>
                        <p className="mt-1 text-sm text-slate-500">
                            Create your first account using the form above.
                        </p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-5 md:grid-cols-2">

                        {accounts.map((account, index) => (
                            <div
                                key={account.accountNumber}
                                className="overflow-hidden rounded-2xl border border-slate-200 transition hover:border-blue-300 hover:shadow-md"
                            >
                                <div className={`p-5 ${
                                    index % 2 === 0
                                        ? "bg-gradient-to-br from-slate-800 to-slate-900"
                                        : "bg-gradient-to-br from-indigo-700 to-blue-900"
                                } text-white`}>
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-3">
                                            <div className="rounded-xl bg-white/15 p-3">
                                                <CreditCard size={22} />
                                            </div>
                                            <div>
                                                <h3 className="font-bold">
                                                    {account.accountType} Account
                                                </h3>
                                                <p className="text-xs text-slate-300">
                                                    SmartFin Banking
                                                </p>
                                            </div>
                                        </div>

                                        <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                                            account.status === "ACTIVE"
                                                ? "bg-emerald-400/20 text-emerald-200"
                                                : "bg-white/15 text-white"
                                        }`}>
                                            {account.status}
                                        </span>
                                    </div>

                                    <p className="mt-7 text-xs tracking-wider text-slate-300">
                                        ACCOUNT NUMBER
                                    </p>
                                    <p className="mt-1 break-all text-lg font-semibold tracking-wider">
                                        {account.accountNumber}
                                    </p>
                                </div>

                                <div className="p-5">
                                    <p className="text-xs font-medium text-slate-500">
                                        Available Balance
                                    </p>

                                    <h3 className="mt-2 text-2xl font-bold text-slate-800">
                                        {account.currency} {formatBalance(account.balance)}
                                    </h3>

                                    <div className="mt-5 flex items-center justify-between border-t border-slate-100 pt-4">
                                        <span className="text-xs text-slate-400">
                                            Account Type
                                        </span>
                                        <span className="text-xs font-semibold text-slate-600">
                                            {account.accountType}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ))}

                    </div>
                )}
            </div>
        </div>
    );
}

export default Accounts;
