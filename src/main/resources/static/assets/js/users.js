$(document).ready(() => {
    console.log("🚀 User Dashboard Script Loaded");

	loadUserData();
    loadDashboardData();
});
function loadUserData() {
    console.log("📥 Fetching user data...");
    const token = localStorage.getItem("token");

    if (!token) {
        console.warn("⚠️ No token found! Redirecting to login...");
        localStorage.clear();
        window.location.hash = "#login";
        return;
    }

    $.ajax({
        url: "http://localhost:9091/api/users/me",
        method: "GET",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
        success: (data) => {
            if (!data || !data.name || !data.email) {
                console.error("❌ Invalid User Data");
                return;
            }

            console.log("✅ User Data:", data);

            // ✅ Dynamically update the username and email in the UI
            $("#userName").text(data.name);
            $("#userEmail").text(`(${data.email})`);
        },
        error: (error) => {
            console.error("❌ Error fetching user data:", error);
        }
    });
}
function loadDashboardData() {
    console.log("📥 Loading user dashboard data...");
    const token = localStorage.getItem("token");

    if (!token) {
        console.warn("⚠️ No token found! Redirecting to login...");
        localStorage.clear();
        window.location.hash = "#login";
        return;
    }

    $.ajax({
        url: "http://localhost:9091/api/transactions/dashboard",
        method: "GET",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
        success: (data) => {
            if (!data) {
                console.error("❌ Invalid Dashboard Data");
                return;
            }

            console.log("✅ Dashboard Data:", data);

            // ✅ Update Dashboard Data
            $("#income").text(formatCurrency(data.income));
            $("#expense").text(formatCurrency(data.expense));
            $("#cashInHand").text(formatCurrency(data.cash_in_hand));
            $("#transactionCount").text(data.num_transactions);

            // ✅ Update Budget Meter
            if (data.budget) {
                $("#budgetAmount").text(formatCurrency(data.budget.spent));
                $("#remainingBudget").text(formatCurrency(data.budget.remaining));
            }

            // ✅ Ensure Charts are Updated
            updateCharts(data);
			updateBudgetMeter(data.budget.spent, data.budget.remaining);
        },
        error: (error) => {
            console.error("❌ Error loading dashboard data:", error);
        }
    });
}

function formatCurrency(value) {
    return `€ ${parseFloat(value).toFixed(2)}`;
}

// ✅ Function to update all charts
function updateCharts(data) {
    console.log("📊 Updating Charts...");
    
    if (data.expenseBreakdown && Object.keys(data.expenseBreakdown).length > 0) {
        createExpenseChart(data.expenseBreakdown);
    } else {
        console.warn("⚠️ No expense breakdown data found!");
    }

    if (data.budget) {
        updateBudgetMeter(data.budget.spent, data.budget.remaining);
    } else {
        console.warn("⚠️ No budget data found!");
    }
}

// ✅ Function to create the Expense Breakdown Chart
function createExpenseChart(expenseBreakdown) {
    const ctx = document.getElementById("expenseChart");

    if (!ctx) {
        console.error("❌ Chart canvas element not found!");
        return;
    }

    new Chart(ctx, {
        type: "pie",
        data: {
            labels: Object.keys(expenseBreakdown),
            datasets: [{
                data: Object.values(expenseBreakdown),
                backgroundColor: ["#FF5733", "#33FF57", "#3357FF", "#FF33A1", "#A133FF", "#33FFF5"]
            }]
        }
    });

    console.log("✅ Expense Chart Rendered!");
}

// ✅ Function to update the Budget Meter
function updateBudgetMeter(spent, remaining) {
    const ctx = document.getElementById("budgetChart");

    if (!ctx) {
        console.error("❌ Budget chart element not found!");
        return;
    }

    // Destroy previous chart instance if exists
    if (window.budgetChartInstance) {
        window.budgetChartInstance.destroy();
    }

    // Create a Doughnut Chart (Semi-circle meter)
    window.budgetChartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Spent", "Remaining"],
            datasets: [{
                data: [spent, remaining],
                backgroundColor: ["#FF5733", "#33FF57"], // Red for spent, green for remaining
                borderWidth: 0
            }]
        },
        options: {
            rotation: -90, // Start from the top
            circumference: 180, // Show as semi-circle
            cutout: "70%", // Adjust thickness
            plugins: {
                legend: {
                    display: true,
                    position: "bottom",
                    labels: {
                        color: "#fff",
                        font: {
                            size: 14
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (tooltipItem) {
                            return `€ ${tooltipItem.raw.toFixed(2)}`;
                        }
                    }
                }
            }
        }
    });

    // ✅ Update Budget Text
    document.getElementById("remainingBudget").innerText = remaining < 0 
        ? `Overbudget by €${Math.abs(remaining).toFixed(2)}` 
        : `€ ${remaining.toFixed(2)}`;
}
document.getElementById("editBudget").addEventListener("click", function () {
    let newBudget = prompt("Enter your new budget amount:");

    if (newBudget && !isNaN(newBudget)) {
        newBudget = parseFloat(newBudget);

        // ✅ Update Budget UI
        document.getElementById("budgetAmount").innerText = `€ ${newBudget.toFixed(2)}`;

        // ✅ Fetch existing spent amount
        let spent = window.budgetChartInstance?.data.datasets[0].data[0] || 0;
        let remaining = newBudget - spent;

        // ✅ Update the Budget Meter with new budget values
        updateBudgetMeter(spent, remaining);
    }
});
