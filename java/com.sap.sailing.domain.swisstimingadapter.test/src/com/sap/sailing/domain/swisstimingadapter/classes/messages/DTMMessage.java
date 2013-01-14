package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class DTMMessage {
    private String raceId;
    private int markIndex;
    private String sailNumber;
    private double distance;

    public DTMMessage(String raceId, int markIndex, String sailNumber, double distance) {
        super();
        this.raceId = raceId;
        this.markIndex = markIndex;
        this.sailNumber = sailNumber;
        this.distance = distance;
    }

    public DTMMessage() {
        super();
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public int getMarkIndex() {
        return markIndex;
    }

    public void setMarkIndex(int markIndex) {
        this.markIndex = markIndex;
    }

    public String getSailNumber() {
        return sailNumber;
    }

    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "DTM|" + raceId + ";" + markIndex + ";" + sailNumber;
    }

}
