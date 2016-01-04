package com.sap.sailing.gwt.home.mobile.partials.stage;

import com.sap.sailing.gwt.home.communication.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil;

/**
 * Teaser band with no action on the homepage stage
 * 
 */
public class NoActionStageTeaserBand extends StageTeaserBand {

    public NoActionStageTeaserBand(EventListEventDTO event, MobilePlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getDisplayName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
    }
}
