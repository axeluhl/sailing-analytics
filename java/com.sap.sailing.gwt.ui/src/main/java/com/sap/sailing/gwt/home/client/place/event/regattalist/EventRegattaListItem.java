package com.sap.sailing.gwt.home.client.place.event.regattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaListItem extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaListItem> {
    }

    @UiField SpanElement regattaName;
    @UiField SpanElement leaderboardGroupName;
    
    private RegattaDTO regatta;
    
    private final LeaderboardGroupDTO leaderboardGroup;
    private final StrippedLeaderboardDTO leaderboard;

    public EventRegattaListItem(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;

        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        updateUI();
    }

    private void updateUI() {
        regattaName.setInnerText(leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name);
        leaderboardGroupName.setInnerText(leaderboardGroup.getName());
    }

}
