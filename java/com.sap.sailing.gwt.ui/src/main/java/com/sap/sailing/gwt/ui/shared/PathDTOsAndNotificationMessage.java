package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class PathDTOsAndNotificationMessage implements Serializable
{
	private static final long	serialVersionUID	= 3181973716884838685L;
	private PathDTO[] pathDTOs;
	private String notificationMessage = "";
	
	public PathDTO[] getPathDTOs() {
		return this.pathDTOs;
	}
	
	public void setPathDTOs(PathDTO[] pathDTOs) {
		this.pathDTOs = pathDTOs;
	}
	
	public String getNotificationMessage() {
		return this.notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
