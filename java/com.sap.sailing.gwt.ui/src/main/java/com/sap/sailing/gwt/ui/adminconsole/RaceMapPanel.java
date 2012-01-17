package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventDisplayer;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.leaderboard.SmallWindHistoryPanel;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final CompetitorSelectionProvider competitorSelectionModel;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final RacesListBoxPanel raceListBox;
    private final TimePanel timePanel;

    private final Timer timer;
    private List<Pair<CheckBox, String>> checkboxAndType;
    private CheckBox checkBoxDouglasPeuckerPoints;
    private final CheckBox showOnlySelectedCompetitors;
    private final IntegerBox tailLengthBox;

    private final RaceMap raceMap;
    private final QuickRanksListBoxComposite quickRanksListBox;
    
    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringMessages stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.competitorSelectionModel = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        this.timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */500);
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
                    if (!timer.isPlaying() && raceMap.lastManeuverResult != null) {
                        raceMap.removeAllManeuverMarkers();
                        showManeuvers(raceMap.lastManeuverResult);
                    }
                }
            });
            verticalCheckBoxPanel.add(pair.getA());
        }

        checkBoxDouglasPeuckerPoints = new CheckBox(stringConstants.douglasPeuckerPoints());
        checkBoxDouglasPeuckerPoints.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (!timer.isPlaying() && raceMap.lastDouglasPeuckerResult != null && event.getValue()) {
                    raceMap.getSettings().setShowDouglasPeuckerPoints(true);
                    raceMap.removeAllMarkDouglasPeuckerpoints();
                    raceMap.showMarkDouglasPeuckerPoints(raceMap.lastDouglasPeuckerResult);
                } else if (!event.getValue()) {
                    raceMap.getSettings().setShowDouglasPeuckerPoints(false);
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
        AbsolutePanel mapPanel = new AbsolutePanel();
        mapPanel.setSize("100%", "100%");
        grid.setWidget(2, 1, mapPanel);
        raceMap = new RaceMap(sailingService, errorReporter, timer, competitorSelectionModel);
        raceMap.loadMapsAPI(mapPanel);

        setMapDisplayOptions();

        raceListBox = new RacesListBoxPanel(eventRefresher, stringConstants);
        raceListBox.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, raceListBox);
        PositionDAO pos = new PositionDAO();
        if (!raceMap.boatMarkers.isEmpty()) {
            LatLng latLng = raceMap.boatMarkers.values().iterator().next().getLatLng();
            pos.latDeg = latLng.getLatitude();
            pos.lngDeg = latLng.getLongitude();
        }
        SmallWindHistoryPanel windHistory = new SmallWindHistoryPanel(sailingService, pos,
        /* number of wind displays */5,
        /* time interval between displays in milliseconds */5000, stringConstants, errorReporter);
        raceListBox.addRaceSelectionChangeListener(windHistory);
        grid.setWidget(1, 0, windHistory);
        HorizontalPanel horizontalRanksVerticalAndCheckboxesManeuversPanel = new HorizontalPanel();
        horizontalRanksVerticalAndCheckboxesManeuversPanel.setSpacing(15);
        VerticalPanel ranksAndCheckboxAndTailLengthPanel = new VerticalPanel();
        HorizontalPanel labelAndTailLengthBoxPanel = new HorizontalPanel();
        labelAndTailLengthBoxPanel.add(new Label(stringConstants.tailLength()));
        tailLengthBox = new IntegerBox();
        tailLengthBox.setValue((int) (raceMap.getSettings().getTailLengthInMilliseconds() / 1000));
        tailLengthBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                raceMap.getSettings().setTailLengthInMilliseconds(1000l * event.getValue());
                raceMap.redraw();
            }
        });
        labelAndTailLengthBoxPanel.add(tailLengthBox);
        ranksAndCheckboxAndTailLengthPanel.add(labelAndTailLengthBoxPanel);
        showOnlySelectedCompetitors = new CheckBox(stringConstants.showOnlySelected());
        showOnlySelectedCompetitors.setValue(raceMap.getSettings().isShowOnlySelectedCompetitors());
        showOnlySelectedCompetitors.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                raceMap.getSettings().setShowOnlySelectedCompetitors(event.getValue());
                raceMap.redraw();
            }
        });
        ranksAndCheckboxAndTailLengthPanel.add(showOnlySelectedCompetitors);
        
        quickRanksListBox = new QuickRanksListBoxComposite(competitorSelectionModel);
        quickRanksListBox.getListBox().setVisibleItemCount(20);

        ranksAndCheckboxAndTailLengthPanel.add(quickRanksListBox);

        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(ranksAndCheckboxAndTailLengthPanel);

        VerticalPanel verticalPanelRadioAndCheckboxes = new VerticalPanel();
        verticalPanelRadioAndCheckboxes.add(verticalCheckBoxPanel);
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(verticalPanelRadioAndCheckboxes);

        grid.setWidget(2, 0, horizontalRanksVerticalAndCheckboxesManeuversPanel);
        timePanel = new TimePanel(stringConstants, timer);
        timer.addTimeListener(raceMap);
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

    @Override
    public void fillEvents(List<EventDAO> result) {
        raceListBox.fillEvents(result);
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (!selectedRaces.isEmpty() && selectedRaces.get(selectedRaces.size() - 1) != null) {
            RaceDAO raceDAO = selectedRaces.get(selectedRaces.size() - 1).getC();
            competitorSelectionModel.setCompetitors(raceDAO.competitors);
            if (raceDAO.startOfRace != null) {
                timePanel.timeChanged(raceDAO.startOfRace);
                timer.setTime(raceDAO.startOfRace.getTime());
            }
            updateSlider(raceDAO);
        }
        raceMap.onRaceSelectionChange(selectedRaces);
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
            List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = raceListBox.getSelectedEventAndRace();
            if (!selection.isEmpty()) {
                EventDAO event = selection.get(selection.size() - 1).getA();
                RaceDAO race = selection.get(selection.size() - 1).getC();
                if (event != null && race != null) {
                    sailingService.getQuickRanks(new EventNameAndRaceName(event.name, race.name), date,
                            new AsyncCallback<List<QuickRankDAO>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining quick rankings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(List<QuickRankDAO> result) {
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
    }

    private void setMapDisplayOptions() {
        RaceMapSettings settings = raceMap.getSettings();
        settings.setShowManeuverTack(getCheckboxValueManeuver("TACK"));
        settings.setShowManeuverJibe(getCheckboxValueManeuver("JIBE"));
        settings.setShowManeuverHeadUp(getCheckboxValueManeuver("HEAD_UP"));
        settings.setShowManeuverBearAway(getCheckboxValueManeuver("BEAR_AWAY"));
        settings.setShowManeuverPenaltyCircle(getCheckboxValueManeuver("PENALTY_CIRCLE"));
        settings.setShowManeuverMarkPassing(getCheckboxValueManeuver("MARK_PASSING"));
        settings.setShowManeuverOther(getCheckboxValueManeuver("OTHER"));
        settings.setShowDouglasPeuckerPoints(checkBoxDouglasPeuckerPoints.getValue());
    }
    
    private void showManeuvers(Map<CompetitorDAO, List<ManeuverDAO>> maneuvers) {
        setMapDisplayOptions();
        raceMap.showManeuvers(maneuvers);
    }
}
