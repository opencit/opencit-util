/*
 * Password login with tokens
 * 
 * This plugin lets the splash screen display for about a second before
 * showing a login screen. Submitting the login form with username and
 * password will authenticate with server to receive a login token that
 * is also used for CSRF protection.
 *
 * Dependencies:
 * This script assumes that jQuery ($) has already been loaded.
 * This script assumes that Resource Loader has already been loaded.
 */

console.log("mtwilson-core-html5-login-token:  start.js loaded");

setTimeout(function() {
	resourceLoader.loadHTML("/v1/html5/public/com.intel.mtwilson.core.login.token/login.html", {
		into: "#main",
		tab: "login",
		activate: true // means switch to this tab as soon as its loaded; same as providing callback: function() { mainViewModel.tab("#login", "#main"); }
	});
}, 1000); // delay this call by 1 second for quick display of the logo splash screen

/*
this needs to happen after user has logged in:
	$(document).trigger({
		type: "mtwilson-core-html5:init:ready",
        message: {"username":"USERNAME GOES HERE"},
		time: new Date()
	});
*/
