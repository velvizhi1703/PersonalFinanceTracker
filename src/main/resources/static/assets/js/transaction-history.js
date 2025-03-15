// Ensure script runs only once
if (!window.transactionHistoryInitialized) {
    window.transactionHistoryInitialized = true;
    
    console.log("✅ Transaction History Script Loaded!");

    $(document).ready(() => {
        // Ensure the page content is fully loaded before fetching transactions
        setTimeout(() => {
            if ($("#transactionTableBody").length) {
                console.log("✅ Transaction table found! Fetching transactions...");
                fetchUserTransactions();
            } else {
                console.error("❌ Transaction table not found! Retrying...");
                setTimeout(fetchUserTransactions, 500); // Retry if table is not ready
            }
        }, 500);
    });
}

function fetchUserTransactions() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId) {
        console.error("Missing user ID");
        return;
    }

    $.ajax({
        url: `/api/transactions/${userId}`,
        method: "GET",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
        success: (data) => {
            console.log("Fetched Transactions:", data);
            updateTransactionTable(data);
        },
        error: (error) => {
            console.error("Error fetching transactions:", error);
        }
    });
}

function updateTransactionTable(transactions) {
    const tableBody = $("#transactionTableBody");
    tableBody.empty();

    if (transactions.length === 0) {
        tableBody.append(`<tr><td colspan="5" class="text-center">No transactions found.</td></tr>`);
        return;
    }

    transactions.forEach((t) => {
        tableBody.append(
            `<tr>
                <td>${t.id}</td>
                <td>${t.type}</td>
                <td>${formatCurrency(t.amount)}</td>
                <td>${t.category}</td>
                <td>${new Date(t.date).toLocaleDateString()}</td>
            </tr>`
        );
    });
}

function formatCurrency(value) {
    return `€ ${parseFloat(value).toFixed(2)}`;
}





/* --------------------------- OLD CODE ------------------------ */
/*
function displayFormattedTransactions(data) {
    const contentElement = $("#content");
    if (!contentElement.length) return;

    const uniqueTransactions = removeDuplicates(data);
    const groupedTransactions = { Today: [], Yesterday: [], Older: [] };

    const today = new Date().toISOString().split('T')[0];
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    uniqueTransactions.forEach(t => {
        const transactionDate = new Date(t.date).toISOString().split('T')[0];
        if (transactionDate === today) groupedTransactions.Today.push(t);
        else if (transactionDate === yesterdayStr) groupedTransactions.Yesterday.push(t);
        else groupedTransactions.Older.push(t);
    });

    let transactionTable = `<h2>Transaction History</h2>
        <input type="text" id="searchInput" placeholder="Search transactions..." onkeyup="filterTransactions()">`;

    Object.keys(groupedTransactions).forEach(section => {
        if (groupedTransactions[section].length > 0) {
            transactionTable += `<h3>${section}</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Type</th>
                            <th>Amount (€)</th>
                            <th>Category</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody id="transactionTableBody"></tbody>
                </table>`;
        }
    });

    contentElement.html(transactionTable);
    window.filteredTransactions = [...uniqueTransactions];
    updateTransactionTable();
}

function removeDuplicates(transactions) {
    const seen = new Set();
    return transactions.filter(t => {
        if (seen.has(t.id)) return false;
        seen.add(t.id);
        return true;
    });
}
*/