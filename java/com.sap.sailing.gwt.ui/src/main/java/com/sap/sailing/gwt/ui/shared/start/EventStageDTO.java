package com.sap.sailing.gwt.ui.shared.start;

import com.sap.sailing.gwt.home.client.shared.stage.StageEventType;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public class EventStageDTO extends EventMetadataDTO {
    
    private StageEventType stageType;

    public StageEventType getStageType() {
        return stageType;
    }

    public void setStageType(StageEventType stageType) {
        this.stageType = stageType;
    }
}
