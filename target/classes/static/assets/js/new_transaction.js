document.addEventListener("DOMContentLoaded", function () {
    // ✅ Create the New Transaction Form dynamically for SPA
    document.getElementById("content").innerHTML = `
        <aside class="sidebar">
            <h2>MyWallet</h2>
            <nav>
                <ul>
                    <li><a href="#user_dashboard" data-page="user_dashboard">Dashboard</a></li>
                    <li><a href="#transactions" data-page="transactions">Transactions History</a></li>
                    <li><a href="#new_transaction" data-page="new_transaction">New Transaction</a></li>
                    <li><a href="#saved_transactions" data-page="saved_transactions">Saved Transactions</a></li>
                    <li><a href="#statistics" data-page="statistics">Statistics</a></li>
                    <li><a href="#settings" data-page="settings">Settings</a></li>
                    <li><a href="#" id="logoutButton">Log out</a></li>
                </ul>
            </nav>
        </aside>

        <main class="dashboard-content">
            <h1>New Transaction</h1>
            <form id="transactionForm">
                <label for="transactionType">Transaction Type:</label>
                <select id="transactionType">
                    <option value="credit">Credit</option>
                    <option value="debit">Debit</option>
                </select>

                <label for="transactionAmount">Amount:</label>
                <input type="number" id="transactionAmount" required>

                <label for="transactionCategory">Category:</label>
                <input type="text" id="transactionCategory" required>

                <button type="submit">Add Transaction</button>
            </form>
        </main>
    `;

    const form = document.getElementById("transactionForm");
    const token = localStorage.getItem("token");

    if (!token) {
        loadPage("login");
        return;
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault();
        const transactionData = {
            type: document.getElementById("transactionType").value,
            amount: document.getElementById("transactionAmount").value,
            category: document.getElementById("transactionCategory").value
        };

        fetch("/api/transactions", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(transactionData)
        })
        .then(response => response.json())
        .then(data => {
            alert("Transaction added successfully");
            loadPage("transactions"); // ✅ Redirect to transactions page dynamically
        })
        .catch(error => console.error("Error adding transaction:", error));
    });

    // ✅ Handle Logout
    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("token");
        loadPage("login");
    });
});