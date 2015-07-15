package com.sap.sailing.simulator;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.Position;

public class SimulationResults {

    private Date startTime;
    private long timeStep;
    private long legDuration;
    private Position startPosition;
    private Position endPosition;
    private Map<PathType, Path> paths;
    private String notificationMessage;

    public SimulationResults(){
        this.startTime = null;
        this.timeStep = 0;
        this.legDuration = 0;
        this.paths = null;
        this.notificationMessage = "";
    }

    public SimulationResults(final Date startTime, final long timeStep, final long legDuration, final Position startPosition, final Position endPosition, final Map<PathType, Path> paths, final String notificationMessage) {
        this.startTime = startTime;
        this.timeStep = timeStep;
        this.legDuration = legDuration;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.paths = paths;
        this.notificationMessage = notificationMessage;
    }
    
    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public long getTimeStep() {
        return this.timeStep;
    }

    public void setTimeStep(long timeStep) {
        this.timeStep = timeStep;
    }

    public long getLegDuration() {
        return this.legDuration;
    }

    public void setLegDuration(long legDuration) {
        this.legDuration = legDuration;
    }
    
    public Position getStartPosition() {
        return this.startPosition;
    }
    
    public void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }
    
    public Position getEndPosition() {
        return this.endPosition;
    }
    
    public void setEndPosition(Position endPosition) {
        this.endPosition = endPosition;
    }
    
    public Map<PathType, Path> getPaths() {
        return this.paths;
    }

    public void setPaths(final Map<PathType, Path> paths) {
        this.paths = paths;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(final String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}
