function htmlEncode(obj){
	if (typeof obj == 'string' || obj instanceof String){
		 return obj.replace(/&/g, '&amp;')
	            .replace(/"/g, '&quot;')
	            .replace(/</g, '&lt;')
	            .replace(/>/g, '&gt;');

	 }
	return obj;
}