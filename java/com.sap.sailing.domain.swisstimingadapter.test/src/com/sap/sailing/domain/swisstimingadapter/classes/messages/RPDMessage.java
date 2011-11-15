package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sap.sailing.domain.base.TimePoint;

public class RPDMessage {
    private String raceId;
    private int status;
    private TimePoint dataTime;
    private TimePoint startTime;
    private Date raceTime;
    private int nextMarkLeader;
    private double distanceToNextMarkLeader;
    private List<RacePositionDataElement> racePositionElements;
    
    
    
    public RPDMessage(String raceId, int status, TimePoint dataTime, TimePoint startTime, Date raceTime,
            int nextMarkLeader, double distanceToNextMarkLeader, List<RacePositionDataElement> racePositionElements) {
        super();
        this.raceId = raceId;
        this.status = status;
        this.dataTime = dataTime;
        this.startTime = startTime;
        this.raceTime = raceTime;
        this.nextMarkLeader = nextMarkLeader;
        this.distanceToNextMarkLeader = distanceToNextMarkLeader;
        this.racePositionElements = racePositionElements;
    }
    public RPDMessage() {
        racePositionElements = new ArrayList<RacePositionDataElement>();
    }
    public String getRaceId() {
        return raceId;
    }
    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Date getRaceTime() {
        return raceTime;
    }
    public void setRaceTime(Date raceTime) {
        this.raceTime = raceTime;
    }
    public int getNextMarkLeader() {
        return nextMarkLeader;
    }
    public void setNextMarkLeader(int nextMarkLeader) {
        this.nextMarkLeader = nextMarkLeader;
    }
    public double getDistanceToNextMarkLeader() {
        return distanceToNextMarkLeader;
    }
    public void setDistanceToNextMarkLeader(double distanceToNextMarkLeader) {
        this.distanceToNextMarkLeader = distanceToNextMarkLeader;
    }
    public List<RacePositionDataElement> getRacePositionElements() {
        return racePositionElements;
    }
    public void setRacePositionElements(List<RacePositionDataElement> racePositionElements) {
        this.racePositionElements = racePositionElements;
    }
    
    
    public TimePoint getDataTime() {
        return dataTime;
    }
    public void setDataTime(TimePoint dataTime) {
        this.dataTime = dataTime;
    }
    public TimePoint getStartTime() {
        return startTime;
    }
    public void setStartTime(TimePoint startTime) {
        this.startTime = startTime;
    }
    public String toString(){
        SimpleDateFormat sdataTime = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat sstartTime = new SimpleDateFormat("yyyy.MM.dd");
        SimpleDateFormat sraceTime = new SimpleDateFormat("yyyy.MM.dd");
    
        String elements = "";
        for (RacePositionDataElement el : racePositionElements) {
            elements = elements + el.toString();
        }
        
        return "RPD|" + raceId  + "|" + status + "|" + sdataTime.format(dataTime) + "|" + sstartTime.format(startTime) + "|" + sraceTime.format(raceTime) + "|"  + racePositionElements.size() + elements;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(distanceToNextMarkLeader);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + nextMarkLeader;
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((racePositionElements == null) ? 0 : racePositionElements.hashCode());
        result = prime * result + ((raceTime == null) ? 0 : raceTime.hashCode());
        result = prime * result + status;
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
        RPDMessage other = (RPDMessage) obj;
        if (Double.doubleToLongBits(distanceToNextMarkLeader) != Double
                .doubleToLongBits(other.distanceToNextMarkLeader))
            return false;
        if (nextMarkLeader != other.nextMarkLeader)
            return false;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        if (racePositionElements == null) {
            if (other.racePositionElements != null)
                return false;
        } else if (!racePositionElements.equals(other.racePositionElements))
            return false;
        if (raceTime == null) {
            if (other.raceTime != null)
                return false;
        } else if (!raceTime.equals(other.raceTime))
            return false;
        if (status != other.status)
            return false;
        return true;
    }
    
    
    
}
