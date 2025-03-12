document.addEventListener("DOMContentLoaded", function () {
    const token = localStorage.getItem("token");
    if (!token) {
        loadPage("login");
        return;
    }

    loadTransactions();
});

function loadTransactions() {
	const token = localStorage.getItem("token");
	fetch("/api/transactions/user", {
	    method: "GET",
	    headers: {
	        "Authorization": "Bearer " + token,
	        "Content-Type": "application/json"
	    }
	})

    .then(response => response.json())
    .then(data => {
        const transactionsTableBody = document.getElementById("transactionsTableBody");
        transactionsTableBody.innerHTML = data.map(t => `
            <tr>
                <td>${t.id}</td>
                <td>${t.type}</td>
                <td>Rs. ${t.amount}</td>
                <td>${t.category}</td>
                <td>${new Date(t.date).toLocaleDateString()}</td>
            </tr>
        `).join('');
    })
    .catch(error => console.error("Error fetching transactions:", error));
}
