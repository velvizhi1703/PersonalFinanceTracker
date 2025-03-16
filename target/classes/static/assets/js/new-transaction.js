$(document).ready(function () {
    $("#transactionForm").submit(function (event) {
        event.preventDefault(); // Prevent page refresh

        // Collect form data
        const formData = {
            amount: parseFloat($("#amount").val()),
            type: $("#type").val(),
            category: $("#category").val(),
            date: $("#date").val()
        };

        // Send AJAX request to backend API
        $.ajax({
            url: "http://localhost:9091/api/transactions",
            type: "POST",
            contentType: "application/json",
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("token") // Get JWT from localStorage
            },
            data: JSON.stringify(formData),
            success: function (response) {
                $("#message").text("✅ Transaction added successfully!").css("color", "green");
                $("#transactionForm")[0].reset(); // Reset form after success
            },
            error: function (xhr, status, error) {
                console.error("Error:", error);
                $("#message").text("❌ Failed to add transaction!").css("color", "red");
            }
        });
    });
});

$("#deleteTransaction").click(function () {
    const transactionId = localStorage.getItem("selectedTransactionId"); // Get transaction ID

    if (!transactionId) {
        alert("❌ Error: No transaction selected. Please select a transaction before deleting.");
        return;
    }

    const token = localStorage.getItem("token");

    $.ajax({
        url: `http://localhost:9091/api/transactions/${transactionId}`,
        type: "DELETE",
        headers: {
            "Authorization": "Bearer " + token
        },
        success: function () {
            alert("✅ Transaction deleted successfully!");
            localStorage.removeItem("selectedTransactionId"); // Clear saved ID
            fetchUserTransactions(); // Reload transactions after delete
        },
        error: function (error) {
            console.error("❌ Error deleting transaction:", error);
            alert("❌ Failed to delete transaction.");
        }
    });
});

