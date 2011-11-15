package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class RacePositionDataElement {
    private String sailNumber;
    private int boatType;
    private long ageOfData;
    private double latitude;
    private double longitude;
    private double sog;
    private double vmg;
    private double als;
    private double cog;
    private int nextMark;
    private int rank;
    private double distanceToLeader;
    private double distanceToNextMark;

    

    public RacePositionDataElement(String sailNumber, int boatType, long ageOfData, double latitude, double longitude,
            double sog, double vmg, double als, double cog, int nextMarkIndex, int rank, double distanceToLeader,
            double distanceToNextMark) {
        super();
        this.sailNumber = sailNumber;
        this.boatType = boatType;
        this.ageOfData = ageOfData;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sog = sog;
        this.vmg = vmg;
        this.als = als;
        this.cog = cog;
        this.nextMark = nextMarkIndex;
        this.rank = rank;
        this.distanceToLeader = distanceToLeader;
        this.distanceToNextMark = distanceToNextMark;
    }

    public RacePositionDataElement() {
        super();
    }
    
    public String getSailNumber() {
        return sailNumber;
    }

    public void setSailNumber(String sailNumber) {
        this.sailNumber = sailNumber;
    }

    public int getBoatType() {
        return boatType;
    }

    public void setBoatType(int boatType) {
        this.boatType = boatType;
    }

    public long getAgeOfData() {
        return ageOfData;
    }

    public void setAgeOfData(long ageOfData) {
        this.ageOfData = ageOfData;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSog() {
        return sog;
    }

    public void setSog(double sog) {
        this.sog = sog;
    }

    public double getVmg() {
        return vmg;
    }

    public void setVmg(double vmg) {
        this.vmg = vmg;
    }

    public double getCog() {
        return cog;
    }

    public void setCog(double cog) {
        this.cog = cog;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getDistanceToLeader() {
        return distanceToLeader;
    }

    public void setDistanceToLeader(double distanceToLeader) {
        this.distanceToLeader = distanceToLeader;
    }

    public double getDistanceToNextMark() {
        return distanceToNextMark;
    }

    public void setDistanceToNextMark(double distanceToNextMark) {
        this.distanceToNextMark = distanceToNextMark;
    }

    public double getAls() {
        return als;
    }

    public void setAls(double als) {
        this.als = als;
    }
    

   
    public int getNextMark() {
        return nextMark;
    }

    public void setNextMark(int nextMark) {
        this.nextMark = nextMark;
    }

    public String toString(){
        return "|" + sailNumber + ";" + boatType + ";" + ageOfData  + ";" + latitude  + ";" + longitude + ";" + sog + ";" + vmg + ";" + als + ";" + cog  + ";" +nextMark + ";" + rank + ";" + distanceToLeader + ";" + distanceToNextMark; 
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (ageOfData ^ (ageOfData >>> 32));
        long temp;
        temp = Double.doubleToLongBits(als);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + boatType;
        temp = Double.doubleToLongBits(cog);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(distanceToLeader);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(distanceToNextMark);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + nextMark;
        result = prime * result + rank;
        result = prime * result + ((sailNumber == null) ? 0 : sailNumber.hashCode());
        temp = Double.doubleToLongBits(sog);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(vmg);
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
        RacePositionDataElement other = (RacePositionDataElement) obj;
        if (ageOfData != other.ageOfData)
            return false;
        if (Double.doubleToLongBits(als) != Double.doubleToLongBits(other.als))
            return false;
        if (boatType != other.boatType)
            return false;
        if (Double.doubleToLongBits(cog) != Double.doubleToLongBits(other.cog))
            return false;
        if (Double.doubleToLongBits(distanceToLeader) != Double.doubleToLongBits(other.distanceToLeader))
            return false;
        if (Double.doubleToLongBits(distanceToNextMark) != Double.doubleToLongBits(other.distanceToNextMark))
            return false;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
            return false;
        if (nextMark != other.nextMark)
            return false;
        if (rank != other.rank)
            return false;
        if (sailNumber == null) {
            if (other.sailNumber != null)
                return false;
        } else if (!sailNumber.equals(other.sailNumber))
            return false;
        if (Double.doubleToLongBits(sog) != Double.doubleToLongBits(other.sog))
            return false;
        if (Double.doubleToLongBits(vmg) != Double.doubleToLongBits(other.vmg))
            return false;
        return true;
    }

    
}
