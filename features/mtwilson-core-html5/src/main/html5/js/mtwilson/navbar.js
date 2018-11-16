/**
 * Depends on:  mtwilson/namespace.js
 */

(function(){
    var navbar = namespace("com.intel.mtwilson.core.html5.navbar");
    
                    // given the buttons in the json, loaded with query in context, insert the buttons into the navbar sorted using fnSort (a function)
                    // fnSort is a function that accepts the feature name and returns the insert position (index starting at zero for far left)
    navbar.insertButtonsIntoNavbar = function(csv, hometab) {
                        if( csv === undefined ) { csv = ""; }
                        var sortOrder = csv.split(",");
                        for(var i=0; i<sortOrder.length; i++) {
                            sortOrder[i] = sortOrder[i].trim();
                        }
                        
                        var navbarButtonsArray = [];
                        // if we already have buttons inserted into the navbar,  add them to the navbarButtonsArray so we consider them when placing new buttons
                        $("#navbar ul.nav li").each(function(index,element) {
                            console.log("insertButtonsIntoNavbar DETECTED EXISTING BUTTON: %s", $(element).attr("data-navbar-feature"));
                            navbarButtonsArray.push($(element).attr("data-navbar-feature"));
                        });
                        
                        // discover navbar buttons
                        discovery.eachJSON("/mtwilson-core-html5/navbar/main.json", function(json,context) {
                            // first make sure that we don't already have this feature inserted...
                            var existingIndexOf = navbarButtonsArray.indexOf(context.entry.feature);
                            if( existingIndexOf > -1 ) { console.log("insertButtonsIntoNavbarFeature %s is already in navbar", context.entry.feature); return; }
                            
                            var indexOfThisFeature = sortOrder.indexOf(context.entry.feature);
                            
                            if( indexOfThisFeature < 0 ) {
                                // not in the list - so it would go in the "more" or "..." section
                                console.log("insertButtonsIntoNavbarFeature: feature %s is not in sort order, should go in 'more' section", context.entry.feature);
                            }
                            
                            // get all the currently loaded buttons and find the insertion point for this button
                            console.log("insertButtonsIntoNavbar indexOfThisFeature %s", indexOfThisFeature);
                            // each feature providing buttons may have one or more buttons in the json, we add them all (in order) into the navbar at the position specified by fnSort
                            for(var i=0; i<json.items.length; i++) {
                                if( indexOfThisFeature < 0 ) {
                                    console.log("Skipping navbar button %s from %s because not in list", context.entry.feature, endpoint+json.items[i].href);
                                    continue;
                                }
                                console.log("Loading navbar button html from: %s", endpoint+json.items[i].href);
                                resourceLoader.loadHTML( endpoint+json.items[i].href, 
                                    /*
                                     * 
                                     * @param {object} button like { url: url, html: self.html[url] }
                                     * @param {object} buttonContext like { context: context, descriptor: json, index: i, item: json.items[i] }
                                     * @returns {undefined} ignored
                                     */
                                    function(button, buttonContext) {
                                    console.log("insertButtonsIntoNavbar navbar main loaded html button: %O", button);
                                    // button is object like {"url":url,"html":html} defined by resource loader's loadHTML method
                                    // before setting the html, we extract the <li> from the <body><ul>...</ul></body> received from server
                                    //console.log("Adding navigation button with html: %s", data.html);
                                    // first add it as a script element
                                    var buttonHtmlScriptElement = $(document.createElement("script"));
                                    buttonHtmlScriptElement.attr("type", "application/html");
                                    buttonHtmlScriptElement.append(button.html);
                                    buttonHtmlScriptElement.find("li").attr("data-navbar-feature", context.entry.feature);
                                    buttonHtmlScriptElement.find("li").attr("data-navbar-feature-button-index", buttonContext.index);
                                    var buttonHtml = buttonHtmlScriptElement.find("ul").html();
                                    buttonHtmlScriptElement.remove();
                                    console.log("insertButtonsIntoNavbar Filtered navigation button html to list item: %s", buttonHtml);
                                    //this.html = ko.observable(buttonHtml);
                                    var insertedButton = false;
                                    var navbarButtons = $("#navbar ul.nav li[data-navbar-feature]");
                                    console.log("insertButtonsIntoNavbarFeature navbarButtons length %d: %O", navbarButtons.length, navbarButtons);
                                    for(var i=0; i<navbarButtons.length && !insertedButton; i++) {
                                        console.log("insertButtonsIntoNavbarFeature INDEX IS %s", i);
                                        var comparisonFeatureName = $(navbarButtons[i]).attr("data-navbar-feature");
                                        console.log("insertButtonsIntoNavbarFeature navbbar button: %s",comparisonFeatureName);
                                        var comparisonFeatureIndex = sortOrder.indexOf(comparisonFeatureName);
                                        // following block is first-level sort: among all features, ensure they are in same order specified by configuration setting
                                        if( comparisonFeatureIndex > indexOfThisFeature ) {
                                            console.log("insertButtonsIntoNavbarFeature FOUND insertion point for %s before %s", context.entry.feature, comparisonFeatureName );
                                            // the first feature that is AFTER this feature is where we stop, and insert before it
                                            $(navbarButtons[i]).before(buttonHtml);
                                            insertedButton = true;
                                        }
                                        // following block is second-level sort: within buttons from the same feature, ensure they are in same order specified in main.json
                                        else if( comparisonFeatureIndex === indexOfThisFeature ) {
                                            // found another button from same feature, so compare the button index order
                                            var comparisonFeatureButtonIndex = $(navbarButtons[i]).attr("data-navbar-feature-button-index");
                                            if( comparisonFeatureButtonIndex > buttonContext.index ) {
                                                $(navbarButtons[i]).before(buttonHtml);
                                                insertedButton = true;
                                            }
                                        }
                                    }
                                    if( !insertedButton && indexOfThisFeature > -1 ) {
                                        // didn't find any element that goes after this one, but it's in the button list, so put it at the end
                                        $("#navbar ul.nav").append(buttonHtml);
                                    }
                                    
        
                                    var activeTab = $("#navbar ul.nav li.active a[href]").first()[0];
                                    console.log("ACTIVE TAB ALREADY? %O", activeTab);
                                    // now the button has been inserted, check if it's the hometab to be automatically activated
                                    
                                    if( hometab ) {
                                        console.log("CHOOSING HOME TAB %s", hometab);
                                        console.log("tabs are: %O", $('#navbar ul.nav li a[href="#' + hometab + '"]'));
                                        
                                        // first make sure there isn't already an active tab selected (user clicked or we already picked a first one)
                                        if( ! activeTab ) {
                                            console.log("NO ACTIVE TAB, CHOOSING CONFIGURED HOMETAB");
                                            $('#navbar ul.nav li a[href="#' + hometab + '"]').first().tab('show');
                                        }
                                        
                                    }
                                    else {
                                        // if no hometab is configured, select the first tab
                                        if( ! activeTab ) {
                                            console.log("NO ACTIVE TAB, CHOOSING FIRST ONE");
                                            $('#navbar ul.nav li a[href]').first().tab('show');
                                        }
                                    }
                                    
                                    
                                    $('#navbar ul.nav li a').each(function() { 
                                        var self = $(this);
                                        if( self.attr("data-click-handler") !== "customized" ) {
                                            console.log("NEW ACTION REVISED");
                                            self.click(function(e) {
                                                e.preventDefault();
                                                $(this).tab("show");
                                                var href = $(this).attr("href");
                                                if( href ) {
                                                    href = href.replace(/\./g, "\\."); 
                                                    $("#main > .tab-pane").removeClass("active");
                                                    $(href).addClass("active");
                                                }
                                                else {
                                                    console.log("Cannot activate tab pane - no href in link");
                                                }
                                                console.log("NEW ACTION CLICK REVISED");
                                            });
                                            self.attr("data-click-handler", "customized");
                                        }
                                    });
                                    
                                    
                                },  // end of loadHTML callback function
                                // extra callback args provided to loadHTML callback function as second buttonContext parameter:
                                { context: context, descriptor: json, index: i, item: json.items[i] });
                            }
                        });
                        
                    };
                    
    
})();
