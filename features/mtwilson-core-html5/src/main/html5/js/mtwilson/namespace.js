/**
 * namespace.js
 * 
 * Defines a global namespace function if one doesn't exist yet.
 * 
 * Example of using namespace to define some functions:
 * 
 * // wrapped in anonymous function to show that returned variable does not
 * // need to be stored, it is an alias for the namespace
 * (function(){
 *   var module = namespace("com.example.module");
 *   module.add = function(a,b) { return a+b; }
 *   module.multiply = function(a,b) { return a*b; }
 * })();
 * 
 * Example of using namespace to reference previously defined functions:
 * 
 * // wrapped in anonymous function to show that we only need to know the
 * // namespace and we don't need the original alias
 * (function(){
 *   var math = namespace("com.example.module");
 *   var addition = math.add(1,2);
 *   var multiplication = math.multiply(3,4);
 * })();
 * 
 * 
 */
if (typeof namespace === "undefined" || namespace === null ) {

    namespace = function(packageName) {
        var parts = packageName.split('.'),
                parent = window,
                currentPart = '';

        for (var i = 0, length = parts.length; i < length; i++) {
            currentPart = parts[i];
            parent[currentPart] = parent[currentPart] || {};
            parent = parent[currentPart];
        }

        return parent;
    };

}
