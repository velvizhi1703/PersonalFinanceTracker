window.Router = window.Router || {
	paths: {
		"login": "/pages/login.html",
		"register": "/pages/register.html",
		"users": "/pages/users.html",
		"admin_transactions": "/pages/admin_transactions.html",
		"transaction-history": "/pages/transaction-history.html",
		"admin_users": "/pages/admin_users.html",
		"new-transaction": "/pages/new-transaction.html",
	},

	init: function() {
		if (window.routerInitialized) {
			return;
		}
		window.routerInitialized = true;
		this.handleNavigation();
		window.addEventListener("hashchange", this.handleNavigation);
		window.onpopstate = this.handleNavigation;
	},

	handleNavigation: function() {
		const page = location.hash.replace("#", "") || "login";
		if (!Router.paths[page]) {
			return;
		}
		if (window.lastPage === page) {
			return;
		}

		window.lastPage = page;

		if (window.history.state !== page) {
			history.pushState(page, "", `#${page}`);
		}

		Router.loadPage(page);
	},

	loadPage: function(page) {
		if (!Router.paths[page]) {
			return;
		}

		if (!window.sidebarLoaded && page !== "login" && page !== "register") {
			let sidebarPath = page.startsWith("admin_") ? "/pages/admin_sidebar.html" : "/pages/sidebar.html";
			let sidebarScript = page.startsWith("admin_") ? "/assets/js/admin_sidebar.js" : "/assets/js/sidebar.js";

			$("#sidebar").load(sidebarPath, function() {
				console.log("Sidebar Loaded!");
				$.getScript(sidebarScript, function() {
					console.log("Sidebar JS Executed!");
					window.sidebarLoaded = true;
				});
			});
		}

		if (page === "login" || page === "register") {
			$("#sidebar, #footer").hide();
		} else {
			$("#sidebar, #footer").show();
		}

		if ($("#content").attr("data-loaded") !== page) {
			Router.loadContent(page, "content");
			$("#content").attr("data-loaded", page);
		}
	},

	loadContent: function(page, targetId) {
		if (!Router.paths[page]) return;
		$(`#${targetId}`).html("");
		$.get(Router.paths[page], function(responseText) {
			const parser = new DOMParser();
			const contentDoc = parser.parseFromString(responseText, "text/html");
			const bodyContent = $(contentDoc).find("body").html();

			$(`#${targetId}`).html(bodyContent);
		});
	}
};

$(document).ready(() => {
	if (!window.routerInitialized) {
		Router.init();
	}
});

$(document).on("click", ".nav-link", function(event) {
	event.preventDefault();
	let page = $(this).attr("href").substring(1);
	history.pushState(page, "", `#${page}`);
	Router.handleNavigation();
});