package com.sap.sailing.gwt.home.mobile.places.event.overview.multiregatta;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;

public class MultiRegattaActivity extends AbstractEventActivity<AbstractEventPlace> implements EventViewBase.Presenter {
    
    public MultiRegattaActivity(AbstractEventPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        navigationPathDisplay.showNavigationPath(new NavigationItem(getEventDTO().getLocationOrDisplayName(), getEventNavigation()));
    }
    
    @Override
    protected EventViewBase initView() {
        final AbstractEventOverview view = new MultiRegattaViewImpl(this);
        initSailorInfo(view);
        initWindfinderNavigations(view);
        initQuickfinder(view, true);
        initMedia(view);
        return view;
    }

}
