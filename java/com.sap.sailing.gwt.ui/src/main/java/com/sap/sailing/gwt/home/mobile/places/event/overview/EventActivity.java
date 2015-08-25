package com.sap.sailing.gwt.home.mobile.places.event.overview;

import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventView.Presenter;

public class EventActivity extends AbstractEventActivity<AbstractEventPlace> implements Presenter {
    
    public EventActivity(AbstractEventPlace place, MobileApplicationClientFactory clientFactory) {
        super(place, clientFactory);
    }
    
    @Override
    protected EventViewBase initView() {
        final EventView view = new EventViewImpl(this);
        initSailorInfoOrSeriesNavigation(view);
        initQuickfinder(view, true);
        initMedia(view);
        return view;
    }

}
