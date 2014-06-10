package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.events.recent.EventsOverviewRecent;
import com.sap.sailing.gwt.home.client.place.events.upcoming.EventsOverviewUpcoming;

public class TabletAndDesktopEventsView extends AbstractEventsView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    interface EventsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventsView> {
    }
    
    @UiField(provided=true) EventsOverviewRecent recentEventsWidget;
    @UiField(provided=true) EventsOverviewUpcoming upcomingEventsWidget;
    @UiField Hyperlink recentEventsLink;
    @UiField Hyperlink upcomingEventsLink;
    
    public TabletAndDesktopEventsView(PlaceNavigator navigator) {
        super();
        recentEventsWidget = new EventsOverviewRecent(navigator);
        upcomingEventsWidget = new EventsOverviewUpcoming(navigator);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("recentEventsLink")
    void recentsEventsClicked(ClickEvent event) {
        recentEventsWidget.setVisible(true);
        
        // set style buttoninactive
        
        upcomingEventsWidget.setVisible(false);
    }
    
    @UiHandler("upcomingEventsLink")
    void upcomingEventsClicked(ClickEvent event) {
        recentEventsWidget.setVisible(false);
        upcomingEventsWidget.setVisible(true);
    }
    
    @Override 
    protected void updateEventsUI() {
        recentEventsWidget.updateEvents(getRecentEventsOrderedByYear());
        upcomingEventsWidget.updateEvents(getUpcomingEvents());
    }
}
