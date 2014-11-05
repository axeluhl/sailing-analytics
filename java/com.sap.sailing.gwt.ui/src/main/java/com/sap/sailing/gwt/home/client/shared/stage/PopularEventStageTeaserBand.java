package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

/**
 * Teaser band for a popular but finished event on the homepage stage
 * @author Frank
 *
 */
public class PopularEventStageTeaserBand extends StageTeaserBand {

    public PopularEventStageTeaserBand(EventBaseDTO event, HomePlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.startDate, event.endDate));

        actionLink.setVisible(true);
        actionLink.setText(TextMessages.INSTANCE.viewAnalysis());
        actionLink.setHref(getEventNavigation().getTargetUrl());
    }

    @Override
    public void actionLinkClicked(ClickEvent e) {
        getPlaceNavigator().goToPlace(getEventNavigation());
        e.preventDefault();
    }
}
