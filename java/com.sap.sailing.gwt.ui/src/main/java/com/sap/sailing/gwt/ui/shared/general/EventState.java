package com.sap.sailing.gwt.ui.shared.general;


public enum EventState {
    PLANNED(LabelType.NONE), UPCOMING(LabelType.UPCOMING), RUNNING(LabelType.LIVE, LabelType.LIVE), FINISHED(LabelType.FINISHED);
    
    private final LabelType stateMarker;
    private final LabelType listStateMarker;

    private EventState(LabelType stateMarker) {
        this(stateMarker, LabelType.NONE);
    }
    
    private EventState(LabelType stateMarker, LabelType listStateMarker) {
        this.stateMarker = stateMarker;
        this.listStateMarker = listStateMarker;
    }
    
    public LabelType getStateMarker() {
        return stateMarker;
    }
    
    public LabelType getListStateMarker() {
        return listStateMarker;
    }
}