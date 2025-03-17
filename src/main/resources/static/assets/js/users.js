$(document).ready(() => {
	loadUserData();
	loadDashboardData();
});
function loadUserData() {
	const token = localStorage.getItem("token");

	if (!token) {
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
				return;
			}

			$("#userName").text(data.name);
			$("#userEmail").text(`(${data.email})`);
		},
		error: (error) => {
			console.error("Error fetching user data:", error);
		}
	});
}
function loadDashboardData() {
	const token = localStorage.getItem("token");

	if (!token) {
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
				return;
			}

			$("#income").text(formatCurrency(data.income));
			$("#expense").text(formatCurrency(data.expense));
			$("#cashInHand").text(formatCurrency(data.cash_in_hand));
			$("#transactionCount").text(data.num_transactions);

			if (data.budget) {
				$("#budgetAmount").text(formatCurrency(data.budget.spent));
				$("#remainingBudget").text(formatCurrency(data.budget.remaining));
			}

			updateCharts(data);
			updateBudgetMeter(data.budget.spent, data.budget.remaining);
		},
		error: (error) => {
			console.error("Error loading dashboard data:", error);
		}
	});
}

function formatCurrency(value) {
	return `€ ${parseFloat(value).toFixed(2)}`;
}

function updateCharts(data) {

	if (data.expenseBreakdown && Object.keys(data.expenseBreakdown).length > 0) {
		createExpenseChart(data.expenseBreakdown);
	}

	if (data.budget) {
		updateBudgetMeter(data.budget.spent, data.budget.remaining);
	}
}

function createExpenseChart(expenseBreakdown) {
	const ctx = document.getElementById("expenseChart");

	if (!ctx) {
		return;
	}

	if (window.expenseChartInstance) {
		window.expenseChartInstance.destroy();
	}

	window.expenseChartInstance = new Chart(ctx, {
		type: "doughnut",
		data: {
			labels: Object.keys(expenseBreakdown),
			datasets: [{
				data: Object.values(expenseBreakdown),
				backgroundColor: [
					"#1E88E5",
					"#43A047",
					"#FBC02D",
					"#E53935",
					"#8E24AA",
					"#FB8C00"
				],
				borderWidth: 1
			}]
		},
		options: {
			cutout: "60%",
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
				title: {
					display: true,
					text: "Expense Chart",
					color: "#fff",
					font: {
						size: 18,
						weight: "bold"
					},
					padding: {
						top: 10,
						bottom: 20
					}
				},
				tooltip: {
					callbacks: {
						label: function(tooltipItem) {
							return `€ ${tooltipItem.raw.toFixed(2)}`;
						}
					}
				}
			}
		}
	});

}

function updateBudgetMeter(spent, remaining) {
	const ctx = document.getElementById("budgetChart");

	if (!ctx) {
		return;
	}

	if (window.budgetChartInstance) {
		window.budgetChartInstance.destroy();
	}

	window.budgetChartInstance = new Chart(ctx, {
		type: "doughnut",
		data: {
			labels: ["Spent", "Remaining"],
			datasets: [{
				data: [spent, remaining],
				backgroundColor: ["#FF5733", "#33FF57"],
				borderWidth: 0
			}]
		},
		options: {
			rotation: -90,
			circumference: 180,
			cutout: "70%",
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
						label: function(tooltipItem) {
							return `€ ${tooltipItem.raw.toFixed(2)}`;
						}
					}
				}
			}
		}
	});

	document.getElementById("remainingBudget").innerText = remaining < 0
		? `Overbudget by €${Math.abs(remaining).toFixed(2)}`
		: `€ ${remaining.toFixed(2)}`;
}
document.getElementById("editBudget").addEventListener("click", function() {
	let newBudget = prompt("Enter your new budget amount:");

	if (newBudget && !isNaN(newBudget)) {
		newBudget = parseFloat(newBudget);

		document.getElementById("budgetAmount").innerText = `€ ${newBudget.toFixed(2)}`;

		let spent = window.budgetChartInstance?.data.datasets[0].data[0] || 0;
		let remaining = newBudget - spent;

		updateBudgetMeter(spent, remaining);
	}
});
