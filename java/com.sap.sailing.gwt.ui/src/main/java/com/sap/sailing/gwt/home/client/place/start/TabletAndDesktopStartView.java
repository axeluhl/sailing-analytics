package com.sap.sailing.gwt.home.client.place.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.shared.mainevents.MainEvents;
import com.sap.sailing.gwt.home.client.shared.mainmedia.MainMedia;
import com.sap.sailing.gwt.home.client.shared.stage.Stage;
import com.sap.sailing.gwt.ui.shared.start.StartViewDTO;

public class TabletAndDesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopStartView> {
    }

    @UiField(provided=true) Stage stage;
//    @UiField(provided=true) MainSponsors mainSponsors;
    @UiField(provided=true) MainEvents mainEvents;
    @UiField(provided=true) MainMedia mainMedia;
    
    public TabletAndDesktopStartView(HomePlacesNavigator navigator) {
        stage = new Stage(navigator);
//        mainSponsors = new MainSponsors(navigator);
        mainEvents = new MainEvents(navigator);
        mainMedia = new MainMedia(navigator);
        
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    @Override
    public void setData(StartViewDTO data) {
        stage.setFeaturedEvents(data.getStageEvents());
        mainEvents.setRecentEvents(data.getRecentEvents());
        mainMedia.setData(data.getVideos(), data.getPhotos());
    }
}
