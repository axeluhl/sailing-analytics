package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log with all raw race log entries
 */
public class RaceLogDTO extends AbstractLogDTO<RaceLogEventDTO> implements IsSerializable {
    private String raceColumnName;
    private String fleetName;
    private Integer currentPassId;

    RaceLogDTO() {}

    public RaceLogDTO(String leaderboardName, String raceColumnName, String fleetName, Integer currentPassId, List<RaceLogEventDTO> entries) {
        super(leaderboardName, entries);
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.currentPassId = currentPassId;
    }

    public String getRaceColumnName() {
        return raceColumnName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public Integer getCurrentPassId() {
        return currentPassId;
    }

}
