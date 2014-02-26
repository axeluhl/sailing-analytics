package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for a race log with all raw race log entries
 */
public class RaceLogDTO implements IsSerializable {
    private String leaderboardName;
    private String raceColumnName;
    private String fleetName;
    private Integer currentPassId;
    private List<RaceLogEventDTO> entries;

    RaceLogDTO() {}

    public RaceLogDTO(String leaderboardName, String raceColumnName, String fleetName, Integer currentPassId, List<RaceLogEventDTO> entries) {
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
        this.fleetName = fleetName;
        this.currentPassId = currentPassId;
        this.entries = entries;
    }

    public String getLeaderboardName() {
        return leaderboardName;
    }

    public String getRaceColumnName() {
        return raceColumnName;
    }

    public String getFleetName() {
        return fleetName;
    }

    public List<RaceLogEventDTO> getEntries() {
        return entries;
    }

    public Integer getCurrentPassId() {
        return currentPassId;
    }

}
