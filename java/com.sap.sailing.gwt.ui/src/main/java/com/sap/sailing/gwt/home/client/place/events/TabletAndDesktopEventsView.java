package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.events.recent.EventsOverviewRecent;
import com.sap.sailing.gwt.home.client.place.events.upcoming.EventsOverviewUpcoming;

public class TabletAndDesktopEventsView extends AbstractEventsView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    private final SharedResources homeRes = GWT.create(SharedResources.class);
    
    interface EventsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventsView> {
    }
    
    @UiField(provided=true) EventsOverviewRecent recentEventsWidget;
    @UiField(provided=true) EventsOverviewUpcoming upcomingEventsWidget;
    @UiField Anchor recentEventsLink;
    @UiField Anchor upcomingEventsLink;
    
    public TabletAndDesktopEventsView(HomePlacesNavigator navigator) {
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
        
        recentEventsLink.getElement().addClassName(homeRes.mainCss().navbar_buttonactive());
        upcomingEventsLink.getElement().removeClassName(homeRes.mainCss().navbar_buttonactive());
    }
    
    @UiHandler("upcomingEventsLink")
    void upcomingEventsClicked(ClickEvent event) {
        recentEventsWidget.setVisible(false);
        upcomingEventsWidget.setVisible(true);
        
        upcomingEventsLink.getElement().addClassName(homeRes.mainCss().navbar_buttonactive());
        recentEventsLink.getElement().removeClassName(homeRes.mainCss().navbar_buttonactive());
    }
    
    @Override 
    protected void updateEventsUI() {
        recentEventsWidget.updateEvents(getRecentEventsByYearOrderedByEndDate());
        upcomingEventsWidget.updateEvents(getUpcomingEvents());
    }
}
