package com.sap.sailing.gwt.ui.shared.dispatch.event;

public class EventOverviewVideoStageDTO implements EventOverviewStageContentDTO {
    public enum Type {
        LIVESTREAM, HIGHLIGHTS, MEDIA
    }
    
    private Type type;
    
    @SuppressWarnings("unused")
    private EventOverviewVideoStageDTO() {
    }

    public EventOverviewVideoStageDTO(Type type) {
        super();
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
}
