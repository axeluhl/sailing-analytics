package com.sap.sailing.gwt.home.client.place.events.recent;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventDefaultPlace;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListEventDTO;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListYearDTO;

public class EventsOverviewRecentYear extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecentYear> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    @UiField SpanElement year;
    @UiField SpanElement eventsCount;
    @UiField SpanElement countriesCount;
    @UiField SpanElement sailorsCount;
    @UiField SpanElement trackedRacesCount;
    @UiField Element countriesContainer;
    @UiField Element sailorsContainer;
    @UiField Element trackedRacesContainer;
    @UiField FlowPanel recentEventsTeaserPanel;
    @UiField DivElement contentDiv;
    @UiField HTMLPanel headerDiv;
    
    private boolean isContentVisible;
    
    public EventsOverviewRecentYear(EventListYearDTO yearDTO, HomePlacesNavigator navigator) {
        List<EventListEventDTO> events = yearDTO.getEvents();
        
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.year.setInnerText(String.valueOf(yearDTO.getYear()));
        this.eventsCount.setInnerText(String.valueOf(events.size()));
        if(yearDTO.getSailorCount() > 0) {
            sailorsCount.setInnerText("" + yearDTO.getSailorCount());
        } else {
            sailorsContainer.removeFromParent();
        }
        if(yearDTO.getCountryCount() > 0) {
            countriesCount.setInnerText("" + yearDTO.getCountryCount());
        } else {
            countriesContainer.removeFromParent();
        }
        if(yearDTO.getTrackedRacesCount() > 0) {
            trackedRacesCount.setInnerText("" + yearDTO.getTrackedRacesCount());
        } else {
            trackedRacesContainer.removeFromParent();
        }
//        this.countriesCount.setInnerText("tbd.");
//        this.sailorsCount.setInnerText("tbd.");
//        this.trackedRacesCount.setInnerText("tbd.");
        for (EventListEventDTO eventDTO : events) {
            PlaceNavigation<EventDefaultPlace> eventNavigation = navigator.getEventNavigation(eventDTO.getId().toString(), eventDTO.getBaseURL(), eventDTO.isOnRemoteServer());
            RecentEventTeaser recentEvent = new RecentEventTeaser(eventNavigation, eventDTO);
            recentEventsTeaserPanel.add(recentEvent);
        }
        isContentVisible = true;
        headerDiv.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onHeaderCicked();
            }
        }, ClickEvent.getType());
    }

    private void onHeaderCicked() {
        isContentVisible = !isContentVisible;
        updateContentVisibility();
    }
    
    public void hideContent() {
        isContentVisible = false;
        updateContentVisibility();
    }

    public void showContent() {
        isContentVisible = true;
        updateContentVisibility();
    }
    
    private void updateContentVisibility() {
        if(isContentVisible) {
            contentDiv.getStyle().clearDisplay();
            getElement().removeClassName(EventsOverviewRecentResources.INSTANCE.css().accordioncollapsed());
        } else {
            contentDiv.getStyle().setDisplay(Display.NONE);
            getElement().addClassName(EventsOverviewRecentResources.INSTANCE.css().accordioncollapsed());
        }
    }
}
