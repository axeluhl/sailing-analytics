package com.sap.sailing.gwt.home.desktop.partials.stage;

import com.sap.sailing.gwt.home.communication.start.EventStageDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;

public class UpcomingEventStageTeaser extends StageTeaser {

    private final UpcomingEventStageTeaserBand teaserBand;

    public UpcomingEventStageTeaser(EventStageDTO event, DesktopPlacesNavigator placeNavigator) {
        super(event);
        title.setInnerText(event.getDisplayName());
        subtitle.setInnerText(event.getLocationAndVenue());
        countdownTimerUi.setVisible(true);
        bandCount.setAttribute("data-bandcount", "1");
        teaserBand = new UpcomingEventStageTeaserBand(event, placeNavigator);
        stageTeaserBandsPanel.getElement().appendChild(teaserBand.getElement());
    }

    @Override
    protected void handleUserAction() {
        teaserBand.actionLinkClicked(null);
    }
}
