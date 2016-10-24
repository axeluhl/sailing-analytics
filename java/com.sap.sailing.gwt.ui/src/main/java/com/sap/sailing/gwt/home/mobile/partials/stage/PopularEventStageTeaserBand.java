package com.sap.sailing.gwt.home.mobile.partials.stage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.sap.sailing.gwt.home.communication.event.EventLinkAndMetadataDTO;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Teaser band for a popular but finished event on the homepage stage
 * @author Frank
 *
 */
public class PopularEventStageTeaserBand extends StageTeaserBand {

    public PopularEventStageTeaserBand(EventLinkAndMetadataDTO event, MobilePlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getDisplayName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));

        actionLink.setVisible(true);
        actionLink.setText(StringMessages.INSTANCE.viewAnalysis());
        actionLink.setHref(getEventNavigation().getTargetUrl());
    }

    @Override
    public void actionLinkClicked(ClickEvent e) {
        getPlaceNavigator().goToPlace(getEventNavigation());
        if (e != null) {
            e.preventDefault();
        }
    }
}
