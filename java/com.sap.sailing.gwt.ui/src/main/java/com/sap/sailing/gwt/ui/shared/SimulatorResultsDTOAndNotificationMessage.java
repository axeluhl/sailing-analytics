package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorResultsDTOAndNotificationMessage implements IsSerializable {

	private SimulatorResultsDTO simulatorResultsDTO;
	private String notificationMessage;
	
	public SimulatorResultsDTOAndNotificationMessage() {
		this.notificationMessage = "";
	}
	
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