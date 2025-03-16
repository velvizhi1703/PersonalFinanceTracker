// Ensure Router is only defined once globally
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

    init: function () {
        if (window.routerInitialized) {
            console.log("⚠ Router already initialized, skipping...");
            return;
        }
        window.routerInitialized = true;
        console.log("✅ Router Initialized!", this.paths);
        
        this.handleNavigation();
        window.addEventListener("hashchange", this.handleNavigation);
        window.onpopstate = this.handleNavigation; // Handle browser back/forward
    },

    handleNavigation: function () {
        const page = location.hash.replace("#", "") || "login";
        console.log("🔍 Current Hash:", location.hash);
        console.log("🔍 Extracted Page:", page);
        console.log("🔍 Available Paths:", Router.paths);

        if (!Router.paths[page]) {
            console.error(`❌ Page "${page}" not found in paths.`);
            return;
        }

        if (window.lastPage === page) {
            console.log("⚠️ Same page reload detected. Skipping...");
            return;
        }

        window.lastPage = page;

        // ✅ Update browser history without causing a reload
        if (window.history.state !== page) {
            history.pushState(page, "", `#${page}`);
        }

        Router.loadPage(page);
    },

    loadPage: function (page) {
        if (!Router.paths[page]) {
            console.error(`❌ Page "${page}" not found in paths.`);
            return;
        }

        // ✅ Load Sidebar Only Once After Login
        if (!window.sidebarLoaded && page !== "login" && page !== "register") {
            console.log("✅ Loading Sidebar...");
            let sidebarPath = page.startsWith("admin_") ? "/pages/admin_sidebar.html" : "/pages/sidebar.html";
            let sidebarScript = page.startsWith("admin_") ? "/assets/js/admin_sidebar.js" : "/assets/js/sidebar.js";

            $("#sidebar").load(sidebarPath, function () {
                console.log("✅ Sidebar Loaded!");
                $.getScript(sidebarScript, function () {
                    console.log("✅ Sidebar JS Executed!");
                    window.sidebarLoaded = true; // Prevent sidebar from reloading
                });
            });
        } else {
            console.log("⚡ Sidebar Already Loaded, Skipping...");
        }

        // ✅ Hide Sidebar/Footer on Login/Register
        if (page === "login" || page === "register") {
            $("#sidebar, #footer").hide();
        } else {
            $("#sidebar, #footer").show();
        }

        // ✅ Load Main Content Dynamically
        if ($("#content").attr("data-loaded") !== page) {
            Router.loadContent(page, "content");
            $("#content").attr("data-loaded", page);
        }
    },

    loadContent: function (page, targetId) {
        if (!Router.paths[page]) return;
        $(`#${targetId}`).html("");
        $.get(Router.paths[page], function (responseText) {
            const parser = new DOMParser();
            const contentDoc = parser.parseFromString(responseText, "text/html");
            const bodyContent = $(contentDoc).find("body").html();

            $(`#${targetId}`).html(bodyContent);
        });
    }
};

// ✅ Ensure Router.init() runs only once
$(document).ready(() => {
    if (!window.routerInitialized) {
        console.log("✅ Checking Router paths before init:", Router.paths);
        Router.init();
    }
});

// ✅ Ensure Sidebar Links Work Properly (SPA Navigation)
$(document).on("click", ".nav-link", function (event) {
    event.preventDefault();
    let page = $(this).attr("href").substring(1); // Remove #
    console.log("🔹 Navigating to:", page);
    history.pushState(page, "", `#${page}`);
    Router.handleNavigation();
});
