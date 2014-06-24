package com.sap.sailing.gwt.home.client.place.event;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.header.EventHeader;
import com.sap.sailing.gwt.home.client.place.event.media.EventMedia;
import com.sap.sailing.gwt.home.client.place.event.overview.EventOverview;
import com.sap.sailing.gwt.home.client.place.event.regattalist.EventRegattaList;
import com.sap.sailing.gwt.home.client.place.event.schedule.EventSchedule;
import com.sap.sailing.gwt.home.client.shared.eventsponsors.EventSponsors;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class TabletAndDesktopEventView extends Composite implements EventView, EventPageNavigator {
    private static EventViewUiBinder uiBinder = GWT.create(EventViewUiBinder.class);

    interface EventViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventView> {
    }

    @UiField(provided=true) EventHeader eventHeader;
    @UiField(provided=true) EventOverview eventOverview;
    @UiField(provided=true) EventSchedule eventSchedule;
    @UiField(provided=true) EventMedia eventMedia;
    @UiField(provided=true) EventRegattaList eventRegattaList;
    @UiField EventSponsors eventSponsors;

    private final List<Widget> pageElements;
    
    public TabletAndDesktopEventView(EventDTO event) {
        eventHeader = new EventHeader(event, this);
        eventRegattaList = new EventRegattaList(event);
        eventOverview = new EventOverview(event);
        eventSchedule = new EventSchedule(event);
        eventMedia = new EventMedia(event);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        pageElements = Arrays.asList(new Widget[] { eventOverview, eventRegattaList, eventMedia, eventSchedule });
        setVisibleEventElement(eventOverview);

        eventSponsors.setEventSponsors(event);
    }

    @Override
    public void goToOverview() {
        setVisibleEventElement(eventOverview);
    }

    @Override
    public void goToRegattas() {
        setVisibleEventElement(eventRegattaList);
    }

    @Override
    public void goToSchedule() {
        setVisibleEventElement(eventSchedule);
    }

    @Override
    public void goToMedia() {
        setVisibleEventElement(eventMedia);
    }
    
    private void setVisibleEventElement(Widget visibleWidget) {
        for (Widget element : pageElements) {
            element.setVisible(element == visibleWidget);
        }
    }

}
