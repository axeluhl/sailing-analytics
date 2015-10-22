package com.sap.sailing.gwt.home.mobile.places.event.races;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.racestab.RegattaRacesPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesView.Presenter;

public class RacesActivity extends AbstractEventActivity<RegattaRacesPlace> implements Presenter {

    public RacesActivity(RegattaRacesPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }
    
    @Override
    protected EventViewBase initView() {
        final RacesView view = new RacesViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }
}
