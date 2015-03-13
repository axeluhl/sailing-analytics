package com.sap.sailing.gwt.home.client.place.events.upcoming;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;

public class EventsOverviewUpcoming extends Composite {
    
    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewUpcoming> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final HomePlacesNavigator navigator;

    private final List<UpcomingEvent> upcomingEventComposites;
    
    @UiField HTMLPanel header;
    @UiField HTMLPanel eventsPlaceholder;
    @UiField SpanElement eventsCount;
    
    private boolean isContentVisible = true;

    public EventsOverviewUpcoming(HomePlacesNavigator navigator) {
        this.navigator = navigator;
        upcomingEventComposites = new ArrayList<UpcomingEvent>();
        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        header.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onHeaderCicked();
            }
        }, ClickEvent.getType());
    }
    
    public void updateEvents(ArrayList<EventListEventDTO> arrayList) {
        setVisible(arrayList.size() > 0);
        eventsPlaceholder.clear();
        upcomingEventComposites.clear();
        for (EventListEventDTO event : arrayList) {
            UpcomingEvent upcomingEvent = new UpcomingEvent(event, navigator);
            upcomingEventComposites.add(upcomingEvent);
            eventsPlaceholder.getElement().appendChild(upcomingEvent.getElement());
        }
        eventsCount.setInnerText(""+arrayList.size());
    }
    
    private void onHeaderCicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }
    
    private void updateContentVisibility() {
        if(isContentVisible) {
            eventsPlaceholder.getElement().getStyle().clearDisplay();
            getElement().removeClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
        } else {
            eventsPlaceholder.getElement().getStyle().setDisplay(Display.NONE);
            getElement().addClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
        }
    }

}
