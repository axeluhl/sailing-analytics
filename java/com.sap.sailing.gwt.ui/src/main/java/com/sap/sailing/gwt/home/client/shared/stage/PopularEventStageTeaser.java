package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.dom.client.Style.Display;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class PopularEventStageTeaser extends StageTeaser {

    public PopularEventStageTeaser(EventBaseDTO event, HomePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getName());
        subtitle.setInnerText(event.venue.getName());

        countdown.getStyle().setDisplay(Display.NONE);

        bandCount.setAttribute("data-bandcount", "1");

        stageTeaserBandsPanel.getElement().appendChild(
                new PopularEventStageTeaserBand(event, placeNavigator).getElement());
    }
}
