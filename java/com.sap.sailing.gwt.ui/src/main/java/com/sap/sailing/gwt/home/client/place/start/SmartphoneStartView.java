package com.sap.sailing.gwt.home.client.place.start;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.mainevents.MainEvents;
import com.sap.sailing.gwt.home.client.shared.mainmedia.MainMedia;
import com.sap.sailing.gwt.home.client.shared.mainsponsors.MainSponsors;
import com.sap.sailing.gwt.home.client.shared.socialfooter.SocialFooter;
import com.sap.sailing.gwt.home.client.shared.stage.Stage;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class SmartphoneStartView extends Composite implements StartView {
    private static StartPageMobileViewUiBinder uiBinder = GWT.create(StartPageMobileViewUiBinder.class);

    interface StartPageMobileViewUiBinder extends UiBinder<Widget, SmartphoneStartView> {
    }

    @UiField Stage stage;
    @UiField MainSponsors mainSponsors;
    @UiField MainEvents mainEvents;
    @UiField MainMedia mainMedia;
    @UiField SocialFooter socialFooter;

    public SmartphoneStartView() {
        super();

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setFeaturedEvent(EventDTO featuredEvent) {
        stage.setFeaturedEvent(featuredEvent);
    }

    @Override
    public void setRecentEvents(List<EventDTO> recentEvents) {
        mainEvents.setRecentEvents(recentEvents);
    }
}
