package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class RacePositionDataElement {
    private String sailNumber;
    private int boatType;
    private long ageOfData;
    private double latitude;
    private double longitude;
    private double sog;
    private double vmg;
    private double cog;
    private int rank;
    private double distanceToLeader;
    private double distanceToNextMark;
    public RacePositionDataElement(String sailNumber, int boatType, long ageOfData, double latitude, double longitude,
            double sog, double vmg, double cog, int rank, double distanceToLeader, double distanceToNextMark) {
        super();
        this.sailNumber = sailNumber;
        this.boatType = boatType;
        this.ageOfData = ageOfData;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sog = sog;
        this.vmg = vmg;
        this.cog = cog;
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
    
    
    
}
