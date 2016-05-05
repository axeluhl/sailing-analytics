package com.sap.sailing.gwt.autoplay.client.shared.leaderboard;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderComponentLifecycle;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeLifecycleTabbedSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndSettings;

public class LeaderboardWithHeaderPerspectiveLifecycle extends AbstractPerspectiveLifecycle<LeaderboardWithHeaderPerspectiveSettings,
    PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings>,
    PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings>> {
    
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
    public PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> getSettingsDialogComponent(
            PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> settings) {
        // collect the component lifecycle's for contained components and combine them with the corresponding settings
        
        // TOOD: Take the settings from the settings parameter
        CompositeLifecycleSettings componentLifecyclesAndDefaultSettings = getComponentLifecyclesAndDefaultSettings();
        
        LeaderboardWithHeaderPerspectiveSettings perspectiveSettings = settings.getPerspectiveLifecycleAndSettings().getSettings();
        PerspectiveLifecycleAndSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> perspectiveLifecycleAndSettings =
                new PerspectiveLifecycleAndSettings<>(this, perspectiveSettings); 
        
        PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> perspectiveCompositeSettings = new PerspectiveCompositeLifecycleSettings<>(perspectiveLifecycleAndSettings, componentLifecyclesAndDefaultSettings);
        
        return new PerspectiveCompositeLifecycleTabbedSettingsDialogComponent<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings>(perspectiveCompositeSettings);
    }

    @Override
    public PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> createDefaultSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> cloneSettings(
            PerspectiveCompositeLifecycleSettings<LeaderboardWithHeaderPerspectiveLifecycle, LeaderboardWithHeaderPerspectiveSettings> settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected SettingsDialogComponent<LeaderboardWithHeaderPerspectiveSettings> getPerspectiveSettingsDialogComponent(LeaderboardWithHeaderPerspectiveSettings perspectiveSettings) {
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
