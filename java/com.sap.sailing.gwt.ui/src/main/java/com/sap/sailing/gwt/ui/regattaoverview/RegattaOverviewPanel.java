package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.regattaoverview.RegattaRaceStatesComponent.EntryHandler;
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
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class RegattaOverviewPanel extends SimplePanel {
    private final static String STYLE_VIEWER_TOOLBAR_INNERELEMENT = "viewerToolbar-innerElement";
    
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
    private final Label eventNameLabel;
    private final Label venueNameLabel;
    private final Label timeLabel;
    private final Button settingsButton;
    private final Button refreshNowButton;
    private final Button startStopUpdatingButton;
    private final CheckBox leaderboardCheckBox;
    private final UserAgentDetails userAgent;
    
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");
    
    private static final String STYLE_NAME_PREFIX = "RegattaOverview-";
    private static final String STYLE_REFRESH_STOP_TIME = STYLE_NAME_PREFIX + "RefreshStopTime";
    private static final String STYLE_FUNCTION_BAR = STYLE_NAME_PREFIX + "functionBar";
    private static final String STYLE_CONTENT_WRAPPER = STYLE_NAME_PREFIX + "contentWrapper";
    private static final String STYLE_TITLE_LABEL = STYLE_NAME_PREFIX + "TitleLabel";
    private static final String STYLE_EVENT_LABEL = STYLE_NAME_PREFIX + "EventLabel";
    private static final String STYLE_VENUE_LABEL = STYLE_NAME_PREFIX + "VenueLabel";
    private static final String STYLE_CLOCK_LABEL = STYLE_NAME_PREFIX + "ClockLabel";
    
    public void setEntryClickedHandler(EntryHandler handler) {
        regattaRaceStatesComponent.setEntryClickedHandler(handler);
    }
    
    public RegattaOverviewPanel(SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages, 
            UUID eventId, RegattaRaceStatesSettings settings, UserAgentDetails userAgent) {
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
        mainPanel.addStyleName(STYLE_CONTENT_WRAPPER);
        
        regattaRaceStatesComponent = new RegattaRaceStatesComponent(sailingService, errorReporter, stringMessages, eventId, settings, uiUpdateTimer);
        this.eventRaceGroupListeners.add(regattaRaceStatesComponent);
        regattaRaceStatesComponent.setWidth("100%");
        
        refreshNowButton = new Button(stringMessages.refreshNow());
        refreshNowButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                regattaRaceStatesComponent.loadAndUpdateEventLog();
            }
            
        });
        settingsButton = new Button(stringMessages.settings());
        settingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RegattaRaceStatesSettings>(regattaRaceStatesComponent, stringMessages).show();
            }            
        });
        
        startStopUpdatingButton = new Button(stringMessages.stopUpdating());
        startStopUpdatingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (serverUpdateTimer.getPlayState().equals(PlayStates.Playing)) {
                    serverUpdateTimer.pause();
                    startStopUpdatingButton.setText(stringMessages.startUpdating());
                } else if (serverUpdateTimer.getPlayState().equals(PlayStates.Paused)) {
                    serverUpdateTimer.play();
                    startStopUpdatingButton.setText(stringMessages.stopUpdating());
                }
            }
            
        });
        this.refreshNowButton.getElement().getStyle().setMarginLeft(20.0, Unit.PX);
        this.startStopUpdatingButton.getElement().getStyle().setMarginLeft(20.0, Unit.PX);
        this.serverUpdateTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                regattaRaceStatesComponent.onUpdateServer();
            }
        });
        this.serverUpdateTimer.play();
        this.uiUpdateTimer.addTimeListener(new TimeListener() {
            @Override
            public void timeChanged(Date newTime, Date oldTime) {
                onUpdateUI(newTime);
            }
        });
        this.uiUpdateTimer.play();
        
        eventNameLabel = new Label();
        eventNameLabel.addStyleName(STYLE_EVENT_LABEL);
        eventNameLabel.addStyleName(STYLE_TITLE_LABEL);
        
        venueNameLabel = new Label();
        venueNameLabel.addStyleName(STYLE_TITLE_LABEL);
        venueNameLabel.addStyleName(STYLE_VENUE_LABEL);
        
        timeLabel = new Label();
        timeLabel.addStyleName(STYLE_TITLE_LABEL);
        timeLabel.addStyleName(STYLE_CLOCK_LABEL);
        
        FlexTable flexTable = new FlexTable();
        flexTable.setWidth("100%");
        flexTable.addStyleName(STYLE_FUNCTION_BAR);
        
        Grid eventVenueGrid = new Grid(1, 2);
        eventVenueGrid.setCellPadding(5);
        eventVenueGrid.setWidget(0, 0, eventNameLabel);
        eventVenueGrid.setWidget(0, 1, venueNameLabel);
        
        flexTable.setWidget(0, 0, eventVenueGrid);
        flexTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        
        final boolean showLeaderboardButton = Window.Location.getParameter("enableLeaderboard") != null
                && Window.Location.getParameter("enableLeaderboard").equalsIgnoreCase("true");
        if (showLeaderboardButton) {
            leaderboardCheckBox = addLeaderboardEnablerButton();
            leaderboardCheckBox.getElement().getStyle().setMarginLeft(20.0, Unit.PX);
        } else {
            leaderboardCheckBox = null;
        }

        HorizontalPanel refreshStartStopClockPanel = getRefreshStartStopClockPanel();
        flexTable.setWidget(0, 1, refreshStartStopClockPanel);
        
        mainPanel.add(flexTable);
        mainPanel.add(regattaRaceStatesComponent);
        
        if (showLeaderboardButton) {
            leaderboardsTabPanel = new TabPanel();
            leaderboardsTabPanel.setStyleName("RegattaOverview-Leaderboards");
            leaderboardsTabPanel.setVisible(false);
            mainPanel.add(leaderboardsTabPanel);
        } else {
            leaderboardsTabPanel = null;
        }
        
        onUpdateUI(uiUpdateTimer.getLiveTimePointAsDate());
    }
    
    private CheckBox addLeaderboardEnablerButton() {
        final CheckBox checkBox = new CheckBox(stringMessages.leaderboard());
        
        checkBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        
        checkBox.setEnabled(true);
        checkBox.setValue(false);
        checkBox.setTitle(stringMessages.showHideComponent("Leaderboard"));
        checkBox.addStyleName(STYLE_VIEWER_TOOLBAR_INNERELEMENT);

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

    private HorizontalPanel getRefreshStartStopClockPanel() {
        HorizontalPanel refreshStartStopClockPanel = new HorizontalPanel();
        refreshStartStopClockPanel.setSpacing(5);
        refreshStartStopClockPanel.setStyleName(STYLE_REFRESH_STOP_TIME);
        refreshStartStopClockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        if (leaderboardCheckBox != null) {
            refreshStartStopClockPanel.add(leaderboardCheckBox);
        }
        refreshStartStopClockPanel.add(settingsButton);
        refreshStartStopClockPanel.add(refreshNowButton);
        refreshStartStopClockPanel.add(startStopUpdatingButton);
        refreshStartStopClockPanel.add(timeLabel);
        return refreshStartStopClockPanel;
    }
    
    public void onUpdateUI(Date time) {
        timeLabel.setText(timeFormatter.format(time));
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
    
    private void fillEventAndVenueName() {
        eventNameLabel.setText(eventDTO.getName());
        venueNameLabel.setText(eventDTO.venue.getName());
    }

    protected void setEvent(EventDTO event) {
        eventDTO = event;
        onEventUpdated();
    }

    private void onEventUpdated() {
        fillEventAndVenueName();
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
