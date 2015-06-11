package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

/**
 * Teaser band with no action on the homepage stage
 * 
 */
public class NoActionStageTeaserBand extends StageTeaserBand {

    public NoActionStageTeaserBand(EventStageDTO event, DesktopPlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getDisplayName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));
    }
}
