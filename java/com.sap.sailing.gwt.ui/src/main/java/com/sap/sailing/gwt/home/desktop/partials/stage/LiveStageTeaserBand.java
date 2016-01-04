package com.sap.sailing.gwt.home.desktop.partials.stage;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.communication.start.EventStageDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.utils.EventDatesFormatterUtil;

/**
 * Teaser band for a live event on the homepage stage
 * @author Frank
 *
 */
public class LiveStageTeaserBand extends StageTeaserBand {

    public LiveStageTeaserBand(EventStageDTO event, DesktopPlacesNavigator placeNavigator) {
        super(event, placeNavigator);
 
        bandTitle.setInnerText(event.getDisplayName());
        bandSubtitle.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(event.getStartDate(), event.getEndDate()));

        isLiveDiv.getStyle().setDisplay(Display.INLINE_BLOCK);
        actionLink.setVisible(true);
        actionLink.setText(TextMessages.INSTANCE.watchNow());
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
