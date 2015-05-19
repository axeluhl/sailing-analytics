package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

import com.sap.sailing.domain.common.RegattaName;

public class EventOverviewRegattaTickerStageDTO extends EventOverviewTickerStageDTO {

    private RegattaName regattaIdentifier;

    @SuppressWarnings("unused")
    private EventOverviewRegattaTickerStageDTO() {
    }

    public EventOverviewRegattaTickerStageDTO(RegattaName identifier, String tickerInfo, Date startTime) {
        super(startTime, tickerInfo);
        this.regattaIdentifier = identifier;
    }

    public RegattaName getRegattaIdentifier() {
        return regattaIdentifier;
    }

}
