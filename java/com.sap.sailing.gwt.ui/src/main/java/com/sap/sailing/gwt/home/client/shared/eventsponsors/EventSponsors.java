package com.sap.sailing.gwt.home.client.shared.eventsponsors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventSponsors extends Composite {
    private static EventSponsorsPanelUiBinder uiBinder = GWT.create(EventSponsorsPanelUiBinder.class);

    interface EventSponsorsPanelUiBinder extends UiBinder<Widget, EventSponsors> {
    }

    public EventSponsors() {
        EventSponsorsResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
