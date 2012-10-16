package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class SimulatorResultsDTOAndNotificationMessage implements Serializable {
	
	private static final long	serialVersionUID	= 6890477769867789622L;
	
	private SimulatorResultsDTO simulatorResultsDTO;
	private String notificationMessage = "";
	
	public SimulatorResultsDTO getSimulatorResultsDTO() {
		return this.simulatorResultsDTO;
	}
	
	public void setSimulatorResultsDTO(SimulatorResultsDTO simulatorResultsDTO) {
		this.simulatorResultsDTO = simulatorResultsDTO;
	}
	
	public String getNotificationMessage() {
		return this.notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}