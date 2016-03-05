package com.sap.sailing.gwt.ui.raceboard;

import java.util.Map;
import java.util.UUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.panels.ResizableFlowPanel;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.perspective.AbstractPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;
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
    private final static int TIMEPANEL_COLLAPSED_HEIGHT = 67;
    private final static int TIMEPANEL_EXPANDED_HEIGHT = 96;
    
    private final RaceBoardPerspectiveLifecycle perspectiveLifecycle; 

    private final RaceBoardPanel raceBoardPanel; 
    private final FlowPanel timePanel;

    public RaceBoardPerspective(PerspectiveLifecycleWithAllSettings<RaceBoardPerspectiveLifecycle, RaceBoardPerspectiveSettings> perspectiveLifecycleWithAllSettings,
            SailingServiceAsync sailingService, MediaServiceAsync mediaService,
            UserService userService, AsyncActionsExecutor asyncActionsExecutor,
            Map<CompetitorDTO, BoatDTO> competitorsAndTheirBoats, Timer timer,
            RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName,
            String leaderboardGroupName, UUID eventId,
            ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, RaceTimesInfoProvider raceTimesInfoProvider) {
        super();
        this.settings = perspectiveLifecycleWithAllSettings.getAllSettings().getPerspectiveLifecycleAndSettings().getSettings();
        this.perspectiveLifecycle = perspectiveLifecycleWithAllSettings.getPerspectiveLifecycle();

        this.raceBoardPanel = new RaceBoardPanel(perspectiveLifecycleWithAllSettings, sailingService, mediaService, userService, asyncActionsExecutor,
                competitorsAndTheirBoats, timer, selectedRaceIdentifier, leaderboardName, null, /* event */null, settings,
                errorReporter, stringMessages, userAgent, raceTimesInfoProvider);
        this.raceBoardPanel.setSize("100%", "100%");
        this.timePanel = createTimePanelLayoutWrapper();
        
        boolean advanceTimePanelEnabled = true; 
        if(advanceTimePanelEnabled) {
            manageTimePanelToggleButton(advanceTimePanelEnabled);
        }
        
        dockPanel = new DockLayoutPanel(Unit.PX);
        dockPanel.addSouth(timePanel, TIMEPANEL_COLLAPSED_HEIGHT);                     
        dockPanel.add(raceBoardPanel);
        dockPanel.addStyleName("dockLayoutPanel");

        initWidget(dockPanel);
    }

    private void manageTimePanelToggleButton(boolean advanceTimePanelEnabled) {
        final Button toggleButton = raceBoardPanel.getTimePanel().getAdvancedToggleButton();

        if(advanceTimePanelEnabled) {
            toggleButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    boolean advancedModeShown = raceBoardPanel.getTimePanel().toggleAdvancedMode();
                    if (advancedModeShown) {
                        dockPanel.setWidgetSize(timePanel, TIMEPANEL_EXPANDED_HEIGHT);
                        toggleButton.removeStyleDependentName("Closed");
                        toggleButton.addStyleDependentName("Open");
                    } else {
                        dockPanel.setWidgetSize(timePanel, TIMEPANEL_COLLAPSED_HEIGHT);
                        toggleButton.addStyleDependentName("Closed");
                        toggleButton.removeStyleDependentName("Open");
                    }
                }
            });
        } else {
            toggleButton.setVisible(false);
        }
    }
    
    private FlowPanel createTimePanelLayoutWrapper() {
        FlowPanel timeLineInnerBgPanel = new ResizableFlowPanel();
        timeLineInnerBgPanel.addStyleName("timeLineInnerBgPanel");
        timeLineInnerBgPanel.add(raceBoardPanel.getTimePanel());
        
        FlowPanel timeLineInnerPanel = new ResizableFlowPanel();
        timeLineInnerPanel.add(timeLineInnerBgPanel);
        timeLineInnerPanel.addStyleName("timeLineInnerPanel");
        
        FlowPanel timelinePanel = new ResizableFlowPanel();
        timelinePanel.add(timeLineInnerPanel);
        timelinePanel.addStyleName("timeLinePanel");
        
        return timelinePanel;
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
