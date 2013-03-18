package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class ABSMessage {
    private String raceId;
    private String leg;
    private String sailNumber;
    private double speed;

    public ABSMessage(String raceId, String leg, String sailNumber, double speed) {
        super();
        this.raceId = raceId;
        this.leg = leg;
        this.sailNumber = sailNumber;
        this.speed = speed;
    }

    public ABSMessage() {
        super();
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    public String getLeg() {
        return leg;
    }

    public void setLeg(String leg) {
        this.leg = leg;
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

    public String toString() {
        return "ABS|" + raceId + ";" + leg + ";" + sailNumber + ";" + speed;
    }

    
}
