$(document).ready(() => {
	fetchUserTransactions();
	$("#filterTransactions").on("change", filterTransactions);
	$("#searchInput").on("keyup", searchTransactions);

});

function fetchUserTransactions() {
	const token = localStorage.getItem("token");
	const userId = localStorage.getItem("userId");
	if (!userId) {
		return;
	}

	$.ajax({
		url: `http://localhost:9091/api/transactions/user/${userId}`,
		method: "GET",
		headers: {
			"Authorization": `Bearer ${token}`,
			"Content-Type": "application/json"
		},
		success: function(response) {
			const transactions = response._embedded?.transactionList || [];

			if (transactions.length === 0) {
				$("#transactionTableBody").html(
					`<tr><td colspan="6" class="text-center text-warning">No transactions available.</td></tr>`
				);
				return;
			}

			updateTransactionTable(transactions);
		},
		error: function(error) {
			$("#transactionTableBody").html(
				`<tr><td colspan="6" class="text-center text-danger">Error loading transactions.</td></tr>`
			);
		}
	});
}
function updateTransactionTable(transactions) {
	const tableBody = $("#transactionTableBody");
	tableBody.empty();

	if (!Array.isArray(transactions) || transactions.length === 0) {
		tableBody.append(`<tr><td colspan="6" class="text-center">No transactions found.</td></tr>`);
		return;
	}

	transactions.forEach((t) => {
		const row = document.createElement("tr");
		row.setAttribute("onclick", `selectTransaction(${t.id})`);

		row.innerHTML = `
		            <td>${t.id}</td>
		            <td>${t.type}</td>
		            <td>${formatCurrency(t.amount)}</td>
		            <td>${t.category}</td>
		            <td>${new Date(t.date).toLocaleDateString()}</td>
					<td>
					<button class="btn btn-danger btn-sm delete-btn" data-id="${t.id}">
					                    <i class="fa fa-trash"></i> Delete
					                </button>
					            </td>
					        `;

		tableBody.append(row);
	});

	filterTransactions();
	searchTransactions();
	attachDeleteEvent();
}

function attachDeleteEvent() {
	$(".delete-btn").off("click").on("click", function() {
		let transactionId = $(this).data("id")
		deleteTransaction(transactionId);
	});
}

function filterTransactions() {
	const selectedType = $("#filterTransactions").val().toUpperCase();
	const rows = $("#transactionTableBody tr");

	rows.each(function() {
		const typeCell = $(this).find("td:nth-child(2)").text().trim();
		$(this).toggle(selectedType === "ALL" || typeCell === selectedType);
	});
}

function searchTransactions() {
	const searchText = $("#searchInput").val().toLowerCase();
	const rows = $("#transactionTableBody tr");

	rows.each(function() {
		const rowText = $(this).text().toLowerCase();
		$(this).toggle(rowText.includes(searchText));
	});
}

function formatCurrency(value) {
	return `€ ${parseFloat(value).toFixed(2)}`;
}

function deleteTransaction(transactionId) {
	if (!confirm("Are you sure you want to delete this transaction?")) return;

	$.ajax({
		url: `http://localhost:9091/api/transactions/${transactionId}`,
		type: "DELETE",
		headers: {
			"Authorization": "Bearer " + localStorage.getItem("token")
		},
		success: function() {
			alert("Transaction deleted successfully!");

			fetchUserTransactions();
		},
		error: function(xhr, status, error) {
			alert("Failed to delete transaction: " + xhr.responseText);
		}
	});
}







