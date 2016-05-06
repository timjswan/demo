package com.eidosmedia.tags.buildRegionTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.eidosmedia.siteconfig.Property;
import com.eidosmedia.tags.logger.Logger;

public class BuildRegionTreeTag extends SimpleTagSupport {
	private Property[] outputChannels;
	private String var = "regionTreeMap";
	private Logger logger = new Logger();
	
	/**
	 * @return output channels
	 */
	public Property[] getOutputChannels() {
		return this.outputChannels;
	}
	
	/**
	 * @param outputChannels - the output channels to set
	 */
	public void setOutputChannels(Property[] outputChannels){
		this.outputChannels = outputChannels;
	}
	
	/**
	 * @param var - name of the var to use in the JspContext
	 */
	public void setVar(String var){
		this.var = var;
	}
	
	/**
	 * Compare the names of two regions to determine whether the region has changed in the next loop
	 * 
	 * @param nextRegion - the name of the next region
	 * @param currRegion - the name of the current region
	 * @return a boolean that states whether the regions match or not
	 */
	private boolean compareRegions(String nextRegion, String currRegion) {
		boolean isNewRegion = false;
		if(!nextRegion.equals(currRegion)){
			isNewRegion = true;
		}
		return isNewRegion;
	}
	
	/**
	 * Make a clone of a List<String>. This is needed as we have to store the products for each new region 
	 * and clear the old ones.
	 * 
	 * @param l - the List<String> to clone
	 * @return a clone of the List<String> - l
	 */	
	private List<String> cloneStringList(List<String> l){
		ArrayList<String> lClone = new ArrayList<String>(l.size());
		for(String item : l){
			lClone.add(item);
		}
		return lClone;
	}
	
	/**
	 * Loop through outPutChannels and get the channel name. Split the channel name to get the region and product. 
	 * Add the product to the map. If the next region is different then clone the products.
	 * Add the cloned products to the map identified by the region name. Clear the current products.
	 * 
	 * @throws IOException, ArrayIndexOutOfBoundsException
	 */
	public void doTag() throws IOException, ArrayIndexOutOfBoundsException {
		// Creates a map of the channel name and it's products
		Map<String, List<String>> regionTreeMap = new HashMap<String, List<String>>();
		List<String> productsInRegion = new ArrayList<String>();
		
		// Loop through the output channels
		for(int i = 0;i < this.outputChannels.length; i++){			
			String channelName = this.outputChannels[i].getAttribute("name");
			
			this.logger.log("Channel name: " + channelName);

			String nextChannelName = "";
			String nextRegion = "";
			
			// We need to get the item ahead to determine whether we are in a new region or not
			// If we run out of channels then we set the next region to 'null'.
			try {
				Property nextOutputChannel = this.outputChannels[i + 1];
				nextChannelName = nextOutputChannel.getAttribute("name");
				String[] nextChannelNameSpl = nextChannelName.split("-");
				nextRegion = nextChannelNameSpl[0];
			} catch (ArrayIndexOutOfBoundsException e){
				this.logger.log("No more channels to search. " + e.getMessage());
				nextRegion = "";
			}
			
			String region = null;
			String product = null;			
			
			// Split the current channel name so we can get region and product
			if(channelName.contains("-")){
				String[] channelNameSpl = channelName.split("-");
				region = channelNameSpl[0];
				product = channelNameSpl[1];
			} else {
				region = channelName;
			}	
			
			this.logger.log("\tCustomer: " + product);
			
			// If the channel has a product then we add it to the list
			if(product != null){
				this.logger.log("\tAdd customer to region");
				productsInRegion.add(product);
			}
			
			this.logger.log("\tNext region: " + nextRegion);
			this.logger.log("\tregion: " + region);
			
			//Compare the current region to the next region to determine whether it has changed
			boolean isNewRegion = this.compareRegions(nextRegion.trim(), region.trim());
			
			// If we are changing region then we make a clone of the product names.
			// Then add the clone to the map identified by the region.
			// Clear the current products so we can add the new ones.
			if(isNewRegion) {
				this.logger.log("\tNew Region");
				List<String> productsInRegionCopy = this.cloneStringList(productsInRegion);				
				regionTreeMap.put(region, productsInRegionCopy);
				productsInRegion.clear();
			}
		}
		// Set the map as the jsp context attribute
		getJspContext().setAttribute(this.var, regionTreeMap);
	}
}
