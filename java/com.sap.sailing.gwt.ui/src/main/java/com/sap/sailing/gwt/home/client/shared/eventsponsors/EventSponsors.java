package com.sap.sailing.gwt.home.client.shared.eventsponsors;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class EventSponsors extends Composite {
    private static EventSponsorsPanelUiBinder uiBinder = GWT.create(EventSponsorsPanelUiBinder.class);

    @UiField HTMLPanel sponsorsPlaceholder;
    
    private final List<EventSponsor> eventSponsorComposites;

    interface EventSponsorsPanelUiBinder extends UiBinder<Widget, EventSponsors> {
    }

    public EventSponsors() {
        EventSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        eventSponsorComposites = new ArrayList<EventSponsor>();
    }
    
    public void setEventSponsors(List<String> sponsorImageUrls) {
        sponsorsPlaceholder.clear();
        eventSponsorComposites.clear();

        if(sponsorImageUrls != null) {
            for (String sponsorImageUrl : sponsorImageUrls) {
                EventSponsor eventSponor = new EventSponsor(null /* name */, sponsorImageUrl, null /* link */);

                eventSponsorComposites.add(eventSponor);
                sponsorsPlaceholder.add(eventSponor);
            }
        }
    }
}
