            function Contact(data) {
                var self = this;
                self.first_name = ko.observable(data.first_name);
                self.last_name = ko.observable(data.last_name);
                self.email_address = ko.observable(data.email_address);
            }
            
            function User(data) {
                var self = this;
                self.id = ko.observable(data.id);
                self.username = ko.observable(data.username);
                self.contact = new Contact(data.contact || {}); // object with first_name, last_name, email_address
                self.transfer_key_pem = ko.observable(data.transfer_key_pem || ""); // PEM-format public key, empty string if not present
            }

            function UserSearchCriteria() {
                var self = this;
                self.id = ko.observable();
                self.firstNameEqualTo = ko.observable();
                self.lastNameEqualTo = ko.observable();
                self.firstNameContains = ko.observable();
                self.nameContains = ko.observable();
                self.emailAddressEqualTo = ko.observable();
                self.emailAddressContains = ko.observable();
                self.filter = ko.observable();
                self.limit = ko.observable();
                self.offset = ko.observable();
            }

            function UserListViewModel() {
                var self = this;
                //data
                self.users = ko.observableArray([]);
                self.viewUserRequest = ko.observable(new User({}));
                self.registerUserRequest = ko.observable(new User({}));
                self.deleteUserRequest = ko.observable(new User({}));
                self.searchCriteria = ko.observable(new UserSearchCriteria());
                // operations
                self.searchUsers = function(searchCriteriaItem) {
                    console.log("Endpoint: %s", endpoint);
        //            console.log("Search users 1: %O", ko.toJSON(searchCriteriaItem)); //   results in error: InvalidStateError: Failed to read the 'selectionDirection' property from 'HTMLInputElement': The input element's type ('hidden') does not support selection
                    console.log("Search users: %O", ko.toJSON(searchCriteriaItem));
        //            console.log("Search users 2: %O", searchCriteriaItem);
        //            console.log("Search users 3: %s", $.param(ko.toJSON(searchCriteriaItem)));
        //            console.log("Search users 4: %s", $.param(searchCriteriaItem)); // id=undefined&role=undefined&algorithm=undefined&user_length=undefined&cipher_mode=undefined&padding_mode=undefined&digest_algorithm=undefined&transfer_policy=undefined&limit=undefined&offset=undefined
                    $.ajax({
                        type: "GET",
                        url: endpoint + "/users",
                        headers: {'Accept': 'application/json'},
                        data: $("#searchUsersForm").serialize(), // or we could use ko to serialize searchCriteriaItem $.params(ko.toJSON(searchCriteriaItem))
                        success: function(data, status, xhr) {
                            console.log("Search results: %O", data);
                            /*
                             * Example:
                             * {"search_results":[{"algorithm":"AES","user_length":128,"id":"3787f629-1827-411e-866e-ce87e37f805a"},{"algorithm":"AES","user_length":128,"id":"dd552684-8238-4c4c-baba-c5e7467d3604"}]}
                             */
                            /*
                             // clear any prior search results
                             while(self.users.length>0) { self.users.pop(); }
                             // add new results
                             for(var i=0; i<data.search_results.length; i++) {
                             self.users.push(new X509Certificate(data.search_results[i]));
                             }
                             */
                            var mappedItems = $.map(data.users, function(item) {
                                return new User(item);
                            });
                            self.users(mappedItems);
                        }
                    });
                };
                self.viewUser = function(viewUserItem) {
                    console.log("View user: %O", viewUserItem);
                    if (viewUserItem) {
                        self.viewUserRequest(viewUserItem);
                    }
                };
                self.closeViewUser = function(viewUserItem) {
                    self.viewUserRequest(new User({}));
                };
                self.registerUser = function(registerUserItem) {
                    console.log("Register user: %O", registerUserItem);
                    $.ajax({
                        type: "POST",
                        url: endpoint + "/users",
                        contentType: "application/json",
                        headers: {'Accept': 'application/json'},
                        data: ko.toJSON(registerUserItem), //registerUserItem.certificate_pem(), // base64 encoded
                        success: function(data, status, xhr) {
                            console.log("Register user response: %O", data);
                            self.searchUsers({});
                            //self.users.push(new X509Certificate(data)); // have to add this and not userItem because server ersponse includes user id
                            $('#addUserModalDialog').modal('hide');
                        }
                    });

                };
                self.confirmDeleteUser = function(deleteUserItem) {
                    console.log("Confirm delete user: %O", deleteUserItem); // deleteUserItem a User object
                    self.deleteUserRequest(deleteUserItem);
                };
                self.deleteUser = function(deleteUserItem) {
                    console.log("Delete user: %O", deleteUserItem); // the deleteUserItem is the form element (don't know why) and .serializeObject returns a User object
        //            var deleteUserId = $("#deleteUserForm input[name='id']")[0].val();
                    var deleteUserId = deleteUserItem.id();
                    console.log("Delete user id: %s", deleteUserId);
                    $.ajax({
                        type: "DELETE",
                        url: endpoint + "/users/" + deleteUserId,
                        success: function(data, status, xhr) {
                            console.log("Delete user response: %O", data);
                            self.users.remove(deleteUserItem);
                            $('#deleteUserModalDialog').modal('hide');
                        }
                    });
                };
            }
