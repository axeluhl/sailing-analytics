package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class WNDMessage {
    private String raceId;
    private int markIndex;
    private double windDirection;
    private double windSpeed;

    public WNDMessage(String raceId, int markIndex, double windDirection, double windSpeed) {
        super();
        this.raceId = raceId;
        this.markIndex = markIndex;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
    }

    public WNDMessage() {
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

    public double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String toString() {
        return "WND|" + raceId + ";" + markIndex + ";" + windDirection + ";" + windSpeed;
    }
}
