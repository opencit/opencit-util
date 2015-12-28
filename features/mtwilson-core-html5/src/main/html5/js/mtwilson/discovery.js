/*
 * discovery.js
 * 
 * Discovers features that implement a specified extension point.
 * Provides a list of URLs that must be loaded, either through resource loader
 * or some other mechanism. 
 * 
 * Example initialization in main application:
 * 
 * var discovery;
 * $(document).ready(function() {
 *     discovery = new FeatureDiscovery();
 * });
 * 
 * 
 * Example of using discovery:
 * 
 * &lt;script type="application/javascript"&gt;
 * discovery.all("/mtwilson-core-html5/menubar/buttons.json",
 * function() {
 *    alert("all features implementing menubar buttons found before executing this (sync)");
 * });
 * &lt;/script&gt;
 * 
 * &lt;script type="application/javascript"&gt;
 * discovery.each("/mtwilson-core-html5/menubar/buttons.json",
 * function() {
 *    alert("this alert shown once for each feature that implements menubar buttons (async)");
 * });
 * &lt;/script&gt;
 * 
 * Scripts using discovery should explicitly require it via the resource loader.
 * 
 * ASSUMPTION: "endpoint" variable is already defined by enclosing app (like "/v1" or "http://1.2.3.4/v1")
 */

function FeatureDiscoverKeyValue(key,value) {
    this.key = key;
    this.value = value;
}
FeatureDiscoverKeyValue.prototype.toString = function() {
    return encodeURIComponent(this.key) + "=" + encodeURIComponent(this.value);
};

function FeatureDiscovery() {
    var self = this;
    self.cache = {}; // map of extension point (path) -> directory listing

    self.getFilenameExtension = function(filename) {
        var re = /(?:\.([^./]+))?$/;
        var ext = re.exec(filename)[1]; // if filename is "foo.txt" then ext == "txt"
        return ext;
    };
    
    /**
     * Gets the a listing of extensions for path and invokes a callback for
     * each one as soon as its loaded (async)
     * @param {string} path should start with leading /
     * @param {function} callback to invoke for each discovered extension
     * @param {object} optional object with field "callback_args" (extra arguments to pass to callback)
     * @returns {undefined}
     */
    self.each = function(path, callback, options) {
        if( !options ) { options = { "callback_args": null }; }
        var directoryCallbackContext = { "path": path, "callback": callback, "callback_args": options["callback_args"] };
        self.directory({"feature":null,"path":path}, (function(context) { return function(obj) { 
            var criteria = obj.query;
            var listing = obj.directory;
            // this callback will be invoked when the directory listing has been retrieved
            // and is responsible for loading each of the listed resources 
            // and doing something with each one of those as its loaded (async)
            console.log("discovery.each.callback called for path: %s", criteria.path);
            // the listing looks like this:  { "entries": [ {...}, {...}, ... ] }
            // where each of the {...} objects looks like this:
            // { "feature":"feature-id", "directory":"/namespace/ext/point", "name":"button.json",
            //   "links": [ { "rel":"download", href="uri" } ]
            // }
            // So we need to call the resource loader with the link
            if( listing["entries"] ) {
                console.log("listing has %s entries", listing.entries.length);
                for(var i=0; i<listing.entries.length; i++) {
                    var entry = listing.entries[i];
                    if( entry["links"] ) {
                        var link = self.getDownloadLink(entry);
                        if( link ) {
                            if( context["callback"] && typeof(context["callback"]) === "function" ) {
                                var callback = context["callback"];
                                console.log("discovery.each invoking callback for link: %s", link["href"]);
                                var entryCallbackContext = { "path": context["path"], "query": obj["query"], "directory": obj["directory"], "callback_args": context["callback_args"] };
                                callback(entry, entryCallbackContext);
                            }
                            else {
                                console.log("discovery.each without callback for query: %O", obj["query"]);  //  query is { "feature":feature, "path":path }
                                /*
                                var ext = self.getFilenameExtension(link["href"]);
                                if( ext === "js" ) { ... }
                                if( ext === "json" ) { ... }
                                if( ext === "xml" ) { ... }
                                */
                            }
                        }
                    }
                }
            }
            
        }; })(directoryCallbackContext)
        );
    };
    
    /**
     * Assumes each directory entry has only one download link in accordance
     * with the current directory API definition.
     * 
     * @param {type} directoryEntry
     * @returns {FeatureDiscovery.self.getDownloadLink.link}
     */
    self.getDownloadLink = function(directoryEntry) {
        var links = self.getDownloadLinks(directoryEntry);
        if( links && links.length > 0 ) {
            return links[0];
        }
        console.log("directory entry does not have a download link: %O", directoryEntry);
        return null;
    };

    self.getDownloadLinks = function(directoryEntry) {
        var links = [];
        if( directoryEntry["links"] ) {
            for(var i=0; i<directoryEntry.links.length; i++) {
                var link = directoryEntry.links[i];
                if( link["rel"] === "download" ) {
                    links.push(link);
                }
            }
        }
        console.log("directory entry has %d download links", links.length);
        return links;
    };

    self.getAllDownloadLinks = function(directoryListing) {
        var links = [];
        if( directoryListing["entries"] ) {
            for(var a=0; a<directoryListing.entries.length; a++) {
                var entryLinks = self.getDownloadLinks(directoryListing.entries[a]);
                for(var i=0; i<entryLinks.length; i++) {
                    links.push(entryLinks[i]);
                }
            }
        }
        console.log("directory entry has %d download links", links.length);
        return links;
    };
    
    /**
     * 
     * @param {string} path (required)
     * @param {function} callback (required) to invoke when JSON has been loaded; it receives the following parameters:   json content,  and second argument is object { "url": url of json, "path": extension point identifier, "query": directoryQuery, "directory": directoryResult, "entry": directoryEntry, "callback_args": options["callback_args"]  } 
     * @param {object} options can include "callback_args" to pass to callback
     * @returns {undefined}
     */
    self.eachJSON = function(path, callback, options) {
        if( !path ) { throw new Error("Discovery: path is required"); }
        if( !callback || typeof(callback) !== "function" ) { throw new Error("Discovery: callback function is required"); }
        if( !options ) { options = { "callback_args": null }; }
        var eachCallbackContext = { "path": path, "callback": callback, "callback_args": options["callback_args"], "options": options };
        self.each(path, (function(context) { return function(entry, entryCallbackContext) {
            // context includes "query", "directory", "callback", and optional "callback_args"
            console.log("eachJSON received entry: %O", entry);
            var link = self.getDownloadLink(entry);
            if( link ) {
                var loadCallbackContext = { "path": context["path"], "query": entryCallbackContext["query"], "directory": entryCallbackContext["directory"], "entry": entry, "callback_args": context["callback_args"], "options": entryCallbackContext["options"] };
                /*
                 * The loadJSON callback receives an array of { "url": url, "content": content } but in this case we know that
                 * the caller is asking for a callback for each JSON result to be invoked separately, so we simplify the callback
                 * signature to be just (json, context)  and move the url into the context. 
                 * Also we know the result_array will only have one element because we're invoking loadJSON with only one url to load.
                 */
                var callback = function(result_array, callback_args) { 
                    var result = result_array[0]; 
                    callback_args["url"] = result["url"]; 
                    console.log("Invoking eachJSON callback with json: %O and context: %O", result.content, callback_args); 
                    context.callback(result.content, callback_args);
                };
                resourceLoader.loadJSON([endpoint+link["href"]], callback, { "callback_args": loadCallbackContext } );
            }
        }; } )(eachCallbackContext), options);
    };
    
    self.eachJS = function(path, callback, options) {
        if( !path ) { throw new Error("Discovery: path is required"); }
        if( !callback || typeof(callback) !== "function" ) { throw new Error("Discovery: callback function is required"); }
        if( !options ) { options = { "callback_args": null }; }
        var eachCallbackContext = { "path": path, "callback": callback, "callback_args": options["callback_args"], "options": options };
        self.each(path, (function(context) { return function(entry, entryCallbackContext) {
            // context includes "query", "directory", "callback", and optional "callback_args"
            console.log("eachJS received entry: %O", entry);
            var link = self.getDownloadLink(entry);
            if( link ) {
                // the "path" is already present in entryCallbackContext["query"]["path"], so maybe not necessary to include it separately, but it's the first arg to the each method, and there's a possibility in the future the query might have different args
                var loadCallbackContext = { "path": context["path"], "query": entryCallbackContext["query"], "directory": entryCallbackContext["directory"], "entry": entry, "callback_args": context["callback_args"], "options": entryCallbackContext["options"] };
                /*
                 * The loadJS callback receives an array of { "url": url, "content": content } but in this case we know that
                 * the caller is asking for a callback for each JSON result to be invoked separately, so we simplify the callback
                 * signature to be just (json, context)  and move the url into the context. 
                 * Also we know the result_array will only have one element because we're invoking loadJSON with only one url to load.
                 * 
                 * Context argument is expected to be { "urls": request["urls"] }
                 */
                var callback = function(callback_args, requestContext) { 
                    console.log("Invoking eachJS callback with args: %O and context: %O", callback_args, requestContext); 
                    context.callback(callback_args, requestContext);
                };
                resourceLoader.loadJS([endpoint+link["href"]], callback, loadCallbackContext);
            }
        }; } )(eachCallbackContext), options);
       
    };
    
    self.allJSON = function(path, callback) {
        self.all(path, function(context) {
            console.log("allJSON received context: %O", context);
            var links = self.getAllDownloadLinks(context.directory);
            var urls = [];
            for(var i=0; i<links.length; i++) {
                if( links[i]["rel"] === "download" ) {
                    console.log("found download link: %s", links[i]["href"]);
                    urls.push(endpoint+links[i]["href"]);
                }
            }
            if( urls.length > 0 ) {
                console.log("discovery.allJSON calling resource loader for %s urls", urls.length);
                // the "wait for all to load then invoke callback" feature is actually in loadJS, when we provide an array of urls to load
                resourceLoader.loadJSON(urls, (function(arg1){ return function() { callback(arg1); } })(context));
            }            
        });
    };
    
    /**
     * Gets the a listing of extensions for path and invokes a callback for
     * each one only after all have loaded (sync)
     * 
     * @param {type} path  should start with leading /
     * @param {type} callback
     * @returns {undefined}
     */
    self.all = function(path, callback) {
        self.directory({"feature":null,"path":path}, function(obj) { 
            var criteria = obj.query;
            var listing = obj.directory;
            // this callback will be invoked when the directory listing has been retrieved
            // and is responsible for loading each of the listed resources 
            // and doing something with each one of those after all have loaded (sync)
            console.log("discovery.all.callback called for path: %s", criteria.path);
            // the listing looks like this:  { "entries": [ {...}, {...}, ... ] }
            // where each of the {...} objects looks like this:
            // { "feature":"feature-id", "directory":"/namespace/ext/point", "name":"button.json",
            //   "links": [ { "rel":"download", href="uri" } ]
            // }
            // So we need to call the resource loader with the link
            var urls = [];
            if( listing["entries"] !== null ) {
                console.log("listing has %s entries", listing.entries.length);
                for(var i=0; i<listing.entries.length; i++) {
                    var entry = listing.entries[i];
                    if( entry["links"] !== null ) {
                        for(var j=0; j<entry.links.length; j++) {
                            var link = entry.links[j];
                            if( link["rel"] === "download") {
                                console.log("found download link: %s", link["href"]);
                                urls.push(endpoint+link["href"]);
                            }
                        }
                    }
                }
            }
            if( urls.length > 0 ) {
                console.log("discovery.all calling resource loader for %s urls", urls.length);
                // the "wait for all to load then invoke callback" feature is actually in loadJS, when you provide an array of urls to load
                resourceLoader.loadJS(urls, (function(arg1){ return function() { callback(arg1); } })(obj));
            }
        });
    };
    
    
    
    /**
     * 
     * @param {type} directoryFilterCriteria object containing "feature" (optional) to restrict search to a specific feature, and "path" (required) to restrict search to a specific extension point
     * @returns {undefined}
     */
    self.directory = function(directoryFilterCriteria, callback) {
        if( !directoryFilterCriteria ) { throw new Error("Discovery: search criteria required"); }
        if( !callback || typeof(callback) !== "function" ) { throw new Error("Discovery: callback function is required"); }
        
        // adjust filter criteria for public resource requests;  shorthand is to use /public prefix in the URL and needs to be converted to public:true and no /public prefix in URL
        var isPublic = false;
        if( typeof directoryFilterCriteria["path"] === "string" && directoryFilterCriteria["path"].indexOf("/public") === 0 ) {
            isPublic = true;
            directoryFilterCriteria["path"] = directoryFilterCriteria["path"].substr("/public".length);
            directoryFilterCriteria["public"] = true;
            console.log("edited path for public resource discovery: %s", directoryFilterCriteria["path"]);
        }
        
        console.log("discovery.directory with endpoint %s and criteria %O", endpoint, ko.toJSON(directoryFilterCriteria));
        var hash = self.hash(directoryFilterCriteria);
        console.log("hash of criteria: %s", hash);
        if( self.cache[hash] ) {
            console.log("discovery for %s already cached: %O", hash, self.cache[hash]);
            callback(self.cache[hash]);
        }
        $.ajax({
            type: "GET",
            url: endpoint + ( isPublic ? "/html5/public/directory" : "/html5/directory" ),
            //accept: "application/json",
            headers: {'Accept': 'application/json'},
            data: directoryFilterCriteria,
            success: function(responseJsonContent, status, xhr) {
                console.log("Directory results: %O", responseJsonContent);
                /*
                 * Example:
                 * {"entries":[{"feature":"mtwilson-core-feature","directory":"/menubar","name":"buttons.json","size":0,"links":[{"rel":"file","href":"/html5/directory?feature=mtwilson-core-feature&path=/menubar/buttons.json"},{"rel":"download","href":"/html5/features/mtwilson-core-feature//menubar/buttons.json"}]},{"feature":"mtwilson-core-version","directory":"/menubar","name":"buttons.json","size":21,"links":[{"rel":"file","href":"/html5/directory?feature=mtwilson-core-version&path=/menubar/buttons.json"},{"rel":"download","href":"/html5/features/mtwilson-core-version//menubar/buttons.json"}]}],"faults":[]}
                 */
                self.cache[hash] = { "query": directoryFilterCriteria, "directory": responseJsonContent };
                callback(self.cache[hash]);                
            }
        });
    };
    
    /**
     * Produces a unique value representing the given directory filter criteria.
     * This is used to look up prior directory listings in the cache.
     * 
     * Currently implemented as jQuery $.param(object) to serialize it into
     * a string. 
     * 
     * @param {type} directoryFilterCriteria
     * @returns {undefined}
     */
    self.hash = function(directoryFilterCriteria) {
        var sortedQueryParams = [];
        for(var key in directoryFilterCriteria) {
            if(directoryFilterCriteria.hasOwnProperty(key)) {
                sortedQueryParams.push(new FeatureDiscoverKeyValue(key, directoryFilterCriteria[key]));
            }
        }
        sortedQueryParams.sort(function(a,b) { return a.key < b.key ? -1 : 1 });
        return sortedQueryParams.join("&");
    };
}
