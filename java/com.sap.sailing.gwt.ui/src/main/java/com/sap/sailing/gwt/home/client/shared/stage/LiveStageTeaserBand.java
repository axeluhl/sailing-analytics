package com.sap.sailing.gwt.home.client.shared.stage;

import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class LiveStageTeaserBand extends StageTeaserBand {

    public LiveStageTeaserBand(EventDTO event, PlaceNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));

        actionLink.setVisible(true);
        actionLink.setText("Show event");
    }

    @Override
    public void actionLinkClicked() {
        EventDTO event = getEvent();
        getPlaceNavigator().goToEvent(event.id.toString(), event.getBaseURL());
    }
}
