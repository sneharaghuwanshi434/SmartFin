
import { useEffect, useState } from "react";
import {
    RefreshCw,
    ArrowDownCircle,
    ArrowUpCircle,
    ArrowLeftRight,
    CalendarDays,
    X,
    Eye,
    XCircle,
    Copy,
    CheckCircle2,
    Download
} from "lucide-react";
import api from "../services/api";

function Transactions() {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [filter, setFilter] = useState("ALL");

    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [appliedDates, setAppliedDates] = useState(null);
    const [dateError, setDateError] = useState("");

    const [selectedTransaction, setSelectedTransaction] = useState(null);
    const [copied, setCopied] = useState(false);
    const [downloading, setDownloading] = useState(false);
    const [downloadError, setDownloadError] = useState("");

    const pageSize = 10;

    // Fetch transactions with backend filters
    const fetchTransactions = async () => {
        try {
            setLoading(true);
            setError("");

            const params = new URLSearchParams({
                page: String(page),
                size: String(pageSize),
                transactionType: filter
            });

            let response;

            if (appliedDates) {
                const start = new Date(
                    `${appliedDates.startDate}T00:00:00+05:30`
                ).toISOString();

                const end = new Date(
                    `${appliedDates.endDate}T23:59:59.999+05:30`
                ).toISOString();

                params.append("startDate", start);
                params.append("endDate", end);

                response = await api.get(
                    `/api/transactions/history/filter?${params.toString()}`
                );
            } else {
                response = await api.get(
                    `/api/transactions/history?${params.toString()}`
                );
            }

            const data = response.data;

            setTransactions(data.content || []);
            setTotalPages(data.totalPages || 0);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load transactions."
            );
            setTransactions([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTransactions();
    }, [page, appliedDates, filter]);

    // Handle transaction type filter
    const handleTypeFilter = (type) => {
        setPage(0);
        setFilter(type);
    };

    // Handle date filter
    const handleDateFilter = (event) => {
        event.preventDefault();

        if (!startDate || !endDate) {
            setDateError("Please select both start and end dates.");
            return;
        }

        if (startDate > endDate) {
            setDateError("Start date cannot be after end date.");
            return;
        }

        setDateError("");
        setPage(0);
        setAppliedDates({ startDate, endDate });
    };

    const clearDateFilter = () => {
        setStartDate("");
        setEndDate("");
        setDateError("");
        setAppliedDates(null);
        setPage(0);
    };

    // Format date
    const formatDate = (date) => {
        if (!date) return "N/A";

        return new Date(date).toLocaleString("en-IN", {
            dateStyle: "medium",
            timeStyle: "short"
        });
    };

    // Format transaction amount
    const formatAmount = (transaction) => {
        const amount = Number(transaction.amount);

        if (!Number.isFinite(amount)) return "N/A";

        const formattedAmount = amount.toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });

        if (transaction.transactionType === "DEPOSIT") {
            return `+₹${formattedAmount}`;
        }

        if (transaction.transactionType === "WITHDRAWAL") {
            return `−₹${formattedAmount}`;
        }

        return `₹${formattedAmount}`;
    };

    const getAmountStyle = (type) => {
        switch (type?.toUpperCase()) {
            case "DEPOSIT":
                return "text-green-600";
            case "WITHDRAWAL":
                return "text-red-600";
            case "TRANSFER":
                return "text-blue-600";
            default:
                return "text-slate-700";
        }
    };

    const getTransactionIcon = (type) => {
        switch (type?.toUpperCase()) {
            case "DEPOSIT":
                return <ArrowDownCircle className="text-green-600" />;
            case "WITHDRAWAL":
                return <ArrowUpCircle className="text-red-600" />;
            case "TRANSFER":
                return <ArrowLeftRight className="text-blue-600" />;
            default:
                return <RefreshCw className="text-slate-500" />;
        }
    };

    const getStatusStyle = (status) => {
        switch (status?.toUpperCase()) {
            case "SUCCESS":
                return "bg-green-100 text-green-700";
            case "PENDING":
                return "bg-yellow-100 text-yellow-700";
            case "FAILED":
                return "bg-red-100 text-red-700";
            default:
                return "bg-slate-100 text-slate-600";
        }
    };

    // Copy reference number
    const handleCopyReference = async () => {
        if (!selectedTransaction?.referenceNumber) return;

        try {
            await navigator.clipboard.writeText(
                selectedTransaction.referenceNumber
            );

            setCopied(true);
            setTimeout(() => setCopied(false), 2000);

        } catch {
            setCopied(false);
        }
    };

    // Download PDF receipt
    const handleDownloadReceipt = async () => {
        if (!selectedTransaction?.referenceNumber) return;

        try {
            setDownloading(true);
            setDownloadError("");

            const referenceNumber =
                selectedTransaction.referenceNumber;

            const response = await api.get(
                `/api/transactions/${encodeURIComponent(referenceNumber)}/receipt`,
                {
                    responseType: "blob"
                }
            );

            const blob = new Blob([response.data], {
                type: "application/pdf"
            });

            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = url;
            link.download = `SmartFin-Receipt-${referenceNumber}.pdf`;

            document.body.appendChild(link);
            link.click();
            link.remove();

            window.setTimeout(() => {
                window.URL.revokeObjectURL(url);
            }, 1000);

        } catch (err) {
            setDownloadError(
                "Unable to download receipt. Please try again."
            );
        } finally {
            setDownloading(false);
        }
    };

    const filterOptions = [
        { label: "All", value: "ALL" },
        { label: "Deposits", value: "DEPOSIT" },
        { label: "Withdrawals", value: "WITHDRAWAL" },
        { label: "Transfers", value: "TRANSFER" }
    ];

    return (
        <div className="space-y-6">

            <div>
                <h1 className="text-2xl font-bold text-slate-800">
                    Transaction History
                </h1>
                <p className="text-slate-500 mt-2">
                    View and filter all your transactions.
                </p>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">

                <div className="flex flex-wrap justify-between items-center gap-3 mb-6">
                    <h2 className="text-lg font-semibold text-slate-800">
                        All Transactions
                    </h2>

                    <button
                        onClick={fetchTransactions}
                        disabled={loading}
                        className="flex items-center gap-2 px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
                    >
                        <RefreshCw size={16} />
                        Refresh
                    </button>
                </div>

                {/* Date Range Filter */}
                <form
                    onSubmit={handleDateFilter}
                    className="bg-slate-50 border border-slate-200 rounded-xl p-4 mb-6"
                >
                    <div className="flex items-center gap-2 mb-4">
                        <CalendarDays size={19} className="text-blue-600" />
                        <h3 className="font-semibold text-slate-700">
                            Filter by Date Range
                        </h3>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                        <div>
                            <label className="block text-sm text-slate-600 mb-2">
                                Start Date
                            </label>
                            <input
                                type="date"
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                                className="w-full border border-slate-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div>
                            <label className="block text-sm text-slate-600 mb-2">
                                End Date
                            </label>
                            <input
                                type="date"
                                value={endDate}
                                onChange={(e) => setEndDate(e.target.value)}
                                className="w-full border border-slate-300 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-blue-500"
                            />
                        </div>

                        <div className="flex items-end gap-2">
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50"
                            >
                                Apply Filter
                            </button>

                            <button
                                type="button"
                                onClick={clearDateFilter}
                                disabled={loading}
                                title="Clear date filter"
                                className="px-3 py-2 rounded-lg border border-slate-300 text-slate-600 hover:bg-slate-200 disabled:opacity-50"
                            >
                                <X size={18} />
                            </button>
                        </div>
                    </div>

                    {dateError && (
                        <p className="text-sm text-red-600 mt-3">
                            {dateError}
                        </p>
                    )}

                    {appliedDates && (
                        <p className="text-sm text-blue-700 mt-3">
                            Showing transactions from {appliedDates.startDate}
                            {" "}to{" "}
                            {appliedDates.endDate}
                        </p>
                    )}
                </form>

                {/* Transaction Type Filter */}
                <div className="flex flex-wrap gap-3 mb-6">
                    {filterOptions.map((option) => (
                        <button
                            key={option.value}
                            onClick={() => handleTypeFilter(option.value)}
                            disabled={loading}
                            className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                                filter === option.value
                                    ? "bg-blue-600 text-white"
                                    : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                            } disabled:opacity-50`}
                        >
                            {option.label}
                        </button>
                    ))}
                </div>

                {/* Transaction Table */}
                {loading ? (
                    <p className="text-center py-10 text-slate-500">
                        Loading transactions...
                    </p>
                ) : error ? (
                    <p className="text-center py-10 text-red-600">
                        {error}
                    </p>
                ) : transactions.length === 0 ? (
                    <p className="text-center py-10 text-slate-500">
                        No transactions found for this filter.
                    </p>
                ) : (
                    <>
                        <div className="overflow-x-auto">
                            <table className="w-full text-left">
                                <thead>
                                <tr className="border-b text-slate-500 text-sm">
                                    <th className="py-4 px-3">Transaction</th>
                                    <th className="py-4 px-3">Reference</th>
                                    <th className="py-4 px-3">Amount</th>
                                    <th className="py-4 px-3">Description</th>
                                    <th className="py-4 px-3">Date</th>
                                    <th className="py-4 px-3">Status</th>
                                    <th className="py-4 px-3">Action</th>
                                </tr>
                                </thead>

                                <tbody>
                                {transactions.map((transaction) => (
                                    <tr
                                        key={transaction.referenceNumber}
                                        className="border-b last:border-0 hover:bg-slate-50"
                                    >
                                        <td className="py-4 px-3">
                                            <div className="flex items-center gap-3">
                                                {getTransactionIcon(
                                                    transaction.transactionType
                                                )}
                                                <span className="font-medium text-slate-700">
                                                        {transaction.transactionType}
                                                    </span>
                                            </div>
                                        </td>

                                        <td className="py-4 px-3 text-sm text-slate-600 whitespace-nowrap">
                                            {transaction.referenceNumber}
                                        </td>

                                        <td className="py-4 px-3 font-semibold whitespace-nowrap">
                                                <span className={getAmountStyle(
                                                    transaction.transactionType
                                                )}>
                                                    {formatAmount(transaction)}
                                                </span>
                                        </td>

                                        <td className="py-4 px-3 text-sm text-slate-600 min-w-52">
                                            {transaction.description || "N/A"}
                                        </td>

                                        <td className="py-4 px-3 text-sm text-slate-600 whitespace-nowrap">
                                            {formatDate(transaction.createdAt)}
                                        </td>

                                        <td className="py-4 px-3">
                                                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusStyle(
                                                    transaction.status
                                                )}`}>
                                                    {transaction.status}
                                                </span>
                                        </td>

                                        <td className="py-4 px-3">
                                            <button
                                                onClick={() => {
                                                    setSelectedTransaction(transaction);
                                                    setCopied(false);
                                                    setDownloadError("");
                                                }}
                                                className="flex items-center gap-2 text-blue-600 hover:text-blue-800 font-medium text-sm"
                                            >
                                                <Eye size={16} />
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        <div className="flex justify-between items-center mt-6">
                            <p className="text-sm text-slate-500">
                                Page {page + 1} of {Math.max(totalPages, 1)}
                            </p>

                            <div className="flex gap-2">
                                <button
                                    onClick={() => setPage((prev) => prev - 1)}
                                    disabled={page === 0 || loading}
                                    className="px-4 py-2 border rounded-lg disabled:opacity-40 hover:bg-slate-100"
                                >
                                    Previous
                                </button>

                                <button
                                    onClick={() => setPage((prev) => prev + 1)}
                                    disabled={page + 1 >= totalPages || loading}
                                    className="px-4 py-2 border rounded-lg disabled:opacity-40 hover:bg-slate-100"
                                >
                                    Next
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>

            {/* Transaction Details Modal */}
            {selectedTransaction && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
                    onClick={() => setSelectedTransaction(null)}
                >
                    <div
                        className="bg-white w-full max-w-lg rounded-2xl shadow-xl p-6"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="flex justify-between items-center mb-6">
                            <div>
                                <h2 className="text-xl font-bold text-slate-800">
                                    Transaction Details
                                </h2>
                                <p className="text-sm text-slate-500 mt-1">
                                    Complete transaction information
                                </p>
                            </div>

                            <button
                                onClick={() => setSelectedTransaction(null)}
                                className="p-2 rounded-lg hover:bg-slate-100 text-slate-500"
                            >
                                <XCircle size={22} />
                            </button>
                        </div>

                        <div className="flex flex-col items-center bg-slate-50 rounded-xl p-5 mb-5">
                            {getTransactionIcon(
                                selectedTransaction.transactionType
                            )}

                            <p className="text-sm text-slate-500 mt-3">
                                {selectedTransaction.transactionType}
                            </p>

                            <h3 className={`text-3xl font-bold mt-2 ${getAmountStyle(
                                selectedTransaction.transactionType
                            )}`}>
                                {formatAmount(selectedTransaction)}
                            </h3>

                            <span className={`mt-3 px-3 py-1 rounded-full text-xs font-semibold ${getStatusStyle(
                                selectedTransaction.status
                            )}`}>
                                {selectedTransaction.status}
                            </span>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <p className="text-xs text-slate-500 mb-1">
                                    Reference Number
                                </p>

                                <div className="flex items-center justify-between gap-2">
                                    <p className="text-sm font-semibold text-slate-800 break-all">
                                        {selectedTransaction.referenceNumber || "N/A"}
                                    </p>

                                    <button
                                        onClick={handleCopyReference}
                                        className="flex items-center gap-1 text-blue-600 text-sm hover:text-blue-800 shrink-0"
                                    >
                                        {copied ? (
                                            <CheckCircle2 size={16} />
                                        ) : (
                                            <Copy size={16} />
                                        )}
                                        {copied ? "Copied" : "Copy"}
                                    </button>
                                </div>
                            </div>

                            <div>
                                <p className="text-xs text-slate-500 mb-1">
                                    Description
                                </p>
                                <p className="text-sm font-medium text-slate-800">
                                    {selectedTransaction.description || "N/A"}
                                </p>
                            </div>

                            <div>
                                <p className="text-xs text-slate-500 mb-1">
                                    Transaction Date & Time
                                </p>
                                <p className="text-sm font-medium text-slate-800">
                                    {formatDate(selectedTransaction.createdAt)}
                                </p>
                            </div>
                        </div>

                        {downloadError && (
                            <p className="text-sm text-red-600 mt-4">
                                {downloadError}
                            </p>
                        )}

                        <div className="flex gap-3 mt-6">
                            <button
                                onClick={handleDownloadReceipt}
                                disabled={downloading}
                                className="flex-1 flex items-center justify-center gap-2 py-3 rounded-lg bg-green-600 text-white font-medium hover:bg-green-700 disabled:opacity-50"
                            >
                                <Download size={18} />
                                {downloading ? "Downloading..." : "Download Receipt"}
                            </button>

                            <button
                                onClick={() => setSelectedTransaction(null)}
                                className="px-5 py-3 rounded-lg border border-slate-300 text-slate-700 font-medium hover:bg-slate-100"
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Transactions;
