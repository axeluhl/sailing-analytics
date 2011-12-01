package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.event.MapDragEndHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MapZoomEndHandler;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.event.MarkerMouseOutHandler;
import com.google.gwt.maps.client.event.MarkerMouseOverHandler;
import com.google.gwt.maps.client.event.PolylineClickHandler;
import com.google.gwt.maps.client.event.PolylineMouseOutHandler;
import com.google.gwt.maps.client.event.PolylineMouseOverHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
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
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.PositionDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class RaceMapPanel extends FormPanel implements EventDisplayer, TimeListener, ProvidesResize, RequiresResize,
        RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private MapWidget map;
    private final RacesListBoxPanel newRaceListBox;
    private final ListBox quickRanksBox;
    private final List<CompetitorDAO> quickRanksList;
    private final TimePanel timePanel;
    private Icon boatIcon;
    private Icon boatIconHighlighted;
    private Icon buoyIcon;
    private Icon tackToStarboardIcon;
    private Icon tackToPortIcon;
    private Icon jibeToStarboardIcon;
    private Icon jibeToPortIcon;
    private Icon markPassingToStarboardIcon;
    private Icon markPassingToPortIcon;
    private Icon headUpOnStarboardIcon;
    private Icon headUpOnPortIcon;
    private Icon bearAwayOnStarboardIcon;
    private Icon bearAwayOnPortIcon;
    private Icon unknownManeuverIcon;
    private Icon penaltyCircleToStarboardIcon;
    private Icon penaltyCircleToPortIcon;
    private LatLng lastMousePosition;
    private final Set<CompetitorDAO> competitorsSelectedInMap;
    private final Timer timer;
    private List<Pair<CheckBox, String>> checkboxAndType;

    private long TAILLENGTHINMILLISECONDS = 30000l;

    /**
     * If the user explicitly zoomed or panned the map, don't adjust zoom/pan unless a new race is selected
     */
    private boolean mapZoomedOrPannedSinceLastRaceSelectionChange = false;

    /**
     * Tails of competitors currently displayed as overlays on the map.
     */
    private final Map<CompetitorDAO, Polyline> tails;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the first fix shown
     * in {@link #tails} is .
     */
    private final Map<CompetitorDAO, Integer> firstShownFix;

    /**
     * Key set is equal to that of {@link #tails} and tells what the index in in {@link #fixes} of the last fix shown in
     * {@link #tails} is .
     */
    private final Map<CompetitorDAO, Integer> lastShownFix;

    /**
     * Fixes of each competitors tail. If a list is contained for a competitor, the list contains a timely "contiguous"
     * list of fixes for the competitor. This means the server has no more data for the time interval covered, unless
     * the last fix was {@link GPSFixDAO#extrapolated obtained by extrapolation}.
     */
    private final Map<CompetitorDAO, List<GPSFixDAO>> fixes;

    /**
     * Markers used as boat display on the map
     */
    private final Map<CompetitorDAO, Marker> boatMarkers;

    private final Map<MarkDAO, Marker> buoyMarkers;

    /**
     * markers displayed in response to {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    private Set<Marker> douglasMarkers;

    /**
     * markers displayed in response to {@link SailingServiceAsync#getDouglasPoints(String, String, Map, Map, double, AsyncCallback)}
     */
    private Set<Marker> maneuverMarkers;
    
    // key for domain web4sap.com
    private final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRRLCigyC_gRDASMpyomD2do5awpNhRCyD_q-27hwxKe_T6ivSZ_0NgbUg";

    private final CheckBox showOnlySelected;

    private final IntegerBox tailLengthBox;
    
    protected Map<CompetitorDAO, List<ManeuverDAO>> lastManeuverResult;

    public RaceMapPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            final EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = new Timer(/* delayBetweenAutoAdvancesInMilliseconds */3000);
        competitorsSelectedInMap = new HashSet<CompetitorDAO>();
        tails = new HashMap<CompetitorDAO, Polyline>();
        firstShownFix = new HashMap<CompetitorDAO, Integer>();
        lastShownFix = new HashMap<CompetitorDAO, Integer>();
        buoyMarkers = new HashMap<MarkDAO, Marker>();
        boatMarkers = new HashMap<CompetitorDAO, Marker>();
        checkboxAndType = new ArrayList<Pair<CheckBox,String>>();
        VerticalPanel verticalCheckBoxPanel = new VerticalPanel();
        verticalCheckBoxPanel.add(new Label(stringConstants.maneuverTypes()));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("HEAD_UP"), "HEAD_UP"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("BEAR_AWAY"), "BEAR_AWAY"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("TACK"), "TACK"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("JIBE"), "JIBE"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("PENALTY_CIRCLE"), "PENALTY_CIRCLE"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("MARK_PASSING"), "MARK_PASSING"));
        checkboxAndType.add(new Pair<CheckBox, String>(new CheckBox("OTHER"), "OTHER"));
        for (Pair<CheckBox, String> pair : checkboxAndType) {
            pair.getA().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    if(!timer.isPlaying() && lastManeuverResult!=null){
                        removeAllManeuverMarkers();
                        showManeuvers(lastManeuverResult);
                    }
                }
            });
            verticalCheckBoxPanel.add(pair.getA());
        }
        fixes = new HashMap<CompetitorDAO, List<GPSFixDAO>>();
        this.grid = new Grid(3, 2);
        setWidget(grid);
        grid.setSize("100%", "100%");
        grid.getColumnFormatter().setWidth(0, "20%");
        grid.getColumnFormatter().setWidth(1, "80%");
        grid.getCellFormatter().setHeight(2, 1, "100%");
        loadMapsAPI();
        newRaceListBox = new RacesListBoxPanel(eventRefresher, stringConstants);
        newRaceListBox.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, newRaceListBox);
        PositionDAO pos = new PositionDAO();
        if (!boatMarkers.isEmpty()) {
            LatLng latLng = boatMarkers.values().iterator().next().getLatLng();
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
        tailLengthBox.setValue((int) (TAILLENGTHINMILLISECONDS / 1000));
        tailLengthBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                TAILLENGTHINMILLISECONDS = 1000l * event.getValue();
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
        horizontalRanksVerticalAndCheckboxesManeuversPanel.add(verticalCheckBoxPanel);
        grid.setWidget(2, 0, horizontalRanksVerticalAndCheckboxesManeuversPanel);
        timePanel = new TimePanel(stringConstants, timer);
        timer.addTimeListener(this);
        timer.addTimeListener(windHistory);
        grid.setWidget(1, 1, timePanel);
    }
    
    private boolean getCheckboxValueManeuver(String maneuverType){
        for (Pair<CheckBox, String> pair : checkboxAndType) {
            if(pair.getB().equals(maneuverType)){
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
            Marker highlightedMarker = boatMarkers.get(competitorDAO);
            if (highlightedMarker != null) {
                Marker lowlightedMarker = createBoatMarker(competitorDAO, highlightedMarker.getLatLng().getLatitude(),
                        highlightedMarker.getLatLng().getLongitude(), /* highlighted */
                        false);
                map.removeOverlay(highlightedMarker);
                map.addOverlay(lowlightedMarker);
                boatMarkers.put(competitorDAO, lowlightedMarker);
                competitorsSelectedInMap.remove(competitorDAO);
            }
        } else if (itemSelected && !competitorsSelectedInMap.contains(competitorDAO)) {
            Marker lowlightedMarker = boatMarkers.get(competitorDAO);
            if (lowlightedMarker != null) {
                Marker highlightedMarker = createBoatMarker(competitorDAO, lowlightedMarker.getLatLng().getLatitude(),
                        lowlightedMarker.getLatLng().getLongitude(), /* highlighted */
                        true);
                map.removeOverlay(lowlightedMarker);
                map.addOverlay(highlightedMarker);
                boatMarkers.put(competitorDAO, highlightedMarker);
                int selectionIndex = quickRanksList.indexOf(competitorDAO);
                quickRanksBox.setItemSelected(selectionIndex, true);
            }
            // add the competitor even if not currently contained in map
            competitorsSelectedInMap.add(competitorDAO);
        }
    }

    private void loadMapsAPI() {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new LargeMapControl3D());
                map.addControl(new MenuMapTypeControl());
                // Add the map to the HTML host page
                grid.setWidget(2, 1, map);
                map.setSize("100%", "100%");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);
                map.addMapZoomEndHandler(new MapZoomEndHandler() {
                    @Override
                    public void onZoomEnd(MapZoomEndEvent event) {
                        mapZoomedOrPannedSinceLastRaceSelectionChange = true;
                    }
                });
                map.addMapDragEndHandler(new MapDragEndHandler() {
                    @Override
                    public void onDragEnd(MapDragEndEvent event) {
                        mapZoomedOrPannedSinceLastRaceSelectionChange = true;
                    }
                });
                map.addMapMouseMoveHandler(new MapMouseMoveHandler() {
                    @Override
                    public void onMouseMove(MapMouseMoveEvent event) {
                        lastMousePosition = event.getLatLng();
                    }
                });
                boatIcon = Icon.newInstance("/images/boat16.png");
                boatIcon.setIconAnchor(Point.newInstance(8, 8));
                boatIconHighlighted = Icon.newInstance("/images/boat-selected16.png");
                boatIconHighlighted.setIconAnchor(Point.newInstance(8, 8));
                buoyIcon = Icon.newInstance("/images/safe-water-small.png");
                buoyIcon.setIconAnchor(Point.newInstance(10, 19));
                tackToStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|00FF00|000000");
                tackToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                tackToPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|FF0000|000000");
                tackToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                jibeToStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|00FF00|000000");
                jibeToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                jibeToPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|FF0000|000000");
                jibeToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                headUpOnStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|00FF00|000000");
                headUpOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                headUpOnPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|FF0000|000000");
                headUpOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
                bearAwayOnStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|00FF00|000000");
                bearAwayOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                bearAwayOnPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|FF0000|000000");
                bearAwayOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
                markPassingToStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|00FF00|000000");
                markPassingToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                markPassingToPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|FF0000|000000");
                markPassingToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                unknownManeuverIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000");
                unknownManeuverIcon.setIconAnchor(Point.newInstance(10, 33));
                penaltyCircleToStarboardIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|00FF00|000000");
                penaltyCircleToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                penaltyCircleToPortIcon = Icon.newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|FF0000|000000");
                penaltyCircleToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            }
        });
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        newRaceListBox.fillEvents(result);
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        mapZoomedOrPannedSinceLastRaceSelectionChange = false;
        if (!selectedRaces.isEmpty() && selectedRaces.get(selectedRaces.size() - 1) != null) {
            RaceDAO raceDAO = selectedRaces.get(selectedRaces.size() - 1).getC();
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
                    final Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> fromAndToAndOverlap = computeFromAndTo(date);
                    sailingService.getBoatPositions(new EventNameAndRaceName(event.name, race.name), fromAndToAndOverlap.getA(),
                            fromAndToAndOverlap.getB(), true, new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Error obtaining boat positions: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                    Date from = new Date(date.getTime() - TAILLENGTHINMILLISECONDS);
                                    updateFixes(result, fromAndToAndOverlap.getC());
                                    showBoatsOnMap(from, date);
                                    if (douglasMarkers != null) {
                                        removeAllMarkDouglasPeuckerpoints();
                                    }
                                    if (maneuverMarkers != null) {
                                        removeAllManeuverMarkers();
                                    }
                                }
                            });
                    sailingService.getMarkPositions(new EventNameAndRaceName(event.name, race.name), date, new AsyncCallback<List<MarkDAO>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Error trying to obtain mark positions: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(List<MarkDAO> result) {
                            showMarksOnMap(result);
                        }
                    });
                    sailingService.getQuickRanks(new EventNameAndRaceName(event.name, race.name), date, new AsyncCallback<List<QuickRankDAO>>() {
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

    /**
     * From {@link #fixes} as well as the selection of {@link #getCompetitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS fixes from the server. No update is performed here to {@link #fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all fixes known by the server for that competitor starting
     * at <code>upTo-{@link #TAILLENGTHINMILLISECONDS}</code> and ending at <code>upTo</code> (exclusive).
     * 
     * @return a triple whose {@link Triple#getA() first} component contains the "from", and whose {@link Triple#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show; the
     *         {@link Triple#getC() third} component tells whether the existing fixes can remain and be augmented by
     *         those requested or need to be replaced
     */
    private Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> computeFromAndTo(
            Date upTo) {
        Date tailStart = new Date(upTo.getTime() - TAILLENGTHINMILLISECONDS);
        Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Boolean> overlapWithKnownFixes = new HashMap<CompetitorDAO, Boolean>();
        for (CompetitorDAO competitor : getCompetitorsToShow()) {
            List<GPSFixDAO> fixesForCompetitor = fixes.get(competitor);
            Date fromDate;
            Date toDate;
            Date timepointOfLastKnownFix = fixesForCompetitor == null ? null
                    : getTimepointOfLastNonExtrapolated(fixesForCompetitor);
            Date timepointOfFirstKnownFix = fixesForCompetitor == null ? null
                    : getTimepointOfFirstNonExtrapolated(fixesForCompetitor);
            boolean overlap = false;
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !tailStart.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !tailStart.after(timepointOfLastKnownFix)) {
                // the beginning of what we need is contained in the interval we already have; skip what we already have
                fromDate = timepointOfLastKnownFix;
                overlap = true;
            } else {
                fromDate = tailStart;
            }
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !upTo.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !upTo.after(timepointOfLastKnownFix)) {
                // the end of what we need is contained in the interval we already have; skip what we already have
                toDate = timepointOfFirstKnownFix;
                overlap = true;
            } else {
                toDate = upTo;
            }
            // only request something for the competitor if we're missing information at all
            if (fromDate.before(toDate) || fromDate.equals(toDate)) {
                from.put(competitor, fromDate);
                to.put(competitor, toDate);
                overlapWithKnownFixes.put(competitor, overlap);
            }
        }
        return new Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>>(from, to,
                overlapWithKnownFixes);
    }

    private Date getTimepointOfFirstNonExtrapolated(List<GPSFixDAO> fixesForCompetitor) {
        for (GPSFixDAO fix : fixesForCompetitor) {
            if (!fix.extrapolated) {
                return fix.timepoint;
            }
        }
        return null;
    }

    private Date getTimepointOfLastNonExtrapolated(List<GPSFixDAO> fixesForCompetitor) {
        if (!fixesForCompetitor.isEmpty()) {
            for (ListIterator<GPSFixDAO> fixIter = fixesForCompetitor.listIterator(fixesForCompetitor.size() - 1); fixIter
                    .hasPrevious();) {
                GPSFixDAO fix = fixIter.previous();
                if (!fix.extrapolated) {
                    return fix.timepoint;
                }
            }
        }
        return null;
    }

    /**
     * Adds the fixes received in <code>result</code> to {@link #fixes} and ensures they are still contiguous for each
     * competitor. If <code>overlapsWithKnownFixes</code> indicates that the fixes received in <code>result</code>
     * overlap with those already known, the fixes are merged into the list of already known fixes for the competitor.
     * Otherwise, the fixes received in <code>result</code> replace those known so far for the respective competitor.
     */
    private void updateFixes(Map<CompetitorDAO, List<GPSFixDAO>> result,
            Map<CompetitorDAO, Boolean> overlapsWithKnownFixes) {
        for (Map.Entry<CompetitorDAO, List<GPSFixDAO>> e : result.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDAO> fixesForCompetitor = fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    fixes.put(e.getKey(), fixesForCompetitor);
                }
                if (!overlapsWithKnownFixes.get(e.getKey())) {
                    fixesForCompetitor.clear();
                    // to re-establish the invariants for tails, firstShownFix and lastShownFix, we now need to remove
                    // all
                    // points from the competitor's polyline and clear the entries in firstShownFix and lastShownFix
                    if (map != null && tails.containsKey(e.getKey())) {
                        map.removeOverlay(tails.remove(e.getKey()));
                    }
                    firstShownFix.remove(e.getKey());
                    lastShownFix.remove(e.getKey());
                    fixesForCompetitor.addAll(e.getValue());
                } else {
                    mergeFixes(e.getKey(), e.getValue());
                }
            }
        }
    }

    /**
     * While updating the {@link #fixes} for <code>competitorDAO</code>, the invariants for {@link #tails} and
     * {@link #firstShownFix} and {@link #lastShownFix} are maintained: each time a fix is inserted, the
     * {@link #firstShownFix}/{@link #lastShownFix} records for <code>competitorDAO</code> are incremented if they are
     * greater or equal to the insertion index and we have a tail in {@link #tails} for <code>competitorDAO</code>.
     * Additionally, if the fix is in between the fixes shown in the competitor's tail, the tail is adjusted by
     * inserting the corresponding fix.
     */
    private void mergeFixes(CompetitorDAO competitorDAO, List<GPSFixDAO> mergeThis) {
        List<GPSFixDAO> intoThis = fixes.get(competitorDAO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDAO) == null ? -1 : firstShownFix.get(competitorDAO);
        int indexOfLastShownFix = lastShownFix.get(competitorDAO) == null ? -1 : lastShownFix.get(competitorDAO);
        Polyline tail = tails.get(competitorDAO);
        int intoThisIndex = 0;
        for (GPSFixDAO mergeThisFix : mergeThis) {
            while (intoThisIndex < intoThis.size()
                    && intoThis.get(intoThisIndex).timepoint.before(mergeThisFix.timepoint)) {
                intoThisIndex++;
            }
            if (intoThisIndex < intoThis.size() && intoThis.get(intoThisIndex).timepoint.equals(mergeThisFix.timepoint)) {
                // exactly same time point; replace with fix from mergeThis
                intoThis.set(intoThisIndex, mergeThisFix);
            } else {
                intoThis.add(intoThisIndex, mergeThisFix);
                if (indexOfFirstShownFix >= intoThisIndex) {
                    indexOfFirstShownFix++;
                }
                if (indexOfLastShownFix >= intoThisIndex) {
                    indexOfLastShownFix++;
                }
                if (tail != null && intoThisIndex >= indexOfFirstShownFix && intoThisIndex <= indexOfLastShownFix) {
                    tail.insertVertex(intoThisIndex - indexOfFirstShownFix,
                            LatLng.newInstance(mergeThisFix.position.latDeg, mergeThisFix.position.lngDeg));
                }
            }
            intoThisIndex++;
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

    private void showMarksOnMap(List<MarkDAO> result) {
        if (map != null) {
            Set<MarkDAO> toRemove = new HashSet<MarkDAO>(buoyMarkers.keySet());
            for (MarkDAO markDAO : result) {
                Marker buoyMarker = buoyMarkers.get(markDAO);
                if (buoyMarker == null) {
                    buoyMarker = createBuoyMarker(markDAO);
                    buoyMarkers.put(markDAO, buoyMarker);
                    map.addOverlay(buoyMarker);
                } else {
                    buoyMarker.setLatLng(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg));
                    toRemove.remove(markDAO);
                }
            }
            for (MarkDAO toRemoveMarkDAO : toRemove) {
                Marker marker = buoyMarkers.remove(toRemoveMarkDAO);
                map.removeOverlay(marker);
            }
        }
    }

    /**
     * @param from
     *            time point for first fix to show in tails
     * @param to
     *            time point for last fix to show in tails
     */
    private void showBoatsOnMap(Date from, Date to) {
        if (map != null) {
            LatLngBounds newMapBounds = null;
            Set<CompetitorDAO> competitorDAOsOfUnusedTails = new HashSet<CompetitorDAO>(tails.keySet());
            Set<CompetitorDAO> competitorDAOsOfUnusedMarkers = new HashSet<CompetitorDAO>(boatMarkers.keySet());
            for (CompetitorDAO competitorDAO : getCompetitorsToShow()) {
                if (fixes.containsKey(competitorDAO)) {
                    Polyline tail = tails.get(competitorDAO);
                    if (tail == null) {
                        tail = createTailAndUpdateIndices(competitorDAO, from, to);
                        map.addOverlay(tail);
                    } else {
                        updateTail(tail, competitorDAO, from, to);
                        competitorDAOsOfUnusedTails.remove(competitorDAO);
                    }
                    LatLngBounds bounds = tail.getBounds();
                    if (newMapBounds == null) {
                        newMapBounds = bounds;
                    } else {
                        newMapBounds.extend(bounds.getNorthEast());
                        newMapBounds.extend(bounds.getSouthWest());
                    }
                    if (lastShownFix.containsKey(competitorDAO)) {
                        GPSFixDAO lastPos = fixes.get(competitorDAO).get(lastShownFix.get(competitorDAO));
                        Marker boatMarker = boatMarkers.get(competitorDAO);
                        if (boatMarker == null) {
                            boatMarker = createBoatMarker(competitorDAO, lastPos.position.latDeg,
                                    lastPos.position.lngDeg, false);
                            map.addOverlay(boatMarker);
                            boatMarkers.put(competitorDAO, boatMarker);
                        } else {
                            competitorDAOsOfUnusedMarkers.remove(competitorDAO);
                            boatMarker.setLatLng(LatLng.newInstance(lastPos.position.latDeg, lastPos.position.lngDeg));
                        }
                    }
                }
            }
            if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                map.setCenter(newMapBounds.getCenter());
            }
            for (CompetitorDAO unusedMarkerCompetitorDAO : competitorDAOsOfUnusedMarkers) {
                map.removeOverlay(boatMarkers.remove(unusedMarkerCompetitorDAO));
            }
            for (CompetitorDAO unusedTailCompetitorDAO : competitorDAOsOfUnusedTails) {
                map.removeOverlay(tails.remove(unusedTailCompetitorDAO));
            }
        }
    }

    private Collection<CompetitorDAO> getCompetitorsToShow() {
        if (showOnlySelected.getValue()) {
            return competitorsSelectedInMap;
        } else {
            return quickRanksList;
        }
    }

    /**
     * If the tail starts before <code>from</code>, removes leading vertices from <code>tail</code> that are before
     * <code>from</code>. This is determined by using the {@link #firstShownFix} index which tells us where in
     * {@link #fixes} we find the sequence of fixes currently represented in the tail.
     * <p>
     * 
     * If the tail starts after <code>from</code>, vertices for those {@link #fixes} for <code>competitorDAO</code> at
     * or after time point <code>from</code> and before the time point of the first fix displayed so far in the tail and
     * before <code>to</code> are prepended to the tail.
     * <p>
     * 
     * Now to the end of the tail: if the existing tail's end exceeds <code>to</code>, the vertices in excess are
     * removed (aided by {@link #lastShownFix}). Otherwise, for the competitor's fixes starting at the tail's end up to
     * <code>to</code> are appended to the tail.
     * <p>
     * 
     * When this method returns, {@link #firstShownFix} and {@link #lastShownFix} have been updated accordingly.
     */
    private void updateTail(Polyline tail, CompetitorDAO competitorDAO, Date from, Date to) {
        List<GPSFixDAO> fixesForCompetitor = fixes.get(competitorDAO);
        int indexOfFirstShownFix = firstShownFix.get(competitorDAO) == null ? -1 : firstShownFix.get(competitorDAO);
        while (indexOfFirstShownFix != -1 && tail.getVertexCount() > 0
                && fixesForCompetitor.get(indexOfFirstShownFix).timepoint.before(from)) {
            tail.deleteVertex(0);
            indexOfFirstShownFix++;
        }
        // now the polyline contains no more vertices representing fixes before "from";
        // go back in time starting at indexOfFirstShownFix while the fixes are still at or after "from"
        // and insert corresponding vertices into the polyline
        while (indexOfFirstShownFix > 0 && !fixesForCompetitor.get(indexOfFirstShownFix - 1).timepoint.before(from)) {
            indexOfFirstShownFix--;
            GPSFixDAO fix = fixesForCompetitor.get(indexOfFirstShownFix);
            tail.insertVertex(0, LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
        }
        // now adjust the polylines tail: remove excess vertices that are after "to"
        int indexOfLastShownFix = lastShownFix.get(competitorDAO) == null ? -1 : lastShownFix.get(competitorDAO);
        while (indexOfLastShownFix != -1 && tail.getVertexCount() > 0
                && fixesForCompetitor.get(indexOfLastShownFix).timepoint.after(to)) {
            tail.deleteVertex(tail.getVertexCount() - 1);
            indexOfLastShownFix--;
        }
        // now the polyline contains no more vertices representing fixes after "to";
        // go forward in time starting at indexOfLastShownFix while the fixes are still at or before "to"
        // and insert corresponding vertices into the polyline
        while (indexOfLastShownFix < fixesForCompetitor.size() - 1
                && !fixesForCompetitor.get(indexOfLastShownFix + 1).timepoint.after(to)) {
            indexOfLastShownFix++;
            GPSFixDAO fix = fixesForCompetitor.get(indexOfLastShownFix);
            tail.insertVertex(tail.getVertexCount(), LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg));
        }
        firstShownFix.put(competitorDAO, indexOfFirstShownFix);
        lastShownFix.put(competitorDAO, indexOfLastShownFix);
    }

    private Marker createBuoyMarker(final MarkDAO markDAO) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (buoyIcon != null) {
            options.setIcon(buoyIcon);
        }
        options.setTitle(markDAO.name);
        final Marker buoyMarker = new Marker(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg),
                options);
        buoyMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = buoyMarker.getLatLng();
                showMarkInfoWindow(markDAO, latlng);
            }
        });
        return buoyMarker;
    }

    private Marker createBoatMarker(final CompetitorDAO competitorDAO, double latDeg, double lngDeg, boolean highlighted) {
        MarkerOptions options = MarkerOptions.newInstance();
        if (highlighted) {
            if (boatIconHighlighted != null) {
                options.setIcon(boatIconHighlighted);
            }
        } else {
            if (boatIcon != null) {
                options.setIcon(boatIcon);
            }
        }
        options.setTitle(competitorDAO.name);
        final Marker boatMarker = new Marker(LatLng.newInstance(latDeg, lngDeg), options);
        boatMarker.addMarkerClickHandler(new MarkerClickHandler() {
            @Override
            public void onClick(MarkerClickEvent event) {
                LatLng latlng = boatMarker.getLatLng();
                showCompetitorInfoWindow(competitorDAO, latlng);
            }
        });
        boatMarker.addMarkerMouseOverHandler(new MarkerMouseOverHandler() {
            @Override
            public void onMouseOver(MarkerMouseOverEvent event) {
                map.setTitle(competitorDAO.name);
                // setSelectedInMap(competitorDAO, true);
                // quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), true);
            }
        });
        boatMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {
            @Override
            public void onMouseOut(MarkerMouseOutEvent event) {
                map.setTitle("");
                /*
                 * if(!quickRanksBox.isItemSelected(quickRanksList.indexOf(competitorDAO))){
                 * setSelectedInMap(competitorDAO, false); }
                 */
                // quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), false);
            }
        });
        return boatMarker;
    }

    private void showMarkInfoWindow(MarkDAO markDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDAO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDAO competitorDAO, LatLng where) {
        GPSFixDAO latestFixForCompetitor = fixes.get(competitorDAO).get(lastShownFix.get(competitorDAO));
        map.getInfoWindow().open(where,
                new InfoWindowContent(getInfoWindowContent(competitorDAO, latestFixForCompetitor)));
    }

    private Widget getInfoWindowContent(MarkDAO markDAO) {
        VerticalPanel result = new VerticalPanel();
        result.add(new Label("Mark " + markDAO.name));
        result.add(new Label("" + markDAO.position));
        return result;
    }

    private Widget getInfoWindowContent(CompetitorDAO competitorDAO, GPSFixDAO lastFix) {
        final VerticalPanel result = new VerticalPanel();
        result.add(new Label("Competitor " + competitorDAO.name));
        result.add(new Label("" + lastFix.position));
        result.add(new Label(lastFix.speedWithBearing.speedInKnots + "kts " + lastFix.speedWithBearing.bearingInDegrees
                + "deg"));
        result.add(new Label("Tack: " + lastFix.tack));
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = newRaceListBox.getSelectedEventAndRace();
        if (!selection.isEmpty()) {
            EventDAO event = selection.get(selection.size() - 1).getA();
            RaceDAO race = selection.get(selection.size() - 1).getC();
            if (event != null && race != null) {
                Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
                from.put(competitorDAO, fixes.get(competitorDAO).get(firstShownFix.get(competitorDAO)).timepoint);
                Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
                to.put(competitorDAO, fixes.get(competitorDAO).get(lastShownFix.get(competitorDAO)).timepoint);
                /* currently not showing Douglas-Peucker points; TODO use checkboxes to select what to show (Bug #6)
                sailingService.getDouglasPoints(event.name, race.name, from, to, 3, // epsilon/meters
                        new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                if (douglasMarkers != null) {
                                    removeAllMarkDouglasPeuckerpoints();
                                }
                                if (!timer.isPlaying()) {
                                    showMarkDouglasPeuckerPoints(result);
                                }
                            }
                        });
                 */
                sailingService.getManeuvers(new EventNameAndRaceName(event.name, race.name), from, to,
                        new AsyncCallback<Map<CompetitorDAO, List<ManeuverDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining maneuvers: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<ManeuverDAO>> result) {
                                RaceMapPanel.this.lastManeuverResult = result;
                                if (maneuverMarkers != null) {
                                    removeAllManeuverMarkers();
                                }
                                if (!timer.isPlaying()) {
                                    showManeuvers(result);
                                }
                            }
                        });
            }
        }
        return result;
    }

    private String getColorString(CompetitorDAO competitorDAO) {
        // TODO green no more than 70, red no less than 120
        return "#" + Integer.toHexString(competitorDAO.hashCode()).substring(0, 4).toUpperCase() + "00";
    }

    /**
     * Creates a polyline for the competitor represented by <code>competitorDAO</code>, taking the fixes from
     * {@link #fixes fixes.get(competitorDAO)} and using the fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #lastShownFix}, {@link #firstShownFix} and {@link #tails}.
     */
    private Polyline createTailAndUpdateIndices(final CompetitorDAO competitorDAO, Date from, Date to) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDAO> fixesForCompetitor = fixes.get(competitorDAO);
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        for (Iterator<GPSFixDAO> fixIter = fixesForCompetitor.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDAO fix = fixIter.next();
            if (!fix.timepoint.before(to)) {
                indexOfLast = i;
            } else {
                LatLng point = null;
                if (indexOfFirst == -1) {
                    if (!fix.timepoint.before(from)) {
                        indexOfFirst = i;
                        point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                    }
                } else {
                    point = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                }
                if (point != null) {
                    points.add(point);
                }
            }
            i++;
        }
        if (indexOfLast == -1) {
            indexOfLast = i - 1;
        }
        if (indexOfFirst != -1 && indexOfLast != -1) {
            firstShownFix.put(competitorDAO, indexOfFirst);
            lastShownFix.put(competitorDAO, indexOfLast);
        }
        PolylineOptions options = PolylineOptions.newInstance(
        /* clickable */true, /* geodesic */true);
        Polyline result = new Polyline(points.toArray(new LatLng[0]), getColorString(competitorDAO), /* width */3,
        /* opacity */0.5, options);
        result.addPolylineClickHandler(new PolylineClickHandler() {
            @Override
            public void onClick(PolylineClickEvent event) {
                showCompetitorInfoWindow(competitorDAO, lastMousePosition);
            }
        });
        result.addPolylineMouseOverHandler(new PolylineMouseOverHandler() {
            @Override
            public void onMouseOver(PolylineMouseOverEvent event) {
                map.setTitle(competitorDAO.name);
                /*
                 * if(quickRanksBox.isItemSelected(quickRanksList.indexOf(competitorDAO))){
                 * setSelectedInMap(competitorDAO, true); }
                 */
                // quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), true);
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
                /*
                 * if(!quickRanksBox.isItemSelected(quickRanksList.indexOf(competitorDAO))){
                 * setSelectedInMap(competitorDAO, false); }
                 */
                // quickRanksBox.setItemSelected(quickRanksList.indexOf(competitorDAO), false);
            }
        });
        tails.put(competitorDAO, result);
        return result;
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
        if (this.map != null) {
            this.map.onResize();
        }
    }

    private void removeAllMarkDouglasPeuckerpoints() {
        for (Marker marker : douglasMarkers) {
            map.removeOverlay(marker);
        }
        douglasMarkers = null;
    }

    private void removeAllManeuverMarkers() {
        if (maneuverMarkers != null) {
            for (Marker marker : maneuverMarkers) {
                map.removeOverlay(marker);
            }
            maneuverMarkers = null;
        }
    }

    /* TODO see Bug #6, use checkboxes to select what to visualize
    private void showMarkDouglasPeuckerPoints(
            Map<CompetitorDAO, List<GPSFixDAO>> gpsFixPointMapForCompetitors) {
        douglasMarkers = new HashSet<Marker>();
        if (map != null && gpsFixPointMapForCompetitors != null) {
            Set<CompetitorDAO> keySet = gpsFixPointMapForCompetitors.keySet();
            Iterator<CompetitorDAO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDAO competitorDAO = iter.next();
                List<GPSFixDAO> gpsFix = gpsFixPointMapForCompetitors.get(competitorDAO);
                for (GPSFixDAO fix : gpsFix) {
                    LatLng latLng = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                    MarkerOptions options = MarkerOptions.newInstance();
                    options.setTitle(fix.speedWithBearing.toString());
                    Marker marker = new Marker(latLng, options);
                    douglasMarkers.add(marker);
                    map.addOverlay(marker);
                }
            }
        }
    }
    */

    private void showManeuvers(Map<CompetitorDAO, List<ManeuverDAO>> maneuvers) {
        maneuverMarkers = new HashSet<Marker>();
        if (map != null && maneuvers != null) {
            Set<CompetitorDAO> keySet = maneuvers.keySet();
            Iterator<CompetitorDAO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDAO competitorDAO = iter.next();
                List<ManeuverDAO> maneuversForCompetitor = maneuvers.get(competitorDAO);
                for (ManeuverDAO maneuver : maneuversForCompetitor) {
                    boolean showThisManeuver = true;
                    LatLng latLng = LatLng.newInstance(maneuver.position.latDeg, maneuver.position.lngDeg);
                    MarkerOptions options = MarkerOptions.newInstance();
                    options.setTitle("" + maneuver.timepoint + ": " + maneuver.type + " "
                            + maneuver.directionChangeInDegrees + "deg from " + maneuver.speedWithBearingBefore
                            + " to " + maneuver.speedWithBearingAfter);
                    if (maneuver.type.equals("TACK") && getCheckboxValueManeuver("TACK")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(tackToPortIcon);
                        } else {
                            options.setIcon(tackToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("JIBE") && getCheckboxValueManeuver("JIBE")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(jibeToPortIcon);
                        } else {
                            options.setIcon(jibeToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("HEAD_UP") && getCheckboxValueManeuver("HEAD_UP")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(headUpOnPortIcon);
                        } else {
                            options.setIcon(headUpOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("BEAR_AWAY") && getCheckboxValueManeuver("BEAR_AWAY")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(bearAwayOnPortIcon);
                        } else {
                            options.setIcon(bearAwayOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("PENALTY_CIRCLE") && getCheckboxValueManeuver("PENALTY_CIRCLE")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(penaltyCircleToPortIcon);
                        } else {
                            options.setIcon(penaltyCircleToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("MARK_PASSING") && getCheckboxValueManeuver("MARK_PASSING")) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(markPassingToPortIcon);
                        } else {
                            options.setIcon(markPassingToStarboardIcon);
                        }
                    } else {
                        if(getCheckboxValueManeuver("OTHER")){
                            options.setIcon(unknownManeuverIcon);
                        }else{
                            showThisManeuver = false;
                        }
                    }
                    if(showThisManeuver){
                        Marker marker = new Marker(latLng, options);
                        maneuverMarkers.add(marker);
                        map.addOverlay(marker);
                    }
                }
            }
        }
        
    }

}
