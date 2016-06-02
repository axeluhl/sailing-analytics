package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveIdAndSettings;

public class LeaderboardWithHeaderPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithHeaderPerspectiveSettings,
    PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings>,
    PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<LeaderboardWithHeaderPerspectiveSettings>> {
    
    private final SAPHeaderComponentLifecycle sapHeaderLifecycle;
    private final LeaderboardPanelLifecycle leaderboardPanelLifecycle;
    private final StringMessages stringMessages;

    public LeaderboardWithHeaderPerspectiveLifecycle(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.leaderboardPanelLifecycle = new LeaderboardPanelLifecycle(leaderboard, stringMessages);
        this.sapHeaderLifecycle = new SAPHeaderComponentLifecycle(stringMessages.leaderboard() +  ": " + leaderboard.getDisplayName(), stringMessages);
        
        this.componentLifecycles.add(leaderboardPanelLifecycle);
        this.componentLifecycles.add(sapHeaderLifecycle);
    }
    
    @Override
    public PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> createDefaultSettings() {
        PerspectiveIdAndSettings<LeaderboardWithHeaderPerspectiveSettings> perspectiveIdAndSettings = 
                new PerspectiveIdAndSettings<>(getComponentId(), createPerspectiveOwnDefaultSettings());
        return new PerspectiveCompositeSettings<>(perspectiveIdAndSettings, getComponentIdsAndDefaultSettings().getSettingsPerComponentId());
    }

    @Override
    public LeaderboardWithHeaderPerspectiveSettings createPerspectiveOwnDefaultSettings() {
        return new LeaderboardWithHeaderPerspectiveSettings();
    }

    @Override
    public PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> cloneSettings(
            PerspectiveCompositeSettings<LeaderboardWithHeaderPerspectiveSettings> settings) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public SettingsDialogComponent<LeaderboardWithHeaderPerspectiveSettings> getPerspectiveOwnSettingsDialogComponent(LeaderboardWithHeaderPerspectiveSettings perspectiveSettings) {
        return new LeaderboardPerspectiveSettingsDialogComponent(perspectiveSettings, stringMessages);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.leaderboard() + " " + stringMessages.page();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    public SAPHeaderComponentLifecycle getSapHeaderLifecycle() {
        return sapHeaderLifecycle;
    }

    public LeaderboardPanelLifecycle getLeaderboardPanelLifecycle() {
        return leaderboardPanelLifecycle;
    }

}
