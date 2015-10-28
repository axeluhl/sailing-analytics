package com.sap.sailing.gwt.home.mobile.places.event.overview.regatta;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;

public class RegattaActivity extends AbstractEventActivity<RegattaOverviewPlace> implements EventViewBase.Presenter {

    public RegattaActivity(RegattaOverviewPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
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
