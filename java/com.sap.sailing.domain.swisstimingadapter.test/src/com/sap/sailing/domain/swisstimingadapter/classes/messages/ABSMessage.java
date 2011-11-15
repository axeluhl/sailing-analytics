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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leg == null) ? 0 : leg.hashCode());
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
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
        ABSMessage other = (ABSMessage) obj;
        if (leg == null) {
            if (other.leg != null)
                return false;
        } else if (!leg.equals(other.leg))
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
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
