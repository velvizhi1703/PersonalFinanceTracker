$(document).ready(function() {
	const token = localStorage.getItem("token");
	if (!token) {
		localStorage.clear();
		window.location.hash = "#login";
		return;
	}

	let userRole = localStorage.getItem("userRole");
	if (!userRole) {
		fetchUserDetails();
	}

	if (userRole !== "ROLE_ADMIN") {
		window.location.hash = "#users";
		return;
	}
	$("#adminDashboardContainer").removeClass("d-none");

	$(window).on("hashchange", function() {
		if (window.location.hash === "#admin_transactions") {
			loadAdminTransactions();
		}
	});

	if (window.location.hash === "#admin_transactions") {
		loadAdminTransactions();
	}
});


let transactions = [];
let filteredTransactions = [];
let currentPage = 1;
const rowsPerPage = 10;

function loadAdminTransactions() {
	$.ajax({
		url: `http://localhost:9091/api/transactions/admin`,
		method: "GET",
		headers: {
			"Authorization": "Bearer " + localStorage.getItem("token"),
			"Content-Type": "application/json"
		},
		success: function(data) {
			const transactionsData = data._embedded?.transactionList || [];

			if (transactionsData.length === 0) {
			}
			transactions = transactionsData;
			filteredTransactions = transactions;
			currentPage = 1;
			displayAdminTransactions();
		},
		error: function(error) {
			console.error("Error fetching transactions:", error);
		}
	});
}

function displayAdminTransactions() {
	const transactionsContainer = $("#transactionsContainer");
	const transactionsTableBody = $("#transactionsTableBody");

	if (!transactionsTableBody.length) {
		return;
	}
	transactionsTableBody.html("");

	let start = (currentPage - 1) * rowsPerPage;
	let end = start + rowsPerPage;
	let paginatedItems = filteredTransactions.slice(start, end);

	if (paginatedItems.length === 0) {
		transactionsTableBody.html("<tr><td colspan='6'>No transactions available</td></tr>");
	} const rows = paginatedItems.map(transaction => {
		return `
		               <tr id="transaction-${transaction.id}">
		                   <td>${transaction.id}</td>
		                   <td>${transaction.userEmail || "N/A"}</td>
		                   <td>${transaction.category || "No Category"}</td>
		                   <td>${transaction.date ? new Date(transaction.date).toLocaleDateString() : "N/A"}</td>
		                   <td>${transaction.type || "N/A"}</td>
		                   <td>€ ${transaction.amount || 0}</td>
		                   </tr>
		           `;
	}).join('');

	transactionsTableBody.html(rows);
}


updatePaginationControls();

function updatePaginationControls() {
    $("#pageNumber").text(`Page ${currentPage} of ${Math.ceil(filteredTransactions.length / rowsPerPage)}`);
    $("#prevPage").prop("disabled", currentPage === 1);
    $("#nextPage").prop("disabled", currentPage * rowsPerPage >= filteredTransactions.length);
}
$("#searchTransactionsAdmin").on("input", function() {
	let searchText = $(this).val().toLowerCase();

	filteredTransactions = transactions.filter(transaction =>
		transaction.id.toString().includes(searchText) ||
		(transaction.userEmail && transaction.userEmail.toLowerCase().includes(searchText)) ||
		(transaction.category && transaction.category.toLowerCase().includes(searchText)) ||
		(transaction.date && new Date(transaction.date).toLocaleDateString().includes(searchText)) ||
		(transaction.type && transaction.type.toLowerCase().includes(searchText)) ||
		(transaction.amount && transaction.amount.toString().includes(searchText))
	);

	currentPage = 1;
	displayAdminTransactions();
});

$("#prevPage").on("click", function() {
	if (currentPage > 1) {
		currentPage--;
		displayAdminTransactions();
	}
});

$("#nextPage").on("click", function() {
	if (currentPage * rowsPerPage < filteredTransactions.length) {
		currentPage++;
		displayAdminTransactions();
	}
});

