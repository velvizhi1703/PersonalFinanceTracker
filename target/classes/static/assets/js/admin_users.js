$(document).ready(() => {
    loadUsers();
});

// Function to load users and display in the table
function loadUsers() {
    $.ajax({
        url: "http://localhost:9091/api/users",
        method: "GET",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        success: (data) => {
            console.log("Users Fetched:", data);
            const usersTableBody = $("#usersTableBody");

            if (!usersTableBody.length) {
                console.error("❌ usersTableBody not found!");
                return;
            }

            usersTableBody.html(data.map(user => `
                <tr>
                    <td>${user.id}</td>
                    <td>${user.name}</td>
                    <td>${user.email}</td>
                    <td>Rs. ${user.totalExpense || 0}</td>
                    <td>Rs. ${user.totalIncome || 0}</td>
                    <td>${user.numTransactions || 0}</td>
                    <td class="${user.status === 'Enabled' ? 'text-success' : 'text-danger'}">
                        ${user.status || 'Unknown'}
                    </td>
                    <td>
                        <button onclick="toggleUserStatus(${user.id}, '${user.status}')" 
                            class="btn btn-${user.status === 'Enabled' ? 'danger' : 'success'}">
                            ${user.status === 'Enabled' ? 'Disable' : 'Enable'}
                        </button>
                    </td>
                </tr>
            `).join(''));
        },
        error: (error) => console.error("Error fetching users:", error)
    });
}

// Function to toggle user status
function toggleUserStatus(userId, currentStatus) {
    const newStatus = currentStatus.toUpperCase() === "ENABLED" ? "DISABLED" : "ENABLED";

    $.ajax({
        url: `http://localhost:9091/api/users/${userId}/status`,
        method: "PUT",
        headers: {
            "Authorization": "Bearer " + localStorage.getItem("token"),
            "Content-Type": "application/json"
        },
        data: JSON.stringify({ status: newStatus }),
        success: (responseMessage) => {
            console.log(`User ${userId} status changed: ${responseMessage}`);
            loadUsers(); // Reload users after updating status
        },
        error: (error) => console.error("Error updating user status:", error)
    });
}
