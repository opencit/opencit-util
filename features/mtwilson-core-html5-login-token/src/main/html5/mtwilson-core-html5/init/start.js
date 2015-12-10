/*
 * Anonymous login
 * 
 * This plugin lets the splash screen display for about a second before
 * announcing that the app is ready to start. There is no login request.
 *
 * Dependencies:
 * This script assumes that jQuery ($) has already been loaded.
 * This script assumes that Resource Loader has already been loaded.
 */

console.log("mtwilson-core-html5-login-token:  start.js loaded");

setTimeout(function() {
	resourceLoader.loadHTML("/html5/public/mtwilson-core-html5-login-token/login.html", {
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
