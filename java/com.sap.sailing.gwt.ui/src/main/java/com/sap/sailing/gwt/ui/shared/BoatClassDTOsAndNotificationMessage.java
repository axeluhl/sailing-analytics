package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class BoatClassDTOsAndNotificationMessage implements Serializable
{
	private static final long	serialVersionUID	= -8492872623041990707L;
	private BoatClassDTO[] boatClassDTOs;
	private String notificationMessage = "";
	
	public BoatClassDTO[] getBoatClassDTOs() {
		return this.boatClassDTOs;
	}
	
	public void setBoatClassDTOs(BoatClassDTO[] boatClassDTOs) {
		this.boatClassDTOs = boatClassDTOs;
	}
	
	public String getNotificationMessage() {
		return this.notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
