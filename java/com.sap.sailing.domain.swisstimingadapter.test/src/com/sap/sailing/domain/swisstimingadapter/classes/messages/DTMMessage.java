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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + markIndex;
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((sailNumber == null) ? 0 : sailNumber.hashCode());
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
        DTMMessage other = (DTMMessage) obj;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
            return false;
        if (markIndex != other.markIndex)
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
        return true;
    }
    
    

}
