package com.sap.sailing.gwt.home.client.place.event.regattaheader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaHeader extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaHeader> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    private final Timer timerForClientServerOffset;

    @UiField SpanElement regattaName;
    @UiField SpanElement leaderboardGroupName;
    @UiField SpanElement regattaPhase;
    @UiField SpanElement competitorsCount;
    @UiField DivElement isLiveDiv;

    public EventRegattaHeader(EventDTO event,  Timer timerForClientServerOffset) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        
        EventRegattaHeaderResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(RegattaDTO regatta, LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        boolean hasLiveRace = leaderboard.hasLiveRace(timerForClientServerOffset.getLiveTimePointInMillis());
        isLiveDiv.setAttribute("data-labeltype", hasLiveRace ? "live" : "");
        
        regattaName.setInnerText(leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name);
        leaderboardGroupName.setInnerText(leaderboardGroup.getName());
        
        if(leaderboard.rows != null) {
            competitorsCount.setInnerText(String.valueOf(leaderboard.rows.size()));
        } else {
            competitorsCount.getStyle().setVisibility(Visibility.HIDDEN);
        }
        regattaPhase.setInnerText(calculateCurrentRegattaPhase(leaderboard));
    }
    
    private String calculateCurrentRegattaPhase(StrippedLeaderboardDTO leaderboard) {
        return "";
    }
}
