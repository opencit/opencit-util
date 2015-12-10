/**
 * Takes a variable number of arguments indicating the fields to sort on.
 * 
 * Example:
 * <pre>
 * var list = [];
 * list.push({name:"Alice",city:"Sacramento",age:32});
 * list.push({name:"Bob",city:"Folsom",age:29});
 * list.push({name:"Bob",city:"Roseville",age:29});
 * list.push({name:"Alice",city:"Folsom",age:28});
 * list.push({name:"Alice",city:"Folsom",age:31});
 * list.sort(sortBy("name","-city","+age")); // sorts by name (default asc), city (specified desc), and then age (specified asc)
 * for(var i=0; i<list.length; i++) { console.log("Item: %O", list[i]); }
 * // results: Alice/Sacramento/32, Alice/Folsom/28, Alice/Folsom/31, Bob/Roseville/29, Bob/Folsom/29
 * </pre>
 * propertyName default sort order is ascending but you can pass "+propertyName" for explicitly ascending or "-propertyName" for descending
 * @return {function} a comparison function that operates on the named property in its two arguments
 */
function sortBy() {
    var order = [];
    for (var i = 0; i < arguments.length; i++) {
        var propertyName = arguments[i];
        var sortOrder = 1; // default ascending
        if (propertyName[0] === "+") {
            sortOrder = 1; // explicitly ascending
            propertyName = propertyName.substr(1);
        }
        if (propertyName[0] === "-") {
            sortOrder = -1; // descending
            propertyName = propertyName.substr(1);
        }
        order.push({propertyName: propertyName, sortOrder: sortOrder});
    }
    return function(a, b) {
        var result = 0;
        for (var i = 0; result === 0 && i < order.length; i++) {
            var propertyName = order[i].propertyName;
            var sortOrder = order[i].sortOrder;
            var propA = a[propertyName];
            var propB = b[propertyName];
            var valueA = (typeof(propA) === "function" ? propA() : propA);
            var valueB = (typeof(propB) === "function" ? propB() : propB);
            if (valueA < valueB) {
                result = -1;
            }
            if (valueA > valueB) {
                result = 1;
            }
            result = result * sortOrder;
        }
        return result;
    };
}


/**
 * Example:
 * <pre>
 * var list = [];
 * list.push({name:"Alice",city:"Sacramento"});
 * list.push({name:"Bob",city:"Folsom"});
 * console.log("Sort by name: %O", list.sort(sortBy("name")));
 * console.log("Sort by city: %O", list.sort(sortBy("city")));
 * </pre>
 * @param propertyName default sort order is ascending but you can pass "+propertyName" for explicitly ascending or "-propertyName" for descending
 * @return {function} a comparison function that operates on the named property in its two arguments
 */

/*
 * this original function supported only one sort field
 */
/*
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
 var propA = a[propertyName];
 var propB = b[propertyName];
 var valueA = (typeof(propA) === "function" ? propA() : propA);
 var valueB = (typeof(propB) === "function" ? propB() : propB);
 if( valueA < valueB) { result = -1; }
 if( valueA > valueB) { result = 1; }
 return result * sortOrder;
 };
 }
 */