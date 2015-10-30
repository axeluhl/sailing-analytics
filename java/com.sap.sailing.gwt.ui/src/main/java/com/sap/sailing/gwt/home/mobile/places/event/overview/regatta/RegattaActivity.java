package com.sap.sailing.gwt.home.mobile.places.event.overview.regatta;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO.EventType;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;

public class RegattaActivity extends AbstractEventActivity<RegattaOverviewPlace> implements EventViewBase.Presenter {

    public RegattaActivity(RegattaOverviewPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        List<NavigationItem> navigationItems = new ArrayList<>();
        if(getEventDTO().getType() == EventType.SERIES_EVENT) {
            navigationItems.add(new NavigationItem(getEventDTO().getSeriesName(), getSeriesNavigationForCurrentEvent()));
        }
        navigationItems.add(new NavigationItem(getEventDTO().getLocationOrDisplayName(), getEventNavigation()));
        
        if(getEventDTO().getType() == EventType.MULTI_REGATTA) {
            navigationItems.add(new NavigationItem(getRegatta().getDisplayName(), getRegattaOverviewNavigation(getRegattaId())));
        }
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }

    @Override
    protected EventViewBase initView() {
        final AbstractEventOverview view = new RegattaOverviewImpl(this);
        initSeriesNavigation(view);
        initQuickfinder(view, true);
        if (!isMultiRegattaEvent()) {
            initMedia(view);
        }
        return view;
    }

}
