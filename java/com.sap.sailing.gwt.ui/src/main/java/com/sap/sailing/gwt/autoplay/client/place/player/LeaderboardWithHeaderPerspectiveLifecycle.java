package com.sap.sailing.gwt.autoplay.client.place.player;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettingsDialogComponent;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardWithHeaderPerspectiveSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveConstructorArgs;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class LeaderboardWithHeaderPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithHeaderPerspective, 
    LeaderboardWithHeaderPerspectiveSettings, LeaderboardPerspectiveSettingsDialogComponent,
    LeaderboardWithHeaderPerspectiveLifecycle.ConstructorArgs> {

    private final SAPHeaderLifecycle sapHeaderLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final StringMessages stringMessages;

    public LeaderboardWithHeaderPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        this.sapHeaderLifecycle = new SAPHeaderLifecycle(stringMessages.leaderboard() +  ": " + leaderboard.getDisplayName(), stringMessages);
        
        this.componentLifecycles.add(leaderboardPanelLifecycle);
        this.componentLifecycles.add(sapHeaderLifecycle);
    }
    
    @Override
    public LeaderboardPerspectiveSettingsDialogComponent getSettingsDialogComponent(LeaderboardWithHeaderPerspectiveSettings settings) {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public LeaderboardWithHeaderPerspectiveSettings createDefaultSettings() {
        return new LeaderboardWithHeaderPerspectiveSettings();
    }

    @Override
    public LeaderboardWithHeaderPerspectiveSettings cloneSettings(LeaderboardWithHeaderPerspectiveSettings settings) {
        return new LeaderboardWithHeaderPerspectiveSettings(settings.isLeaderboardAutoZoom(), settings.getLeaderboardZoomFactor());
    }

    @Override
    public String getLocalizedShortName() {
        return "Leaderboard Viewer";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }


    public SAPHeaderLifecycle getSapHeaderLifecycle() {
        return sapHeaderLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

    @Override
    public LeaderboardWithHeaderPerspective createComponent(ConstructorArgs leaderboardPerspectiveConstructorArgs,
            LeaderboardWithHeaderPerspectiveSettings settings) {
        return leaderboardPerspectiveConstructorArgs.createComponent(settings);
    }

    
    public static class ConstructorArgs implements PerspectiveConstructorArgs<LeaderboardWithHeaderPerspective, LeaderboardWithHeaderPerspectiveSettings> {
        private final PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> componentLifecyclesAndSettings;
        private final SailingServiceAsync sailingService;
        private final AsyncActionsExecutor asyncActionsExecutor;
        private final CompetitorSelectionProvider competitorSelectionProvider;
        private final Timer timer;
        private final String leaderboardName;
        private final ErrorReporter errorReporter;
        private final StringMessages stringMessages;
        private final UserAgentDetails userAgent;
        private boolean startInFullScreenMode;
        
        public ConstructorArgs(PerspectiveLifecycleAndComponentSettings<LeaderboardWithHeaderPerspectiveLifecycle> componentLifecyclesAndSettings,
                SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
                CompetitorSelectionProvider competitorSelectionProvider, Timer timer,
                String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
                UserAgentDetails userAgent, boolean startInFullScreenMode) {
            this.componentLifecyclesAndSettings = componentLifecyclesAndSettings;
            this.sailingService = sailingService;
            this.asyncActionsExecutor = asyncActionsExecutor;
            this.competitorSelectionProvider = competitorSelectionProvider;
            this.timer = timer;
            this.leaderboardName = leaderboardName;
            this.errorReporter = errorReporter;
            this.stringMessages = stringMessages;
            this.userAgent = userAgent;
            this.startInFullScreenMode = startInFullScreenMode;
        }
        
        @Override
        public LeaderboardWithHeaderPerspective createComponent(LeaderboardWithHeaderPerspectiveSettings settings) {
            return new LeaderboardWithHeaderPerspective(settings, componentLifecyclesAndSettings, sailingService, asyncActionsExecutor,
                    competitorSelectionProvider, timer, leaderboardName, errorReporter, stringMessages, userAgent, startInFullScreenMode);
        }
    }
}
