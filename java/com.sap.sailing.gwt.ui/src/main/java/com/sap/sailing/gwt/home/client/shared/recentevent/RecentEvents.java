package com.sap.sailing.gwt.home.client.shared.recentevent;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class RecentEvents extends Composite {
    
    @UiField DivElement recentEventsGrid;
    @UiField DivElement headlineDiv;
    @UiField HeadingElement titleHeading;

    private final List<RecentEvent> recentEventComposites;

    interface RecentEventsUiBinder extends UiBinder<Widget, RecentEvents> {
    }
    
    private static RecentEventsUiBinder uiBinder = GWT.create(RecentEventsUiBinder.class);

    private final PlaceNavigator navigator;
    
    public RecentEvents(PlaceNavigator navigator) {
        this.navigator = navigator;
        recentEventComposites = new ArrayList<RecentEvent>();
        
        RecentEventResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setEvents(List<EventBaseDTO> events, String headlineText) {
        recentEventsGrid.removeAllChildren();
        recentEventComposites.clear();

        if(headlineText != null) {
            titleHeading.setInnerText(headlineText);
            recentEventsGrid.appendChild(headlineDiv);
        } else {
            titleHeading.setInnerText("");
        }

        for(EventBaseDTO event: events) {
            RecentEvent recentEvent = new RecentEvent(navigator);
            recentEvent.setEvent(event);
            recentEventComposites.add(recentEvent);
            recentEventsGrid.appendChild(recentEvent.getElement());
        }

    }
    
}
