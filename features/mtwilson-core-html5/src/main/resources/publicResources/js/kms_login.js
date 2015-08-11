// TODO: move this into a separate javascript file, and then into an API that
//       we can call so the server will tell us about installed plugins
var KMS_PAGES = [
    { "href": "navbar.html", "target": "#navbar" },
    { "href": "dashboard.html", "target": "#main", "target_tab": "dashboard" },  // TODO:  target_tab should be calculated automatically or generated and synchronized with the links ... the links should be generated from this list too...
    { "href": "settings.html", "target": "#main", "target_tab": "settings" },
    { "href": "help.html", "target": "#main", "target_tab": "help" },
    { "href": "logout.html", "target": "#main", "target_tab": "logout" },
    { "href": "license.html", "target": "#main", "target_tab": "license" },
    { "href": "/v1/resources/profile.html", "target": "#main", "target_tab": "my_profile" },
    { "href": "/v1/resources/users.html", "target": "#main", "target_tab": "users" },
    { "href": "/v1/resources/saml_certificates.html", "target": "#main", "target_tab": "saml_certificates" },
    { "href": "/v1/resources/tpm_identity_certificates.html", "target": "#main", "target_tab": "tpm_identity_certificates" }
];

function UserProfile(data) {
    this.username = ko.observable(data.username);
    this.authorizationToken = ko.observable(data.authorizationToken);
    this.authenticated = ko.observable(false);
    this.error = ko.observable(data.error);
}

function LoginRequest(data) {
    this.username = ko.observable(data.username);
    this.password = ko.observable(data.password);
    this.remember_me = ko.observable(data.remember_me);
    this.error = ko.observable(data.error);
}

function LoginViewModel() {
    var self = this;
    self.loginRequest = new LoginRequest({});
    self.userProfile = new UserProfile({});
    self.options = {
        "postLoginActivatePage": "dashboard.html",  // dashboard should be the first view we show after login; dashboard can then load the "run once" or "are there notifications" code.
        "postLogoutRedirect": "index.html" //  where to send user after logout is done
    };

    // operations
    self.login = function(loginFormElement) {
        console.log("Endpoint: %s", endpoint);
        //            console.log("Search keys 1: %O", ko.toJSON(searchCriteriaItem)); //   results in error: InvalidStateError: Failed to read the 'selectionDirection' property from 'HTMLInputElement': The input element's type ('hidden') does not support selection
        console.log("Login request to %s: %O", endpoint+"/login", ko.toJSON(self.loginRequest)); // attempting to serialize loginRequestItem produces errors, probably because it represents the entire form
//                    console.log("Login request form: %O", loginFormElement); // we could use jquery validation with the form element
        $.ajax({
            type: "POST",
            url: endpoint + "/login",
//                        accept: "application/json",
            contentType: "application/json",
            headers: {'Accept': 'application/json'},
            data: ko.toJSON(self.loginRequest), //$("#loginForm").serialize(), 
            success: function(data, status, xhr) {
                console.log("Login results: %O", data);
                /*
                 * Example:
                 * {"authorization_token":"G4zpaAK426bZNqMTGGGbWVMiYJnd04Iy5DK75J1iVb4="}
                 */
                
                var authorizationToken = data.authorization_token;
                
                self.userProfile.username(self.loginRequest.username);
                self.userProfile.authorizationToken(authorizationToken);
                self.userProfile.authenticated(true);
                
                // send the authorization token automatically with every ajax request
                $(document).ajaxSend(function(event,jqxhr,settings){
                    console.log("ajaxSend: url = "+settings.url);
                    // check if url starts with /v1  (for example /v1/users)
                    if( settings.url.lastIndexOf('/v1',0) === 0 ) {
                        console.log("ajaxSend: accept header: %O", settings.accept);
                        console.log("ajaxSend: headers object: %O", settings.headers);
                        console.log("ajaxSend: AJAX request to /v1, setting authorization token: "+authorizationToken);
                        jqxhr.setRequestHeader("Authorization", "Token "+authorizationToken);
                    }
                });
                
                
                // load the navbar and the dashboard, and activate the post-login primary view
                
                var nextView = self.options.postLoginActivatePage; // "dashboard.html";
                for(var i=0; i<KMS_PAGES.length; i++) {
                    var viewDescriptor = KMS_PAGES[i]; // { href: ...,  target: "#main",  target_tab: "some-id" }
                    console.log("post login loading page: %O", viewDescriptor);
                    var loadOptions = { into: viewDescriptor.target, tab: null, activate: null };
                    if( viewDescriptor.target_tab ) {
                        loadOptions.tab = viewDescriptor.target_tab;
                    }
                    loadOptions.activate = (nextView === viewDescriptor.href); // automatically activate the page specifeid by the "postLoginActivatePage" option
                    resourceLoader.loadHTML( viewDescriptor.href , loadOptions );
                }
                
/*
                resourceLoader.loadHTML("navbar.html",
                        {into: "#navbar"
                }
                        
                        );
                        
                resourceLoader.loadHTML("dashboard.html",
                        {into: "#main",
                            tab: "dashboard",
                            activate: true // means switch to this tab as soon as its loaded; same as providing callback: function() { mainViewModel.tab("#login", "#main"); }
                        });
                        
                resourceLoader.loadHTML("settings.html",
                        {into: "#main",
                            tab: "settings",
                            activate: false // don't switch to it right away; load it in background
                        });
                resourceLoader.loadHTML("help.html",
                        {into: "#main",
                            tab: "help",
                            activate: false // don't switch to it right away; load it in background
                        });
                resourceLoader.loadHTML("profile.html",
                        {into: "#main",
                            tab: "my_profile",
                            activate: false // don't switch to it right away; load it in background
                        });
                resourceLoader.loadHTML("logout.html",
                        {into: "#main",
                            tab: "logout",
                            activate: false // don't switch to it right away; load it in background
                        });
                resourceLoader.loadHTML("license.html",
                        {into: "#main",
                            tab: "license",
                            activate: false // don't switch to it right away; load it in background
                        }); */
                        
            },
            error: function(xhr, status, errorMessage) {
                console.log("Login failed with status: %s", status); // "error"
                console.log("Login failed with message: %s", errorMessage); // "Unauthorized" which is same as xhr.statusText (the http status message)
                console.log("Login failed: %O", xhr);
                // xhr.responseText has the error html page from the server response
                // xhr.status has the http status code like 401
                if (xhr.status == 401) {
                    self.loginRequest.error("Unauthorized");
//                                self.loginRequest({"error":"Unauthorized","username":"","password":"","remember_me":""}); // instead of passing the error message from the server, we need to internationalize this
                }
            }
        });
        
    self.logout = function(logoutFormElement) {
        self.userProfile.authenticated(false);
        self.userProfile.authorizationToken(null);
        console.log("Logout, authenticated=%O, authorizationToken=%O", self.userProfile.authenticated(), self.userProfile.authorizationToken());
        // instead of trying to clear all data, just reload the page now to the index
        window.location = self.options.postLogoutRedirect; // "index.html";
    };
        
    };

}
