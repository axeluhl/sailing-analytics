package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class SimulatedPathsEvenTimedResultDTO implements Serializable
{
	private static final long	serialVersionUID	= 7475673099383147929L;
	
	private PathDTO[] pathDTOs;
	private RaceMapDataDTO raceMapDataDTO;
	private WindFieldDTO windFieldDTO;
	private String notificationMessage;
	
	public PathDTO[] getPathDTOs() {
		return this.pathDTOs;
	}
	
	public void setPathDTOs(PathDTO[] pathDTOs) {
		this.pathDTOs = pathDTOs;
	}
	
	public RaceMapDataDTO getRaceMapDataDTO() {
		return this.raceMapDataDTO;
	}
	
	public void setRaceMapDataDTO(RaceMapDataDTO raceMapDataDTO) {
		this.raceMapDataDTO = raceMapDataDTO;
	}
	
	public WindFieldDTO getWindFieldDTO() {
		return this.windFieldDTO;
	}
	
	public void setWindFieldDTO(WindFieldDTO windFieldDTO) {
		this.windFieldDTO = windFieldDTO;
	}
	
	public String getNotificationMessage() {
		return this.notificationMessage;
	}
	
	public void setNotificationMessage(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
}
