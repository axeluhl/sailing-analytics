package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.start.EventStageDTO;

/**
 * Teaser band for an upcoming event on the homepage stage
 * @author Frank
 *
 */
public class UpcomingEventStageTeaserBand extends StageTeaserBand {

    public UpcomingEventStageTeaserBand(EventStageDTO event, HomePlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getDisplayName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));

        actionLink.setVisible(true);
        actionLink.setText(TextMessages.INSTANCE.moreInfo());
        actionLink.setHref(getEventNavigation().getTargetUrl());
    }

    @Override
    public void actionLinkClicked(ClickEvent e) {
        getPlaceNavigator().goToPlace(getEventNavigation());
        e.preventDefault();
    }
}
