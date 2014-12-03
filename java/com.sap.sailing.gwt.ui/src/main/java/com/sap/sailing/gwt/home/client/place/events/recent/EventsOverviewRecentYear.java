package com.sap.sailing.gwt.home.client.place.events.recent;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.shared.recentevent.RecentEvent;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class EventsOverviewRecentYear extends Composite {

    interface EventsOverviewUiBinder extends UiBinder<Widget, EventsOverviewRecentYear> {
    }
    
    private static EventsOverviewUiBinder uiBinder = GWT.create(EventsOverviewUiBinder.class);

    @UiField SpanElement year;
    @UiField SpanElement eventsCount;
//    @UiField SpanElement countriesCount;
//    @UiField SpanElement sailorsCount;
//    @UiField SpanElement trackedRacesCount;
    @UiField DivElement recentEventsTeaserPanel;
    @UiField HTMLPanel contentDiv;
    @UiField HTMLPanel headerDiv;
    
    private boolean isContentVisible;
    
    public EventsOverviewRecentYear(Integer year, List<EventBaseDTO> events, HomePlacesNavigator navigator) {
        EventsOverviewRecentResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        this.year.setInnerText(String.valueOf(year));
        this.eventsCount.setInnerText(String.valueOf(events.size()));
//        this.countriesCount.setInnerText("tbd.");
//        this.sailorsCount.setInnerText("tbd.");
//        this.trackedRacesCount.setInnerText("tbd.");
        for (EventBaseDTO eventDTO : events) {
            RecentEvent recentEvent = new RecentEvent(navigator, eventDTO);
            recentEventsTeaserPanel.appendChild(recentEvent.getElement());
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
        contentDiv.setVisible(isContentVisible);
        if(isContentVisible) {
            headerDiv.getElement().removeClassName(EventsOverviewRecentResources.INSTANCE.css().eventsoverviewrecent_yearcollapsed());
        } else {
            headerDiv.getElement().addClassName(EventsOverviewRecentResources.INSTANCE.css().eventsoverviewrecent_yearcollapsed());
        }
    }
}
