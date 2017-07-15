package com.sap.sailing.gwt.regattaoverview.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.regattaoverview.client.RegattaRaceStatesComponent.EntryHandler;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaOverviewContextDefinition;
import com.sap.sailing.gwt.settings.client.regattaoverview.RegattaRaceStatesSettings;
import com.sap.sailing.gwt.settings.client.utils.StoredSettingsLocationFactory;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
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
import com.sap.sse.gwt.client.shared.components.LinkWithSettingsGenerator;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.shared.settings.DefaultOnSettingsLoadedCallback;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

public class RegattaOverviewPanel extends SimplePanel {
    
    private final long serverUpdateRateInMs = 10000;
    private final long uiUpdateRateInMs = 450;
    
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    
    private final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    
    private final RegattaOverviewContextDefinition regattaOverviewContextDefinition;
    private EventDTO eventDTO;
    private List<RaceGroupDTO> raceGroupDTOs;
    private boolean showLeaderboard = false;
    
    private RegattaRaceStatesComponent regattaRaceStatesComponent;
    
    private TabPanel leaderboardsTabPanel;
    private final Anchor settingsButton;
    private final Anchor refreshNowButton;
    private final Anchor startStopUpdatingButton;
    private CheckBox leaderboardCheckBox;
    private final FlowPanel repeatedInfoLabel = new FlowPanel();
    private VerticalPanel mainPanel;
    private UserService userService;
    private ComponentContext<RegattaRaceStatesSettings> componentContext;
    
    private final RegattaOverviewResources.LocalCss style = RegattaOverviewResources.INSTANCE.css();
    private final SharedResources RES = SharedResources.INSTANCE;
    
    private EntryHandler entryClickedHandler = null;
    
    public void setEntryClickedHandler(EntryHandler handler) {
        entryClickedHandler = handler;
        if(regattaRaceStatesComponent != null) {
            regattaRaceStatesComponent.setEntryClickedHandler(handler);
        }
    }
    
    public RegattaOverviewPanel(
            SailingServiceAsync sailingService,
            UserService userService,
            final ErrorReporter errorReporter, 
            final StringMessages stringMessages, 
            RegattaOverviewContextDefinition regattaOverviewContextDefinition) {
        
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaOverviewContextDefinition = regattaOverviewContextDefinition;
        this.serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRateInMs);
        this.uiUpdateTimer = new Timer(PlayModes.Live, uiUpdateRateInMs);
        this.eventDTO = null;
        this.raceGroupDTOs = new ArrayList<RaceGroupDTO>();
        this.userService = userService;

        mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        mainPanel.addStyleName(style.contentWrapper());
        
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
        settingsButton.ensureDebugId("RegattaOverviewSettingsButton");
        settingsButton.setStyleName(style.settingsButton());
        settingsButton.addStyleName(RES.mainCss().button());
        settingsButton.addStyleName(style.button());

        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO should we always set ignoreLocalSettings=true when creating links?
                new SettingsDialog<RegattaRaceStatesSettings>(regattaRaceStatesComponent, stringMessages,
                        new LinkWithSettingsGenerator<>(regattaOverviewContextDefinition,
                                IgnoreLocalSettings.getIgnoreLocalSettingsFromCurrentUrl())).show();
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
        
        //FIXME The chained calls from here are independent from each other and should be implemented with Dispatching Framework
        sailingService.getEventById(regattaOverviewContextDefinition.getEvent(), false, new MarkedAsyncCallback<EventDTO>(
                new AsyncCallback<EventDTO>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        settingsButton.setEnabled(false);
                        errorReporter.reportError(stringMessages.errorLoadingEvent(regattaOverviewContextDefinition.getEvent(), cause.getMessage()));
                        continueInitAfterEventRetrieved();
                    }
        
                    @Override
                    public void onSuccess(EventDTO result) {
                        setEvent(result);
                        continueInitAfterEventRetrieved();
                    }
                }));
        
    }

    private void continueInitAfterEventRetrieved() {
        sailingService.getRegattaStructureForEvent(regattaOverviewContextDefinition.getEvent(), new MarkedAsyncCallback<List<RaceGroupDTO>>(
                new AsyncCallback<List<RaceGroupDTO>>() {
                    @Override
                    public void onFailure(Throwable cause) {
                        errorReporter.reportError(stringMessages.errorLoadingRegattaStructure(regattaOverviewContextDefinition.getEvent(), cause.getMessage()));
                        continueInitAfterRaceGroupsRetrieved();
                    }
        
                    @Override
                    public void onSuccess(List<RaceGroupDTO> result) {
                        raceGroupDTOs.clear();
                        raceGroupDTOs.addAll(result);
                        continueInitAfterRaceGroupsRetrieved();
                    }
                    
                }));
    }

    private void continueInitAfterRaceGroupsRetrieved() {
        final StoredSettingsLocation storageDefinition = StoredSettingsLocationFactory.createStoredSettingsLocatorForRegattaOverview(regattaOverviewContextDefinition);
        final RegattaRaceStatesComponentLifecycle lifecycle = new RegattaRaceStatesComponentLifecycle(eventDTO == null ? null : eventDTO.venue.getCourseAreas(), raceGroupDTOs);
        componentContext = new ComponentContextWithSettingsStorage<>(
                lifecycle, userService, storageDefinition);
        
        componentContext.getInitialSettings(new DefaultOnSettingsLoadedCallback<RegattaRaceStatesSettings>() {
            @Override
            public void onSuccess(RegattaRaceStatesSettings defaultSettings) {
                continueInitAfterSettingsRetrieved(defaultSettings);
            }
        });
    }

    private void continueInitAfterSettingsRetrieved(RegattaRaceStatesSettings defaultSettings) {
        regattaRaceStatesComponent = new RegattaRaceStatesComponent(null, componentContext, sailingService, errorReporter,
                stringMessages,
                regattaOverviewContextDefinition.getEvent(), eventDTO, raceGroupDTOs, defaultSettings, uiUpdateTimer);
        
        regattaRaceStatesComponent.setEntryClickedHandler(entryClickedHandler);
        
        regattaRaceStatesComponent.setWidth("100%");
        
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
        

        // TODO create a perspective and add this parameter to the perspectiveOwnSettings
        final boolean showLeaderboardButton = Window.Location.getParameter("enableLeaderboard") != null
                && Window.Location.getParameter("enableLeaderboard").equalsIgnoreCase("true");
        if (showLeaderboardButton) {
            leaderboardCheckBox = addLeaderboardEnablerButton();
        } else {
            leaderboardCheckBox = null;
        }
        FlowPanel flowPanelLeft = new FlowPanel();
        flowPanelLeft.addStyleName(style.functionBarLeft());
        flowPanelLeft.add(repeatedInfoLabel);
        repeatedInfoLabel.setStyleName(style.repeatedInfoLabel());
        regattaRaceStatesComponent.setRepeatedInfoLabel(repeatedInfoLabel);

        FlowPanel flowPanelRight = new FlowPanel();
        flowPanelRight.addStyleName(style.functionBar());

        if (leaderboardCheckBox != null) {
            flowPanelRight.add(leaderboardCheckBox);
        }
        // flexTable.add(refreshNowButton);
        flowPanelRight.add(startStopUpdatingButton);
        flowPanelRight.add(settingsButton);

        FlexTable leftRightToolbar = new FlexTable();
        leftRightToolbar.setWidget(0, 1, flowPanelRight);
        leftRightToolbar.setWidget(0, 0, flowPanelLeft);

        leftRightToolbar.getElement().getStyle().setWidth(100, Unit.PCT);
        mainPanel.add(leftRightToolbar);
        mainPanel.add(regattaRaceStatesComponent);
        
        if (showLeaderboardButton) {
            leaderboardsTabPanel = new TabPanel();
            leaderboardsTabPanel.setStyleName(style.leaderboards());
            leaderboardsTabPanel.setVisible(false);
            mainPanel.add(leaderboardsTabPanel);
        } else {
            leaderboardsTabPanel = null;
        }
        checkToEnableSettingsButton();
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
                final LeaderboardSettings leaderboardSettings = new LeaderboardSettings(); 
                sailingService.getLeaderboardsByEvent(eventDTO, new MarkedAsyncCallback<List<StrippedLeaderboardDTO>>(
                        new AsyncCallback<List<StrippedLeaderboardDTO>>() {
                            @Override
                            public void onSuccess(List<StrippedLeaderboardDTO> result) {
                                leaderboardsTabPanel.clear();
                                for (StrippedLeaderboardDTO leaderboard : result) {
                                    LeaderboardPanel leaderboardPanel = new LeaderboardPanel(null, null, sailingService,
                                            new AsyncActionsExecutor(), leaderboardSettings, false, 
                                            /*preSelectedRace*/null, 
                                            competitorSelectionProvider, 
                                            null, leaderboard.name, 
                                            errorReporter, stringMessages, /* showRaceDetails */false,new ClassicLeaderboardStyle());
                                    leaderboardsTabPanel.add(leaderboardPanel,
                                            (leaderboard.getDisplayName() == null ? leaderboard.name : leaderboard.getDisplayName())
                                            + " " + stringMessages.leaderboard());
                                }
                                if (!result.isEmpty()) {
                                    leaderboardsTabPanel.setVisible(true);
                                    leaderboardsTabPanel.selectTab(0);
                                } else {
                                    leaderboardCheckBox.setValue(false);
                                    errorReporter.reportError(stringMessages.errorLoadingLeaderBoardByEvent());
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

    private void checkToEnableSettingsButton() {
        if (eventDTO != null && raceGroupDTOs.size() > 0) {
            settingsButton.setEnabled(true);
        }
    }
    
    private void setEvent(EventDTO event) {
        eventDTO = event;
        fireEvent(new EventDTOLoadedEvent(eventDTO));
    }
        
}
