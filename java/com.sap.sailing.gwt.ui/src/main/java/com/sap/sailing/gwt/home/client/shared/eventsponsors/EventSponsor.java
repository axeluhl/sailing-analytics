package com.sap.sailing.gwt.home.client.shared.eventsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventSponsor extends Composite {
    private static EventSponsorPanelUiBinder uiBinder = GWT.create(EventSponsorPanelUiBinder.class);

    interface EventSponsorPanelUiBinder extends UiBinder<Widget, EventSponsor> {
    }

    @UiField ImageElement sponsorImage;
    @UiField Anchor sponsorLink;
    
    public EventSponsor(String sponsorName, String sponsorImageUrl, String sponsorWebsiteUrl) {
        EventSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        sponsorImage.setSrc(sponsorImageUrl);
        sponsorLink.setHref("http://www.sap.com?campaigncode=CRM-XH21-OSP-Sailing");
    }
}
