package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RPDMessage {
    private String raceId;
    private int status;
    private Date dataTime;
    private Date startTime;
    private Date raceTime;
    private int nextMarkLeader;
    private double distanceToNextMarkLeader;
    private List<RacePositionDataElement> racePositionElements;

    public RPDMessage(String raceId, int status, Date dataTime, Date startTime, Date raceTime, int nextMarkLeader,
            double distanceToNextMarkLeader, List<RacePositionDataElement> racePositionElements) {
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

    public Date getDataTime() {
        return dataTime;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String toString() {
        SimpleDateFormat sdataTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat sstartTime = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sraceTime = new SimpleDateFormat("HH:mm:ss");

        String sdataTimeString = dataTime == null ? "" : sdataTime.format(dataTime);
        String sec3 = new String(sdataTimeString);
        String sdataTimeStringWithColon = sec3.substring(0, sec3.length()-3) + ":" +  sec3.substring(sec3.length()-2, sec3.length());
        
        String sstartTimeString = startTime == null ? "" : sstartTime.format(startTime);
        String ssraceTimeString = raceTime == null ? "" : sraceTime.format(raceTime);
        
        String elements = "";
        for (RacePositionDataElement el : racePositionElements) {
            elements = elements + el.toString();
        }

        return "RPD|" + raceId + "|" + status + "|" + sdataTimeStringWithColon + "|"
                + sstartTimeString + "|" + ssraceTimeString + "|" + nextMarkLeader + "|" + distanceToNextMarkLeader + "|" 
                + racePositionElements.size() + elements;
    }
}
