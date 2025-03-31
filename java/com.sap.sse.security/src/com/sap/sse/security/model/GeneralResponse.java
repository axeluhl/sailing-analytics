package com.sap.sse.security.model;

import java.io.Serializable;

/***
 * This Class will be used to send general response in JSON format.
 * @author Usman Ali
 *
 */
public class GeneralResponse implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private boolean responseStatus;
	private String responseMessage;
	
	public GeneralResponse(boolean responseStatus, String responseMessage) {
		super();
		this.responseStatus = responseStatus;
		this.responseMessage = responseMessage;
	}
	
	public boolean isResponseStatus() {
		return responseStatus;
	}
	public void setResponseStatus(boolean responseStatus) {
		this.responseStatus = responseStatus;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	@Override
	public String toString() {
		return " {\"responseStatus\":\"" + responseStatus + "\", \"responseMessage\":\"" + responseMessage + "\"}";
		
	}
	
	
}
