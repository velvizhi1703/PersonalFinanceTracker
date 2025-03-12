document.addEventListener("DOMContentLoaded", function () {
    const role = localStorage.getItem("userRole"); // Fetch user role from localStorage
    const token = localStorage.getItem("token");

    // If no token found, redirect to login
    if (!token) {
        loadPage("login");
        return;
    }

    // Display appropriate dashboard link based on role
    const dashboardLinks = document.getElementById("dashboardLinks");

    if (role === "ROLE_ADMIN") {
        dashboardLinks.innerHTML = `
            <a href="#admin_dashboard" data-page="admin_dashboard">Admin Dashboard</a>
        `;
        loadPage("admin_dashboard");  // Redirect directly to the admin dashboard
        loadScript("admin_dashboard.js");  // Dynamically load admin dashboard script

    } else if (role === "ROLE_USER") {
        dashboardLinks.innerHTML = `
            <a href="#user_dashboard" data-page="user_dashboard">User Dashboard</a>
        `;
        loadPage("user_dashboard");  // Redirect directly to the user dashboard
        loadScript("user_dashboard.js");  // Dynamically load user dashboard script

    } else {
        loadPage("login");  // Redirect to login if role is missing
    }
});

// ✅ Function to dynamically load JavaScript files
function loadScript(scriptName) {
    const scriptElement = document.createElement("script");
    scriptElement.src = `/static/js/${scriptName}`; // ✅ Fixed string formatting
    scriptElement.type = "text/javascript";
    scriptElement.defer = true; // ✅ Ensures script loads properly
    document.body.appendChild(scriptElement);
}
