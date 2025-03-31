package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class CBSMessage {
    private int raceId;
    private String sailNumber;
    private double speed;
    public CBSMessage(int raceId, String sailNumber, double speed) {
        super();
        this.raceId = raceId;
        this.sailNumber = sailNumber;
        this.speed = speed;
    }
    public CBSMessage() {
        super();
    }
    public int getRaceId() {
        return raceId;
    }
    public void setRaceId(int raceId) {
        this.raceId = raceId;
    }
    public String getSailNumber() {
        return sailNumber;
    }
    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }
    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    @Override
    public String toString() {
        return "CBS|" + raceId + ";" + sailNumber + ";" + speed;
    }
}
