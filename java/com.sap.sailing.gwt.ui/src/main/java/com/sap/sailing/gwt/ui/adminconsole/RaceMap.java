package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapType;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl3D;
import com.google.gwt.maps.client.control.MenuMapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
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
import com.google.gwt.maps.client.geom.Projection;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.maps.client.overlay.Polyline;
import com.google.gwt.maps.client.overlay.PolylineOptions;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Util.Triple;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class RaceMap {
    protected MapWidget map;

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    /**
     * Two sails on downwind leg, wind from port (sails on starboard); no highlighting
     */
    protected ImageRotator boatIconDownwindPortRotator;

    /**
     * Two sails on downwind leg, wind from port (sails on starboard); with highlighting
     */
    protected ImageRotator boatIconHighlightedDownwindPortRotator;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); no highlighting
     */
    protected ImageRotator boatIconDownwindStarboardRotator;

    /**
     * Two sails on downwind leg, wind from starboard (sails on port); with highlighting
     */
    protected ImageRotator boatIconHighlightedDownwindStarboardRotator;

    /**
     * One sail, wind from port (sails on starboard); no highlighting
     */
    protected ImageRotator boatIconPortRotator;

    /**
     * One sail, wind from port (sails on starboard); with highlighting
     */
    protected ImageRotator boatIconHighlightedPortRotator;

    /**
     * One sail, wind from starboard (sails on port); no highlighting
     */
    protected ImageRotator boatIconStarboardRotator;

    /**
     * One sail, wind from starboard (sails on port); with highlighting
     */
    protected ImageRotator boatIconHighlightedStarboardRotator;

    protected Icon buoyIcon;
    protected Icon tackToStarboardIcon;
    protected Icon tackToPortIcon;
    protected Icon jibeToStarboardIcon;
    protected Icon jibeToPortIcon;
    protected Icon markPassingToStarboardIcon;
    protected Icon markPassingToPortIcon;
    protected Icon headUpOnStarboardIcon;
    protected Icon headUpOnPortIcon;
    protected Icon bearAwayOnStarboardIcon;
    protected Icon bearAwayOnPortIcon;
    protected Icon unknownManeuverIcon;
    protected Icon penaltyCircleToStarboardIcon;
    protected Icon penaltyCircleToPortIcon;
    protected LatLng lastMousePosition;

    protected long tailLengthInMilliSeconds = 30000l;

    protected List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedEventAndRace;

    /**
     * If the user explicitly zoomed or panned the map, don't adjust zoom/pan unless a new race is selected
     */
    protected boolean mapZoomedOrPannedSinceLastRaceSelectionChange = false;

    /**
     * Used to check if the first initial zoom to the buoy markers was already done.
     */
    protected boolean mapFirstZoomDone = false;


    // key for domain web4sap.com
    protected final String mapsAPIKey = "ABQIAAAAmvjPh3ZpHbnwuX3a66lDqRRLCigyC_gRDASMpyomD2do5awpNhRCyD_q-27hwxKe_T6ivSZ_0NgbUg";

    protected static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    protected final RaceMapData data;
    
    private final Timer timer;

    protected boolean showDouglasPeuckerPoints = false;

    protected boolean showManeuverHeadUp = false;

    protected boolean showManeuverBearAway = false;

    protected boolean showManeuverTack = false;

    protected boolean showManeuverJibe = false;

    protected boolean showManeuverPenaltyCircle = false;

    protected boolean showManeuverMarkPassing = false;

    protected boolean showManeuverOther = false;
    
    public RaceMap(SailingServiceAsync sailingService, ErrorReporter errorReporter, Timer timer) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.timer = timer;

        data = new RaceMapData();
    }

    public void loadMapsAPI(final Grid grid, final int gridRow, final int gridColumn) {
        Maps.loadMapsApi(mapsAPIKey, "2", false, new Runnable() {
            public void run() {
                map = new MapWidget();
                map.addControl(new LargeMapControl3D());
                map.addControl(new MenuMapTypeControl());
                map.addControl(new ScaleControl());
                // Add the map to the HTML host page
                map.setSize("100%", "100%");
                map.setScrollWheelZoomEnabled(true);
                map.setContinuousZoom(true);

//                MapType currentMapType = map.getCurrentMapType();
//                Projection projection = currentMapType.getProjection();
                
                grid.setWidget(gridRow, gridColumn, map);
                
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
                boatIconDownwindPortRotator = new ImageRotator(resources.lowlightedBoatIconDW_Port());
                boatIconHighlightedDownwindPortRotator = new ImageRotator(resources.highlightedBoatIconDW_Port());
                boatIconDownwindStarboardRotator = new ImageRotator(resources.lowlightedBoatIconDW_Starboard());
                boatIconHighlightedDownwindStarboardRotator = new ImageRotator(resources
                        .highlightedBoatIconDW_Starboard());
                boatIconPortRotator = new ImageRotator(resources.lowlightedBoatIcon_Port());
                boatIconHighlightedPortRotator = new ImageRotator(resources.highlightedBoatIcon_Port());
                boatIconStarboardRotator = new ImageRotator(resources.lowlightedBoatIcon_Starboard());
                boatIconHighlightedStarboardRotator = new ImageRotator(resources.highlightedBoatIcon_Starboard());
                buoyIcon = Icon.newInstance(resources.buoyIcon().getSafeUri().asString());
                buoyIcon.setIconAnchor(Point.newInstance(4, 4));
                tackToStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|00FF00|000000");
                tackToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                tackToPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=T|FF0000|000000");
                tackToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                jibeToStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|00FF00|000000");
                jibeToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                jibeToPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=J|FF0000|000000");
                jibeToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                headUpOnStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|00FF00|000000");
                headUpOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                headUpOnPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=H|FF0000|000000");
                headUpOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
                bearAwayOnStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|00FF00|000000");
                bearAwayOnStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                bearAwayOnPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=B|FF0000|000000");
                bearAwayOnPortIcon.setIconAnchor(Point.newInstance(10, 33));
                markPassingToStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|00FF00|000000");
                markPassingToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                markPassingToPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=M|FF0000|000000");
                markPassingToPortIcon.setIconAnchor(Point.newInstance(10, 33));
                unknownManeuverIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=?|FFFFFF|000000");
                unknownManeuverIcon.setIconAnchor(Point.newInstance(10, 33));
                penaltyCircleToStarboardIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|00FF00|000000");
                penaltyCircleToStarboardIcon.setIconAnchor(Point.newInstance(10, 33));
                penaltyCircleToPortIcon = Icon
                        .newInstance("http://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=P|FF0000|000000");
                penaltyCircleToPortIcon.setIconAnchor(Point.newInstance(10, 33));
            }
        });
    }

    /**
     * From {@link #data.fixes} as well as the selection of {@link #getCompetitorsToShow competitors to show}, computes the
     * from/to times for which to request GPS raceMapData.fixes from the server. No update is performed here to {@link #data.fixes}. The
     * result guarantees that, when used in
     * {@link SailingServiceAsync#getBoatPositions(String, String, Map, Map, boolean, AsyncCallback)}, for each
     * competitor from {@link #competitorsToShow} there are all raceMapData.fixes known by the server for that competitor starting
     * at <code>upTo-{@link #tailLengthInMilliSeconds}</code> and ending at <code>upTo</code> (exclusive).
     * 
     * @return a triple whose {@link Triple#getA() first} component contains the "from", and whose {@link Triple#getB()
     *         second} component contains the "to" times for the competitors whose trails / positions to show; the
     *         {@link Triple#getC() third} component tells whether the existing raceMapData.fixes can remain and be augmented by
     *         those requested or need to be replaced
     */
    protected Triple<Map<CompetitorDAO, Date>, Map<CompetitorDAO, Date>, Map<CompetitorDAO, Boolean>> computeFromAndTo(
            Date upTo, Collection<CompetitorDAO> competitorsToShow) {
        Date tailstart = new Date(upTo.getTime() - tailLengthInMilliSeconds);
        Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
        Map<CompetitorDAO, Boolean> overlapWithKnownFixes = new HashMap<CompetitorDAO, Boolean>();
        
        for (CompetitorDAO competitor : competitorsToShow) {
            List<GPSFixDAO> fixesForCompetitor = data.fixes.get(competitor);
            Date fromDate;
            Date toDate;
            Date timepointOfLastKnownFix = fixesForCompetitor == null ? null
                    : data.getTimepointOfLastNonExtrapolated(fixesForCompetitor);
            Date timepointOfFirstKnownFix = fixesForCompetitor == null ? null
                    : data.getTimepointOfFirstNonExtrapolated(fixesForCompetitor);
            boolean overlap = false;
            if (fixesForCompetitor != null && timepointOfFirstKnownFix != null
                    && !tailstart.before(timepointOfFirstKnownFix) && timepointOfLastKnownFix != null
                    && !tailstart.after(timepointOfLastKnownFix)) {
                // the beginning of what we need is contained in the interval we already have; skip what we already have
                fromDate = timepointOfLastKnownFix;
                overlap = true;
            } else {
                fromDate = tailstart;
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


    /**
     * Adds the raceMapData.fixes received in <code>result</code> to {@link #data.fixes} and ensures they are still contiguous for each
     * competitor. If <code>overlapsWithKnownraceMapData.fixes</code> indicates that the raceMapData.fixes received in <code>result</code>
     * overlap with those already known, the raceMapData.fixes are merged into the list of already known raceMapData.fixes for the competitor.
     * Otherwise, the raceMapData.fixes received in <code>result</code> replace those known so far for the respective competitor.
     */
    protected void updateFixes(Map<CompetitorDAO, List<GPSFixDAO>> result,
            Map<CompetitorDAO, Boolean> overlapsWithKnownFixes) {
        for (Map.Entry<CompetitorDAO, List<GPSFixDAO>> e : result.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                List<GPSFixDAO> fixesForCompetitor = data.fixes.get(e.getKey());
                if (fixesForCompetitor == null) {
                    fixesForCompetitor = new ArrayList<GPSFixDAO>();
                    data.fixes.put(e.getKey(), fixesForCompetitor);
                }
                if (!overlapsWithKnownFixes.get(e.getKey())) {
                    fixesForCompetitor.clear();
                    // to re-establish the invariants for raceMapData.tails, raceMapData.firstShownFix and raceMapData.lastShownFix, we now need to remove
                    // all
                    // points from the competitor's polyline and clear the entries in raceMapData.firstShownFix and raceMapData.lastShownFix
                    if (map != null && data.tails.containsKey(e.getKey())) {
                        map.removeOverlay(data.tails.remove(e.getKey()));
                    }
                    data.firstShownFix.remove(e.getKey());
                    data.lastShownFix.remove(e.getKey());
                    fixesForCompetitor.addAll(e.getValue());
                } else {
                    data.mergeFixes(e.getKey(), e.getValue());
                }
            }
        }
    }

    protected void showMarksOnMap(List<MarkDAO> result) {
        if (map != null) {
            Set<MarkDAO> toRemove = new HashSet<MarkDAO>(data.buoyMarkers.keySet());
            for (MarkDAO markDAO : result) {
                Marker buoyMarker = data.buoyMarkers.get(markDAO);
                if (buoyMarker == null) {
                    buoyMarker = createBuoyMarker(markDAO);
                    data.buoyMarkers.put(markDAO, buoyMarker);
                    map.addOverlay(buoyMarker);
                } else {
                    buoyMarker.setLatLng(LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg));
                    toRemove.remove(markDAO);
                }
            }
            for (MarkDAO toRemoveMarkDAO : toRemove) {
                Marker marker = data.buoyMarkers.remove(toRemoveMarkDAO);
                map.removeOverlay(marker);
            }
            zoomMapFirstTimeToMarks(data.buoyMarkers.keySet());
        }
    }

/**
     * Zooms the map to the given marks if the map was not zoomed yet. If the map zoom to the marks was successful one
     * time, the initial zoom to these marks can not be done again except the race selection is changed via {@link RaceMapPanel#onRaceSelectionChange(List).
     * 
     * @param marksToZoomAt
     *            the marks to zoom at
     */
    protected void zoomMapFirstTimeToMarks(Set<MarkDAO> marksToZoomAt) {
        if (!mapZoomedOrPannedSinceLastRaceSelectionChange && !mapFirstZoomDone) {
            LatLngBounds newBounds = null;
            if (marksToZoomAt != null && !marksToZoomAt.isEmpty()) {
                for (MarkDAO markDAO : marksToZoomAt) {
                    LatLng latLngZoomFirstTime = LatLng.newInstance(markDAO.position.latDeg, markDAO.position.lngDeg);
                    LatLngBounds bounds = LatLngBounds.newInstance(latLngZoomFirstTime, latLngZoomFirstTime);
                    if (newBounds == null) {
                        newBounds = bounds;
                    } else {
                        newBounds.extend(bounds.getNorthEast());
                        newBounds.extend(bounds.getSouthWest());
                    }
                }

            }
            if (newBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newBounds));
                map.setCenter(newBounds.getCenter());
                map.checkResizeAndCenter();
                mapFirstZoomDone = true;
                /*
                 * Reset the mapZoomedOrPannedSinceLastRaceSelection: In spite of the fact that the map was just zoomed
                 * to the bounds of the buoys, it was not a zoom or pan triggered by the user. As a consequence the
                 * mapZoomedOrPannedSinceLastRaceSelection option has to reset again.
                 */
                mapZoomedOrPannedSinceLastRaceSelectionChange = false;
            }
        }
    }

    /**
     * @param from
     *            time point for first fix to show in raceMapData.tails
     * @param to
     *            time point for last fix to show in raceMapData.tails
     */
    protected void showBoatsOnMap(Date from, Date to, Collection<CompetitorDAO> competitorsToShow,
            Set<CompetitorDAO> competitorsSelectedInMap) {
        if (map != null) {
            LatLngBounds newMapBounds = null;
            Set<CompetitorDAO> competitorDAOsOfUnusedTails = new HashSet<CompetitorDAO>(data.tails.keySet());
            Set<CompetitorDAO> competitorDAOsOfUnusedMarkers = new HashSet<CompetitorDAO>(data.boatMarkers.keySet());

            for (CompetitorDAO competitorDAO : competitorsToShow) {
                if (data.fixes.containsKey(competitorDAO)) {
                    Polyline tail = data.tails.get(competitorDAO);
                    if (tail == null) {
                        tail = createTailAndUpdateIndices(competitorDAO, from, to);
                        map.addOverlay(tail);
                    } else {
                        data.updateTail(tail, competitorDAO, from, to);
                        competitorDAOsOfUnusedTails.remove(competitorDAO);
                    }
                    LatLngBounds bounds = tail.getBounds();
                    if (newMapBounds == null) {
                        newMapBounds = bounds;
                    } else {
                        newMapBounds.extend(bounds.getNorthEast());
                        newMapBounds.extend(bounds.getSouthWest());
                    }
                    if (data.lastShownFix.containsKey(competitorDAO) && data.lastShownFix.get(competitorDAO) != -1) {
                        GPSFixDAO lastPos = data.getBoatFix(competitorDAO);
                        Marker boatMarker = data.boatMarkers.get(competitorDAO);
                        if (boatMarker == null) {
                            boatMarker = createBoatMarker(competitorDAO, false);
                            map.addOverlay(boatMarker);
                            data.boatMarkers.put(competitorDAO, boatMarker);
                        } else {
                            competitorDAOsOfUnusedMarkers.remove(competitorDAO);
                            // check if anchors match; re-use marker with setImage only if anchors match
                            Point newAnchor = getBoatImageRotator(lastPos,
                                    competitorsSelectedInMap.contains(competitorDAO)).getAnchor();
                            Point oldAnchor = boatMarker.getIcon().getIconAnchor();
                            if (oldAnchor.getX() == newAnchor.getX() && oldAnchor.getY() == newAnchor.getY()) {
                                boatMarker.setLatLng(LatLng.newInstance(lastPos.position.latDeg,
                                        lastPos.position.lngDeg));
                                boatMarker.setImage(getBoatImageURL(lastPos,
                                        competitorsSelectedInMap.contains(competitorDAO)));
                            } else {
                                // anchors don't match; replace marker
                                map.removeOverlay(boatMarker);
                                boatMarker = createBoatMarker(competitorDAO,
                                        competitorsSelectedInMap.contains(competitorDAO));
                                map.addOverlay(boatMarker);
                                data.boatMarkers.put(competitorDAO, boatMarker);
                            }
                        }
                    }
                }
            }
            if (!mapZoomedOrPannedSinceLastRaceSelectionChange && newMapBounds != null) {
                map.setZoomLevel(map.getBoundsZoomLevel(newMapBounds));
                map.setCenter(newMapBounds.getCenter());
                mapFirstZoomDone = true;
            }
            for (CompetitorDAO unusedMarkerCompetitorDAO : competitorDAOsOfUnusedMarkers) {
                map.removeOverlay(data.boatMarkers.remove(unusedMarkerCompetitorDAO));
            }
            for (CompetitorDAO unusedTailCompetitorDAO : competitorDAOsOfUnusedTails) {
                map.removeOverlay(data.tails.remove(unusedTailCompetitorDAO));
            }
        }
    }

    
    private String getBoatImageURL(GPSFixDAO boatFix, boolean highlighted) {
        return getBoatImageURL(getBoatImageRotator(boatFix, highlighted), boatFix);
    }

    private String getBoatImageURL(ImageRotator boatImageRotator, GPSFixDAO boatFix) {
        // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
        int zoomLevel = map.getZoomLevel();
        double minScaleFactor = 0.1; 
        double maxScaleFactor = 1.0;
        
        double scaleDiffPerZoomLevel = (maxScaleFactor - minScaleFactor) / 21.0;   
        
        double scaleFactor = minScaleFactor + scaleDiffPerZoomLevel * zoomLevel;
        
        return boatImageRotator.getRotatedImageURL(boatFix.speedWithBearing.bearingInDegrees, scaleFactor);
    }

    private Icon getBoatImageIcon(GPSFixDAO boatFix, boolean highlighted) {
        ImageRotator boatImageRotator = getBoatImageRotator(boatFix, highlighted);
        Icon icon = Icon.newInstance(getBoatImageURL(boatImageRotator, boatFix));
        icon.setIconAnchor(boatImageRotator.getAnchor());
        return icon;
    }

    private ImageRotator getBoatImageRotator(GPSFixDAO boatFix, boolean highlighted) {
        if (boatFix.tack == Tack.PORT) {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindStarboardRotator;
                } else {
                    return boatIconDownwindStarboardRotator;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedStarboardRotator;
                } else {
                    return boatIconStarboardRotator;
                }
            }
        } else {
            if (LegType.DOWNWIND == boatFix.legType) {
                if (highlighted) {
                    return boatIconHighlightedDownwindPortRotator;
                } else {
                    return boatIconDownwindPortRotator;
                }
            } else {
                if (highlighted) {
                    return boatIconHighlightedPortRotator;
                } else {
                    return boatIconPortRotator;
                }
            }
        }
    }

    protected Marker createBuoyMarker(final MarkDAO markDAO) {
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

    protected Marker createBoatMarker(final CompetitorDAO competitorDAO, boolean highlighted) {
        GPSFixDAO boatFix = data.getBoatFix(competitorDAO);
        double latDeg = boatFix.position.latDeg;
        double lngDeg = boatFix.position.lngDeg;
        MarkerOptions options = MarkerOptions.newInstance();
        Icon icon = getBoatImageIcon(boatFix, highlighted);
        options.setIcon(icon);
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
            }
        });
        return boatMarker;
    }

    private void showMarkInfoWindow(MarkDAO markDAO, LatLng latlng) {
        map.getInfoWindow().open(latlng, new InfoWindowContent(getInfoWindowContent(markDAO)));
    }

    private void showCompetitorInfoWindow(final CompetitorDAO competitorDAO, LatLng where) {
        GPSFixDAO latestFixForCompetitor = data.getBoatFix(competitorDAO);
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
        result.add(new Label("Tack: " + lastFix.tack.name()));
        if (!selectedEventAndRace.isEmpty()) {
            EventDAO event = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getA();
            RaceDAO race = selectedEventAndRace.get(selectedEventAndRace.size() - 1).getC();
            if (event != null && race != null) {
                Map<CompetitorDAO, Date> from = new HashMap<CompetitorDAO, Date>();
                from.put(competitorDAO, data.fixes.get(competitorDAO).get(data.firstShownFix.get(competitorDAO)).timepoint);
                Map<CompetitorDAO, Date> to = new HashMap<CompetitorDAO, Date>();
                to.put(competitorDAO, data.getBoatFix(competitorDAO).timepoint);
                /* currently not showing Douglas-Peucker points; TODO use checkboxes to select what to show (Bug #6) */
                sailingService.getDouglasPoints(new EventNameAndRaceName(event.name, race.name), from, to, 3,
                        new AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining douglas positions: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<GPSFixDAO>> result) {
                                data.lastDouglasPeuckerResult = result;
                                if (data.douglasMarkers != null) {
                                    removeAllMarkDouglasPeuckerpoints();
                                }
                                if (!timer.isPlaying()) {
                                    if (showDouglasPeuckerPoints) {
                                        showMarkDouglasPeuckerPoints(result);
                                    }
                                }
                            }
                        });
                sailingService.getManeuvers(new EventNameAndRaceName(event.name, race.name), from, to,
                        new AsyncCallback<Map<CompetitorDAO, List<ManeuverDAO>>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error obtaining maneuvers: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Map<CompetitorDAO, List<ManeuverDAO>> result) {
                                data.lastManeuverResult = result;
                                if (data.maneuverMarkers != null) {
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
        return "#" + (Integer.toHexString(competitorDAO.hashCode()) + "000000").substring(0, 4).toUpperCase() + "00";
    }

    /**
     * Creates a polyline for the competitor represented by <code>competitorDAO</code>, taking the raceMapData.fixes from
     * {@link #data.fixes raceMapData.fixes.get(competitorDAO)} and using the raceMapData.fixes starting at time point <code>from</code> (inclusive)
     * up to the last fix with time point before <code>to</code>. The polyline is returned. Updates are applied to
     * {@link #data.lastShownFix}, {@link #data.firstShownFix} and {@link #data.tails}.
     */
    protected Polyline createTailAndUpdateIndices(final CompetitorDAO competitorDAO, Date from, Date to) {
        List<LatLng> points = new ArrayList<LatLng>();
        List<GPSFixDAO> fixesForCompetitor = data.fixes.get(competitorDAO);
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
            data.firstShownFix.put(competitorDAO, indexOfFirst);
            data.lastShownFix.put(competitorDAO, indexOfLast);
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
            }
        });
        result.addPolylineMouseOutHandler(new PolylineMouseOutHandler() {
            @Override
            public void onMouseOut(PolylineMouseOutEvent event) {
                map.setTitle("");
            }
        });
        data.tails.put(competitorDAO, result);
        return result;
    }

    protected void removeAllMarkDouglasPeuckerpoints() {
        if (data.douglasMarkers != null) {
            for (Marker marker : data.douglasMarkers) {
                map.removeOverlay(marker);
            }
        }
        data.douglasMarkers = null;
    }

    protected void removeAllManeuverMarkers() {
        if (data.maneuverMarkers != null) {
            for (Marker marker : data.maneuverMarkers) {
                map.removeOverlay(marker);
            }
            data.maneuverMarkers = null;
        }
    }

    protected void showMarkDouglasPeuckerPoints(Map<CompetitorDAO, List<GPSFixDAO>> gpsFixPointMapForCompetitors) {
        data.douglasMarkers = new HashSet<Marker>();
        if (map != null && gpsFixPointMapForCompetitors != null) {
            Set<CompetitorDAO> keySet = gpsFixPointMapForCompetitors.keySet();
            Iterator<CompetitorDAO> iter = keySet.iterator();
            while (iter.hasNext()) {
                CompetitorDAO competitorDAO = iter.next();
                List<GPSFixDAO> gpsFix = gpsFixPointMapForCompetitors.get(competitorDAO);
                for (GPSFixDAO fix : gpsFix) {
                    LatLng latLng = LatLng.newInstance(fix.position.latDeg, fix.position.lngDeg);
                    MarkerOptions options = MarkerOptions.newInstance();
                    options.setTitle(fix.timepoint+": "+fix.position+", "+fix.speedWithBearing.toString());
                    Marker marker = new Marker(latLng, options);
                    data.douglasMarkers.add(marker);
                    map.addOverlay(marker);
                }
            }
        }
    }

    protected void showManeuvers(Map<CompetitorDAO, List<ManeuverDAO>> maneuvers) {
        data.maneuverMarkers = new HashSet<Marker>();
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
                    if (maneuver.type.equals("TACK") && showManeuverTack) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(tackToPortIcon);
                        } else {
                            options.setIcon(tackToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("JIBE") && showManeuverJibe) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(jibeToPortIcon);
                        } else {
                            options.setIcon(jibeToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("HEAD_UP") && showManeuverHeadUp) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(headUpOnPortIcon);
                        } else {
                            options.setIcon(headUpOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("BEAR_AWAY") && showManeuverBearAway) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(bearAwayOnPortIcon);
                        } else {
                            options.setIcon(bearAwayOnStarboardIcon);
                        }
                    } else if (maneuver.type.equals("PENALTY_CIRCLE") && showManeuverPenaltyCircle) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(penaltyCircleToPortIcon);
                        } else {
                            options.setIcon(penaltyCircleToStarboardIcon);
                        }
                    } else if (maneuver.type.equals("MARK_PASSING") && showManeuverMarkPassing) {
                        if (maneuver.newTack.equals("PORT")) {
                            options.setIcon(markPassingToPortIcon);
                        } else {
                            options.setIcon(markPassingToStarboardIcon);
                        }
                    } else {
                        if (maneuver.type.equals("UNKNOWN") && showManeuverOther) {
                            options.setIcon(unknownManeuverIcon);
                        } else {
                            showThisManeuver = false;
                        }
                    }
                    if (showThisManeuver) {
                        Marker marker = new Marker(latLng, options);
                        data.maneuverMarkers.add(marker);
                        map.addOverlay(marker);
                    }
                }
            }
        }
    }
}
