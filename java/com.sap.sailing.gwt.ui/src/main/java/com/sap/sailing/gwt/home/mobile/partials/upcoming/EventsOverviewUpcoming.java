package com.sap.sailing.gwt.home.mobile.partials.upcoming;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.events.CollapseAnimation;
import com.sap.sailing.gwt.home.mobile.app.MobilePlacesNavigator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;

public class EventsOverviewUpcoming extends Composite {
    
    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewUpcoming> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    private final MobilePlacesNavigator navigator;

    
    @UiField HTMLPanel header;
    @UiField FlowPanel eventsPlaceholder;
    @UiField
    DivElement eventsCount;
    @UiField StringMessages i18n;
    
    private boolean isContentVisible = true;
    
    private final CollapseAnimation eventsAnimation;

    public EventsOverviewUpcoming(MobilePlacesNavigator navigator) {
        this.navigator = navigator;
        EventsOverviewUpcomingResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        eventsAnimation = new CollapseAnimation(eventsPlaceholder.getElement());
        

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
        for (EventListEventDTO event : arrayList) {
            EventsOverviewUpcomingEvent upcomingEvent = new EventsOverviewUpcomingEvent(event, navigator);
            eventsPlaceholder.getElement().appendChild(upcomingEvent.getElement());
        }
        eventsCount.setInnerText(i18n.eventsCount(arrayList.size()));
    }
    
    private void onHeaderCicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }
    
    private void updateContentVisibility() {
        eventsAnimation.animate(isContentVisible);
        if(isContentVisible) {
            getElement().removeClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
        } else {
            getElement().addClassName(EventsOverviewUpcomingResources.INSTANCE.css().accordioncollapsed());
        }
    }

    

}
