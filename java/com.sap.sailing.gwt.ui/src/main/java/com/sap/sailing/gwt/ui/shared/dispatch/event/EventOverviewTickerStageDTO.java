package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

public class EventOverviewTickerStageDTO implements EventOverviewStageDTO {

    public Date getStartTime() {
        return new Date(new Date().getTime() + 62500);
    }

}
