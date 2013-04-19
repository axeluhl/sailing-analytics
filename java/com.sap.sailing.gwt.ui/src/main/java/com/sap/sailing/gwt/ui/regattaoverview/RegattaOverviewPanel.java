package com.sap.sailing.gwt.ui.regattaoverview;

import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;

public class RegattaOverviewPanel extends SimplePanel implements RegattaOverviewRaceSelectionChangeListener {
    
    private final long serverUpdateRate = 10000;
    private final long uiUpdateRate = 1000;
    
    private final Timer serverUpdateTimer;
    private final Timer uiUpdateTimer;
    
    private final SailingServiceAsync sailingService;
    protected final StringMessages stringMessages;
    @SuppressWarnings("unused")
    private final ErrorReporter errorReporter;
    
    private final String eventIdAsString;
    
    private RegattaOverviewRaceSelectionModel raceSelectionProvider;
    
    private RegattaOverviewTableComposite regattaOverviewTableComposite;
    private CourseDesignTableComposite raceCourseDesignDetailsComposite;
    
    private final Label eventNameLabel;
    private final Label venueNameLabel;
    private final Label timeLabel;
    private final Button startStopUpdatingButton;
    
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");
    
    public RegattaOverviewPanel(SailingServiceAsync sailingService, final ErrorReporter errorReporter, final StringMessages stringMessages, String eventIdAsString) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.eventIdAsString = eventIdAsString;
        
        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        eventNameLabel = new Label();
        venueNameLabel = new Label();
        
        timeLabel = new Label();
        
        Grid grid = new Grid(1, 2);
        
        raceSelectionProvider = new RegattaOverviewRaceSelectionModel(false);
        raceSelectionProvider.addRegattaOverviewRaceSelectionChangeListener(this);
        
        regattaOverviewTableComposite = new RegattaOverviewTableComposite(sailingService, errorReporter, stringMessages, eventIdAsString, raceSelectionProvider);
        
        raceCourseDesignDetailsComposite = new CourseDesignTableComposite(sailingService, errorReporter, stringMessages);
        grid.setWidget(0, 0, raceCourseDesignDetailsComposite);
        
        grid.setWidget(0, 1, regattaOverviewTableComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        
        Button refreshNowButton = new Button(stringMessages.refreshNow());
        refreshNowButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                regattaOverviewTableComposite.loadAndUpdateEventLog();
            }
            
        });
        
        this.startStopUpdatingButton = new Button(stringMessages.stopUpdating());
        this.startStopUpdatingButton.addClickHandler(new ClickHandler() {

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
        
        this.serverUpdateTimer = new Timer(PlayModes.Live, serverUpdateRate);
        this.serverUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                regattaOverviewTableComposite.onUpdateServer(date);
            }
        });
        this.serverUpdateTimer.play();

        this.uiUpdateTimer = new Timer(PlayModes.Live, uiUpdateRate);
        this.uiUpdateTimer.addTimeListener(new TimeListener() {

            @Override
            public void timeChanged(Date date) {
                onUpdateUI(date);
            }
        });
        this.uiUpdateTimer.play();
        
        Grid gridTimeRefreshStop = new Grid(1, 4);
        gridTimeRefreshStop.setWidget(0, 0, new Label(stringMessages.currentTime()));
        gridTimeRefreshStop.setWidget(0, 1, timeLabel);
        gridTimeRefreshStop.setWidget(0, 2, refreshNowButton);
        gridTimeRefreshStop.setWidget(0, 3, startStopUpdatingButton);
        
        mainPanel.add(eventNameLabel);
        mainPanel.add(venueNameLabel);
        mainPanel.add(gridTimeRefreshStop);
        mainPanel.add(grid);
        
        fillEventAndVenueName();
        onUpdateUI(new Date());
    }
    
    public void onUpdateUI(Date time) {
        timeLabel.setText(timeFormatter.format(time));
    }

    @Override
    public void onRegattaOverviewEntrySelectionChange(List<RegattaOverviewEntryDTO> selectedRegattaOverviewEntries) {
        final RegattaOverviewEntryDTO selectedRegattaOverviewEntry;
        if (selectedRegattaOverviewEntries.iterator().hasNext()) {
            selectedRegattaOverviewEntry = selectedRegattaOverviewEntries.iterator().next();
        if (selectedRegattaOverviewEntry != null && regattaOverviewTableComposite.getAllRaces() != null) {
            for(RegattaOverviewEntryDTO regattaOverviewEntryDTO: regattaOverviewTableComposite.getAllRaces()) {
                if(regattaOverviewEntryDTO.equals(selectedRegattaOverviewEntry)) {
                    raceCourseDesignDetailsComposite.setRace(regattaOverviewEntryDTO);
                    break;
                }
            }
        }
        } else {
            raceCourseDesignDetailsComposite.setRace(null);
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
