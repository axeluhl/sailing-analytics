package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class UpcomingEventStageTeaser extends StageTeaser {

    public UpcomingEventStageTeaser(EventDTO event, PlaceNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getName());
        subtitle.setInnerText(event.venue.getName());
        
        // EventDatesFormatterUtil.formatDateRangeWithYear(featuredEvent.startDate, featuredEvent.endDate))
        
        bandCount.setAttribute("data-bandcount", "2");
        
        stageTeaserBandsPanel.add(new StageTeaserBand(event, placeNavigator));
        stageTeaserBandsPanel.add(new StageTeaserBand(event, placeNavigator));
    }

}
