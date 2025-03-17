$(document).ready(() => {
	$("#loginButton").off().on("click", loginUser);
	
	$("#register-btn").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("register");
	});
});

function loginUser() {
 
    const email = $("#loginEmail").val().trim();
    const password = $("#loginPassword").val().trim();

    if (!email || !password) {
		showAlert("Please enter both email and password!", "danger");
		       return;
    }
	$.ajax({
		url: "/api/auth/login",
		method: "POST",
		headers: {"Content-Type": "application/json"},
		data: JSON.stringify({email, password}),
		dataType: "json",
		success: (response) => {
			const data = response;
			if (data.token) {
	        	localStorage.setItem("token", data.token);
	       		localStorage.setItem("userId", data.userId);
	        	localStorage.setItem("userRole", data.role);
				showAlert("Login Successful! Redirecting...", "success");
				setTimeout(() => {
				                    let redirectPage = data.role === "ROLE_ADMIN" ? "admin_transactions" : "users";
				                    location.hash = `#${redirectPage}`;
				                    Router.handleNavigation();
				                }, 2000);
				            } else {
				                showAlert("Login failed! Token missing.", "danger");
				            }
				        },
				        error: (xhr, status, error) => {
				            showAlert("Login failed. Please check your credentials.", "danger");
				        }
				    });
				}
				function showAlert(message, type) {
				    const alertBox = $("#loginAlert");
				    alertBox.removeClass("d-none alert-success alert-danger").addClass(`alert alert-${type}`).html(message);
				}