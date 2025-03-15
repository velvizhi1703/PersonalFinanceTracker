$(document).ready(() => {
    
    $("#transactionsLink").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("admin_transactions");
    });

    // Handle "Users" Click
    $("#usersLink").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("admin_users");
    });

    // Handle "Categories" Click
    $("a[href='#categories']").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("admin_categories");
    });

    // Handle "New Category" Click
    $("a[href='#new_category']").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("admin_new_category");
    });

    // Handle "Settings" Click
    $("a[href='#settings']").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("admin_settings");
    });

    // Handle "Logout" Click
    $("#adminLogoutButton").off().on("click", (event) => {
        event.preventDefault();
        Router.loadPage("login");
    });
});
