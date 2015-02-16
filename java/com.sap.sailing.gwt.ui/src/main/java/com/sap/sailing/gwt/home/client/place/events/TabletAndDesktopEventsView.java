package com.sap.sailing.gwt.home.client.place.events;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.controls.tabbar.BreadcrumbPane;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.events.recent.EventsOverviewRecent;
import com.sap.sailing.gwt.home.client.place.events.upcoming.EventsOverviewUpcoming;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class TabletAndDesktopEventsView extends AbstractEventsView {
    private static EventsPageViewUiBinder uiBinder = GWT.create(EventsPageViewUiBinder.class);

    interface EventsPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopEventsView> {
    }
    
    @UiField StringMessages i18n;
    @UiField(provided=true) EventsOverviewRecent recentEventsWidget;
    @UiField(provided=true) EventsOverviewUpcoming upcomingEventsWidget;
    @UiField BreadcrumbPane breadcrumbs;
    
    public TabletAndDesktopEventsView(HomePlacesNavigator navigator) {
        super();
        recentEventsWidget = new EventsOverviewRecent(navigator);
        upcomingEventsWidget = new EventsOverviewUpcoming(navigator);
        
        initWidget(uiBinder.createAndBindUi(this));
        
        initBreadCrumbs();
    }
    
    private void initBreadCrumbs() {
        breadcrumbs.addBreadcrumbItem(i18n.home(), "TODO", new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                
            }
        });
        breadcrumbs.addBreadcrumbItem(i18n.events(), "TODO", new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                
            }
        });
    }

    @Override 
    protected void updateEventsUI() {
        recentEventsWidget.updateEvents(getRecentEventsByYearOrderedByEndDate());
        upcomingEventsWidget.updateEvents(getUpcomingEvents());
    }
}
