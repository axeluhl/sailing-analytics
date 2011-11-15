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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((sailNumber == null) ? 0 : sailNumber.hashCode());
        result = prime * result + ((sailNumber2 == null) ? 0 : sailNumber2.hashCode());
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
        DBBMessage other = (DBBMessage) obj;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
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
        if (sailNumber2 == null) {
            if (other.sailNumber2 != null)
                return false;
        } else if (!sailNumber2.equals(other.sailNumber2))
            return false;
        return true;
    }
    
    
}
