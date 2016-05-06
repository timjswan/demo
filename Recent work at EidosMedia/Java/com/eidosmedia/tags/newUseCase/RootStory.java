package com.eidosmedia.tags.newUseCase;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import EOM.ObjectType;

import com.eidosmedia.eom.metadata.Metadata;
import com.eidosmedia.eom.story.StoryImpl;
import com.eidosmedia.tags.logger.Logger;
import com.eidosmedia.wa.render.WebObject;
import com.eidosmedia.wa.util.EomDb;
import com.eidosmedia.wa.util.EomDbObject;
import com.eidosmedia.wa.util.SystemMetadata;

public class RootStory extends StoryImpl {
	public List<String> unusableChannelCopies = new ArrayList<String>();
	public EomDb db;
	public String channels;
	public ObjectType objType;
	public EomDbObject eomDbObject;
	public String edition;
	public String product;
	public String scope;
	public WebObject webObject;
	public Document metaDataDoc;
	public SystemMetadata sysAtts;
	public Document storyDoc;
	public String[] channelCopyList;
	public String storyName;
	public String workFolder;
	public Metadata metaData;
	
	public RootStory(EomDbObject eomDbObj, WebObject webObj) throws Exception{
		super(eomDbObj);
		this.eomDbObject = eomDbObj;
		this.webObject = webObj;
		this.metaDataDoc = webObj.getUserMetadata();
		this.metaData = eomDbObj.getMetadata();
		this.sysAtts = eomDbObj.getSystemMetadata();
		this.storyDoc = this.getContentDocument();		
		this.storyName = eomDbObj.getName();
		this.workFolder = this.sysAtts.getWorkFolder();
	}
	
	/**
	 * Add invalid channel copies to a list to check against when a new channel copy is being created
	 * 
	 * @param chName - the channel name to add
	 */
	public void addUnusableChannelCopy(String chName){
		this.unusableChannelCopies.add(chName);
	}
	
	/**
	 * Check to see if the current object is a root story
	 * 
	 * @param chName - the channel name to check
	 */
	public boolean hasUnusableChannelCopy(String chName){
		return this.unusableChannelCopies.contains(chName);
	}
}
