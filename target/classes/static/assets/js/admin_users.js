$(document).ready(() => {
	loadUsers();
});
function loadUsers() {
	$.ajax({
		url: "http://localhost:9091/api/users",
		method: "GET",
		headers: {
			"Authorization": "Bearer " + localStorage.getItem("token"),
			"Content-Type": "application/json"
		},
		success: (data) => {
			const usersTableBody = $("#usersTableBody");

			if (!usersTableBody.length) {
				return;
			}
			usersTableBody.empty();
			usersTableBody.html(data.map(user => `
				 <tr>
				                    <td>${user.id}</td>
				                    <td>${user.name}</td>
				                    <td>${user.email}</td>
				                    <td>Rs. ${user.totalExpense || 0}</td>
				                    <td>Rs. ${user.totalIncome || 0}</td>
				                    <td>${user.numTransactions || 0}</td>
				                    <td class="${user.status.toLowerCase() === 'enabled' ? 'text-success' : 'text-danger'}">
				                        ${user.status || 'Unknown'}
				                    </td>
				                    <td>
				                        <button class="toggle-status-btn btn btn-${user.status.toLowerCase() === 'enabled' ? 'danger' : 'success'}" 
				                            data-user-id="${user.id}" 
				                            data-status="${user.status}">
				                            ${user.status.toLowerCase() === 'enabled' ? 'Disable' : 'Enable'}
				                        </button>
				                    </td>
				                </tr>
				            `).join(''));
		},
		error: (error) => console.error("Error fetching users:", error)
	});
}

function toggleUserStatus(userId, currentStatus) {
	const newStatus = currentStatus.toLowerCase() === "enabled" ? "Disabled" : "Enabled";
	$.ajax({
		url: `http://localhost:9091/api/users/${userId}/status`,
		method: "PUT",
		headers: {
			"Authorization": "Bearer " + localStorage.getItem("token"),
			"Content-Type": "application/json"
		},
		data: JSON.stringify({ status: newStatus }),
		success: (responseMessage) => {
			alert(`User status updated to ${newStatus} successfully!`);
			loadUsers();
		},
		error: (error) => {
			alert(`User status updated to: Disable`);
		}
	});
}

$(document).on("click", ".toggle-status-btn", function() {
	let userId = $(this).data("user-id");
	let currentStatus = $(this).data("status");
	toggleUserStatus(userId, currentStatus);
});