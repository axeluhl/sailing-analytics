package com.sap.sailing.gwt.home.mobile.partials.stage;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

public class LiveEventStageTeaser extends StageTeaser {

    private final LiveStageTeaserBand teaserBand;

    public LiveEventStageTeaser(EventStageDTO event, MobilePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getDisplayName());
        subtitle.setInnerText(event.getLocationAndVenue());
        
        countdown.getStyle().setDisplay(Display.NONE);
        countdown.getStyle().setVisibility(Visibility.HIDDEN);
        
        bandCount.setAttribute("data-bandcount", "1");
        
        teaserBand = new LiveStageTeaserBand(event, placeNavigator);
        stageTeaserBandsPanel.getElement().appendChild(teaserBand.getElement());
    }

    @Override
    protected void handleUserAction() {
        teaserBand.actionLinkClicked(null);
    }

}
