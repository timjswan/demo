package com.eidosmedia.tags.channelEditionName;

import java.io.IOException;

import javax.servlet.jsp.tagext.*;
import com.eidosmedia.siteconfig.Property;

public class EditionNameTag extends SimpleTagSupport {
	private String editionName;
	private Property edition;
	private String var = "editionName";	
	
	public Property getEdition() {
		return edition;
	}
	
	public void setEdition(Property edition){
		this.edition = edition;
	}
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	public String getEditionName() {		
		return editionName;
	}
	
	public void setEditionName(String editionName) {
		this.editionName = editionName;
	}

	public void doTag() throws IOException {
		getJspContext().setAttribute(var, edition.getAttribute("name"));
	}
}