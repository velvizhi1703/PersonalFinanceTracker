document.addEventListener("DOMContentLoaded", function () {
    // ✅ Create the Settings UI dynamically for SPA
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
            <h1>Settings</h1>
            <form id="settingsForm">
                <label for="currency">Preferred Currency:</label>
                <select id="currency">
                    <option value="USD">USD</option>
                    <option value="EUR">EUR</option>
                    <option value="INR">INR</option>
                </select>
                <button type="submit">Save</button>
            </form>
        </main>
    `;

    const token = localStorage.getItem("token");
    if (!token) {
        loadPage("login");
        return;
    }

    document.getElementById("settingsForm").addEventListener("submit", function (event) {
        event.preventDefault();
        const selectedCurrency = document.getElementById("currency").value;

        fetch("/api/settings", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ currency: selectedCurrency })
        })
        .then(response => response.json())
        .then(data => {
            alert("Settings updated successfully");
        })
        .catch(error => console.error("Error updating settings:", error));
    });

    // ✅ Handle Logout
    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("token");
        loadPage("login");
    });
});
