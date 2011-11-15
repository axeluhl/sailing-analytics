package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;

import com.sap.sailing.domain.base.TimePoint;

public class TimingDataElement {
    private int markIndex;
    private int rank;
    private TimePoint timeSinceStart;

    public TimingDataElement(int markIndex, int rank, TimePoint timeSinceStart) {
        super();
        this.markIndex = markIndex;
        this.rank = rank;
        this.timeSinceStart = timeSinceStart;
    }

    public TimingDataElement() {
        super();
    }

 
    public int getMarkIndex() {
        return markIndex;
    }

    public void setMarkIndex(int markIndex) {
        this.markIndex = markIndex;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public TimePoint getTimeSinceStart() {
        return timeSinceStart;
    }

    public void setTimeSinceStart(TimePoint timeSinceStart) {
        this.timeSinceStart = timeSinceStart;
    }

    @Override
    public String toString() {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd");
        return "|" + markIndex + ";" + rank + ";" + sd.format(timeSinceStart);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + markIndex;
        result = prime * result + rank;
        result = prime * result + ((timeSinceStart == null) ? 0 : timeSinceStart.hashCode());
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
        TimingDataElement other = (TimingDataElement) obj;
        if (markIndex != other.markIndex)
            return false;
        if (rank != other.rank)
            return false;
        if (timeSinceStart == null) {
            if (other.timeSinceStart != null)
                return false;
        } else if (!timeSinceStart.equals(other.timeSinceStart))
            return false;
        return true;
    }
    
    
}
