package com.eidosmedia.tags.newUseCase;

import java.util.Random;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import com.eidosmedia.tags.logger.Logger;

public class ProgressHelper {
	private int amount;
	private JspContext jspContext = null;
	private String statusAttributeName;
	private String progressAttributeName;
	private Logger logger = new Logger();
	private int attributeScope = PageContext.APPLICATION_SCOPE;
	private boolean useAttributeScope = true;
	
	public ProgressHelper(int amount){
		this.amount = amount;
	}
	
	public ProgressHelper(int amount, JspContext jspContext, String statusAttributeName, String progressAttributeName){
		this.amount = amount;
		this.jspContext = jspContext;
		String progressId = (String) this.jspContext.getAttribute("progressId", PageContext.APPLICATION_SCOPE);
		this.statusAttributeName = statusAttributeName.concat(progressId);
		this.progressAttributeName = progressAttributeName.concat(progressId);
		
		this.setAttribute("statusAttributeName", this.statusAttributeName);
		this.setAttribute("progressAttributeName", this.progressAttributeName);
	}
	
	public int getProgress(int count){
		int percentComplete = (count * 100) / this.amount;
		return percentComplete;
	}
	
	/**
	 * Generate an id for the progress jstl tag. 
	 * It's easier to pass all params as a string when using jstl and convert them to the types needed here.
	 * 
	 * @param minRangeStr - the minimum the random number can be
	 * @param maxRangeStr - the maximum the random number can be
	 * @param formatOrder - the order the result string should be arranged i.e digits first characters second
	 * @param numberOfCharsStr - the number of characters to concat to the number string
	 */
	public static String generateId(String minRangeStr, String maxRangeStr, String formatOrder, String numberOfCharsStr){
		int minRange = Integer.parseInt(minRangeStr);
		int maxRange = Integer.parseInt(maxRangeStr);
		int numberOfChars = Integer.parseInt(numberOfCharsStr);
		Random rng = new Random();
		long randomNumber = (long)(rng.nextDouble()*(maxRange - minRange));
		String randomStr = NewUseCaseTag.randomCharSequence(rng, numberOfChars);

		return String.format(formatOrder, randomStr, randomNumber);
	}
	
	/**
	 * Set a string attribute that can be accessed within a JSP.
	 * 
	 * @param name - the name of the attribute to retrieve
	 * @param value - String value
	 */	
	public void setAttribute(String name, String value){
		if(this.useAttributeScope){
			this.jspContext.setAttribute(name, value, this.attributeScope);
		} else {
			this.jspContext.setAttribute(name, value);
		}
	}
	
	/**
	 * Set an integer attribute that can be accessed within a JSP.
	 * 
	 * @param name - the name of the attribute to retrieve
	 * @param value - int value
	 */	
	public void setAttribute(String name, int value){
		if(this.useAttributeScope){
			this.jspContext.setAttribute(name, value, this.attributeScope);
		} else {
			this.jspContext.setAttribute(name, value);
		}
	}
	
	/**
	 * Set the progress percentage so it can be retrieved via jQuery AJAX when the user creates a new campaign.
	 * This is calculated from the amount of valid stories in the stories folder using the number in the sequence.
	 * 
	 * @param count - the sequence countof the story being created
	 */
	public void setProgress(int count){
		this.setAttribute(this.progressAttributeName, this.getProgress(count));
		this.logger.log("Progress: " + this.getProgress(count) + ", " + this.amount);
		this.logger.log("Progress attribute: " + this.jspContext.getAttribute(this.progressAttributeName, this.attributeScope));
	}
	
	/**
	 * Get the jsp context.
	 */
	public JspContext getJspContext(){
		return this.jspContext;
	}
	
	/**
	 * Set a status attribute as feedback to the user when the stories have been created.
	 * 
	 * @param - message the message to be displayed
	 */
	public void setStatus(String message){
		if(this.jspContext != null){
			this.setAttribute(this.statusAttributeName, message);
			this.logger.log("Status attribute: " + this.jspContext.getAttribute(this.statusAttributeName, this.attributeScope));
		}
	}
	
	/**
	 * Destroy the JSP context attributes.
	 */
	public void destroy(){
		this.setAttribute(this.progressAttributeName, "");
		this.setAttribute(this.statusAttributeName, "");
	}
}
