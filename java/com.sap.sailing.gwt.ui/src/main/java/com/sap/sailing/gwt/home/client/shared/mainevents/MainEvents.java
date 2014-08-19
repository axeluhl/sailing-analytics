package com.sap.sailing.gwt.home.client.shared.mainevents;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.recentevent.RecentEvent;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;

public class MainEvents extends Composite {
    interface MainEventsUiBinder extends UiBinder<Widget, MainEvents> {
    }
    
    private static MainEventsUiBinder uiBinder = GWT.create(MainEventsUiBinder.class);

    private List<EventBaseDTO> recentEvents;
    
    @UiField DivElement recentEventsDiv;
    @UiField Anchor showAllEventsAnchor;
    
    private final PlaceNavigator navigator;
    
    public MainEvents(PlaceNavigator navigator) {
        this.navigator = navigator;
        
        MainEventsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        recentEvents = new ArrayList<EventBaseDTO>();
    }

    public void setRecentEvents(List<EventBaseDTO> theRecentEvents) {
        recentEventsDiv.removeAllChildren();
        recentEvents.clear();
        recentEvents.addAll(theRecentEvents);
        
        int size = recentEvents.size();
        if(size > 0) {
            createRecentEvent(recentEvents.get(0));
        }
        if(size > 1) {
            createRecentEvent(recentEvents.get(1));
        }
        if(size > 2) {
            createRecentEvent(recentEvents.get(2));
        }
    }

    private void createRecentEvent(EventBaseDTO eventBase) {
        RecentEvent event = new RecentEvent(navigator);
        event.setEvent(eventBase);
        recentEventsDiv.appendChild(event.getElement());
    }
    
    @UiHandler("showAllEventsAnchor")
    public void showAllEvents(ClickEvent e) {
        navigator.goToEvents();;
    }

}
