package com.sap.sailing.domain.racelog;

public interface RaceLogEventVisitor {
    public void visit(RaceLogFlagEvent event);

    public void visit(RaceLogPassChangeEvent event);

    public void visit(RaceLogRaceStatusEvent event);

    public void visit(RaceLogStartTimeEvent event);

    public void visit(RaceLogCourseAreaChangedEvent event);
    
    public void visit(RaceLogCourseDesignChangedEvent event);
    
    public void visit(RaceLogFinishPositioningListChangedEvent event);
    
    public void visit(RaceLogFinishPositioningConfirmedEvent event);
    
    public void visit(RaceLogPathfinderEvent event);
    
    public void visit(RaceLogGateLineOpeningTimeEvent event);
}
