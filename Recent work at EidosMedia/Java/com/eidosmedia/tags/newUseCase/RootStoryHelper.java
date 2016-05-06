package com.eidosmedia.tags.newUseCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import EOM.Format;
import EOM.ObjectType;

import com.eidosmedia.eom.story.Story;
import com.eidosmedia.tags.logger.Logger;
import com.eidosmedia.wa.render.WebObjectImpl;
import com.eidosmedia.wa.util.EomDb;
import com.eidosmedia.wa.util.EomDbObject;
import com.eidosmedia.wa.util.SystemMetadata;

public class RootStoryHelper {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Logger logger;
	private Map<String, Map<String, Node>> newStoryMap = new HashMap<String, Map<String, Node>>();
	private Document newStoryContent = null;
	private String channelCopyNameFromChannels;
	private String workFolder;
	private ArrayList<String> usedIds = new ArrayList<String>();
	private RootStory story;
	private String[] channelCopyList;
	private String channels;
	private String version;
	private String storyMetaProduct = "";
	private String uiSelectedProduct;
	private String storyMetaScope = "";
	private String uiSelectedScope;
	private ObjectType objType;
	private EomDb db;
	private int channelsCreated = 0;
	private String sysName;

	public RootStoryHelper(String sysName, RootStory rootStory, EomDb db, String channels, String version, 
			String uiSelectedProduct, String uiSelectedScope, ObjectType objType, Logger logger){
		try {			
			this.db = db;
			this.channels = channels;
			this.version = version;
			this.uiSelectedProduct = this.splitUiValue(uiSelectedProduct.toLowerCase(), "-", 1);
			this.uiSelectedScope = this.splitUiValue(uiSelectedScope.toLowerCase(), "-", 1);
			this.objType = objType;
			this.story = rootStory;
			this.logger = logger;
			this.sysName = sysName;
			
			Element productNode = (Element) NewUseCaseTag.getNodeListFromXpath(rootStory.metaDataDoc, "//metadata/product", this.logger).item(0);
			Element scopeNode = (Element) NewUseCaseTag.getNodeListFromXpath(rootStory.metaDataDoc, "//metadata/scope", this.logger).item(0);
			if(productNode != null && scopeNode != null){
				String storyProductFull = productNode.getTextContent();				
				if(!storyProductFull.equals("")){
					this.storyMetaProduct = this.splitUiValue(storyProductFull, "-", 1);
				}
				
				String storyScopeFull = scopeNode.getTextContent();
				if(!storyScopeFull.equals("")){
					this.storyMetaScope = this.splitUiValue(storyScopeFull, "-", 1);
				}
			}

			this.channelCopyList = this.channels.split(",");
			
			this.mapStoryContent();
			this.setChannelCopyContent();
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	// Getter setter methods
	
	public void setChannelCopyNameFromChannels(String channel){
		this.channelCopyNameFromChannels = channel.trim();
	}
	
	public int getAmountOfChannelsCreated(){
		return this.channelsCreated;
	}
	
	// End of getter setter methods
	
	private String splitUiValue(String uiVal, String spl, int get){
		String[] uiValSpl = uiVal.split(spl);
		return uiValSpl[get];	
	}
	
	private void trimWhiteSpace(Node node){
	    NodeList children = node.getChildNodes();

	    for(int i = 0; i < children.getLength(); ++i) {
	        Node child = children.item(i);
	        if(child.getNodeType() == Node.TEXT_NODE) {
	            child.setTextContent(child.getTextContent().trim());
	        }
	        trimWhiteSpace(child);
	    }
	}
	
	/**
	 * Generate an alphanumeric id. Different from generateId() in NewUseCaseTag as we have to use unique IDs
	 */
	public String generateId(){
		long minRange = 10000000000L;
		long maxRange = 999999999999L;
		String result = null;

		if(!this.usedIds.contains(result)){
			result = NewUseCaseTag.generateId(minRange, maxRange, "%d%s", 3);
			this.usedIds.add(result);
		} else {
			generateId();
		}

		return result;
	}
	
	/**
	 * Check against the channel configuration to see if the story content item should be inherited or not
	 */
	private boolean isInherited(String storyItemName, Document channelCfg) {
		boolean isInherited = false;

		try {			
			String inheritXp = "/channel/compound/inherit[@item='" + storyItemName + "']";
			int inheritedLength = NewUseCaseTag.getNodeListFromXpath(channelCfg, inheritXp, this.logger)
					.getLength();
			if(inheritedLength > 0){
				isInherited = true;
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}

		return isInherited;
	}

	/**
	 * Create a map of content items according to the dtx. Loop through the items in the dtx against the root story.
	 * The map is paired by the content item's name and a map of the root story content item's id attribute and its content node.
	 */
	public void mapStoryContent() throws Throwable {
		String dtxPath = "/SysConfig/" + this.sysName + "/Rules/" + this.sysName + ".dtx";

		try {
		    Document dtx = NewUseCaseTag.buildNewDocFromByteArr(this.db.getContentByPath(dtxPath), this.logger);
		    NodeList contentItems = NewUseCaseTag.getNodeListFromXpath(dtx, 
		    		"/emDtdExt/contentItems/name", this.logger);
		    NodeList storyTagList = NewUseCaseTag.getNodeListFromXpath(this.story.storyDoc, 
		    		"/doc/story", this.logger);

		    for(int i =0; i < contentItems.getLength(); i++){
		    	String name = contentItems.item(i).getTextContent();
		    	this.logger.log("element name from dtx: " + name);
		    	NodeList storyItemList = ((Element) storyTagList.item(0)).getElementsByTagName(name);
		    	Node storyItem = storyItemList.item(0);
		    	Map<String, Node> tempMap = new HashMap<String, Node>();
		    	
		    	if(storyItemList.getLength() > 0 && storyItem.getParentNode().getNodeName().equals("story")){
			    	this.logger.log("Story item node: " + storyItem);
			    	String storyItemId = storyItem.getAttributes().getNamedItem("id").getNodeValue();
		    		tempMap.put(storyItemId, storyItem);			    	
			    	this.newStoryMap.put(name, tempMap);
		    	}
		    }
		} catch (Throwable e) {
			this.logger.error(e);
			throw e;
		}		
	}
	
	/**
	 * Loop through the generated map of story content.
	 * For each iteration loop through the child map that stores the id and content node and replace the empty child in the base template xml.
	 * Add any inherit links.
	 * Store the modified xml in a class variable.
	 */
	public void setChannelCopyContent() throws Throwable {
		try {
			byte[] templateContentRaw = this.db.getContentByPath(this.story.sysAtts.getTemplateName());
		    this.newStoryContent = NewUseCaseTag.buildNewDocFromByteArr(templateContentRaw, this.logger);
		    
		    String channelCfgPath = "/SysConfig/" + this.sysName + "/SiteConfig/Channels/CustomerChannelTemp.cfg";
			Document channelCfg = NewUseCaseTag
					.buildNewDocFromByteArr(this.db.getContentByPath(channelCfgPath), this.logger);

		    NodeList storyTagList = NewUseCaseTag.getNodeListFromXpath(this.newStoryContent, "/doc/story", this.logger);
		    Element storyTag = (Element) storyTagList.item(0);
		    Iterator<Entry<String, Map<String, Node>>> newStoryMapIt = this.newStoryMap.entrySet().iterator();
			
			while(newStoryMapIt.hasNext()){
				@SuppressWarnings("rawtypes")
				Map.Entry newStoryMapPair = (Map.Entry) newStoryMapIt.next();
				String itemId = null;
				String itemName = (String) newStoryMapPair.getKey();
				Node itemContent = null;
				Iterator<Entry<String, Node>> itemValRawIt = 
						((Map<String, Node>) newStoryMapPair.getValue()).entrySet().iterator();

				while(itemValRawIt.hasNext()){
					Map.Entry itemValMapPair = (Map.Entry) itemValRawIt.next();
					itemId = (String) itemValMapPair.getKey();
					itemContent = (Node) itemValMapPair.getValue();
				}

				Node oldStoryItem = storyTag.getElementsByTagName(itemName).item(0);
				Node newStoryItemImport = this.newStoryContent.importNode(itemContent, true);
				Element newStoryItemImportEl = (Element) newStoryItemImport;
				this.logger.log("Is inherited? " + String.valueOf(this.isInherited(itemName, channelCfg)));

				if(this.isInherited(itemName, channelCfg)){
					newStoryItemImportEl.setAttribute("inherit", this.story.storyName + "?id=" + itemId);
				} else {
					newStoryItemImportEl.setAttribute("idr", itemId);
				}

				this.trimWhiteSpace((Node) storyTag);
				storyTag.replaceChild(newStoryItemImport, oldStoryItem);

				this.logger.log("\t" + itemName);
				newStoryMapIt.remove();
			}
		} catch (Throwable e) {
			this.logger.error(e);
			throw e;
		}
	}
	
	/**
	 * Set the tag attributes for all tags in the new channel copy
	 */
	private void setNewStoryTagAttributes(){
	    NodeList storyTagList = NewUseCaseTag
	    		.getNodeListFromXpath(this.newStoryContent, "/doc/story/*", this.logger);
	    
	    for(int i = 0; i < storyTagList.getLength(); i++){	    	
	    	Element storyEl = (Element) storyTagList.item(i);
	    	if(storyEl.getNodeType() == Node.ELEMENT_NODE){
	    		storyEl.setAttribute("id", this.generateId());				
	    		storyEl.setAttribute("channel", this.channelCopyNameFromChannels);
	    	}
	    }		
	}
	
	/**
	 * Set the edition in the system attributes
	 */
	private void setEdition(){
		if(!this.version.equals("")){
			Node prodInfo = this.story.sysAtts.getNode(null, "productInfo");
			this.story.sysAtts.setProp(prodInfo, "edition", this.version);
		}
	}
	
	/**
	 * Get the children from the root story. Get the channel copy name from each child. 
	 * Compare the new channel copy name against the child copy name.
	 */
	private void compareChannelCopies() throws Throwable {
		int numberOfMatched = 0;

		try {
			String channelCopyName = null;
			String version = null;
			
			// Get the product and scope from the UI and check against the product and scope in the metadata
			boolean productMatched = this.uiSelectedProduct.equals(this.storyMetaProduct);
			boolean scopeMatched = this.uiSelectedScope.equals(this.storyMetaScope);

			if(productMatched && scopeMatched){
				for(Story channelCopy : this.story.getChildren()){
					// Could use channelCopy.getPubChannel() 
					// However, It is dangerous to assume that the sysconfig has been set up with 
					// the correct channel names. Instead we get the channel copy name and the version
					// from the system attributes.
					SystemMetadata channelCopySysAtts = channelCopy.getSystemMetadata();
					Element channelCopyNameNode = (Element) NewUseCaseTag.getNodeListFromXpath(channelCopySysAtts.getDocument(), 
							"//productInfo/name", this.logger).item(0);
					Element versionNode = (Element) NewUseCaseTag.getNodeListFromXpath(channelCopySysAtts.getDocument(), 
							"//productInfo/edition", this.logger).item(0);
					
					channelCopyName = channelCopyNameNode.getTextContent();
					
					// Check to see whether the channel copy name matches and the version matches
					boolean channelMatched = this.channelCopyNameFromChannels.equals(channelCopyName);
					boolean versionMatched = this.version.equals(versionNode.getTextContent());

					if(!channelMatched){
						this.logger.log("\t\tChannel doesn't match.\n");
					} else if(channelMatched && !versionMatched){
						this.logger.log("\t\tChannel matches but version doesn't.\n");
					} else if (channelMatched && versionMatched) {
						numberOfMatched++;
						this.story.addUnusableChannelCopy(channelCopyName);
					}
				}
			} else {
				throw new Throwable("Invalid product or scope!");
			}
		} catch (Throwable e) {
			this.logger.error(e);
			throw new Throwable("Couldn't compare channel copies!");
		}	
		
		if(numberOfMatched > 0){
			Throwable e = new Throwable(this.story.unusableChannelCopies.toString() + " channel(s) already exist(s).");
			this.logger.error(e);
		}
	}
	
	public void createChannelCopy() {
		try {
			/* Create a filename for the new file. The format is: rootfilename@channel.xml */
			String rootStoryBaseNameWithExt = this.story.eomDbObject.getName();
			String rootStoryBaseNameWithoutExt = rootStoryBaseNameWithExt.substring(0, rootStoryBaseNameWithExt.lastIndexOf("."));					
			StringBuffer sb = new StringBuffer(50);
			sb.append(rootStoryBaseNameWithoutExt)
				.append("@")
				.append(this.channelCopyNameFromChannels);
			
			if(!this.version.equals("")){
				sb.append("-")
					.append(this.version);
			}
			
			sb.append(".xml");
			String channelCopyName = sb.toString();
			
			this.logger.log("Creating channel copy with name: " + channelCopyName);
			
			/* Get the folder object from the eomdb object that was created from the searched uuid. 
			 * Create a new file within the folder object, create a format to write the content to it 
			 * and check it in. */
			EOM._FolderStub folderStub = (EOM._FolderStub)this.story.eomDbObject.getFolder();
			EOM.File newFile = folderStub.create_file(channelCopyName, this.objType);		
			newFile.check_in("", false);
			
			/* Add product and scope to the metadata */
			WebObjectImpl newFileWebObject = new WebObjectImpl(new EomDbObject(this.db, newFile));
			newFileWebObject.setUserMetadata(this.story.metaDataDoc);

			/* Make a copy of the sysAtts that belong to the object created from the searched uuid. 
			 * Update the attributes and add them to the new file. */
			SystemMetadata newFileNewSysMeta = this.story.sysAtts;
			newFileNewSysMeta.setProp(newFileNewSysMeta.getNode(null, "productInfo"), 
					"name", this.channelCopyNameFromChannels);
			newFileNewSysMeta.setProp(null, "workFolder", this.workFolder);
			newFileNewSysMeta.setProp(null, "wordCount", "0");		
			newFile.set_system_attributes(newFileNewSysMeta.toString());
			
			DOMSource source = new DOMSource(this.newStoryContent);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			/* Create a byte array from the new file xml content and then use the new file 
			 * format to write it to the new file. */
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(bos);
			transformer.transform(source, result);
			byte[] newStoryContentByteArr = bos.toByteArray();

			newFile.get_default_format().write_all(newStoryContentByteArr);
			
			this.channelsCreated++;
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Loop through the channels selected in the UI and create a channel copy according to each channel's name
	 */
	public void createChannelCopies(){
		try {
			for(String channel : this.channelCopyList){	    	
				this.setChannelCopyNameFromChannels(channel);
				this.compareChannelCopies();
				if(this.newStoryContent != null
						&& !this.story.hasUnusableChannelCopy(this.channelCopyNameFromChannels)){							    	
		    		this.setNewStoryTagAttributes();
		    		this.setEdition();
		    		this.createChannelCopy();
	    		} else {
	    			this.logger.error(new Throwable("Not creating channel copy."));
	    		}
	    	} 
    	} catch (Throwable e) {
			this.logger.error(e);
		}
	}
}