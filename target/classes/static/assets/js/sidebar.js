/**
 * 
 */

$(document).ready(() => {
	// Ensure User Dashboard loads dynamically
	$("#dashboardLink").off().on("click", (event) => {
       event.preventDefault();
	   Router.loadPage("users");
   	});
	
	// Handle "Transactions History" Click
	$("#transactionsHistoryLink").off().on("click", (event) => {
       event.preventDefault();
	   Router.loadPage("transaction-history");
   	});
   
   	// Handle "New Transaction" Click
	$("#newTransactionLink").off().on("click", (event) => {
       event.preventDefault();
	   Router.loadPage("new-transaction");
   	});
   
   	// Handle "Logout" Click
   	$("#userLogoutButton").off().on("click", (event) => {
		event.preventDefault();
	   	Router.loadPage("login");
   	});
});