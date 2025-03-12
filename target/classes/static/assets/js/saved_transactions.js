document.addEventListener("DOMContentLoaded", function () {
    // ✅ Create the Saved Transactions UI dynamically for SPA
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
            <h1>Saved Transactions</h1>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Type</th>
                        <th>Amount</th>
                        <th>Category</th>
                        <th>Date</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody id="savedTransactionsTableBody">
                    <!-- Saved transactions will be dynamically inserted here -->
                </tbody>
            </table>
        </main>
    `;

    const token = localStorage.getItem("token");
    if (!token) {
        loadPage("login");
        return;
    }

    fetch("/api/saved_transactions", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token,
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(data => {
        const tableBody = document.getElementById("savedTransactionsTableBody");
        tableBody.innerHTML = data.map(t => `
            <tr>
                <td>${t.id}</td>
                <td>${t.type}</td>
                <td>${t.amount}</td>
                <td>${t.category}</td>
                <td>${t.date}</td>
                <td><button onclick="deleteTransaction(${t.id})">Delete</button></td>
            </tr>
        `).join('');
    })
    .catch(error => console.error("Error fetching saved transactions:", error));

    function deleteTransaction(transactionId) {
        fetch(`/api/transactions/${transactionId}`, {
            method: "DELETE",
            headers: { "Authorization": "Bearer " + token }
        })
        .then(() => {
            alert("Transaction deleted");
            loadPage("saved_transactions"); // ✅ Reload page dynamically after deletion
        })
        .catch(error => console.error("Error deleting transaction:", error));
    }

    // ✅ Handle Logout
    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("token");
        loadPage("login");
    });
});