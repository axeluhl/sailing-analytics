package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

public class UpcomingEventStageTeaser extends StageTeaser {

    public UpcomingEventStageTeaser(EventStageDTO event, HomePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getDisplayName());
        subtitle.setInnerText(event.getLocationAndVenue());
        
        bandCount.setAttribute("data-bandcount", "1");
        stageTeaserBandsPanel.getElement().appendChild(new UpcomingEventStageTeaserBand(event, placeNavigator).getElement());
    }
}
