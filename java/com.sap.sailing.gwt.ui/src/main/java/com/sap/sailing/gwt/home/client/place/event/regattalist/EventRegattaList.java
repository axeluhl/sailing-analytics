package com.sap.sailing.gwt.home.client.place.event.regattalist;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.place.event.regatta.Regatta;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaList extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaList> {
    }

    @UiField DivElement regattaGroupsNavigationPanel;
    @UiField DivElement regattaListNavgiationDiv;
    @UiField HTMLPanel regattaListItemPanel;
    @UiField AnchorElement allRegattasLink;

    public EventRegattaList(EventDTO event, List<RaceGroupDTO> raceGroups,
            Map<String, Pair<StrippedLeaderboardDTO, LeaderboardGroupDTO>> leaderboardsWithLeaderboardGroup,
            Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        regattaListNavgiationDiv.getStyle().setDisplay(Display.NONE);
        
        for (RaceGroupDTO raceGroup: raceGroups) {
            Regatta regatta = new Regatta(event, true, timerForClientServerOffset, pageNavigator);
            Pair<StrippedLeaderboardDTO, LeaderboardGroupDTO> leaderboardWithLeaderboardGroup = leaderboardsWithLeaderboardGroup.get(raceGroup.getName());
            regatta.setData(raceGroup, leaderboardWithLeaderboardGroup != null ? leaderboardWithLeaderboardGroup.getA() : null,
                    leaderboardWithLeaderboardGroup != null ? leaderboardWithLeaderboardGroup.getB() : null);
            regattaListItemPanel.add(regatta);
        }
    }
}
