package com.sap.sailing.gwt.home.mobile.places.event.overview.regatta;

import java.util.List;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;

public class RegattaActivity extends AbstractEventActivity<RegattaOverviewPlace> implements EventViewBase.Presenter {

    private final FlagImageResolver flagImageResolver;

    public RegattaActivity(RegattaOverviewPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory, FlagImageResolver flagImageResolver) {
        super(place, eventDTO, clientFactory);
        this.flagImageResolver = flagImageResolver;
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        List<NavigationItem> navigationItems = getNavigationPathToRegattaLevel();
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }

    @Override
    protected EventViewBase initView() {
        final AbstractEventOverview view = new RegattaOverviewImpl(this, flagImageResolver);
        initSailorInfo(view);
        initSeriesNavigation(view);
        initQuickfinder(view, true);
        initWindfinderNavigations(view);
        if (!isMultiRegattaEvent()) {
            initMedia(view);
        }
        return view;
    }

}
