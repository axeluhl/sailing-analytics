package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.regattaoverview.client.RegattaRaceStatesComponent.EntryHandler;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;
import com.sap.sse.gwt.theme.client.resources.ThemeResources;

public class RegattaOverviewPanel extends SimplePanel {
    
    private final long serverUpdateRateInMs = 10000;
    private final long uiUpdateRateInMs = 450;
    
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    
    private final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    
    private final UUID eventId;
    private EventDTO eventDTO;
    private List<RaceGroupDTO> raceGroupDTOs;
    private List<EventAndRaceGroupAvailabilityListener> eventRaceGroupListeners;
    private boolean showLeaderboard = false;
    
    private RegattaRaceStatesComponent regattaRaceStatesComponent;
    
    private final TabPanel leaderboardsTabPanel;
    private final Label timeLabel;
    private final Anchor settingsButton;
    private final Anchor refreshNowButton;
    private final Anchor startStopUpdatingButton;
    private final CheckBox leaderboardCheckBox;
    private final UserAgentDetails userAgent;
    
    private final RegattaOverviewResources.LocalCss style = RegattaOverviewResources.INSTANCE.css();
    private final ThemeResources RES = ThemeResources.INSTANCE;
    
    public void setEntryClickedHandler(EntryHandler handler) {
        regattaRaceStatesComponent.setEntryClickedHandler(handler);
    }
    
    public RegattaOverviewPanel(
            SailingServiceAsync sailingService, 
            final ErrorReporter errorReporter, 
            final StringMessages stringMessages, 
            UUID eventId, 
            RegattaRaceStatesSettings settings, 
            UserAgentDetails userAgent) {
        
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.eventId = eventId;
        this.serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRateInMs);
        this.uiUpdateTimer = new Timer(PlayModes.Live, uiUpdateRateInMs);
        this.eventDTO = null;
        this.userAgent = userAgent;
        this.raceGroupDTOs = new ArrayList<RaceGroupDTO>();
        this.eventRaceGroupListeners = new ArrayList<EventAndRaceGroupAvailabilityListener>();

        retrieveEvent();
        retrieveRegattaStructure();
        
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        mainPanel.addStyleName(style.contentWrapper());
        
        regattaRaceStatesComponent = new RegattaRaceStatesComponent(sailingService, errorReporter, stringMessages, eventId, settings, uiUpdateTimer);
        this.eventRaceGroupListeners.add(regattaRaceStatesComponent);
        regattaRaceStatesComponent.setWidth("100%");
        
        refreshNowButton = new Anchor(stringMessages.refreshNow());
        refreshNowButton.setStyleName(RES.mainCss().button());
        refreshNowButton.addStyleName(style.button());

        refreshNowButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                regattaRaceStatesComponent.loadAndUpdateEventLog();
            }
            
        });
        settingsButton = new Anchor("&nbsp;", true);
        settingsButton.setStyleName(style.settingsButton());
        settingsButton.addStyleName(RES.mainCss().button());
        settingsButton.addStyleName(style.button());

        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RegattaRaceStatesSettings>(regattaRaceStatesComponent, stringMessages).show();
            }            
        });
        
        startStopUpdatingButton = new Anchor("&nbsp;", true);
        startStopUpdatingButton.setStyleName(RES.mainCss().button());
        startStopUpdatingButton.addStyleName(style.refreshButton());
        startStopUpdatingButton.addStyleName(style.button());
        startStopUpdatingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (serverUpdateTimer.getPlayState().equals(PlayStates.Playing)) {
                    serverUpdateTimer.pause();
                    startStopUpdatingButton.removeStyleName(style.refreshButton_live());
                } else if (serverUpdateTimer.getPlayState().equals(PlayStates.Paused)) {
                    serverUpdateTimer.play();
                    startStopUpdatingButton.addStyleName(style.refreshButton_live());
                }
            }
            
        });
        this.serverUpdateTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                regattaRaceStatesComponent.onUpdateServer();
            }
        });
        this.serverUpdateTimer.play();
        startStopUpdatingButton.addStyleName(style.refreshButton_live());

        this.uiUpdateTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                fireEvent(new EventTimeUpdateEvent(newTime));
            }
        });
        this.uiUpdateTimer.play();
        

        
        timeLabel = new Label();
        timeLabel.addStyleName(style.titleLabel());
        timeLabel.addStyleName(style.clockLabel());
        
        
        
        
        final boolean showLeaderboardButton = Window.Location.getParameter("enableLeaderboard") != null
                && Window.Location.getParameter("enableLeaderboard").equalsIgnoreCase("true");
        if (showLeaderboardButton) {
            leaderboardCheckBox = addLeaderboardEnablerButton();
        } else {
            leaderboardCheckBox = null;
        }
        FlowPanel flexTable = new FlowPanel();
        flexTable.addStyleName(style.functionBar());

        if (leaderboardCheckBox != null) {
            flexTable.add(leaderboardCheckBox);
        }
        // flexTable.add(refreshNowButton);
        flexTable.add(startStopUpdatingButton);
        flexTable.add(settingsButton);

        mainPanel.add(new SimplePanel(flexTable));

        mainPanel.add(regattaRaceStatesComponent);
        
        if (showLeaderboardButton) {
            leaderboardsTabPanel = new TabPanel();
            leaderboardsTabPanel.setStyleName(style.leaderboards());
            leaderboardsTabPanel.setVisible(false);
            mainPanel.add(leaderboardsTabPanel);
        } else {
            leaderboardsTabPanel = null;
        }
        
    }

    private CheckBox addLeaderboardEnablerButton() {
        final CheckBox checkBox = new CheckBox(stringMessages.leaderboard());

        checkBox.setEnabled(true);
        checkBox.setValue(false);
        checkBox.setTitle(stringMessages.showHideComponent("Leaderboard"));
        // checkBox.getElement().getStyle().setMarginRight(10, Unit.PX);
        checkBox.setStyleName(RES.mainCss().button());
        checkBox.addStyleName(style.button());
        checkBox.addStyleName(style.buttonLeaderboard());
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> newValue) {
                boolean visible = checkBox.getValue();
                showLeaderboard = visible;
                loadLeaderboard();
            }
        });
        
        return checkBox;
    }
    
    private void loadLeaderboard() {
        /*
         * Load a tabbed widget with one tab per regatta. Each tab contains the leaderboard for the regatta.
         */
        if (leaderboardsTabPanel != null) {
            if (showLeaderboard) {
                final CompetitorSelectionModel competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */ true);
                final LeaderboardSettings leaderboardSettings = LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(null, null, null, /* autoExpandFirstRace */ false, /* showRegattaRank */ true); 
                sailingService.getLeaderboardsByEvent(eventDTO, new MarkedAsyncCallback<List<StrippedLeaderboardDTO>>(
                        new AsyncCallback<List<StrippedLeaderboardDTO>>() {
                            @Override
                            public void onSuccess(List<StrippedLeaderboardDTO> result) {
                                leaderboardsTabPanel.clear();
                                for (StrippedLeaderboardDTO leaderboard : result) {
                                    LeaderboardPanel leaderboardPanel = new LeaderboardPanel(sailingService, 
                                            new AsyncActionsExecutor(), leaderboardSettings, false, 
                                            /*preSelectedRace*/null, 
                                            competitorSelectionProvider, 
                                            null, leaderboard.name, 
                                            errorReporter, stringMessages, userAgent, /*showRaceDetails*/false);
                                    leaderboardsTabPanel.add(leaderboardPanel, leaderboard.getDisplayName() + " " + stringMessages.leaderboard());
                                }
                                if (!result.isEmpty()) {
                                    leaderboardsTabPanel.setVisible(true);
                                    leaderboardsTabPanel.selectTab(0);
                                } else {
                                    leaderboardCheckBox.setValue(false);
                                    errorReporter.reportError("Error trying to load leaderboard. Either the event could not be associated to Regatta or there are no tracked races.");
                                }
                            }
                            @Override
                            public void onFailure(Throwable caught) {
                            }
                        }));
            } else {
                leaderboardsTabPanel.clear();
                leaderboardsTabPanel.setVisible(false);
            }
        }
    }


    
    private void retrieveEvent() {
        sailingService.getEventById(eventId, false, new MarkedAsyncCallback<EventDTO>(
                new AsyncCallback<EventDTO>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        settingsButton.setEnabled(false);
                        errorReporter.reportError("Error trying to load event with id " + eventId + " : "
                                + cause.getMessage());
                    }
        
                    @Override
                    public void onSuccess(EventDTO result) {
                        if (result != null) {
                            setEvent(result);
                        }
                    }
                }));
    }
    


    protected void setEvent(EventDTO event) {
        eventDTO = event;
        onEventUpdated();
    }

    private void onEventUpdated() {
        fireEvent(new EventDTOLoadedEvent(eventDTO));

        for (EventAndRaceGroupAvailabilityListener listener : this.eventRaceGroupListeners) {
            listener.onEventUpdated(eventDTO);
        }
        checkToEnableSettingsButton();
    }
    
    private void retrieveRegattaStructure() {
        sailingService.getRegattaStructureForEvent(eventId, new MarkedAsyncCallback<List<RaceGroupDTO>>(
                new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        errorReporter.reportError("Error trying to load regattas for event with id " + eventId + " : "
                                + cause.getMessage());
                    }
        
                    @Override
                    public void onSuccess(List<RaceGroupDTO> result) {
                        if (result != null) {
                            setRaceGroups(result);
                        }
                    }
                    
                }));
    }

    protected void setRaceGroups(List<RaceGroupDTO> result) {
        raceGroupDTOs.clear();
        raceGroupDTOs.addAll(result);
        onRaceGroupsUpdated();
    }

    private void onRaceGroupsUpdated() {
        for (EventAndRaceGroupAvailabilityListener listener : this.eventRaceGroupListeners) {
            listener.onRaceGroupsUpdated(raceGroupDTOs);
        }
        checkToEnableSettingsButton();
    }

    private void checkToEnableSettingsButton() {
        if (eventDTO != null && raceGroupDTOs.size() > 0) {
            settingsButton.setEnabled(true);
        }
    }
    
}
