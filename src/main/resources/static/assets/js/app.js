document.addEventListener("DOMContentLoaded", function () {
    localStorage.clear();  // ✅ Clear stored token so user always starts fresh
    sessionStorage.clear();
	attachLoginListener(); 
    checkAuthentication(); // ✅ This now correctly redirects only if logged in
    updateNavbar();
    window.addEventListener("hashchange", handleNavigation);
    handleNavigation(); 
});


// 🟢 Check Authentication and Redirect
function checkAuthentication() {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");

    if (!token || !userId || !userRole) {
        console.warn("⚠️ No valid token found. Redirecting to login...");
        localStorage.clear();
        sessionStorage.clear();
        window.location.hash = "#login";
        showPage("loginPage");
        return;
    }

    console.log("✅ User authenticated. Checking role...");
    
    if (!userRole) {
        console.warn("⚠️ No stored user role found! Fetching from API...");
        fetchUserDetails();
    } else {
        window.location.hash = userRole === "ROLE_ADMIN" ? "#admin_dashboard" : "#user_dashboard";
    }
}

// 🟢 Function to Show Correct Page
function showPage(pageId) {
    const pages = ["loginPage", "registerPage", "dashboardContainer", "adminDashboardContainer"];

    pages.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.classList.add("d-none");
        }
    });

    const targetPage = document.getElementById(pageId);
    if (targetPage) {
        targetPage.classList.remove("d-none");
    } else {
        console.warn(`⚠️ Page ${pageId} not found.`);
    }
}

// 🟢 Handle Page Switching
function handleNavigation() {

    const page = window.location.hash.replace("#", "") || "login";
    console.log(`🔍 Navigating to: ${page}`);
	
	const token = localStorage.getItem("token");
	const userRole = localStorage.getItem("userRole");
	
	if (!token || !userRole) {
	        console.warn("⚠️ User not logged in. Redirecting to login...");
	        showPage("loginPage");
	        window.location.hash = "#login";
	        return;
	    }

		if (page === "admin_dashboard" && userRole === "ROLE_ADMIN") {
		        showPage("adminDashboardContainer");
		    } else if (page === "user_dashboard" && userRole === "ROLE_USER") {
		        showPage("dashboardContainer");
		        loadDashboardData();
		    } else if (page === "transactions" && userRole === "ROLE_USER") {
		        fetchUserTransactions(); // ✅ Only load transactions when clicking Transactions
		    } else if (page === "register") {
		        showPage("registerPage");
		        setTimeout(attachRegisterListener, 100);
		    } else {
		        showPage("loginPage");
		        setTimeout(attachLoginListener, 100);
		    }

		    // Ensure page is refreshed properly
		    window.scrollTo(0, 0);
		}

// 🟢 Fetch User Details
function fetchUserDetails() {
    const token = localStorage.getItem("token");

    fetch("http://localhost:9091/api/users/me", {
        method: "GET",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" }
    })
    .then(response => {
        if (!response.ok) throw new Error(`❌ API Error: ${response.status}`);
        return response.json();
    })
    .then(data => {
        if (!data || !data.id || !data.roles) {
            throw new Error("❌ Invalid user data received.");
        }

        localStorage.setItem("userId", data.id);
        localStorage.setItem("userRole", data.roles[0]);

        console.log(`✅ User Role Set: ${data.roles[0]}`);
		window.location.assign(`index.html#${data.role === "ROLE_ADMIN" ? "admin_dashboard" : "user_dashboard"}`);

    })
    .catch(error => {
        console.error("❌ Failed to fetch user details:", error);
        localStorage.clear();
        window.location.hash = "#login";
    });
}

// 🟢 Attach Event Listener for Login Form
function attachLoginListener() {
	 const loginForm = document.getElementById("loginForm");
	    if (!loginForm) {
	        console.error("❌ Login form not found!");
	        return;
	    }

	    loginForm.removeEventListener("submit", loginUser);  // ✅ Ensure no duplicate listeners
	    loginForm.addEventListener("submit", loginUser);
	}

	async function loginUser(event) {
	    event.preventDefault();
        const email = document.getElementById("loginEmail").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        if (!email || !password) {
            alert("❌ Please fill in all fields!");
            return;
        }

        try {
            const response = await fetch("http://localhost:9091/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
				const errorText = await response.text();
                throw new Error("❌ Login failed. Check credentials.");
            }

            const data = await response.json();
			console.log("✅ Login Successful:", data);

            localStorage.setItem("token", data.token);
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("userRole", data.role);
			console.log("🔄 Redirecting to:", data.role === "ROLE_ADMIN" ? "admin_dashboard" : "user_dashboard");
			 window.location.hash = data.role === "ROLE_ADMIN" ? "#admin_dashboard" : "#user_dashboard";
			        handleNavigation();  // ✅ Ensure page updates dynamically

			    } catch (error) {
			        console.error("❌ Login Error:", error);
			        alert("❌ Login failed. Check credentials and try again.");
			    }
			}
    

// 🟢 Logout
document.addEventListener("DOMContentLoaded", function () {
	document.getElementById("logoutButton").addEventListener("click", function () {
	    console.log("🚪 Logging out...");
	    localStorage.clear();
	    sessionStorage.clear();
	    window.location.assign("index.html#login"); // ✅ Redirect properly to login
	});

});

// 🟢 Ensure Navbar Updates Based on Login Status
function updateNavbar() {
    const userRole = localStorage.getItem("userRole");
    const loginLink = document.getElementById("loginLink");
    const registerLink = document.getElementById("registerLink");
    const logoutButton = document.getElementById("logoutButton");

    if (userRole) {
        loginLink.classList.add("d-none");
        registerLink.classList.add("d-none");
        logoutButton.parentElement.classList.remove("d-none");
    } else {
        loginLink.classList.remove("d-none");
        registerLink.classList.remove("d-none");
        logoutButton.parentElement.classList.add("d-none");
    }
}

// 🟢 Call updateNavbar after login or page load
window.addEventListener("hashchange", updateNavbar);
document.addEventListener("DOMContentLoaded", updateNavbar);

document.getElementById("transactionsHistoryLink").addEventListener("click", function(event) {
    event.preventDefault(); // Prevent default navigation
    fetchUserTransactions(); // ✅ Now calling from `users.js`
});



