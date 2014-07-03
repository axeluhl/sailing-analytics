package com.sap.sailing.gwt.home.client.place.event.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventRegattaHeader extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaHeader> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;

    @UiField SpanElement regattaName;
    @UiField SpanElement leaderboardGroupName;
    @UiField SpanElement competitorsCount;
    @UiField DivElement isLiveElement;

    public EventRegattaHeader(EventDTO event) {
        this.event = event;
        
        EventRegattaHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(String theRegattaName, String theLeaderboardGroupName, int theCompetitorCount, boolean isLive) {
        regattaName.setInnerText(theRegattaName);
        leaderboardGroupName.setInnerText(theLeaderboardGroupName);
        
        competitorsCount.setInnerText(String.valueOf(theCompetitorCount));
    }
}
