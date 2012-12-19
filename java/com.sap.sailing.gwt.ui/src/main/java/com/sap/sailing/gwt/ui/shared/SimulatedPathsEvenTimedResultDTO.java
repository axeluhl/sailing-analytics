package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatedPathsEvenTimedResultDTO implements IsSerializable
{
    public PathDTO[] pathDTOs;
    public RaceMapDataDTO raceMapDataDTO;
    public WindFieldDTO windFieldDTO;
    public String notificationMessage;

    public SimulatedPathsEvenTimedResultDTO() {

    }

    public SimulatedPathsEvenTimedResultDTO(final PathDTO[] pathDTOs, final RaceMapDataDTO raceMapDataDTO, final WindFieldDTO windFieldDTO,
            final String notificationMessage) {
        this.pathDTOs = pathDTOs;
        this.raceMapDataDTO = raceMapDataDTO;
        this.windFieldDTO = windFieldDTO;
        this.notificationMessage = notificationMessage;
    }

    // public PathDTO[] getPathDTOs() {
    // return this.pathDTOs;
    // }
    //
    // public void setPathDTOs(final PathDTO[] pathDTOs) {
    // this.pathDTOs = pathDTOs;
    // }
    //
    // public RaceMapDataDTO getRaceMapDataDTO() {
    // return this.raceMapDataDTO;
    // }
    //
    // public void setRaceMapDataDTO(final RaceMapDataDTO raceMapDataDTO) {
    // this.raceMapDataDTO = raceMapDataDTO;
    // }
    //
    // public WindFieldDTO getWindFieldDTO() {
    // return this.windFieldDTO;
    // }
    //
    // public void setWindFieldDTO(final WindFieldDTO windFieldDTO) {
    // this.windFieldDTO = windFieldDTO;
    // }
    //
    // public String getNotificationMessage() {
    // return this.notificationMessage;
    // }
    //
    // public void setNotificationMessage(final String notificationMessage) {
    // this.notificationMessage = notificationMessage;
    // }
}
