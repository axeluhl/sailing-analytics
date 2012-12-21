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
}
