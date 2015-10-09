package com.sap.sailing.gwt.ui.shared.start;

import com.sap.sailing.gwt.ui.shared.general.EventLinkAndMetadataDTO;

public class EventStageDTO extends EventLinkAndMetadataDTO {

    private StageEventType stageType;
    private String stageImageURL;

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
}
