package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

public class EventOverviewTickerStageDTO implements EventOverviewStageContentDTO {

    private Date startTime;
    private String tickerInfo;
    private String stageImageUrl;

    protected EventOverviewTickerStageDTO() {
    }

    public EventOverviewTickerStageDTO(Date startTime, String tickerInfo, String stageImageUrl) {
        this.startTime = startTime;
        this.tickerInfo = tickerInfo;
        this.stageImageUrl = stageImageUrl;
    }

    public Date getStartTime() {
        return startTime;
    }

    public String getTickerInfo() {
        return tickerInfo;
    }

    public String getStageImageUrl() {
        return stageImageUrl;
    }
}
