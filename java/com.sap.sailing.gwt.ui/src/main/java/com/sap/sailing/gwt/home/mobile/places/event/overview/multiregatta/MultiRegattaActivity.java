package com.sap.sailing.gwt.home.mobile.places.event.overview.multiregatta;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.AbstractEventOverview;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;

public class MultiRegattaActivity extends AbstractEventActivity<AbstractEventPlace> implements EventViewBase.Presenter {
    
    public MultiRegattaActivity(AbstractEventPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }
    
    @Override
    protected EventViewBase initView() {
        final AbstractEventOverview view = new MultiRegattaViewImpl(this);
        initSailorInfo(view);
        initQuickfinder(view, true);
        initMedia(view);
        return view;
    }

}
