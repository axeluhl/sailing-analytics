package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class UpcomingEventStageTeaser extends StageTeaser {

    public UpcomingEventStageTeaser(EventDTO event, PlaceNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getName());
        subtitle.setInnerText(event.venue.getName());
        
        countDown.setAttribute("data-starttime", "");
        countDown.setAttribute("data-days", "3");
        countDown.setAttribute("data-hours", "2");
        countDown.setAttribute("data-minutes", "");
        countDown.setAttribute("data-seconds", "");

        bandCount.setAttribute("data-bandcount", "1");
        stageTeaserBandsPanel.add(new StageTeaserBand(event, placeNavigator));
    }

}
