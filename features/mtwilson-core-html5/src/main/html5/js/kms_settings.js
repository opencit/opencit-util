/**
 * 
 * @param propertyName default sort order is ascending but you can pass "+propertyName" for explicitly ascending or "-propertyName" for descending
 * @return {function} a comparison function that operates on the named property in its two arguments
 */
function sortBy(propertyName) {
    var sortOrder = 1; // default ascending
    if( propertyName[0] === "+" ) {
        sortOrder = 1; // explicitly ascending
        propertyName = propertyName.substr(1);
    }
    if( propertyName[0] === "-" ) {
        sortOrder = -1; // descending
        propertyName = propertyName.substr(1);
    }
    return function(a,b) {
        var result = 0;
        if( a[propertyName] < b[propertyName]) { result = -1; }
        if( a[propertyName] > b[propertyName]) { result = 1; }
        return result * sortOrder;
    };
}

            function Setting(data) {
                this.name = ko.observable(data.name);
                this.value = ko.observable(data.value);
            }
                        
            function SettingSearchCriteria() {
                this.name = ko.observable();
                this.value = ko.observable();
                this.limit = ko.observable();
                this.offset = ko.observable();
            }

            function SettingListViewModel() {
                var self = this;
                //data
                self.settings = ko.observableArray([]);
                self.editSettingRequest = ko.observable(new Setting({}));
                self.deleteSettingRequest = ko.observable(new Setting({}));
                self.searchCriteria = ko.observable(new SettingSearchCriteria());
                // operations
                self.searchSettings = function(searchCriteriaItem) {
                    console.log("Endpoint: %s", endpoint);
        //            console.log("Search keys 1: %O", ko.toJSON(searchCriteriaItem)); //   results in error: InvalidStateError: Failed to read the 'selectionDirection' property from 'HTMLInputElement': The input element's type ('hidden') does not support selection
                    console.log("Search settings: %O", ko.toJSON(searchCriteriaItem));
        //            console.log("Search keys 2: %O", searchCriteriaItem);
        //            console.log("Search keys 3: %s", $.param(ko.toJSON(searchCriteriaItem)));
        //            console.log("Search keys 4: %s", $.param(searchCriteriaItem)); // id=undefined&role=undefined&algorithm=undefined&key_length=undefined&cipher_mode=undefined&padding_mode=undefined&digest_algorithm=undefined&transfer_policy=undefined&limit=undefined&offset=undefined
                    $.ajax({
                        type: "GET",
                        url: endpoint + "/configuration-settings",
                        //accept: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: $("#searchSettingsForm").serialize(), // or we could use ko to serialize searchCriteriaItem $.params(ko.toJSON(searchCriteriaItem))
                        success: function(responseJsonContent, status, xhr) {
                            console.log("Search results: %O", responseJsonContent);
                            /*
                             * Example:
                             * {"meta":{},"data":[{"name":"kms.tls.cert.ip","value":"127.0.0.1"},{"name":"jetty.webxml","value":"C:\\Users\\jbuhacof\\workspace\\dcg_security-kms\\kms-servlet3\\src\\main\\resources\\WEB-INF\\web.xml"},{"name":"jetty.secure.port","value":"443"},{"name":"kms.tls.cert.dns","value":"JBUHACOF-MOBL.amr.corp.intel.com"},{"name":"javax.net.ssl.keyStore","value":"C:\\kms\\configuration\\keystore.jks"},{"name":"envelope.keystore.file","value":"C:\\kms\\configuration\\envelope.p12"},{"name":"storage.keystore.file","value":"C:\\kms\\configuration\\storage.jck"},{"name":"jetty.port","value":"80"},{"name":"jetty.hypertext","value":"C:\\Users\\jbuhacof\\workspace\\dcg_security-kms\\kms-html5\\src\\main\\resources\\publicResources"},{"name":"kms.tls.cert.dn","value":"CN=kms"}]}
                             */
                            /*
                             // clear any prior search results
                             while(self.keys.length>0) { self.keys.pop(); }
                             // add new results
                             for(var i=0; i<responseJsonContent.settings.length; i++) {
                             self.keys.push(new Key(responseJsonContent.settings[i]));
                             }
                             */
                            responseJsonContent.settings.sort(sortBy("name")); // sort by setting name
                            var mappedItems = $.map(responseJsonContent.settings, function(item) {
                                return new Setting(item);
                            });
                            self.settings(mappedItems);
                        }
                    });
                };
                self.reloadSettings = function() {
                    console.log("Reload settings"); 
                    var reloadSettings = { "settings": [ ], "reload": true };
                    console.log("Reload setting items: %s", ko.toJSON(reloadSettings));
                    // deleting a setting in this API is accomplished by editing it with a null value
                    $.ajax({
                        type: "POST",
                        url: endpoint + "/configuration-settings",
                        contentType: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: ko.toJSON(reloadSettings),
                        success: function(data, status, xhr) {
                            console.log("Reload setting response: %O", data);
                            /**
                             * The return value is same as in search- it's the updated current list of all settings
                             * 
                             */
                            data.settings.sort(sortBy("name")); // sort by setting name
                             var mappedItems = $.map(data.settings, function(item) {
                                return new Setting(item);
                            });
                            self.settings(mappedItems);
                        }
                    });
                };
                
                self.editSetting = function(editSettingItem) {
                    console.log("Edit setting: %O", editSettingItem);
                    console.log("Edit setting item: %s", ko.toJSON(editSettingItem));
                    var updatedSettings = { "settings": [ editSettingItem ] };
                    console.log("Edit setting items: %s", ko.toJSON(updatedSettings));
                    $.ajax({
                        type: "POST",
                        url: endpoint + "/configuration-settings",
                        contentType: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: ko.toJSON(updatedSettings),
                        success: function(data, status, xhr) {
                            console.log("Edit setting response: %O", data);
                            /**
                             * The return value is same as in search- it's the updated current list of all settings
                             * 
                             */
                            data.settings.sort(sortBy("name")); // sort by setting name
                             var mappedItems = $.map(data.settings, function(item) {
                                return new Setting(item);
                            });
                            self.settings(mappedItems);
                            $('#editSettingModalDialog').modal('hide');
                        }
                    });

                };

                self.confirmDeleteSetting = function(deleteSettingItem) {
                    console.log("Confirm delete setting: %O", deleteSettingItem); // deleteKeyItem a Key object
                    self.deleteSettingRequest(deleteSettingItem);
                };
                self.deleteSetting = function(deleteSettingItem) {
                    console.log("Delete setting: %O", deleteSettingItem); // the deleteKeyItem is the form element (don't know why) and .serializeObject returns a Key object
        //            var deleteKeyId = $("#deleteKeyForm input[name='id']")[0].val();
                    console.log("Delete setting item: %s", ko.toJSON(deleteSettingItem));
                    var deletedSettings = { "settings": [ { "name":deleteSettingItem.name, "value":null } ] };
                    console.log("Delete setting items: %s", ko.toJSON(deletedSettings));
                    // deleting a setting in this API is accomplished by editing it with a null value
                    $.ajax({
                        type: "POST",
                        url: endpoint + "/configuration-settings",
                        contentType: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: ko.toJSON(deletedSettings),
                        success: function(data, status, xhr) {
                            console.log("Delete setting response: %O", data);
                            self.settings.remove(deleteSettingItem);
                            $('#deleteSettingModalDialog').modal('hide');
                        }
                    });
                };
            }
