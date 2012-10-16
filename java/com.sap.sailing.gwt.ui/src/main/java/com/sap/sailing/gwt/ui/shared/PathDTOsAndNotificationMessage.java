package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PathDTOsAndNotificationMessage implements IsSerializable {
	
	private PathDTO[] pathDTOs;
	private String notificationMessage;
	
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
