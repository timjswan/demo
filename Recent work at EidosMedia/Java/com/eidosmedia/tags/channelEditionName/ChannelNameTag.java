package com.eidosmedia.tags.channelEditionName;

import java.io.IOException;

import javax.servlet.jsp.tagext.*;
import com.eidosmedia.siteconfig.Property;

public class ChannelNameTag extends SimpleTagSupport {
	private String channelName;
	private Property channel;
	private String var = "channelName";
	
	public Property getChannel() {
		return channel;
	}
	
	public void setChannel(Property channel){
		this.channel = channel;
	}
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	public String getChannelName() {		
		return channelName;
	}
	
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void doTag() throws IOException {
		String channelName = this.channel.getAttribute("name");
		if(channelName == ""){
			channelName = "None";
		}

		getJspContext().setAttribute(this.var, channelName);
	}
}