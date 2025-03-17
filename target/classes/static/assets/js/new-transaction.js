$(document).ready(function() {
	$("#transactionForm").submit(function(event) {
		event.preventDefault();
		const formData = {
			amount: parseFloat($("#amount").val()),
			type: $("#type").val(),
			category: $("#category").val(),
			date: $("#date").val()
		};
		$.ajax({
			url: "http://localhost:9091/api/transactions",
			type: "POST",
			contentType: "application/json",
			headers: {
				"Authorization": "Bearer " + localStorage.getItem("token")
			},
			data: JSON.stringify(formData),
			success: function(response) {
				 showMessage("Transaction added successfully!", "success");
				$("#transactionForm")[0].reset();
			},
			error: function(xhr, status, error) {
				  showMessage("Failed to add transaction!", "danger");
			}
		});
	});
	 function showMessage(message, type) {
	        const alertBox = $("#alertMessage");
	        alertBox.removeClass("d-none alert-success alert-danger")
	                .addClass(`alert alert-${type}`)
	                .text(message);
	        
	        // Hide message after 3 seconds
	        setTimeout(() => {
	            alertBox.addClass("d-none");
	        }, 3000);
	    }
	});



