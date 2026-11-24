
import { useEffect, useState, useRef } from "react";
import {
    ArrowLeftRight,
    RefreshCw,
    CheckCircle2,
    AlertCircle
} from "lucide-react";
import api from "../services/api";

function Transfers() {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [transferring, setTransferring] = useState(false);

    const [sourceAccountNumber, setSourceAccountNumber] = useState("");
    const [destinationAccountNumber, setDestinationAccountNumber] = useState("");
    const [transferAmount, setTransferAmount] = useState("");

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const transferKey = useRef(null);

    const fetchAccounts = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get("/api/accounts");
            setAccounts(response.data || []);

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load bank accounts."
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    const selectedSource = accounts.find(
        (account) => account.accountNumber === sourceAccountNumber
    );

    const selectedDestination = accounts.find(
        (account) => account.accountNumber === destinationAccountNumber
    );

    const handleTransfer = async (event) => {
        event.preventDefault();

        setError("");
        setSuccess("");

        if (!sourceAccountNumber || !destinationAccountNumber) {
            setError("Please select both accounts.");
            return;
        }

        if (sourceAccountNumber === destinationAccountNumber) {
            setError("Source and destination accounts cannot be same.");
            return;
        }

        const amount = Number(transferAmount);

        if (!Number.isFinite(amount) || amount <= 0) {
            setError("Enter a valid transfer amount.");
            return;
        }

        if (amount > Number(selectedSource?.balance)) {
            setError("Insufficient account balance.");
            return;
        }

        if (
            selectedSource?.currency !== selectedDestination?.currency
        ) {
            setError("Both accounts must have the same currency.");
            return;
        }

        try {
            setTransferring(true);

            if (!transferKey.current) {
                transferKey.current = crypto.randomUUID();
            }

            const response = await api.post(
                "/api/transactions/transfer",
                {
                    sourceAccountNumber,
                    destinationAccountNumber,
                    amount,
                    idempotencyKey: transferKey.current
                }
            );

            setSuccess(
                response.data?.message ||
                "Money transferred successfully."
            );

            setTransferAmount("");
            transferKey.current = null;

            await fetchAccounts();

        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Transfer failed. Please try again."
            );
        } finally {
            setTransferring(false);
        }
    };

    if (loading) {
        return (
            <div className="text-center py-12 text-slate-500">
                Loading accounts...
            </div>
        );
    }

    return (
        <div className="space-y-6">

            <div>
                <h1 className="text-2xl font-bold text-slate-800">
                    Money Transfers
                </h1>
                <p className="text-slate-500 mt-2">
                    Transfer money between your bank accounts.
                </p>
            </div>

            <div className="bg-white border border-slate-200 shadow-sm rounded-2xl p-6 max-w-2xl">

                <div className="flex items-center gap-3 mb-6">
                    <div className="bg-blue-100 p-3 rounded-xl">
                        <ArrowLeftRight className="text-blue-600" size={24} />
                    </div>

                    <div>
                        <h2 className="text-lg font-semibold text-slate-800">
                            Transfer Money
                        </h2>
                        <p className="text-sm text-slate-500">
                            Move funds between your accounts
                        </p>
                    </div>
                </div>

                {error && (
                    <div className="flex gap-2 items-center bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
                        <AlertCircle size={18} />
                        {error}
                    </div>
                )}

                {success && (
                    <div className="flex gap-2 items-center bg-green-50 text-green-700 p-3 rounded-lg mb-4 text-sm">
                        <CheckCircle2 size={18} />
                        {success}
                    </div>
                )}

                {accounts.length < 2 ? (
                    <div className="bg-yellow-50 text-yellow-800 p-4 rounded-lg text-sm">
                        You need at least two bank accounts to transfer money.
                    </div>
                ) : (
                    <form onSubmit={handleTransfer} className="space-y-5">

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-2">
                                From Account
                            </label>

                            <select
                                value={sourceAccountNumber}
                                onChange={(e) => {
                                    setSourceAccountNumber(e.target.value);
                                    setSuccess("");
                                    transferKey.current = null;
                                }}
                                className="w-full border border-slate-300 rounded-lg px-3 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                                required
                            >
                                <option value="">Select source account</option>

                                {accounts.map((account) => (
                                    <option
                                        key={account.accountNumber}
                                        value={account.accountNumber}
                                    >
                                        {account.accountType} - {account.accountNumber}
                                    </option>
                                ))}
                            </select>

                            {selectedSource && (
                                <p className="text-sm text-slate-500 mt-2">
                                    Available Balance: {selectedSource.currency}{" "}
                                    {Number(selectedSource.balance).toLocaleString("en-IN")}
                                </p>
                            )}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-2">
                                To Account
                            </label>

                            <select
                                value={destinationAccountNumber}
                                onChange={(e) => {
                                    setDestinationAccountNumber(e.target.value);
                                    setSuccess("");
                                    transferKey.current = null;
                                }}
                                className="w-full border border-slate-300 rounded-lg px-3 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                                required
                            >
                                <option value="">Select destination account</option>

                                {accounts
                                    .filter(
                                        (account) =>
                                            account.accountNumber !== sourceAccountNumber
                                    )
                                    .map((account) => (
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
                            <label className="block text-sm font-medium text-slate-700 mb-2">
                                Transfer Amount
                            </label>

                            <input
                                type="number"
                                min="0.01"
                                step="0.01"
                                value={transferAmount}
                                onChange={(e) => {
                                    setTransferAmount(e.target.value);
                                    setSuccess("");
                                    transferKey.current = null;
                                }}
                                placeholder="Enter amount"
                                className="w-full border border-slate-300 rounded-lg px-3 py-3 outline-none focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>

                        {selectedDestination && (
                            <p className="text-sm text-slate-500">
                                Destination: {selectedDestination.accountType}
                            </p>
                        )}

                        <button
                            type="submit"
                            disabled={transferring}
                            className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50"
                        >
                            {transferring ? (
                                <>
                                    <RefreshCw size={18} className="animate-spin" />
                                    Processing Transfer...
                                </>
                            ) : (
                                <>
                                    <ArrowLeftRight size={18} />
                                    Transfer Money
                                </>
                            )}
                        </button>

                    </form>
                )}
            </div>
        </div>
    );
}

export default Transfers;
