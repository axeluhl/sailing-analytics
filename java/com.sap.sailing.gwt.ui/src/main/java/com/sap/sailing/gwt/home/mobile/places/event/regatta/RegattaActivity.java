package com.sap.sailing.gwt.home.mobile.places.event.regatta;

import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

public class RegattaActivity extends AbstractEventActivity<RegattaOverviewPlace> implements RegattaView.Presenter {

    public RegattaActivity(RegattaOverviewPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }

    @Override
    protected EventViewBase initView() {
        final RegattaView view = new RegattaViewImpl(this);
        initQuickfinder(view, true);
        return view;
    }

}
