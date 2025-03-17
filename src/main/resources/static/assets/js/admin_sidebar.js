$(document).ready(() => {
	$("#transactionsLink").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("admin_transactions");
	});

	$("#usersLink").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("admin_users");
	});

	$("#adminLogoutButton").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("login");
	});
});
