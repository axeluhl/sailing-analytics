package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.SmallWindHistoryPanel;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final RacesListBoxPanel newRaceListBox;
    private final ListBox quickRanksBox;
    private final List<CompetitorDAO> quickRanksList;
    private final TimePanel timePanel;

    private final Set<CompetitorDAO> competitorsSelectedInMap;
    private final Timer timer;
    private List<Pair<CheckBox, String>> checkboxAndType;
    private CheckBox checkBoxDouglasPeuckerPoints;

    private final CheckBox showOnlySelected;

    private final IntegerBox tailLengthBox;

    /**
     * RPC calls may receive responses out of order if there are multiple calls in-flight at the same time. If the time
     * slider is moved quickly it generates many requests for boat positions quickly after each other. Sometimes,
     * responses for requests send later may return before the responses to all earlier requests have been received and
     * processed. This counter is used to number the requests. When processing of a response for a later request has
     * already begun, responses to earlier requests will be ignored.
     */
    private int boatPositionRequestIDCounter;

    /**
     * Corresponds to {@link #boatPositionRequestIDCounter}. As soon as the processing of a response for a request ID
     * begins, this attribute is set to the ID. A response won't be processed if a later response is already being
     * processed.
     */
    private int startedProcessingRequestID;

    private final RaceMap raceMap;

    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */500);
        competitorsSelectedInMap = new HashSet<CompetitorDAO>();
        checkboxAndType = new ArrayList<Pair<CheckBox, String>>();
        final VerticalPanel verticalCheckBoxPanel = new VerticalPanel();
        verticalCheckBoxPanel.add(new Label(stringConstants.maneuverTypes()));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox(stringConstants.headUp()), "HEAD_UP"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox(stringConstants.bearAway()), "BEAR_AWAY"));
        CheckBox checkBoxTack = new CheckBox(stringConstants.tack());
        checkBoxTack.setValue(true);
        checkboxAndType.add(new Pair<CheckBox, String>(checkBoxTack, "TACK"));
        CheckBox checkBoxJibe = new CheckBox(stringConstants.jibe());
        checkBoxJibe.setValue(true);
        checkboxAndType.add(new Pair<CheckBox, String>(checkBoxJibe, "JIBE"));
        CheckBox checkBoxPenalty = new CheckBox(stringConstants.penaltyCircle());
        checkBoxPenalty.setValue(true);
        checkboxAndType.add(new Pair<CheckBox, String>(checkBoxPenalty, "PENALTY_CIRCLE"));
        CheckBox checkBoxMarkPassing = new CheckBox(stringConstants.markPassing());
        checkBoxMarkPassing.setValue(true);
        checkboxAndType.add(new Pair<CheckBox, String>(checkBoxMarkPassing, "MARK_PASSING"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox(stringConstants.otherManeuver()), "OTHER"));
        
        for (Pair<CheckBox, String> pair : checkboxAndType) {
            pair.getA().addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if (!timer.isPlaying() && raceMap.data.lastManeuverResult != null) {
                        raceMap.removeAllManeuverMarkers();
                        showManeuvers(raceMap.data.lastManeuverResult);
                    }
                }
            });
            verticalCheckBoxPanel.add(pair.getA());
        }

        checkBoxDouglasPeuckerPoints = new CheckBox(stringConstants.douglasPeuckerPoints());
        checkBoxDouglasPeuckerPoints.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (!timer.isPlaying() && raceMap.data.lastDouglasPeuckerResult != null && event.getValue()) {
                    raceMap.showDouglasPeuckerPoints = true;
                    raceMap.removeAllMarkDouglasPeuckerpoints();
                    raceMap.showMarkDouglasPeuckerPoints(raceMap.data.lastDouglasPeuckerResult);
                } else if (!event.getValue()) {
                    raceMap.showDouglasPeuckerPoints = false;
                    raceMap.removeAllMarkDouglasPeuckerpoints();
                }
            }
        });
        verticalCheckBoxPanel.add(checkBoxDouglasPeuckerPoints);
        this.grid = new Grid(3, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        grid.getCellFormatter().setHeight(2, 1, "100%");
        
        raceMap = new RaceMap(sailingService, errorReporter, timer);
        raceMap.loadMapsAPI(grid, 2, 1);

        setMapDisplayOptions();

        newRaceListBox = new RacesListBoxPanel(eventRefresher, stringConstants);
        newRaceListBox.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, newRaceListBox);
        PositionDAO pos = new PositionDAO();
        if (!raceMap.data.boatMarkers.isEmpty()) {
            LatLng latLng = raceMap.data.boatMarkers.values().iterator().next().getLatLng();
            pos.latDeg = latLng.getLatitude();
            pos.lngDeg = latLng.getLongitude();
        }
        SmallWindHistoryPanel windHistory = new SmallWindHistoryPanel(sailingService, pos,
        /* number of wind displays */5,
        /* time interval between displays in milliseconds */5000, stringConstants, errorReporter);
        newRaceListBox.addRaceSelectionChangeListener(windHistory);
        grid.setWidget(1, 0, windHistory);
        HorizontalPanel horizontalRanksVerticalAndCheckboxesManeuversPanel = new HorizontalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.setSpacing(15);
        VerticalPanel ranksAndCheckboxAndTailLength = new VerticalPanel();
        HorizontalPanel labelAndTailLengthBox = new HorizontalPanel();
        labelAndTailLengthBox.add(new Label(stringConstants.tailLength()));
        tailLengthBox = new IntegerBox();
        tailLengthBox.setValue((int) (raceMap.tailLengthInMilliSeconds / 1000));
        tailLengthBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                raceMap.tailLengthInMilliSeconds = 1000l * event.getValue();
                refreshMapContents();
            }
        });
        labelAndTailLengthBox.add(tailLengthBox);
        ranksAndCheckboxAndTailLength.add(labelAndTailLengthBox);
        showOnlySelected = new CheckBox(stringConstants.showOnlySelected());
        showOnlySelected.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                refreshMapContents();
            }
        });
        ranksAndCheckboxAndTailLength.add(showOnlySelected);
        quickRanksList = new ArrayList<CompetitorDAO>();
        quickRanksBox = new ListBox(/* isMultipleSelect */true);
        quickRanksBox.setVisibleItemCount(20);
        quickRanksBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                updateBoatSelection();
            }
        });
        quickRanksBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateBoatSelection();
            }
        });
        ranksAndCheckboxAndTailLength.add(quickRanksBox);
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(ranksAndCheckboxAndTailLength);

        VerticalPanel verticalPanelRadioAndCheckboxes = new VerticalPanel();
        verticalPanelRadioAndCheckboxes.add(verticalCheckBoxPanel);
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(verticalPanelRadioAndCheckboxes);

        grid.setWidget(2, 0, horizontalRanksVerticalAndCheckboxesManeuversPanel);
        timePanel = new TimePanel(stringConstants, timer);
        timer.addTimeListener(this);
        timer.addTimeListener(windHistory);
        grid.setWidget(1, 1, timePanel);
    }

    private boolean getCheckboxValueManeuver(String maneuverType) {
        for (Pair<CheckBox, String> pair : checkboxAndType) {
            if (pair.getB().equals(maneuverType)) {
                return pair.getA().getValue();
            }
        }
        return false;
    }

    private void updateBoatSelection() {
        for (int i = 0; i < quickRanksBox.getItemCount(); i++) {
            setSelectedInMap(quickRanksList.get(i), quickRanksBox.isItemSelected(i));
        }
        if (showOnlySelected.getValue()) {
            refreshMapContents();
        }
    }

    private void setSelectedInMap(CompetitorDAO competitorDAO, boolean itemSelected) {
        if (!itemSelected && competitorsSelectedInMap.contains(competitorDAO)) {
            // "lowlight" currently selected competitor
            Marker highlightedMarker = raceMap.data.boatMarkers.get(competitorDAO);
            if (highlightedMarker != null) {
                Marker lowlightedMarker = raceMap.createBoatMarker(competitorDAO, false);
                raceMap.map.removeOverlay(highlightedMarker);
                raceMap.map.addOverlay(lowlightedMarker);
                raceMap.data.boatMarkers.put(competitorDAO, lowlightedMarker);
                competitorsSelectedInMap.remove(competitorDAO);
            }
        } else if (itemSelected && !competitorsSelectedInMap.contains(competitorDAO)) {
            Marker lowlightedMarker = raceMap.data.boatMarkers.get(competitorDAO);
            if (lowlightedMarker != null) {
                Marker highlightedMarker = raceMap.createBoatMarker(competitorDAO, true);
                raceMap.map.removeOverlay(lowlightedMarker);
                raceMap.map.addOverlay(highlightedMarker);
                raceMap.data.boatMarkers.put(competitorDAO, highlightedMarker);
                int selectionIndex = quickRanksList.indexOf(competitorDAO);
                quickRanksBox.setItemSelected(selectionIndex, true);
            }
            // add the competitor even if not currently contained in map
            competitorsSelectedInMap.add(competitorDAO);
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        newRaceListBox.fillEvents(result);
        raceMap.selectedEventAndRace = newRaceListBox.getSelectedEventAndRace();
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        raceMap.mapFirstZoomDone = false;
        raceMap.mapZoomedOrPannedSinceLastRaceSelectionChange = false;
        if (!selectedRaces.isEmpty() && selectedRaces.get(selectedRaces.size() - 1) != null) {
            RaceDAO raceDAO = selectedRaces.get(selectedRaces.size() - 1).getC();
            if (raceDAO.startOfRace != null) {
                timePanel.timeChanged(raceDAO.startOfRace);
                timer.setTime(raceDAO.startOfRace.getTime());
            }
            updateSlider(raceDAO);
        }
        // force display of currently selected race
        refreshMapContents();
    }

    private void refreshMapContents() {
        timeChanged(timer.getTime());
    }

    private void updateSlider(RaceDAO selectedRace) {
        if (selectedRace.startOfTracking != null) {
            timePanel.setMin(selectedRace.startOfTracking);
        }
        if (selectedRace.timePointOfNewestEvent != null) {
            timePanel.setMax(selectedRace.timePointOfNewestEvent);
        }
    }

    @Override
    public void timeChanged(final Date date) {
        if (date != null) {
            List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = newRaceListBox.getSelectedEventAndRace();
            if (!selection.isEmpty()) {
                EventDAO event = selection.get(selection.size() - 1).getA();
                RaceDAO race = selection.get(selection.size() - 1).getC();
                if (event != null && race != null) {
                    final Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> fromAndToAndOverlap = raceMap.computeFromAndTo(date,this.getCompetitorsToShow());
                    final int requestID = boatPositionRequestIDCounter++;
                    sailingService.getBoatPositions(new EventNameAndRaceName(event.name, race.name),
                            fromAndToAndOverlap.getA(), fromAndToAndOverlap.getB(), true,
                            new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining boat positions: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                    // process response only if not received out of order
                                    if (startedProcessingRequestID < requestID) {
                                        startedProcessingRequestID = requestID;
                                        Date from = new Date(date.getTime() - raceMap.tailLengthInMilliSeconds);
                                        raceMap.updateFixes(result, fromAndToAndOverlap.getC());
                                        raceMap.showBoatsOnMap(from, date, getCompetitorsToShow(), competitorsSelectedInMap);
                                        if (raceMap.data.douglasMarkers != null) {
                                            raceMap.removeAllMarkDouglasPeuckerpoints();
                                        }
                                        if (raceMap.data.maneuverMarkers != null) {
                                            raceMap.removeAllManeuverMarkers();
                                        }
                                    }
                                }
                            });
                    sailingService.getMarkPositions(new EventNameAndRaceName(event.name, race.name), date,
                            new AsyncCallback<List<MarkDAO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error trying to obtain mark positions: "
                                            + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<MarkDAO> result) {
                                    raceMap.showMarksOnMap(result);
                                }
                            });
                    sailingService.getQuickRanks(new EventNameAndRaceName(event.name, race.name), date,
                            new AsyncCallback<List<QuickRankDAO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining quick rankings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<QuickRankDAO> result) {
                                    showQuickRanks(result);
                                }
                            });
                }
            }
        }
    }

    private void showQuickRanks(List<QuickRankDAO> result) {
        quickRanksBox.clear();
        quickRanksList.clear();
        int i = 0;
        for (QuickRankDAO quickRank : result) {
            quickRanksList.add(quickRank.competitor);
            quickRanksBox.addItem("" + quickRank.rank + ". " + quickRank.competitor.name + " ("
                    + quickRank.competitor.threeLetterIocCountryCode + ") in leg #" + (quickRank.legNumber + 1));
            // maintain previous selection, based on competitorsSelectedInMap
            if (competitorsSelectedInMap.contains(quickRank.competitor)) {
                quickRanksBox.setItemSelected(i, /* selected */true);
            }
            i++;
        }
    }

    private Collection<CompetitorDAO> getCompetitorsToShow() {
        if (showOnlySelected.getValue()) {
            return competitorsSelectedInMap;
        } else {
            // here the quickrankslist is emtpy, because of that te map is not zoomed correctly
            return quickRanksList;
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
    }

    private void setMapDisplayOptions()
    {
        raceMap.showManeuverTack = getCheckboxValueManeuver("TACK");
        raceMap.showManeuverJibe = getCheckboxValueManeuver("JIBE");
        raceMap.showManeuverHeadUp = getCheckboxValueManeuver("HEAD_UP");
        raceMap.showManeuverBearAway = getCheckboxValueManeuver("BEAR_AWAY");
        raceMap.showManeuverPenaltyCircle = getCheckboxValueManeuver("PENALTY_CIRCLE");
        raceMap.showManeuverMarkPassing = getCheckboxValueManeuver("MARK_PASSING");
        raceMap.showManeuverOther = getCheckboxValueManeuver("OTHER");
        
        raceMap.showDouglasPeuckerPoints = checkBoxDouglasPeuckerPoints.getValue();
    }
    
    private void showManeuvers(Map<CompetitorDAO, List<ManeuverDAO>> maneuvers) {
        setMapDisplayOptions();
        
        raceMap.showManeuvers(maneuvers);
    }
}
