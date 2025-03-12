document.addEventListener("DOMContentLoaded", function () {
    attachTransactionListener(); // Attach event listener for transactions
});

// 🟢 Attach Event Listener for Transactions History
function attachTransactionListener() {
    const transactionsLink = document.getElementById("transactionsHistoryLink");
    if (transactionsLink) {
        transactionsLink.addEventListener("click", function (event) {
            event.preventDefault(); // Prevent default link behavior
			 document.getElementById("dashboardSummary").classList.add("d-none");

			            // Show transactions container
			            document.getElementById("transactionsHistoryContainer").classList.remove("d-none");

			            // Fetch and display transactions
						setActiveSidebarLink(transactionsLink);
						fetchUserTransactions();
						        });
						    } else {
						        console.warn("⚠️ transactionsHistoryLink not found!");
						    }
						}

						// 🟢 Function to Handle Sidebar Active Highlight
						function setActiveSidebarLink(activeLink) {
						    // 🔹 Remove active class from all sidebar links
						    document.querySelectorAll(".sidebar nav ul li a").forEach(link => {
						        link.classList.remove("active");
						    });

						    // 🔹 Add active class to the clicked link
						    activeLink.classList.add("active");
						}
function fetchUserTransactions() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId || userId === "undefined") {
        console.error("❌ User ID is missing, cannot fetch transactions.");
        return;
    }

	fetch(`http://localhost:9091/api/transactions/user/${userId}`, {
	        method: "GET",
	        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" }
	    })
	    .then(response => response.json())
	    .then(data => {
	        console.log("✅ API Response Before Formatting:", data);  // 🔍 Debugging Step

	        // ✅ Apply formatting before displaying
	        let formattedData = formatTransactions(data);
	        console.log("✅ API Response After Formatting:", formattedData);  // 🔍 Debugging Step
	        displayFormattedTransactions(formattedData);
	    })
	    .catch(error => console.error("❌ Error fetching transactions:", error));
	}
function formatTransactions(data) {
    return data.map(t => {
        let formattedTransaction = { ...t }; // Create a copy to avoid modifying the original data
		if (!formattedTransaction.type || formattedTransaction.type.trim() === "") {
		            console.warn(`⚠️ Missing type for transaction ID: ${formattedTransaction.id}`);
		            formattedTransaction.type = "UNKNOWN"; // Default value
		        } else {
		            formattedTransaction.type = formattedTransaction.type.toUpperCase(); // Normalize case
		        }

				formattedTransaction.amount = parseFloat(formattedTransaction.amount);

				        return formattedTransaction;
				    });
				}

function displayFormattedTransactions(data) {
    const contentElement = document.getElementById("content");
    if (!contentElement) return;

    // ✅ Remove Duplicate Transactions Before Processing
    let uniqueTransactions = removeDuplicates(data);

    const groupedTransactions = { Today: [], Yesterday: [], Older: [] };

    const today = new Date().toISOString().split('T')[0];
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split('T')[0];

    uniqueTransactions.forEach(t => {
        const transactionDate = new Date(t.date).toISOString().split('T')[0];
        if (transactionDate === today) {
            groupedTransactions.Today.push(t);
        } else if (transactionDate === yesterdayStr) {
            groupedTransactions.Yesterday.push(t);
        } else {
            groupedTransactions.Older.push(t);
        }
    });

    let searchControls = `
        <label>Filter Transactions:</label>
        <input type="text" id="searchInput" placeholder="Search transactions..." onkeyup="filterTransactions()">
    `;

	let paginationControls = `
	    <button id="prevPage" class="btn btn-primary" onclick="changePage(-1)">Previous</button>
	    <span id="pageNumber"> Page 1 </span>
	    <button id="nextPage" class="btn btn-primary" onclick="changePage(1)">Next</button>
	`;


    let transactionTable = `<h2>Transaction History</h2>${searchControls}`;

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

    contentElement.innerHTML = transactionTable;

    // ✅ Store transactions separately for filtering and pagination
    window.originalTransactions = uniqueTransactions;  // Keep original for reference
    window.filteredTransactions = [...uniqueTransactions]; // Store a copy for filtering
    window.currentPage = 1;
    window.transactionsPerPage = 5;

    updateTransactionTable();
}

// 🟢 Function to Remove Duplicate Transactions
function removeDuplicates(transactions) {
    const seen = new Set();
    return transactions.filter(t => {
        if (seen.has(t.id)) {
            return false;  // Ignore duplicate transactions
        } else {
            seen.add(t.id);
            return true;
        }
    });
}
document.getElementById("filterTransactions").addEventListener("change", function () {
    const selectedType = this.value.toUpperCase(); // Convert to uppercase to match API format

    if (selectedType === "ALL") {
        window.filteredTransactions = [...window.originalTransactions]; // Reset filter
    } else {
        window.filteredTransactions = window.originalTransactions.filter(t => 
            t.type.toUpperCase() === selectedType // Match exactly
        );
    }

    filterTransactions(); 
});


// 🟢 Function to Update Table Based on Current Page
function updateTransactionTable() {
    let transactionsToShow = (window.searchedTransactions || window.filteredTransactions).slice(
        (window.currentPage - 1) * window.transactionsPerPage,
        window.currentPage * window.transactionsPerPage
    );

    let tableBody = document.getElementById("transactionTableBody");
    tableBody.innerHTML = transactionsToShow.map(t => ` 
        <tr>
            <td>${t.id}</td>
            <td>${t.type}</td>
            <td>${formatCurrency(t.amount)}</td> <!-- Format amount here -->
            <td>${t.category}</td>
            <td>${new Date(t.date).toLocaleDateString()}</td>
        </tr>
    `).join("");

    document.getElementById("pageNumber").innerText = `Page ${window.currentPage}`;
}

// 🟢 Function to Change Pagination Page
function changePage(direction) {
    let totalPages = Math.ceil(window.filteredTransactions.length / window.transactionsPerPage);
    
    if (direction === -1 && window.currentPage > 1) {
        window.currentPage--;
    } else if (direction === 1 && window.currentPage < totalPages) {
        window.currentPage++;
    }

    updateTransactionTable();
}

// 🟢 Function to Filter Transactions (Fixed)
function filterTransactions() {
    let query = document.getElementById("searchInput").value.toLowerCase();

    // ✅ Always filter from `originalTransactions`
	 window.searchedTransactions = window.filteredTransactions.filter(t => 
	        t.type.toLowerCase().includes(query) ||
	        t.category.toLowerCase().includes(query) ||
	        t.amount.toString().includes(query)
	    );

	    window.currentPage = 1;
	    updateTransactionTable();
	}


// 🟢 Format Currency (Ensure Only One € Symbol)
function formatCurrency(value) {
    if (isNaN(value)) return "€ 0.00";  // Prevent NaN errors
    return `€ ${parseFloat(value).toFixed(2)}`; // Format here only
}


// 🟢 Load User Dashboard Data
function loadDashboardData() {
    fetch("http://localhost:9091/api/transactions/dashboard", {
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "Content-Type": "application/json"
        }
    })
    .then(response => response.json())
    .then(data => {
        console.log("✅ Dashboard Data:", data);
		if (!data || typeof data !== "object") {
		       console.error("❌ Invalid Dashboard Data");
		       return;
		   }
		document.getElementById("income").innerText = formatCurrency(data.income);
		       document.getElementById("expense").innerText = formatCurrency(data.expense);
		       document.getElementById("cashInHand").innerText = formatCurrency(data.cash_in_hand);
		       document.getElementById("transactionCount").innerText = data.num_transactions;
		       
			   if (data.expenseBreakdown && data.budget) {
			               updateCharts(data.expenseBreakdown, data.budget);
			           } else {
			               console.warn("⚠️ Missing expense breakdown or budget data.");
			           }
			       })
			       .catch(error => console.error("❌ Error loading dashboard data:", error));
			   }
// 🟢 Function to Update Charts
function updateCharts(expenseBreakdown, budget) {
    const ctxExpense = document.getElementById("expenseChart");
    const ctxBudget = document.getElementById("budgetChart");

    if (!ctxExpense || !ctxBudget) {
        console.warn("⚠️ Chart elements not found. Skipping update.");
        return;
    }

    // ✅ Expense Breakdown Chart
    new Chart(ctxExpense, {
        type: "doughnut",
        data: {
            labels: Object.keys(expenseBreakdown),
            datasets: [{
                data: Object.values(expenseBreakdown),
                backgroundColor: ["#ff6384", "#ff9f40", "#ffcd56", "#4bc0c0"]
            }]
        },
        options: { responsive: true }
    });

    // ✅ Budget Chart
    new Chart(ctxBudget, {
        type: "doughnut",
        data: {
               labels: ["Spent (€)", "Balance (€)"],
            datasets: [{
                data: [budget.spent, budget.remaining],
                backgroundColor: ["#ff6384", "#4bc0c0"]
            }]
        },
        options: { responsive: true }
    });
}
if (document.getElementById("backToDashboard")) {
    document.getElementById("backToDashboard").addEventListener("click", function () {
        document.getElementById("transactionsHistoryContainer").classList.add("d-none");
        document.getElementById("dashboardSummary").classList.remove("d-none");
    });
}

