package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MarkedAsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewPanel extends SimplePanel implements RegattaOverviewRaceSelectionChangeListener {
    
    private final long serverUpdateRateInMs = 10000;
    private final long uiUpdateRateInMs = 1000;
    
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    
    private final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    
    private final String eventIdAsString;
    
    private RegattaOverviewRaceSelectionModel raceSelectionProvider;
    
    private RegattaRaceStatesComponent regattaRaceStatesComponent;
    private RaceCourseComposite raceCourseComposite;
    
    private final Label eventNameLabel;
    private final Label venueNameLabel;
    private final Label timeLabel;
//    private final Button filterButton;
    private final Button settingsButton;
    private final Button refreshNowButton;
    private final Button startStopUpdatingButton;
    
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");
    
    private static final String STYLE_NAME_PREFIX = "RegattaOverview-";
    private static final String STYLE_REFRESH_STOP_TIME = STYLE_NAME_PREFIX + "RefreshStopTime";
    private static final String STYLE_FUNCTION_BAR = STYLE_NAME_PREFIX + "functionBar";
    private static final String STYLE_CONTENT_WRAPPER = STYLE_NAME_PREFIX + "contentWrapper";
    private static final String STYLE_TITLE_LABEL = STYLE_NAME_PREFIX + "TitleLabel";
    private static final String STYLE_EVENT_LABEL = STYLE_NAME_PREFIX + "EventLabel";
    private static final String STYLE_VENUE_LABEL = STYLE_NAME_PREFIX + "VenueLabel";
    private static final String STYLE_CLOCK_LABEL = STYLE_NAME_PREFIX + "ClockLabel";
    
    public RegattaOverviewPanel(SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages, String eventIdAsString) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        mainPanel.addStyleName(STYLE_CONTENT_WRAPPER);
        
        Grid grid = new Grid(2, 2);
        grid.setWidth("100%");
        
        Label raceOverviewLabel = new Label(stringMessages.courseAreaOverview());
        raceOverviewLabel.addStyleName(STYLE_TITLE_LABEL);
        
        Label courseDesignOverviewLabel = new Label(stringMessages.courseDesignOverview());
        courseDesignOverviewLabel.addStyleName(STYLE_TITLE_LABEL);
        
        grid.setWidget(0, 0, raceOverviewLabel);
        grid.setWidget(0, 1, courseDesignOverviewLabel);
        
        raceSelectionProvider = new RegattaOverviewRaceSelectionModel(false);
        raceSelectionProvider.addRegattaOverviewRaceSelectionChangeListener(this);
        
        regattaRaceStatesComponent = new RegattaRaceStatesComponent(sailingService, errorReporter, stringMessages, eventIdAsString, raceSelectionProvider);
        grid.setWidget(1, 0, regattaRaceStatesComponent);
        
        raceCourseComposite = new RaceCourseComposite(sailingService, errorReporter, stringMessages);
        grid.setWidget(1, 1, raceCourseComposite);
        
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getRowFormatter().setVerticalAlign(1, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().setWidth(0, "80%");
        grid.getColumnFormatter().setWidth(1, "20%");
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingLeft(20.0, Unit.PX);
        
        //TODO: change filter button
//        filterButton = new Button(stringMessages.disableRaceFilter());
//        filterButton.addClickHandler(new ClickHandler() {
//            
//            @Override
//            public void onClick(ClickEvent event) {
//                boolean isFilterActive = regattaRaceStatesComponent.switchFilter();
//                filterButton.setText(isFilterActive ? 
//                        stringMessages.disableRaceFilter() : stringMessages.enableRaceFilter());
//            }
//        });
        
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
        
        this.serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRateInMs);
        this.serverUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                regattaRaceStatesComponent.onUpdateServer(date);
            }
        });
        this.serverUpdateTimer.play();

        this.uiUpdateTimer = new Timer(PlayModes.Live, uiUpdateRateInMs);
        this.uiUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                onUpdateUI(date);
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
        
        HorizontalPanel refreshStartStopClockPanel = getRefreshStartStopClockPanel();
        
        flexTable.setWidget(0, 1, refreshStartStopClockPanel);
        
        mainPanel.add(flexTable);
        mainPanel.add(grid);
        
        fillEventAndVenueName();
        onUpdateUI(new Date());
    }

    private HorizontalPanel getRefreshStartStopClockPanel() {
        HorizontalPanel refreshStartStopClockPanel = new HorizontalPanel();
        refreshStartStopClockPanel.setSpacing(5);
        refreshStartStopClockPanel.setStyleName(STYLE_REFRESH_STOP_TIME);
        refreshStartStopClockPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
//        refreshStartStopClockPanel.add(filterButton);
        refreshStartStopClockPanel.add(settingsButton);
        refreshStartStopClockPanel.add(refreshNowButton);
        refreshStartStopClockPanel.add(startStopUpdatingButton);
        refreshStartStopClockPanel.add(timeLabel);
        return refreshStartStopClockPanel;
    }
    
    public void onUpdateUI(Date time) {
        timeLabel.setText(timeFormatter.format(time));
    }

    @Override
    public void onRegattaOverviewEntrySelectionChange(List<RegattaOverviewEntryDTO> selectedRegattaOverviewEntries) {
        final RegattaOverviewEntryDTO selectedRegattaOverviewEntry;
        if (selectedRegattaOverviewEntries.iterator().hasNext()) {
            selectedRegattaOverviewEntry = selectedRegattaOverviewEntries.iterator().next();
        if (selectedRegattaOverviewEntry != null && regattaRaceStatesComponent.getAllRaces() != null) {
            for(RegattaOverviewEntryDTO regattaOverviewEntryDTO: regattaRaceStatesComponent.getAllRaces()) {
                if(regattaOverviewEntryDTO.equals(selectedRegattaOverviewEntry)) {
                    raceCourseComposite.setRace(regattaOverviewEntryDTO);
                    break;
                }
            }
        }
        } else {
            raceCourseComposite.setRace(null);
        }
    }
    
    private void fillEventAndVenueName() {
        sailingService.getEventByIdAsString(eventIdAsString, new MarkedAsyncCallback<EventDTO>() {

            @Override
            protected void handleFailure(Throwable cause) {
                
            }

            @Override
            protected void handleSuccess(EventDTO result) {
                if (result != null) {
                    eventNameLabel.setText(result.name);
                    venueNameLabel.setText(result.venue.name);
                }
            }
        });
    }
    
}
