document.addEventListener("DOMContentLoaded", function () {
    console.log("🚀 Navbar Script Loaded");

    // Function to initialize navbar links after navbar is injected
    function initializeNavbar() {
        const navLinks = document.getElementById("navLinks");

        if (!navLinks) {
            console.error("❌ navLinks not found. Navbar not loaded yet!");
            return;
        }

        const userRole = localStorage.getItem("userRole") || "ROLE_USER"; // Default to User
        let transactionsPage = "#user_dashboard"; // Default for Users

        if (userRole === "ROLE_ADMIN") {
            transactionsPage = "#admin_dashboard"; // Change for Admins
        }

        // 🟢 Dynamically Add Links
        navLinks.innerHTML = `
            <li class="nav-item"><a href="${transactionsPage}" class="nav-link">Dashboard</a></li>
            <li class="nav-item"><a href="#logout" class="nav-link" id="logout">Logout</a></li>
        `;

        // 🔴 Attach Logout Event Listener
        document.getElementById("logout").addEventListener("click", function () {
            console.log("🚪 Logging out...");
            localStorage.clear();
            window.location.hash = "#login";
        });

        console.log("✅ Navbar links initialized.");
    }

    // ✅ Wait until the router loads the navbar
    const observer = new MutationObserver(() => {
        if (document.getElementById("navLinks")) {
            observer.disconnect(); // Stop observing once loaded
            initializeNavbar();
        }
    });

    observer.observe(document.body, { childList: true, subtree: true });
});
