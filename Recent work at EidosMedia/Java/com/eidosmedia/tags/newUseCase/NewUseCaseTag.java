package com.eidosmedia.tags.newUseCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.jsp.tagext.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import EOM.ObjectIteratorHolder;
import EOM.ObjectsHolder;
import EOM._Object;

import com.eidosmedia.eom.story.*;
import com.eidosmedia.tags.logger.Logger;
import com.eidosmedia.wa.render.*;
import com.eidosmedia.wa.util.*;

public class NewUseCaseTag extends SimpleTagSupport {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Logger logger = new Logger();
	private String uuid;
	private List<String> uuidList = new ArrayList<String>();
	public String channels = null;
	public EomDb db;
	public EomDbObject eomDbObject;
	private WebObjectImpl webObject;
	public EOM.ObjectType objType;
	public String version;
	public String uiSelectedProduct;
	public String scope;
	private String titleName;
	
	/* Getter and setter methods. */
	
	public void setTitleName(String sysName){
		this.titleName = sysName;
	}
	
	public String getTitleName(){
		return this.titleName;
	}
	
	public void setChannels (String channel){
		this.channels = channel;
	}
	
	public String getChannels(){
		return this.channels;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public void setProduct(String product){
		this.uiSelectedProduct = product;
	}
	
	public String getProduct(){
		return this.uiSelectedProduct;
	}
	
	public void setScope(String scope){
		this.scope = scope;
	}
	
	public String getScope(){
		return this.scope;
	}
	
	public void setDb (EomDb db){
		this.db = db;
	}
	
	public EomDb getDb(){
		return this.db;
	}
	
	/**
	 * Create an eomDbObject from a uuid and set the eomDbObject
	 */
	private void setEomDbObject(){
		try {
			this.eomDbObject = this.db.getEomDbObjectByUuid(this.uuid);
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Create a webObject from the eomDbObject and set the eomDbObject
	 */
	private void setWebObject(){
		try {
			this.webObject = new WebObjectImpl(this.eomDbObject);
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Get the eom object type from the eomDbObject and set the object type
	 */
	private void setObjectType() {
		try {
			this.objType = this.eomDbObject.getObjectType();
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/* End of getter and setter methods. */
	
	/**
	 * Generate a sequence of characters randomly from the alphabet
	 * 
	 * @param rng - Random number generator
	 * @param numberOfChars - the amount of characters in the sequence
	 */
	public static String randomCharSequence(Random rng, int numberOfChars){
		StringBuilder randomStrBuilder = new StringBuilder();
		
		for(int i = 0; i < numberOfChars; i++) {
			char c = (char)(rng.nextInt(26) + 'a');
	        randomStrBuilder.append(c);
	    }
		
		return randomStrBuilder.toString().toUpperCase();
	}
	
	/**
	 * Generate an alphanumeric id. 
	 * 
	 * @param minRange - the minimum the random number can be
	 * @param maxRange - the maximum the random number can be
	 * @param formatOrder - the order the result string should be arranged i.e digits first characters second
	 * @param numberOfChars - the number of characters to concat to the number string
	 */
	public static String generateId(long minRange, long maxRange, String formatOrder, int numberOfChars){
		Random rng = new Random();
		long randomNumber = (long)(rng.nextDouble()*(maxRange - minRange));
		String randomStr = NewUseCaseTag.randomCharSequence(rng, numberOfChars);

		return String.format(formatOrder, randomNumber, randomStr);
	}	
	
	/**
	 * Generate a numerical id.
	 * 
	 * @param minRange - the minimum the random number can be
	 * @param maxRange - the maximum the random number can be
	 */
	public static String generateId(long minRange, long maxRange){
		Random rng = new Random();
		long randomNumber = (long)(rng.nextDouble()*(maxRange - minRange));
		String result = String.valueOf(randomNumber);

		return result;
	}
	
	/**
	 * Take a byte array and parse it into a document using a DocumentBuilder
	 * 
	 * @param byteArr - the byte arr to transform into a document
	 * @param logger - pass the logger into the method as it is static
	 */
	public static Document buildNewDocFromByteArr(byte[] byteArr, Logger logger){
		Document doc = null;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    factory.setNamespaceAware(true);
		    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);

		    DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new EntityResolver() {
		        @Override
		        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {		          
		        	return new InputSource(new StringReader(""));
		        }
		    });
			doc = builder.parse(new ByteArrayInputStream(byteArr));
		} catch (Throwable e) {
			logger.error(e);
		}

	    return doc;
	}
	
	/**
	 * Get a list of nodes from an xpath.
	 * 
	 * @param doc - the document to evaluate the xpath against
	 * @param xPathExpression - the xpath
	 * @param logger - pass the logger into the method as it is static
	 */
	public static NodeList getNodeListFromXpath(Document doc, String xPathExpression, Logger logger){		
		NodeList nl = null;

		try {
			XPath xPath =  XPathFactory.newInstance().newXPath();
			nl = (NodeList) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODESET);
		} catch (Throwable e) {
			logger.error(e);
		}

		return nl;
	}
	
	/**
	 * Check if channels have been passed from the UI
	 */
	private void checkChannels() throws Throwable{
		if(this.channels == null || this.channels.equals("")){
			throw new Throwable("No channels were selected!");
		}
	}
	
	/**
	 * Check if the uuid being searched actually brings back an EOM db object
	 */
	private void checkIfObjectExists() throws Throwable{
		if(this.eomDbObject == null){
			throw new Throwable("Object does not exist!");
		}
	}
	
	/**
	 * Loop through all stories in the /Stories folder and build a list of their uuids.
	 * A certain amount of objects can be added to an object holder so that all objects aren't returned at once.
	 * 
	 * The object iterator iterates through the object holder and the next 
	 * group of objects is stored in the object holder
	 */
	public void buildStoryUuidList(){
		String storyFolderPath = "/" + this.titleName + "/Stories";
		try {
			EOM.Folder storiesFolder = this.db.getFolder(null, storyFolderPath, false);			
			
			this.logger.log("Obj name: " + storiesFolder.get_name());
			
			int amountOfObjsBack = 0;
			ObjectsHolder objsHolder = new ObjectsHolder();
			storiesFolder.list_objects(amountOfObjsBack, objsHolder, new ObjectIteratorHolder());
			for(_Object obj : objsHolder.value){
				String tempUuid = obj.get_uuid_string();
				this.logger.log("UUID: " + tempUuid);
				this.uuidList.add(tempUuid);
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Check to see if the current object is a root story
	 * Not needed at the moment as we are always getting the root story.
	 */
	/*private boolean checkRootStory(String uuid){
		 Match the searched uuid against the uuid obtained from the root story 
		boolean isRootStory = false;

		if(this.uuid.equals(uuid)){
			isRootStory = true;
		}
		this.logger.log("Is already root story? " + String.valueOf(isRootStory));		
		return isRootStory;
	}*/
	
	/**
	 * If the current object is not a root story then get the root story and set it.
	 * Not needed at the moment as we are always getting the root story.
	 */
	/*private void setRoot() throws Throwable {
		 Get the root story from the webobject and see if its uuid matches the searched uuid. 
		 * If it does then the object we are searching for is already a root story so we use that.
		 * If not overwrite the class eomdb object with the root story eomdb object.
		 * Then we get the root story from the temp web object. 
		try {
			Story rootStory = this.webObject.getRootStory();
			String rootStoryUuid = rootStory.getEomDbObject().getUuid();
			boolean isRootStory = this.checkRootStory(rootStoryUuid);
			
			this.logger.log("Root story uuid: " + rootStoryUuid);
			
			if(!isRootStory){				
				WebObjectImpl tempWebObj = new WebObjectImpl(this.eomDbObject);
				rootStory = (StoryImpl)tempWebObj.getRootStory();
				this.eomDbObject = rootStory.getEomDbObject();
			} else {				
				rootStory = (StoryImpl)rootStory;
				this.eomDbObject = rootStory.getEomDbObject();
			}
		} catch (Throwable e) {
			this.logger.error(e);
			throw e;
		}
	}*/
	
	public void doTag() {		
		this.logger.log("Passed channels: " + this.getChannels());		
		try {			
			this.checkChannels();			
			this.buildStoryUuidList();
			
			ProgressHelper progressHelper = new ProgressHelper(this.uuidList.size(), getJspContext(), 
					"status", "progress");
			progressHelper.setStatus("Building story list.");
			
			RootStory rootStory;
			RootStoryHelper rootStoryHelper = null;

			int count = 1;
			int total = 0;
			for(String uuidFromList : this.uuidList){
				this.logger.log("\n\n");
				this.uuid = uuidFromList;
				this.setEomDbObject();
				this.checkIfObjectExists();
				this.setWebObject();
				this.setObjectType();		
				String objTypeName = this.objType.get_name();
				
				this.logger.log("Object type: " + objTypeName);
				
			    if(objTypeName.equals("EOM::CompoundStory")){	    
			    	//this.setRoot();			    	
			    	rootStory = new RootStory(this.eomDbObject, this.webObject);
			    	rootStoryHelper = new RootStoryHelper(this.titleName, rootStory, this.db, this.channels, 
			    			this.version, this.uiSelectedProduct, this.scope, objType, this.logger);
			    	rootStoryHelper.createChannelCopies();
			    	
			    	total += rootStoryHelper.getAmountOfChannelsCreated();
			    	
			    	progressHelper.setStatus("Creating channel copies for story " + count + ".");
			    	progressHelper.setProgress(count);
					
			    	count++;
			    } else {
			    	throw new Throwable("Source object is not an 'EOM::CompoundStory'");
			    }
			}
			if(total > 0){
				progressHelper.setStatus("Number of channel copies created: " + total);
				//progressHelper.destroy();
				this.logger.log("Number of channel copies created: " + total);
				CampaignIdHelper.setId();
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
}