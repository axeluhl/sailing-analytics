package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockAtMarkElement {
    private int markindex;
    private Date marktime;
    private String sailnumber;

    public ClockAtMarkElement(int markindex, Date marktime, String sailnumber) {
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


    public Date getMarktime() {
        return marktime;
    }

    public void setMarktime(Date marktime) {
        this.marktime = marktime;
    }

    public String getSailnumber() {
        return sailnumber;
    }

    public void setSailnumber(String sailnumber) {
        this.sailnumber = sailnumber;
    }

    public String toString() {
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
        return "|" + markindex + ";" + (marktime == null ? "" : sd.format(marktime)) + ";" + sailnumber;
    }


}
