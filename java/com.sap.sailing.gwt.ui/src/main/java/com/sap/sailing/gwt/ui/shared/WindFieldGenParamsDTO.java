package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.PositionDTO;

public class WindFieldGenParamsDTO implements IsSerializable {

    private PositionDTO northWest;
    private PositionDTO southEast;

    private int xRes;
    private int yRes;
    private int borderY;
    private int borderX;

    private Date startTime;
    private Date endTime;
    private Date timeStep;

    private boolean keepState;
    
    /**
     * Currently m is for measured
     */
    private char mode;
    
    /**
     * Show the wind arrows in wind display and replay modes.
     */
    private boolean showArrows;
    
    /**
     * Show the "heat map" and the wind lines in the wind display and replay modes.
     */
    private boolean showGrid;

    private boolean showStreamlets;
    private boolean showStreamlets2;

    private boolean showLines;
    
    public boolean showOmniscient = true;
    public boolean showOpportunist = true;

    private char seedLines;

    
    public WindFieldGenParamsDTO() {
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

    public int getBorderY() {
        return borderY;
    }
    
    public int getBorderX() {
    	return borderX;
    }

    public void setBorder(int border) {
        this.borderY = border;
        this.borderX = (int)Math.round(border * ((double)xRes) / ((double)yRes));
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

    public void setDefaultTimeSettings(Date startTime, Date timeStep, Date endTime) {
    	this.startTime = startTime;
    	this.timeStep = timeStep;
    	this.endTime = endTime;    	
    }

    public char getMode() {
        return mode;
    }

    public void setMode(char mode) {
        this.mode = mode;
    }

    public boolean isKeepState() {
        return keepState;
    }

    public void setKeepState(boolean keepState) {
        this.keepState = keepState;
    }

    public boolean isShowArrows() {
        return showArrows;
    }

    public void setShowArrows(boolean showArrows) {
        this.showArrows = showArrows;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isShowStreamlets() {
        return showStreamlets;
    }

    public void setShowStreamlets(boolean showStreamlets) {
        this.showStreamlets = showStreamlets;
    }

    public boolean isShowStreamlets2() {
        return showStreamlets2;
    }

    public void setShowStreamlets2(boolean showStreamlets2) {
        this.showStreamlets2 = showStreamlets2;
    }

    public boolean isShowLines() {
        return showLines;
    }

    public void setShowLines(boolean showLines) {
        this.showLines = showLines;
    }

    public char getSeedLines() {
        return seedLines;
    }

    public void setSeedLines(char seedLines) {
        this.seedLines = seedLines;
    }

}
