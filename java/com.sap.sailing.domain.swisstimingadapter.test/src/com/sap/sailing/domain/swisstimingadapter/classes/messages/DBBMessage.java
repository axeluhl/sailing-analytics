package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class DBBMessage {
    private String raceId;
    private String sailNumber;
    private String sailNumber2;
    private double distance;
    public DBBMessage(String raceId, String sailNumber, String sailNumber2, double distance) {
        super();
        this.raceId = raceId;
        this.sailNumber = sailNumber;
        this.sailNumber2 = sailNumber2;
        this.distance = distance;
    }
    public DBBMessage() {
        super();
    }
    public String getRaceId() {
        return raceId;
    }
    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
    public String getSailNumber() {
        return sailNumber;
    }
    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }
    public String getSailNumber2() {
        return sailNumber2;
    }
    public void setSailNumber2(String sailNumber2) {
        this.sailNumber2 = sailNumber2;
    }
    public double getDistance() {
        return distance;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    @Override
    public String toString() {
        return "DBB|" + raceId + ";" +  sailNumber + ";" +  sailNumber2;
    }
}
