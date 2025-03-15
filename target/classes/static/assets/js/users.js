$(document).ready(() => {
    console.log("🚀 User Dashboard Script Loaded");
    loadDashboardData();
});

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
    const totalBudget = spent + remaining; // Total budget is the sum of spent and remaining
    const spentPercentage = (spent / totalBudget) * 100; // Calculate percentage spent

    let budgetMeter = document.getElementById("budgetMeter");
    if (!budgetMeter) {
        console.error("❌ Budget meter element not found!");
        return;
    }

    budgetMeter.style.width = `${spentPercentage}%`;
    budgetMeter.style.backgroundColor = spentPercentage > 80 ? "#FF5733" : "#33FF57";
    budgetMeter.innerText = `${spentPercentage.toFixed(2)}% Spent`;
}
