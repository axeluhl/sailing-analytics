package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.MediaPlayerLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanelLifecycle;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;

/**
 * A perspective managing a RaceboardPanel and the corresponding timepanel.
 * @author Frank
 *
 */
public class RaceBoardPerspective extends AbstractPerspectiveComposite<RaceBoardPerspectiveSettings> implements LeaderboardUpdateProvider {
    private RaceBoardPerspectiveSettings settings;
    private final DockLayoutPanel dockPanel;
    private final static int TIMEPANEL_HEIGHT = 67;
    
    public RaceBoardPerspective(RaceBoardPerspectiveSettings perspectiveSettings, WindChartLifecycle.ConstructionParameters windChartConstParams,
            RaceMapLifecycle.ConstructionParameters raceMapConstParams,
            LeaderboardPanelLifecycle.ConstructionParameters leaderboardPanelConstParams,
            MultiCompetitorRaceChartLifecycle.ConstructionParameters multiChartConstParams,
            MediaPlayerLifecycle.ConstructionParameters mediaPlayerConstParams,
            StringMessages stringMessages, RaceBoardPanel raceBoardPanel) {
        super();
        this.settings = perspectiveSettings;

        raceBoardPanel.setSize("100%", "100%");
        FlowPanel timePanel = createTimePanel(raceBoardPanel);
        
        final Button toggleButton = raceBoardPanel.getTimePanel().getAdvancedToggleButton();
        toggleButton.setVisible(false);

        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addSouth(timePanel, TIMEPANEL_HEIGHT);                     
        dockPanel.add(raceBoardPanel);

        initWidget(dockPanel);
    }

    private FlowPanel createTimePanel(RaceBoardPanel raceBoardPanel) {
        FlowPanel timeLineInnerBgPanel = new FlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimePanel());
        
        FlowPanel timeLineInnerPanel = new FlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new FlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
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
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getSettingsDialogComponent() {
        return new RaceBoardPerspectiveSettingsDialogComponent(settings, StringMessages.INSTANCE);
    }

    @Override
    public RaceBoardPerspectiveSettings getSettings() {
        return settings;
    }

    @Override
    public void updateSettings(RaceBoardPerspectiveSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }

    @Override
    public void addLeaderboardUpdateListener(LeaderboardUpdateListener listener) {
    }

    @Override
    public void removeLeaderboardUpdateListener(LeaderboardUpdateListener listener) {
    }
}
