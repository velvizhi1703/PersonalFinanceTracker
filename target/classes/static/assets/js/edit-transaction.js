$(document).ready(function () {
    const transactionId = new URLSearchParams(window.location.search).get("id"); // Get transaction ID from URL
    
    console.log("🛠️ Debug: Transaction ID =", transactionId); // ✅ Debugging step

    if (!transactionId) {
        alert("❌ Error: Transaction ID is missing. Cannot delete.");
        return;
    }

    $("#deleteTransaction").click(function () {
        if (!confirm("Are you sure you want to delete this transaction?")) return;

        $.ajax({
            url: `http://localhost:9091/api/transactions/${transactionId}`,
            type: "DELETE",
            headers: {
                "Authorization": "Bearer " + localStorage.getItem("token")
            },
            success: function () {
                alert("✅ Transaction deleted successfully!");
                window.location.href = "#transaction-history"; // Redirect after delete
            },
            error: function (xhr, status, error) {
                console.error("❌ Delete Error:", xhr.responseText);
                alert("❌ Failed to delete transaction: " + xhr.responseText);
            }
        });
    });
});
