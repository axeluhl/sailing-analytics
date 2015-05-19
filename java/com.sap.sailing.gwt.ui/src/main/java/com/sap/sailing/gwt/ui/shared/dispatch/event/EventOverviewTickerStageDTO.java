package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

public class EventOverviewTickerStageDTO implements EventOverviewStageDTO {

    private Date startTime;
    private String tickerInfo;

    protected EventOverviewTickerStageDTO() {
    }

    public EventOverviewTickerStageDTO(Date startTime, String tickerInfo) {
        this.startTime = startTime;
        this.tickerInfo = tickerInfo;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getTickerInfo() {
        return tickerInfo;
    }

}
