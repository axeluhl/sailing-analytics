package com.sap.sailing.domain.yellowbrickadapter.impl;

public class TeamPositions {
    private final int deviceSerialNumber;
    private final int competitorIdWithinRace;
    private final String competitorName;
    private final Iterable<TeamPosition> positions;

    public TeamPositions(int deviceSerialNumber, int competitorIdWithinRace, String competitorName,
            Iterable<TeamPosition> positions) {
        super();
        this.deviceSerialNumber = deviceSerialNumber;
        this.competitorIdWithinRace = competitorIdWithinRace;
        this.competitorName = competitorName;
        this.positions = positions;
    }

    public int getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public int getCompetitorIdWithinRace() {
        return competitorIdWithinRace;
    }

    public String getCompetitorName() {
        return competitorName;
    }

    public Iterable<TeamPosition> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "TeamPositions [deviceSerialNumber=" + deviceSerialNumber + ", competitorIdWithinRace="
                + competitorIdWithinRace + ", competitorName=" + competitorName + ", positions=" + positions + "]";
    }
}
