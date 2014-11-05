package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Visibility;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class LiveEventStageTeaser extends StageTeaser {

    public LiveEventStageTeaser(EventBaseDTO event, HomePlacesNavigator placeNavigator) {
        super(event);

        title.setInnerText(event.getName());
        subtitle.setInnerText(event.venue.getName());
        
        countdown.getStyle().setDisplay(Display.NONE);
        countdown.getStyle().setVisibility(Visibility.HIDDEN);
        
        bandCount.setAttribute("data-bandcount", "1");
        
        stageTeaserBandsPanel.getElement().appendChild(new LiveStageTeaserBand(event, placeNavigator).getElement());
    }

}
