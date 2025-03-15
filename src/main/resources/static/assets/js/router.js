const Router = {
    paths: {
        "login": "/pages/login.html",
        "register": "/pages/register.html",
        "users": "/pages/users.html",
        "admin_transactions": "/pages/admin_transactions.html",
        "transaction-history": "/pages/transaction-history.html",
		"admin_users":"/pages/admin_users.html"
    },
	
	

    init: function () {
		console.log("Router Initialized!", this.paths);
        this.handleNavigation();
        window.addEventListener("hashchange", this.handleNavigation);
    },

    handleNavigation: function () {
        const page = location.hash.replace("#", "") || "login";
        console.log(`Navigating to: ${page}`);
		if (window.lastPage === page) {
		           console.log("⚠️ Same page reload detected. Skipping...");
		           return; // ✅ Prevent duplicate navigation
		       }
		       window.lastPage = page; 
			   history.pushState(null, "", `#${page}`);
        Router.loadPage(page);
    },

    loadPage: function (page) {
        if (!Router.paths[page]) {
            console.error(`Page "${page}" not found in paths.`);
            return;
        }

        // Load the main content
		if (page.startsWith("admin_")) {
		    console.log("✅ Loading admin sidebar...");
		    if ($("#sidebar").attr("data-type") !== "admin") {  // ✅ Only reload if not already admin sidebar
		        $("#sidebar").attr("data-type", "admin").load("/pages/admin_sidebar.html", function () {
		            console.log("✅ Admin sidebar loaded!");
		            $.getScript("/assets/js/admin_sidebar.js");
		        });
		    }
		} else if (page !== "login" && page !== "register") {
		    console.log("✅ Loading user sidebar...");
		    if ($("#sidebar").attr("data-type") !== "user") {  // ✅ Only reload if not already user sidebar
		        $("#sidebar").attr("data-type", "user").load("/pages/sidebar.html", function () {
		            console.log("✅ User sidebar loaded!");
		            $.getScript("/assets/js/sidebar.js");
		        });
		    }
		}

		      // Show/hide sidebar and footer based on the page
		      if (page === "login" || page === "register") {
		          $("#sidebar, #footer").hide();
		      } else {
		          $("#sidebar, #footer").show();
		      }

		      // ✅ Only Load Page Content if NOT Sidebar or Footer
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

		            // Set loaded content
		            $(`#${targetId}`).html(bodyContent);
		        });
		    }
		};

		$(document).ready(() => {
			if (!window.routerInitialized) {
			       window.routerInitialized = true; // ✅ Prevent multiple Router.init() calls
			       Router.init();
			   }
		});