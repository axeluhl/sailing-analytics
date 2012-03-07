package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentChecker.UserAgentTypes;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardViewMode;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.UserDTO;

public class TVViewPanel extends SimplePanel implements RaceTimesInfoProviderListener {
    
    private RaceTimesInfoProvider raceTimesInfoProvider;
    
    private LeaderboardPanel leaderboardPanel;
    private LeaderboardDTO leaderboard;
    
    private RaceBoardPanel raceBoardPanel;
    
    private SailingServiceAsync sailingService;
    private StringMessages stringMessages;
    private ErrorReporter errorReporter;
    private UserAgentTypes userAgentType;
    private UserDTO userDTO;
    
    public TVViewPanel(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            String leaderboardName, UserAgentTypes userAgentType, UserDTO userDTO) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.userAgentType = userAgentType;
        this.userDTO = userDTO;
        
        raceTimesInfoProvider = null;
        raceBoardPanel = null;
        
        leaderboardPanel = createLeaderboardPanel(sailingService, stringMessages, errorReporter, leaderboardName, userAgentType);
        setWidget(leaderboardPanel);
    }
    
    private LeaderboardPanel createLeaderboardPanel(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, String leaderboardName,
            UserAgentTypes userAgentType) {
        LeaderboardSettings settings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(/* autoExpandFirstRace */ false); 
        CompetitorSelectionModel selectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        Timer timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */3000l);
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, settings,
        /* preSelectedRace */null, selectionModel, timer, leaderboardName, null, errorReporter, stringMessages,
                userAgentType) {
            @Override
            protected void setLeaderboard(LeaderboardDTO leaderboard) {
                super.setLeaderboard(leaderboard);
                TVViewPanel.this.leaderboard = leaderboard;
                if (raceTimesInfoProvider == null) {
                    raceTimesInfoProvider = createRaceTimesInfoProvider(sailingService, stringMessages, errorReporter);
                }
            }
        };
        return leaderboardPanel;
    }
    
    private RaceTimesInfoProvider createRaceTimesInfoProvider(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter) {
        RaceIdentifier raceId = null;
        for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
            if (race.isTrackedRace()) {
                raceId = race.getRaceIdentifier();
                break;
            }
        }
        return raceId == null ? null : new RaceTimesInfoProvider(sailingService, errorReporter, raceId, 3000l);
    }
    
    private RaceBoardPanel createRaceBoardPanel(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            String leaderboardName, UserAgentTypes userAgentType, UserDTO userDTO) {
        RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
        List<RaceIdentifier> singletonList = Collections.singletonList(raceTimesInfoProvider.getRaceIdentifier());
        raceSelectionModel.setSelection(singletonList);
        RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, userDTO, raceSelectionModel, leaderboardName, null,
                errorReporter, stringMessages, userAgentType, RaceBoardViewMode.ONE_SCREEN, raceTimesInfoProvider);
        return raceBoardPanel;
    }
    
    private void showLeaderboard() {
        setWidget(leaderboardPanel);
        raceBoardPanel = null;
    }

    @Override
    public void raceTimesInfoReceived(RaceTimesInfoDTO raceTimesInfo) {
        if (raceTimesInfo != null) {
            if (raceTimesInfo.endOfRace != null) {
                showLeaderboard();
            } else if (raceTimesInfo.startOfTracking != null && raceBoardPanel == null) {
                raceBoardPanel = createRaceBoardPanel(sailingService, stringMessages, errorReporter, leaderboard.name, userAgentType, userDTO);
                setWidget(raceBoardPanel);
            }
        } else {
            showLeaderboard();
        }
    }

}
