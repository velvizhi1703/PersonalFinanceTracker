$(document).ready(function () {
    console.log(" Register Script Loaded");
	$("#register-btn").off("click").on("click", registerUser);
	$("#login-btn").off("click").on("click", (event) => {
		event.preventDefault();
		Router.loadPage("login");
	})
});


function registerUser() {
    console.log(" Register button clicked!");

    const username = $("#username").val().trim();
    const email = $("#email").val().trim();
    const password = $("#password").val().trim();
    const confirmPassword = $("#confirmPassword").val().trim();

    if (!username || !email || !password || password !== confirmPassword) {
        alert(" Please fill all fields correctly!");
        return;
    }
	
	$.ajax({
		url: "/api/users/register",
		method: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify({ username, email, password }),
		dataType: "json",
		success: (data) => {
	    	console.log(" Registration Successful:", data);
			
			alert(" Registration successful! Redirecting to login...");
			Router.loadPage("login");
		},
		error: () => {
			console.error(" Registration failed. Try again.");
			alert(" Registration failed. Try again.");
		}
	});
}
