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

    public String toString(){
        return "WND|" + raceId + ";" + markIndex + ";" + windDirection+ ";" +windSpeed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + markIndex;
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        long temp;
        temp = Double.doubleToLongBits(windDirection);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(windSpeed);
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
        WNDMessage other = (WNDMessage) obj;
        if (markIndex != other.markIndex)
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        if (Double.doubleToLongBits(windDirection) != Double.doubleToLongBits(other.windDirection))
            return false;
        if (Double.doubleToLongBits(windSpeed) != Double.doubleToLongBits(other.windSpeed))
            return false;
        return true;
    }
    
    
}
