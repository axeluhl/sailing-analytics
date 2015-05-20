package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public class EventOverviewRaceTickerStageDTO extends EventOverviewTickerStageDTO {

    private RegattaAndRaceIdentifier regattaAndRaceIdentifier;

    @SuppressWarnings("unused")
    private EventOverviewRaceTickerStageDTO() {
    }

    public EventOverviewRaceTickerStageDTO(RegattaAndRaceIdentifier identifier, String tickerInfo, Date startTime, String stageImageUrl) {
        super(startTime, tickerInfo, stageImageUrl);
        this.regattaAndRaceIdentifier = identifier;
    }

    public RegattaAndRaceIdentifier getRegattaAndRaceIdentifier() {
        return regattaAndRaceIdentifier;
    }

}
