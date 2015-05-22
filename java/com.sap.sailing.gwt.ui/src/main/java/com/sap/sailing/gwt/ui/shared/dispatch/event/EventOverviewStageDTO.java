package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class EventOverviewStageDTO implements DTO {

    private String eventMessage;
    private EventOverviewStageContentDTO stageContent;

    // TODO news

    @SuppressWarnings("unused")
    private EventOverviewStageDTO() {
    }

    public EventOverviewStageDTO(String eventMessage, EventOverviewStageContentDTO stageContent) {
        super();
        this.eventMessage = eventMessage;
        this.stageContent = stageContent;
    }

    public String getEventMessage() {
        return eventMessage;
    }
    
    public EventOverviewStageContentDTO getStageContent() {
        return stageContent;
    }
}
