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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + raceId;
        result = prime * result + ((sailNumber == null) ? 0 : sailNumber.hashCode());
        long temp;
        temp = Double.doubleToLongBits(speed);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CBSMessage other = (CBSMessage) obj;
        if (raceId != other.raceId)
            return false;
        if (sailNumber == null) {
            if (other.sailNumber != null)
                return false;
        } else if (!sailNumber.equals(other.sailNumber))
            return false;
        if (Double.doubleToLongBits(speed) != Double.doubleToLongBits(other.speed))
            return false;
        return true;
    }
    
    
}
