/*
 * Anonymous login
 * 
 * This plugin lets the splash screen display for about a second before
 * announcing that the app is ready to start. There is no login request.
 *
 * Dependencies:
 * This script assumes that jQuery ($) has already been loaded.
 */

console.log("mtwilson-core-html5-login-anon:  start.js loaded");

//setTimeout(function() {
function UserProfile(data) {
    var self = this;
    self.username = ko.observable(data.username);
    self.authorizationToken = ko.observable(data.authorizationToken);
    self.authorizationTokenExpires = ko.observable();
    self.authenticated = ko.observable(false);
    self.error = ko.observable(data.error);
}
function LoginViewModel() {
    var self = this;
    self.userProfile = new UserProfile({});
}

// extern: mainViewModel defiend in mtwilson-core-html5
mainViewModel.loginViewModel = new LoginViewModel();


$.ajax({
    type: "POST",
    url: endpoint + "/login",
    contentType: "application/json",
    headers: {'Accept': 'application/json'},
    data: '{ "username": "anonymous", "password": "" }',
    success: function(data, status, xhr) {
        console.log("Anonymous login results: %O", data);
        /*
         * Example:
         * {"authorization_token":"G4zpaAK426bZNqMTGGGbWVMiYJnd04Iy5DK75J1iVb4="}
         */

        var authorizationToken = data.authorization_token;
        mainViewModel.loginViewModel.userProfile.authorizationToken(authorizationToken);

        // send the authorization token automatically with every ajax request
        $(document).ajaxSend(function(event, jqxhr, settings) {
            console.log("ajaxSend: url: %s", settings.url);
            // check if url starts with /v1  (for example /v1/users)
            //if( settings.url.lastIndexOf('/v1',0) === 0 ) {
            console.log("ajaxSend: accept header: %O", settings.accept);
            console.log("ajaxSend: headers object: %O", settings.headers);
            console.log("ajaxSend: AJAX request to /v1, setting authorization token: " + authorizationToken);
            jqxhr.setRequestHeader("Authorization", "Token " + authorizationToken);
            //}
        });
        $(document).ajaxError(function(jqxhr, status, errorMessage) {
            console.log("Triggered ajaxError handler.");
            console.log("Resource request failed with status: %O", status); // "error"
            console.log("Resource request failed failed with message: %O", errorMessage);
            console.log("Resource request failed: %O", jqxhr);
            //document.location.href = self.options.postLogoutRedirect;
            if (status.status === 401) {
                console.log("Request unauthorized; logging out");
                //self.logout();
            }
            else {
                console.log("Server response: %s", status.statusText);
            }
        });


        // inform the application that we just logged on successfully,
        // application will switch to next screen
        $(document).trigger({
            type: "mtwilson-core-html5:init:ready",
            message: {"username": "anonymous"},
            time: new Date()
        });

    },
    error: function(xhr, status, errorMessage) {
        console.log("Login failed with status: %s", status); // "error"
        console.log("Login failed with message: %s", errorMessage); // "Unauthorized" which is same as xhr.statusText (the http status message)
        console.log("Login failed: %O", xhr);
        // xhr.responseText has the error html page from the server response
        // xhr.status has the http status code like 401
        if (xhr.status == 401) {
            //self.loginRequest.error("Unauthorized");
            console.log("Anonymous login unauthorized");
//                                self.loginRequest({"error":"Unauthorized","username":"","password":"","remember_me":""}); // instead of passing the error message from the server, we need to internationalize this
        }
    }
});

//}, 1000); // previously was delaying this call by 1 second (1000 ms) for quick display of the logo splash screen
