package com.sap.sailing.gwt.home.mobile.places.event.media;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaView.Presenter;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;

public class MediaActivity extends AbstractEventActivity<AbstractEventPlace> implements Presenter {

    public MediaActivity(AbstractEventPlace place, EventViewDTO eventDTO, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
    }
    
    @Override
    protected EventViewBase initView() {
        final MediaView view = new MediaViewImpl(this);
        initQuickfinder(view, false);
        initMedia(new MediaCallback() {
            @Override
            public void onSuccess(MediaDTO result) {
                view.setMedia(result.getVideos(), result.getPhotos());
            }
        });
        return view;
    }
}
