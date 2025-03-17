
$(document).ready(() => {
	$("#dashboardLink").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("users");
	});
	$("#transactionsHistoryLink").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("transaction-history");
	});

	$("#newTransactionLink").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("new-transaction");
	});

	$("#userLogoutButton").off().on("click", (event) => {
		event.preventDefault();
		Router.loadPage("login");
	});
});