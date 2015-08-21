            function ProfileViewModel() {
                var self = this;
                //data
                self.myProfile = ko.observable(new User({}));
                // operations
                self.loadProfile = function(userProfileRequest) {
                    console.log("Endpoint: %s", endpoint);
        //            console.log("Search users 1: %O", ko.toJSON(searchCriteriaItem)); //   results in error: InvalidStateError: Failed to read the 'selectionDirection' property from 'HTMLInputElement': The input element's type ('hidden') does not support selection
                    console.log("Load profile: %O", userProfileRequest);
        //            console.log("Search users 2: %O", searchCriteriaItem);
        //            console.log("Search users 3: %s", $.param(ko.toJSON(searchCriteriaItem)));
        //            console.log("Search users 4: %s", $.param(searchCriteriaItem)); // id=undefined&role=undefined&algorithm=undefined&user_length=undefined&cipher_mode=undefined&padding_mode=undefined&digest_algorithm=undefined&transfer_policy=undefined&limit=undefined&offset=undefined
                    $.ajax({
                        type: "GET",
                        url: endpoint + "/users",
                        headers: {'Accept': 'application/json'},
                        data: { usernameEqualTo: userProfileRequest.username },
                        success: function(data, status, xhr) {
                            console.log("Profile results: %O", data);
                            /*
                             * Example:
                             */
                            if( data.users && data.users.length > 0 ) {
                                self.myProfile(new User(data.users[0]));
                            }
                        }
                    });
                };

            }
