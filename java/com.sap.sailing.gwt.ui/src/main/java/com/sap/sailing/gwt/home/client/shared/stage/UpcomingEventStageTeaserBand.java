package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.dom.client.Style.Display;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

/**
 * Teaser band for an upcoming event on the homepage stage
 * @author Frank
 *
 */
public class UpcomingEventStageTeaserBand extends StageTeaserBand {

    public UpcomingEventStageTeaserBand(EventBaseDTO event, PlaceNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));

        actionLink.getStyle().setDisplay(Display.INLINE_BLOCK);
        actionLink.setInnerText(TextMessages.INSTANCE.moreInfo());
    }

    @Override
    public void actionLinkClicked() {
        EventBaseDTO event = getEvent();
        PlaceNavigation<EventPlace> eventNavigation = getPlaceNavigator().getEventNavigation(event.id.toString(), event.getBaseURL(), event.isOnRemoteServer());
        getPlaceNavigator().goToPlace(eventNavigation);
    }
}
