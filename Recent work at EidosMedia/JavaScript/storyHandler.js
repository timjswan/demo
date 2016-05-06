/**
 * A helper for story quickmetadata functions. 
 * This is needed if there is more than one quickmetadata for 
 * different EOM object types i.e there might be a loadUI function for Topics
 */
var storyHandler = (function(dsmHelper){
	/**
	 * Use the dsmHelper to update the product dropdown
	 */
	var loadProductsDropDown = function(){	
		dsmHelper.setContext("products");	
		dsmHelper.load();
		dsmHelper.updateHTMLSelectById("#product");
	};
	
	/**
	 * Use the dsmHelper to update the product dropdown
	 */
	var loadScopeDropDown = function() {
		dsmHelper.setContext("scope");
		dsmHelper.load();
		dsmHelper.updateHTMLSelectById("#scope");
	};
	
	var loadUI = function(){
		loadProductsDropDown();
		loadScopeDropDown();
	};
	
	return {
		loadUI: loadUI
	};
})(dsmHelper || {});