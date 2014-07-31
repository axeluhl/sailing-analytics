package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.dom.client.Style.Display;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class LiveStageTeaserBand extends StageTeaserBand {

    public LiveStageTeaserBand(EventBaseDTO event, PlaceNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));

        actionLink.getStyle().setDisplay(Display.INLINE_BLOCK);
        actionLink.setInnerText("Show event");
    }

    @Override
    public void actionLinkClicked() {
        EventBaseDTO event = getEvent();
        getPlaceNavigator().goToEvent(event.id.toString(), event.getBaseURL());
    }
}
