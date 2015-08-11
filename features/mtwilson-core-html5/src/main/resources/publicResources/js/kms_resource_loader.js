/*
 * kms_resource_loader.js
 * 
 * Loads specified JS, CSS, HTML resources and maintains cache of loaded resources
 * to prevent redundant downloads. Scripts can use the resource loader to declare
 * their dependencies and invoke their entry function after all dependencies have
 * been loaded.
 * 
 * Example initialization in main application:
 * 
 * var resourceLoader;
 * $(document).ready(function() {
 *     resourceLoader = new ResourceLoader();
 *     resourceLoader.registerLoadedJS();
 *     resourceLoader.registerLoadedCSS();
 * });
 * 
 * 
 * Example of using resource loader from a plugin:
 * 
 * &lt;script src="js/script1.js"&gt;&lt;/script&gt;
 * &lt;script type="application/javascript"&gt;
 * resourceLoader.loadJS(["js/script1.js", "js/script2.js", "js/script3.js"],
 * function() {
 *    alert("script1, script2, and script3 executed before this alert");
 *    alert("script1 was already loaded by the surrounding page and was not reloaded");
 * });
 * &lt;/script&gt;
 * 
 * Scripts using the resource loader must assume that it's already available,
 * and do not need to list it as a dependency (although it won't hurt, because
 * if the surrounding page loaded it externally then it will itself be registered)
 * 
 */

function ResourceLoader() {
    var self = this;
    self.css = {}; // map of url -> css content
    self.js = {}; // map of url -> js content
    self.html = {}; // map of url -> html content

    // actions to execute after html has been inserted, such as applying knockout bindings
    self.postWriteHTML = function(url, intoElement) {
        /*
         if( typeof(intoElement) === 'string' ) {
         console.log("ResourceLoader.postWriteHTML: jquery selector for url: %s", url);                        
         var foundChildElements = $(intoElement).children();
         console.log("ResourceLoader.postWriteHTML found: %O", foundChildElements);
         for(var i=0; i<foundChildElements.length; i++) {
         // automatically apply bindings
         console.log("ResourceLoader.postWriteHTML: applying bindings to child: %O", foundChildElements[i]);
         ko.applyBindings(mainViewModel, foundChildElements[i]);
         }
         }
         */
    };
    self.writeHTML = function(url, intoElement) {
        if (typeof(intoElement) === 'object') {
            // if intoElement is an object, assume it has some parameters for us
            console.log("ResourceLoader.writeHTML jquery object");
            // the "into" option will write the html into a specific container tag
            if (intoElement.hasOwnProperty('into')) {
                if (typeof(intoElement.into) === 'string') {
                    // if intoElement is a string, assume its a jquery selector
                    console.log("ResourceLoader.writeHTML into jquery selector: %s", intoElement.into);
                    var element = $(intoElement.into);
                    if (element.length) {
                        console.log("ResourceLoader.writeHTML into exists %s", intoElement.into);
                        // the "id" option will create or reuse a specific element in the container
                        if (intoElement.hasOwnProperty("tab")) {
                            var tabSelector = "#" + intoElement.tab;
                            var identityElement = element.children(tabSelector);
                            if (!identityElement.length) {
                                // no element with that id exists, so create it
                                console.log("ResourceLoader.writeHTML creating tab %s", intoElement.tab);
                                element.append("<div id='" + intoElement.tab + "' class='tab-pane'></div>");
                                identityElement = element.children(tabSelector);
                            }
                            // now we know the identified element exists,
                            // so replace its content
                            identityElement.html(self.html[url]);
                            self.postWriteHTML(url, intoElement);
                            // and check if called asked to auto-activate the tab
                            if (intoElement.hasOwnProperty('activate')) {
                                if (intoElement.activate) {
                                    mainViewModel.tab(tabSelector, intoElement.into);
                                }
                            }
                        }
                        else {
                            // no other id was specified, so just insert into the "into" container
                            $(intoElement.into).html(self.html[url]);
                            self.postWriteHTML(url, intoElement);
                        }
                    }
                }
            }
            if (intoElement.hasOwnProperty('callback') && typeof(intoElement.callback) === 'function') {
                console.log("ResourceLoader.writeHTML callback");
                var fn = intoElement.callback;
                fn({url: url, html: self.html[url]});
            }
        }
        else if (typeof(intoElement) === 'string') {
            console.log("ResourceLoader.writeHTML string");
            self.writeHTML(url, {into: intoElement});
        }
        else if (typeof(intoElement) === 'function') {
            //  if intoElement is a callback, then call it (function is responsible for applying bindings, or whatever, by itself)
            console.log("ResourceLoader.writeHTML function");
            self.writeHTML(url, {callback: intoElement});
        }
        else {
            console.log("ResourceLoader.writeHTML unsupported 2nd argument: %O", intoElement);
        }
    };
    self.loadHTML = function(url, intoElement) {
        if (self.html[url]) {
            // already loaded it, so continue immediately
            console.log("ResourceLoader.loadHTML already in cache: %s", url);
            self.writeHTML(url, intoElement);
        }
        else {
            // have not loaded it yet, so request from server
            console.log("ResourceLoader.loadHTML downloading: %s", url);
            $.ajax({
                type: "GET",
                url: url, // like dashboard.html
                headers: {'Accept': 'application/html'},
                beforeSend: function(xhr, settings) { xhr.meta = { "url":url }; }, // define a new field "meta" in the xhr with our original request url
                success: function(content, status, xhr) {
                    var url = xhr.meta.url;
                    console.log("Fetch HTML %s results %O",url, content);
                    /*
                     * Example:
                     * <!DOCTYPE html><html><head><title>loaded content</title></head></html>
                     */
                    self.html[url] = content;
                    self.writeHTML(url, intoElement);
                }
            });
        }
    };
    self.postLoadJS = function(request) {
        // check if all the requested scripts have loaded, and if so invoke the callback
        if( request.statusTimerId ) { clearTimeout(request.statusTimerId); request.statusTimerId = null; }
        var url_array = request.urls;
        var ready = true;
        var statusCounts = {};
        for (var i = 0; i < url_array.length; i++) {
            var url = url_array[i];
            var status = (self.js[url] && self.js[url].status ? self.js[url].status : "null");
            if( !statusCounts[ status ] ) { statusCounts[status] = 0; }
            statusCounts[status]++;
            ready = ready && status === "done";
        }
        if (ready) {
            console.log("ResourceLoader.loadJS ready for callback on urls: %O", request.urls);
            if (request.done) {
                console.log("ResourceLoader.loadJS skipping callback; already called before on urls: %O", request.urls);
            }
            else {
                request.done = true;
                request.callback();
            }
        }
        else {
            console.log("ResourceLoader.loadJS not ready to callback for urls: %O", request.urls);
            var status_array = $.map(url_array, function(value, index) {
                return {"index": index, "url": value, "status": (self.js[value] ? self.js[value].status : "unknown")};
            });
            console.log("ResourceLoader.loadJS status: %O", status_array);
            // schedule another check unless the load status is error; this is required when two or more simultaneous load requests are issued that share a dependency, the first one sets status to pending and starts the download, the second one sees a download is in progress and calls postLoadJS just to see but if the download has not completed yet  and we don't schedule another check here it would just fail without trying again
            // TODO: increase the next check time slightly each time we do it (maybe log curve) so that as more resources are loaded we're not trying to do all the checks at the same time
            var currentTimeMs = new Date().getTime();
            var priorTimeMs = request.statusTimerRequestedOn ? request.statusTimerRequestedOn : 0;
            console.log("ResourceLoader.loadJS currentTimeMs = %s, priorTimeMs = %s", currentTimeMs, priorTimeMs);
            console.log("ResourceLoader.loadJS last status check requested %d ms ago", currentTimeMs - priorTimeMs);
            console.log("ResourceLoader.loadJS status counts for urls: %O\n%O", url_array, statusCounts);
            if( !statusCounts["error"] ) {
//            if( self.js[url.status = "error" ) {
                request.statusTimerRequestedOn = currentTimeMs;
                request.statusTimerId = setTimeout(self.postLoadJS, 1000, request);
            }
        }
    };
    self.loadJS = function(url_array, callback) {
        var request = {"urls": url_array, "callback": callback, "done": false}; // done wlll be true when we invoke the callback function
        // first register each url and set a status if it's a new entry
        $.map(url_array, function(url) {
            if (!self.js[url]) {
                self.js[url] = { status: "pending" };
            }
            else if (!self.js[url].status) {
                self.js[url].status = "pending";
            }
        });
        for (var i = 0; i < url_array.length; i++) {
            var url = url_array[i];
            if (self.js[url].status === "pending") {
                // have not loaded it yet, so request from server
                console.log("ResourceLoader.loadJS downloading %s", url);
                // set a value in the cache so that if a second loadJS is executed for the same script, it will skip it
                self.js[url].status = "downloading";
                //$.getScript(url, success);  // we could use this but then we can't submit special headers with token etc.
                $.ajax({
                    type: "GET",
                    url: url, // like dashboard.html
                    headers: {'Accept': 'application/javascript'},
                    beforeSend: function(xhr, settings) { xhr.meta = { "url":url }; }, // define a new field "meta" in the xhr with our original request url
                    success: function(content, status, xhr) {
                        var url = xhr.meta.url; // get it from the request object NOT the outer scope (which might change values before we are called)
                        console.log("Fetch JS url: %s results %O", url, content);
                        console.log("XHR is: %O", xhr);
                        /*
                         * Example:
                         * console.log("loaded javascript file");
                         */
                        self.js[url].status = "executing";
                        try {
                            $.globalEval(content);
                            self.js[url].status = "done";
                            self.postLoadJS(request);
                        }
                        catch(e) {
                            self.js[url].status = "error";
                            self.js[url].error = e;
                            console.log("Error while executing script [%s]: %O", url, e);
                        }
                    },
                    error: function(xhr, jqstatus, httpstatus) {
                        var url = xhr.meta.url; // get it from the request object NOT the outer scope (which might change values before we are called)
                        console.error("Cannot load JS url:%s status:%s", url, httpstatus);
                        console.error("xhr: %O", xhr);
                        self.js[url].status = "error";
                    }
                });
            }
            else {
                // already loaded and executed it (or it's already in progress), so skip it -- we don't want to repeat executing the script
                console.log("ResourceLoader.loadJS already in cache: %s", url);
                // but we do want to check if we've loaded all required files, so we can invoke the callback
                self.postLoadJS(request);
            }
        }
    };
    // returns longest common prefix for all elements in the given array, or empty string if no common prefix  or if array is empty or has just one element, or if after all "null" and "undefined" elements are removed the has zero or one string elements
    // usage:   findPrefix(["str1x", "str2y"])  ==> "str"
    // example: first: https://localhost/js/uuid.js,  last: https://localhost/   result: https://localhost/
    self.findPrefix = function(strs) {
        if (!strs.length || strs.length<2) {
            return "";
        }
        // remove all undefined and null elements to avoid errors below, then do the length check
        strs = strs.filter(function(item){ return typeof item !== "undefined" && item !== null; });
        console.log("findPrefix: after filter, args length is: %d", strs.length);
        // if there are no strings or just one string, then a common prefix cannot be defined so we return empty string
        if (strs.length<2) {
            return "";
        }
        // without sorting entire array, just find what would be the first and last elements in the sorted array
        var first = strs[0];
        var last = strs[strs.length - 1];
        var i;
        //console.log("first: %s,  last: %s", first, last);// for example: 
        for (i = 0; i < strs.length; i++) {
            if (strs[i] < first) {
                first = strs[i];
            }
            if (strs[i] > last) {
                last = strs[i];
            }
        }
        console.log("findPrefix: first=%s", first);
        console.log("findPrefix: first.length=%d", first.length);
        console.log("findPrefix: last=%s", last);
        console.log("findPrefix: last.length=%d", last.length);
        var limit = Math.min(first.length, last.length);
        i = 0;
        while (i < limit && first[i] === last[i]) {
            i++;
        }
        return first.slice(0, i); // return the longest common prefix
    };
    //  registerLoadedJS creates entries in our index for javascript files that were
    // loaded via script tags on the main page; that way plugins can still declare
    // them as dependencies but we won't attempt to reload them 
    self.registerLoadedJS = function() {
        // look for all script tags that have a src attribute
        $("script[src]").each(function(index, element) {
            //console.log("Looking at script tag # %d:  %O", index, element);
            // now if  element.src  starts with element.baseURI then remove that part so we're left with the relative path
            var uri = element.src;
            var prefix = self.findPrefix([element.src, element.baseURI]);
            if (prefix) {
                uri = element.src.slice(prefix.length);
            }
            //console.log("Registering script uri: %s", uri);
            self.js[uri] = {status: "done"};
        });
    };
    self.registerLoadedCSS = function() {
        // look for all script tags that have a src attribute
        $('link[rel="stylesheet"][type="text/css"][href]').each(function(index, element) {
            //console.log("Looking at stylesheet tag # %d:  %O", index, element);
            // now if  element.src  starts with element.baseURI then remove that part so we're left with the relative path
            var uri = element.href;
            var prefix = self.findPrefix([element.href, element.baseURI]);
            if (prefix) {
                uri = element.href.slice(prefix.length);
            }
            //console.log("Registering stylesheet uri: %s", uri);
            self.css[uri] = {status: "done"};
        });
    };
}
