package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class STTMessage {
    private String raceId;
    private Date startTime;
    public STTMessage(String raceId, Date startTime) {
        super();
        this.raceId = raceId;
        this.startTime = startTime;
    }
    public STTMessage() {
        super();
    }
    public String getRaceId() {
        return raceId;
    }
    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }
    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
  
    
    @Override
    public String toString() {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd");
        return "STT|" + raceId + sd.format(startTime);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((raceId == null) ? 0 : raceId.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
        STTMessage other = (STTMessage) obj;
        if (raceId == null) {
            if (other.raceId != null)
                return false;
        } else if (!raceId.equals(other.raceId))
            return false;
        if (startTime == null) {
            if (other.startTime != null)
                return false;
        } else if (!startTime.equals(other.startTime))
            return false;
        return true;
    }
    
    
}
