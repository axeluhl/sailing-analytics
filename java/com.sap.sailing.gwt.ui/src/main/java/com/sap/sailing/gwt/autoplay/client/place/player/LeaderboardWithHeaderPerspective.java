package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeader;
import com.sap.sailing.gwt.autoplay.client.shared.header.SAPHeaderLifecycle.SAPHeaderConstructionParameters;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle.LeaderboardPanelConstructionParameters;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;

/**
 * A perspective managing a header with a single leaderboard filling the rest of the screen.
 * @author Frank
 *
 */
public class LeaderboardWithHeaderPerspective extends AbstractPerspectiveComposite<LeaderboardPerspectiveSettings> {
    private LeaderboardPerspectiveSettings settings;
    private final DockLayoutPanel dockPanel;
    private static int SAP_HEADER_HEIGHT = 70;

    public LeaderboardWithHeaderPerspective(LeaderboardPerspectiveSettings perspectiveSettings, 
            SAPHeaderConstructionParameters sapHeaderConstructionParameters,
            LeaderboardPanelConstructionParameters leaderboardParameters,
            StringMessages stringMessages) {
        super();
        this.settings = perspectiveSettings;
        
        SAPHeader sapHeader = sapHeaderConstructionParameters.createComponent();
        LeaderboardPanel leaderboardPanel = leaderboardParameters.createComponent();
        components.add(sapHeader);
        components.add(leaderboardPanel);
        
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addNorth(sapHeader, SAP_HEADER_HEIGHT);
        dockPanel.add(leaderboardPanel);
        initWidget(dockPanel);
    }

    @Override
    public String getPerspectiveName() {
        return StringMessages.INSTANCE.leaderboard() + " Viewer";
    }

    @Override
    public String getLocalizedShortName() {
        return getPerspectiveName();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<LeaderboardPerspectiveSettings> getSettingsDialogComponent() {
        return new LeaderboardPerspectiveSettingsDialogComponent(settings, StringMessages.INSTANCE);
    }

    @Override
    public LeaderboardPerspectiveSettings getSettings() {
        return settings;
    }

    @Override
    public void updateSettings(LeaderboardPerspectiveSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }
}
