package com.sap.sailing.gwt.home.mobile.partials.stage;

import com.sap.sailing.gwt.home.communication.event.EventLinkAndMetadataDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;

public class PopularEventStageTeaser extends StageTeaser {

    private final PopularEventStageTeaserBand teaserBand;

    public PopularEventStageTeaser(EventLinkAndMetadataDTO event, MobilePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getDisplayName());
        subtitle.setInnerText(event.getLocationAndVenue());
        bandCount.setAttribute("data-bandcount", "1");
        teaserBand = new PopularEventStageTeaserBand(event, placeNavigator);
        stageTeaserBandsPanel.getElement().appendChild(teaserBand.getElement());
    }

    @Override
    protected void handleUserAction() {
        teaserBand.actionLinkClicked(null);
    }

}
