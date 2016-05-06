package com.eidosmedia.tags.channelEditionName;

import java.io.IOException;

import javax.servlet.jsp.tagext.*;
import com.eidosmedia.siteconfig.Property;

public class ObtainEditionsTag extends SimpleTagSupport {
	private Property channel;
	private Property[] editions;
	private String var = "editions";
	
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
	
	public Property[] getEditions() {
		return this.editions;
	}
	
	public void setEditions(Property[] editions){
		this.editions = editions;
	}
	
	public void doTag() throws IOException {
		//Get the editions from the channel
		try {
			getJspContext().setAttribute(this.var, 
					this.channel.getProperty("editions").getProperties("edition"));
		} catch (Exception e){
			//If there is an exception return nothing
			String[] noEditions = {"None"};
			getJspContext().setAttribute(this.var, noEditions);
		}
	}
}