package com.sap.sailing.gwt.home.communication.start;

import com.sap.sailing.gwt.home.communication.event.EventLinkAndMetadataDTO;

public class EventStageDTO extends EventLinkAndMetadataDTO {

    private StageEventType stageType;
    private String stageImageURL;
    private boolean isTrackedByTracTrac;


    @Override
    public StageEventType getStageType() {
        return stageType;
    }

    public void setStageType(StageEventType stageType) {
        this.stageType = stageType;
    }

    public String getStageImageURL() {
        return stageImageURL;
    }

    public void setStageImageURL(String stageImageURL) {
        this.stageImageURL = stageImageURL;
    }
    public boolean isTrackedByTracTrac() {
        return isTrackedByTracTrac;
    }
    
    public void setTrackedByTracTrac(boolean isTrackedByTracTrac) {
        this.isTrackedByTracTrac = isTrackedByTracTrac;
    }
}
