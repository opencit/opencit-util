            function NavigationBarButton(data) {
                this.url = ko.observable(data.url);
                // before setting the html, we extract the <li> from the <body><ul>...</ul></body> received from server
                //console.log("Adding navigation button with html: %s", data.html);
                // first add it as a script element
                var buttonHtmlScriptElement = $(document.createElement("script"));
                buttonHtmlScriptElement.attr("type", "application/html");
                buttonHtmlScriptElement.append(data.html);
                var buttonHtml = buttonHtmlScriptElement.find("ul").html();
                buttonHtmlScriptElement.remove();
                console.log("Filtered navigation button html to list item: %s", buttonHtml);
                //this.html = ko.observable(buttonHtml);
                //console.log("THE NAVBAR IS THERE??? %O", $("#navbar").find("ul"));//
//                $("#navbar").find("ul");
                $("#navbar").find("ul").append(buttonHtml);
                console.log("Added button to navbar: %s", buttonHtml);
            }


function NavigationViewModel() {
                var self = this;
                self.items = ko.observableArray([]);
                
//                self.loginRequest = ko.observable(new LoginRequest({}));
//                self.userProfile = ko.observable(new UserProfile({}));
                // operations
                
            }



                /*
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
*/