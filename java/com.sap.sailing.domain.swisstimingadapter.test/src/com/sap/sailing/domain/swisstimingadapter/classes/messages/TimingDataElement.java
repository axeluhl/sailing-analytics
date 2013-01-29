package com.sap.sailing.domain.swisstimingadapter.classes.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimingDataElement {
    private int markIndex;
    private int rank;
    private Date timeSinceStart;

    public TimingDataElement(int markIndex, int rank, Date timeSinceStart) {
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

    public Date getTimeSinceStart() {
        return timeSinceStart;
    }

    public void setTimeSinceStart(Date timeSinceStart) {
        this.timeSinceStart = timeSinceStart;
    }

    @Override
    public String toString() {
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
        return "|" + markIndex + ";" + rank + ";" + sd.format(timeSinceStart);
    }

}
