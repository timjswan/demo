/*###########################################################################
# 
# author   : Tim Swan
# creation : 2016-02-19 
#
# ------------------------------------------------------------------------ 
#
# Description :                                                            
# Helper module for datasource
# Principals functions :#  
#   - Load query from datasource servlet 
#	- Load XML from Sysconfig
#	- Convert into json that can be apapted into a listable html format  
#
###########################################################################*/

/**
 * Create an XHR
 */
var request = (function(){
	if (window.XMLHttpRequest) {
		return new XMLHttpRequest();
    } else {       
		return new ActiveXObject("Microsoft.XMLHTTP");
    }
})();

/**
 * Helper for datasources. The datasource to be collected is determined by passing its id to setContext
 */
var dsmHelper = (function(request, $){
	var app = window.external.EomQueryInterface('methode.application'),
		w = app.ActiveWindow;
	
	var dataObject = {},
		HOST = w.GetSiteConfigString("/siteConfiguration/dsm/@host"),
		PORT = w.GetSiteConfigString("/siteConfiguration/dsm/@port"),
		context = "",
		errorContainer;
	
	/**
	 * Set the name of the datasource we want to collect
	 */
	var setContext = function(newContext) {
		context = newContext;
	};
	
	/**
	 * Set the id of the DOM Object we want to display errors in
	 */
	var setErrorContainer = function(id){
		errorContainer = $(id);
	};
	
	/**
	 * Get the JSON created from the datasource XML
	 */
	var getDataObject = function(){
		return dataObject;
	};
	
	/**
	 * Check to see if the datasource is valid. If a datasource is invalid XML is returned as <error></error>.
	 */
	var invalidDataSource = function(responseXML) {
		if(responseXML.getElementsByTagName("error").length > 0){
        	return true;
        } else {
        	return false;
        }
	};
	
	/**
	 * The function called when the AJAX call in doAjaxRequest returns. We parse the XML and create a JSON.
	 */
	var callBack = function() {
		// If request was a success
		if (request.readyState == 4 && request.status == 200) {
            // save the response
            var responseXML = request.responseXML;
            
            // Check the datasource is valid and the context is set
            if(invalidDataSource(responseXML) && errorContainer.length){
            	errorContainer.text("Invalid datasource: '" + context + "'.");
            } if(invalidDataSource(responseXML) && errorContainer.length && context === ""){
            	errorContainer.text("No datasource context set.");
            } else if(responseXML != null && !invalidDataSource(responseXML)){
            	// Find the root node element (this is always "ra" in the datasource)
                var results = responseXML.getElementsByTagName("ra")[0].childNodes;
                
                // Loop through the child nodes in the 'ra' tag
                for (i = 0; i < results.length; i++) {
                    var row = results[i];
                    
                    // Get the child node values
                    var id = row.getElementsByTagName("id")[0].childNodes[0].nodeValue,
                    	value = row.getElementsByTagName("value")[0].childNodes[0].nodeValue,
                    	label = row.getElementsByTagName("label")[0].childNodes[0].nodeValue;
                    
                    // If they all exist then create an item object then add the item to the data object
                    if(id && value && label){
	                    var item = {
	                    	"id": id,
	                    	"value": value,
	                    	"label": label
	                    }
	                    dataObject["item" + i] = item;
                    }
                }
            }
        }
	};
	
	/**
	 * Make the AJAX call to the datasource
	 */
	var doAjaxRequest = function(query) {
		var url =  "http://"+HOST+":"+PORT+"/datasource/servlet/datasource?ds="+context+"&op=rquery&q=";
		request.open("GET", url, false);		
		request.send(null);
		
		// Not sure why but new Function() makes the below work.
		request.onreadystatechange = new Function(callBack());		
	};
	
	/**
	 * The function called when the AJAX call in doAjaxRequest returns. We parse the XML and create a JSON.
	 */
	var updateHTMLSelectById = function(id){
		// Use a document fragment so the DOM only needs to be updated once
		var frag = document.createDocumentFragment(),
			dropdown = $(id);
		
		// If the dropdown exists
		if(dropdown.length){
			// Loop through the data object and get the item.  
			for(i in dataObject){
				if(dataObject.hasOwnProperty(i)){
					// Create an <option/>
					var option = document.createElement("option"),
						dataItem = dataObject[i];
					
					// Get the id, value and name from the item and update the <option/> from the data.
					option.value = dataItem.id + "-" + dataItem.value;
					option.innerHTML = dataItem.label
					option.name = dataItem.value;
					
					// Append the option to the document fragment
					frag.appendChild(option);
				}
			}
			
			// Append the document fragment to the dropdown
			dropdown.html(frag);
		} else {
			errorContainer.text("Select '" + id + "' doesn't exist.")
		}
	};

	return {
		setContext: setContext,
		setErrorContainer: setErrorContainer,
		load: doAjaxRequest,
		getData: getDataObject,
		updateHTMLSelectById: updateHTMLSelectById
	};
})(request, jQuery);