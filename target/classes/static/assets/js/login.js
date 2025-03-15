$(document).ready(() => {
	console.log(" Login Script Loaded");	
	$("#loginButton").off().on("click", loginUser);
	
	$("#register-btn").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("register");
	});
});

function loginUser() {
    console.log(" Login button clicked!");

    const email = $("#loginEmail").val().trim();
    const password = $("#loginPassword").val().trim();

    if (!email || !password) {
        alert(" Please enter both email and password!");
        return;
    }

    console.log(" Sending login request...");
	$.ajax({
		url: "/api/auth/login",
		method: "POST",
		headers: {"Content-Type": "application/json"},
		data: JSON.stringify({email, password}),
		dataType: "json",
		success: (response) => {
			const data = response;
	    	console.log(" Login Successful:", data);
			
			if (data.token) {
	        	localStorage.setItem("token", data.token);
	       		localStorage.setItem("userId", data.userId);
	        	localStorage.setItem("userRole", data.role);
				
				
				   // ✅ Load Sidebar First, Then Redirect
				    $("#footer").load("/pages/footer.html", function () {
				                   console.log("✅ Footer loaded!");
				               });

				               // ✅ Load Admin Sidebar if Admin Logs In
							   let redirectPage = data.role === "ROLE_ADMIN" ? "admin_transactions" : "users";

							               // ✅ Load Correct Sidebar Before Redirecting
							               $("#sidebar").load(data.role === "ROLE_ADMIN" ? "/pages/admin_sidebar.html" : "/pages/sidebar.html", function () {
							                   console.log(`✅ Sidebar Loaded for ${data.role}`);
							                   $.getScript(data.role === "ROLE_ADMIN" ? "/assets/js/admin_sidebar.js" : "/assets/js/sidebar.js", function () {
							                       console.log(`✅ Sidebar JS executed!`);
							                       
							                       // ✅ Redirect to the Dashboard AFTER Sidebar Loads
							                       location.hash = `#${redirectPage}`;
							                       Router.handleNavigation();
							                   });
							               });

							               // ✅ Show Sidebar & Footer
							               $("#sidebar").show();
							               $("#footer").show();
							           } else {
							               console.error("🚨 Token missing in response");
							           }
							       },
							       error: (xhr, status, error) => {
							           console.error("🚨 Login failed:", error);
							           alert("Invalid credentials. Please try again.");
							       }
							   });
				   }