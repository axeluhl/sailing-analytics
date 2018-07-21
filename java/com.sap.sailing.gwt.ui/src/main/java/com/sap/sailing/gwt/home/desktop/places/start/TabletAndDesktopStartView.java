package com.sap.sailing.gwt.home.desktop.places.start;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.start.StartViewDTO;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.partials.anniversary.DesktopAnniversaries;
import com.sap.sailing.gwt.home.desktop.partials.mainevents.MainEvents;
import com.sap.sailing.gwt.home.desktop.partials.mainmedia.MainMedia;
import com.sap.sailing.gwt.home.desktop.partials.stage.Stage;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AnniversariesView;

public class TabletAndDesktopStartView extends Composite implements StartView {
    
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopStartView> {
    }

    @UiField(provided=true) Stage stage;
    @UiField DesktopAnniversaries anniversaries;
    // @UiField(provided=true) MainSponsors mainSponsors;
    @UiField(provided=true) MainEvents mainEvents;
    @UiField(provided=true) MainMedia mainMedia;
    
    public TabletAndDesktopStartView(DesktopPlacesNavigator navigator) {
        stage = new Stage(navigator);
        //  mainSponsors = new MainSponsors(navigator);
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

    @Override
    public AnniversariesView getAnniversariesView() {
        return this.anniversaries;
    }
}
