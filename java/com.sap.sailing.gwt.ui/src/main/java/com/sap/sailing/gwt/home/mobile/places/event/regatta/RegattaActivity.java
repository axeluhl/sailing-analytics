package com.sap.sailing.gwt.home.mobile.places.event.regatta;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventView;
import com.sap.sailing.gwt.home.mobile.places.event.overview.EventViewImpl;

public class RegattaActivity extends AbstractEventActivity<RegattaOverviewPlace> implements EventView.Presenter {

    public RegattaActivity(RegattaOverviewPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }

    @Override
    protected EventViewBase initView() {
        final EventView view = new EventViewImpl(this);
        initSailorInfoOrSeriesNavigation(view);
        initQuickfinder(view, true);
        if (!isMultiRegattaEvent()) {
            initMedia(new MediaCallback() {
                @Override
                public void onSuccess(MediaDTO result) {
                    view.setMediaForImpressions(result.getPhotos().size(), result.getVideos().size(), result.getPhotos());
                }
            });
        }
        return view;
    }

}
