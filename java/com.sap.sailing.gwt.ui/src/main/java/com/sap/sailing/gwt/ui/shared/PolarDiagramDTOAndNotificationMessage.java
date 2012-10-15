package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class PolarDiagramDTOAndNotificationMessage implements Serializable {
	
	private static final long	serialVersionUID	= 7385409147081730741L;
	
	private PolarDiagramDTO polarDiagramDTO;
	private String notificationMessage;
	
	public PolarDiagramDTO getPolarDiagramDTO() {
		return this.polarDiagramDTO;
	}
	
	public void setPolarDiagramDTO(PolarDiagramDTO polarDiagramDTO) {
		this.polarDiagramDTO = polarDiagramDTO;
	}
	
	public String getNotificationMessage() {
		return this.notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
