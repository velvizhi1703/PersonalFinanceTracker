document.addEventListener("DOMContentLoaded", function () {
    // ✅ Create the Statistics UI dynamically for SPA
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
            <h1>Statistics</h1>
            <canvas id="expenseChart"></canvas>
        </main>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    `;

    const token = localStorage.getItem("token");
    if (!token) {
        loadPage("login");
        return;
    }

    fetch("/api/statistics", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + token,
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(data => {
        const ctx = document.getElementById("expenseChart").getContext("2d");
        new Chart(ctx, {
            type: "pie",
            data: {
                labels: data.categories,
                datasets: [{
                    data: data.amounts,
                    backgroundColor: ["red", "blue", "green", "yellow", "purple"]
                }]
            }
        });
    })
    .catch(error => console.error("Error fetching statistics:", error));

    // ✅ Handle Logout
    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("token");
        loadPage("login");
    });
});
