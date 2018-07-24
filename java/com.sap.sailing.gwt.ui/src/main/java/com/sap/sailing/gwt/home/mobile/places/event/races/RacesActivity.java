package com.sap.sailing.gwt.home.mobile.places.event.races;

import java.util.List;
import java.util.Optional;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesView.Presenter;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RacesActivity extends AbstractEventActivity<RegattaRacesPlace> implements Presenter {

    public RacesActivity(RegattaRacesPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        List<NavigationItem> navigationItems = getNavigationPathToRegattaLevel();
        navigationItems.add(new NavigationItem(i18n.races(), getRegattaRacesNavigation(getRegattaId())));
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
    }
    
    @Override
    protected EventViewBase initView() {
        final RacesView view = new RacesViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }

    @Override
    public Optional<String> getPreferredSeriesName() {
        return getPlace().getPreferredSeriesName();
    }
}
