$(document).ready(function () {
    console.log("Admin Dashboard Script Loaded");

    const token = localStorage.getItem("token");

    if (!token) {
        console.error("No token found! Redirecting to login...");
        localStorage.clear();
        window.location.hash = "#login";
        return;
    }

    let userRole = localStorage.getItem("userRole");
    if (!userRole) {
        console.warn("No stored role! Fetching from API...");
        fetchUserDetails();
    }
	

    if (userRole !== "ROLE_ADMIN") {
        console.warn("Unauthorized! Redirecting non-admin to Users Page...");
        window.location.hash = "#users";
        return;
    }

    console.log("Admin Access Granted! Showing Dashboard...");
    $("#adminDashboardContainer").removeClass("d-none");

      loadAdminTransactions();

    // Hide Transactions section on page load
    $("#transactionsContainer").addClass("d-none");
});


function loadAdminTransactions() {
    console.log("📢 Fetching all transactions for admin...");

    $.ajax({
        url: `http://localhost:9091/api/transactions/admin`, // ✅ Ensure correct API
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        success: function (data) {
            console.log("✅ Admin Transactions Fetched:", data);
			 // ✅ Extract transactions from HATEOAS response
			            const transactions = data._embedded?.transactionList || [];

			            if (transactions.length === 0) {
			                console.warn("🚨 No transactions found!");
			            }

			            displayAdminTransactions(transactions);
			        },
			        error: function (error) {
			            console.error("🚨 Error fetching transactions:", error);
			        }
			    });
			}


			function displayAdminTransactions(transactions) {
			    console.log("✅ Transactions Data Received:", transactions);

			    const transactionsContainer = $("#transactionsContainer");
			    const transactionsTableBody = $("#transactionsTableBody");

			    if (!transactionsTableBody.length) {
			        console.error("❌ transactionsTableBody not found in the HTML!");
			        return;
			    }

			    transactionsTableBody.html(""); // Clear existing table data

			    if (!Array.isArray(transactions) || transactions.length === 0) {
			        console.warn("🚨 No transactions found!");
			        transactionsTableBody.html("<tr><td colspan='6'>No transactions available</td></tr>");
			    } else {
			        console.log(`🔹 Populating table with ${transactions.length} transactions...`);

			        const rows = transactions.map(transaction => `
			            <tr>
			                <td>${transaction.id}</td>
			                <td>${transaction.userEmail || "N/A"}</td>
			                <td>${transaction.category || "No Category"}</td>
			                <td>${transaction.date ? new Date(transaction.date).toLocaleDateString() : "N/A"}</td>
			                <td>${transaction.type || "N/A"}</td>
			                <td>Rs. ${transaction.amount || 0}</td>
			            </tr>
			        `).join('');

			        transactionsTableBody.html(rows);
			        console.log("✅ Transactions Table Updated");
			    }

			    // ✅ Force the table section to be visible
			    transactionsContainer.removeClass("d-none").css("display", "block");
			    console.log("✅ Table is now visible!");
			}
