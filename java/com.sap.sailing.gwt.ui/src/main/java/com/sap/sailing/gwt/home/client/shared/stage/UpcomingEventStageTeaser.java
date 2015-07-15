package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

public class UpcomingEventStageTeaser extends StageTeaser {

    private final UpcomingEventStageTeaserBand teaserBand;

    public UpcomingEventStageTeaser(EventStageDTO event, DesktopPlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getDisplayName());
        subtitle.setInnerText(event.getLocationAndVenue());
        
        bandCount.setAttribute("data-bandcount", "1");
        teaserBand = new UpcomingEventStageTeaserBand(event, placeNavigator);
        stageTeaserBandsPanel.getElement().appendChild(teaserBand.getElement());
    }

    @Override
    protected void handleUserAction() {
        teaserBand.actionLinkClicked(null);
    }
}
