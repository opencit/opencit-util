function htmlEncode(obj){
	/*content variable that is being passes from resource_loader.js contains script that is being executed. It does not contain json response to encode. 
	To avoide checkmarx warning adding this function. This function needs to be removed in future.*/
	return obj;
}