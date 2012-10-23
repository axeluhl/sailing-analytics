package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindFieldGenParamsDTO implements IsSerializable {

    private PositionDTO northWest;
    private PositionDTO southEast;

    private int xRes;
    private int yRes;

    private Date startTime;
    private Date endTime;
    private Date timeStep;

    /**
     * Currently m is for measured
     */
    private char mode;
    
    public WindFieldGenParamsDTO() {
        setDefaultTimeSettings();
    }

    public PositionDTO getNorthWest() {
        return northWest;
    }

    public void setNorthWest(PositionDTO northWest) {
        this.northWest = northWest;
    }

    public PositionDTO getSouthEast() {
        return southEast;
    }

    public void setSouthEast(PositionDTO southEast) {
        this.southEast = southEast;
    }

    public int getxRes() {
        return xRes;
    }

    public void setxRes(int xRes) {
        this.xRes = xRes;
    }

    public int getyRes() {
        return yRes;
    }

    public void setyRes(int yRes) {
        this.yRes = yRes;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(Date timeStep) {
        this.timeStep = timeStep;
    }

    @SuppressWarnings("deprecation")
    public void setDefaultTimeSettings() {
        startTime = new Date();// new Date(0);
        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        timeStep = new Date(15 * 1000);
        endTime = new Date(startTime.getTime() + 10 * 60 * 1000);
    }

    public char getMode() {
        return mode;
    }

    public void setMode(char mode) {
        this.mode = mode;
    }
}
