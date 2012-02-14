package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.SmallWindHistoryPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.TimePanel;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final RacesListBoxPanel raceListBox;
    private final TimePanel timePanel;
    private final Timer timer;
    private final RaceMap raceMap;
    private final QuickRanksListBoxComposite quickRanksListBox;
    private final RaceSelectionModel raceSelectionModel;
    
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RaceMapPanel(SailingServiceAsync sailingService, CompetitorSelectionProvider competitorSelectionProvider,
            ErrorReporter errorReporter, final EventRefresher eventRefresher, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.timer = new Timer(PlayModes.Replay, /* delayBetweenAutoAdvancesInMilliseconds */500);
        this.grid = new Grid(3, 3);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        grid.getCellFormatter().setHeight(2, 1, "100%");
        AbsolutePanel mapPanel = new AbsolutePanel();
        mapPanel.setSize("100%", "100%");
        grid.setWidget(2, 1, mapPanel);
        raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionProvider, stringMessages);
        mapPanel.add(raceMap);
        raceSelectionModel = new RaceSelectionModel();
        raceListBox = new RacesListBoxPanel(eventRefresher, raceSelectionModel, stringMessages);
        raceSelectionModel.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, raceListBox);
        PositionDTO pos = new PositionDTO();
        if (!raceMap.boatMarkers.isEmpty()) {
            LatLng latLng = raceMap.boatMarkers.values().iterator().next().getLatLng();
            pos.latDeg = latLng.getLatitude();
            pos.lngDeg = latLng.getLongitude();
        }
        SmallWindHistoryPanel windHistory = new SmallWindHistoryPanel(sailingService, pos,
        /* number of wind displays */5,
        /* time interval between displays in milliseconds */5000, stringMessages, errorReporter);
        raceSelectionModel.addRaceSelectionChangeListener(windHistory);
        grid.setWidget(1, 0, windHistory);

        ImageResource settingsImage = resources.settingsIcon();
        Anchor showConfigAnchor = new Anchor(AbstractImagePrototype.create(settingsImage).getSafeHtml());
        showConfigAnchor.setTitle(stringMessages.configuration());
        showConfigAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new SettingsDialog<RaceMapSettings>(raceMap, stringMessages).show();
            }
        });
        grid.setWidget(1, 2, showConfigAnchor);
        HorizontalPanel horizontalRanksVerticalAndCheckboxesManeuversPanel = new HorizontalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.setSpacing(15);
        VerticalPanel ranksAndCheckboxAndTailLengthPanel = new VerticalPanel();
        quickRanksListBox = new QuickRanksListBoxComposite(competitorSelectionProvider);
        quickRanksListBox.getListBox().setVisibleItemCount(20);
        ranksAndCheckboxAndTailLengthPanel.add(quickRanksListBox);
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(ranksAndCheckboxAndTailLengthPanel);
        VerticalPanel verticalPanelRadioAndCheckboxes = new VerticalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(verticalPanelRadioAndCheckboxes);
        grid.setWidget(2, 0, horizontalRanksVerticalAndCheckboxesManeuversPanel);
        timePanel = new TimePanel(timer, stringMessages);
        timer.addTimeListener(this);
        timer.addTimeListener(windHistory);
        grid.setWidget(1, 1, timePanel);
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
        raceListBox.fillEvents(result);
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        if (!selectedRaces.isEmpty() && selectedRaces.get(selectedRaces.size() - 1) != null) {
            RaceIdentifier raceIdentifier = selectedRaces.get(selectedRaces.size() - 1);
            RaceDTO race = raceListBox.getRace(raceIdentifier);
            competitorSelectionProvider.setCompetitors(race.competitors);
            
            updateTimePanel(race);
        }
        raceMap.onRaceSelectionChange(selectedRaces);
    }

    private void updateTimePanel(RaceDTO selectedRace) {
        Date min = null;
        Date max = null;
        
        if (selectedRace.startOfTracking != null) {
            min = selectedRace.startOfTracking;
        }
        if (selectedRace.endOfRace != null) {
            max = selectedRace.endOfRace;
        } else if (selectedRace.timePointOfNewestEvent != null) {
            max = selectedRace.timePointOfNewestEvent;
            timer.setPlayMode(PlayModes.Live);
        }
        
        if(min != null && max != null)
            timePanel.setMinMax(min, max);
        
        // set initial timer position
        switch(timer.getPlayMode()) {
            case Live:
                if(selectedRace.timePointOfNewestEvent != null) {
                    timer.setTime(selectedRace.timePointOfNewestEvent.getTime());
                }
                break;
            case Replay:
                if(selectedRace.endOfRace != null) {
                    timer.setTime(selectedRace.endOfRace.getTime());
                } else {
                    timer.setTime(selectedRace.startOfRace.getTime());
                }
                break;
        }
        
        sailingService.getRaceTimesInfo(selectedRace.getRaceIdentifier(), 
                new AsyncCallback<RaceTimesInfoDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error obtaining leg timepoints: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RaceTimesInfoDTO raceTimesInfo) {
                        // raceTimesInfo can be null if the race is not tracked anymore
                        if (raceTimesInfo != null) {
                            timePanel.setLegMarkers(raceTimesInfo.getLegTimes());
                            if (raceTimesInfo.getStartOfRace() != null) {
                                // set the new start time 
                                Date startOfRace = raceTimesInfo.getStartOfRace();
                                Date startOfTimeslider = new Date(startOfRace.getTime() - 5 * 60 * 1000);

                                timePanel.changeMin(startOfTimeslider);
                            }
                            // set time to end of race
                            if(raceTimesInfo.getLastLegTimes() != null) {
                                timer.setTime(raceTimesInfo.getLastLegTimes().firstPassingDate.getTime());
                            }
                        } else {
                            timePanel.reset();
                        }
                    }
                });
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            List<RaceIdentifier> selection = raceSelectionModel.getSelectedRaces();
            if (!selection.isEmpty()) {
                RaceIdentifier race = selection.get(selection.size() - 1);
                if (race != null) {
                    sailingService.getQuickRanks(race, date,
                            new AsyncCallback<List<QuickRankDTO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining quick rankings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<QuickRankDTO> result) {
                                    quickRanksListBox.fillQuickRanks(result);
                                }
                            });
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.user.client.ui.RequiresResize#onResize()
     */
    @Override
    public void onResize() {
        // handle what is required by @link{ProvidesResize}
        Widget child = getWidget();
        if (child instanceof RequiresResize) {
            ((RequiresResize) child).onResize();
        }
        // and ensure the map (indirect child) is also informed about resize
        if (raceMap.map != null) {
            raceMap.map.onResize();
        }
        if (timePanel != null) {
            timePanel.onResize();
        }
    }
}
