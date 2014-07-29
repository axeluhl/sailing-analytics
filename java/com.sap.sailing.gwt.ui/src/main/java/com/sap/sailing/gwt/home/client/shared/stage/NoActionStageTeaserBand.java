package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class NoActionStageTeaserBand extends StageTeaserBand {

    public NoActionStageTeaserBand(EventBaseDTO event, PlaceNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));
    }
}
