package com.sap.sailing.gwt.home.mobile.places.event.races;

import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesView.Presenter;

public class RacesActivity extends AbstractEventActivity<RegattaRacesPlace> implements Presenter {

    public RacesActivity(RegattaRacesPlace place, MobileApplicationClientFactory clientFactory) {
        super(place, clientFactory);
    }
    
    @Override
    protected EventViewBase initView() {
        final RacesView view = new RacesViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }
}
