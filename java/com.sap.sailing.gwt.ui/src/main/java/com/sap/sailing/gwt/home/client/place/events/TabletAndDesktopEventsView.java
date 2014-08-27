package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.HomeResources;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.events.recent.EventsOverviewRecent;
import com.sap.sailing.gwt.home.client.place.events.upcoming.EventsOverviewUpcoming;

public class TabletAndDesktopEventsView extends AbstractEventsView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    private final HomeResources homeRes = GWT.create(HomeResources.class);
    
    interface EventsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventsView> {
    }
    
    @UiField(provided=true) EventsOverviewRecent recentEventsWidget;
    @UiField(provided=true) EventsOverviewUpcoming upcomingEventsWidget;
    @UiField Anchor recentEventsLink;
    @UiField Anchor upcomingEventsLink;
    
    public TabletAndDesktopEventsView(PlaceNavigator navigator) {
        super();
        recentEventsWidget = new EventsOverviewRecent(navigator);
        upcomingEventsWidget = new EventsOverviewUpcoming(navigator);
        upcomingEventsWidget.setVisible(false);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @UiHandler("recentEventsLink")
    void recentsEventsClicked(ClickEvent event) {
        recentEventsWidget.setVisible(true);
        upcomingEventsWidget.setVisible(false);
        
        recentEventsLink.getElement().removeClassName(homeRes.mainCss().buttoninactive());
        upcomingEventsLink.getElement().addClassName(homeRes.mainCss().buttoninactive());
    }
    
    @UiHandler("upcomingEventsLink")
    void upcomingEventsClicked(ClickEvent event) {
        recentEventsWidget.setVisible(false);
        upcomingEventsWidget.setVisible(true);
        
        recentEventsLink.getElement().addClassName(homeRes.mainCss().buttoninactive());
        upcomingEventsLink.getElement().removeClassName(homeRes.mainCss().buttoninactive());
    }
    
    @Override 
    protected void updateEventsUI() {
        recentEventsWidget.updateEvents(getRecentEventsByYear());
        upcomingEventsWidget.updateEvents(getUpcomingEvents());
    }
}
