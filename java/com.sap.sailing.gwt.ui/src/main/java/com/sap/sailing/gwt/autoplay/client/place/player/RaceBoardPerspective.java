package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.Map;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;
import com.sap.sailing.gwt.ui.client.LeaderboardUpdateProvider;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPerspectiveSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleAndComponentSettings;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.security.ui.client.UserService;

/**
 * A perspective managing a RaceboardPanel and the corresponding timepanel.
 * @author Frank
 *
 */
public class RaceBoardPerspective extends AbstractPerspectiveComposite<RaceBoardPerspectiveSettings> implements LeaderboardUpdateProvider {
    private RaceBoardPerspectiveSettings settings;
    private final DockLayoutPanel dockPanel;
    private final static int TIMEPANEL_HEIGHT = 67;
    
    private final RaceBoardPerspectiveLifecycle perspectiveLifecycle; 
    private final PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> componentLifecyclesAndSettings;
    
    public RaceBoardPerspective(RaceBoardPerspectiveSettings perspectiveSettings, 
            PerspectiveLifecycleAndComponentSettings<RaceBoardPerspectiveLifecycle> componentLifecyclesAndSettings,
            SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, AsyncActionsExecutor asyncActionsExecutor,
            Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats, Timer timer,
            RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName,
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider) {
        super();
        this.settings = perspectiveSettings;
        this.perspectiveLifecycle = componentLifecyclesAndSettings.getPerspectiveLifecycle();
        this.componentLifecyclesAndSettings = componentLifecyclesAndSettings;

        RaceBoardPanel raceBoardPanel = createRaceBoardPanel(sailingService, mediaService, userService, asyncActionsExecutor, competitorsAndTheirBoats, timer, selectedRaceIdentifier, leaderboardName, errorReporter, stringMessages, userAgent, raceTimesInfoProvider); 
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

    private RaceBoardPanel createRaceBoardPanel(SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, AsyncActionsExecutor asyncActionsExecutor,
            Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats, Timer timer,
            RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName,
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider) {
      RaceBoardPanel raceBoardPanel = new RaceBoardPanel(sailingService, mediaService, userService, asyncActionsExecutor,
      competitorsAndTheirBoats, timer, selectedRaceIdentifier, leaderboardName, null, /* event */null, settings,
      errorReporter, stringMessages, userAgent, raceTimesInfoProvider, /* showMapControls */ false,
      /* isScreenLargeEnoughToOfferChartSupport */ true);

      return raceBoardPanel;
    }

    @Override
    public String getPerspectiveName() {
        return perspectiveLifecycle.getPerspectiveName();
    }

    @Override
    public String getLocalizedShortName() {
        return perspectiveLifecycle.getLocalizedShortName();
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
        return perspectiveLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<RaceBoardPerspectiveSettings> getSettingsDialogComponent() {
        return perspectiveLifecycle.getSettingsDialogComponent(settings);
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
