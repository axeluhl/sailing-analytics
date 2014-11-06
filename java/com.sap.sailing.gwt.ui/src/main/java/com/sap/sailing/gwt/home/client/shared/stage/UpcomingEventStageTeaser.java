package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class UpcomingEventStageTeaser extends StageTeaser {

    public UpcomingEventStageTeaser(EventBaseDTO event, HomePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getName());
        subtitle.setInnerText(event.venue.getName());
        
        bandCount.setAttribute("data-bandcount", "1");
        stageTeaserBandsPanel.getElement().appendChild(new UpcomingEventStageTeaserBand(event, placeNavigator).getElement());
    }
}
