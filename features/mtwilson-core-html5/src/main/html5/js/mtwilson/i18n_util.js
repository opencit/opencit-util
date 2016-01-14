// requires:  jQuery, i18next

// reference: http://i18next.com/pages/doc_init.html

/*
 * Example: 
 * 
 * $( document ).ready(function() {
 *   var localizer = new Localizer();
 *   localizer.init();
 * });
 * 
 * And later, maybe after a plugin has loaded, that plugin can call:
 * 
 * localizer.load(
 */


function Localizer() {
    var self = this;
    self.options = {
        'lowerCaseLng': false, // when true, will send "en-US" as "en-us" for __lng__ variable
        //'resGetPath': '/v1/html5/public/merge?path=/mtwilson-core-html5/locales/__ns__.__lng__.json', // resource files will be stored in locales/translation.en-US.json  for example
        'backend': { 
        'loadPath': '/v1/html5/public/merge?path=/mtwilson-core-html5/locales/{{ns}}.{{lng}}.json'

        },
        'detectLngQS': 'lang', // optional query string parameter to set language, for example  ?lang=en-US 
        'cookieName': 'lang', // optional cookie name to set language
        'preload': ['en-US'], // optimization, 
        'fallbackLng': 'en-US', // if a key is not available in current language, use the corresponding key from this fallback language, 
        'fallbackToDefaultNS': false, // if a namespace is used and a key is missing, do not look in the default namespace for it;  false happens to also be the default option for this setting
        'useLocalStorage': false,
        'localStorageExpirationTime': 86400000, // in ms, default 1 week
        'fallbackOnNull': true, // if a key is set to null then treat it as missing and use the fallback language
        'debug': true
    };

    // optional translation function t,  currently unused (see i18next docs)
    self.localize1 = function(t) {
        // find all html elements with a data-i18n attribute except for
        // elements that also have a translate=no attribute, and apply
        // the translation function to them
        $("[data-i18n]").not("[translate='no']").each(function() {
            $(this).i18n(); // translate it using built-in i18next rules
        });
    };
    self.localize2 = function(t) {
        // i18next 2.0+ requires us to use separate jquery plugin
        i18nextJquery.init(i18next, $, {
            tName: 't', // --> appends $.t = i18next.t
            i18nName: 'i18n', // --> appends $.i18n = i18next
            handleName: 'localize', // --> appends $(selector).localize(opts); 
            selectorAttr: 'data-i18n', // selector for translating elements
            targetAttr: 'data-i18n-target', // element attribute to grab target element to translate (if diffrent then itself)
            optionsAttr: 'data-i18n-options', // element attribute that contains options, will load/set if useOptionsAttr = true
            useOptionsAttr: false, // see optionsAttr
            parseDefaultValueFromContent: true // parses default values from content ele.val or ele.text
        });        
        
        // handleName above is supposed to be configurable but setting it to i18n didn't work, so:
                    $.fn.i18n = $.fn.localize; // alias to keep our older code working...

        //// 
        // 
        // find all html elements with a data-i18n attribute except for
        // elements that also have a translate=no attribute, and apply
        // the translation function to them
        $("[data-i18n]").not("[translate='no']").each(function() {
            $(this).localize(); // translate it using built-in i18next rules
        });
    };

    self.init = function() {
        if (i18next) {
            // version 2.0 and later
            self.localize = self.localize2;
        // i18next 2.0+ has a separate xhr plugin for loading our translation files
            i18next.use(i18nextXHRBackend).init(self.options, self.localize);
            window.i18n = window.i18next; // alias to keep our older code working...
        }
        else if (i18n) {
            // version 1.0.x
            self.localize = self.localize1;
            i18n.init(self.options, self.localize);
        }
        else {
            console.log("Missing javascript library: i18next");
        }
    };

    // loads additional localization resource bundles;    ns is required, like "simplename" or "com.example.full.name";  lng is optional, like "en" or "en-US", and will default to currently selected language or the default language; fromPath is optional, defaults to /mtwilson-core-html5/i18n
    self.load = function(ns, lng, fromPath) {
        if (discovery) {
            if (lng === null) {
                if (i18n) {
                    lng = i18n.lng(); // returns current language that was either set progammatically via setLng, or detected from query string (detectLngQS option), or cookie (cookieName option), or language set by browser
                }
                else {
                    lng = self.options.fallbackLng;
                }
            }
            if (fromPath === null) {
                fromPath = "/mtwilson-core-html5/locales";
            }
            var filename = ns + "." + lng + ".json";  // for example  main.en-US.json;  plugins can use their name like "simplename.en-US.json" or "com.example.full.name.en-US.json"
            discovery.eachJSON(fromPath + "/" + filename, function(json, context) {
                console.log("i18n content called with json: %O", json);
                console.log("i18n content called after extension: %O", context);
                if (i18n) {
                    i18n.addResourceBundle(lng, ns, json, true); // the 'true' makes it a deep merge so we're adding to (or updating) an existing bundle instead of replacing it
                    // does it automatically reapply or do we call localize() here?
                    console.log("i18n added resource bundle for language: %s in namespace: %s", lng, ns);
                }
                else {
                    console.log("Missing javascript library: i18next");
                }
            });
        }
        else {
            console.log("Missing javascript library: mtwilson-core-discovery");
        }
    };
}