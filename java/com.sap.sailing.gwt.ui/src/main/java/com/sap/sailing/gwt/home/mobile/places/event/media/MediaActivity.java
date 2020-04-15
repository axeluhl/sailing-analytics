package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.List;

import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventActivity;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.mobile.places.event.media.MediaView.Presenter;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MediaActivity extends AbstractEventActivity<AbstractEventPlace> implements Presenter {

    public MediaActivity(AbstractEventPlace place, EventViewDTO eventDTO, NavigationPathDisplay navigationPathDisplay, MobileApplicationClientFactory clientFactory) {
        super(place, eventDTO, clientFactory);
        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        StringMessages i18n = StringMessages.INSTANCE;
        List<NavigationItem> navigationItems = getNavigationPathToEventLevel();
        navigationItems.add(new NavigationItem(i18n.media(), getMediaPageNavigation()));
        navigationPathDisplay.showNavigationPath(navigationItems.toArray(new NavigationItem[navigationItems.size()]));
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
    
    @Override
    protected boolean isRegattaLevel() {
        return !isMultiRegattaEvent();
    }
}
