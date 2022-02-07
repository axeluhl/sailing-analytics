package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.security.ui.client.premium.PaywallResolver;

public abstract class AbstractMultiRaceLeaderboardPanelLifecycle<T extends LeaderboardSettings> extends LeaderboardPanelLifecycle<T> {

    protected final List<String> namesOfRaceColumns;
    protected final boolean canBoatInfoBeShown;
    protected final PaywallResolver leaderboardPaywallResolver;
    
    public AbstractMultiRaceLeaderboardPanelLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes, PaywallResolver leaderboardPaywallResolver) {
        this(leaderboard != null ? leaderboard.getNamesOfRaceColumns() : new ArrayList<String>(),
                leaderboard != null ? !leaderboard.canBoatsOfCompetitorsChangePerRace : false, stringMessages,
                availableDetailTypes, leaderboardPaywallResolver);
    }
    
    protected AbstractMultiRaceLeaderboardPanelLifecycle(List<String> namesOfRaceColumns, boolean canBoatInfoBeShown, StringMessages stringMessages,
            Iterable<DetailType> availableDetailTypes, PaywallResolver leaderboardPaywallResolver) {
        super(stringMessages, availableDetailTypes);
        this.namesOfRaceColumns = namesOfRaceColumns;
        this.canBoatInfoBeShown = canBoatInfoBeShown;
        this.leaderboardPaywallResolver = leaderboardPaywallResolver;
    }
    
    public abstract T extractUserSettings(T currentLeaderboardSettings);
    
    public abstract SettingsDialogComponent<T> getSettingsDialogComponent(T settings);

    public abstract T createDefaultSettings();
    
    public PaywallResolver getLeaderboardPaywallResolver() {
        return leaderboardPaywallResolver;
    }
}
