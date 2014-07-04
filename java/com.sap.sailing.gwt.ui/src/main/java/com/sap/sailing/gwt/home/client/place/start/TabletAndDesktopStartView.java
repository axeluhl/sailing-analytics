package com.sap.sailing.gwt.home.client.place.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.shared.mainevents.MainEvents;
import com.sap.sailing.gwt.home.client.shared.mainmedia.MainMedia;
import com.sap.sailing.gwt.home.client.shared.mainsponsors.MainSponsors;
import com.sap.sailing.gwt.home.client.shared.socialfooter.SocialFooter;
import com.sap.sailing.gwt.home.client.shared.stage.Stage;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class TabletAndDesktopStartView extends Composite implements StartView {
    private static StartPageViewUiBinder uiBinder = GWT.create(StartPageViewUiBinder.class);

    interface StartPageViewUiBinder extends UiBinder<Widget, TabletAndDesktopStartView> {
    }

    @UiField Stage stage;
    @UiField(provided=true) MainSponsors mainSponsors;
    @UiField(provided=true) MainEvents mainEvents;
    @UiField(provided=true) MainMedia mainMedia;
    @UiField SocialFooter socialFooter;

    public TabletAndDesktopStartView(PlaceNavigator navigator) {
        mainSponsors = new MainSponsors(navigator);
        mainEvents = new MainEvents(navigator);
        mainMedia = new MainMedia(navigator);
        
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setFeaturedEvent(EventDTO featuredEvent) {
        stage.setFeaturedEvent(featuredEvent);
    }

    @Override
    public void setRecentEvents(List<EventDTO> recentEvents) {
        mainEvents.setRecentEvents(recentEvents);
        mainMedia.setRecentEvents(recentEvents);
    }
}
