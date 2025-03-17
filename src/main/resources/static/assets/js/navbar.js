document.addEventListener("DOMContentLoaded", function() {
	function initializeNavbar() {
		const navLinks = document.getElementById("navLinks");
		if (!navLinks) {
			return;
		}

		const userRole = localStorage.getItem("userRole") || "ROLE_USER";
		let transactionsPage = "#user_dashboard";

		if (userRole === "ROLE_ADMIN") {
			transactionsPage = "#admin_dashboard";
		}

		navLinks.innerHTML = `
            <li class="nav-item"><a href="${transactionsPage}" class="nav-link">Dashboard</a></li>
            <li class="nav-item"><a href="#logout" class="nav-link" id="logout">Logout</a></li>
        `;

		document.getElementById("logout").addEventListener("click", function() {
			localStorage.clear();
			window.location.hash = "#login";
		});
	}

	const observer = new MutationObserver(() => {
		if (document.getElementById("navLinks")) {
			observer.disconnect();
			initializeNavbar();
		}
	});

	observer.observe(document.body, { childList: true, subtree: true });
});
