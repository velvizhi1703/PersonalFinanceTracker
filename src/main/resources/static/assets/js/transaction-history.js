$(document).ready(() => {
    fetchUserTransactions();

    // ✅ Attach event listeners
    $("#filterTransactions").on("change", filterTransactions);
    $("#searchInput").on("keyup", searchTransactions);
   
});

function fetchUserTransactions() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId) {
        console.error("❌ Missing user ID");
        return;
    }

    $.ajax({
        url: `http://localhost:9091/api/transactions/user/${userId}`,
        method: "GET",
        headers: { 
            "Authorization": `Bearer ${token}`, 
            "Content-Type": "application/json" 
        },
        success: function (response) {
            console.log("✅ Transactions API Response:", response);
			 const transactions = response._embedded?.transactionList || [];

			    if (transactions.length === 0) {
			        console.warn("⚠️ No transactions found!");
			        $("#transactionTableBody").html(
			            `<tr><td colspan="6" class="text-center text-warning">No transactions available.</td></tr>`
			        );
			        return;
			    }

			    updateTransactionTable(transactions);
			},
        error: function (error) {
            console.error("❌ Failed to fetch transactions:", error);
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
		        `;

		        tableBody.append(row);
		    });

		    console.log("✅ Transactions updated in the table!");

		    // ✅ Reapply Filters and Search after Updating
		    filterTransactions();
		    searchTransactions();
		}


// ✅ Function to Filter Transactions by Credit/Debit
function filterTransactions() {
    const selectedType = $("#filterTransactions").val().toUpperCase();
    const rows = $("#transactionTableBody tr");

    rows.each(function () {
        const typeCell = $(this).find("td:nth-child(2)").text().trim();
        $(this).toggle(selectedType === "ALL" || typeCell === selectedType);
    });
}

// ✅ Function to Search Transactions by Keyword
function searchTransactions() {
    const searchText = $("#searchInput").val().toLowerCase();
    const rows = $("#transactionTableBody tr");

    rows.each(function () {
        const rowText = $(this).text().toLowerCase();
        $(this).toggle(rowText.includes(searchText));
    });
}

// ✅ Function to Format Currency
function formatCurrency(value) {
    return `€ ${parseFloat(value).toFixed(2)}`;
}







