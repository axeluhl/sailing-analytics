package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiHandler;
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

    @UiHandler("actionLink")
    public void actionLinkClicked(ClickEvent e) {
        EventDTO event = getEvent();
        getPlaceNavigator().goToEvent(event.id.toString(), event.getBaseURL());
    }
}
