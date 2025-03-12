document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");
    if (!token) {
        console.error("❌ No token found! Redirecting to login...");
		document.getElementById("loginPage").classList.remove("d-none");
		        document.getElementById("dashboardContainer").classList.add("d-none");
        window.location.hash = "#login";
        return;
    }

    console.log("✅ Admin dashboard loaded. Fetching data...");
	// ✅ Ensure dashboard loads only once
	   if (!window.dashboardLoaded) {
	       window.dashboardLoaded = true;
	       loadDashboardData();
	   }
    loadUsers();
	loadTransactions();
    fetchAdminDetails();

    // Detect URL changes and reload API accordingly
    window.addEventListener("hashchange", function () {
        if (window.location.hash === "#users") {
            console.log("🔄 User Dashboard detected, loading users...");
            loadUsers();
        }
        if (window.location.hash === "#transactions") {
            console.log("🔄 Transactions page detected, loading transactions...");
            loadTransactions();
        }
    });
});


// 🟢 Load Users
window.loadUsers = function () {
    fetch("http://localhost:9091/api/users", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log("✅ Fetched Users:", data);

        const usersTableBody = document.getElementById("usersTableBody");
        if (!usersTableBody) return;

        usersTableBody.innerHTML = data.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td>Rs. ${user.totalExpenses}</td>
                <td>Rs. ${user.totalIncome}</td>
                <td>${user.transactionCount}</td>
                <td>${user.status}</td>
                <td><button onclick="disableUser(${user.id})" class="disable-btn">Disable</button></td>
            </tr>
        `).join('');
    })
    .catch(error => console.error("❌ Error fetching users:", error));
};


// 🟢 Logout Function
window.logout = function () {
    console.log("🚪 Logging out...");
    localStorage.clear();
    sessionStorage.clear();
    window.location.hash = "#login";
    window.location.reload();
};
