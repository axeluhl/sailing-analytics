package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;

import com.sap.sailing.domain.base.TimePoint;

public class ClockAtMarkElement {
    private int markindex;
    private TimePoint marktime;
    private String sailnumber;

    public ClockAtMarkElement(int markindex, TimePoint marktime, String sailnumber) {
        super();
        this.markindex = markindex;
        this.marktime = marktime;
        this.sailnumber = sailnumber;
    }

    public ClockAtMarkElement() {
        super();
    }

    public int getMarkindex() {
        return markindex;
    }

    public void setMarkindex(int markindex) {
        this.markindex = markindex;
    }

    public TimePoint getMarktime() {
        return marktime;
    }

    public void setMarktime(TimePoint marktime) {
        this.marktime = marktime;
    }

    public String getSailnumber() {
        return sailnumber;
    }

    public void setSailnumber(String sailnumber) {
        this.sailnumber = sailnumber;
    }

    public String toString() {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd");
        return "|" + markindex + ";" + sd.format(marktime) + ";" + sailnumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + markindex;
        result = prime * result + ((marktime == null) ? 0 : marktime.hashCode());
        result = prime * result + ((sailnumber == null) ? 0 : sailnumber.hashCode());
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
        ClockAtMarkElement other = (ClockAtMarkElement) obj;
        if (markindex != other.markindex)
            return false;
        if (marktime == null) {
            if (other.marktime != null)
                return false;
        } else if (!marktime.equals(other.marktime))
            return false;
        if (sailnumber == null) {
            if (other.sailnumber != null)
                return false;
        } else if (!sailnumber.equals(other.sailnumber))
            return false;
        return true;
    }
    
    

}
