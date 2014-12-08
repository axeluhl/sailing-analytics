package com.sap.sailing.dashboards.gwt.client.startanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.LoadApi;
import com.google.gwt.maps.client.LoadApi.LoadLibrary;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.maps.client.controls.MapTypeStyle;
import com.google.gwt.maps.client.controls.ScaleControlOptions;
import com.google.gwt.maps.client.maptypes.MapTypeStyleFeatureType;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisCompetitorDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.RGBColor;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapOverlaysZIndexes;
import com.sap.sailing.gwt.ui.client.shared.racemap.TailFactory;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.racemap.GoogleMapStyleHelper;

public class StartAnalysisSimpleMap extends AbsolutePanel implements TailFactory {

    private MapWidget map;
    private StartAnalysisDTO startAnalysisDTO;

    public StartAnalysisSimpleMap(int containerID, StartAnalysisDTO startAnalysisDTO) {

        this.startAnalysisDTO = startAnalysisDTO;

        this.getElement().getStyle().setPosition(Position.ABSOLUTE);
        this.getElement().getStyle().setWidth(100, Unit.PCT);
        this.getElement().getStyle().setHeight(100, Unit.PCT);
        loadMapsAPIV3();
    }

    private void loadMapsAPIV3() {
        boolean sensor = true;
        // load all the libs for use in the maps
        ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
        loadLibraries.add(LoadLibrary.DRAWING);
        loadLibraries.add(LoadLibrary.GEOMETRY);

        Runnable onLoad = new Runnable() {
            @Override
            public void run() {
                MapOptions mapOptions = MapOptions.newInstance();
                mapOptions.setScrollWheel(true);
                mapOptions.setMapTypeControl(false);
                mapOptions.setPanControl(false);
                mapOptions.setZoomControl(false);
                mapOptions.setScaleControl(true);
                mapOptions.setDraggable(false);
                mapOptions.setPanControl(false);
                MapTypeStyle[] mapTypeStyles = new MapTypeStyle[4];

                // hide all transit lines including ferry lines
                mapTypeStyles[0] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.TRANSIT);
                // hide points of interest
                mapTypeStyles[1] = GoogleMapStyleHelper.createHiddenStyle(MapTypeStyleFeatureType.POI);
                // simplify road display
                mapTypeStyles[2] = GoogleMapStyleHelper.createSimplifiedStyle(MapTypeStyleFeatureType.ROAD);
                // set water color
                mapTypeStyles[3] = GoogleMapStyleHelper.createColorStyle(MapTypeStyleFeatureType.WATER, new RGBColor(0,
                        136, 255), -35, -34);

                mapOptions.setMapTypeStyles(mapTypeStyles);
                ScaleControlOptions scaleControlOptions = ScaleControlOptions.newInstance();
                scaleControlOptions.setPosition(ControlPosition.BOTTOM_RIGHT);
                mapOptions.setScaleControlOptions(scaleControlOptions);
                mapOptions.setStreetViewControl(false);
                map = new MapWidget(mapOptions);
                StartAnalysisSimpleMap.this.add(map, 0, 0);
                map.setSize("100%", "100%");
                map.getElement().getStyle().setBackgroundColor("#4293DB");
                map.triggerResize();
                drawStartLine(startAnalysisDTO.startLineMarks);
                drawLinesFromStartLineToFirstMark(startAnalysisDTO.startLineMarks, startAnalysisDTO.firstMark);
                drawStartlineMarks(startAnalysisDTO.startLineMarks);
                drawFirstMark(startAnalysisDTO.firstMark);
                drawTails(createGPSFixesForCompetitors(startAnalysisDTO));
                drawBoats(createGPSPositionForCompetitors(startAnalysisDTO));
                zoomMapToNewBounds(calculateNewBounds(startAnalysisDTO.startLineMarkPositions));
            }
        };
        LoadApi.go(onLoad, loadLibraries, sensor);
    }

    private Map<CompetitorDTO, List<GPSFixDTO>> createGPSFixesForCompetitors(StartAnalysisDTO startAnalysisDTO) {

        Map<CompetitorDTO, List<GPSFixDTO>> gpsFixesForCompetitors = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        for (StartAnalysisCompetitorDTO startAnalysisCompetitorDTO : startAnalysisDTO.startAnalysisCompetitorDTOs) {
            gpsFixesForCompetitors.put(startAnalysisCompetitorDTO.competitorDTO,
                    startAnalysisCompetitorDTO.gpsFixDTOs);
        }
        return gpsFixesForCompetitors;
    }
    
    private Map<CompetitorDTO, GPSFixDTO> createGPSPositionForCompetitors(StartAnalysisDTO startAnalysisDTO) {
        Map<CompetitorDTO, GPSFixDTO> gpsPositionForCompetitors = new HashMap<CompetitorDTO, GPSFixDTO>();
        for (StartAnalysisCompetitorDTO startAnalysisCompetitorDTO : startAnalysisDTO.startAnalysisCompetitorDTOs) {
            gpsPositionForCompetitors.put(startAnalysisCompetitorDTO.competitorDTO,
                    startAnalysisCompetitorDTO.gpsFixDTOs.get(startAnalysisCompetitorDTO.gpsFixDTOs.size()-1));
        }
        return gpsPositionForCompetitors;
    }

    private void drawStartLine(List<MarkDTO> startlineMarks) {
        if (map != null && startlineMarks != null) {

            if (startlineMarks.size() == 2) {
                LatLng startLinePoint1 = LatLng.newInstance(startlineMarks.get(0).position.latDeg,
                        startlineMarks.get(0).position.lngDeg);
                LatLng startLinePoint2 = LatLng.newInstance(startlineMarks.get(1).position.latDeg,
                        startlineMarks.get(1).position.lngDeg);

                PolylineOptions options = PolylineOptions.newInstance();
                options.setGeodesic(true);
                options.setStrokeColor("#FFFFFF");
                options.setStrokeWeight(2);
                options.setStrokeOpacity(1.0);

                MVCArray<LatLng> pointsAsArray = MVCArray.newInstance();
                pointsAsArray.insertAt(0, startLinePoint1);
                pointsAsArray.insertAt(1, startLinePoint2);

                Polyline startLine = Polyline.newInstance(options);
                startLine.setPath(pointsAsArray);

                startLine.setMap(map);
            }
        }
    }
    
    private void drawLinesFromStartLineToFirstMark(List<MarkDTO> startlineMarks, MarkDTO firstMark){
        if (map != null && startlineMarks != null && firstMark != null) {
            if (startlineMarks.size() == 2) {
                LatLng startLinePoint1 = LatLng.newInstance(startlineMarks.get(0).position.latDeg,
                        startlineMarks.get(0).position.lngDeg);
                LatLng startLinePoint2 = LatLng.newInstance(startlineMarks.get(1).position.latDeg,
                        startlineMarks.get(1).position.lngDeg);
                LatLng firstMarkPoint = LatLng.newInstance(firstMark.position.latDeg,
                        firstMark.position.lngDeg);

                PolylineOptions options = PolylineOptions.newInstance();
                options.setGeodesic(true);
                options.setStrokeColor("#A3A3A3");
                options.setStrokeWeight(2);
                options.setStrokeOpacity(1.0);

                MVCArray<LatLng> pointsAsArray1 = MVCArray.newInstance();
                pointsAsArray1.insertAt(0, startLinePoint1);
                pointsAsArray1.insertAt(1, firstMarkPoint);
                
                MVCArray<LatLng> pointsAsArray2 = MVCArray.newInstance();
                pointsAsArray2.insertAt(0, startLinePoint2);
                pointsAsArray2.insertAt(1, firstMarkPoint);

                Polyline startMarkFirstMark1 = Polyline.newInstance(options);
                Polyline startMarkFirstMark2 = Polyline.newInstance(options);
                
                startMarkFirstMark1.setPath(pointsAsArray1);
                startMarkFirstMark2.setPath(pointsAsArray2);

                startMarkFirstMark1.setMap(map);
                startMarkFirstMark2.setMap(map);
            }
        }
    }

    private void drawStartlineMarks(List<MarkDTO> startlineMarks) {
        if (map != null) {
            for (MarkDTO markDTO : startlineMarks) {
                CourseMarkOverlay courseMarkOverlay = createCourseMarkOverlay(
                        RaceMapOverlaysZIndexes.COURSEMARK_ZINDEX, markDTO);
                courseMarkOverlay.addToMap();
            }
        }
    }
    
    private void drawFirstMark(MarkDTO firstMark){
        CourseMarkOverlay courseMarkOverlay = createCourseMarkOverlay(
                RaceMapOverlaysZIndexes.COURSEMARK_ZINDEX, firstMark);
        courseMarkOverlay.addToMap();
    }

    private CourseMarkOverlay createCourseMarkOverlay(int zIndex, final MarkDTO markDTO) {
        markDTO.position.latDeg = markDTO.position.latDeg;
        final CourseMarkOverlay courseMarkOverlay = new CourseMarkOverlay(map, zIndex, markDTO);
        return courseMarkOverlay;
    }

    private LatLngBounds calculateNewBounds(List<PositionDTO> startLinePositions) {
        LatLngBounds newBounds = null;
        List<PositionDTO> newStartLinePositionDTOs = new ArrayList<PositionDTO>(startLinePositions);
        Iterable<PositionDTO> positionsToZoom = newStartLinePositionDTOs;

        if (positionsToZoom != null) {
            for (PositionDTO position : positionsToZoom) {
                PositionDTO positionMoovedToNorth = position;
                positionMoovedToNorth.latDeg = positionMoovedToNorth.latDeg - 0.0009;

                LatLng markLatLng = LatLng.newInstance(positionMoovedToNorth.latDeg, positionMoovedToNorth.lngDeg);
                LatLngBounds bounds = LatLngBounds.newInstance(markLatLng, markLatLng);
                if (newBounds == null) {
                    newBounds = bounds;
                } else {
                    newBounds.extend(markLatLng);
                }
                markLatLng = null;
                position = null;
                positionMoovedToNorth.latDeg = positionMoovedToNorth.latDeg + 0.0009;
            }
        }
        // }
        positionsToZoom = null;
        return newBounds;
    }

    private void zoomMapToNewBounds(LatLngBounds newBounds) {
        if (newBounds != null) {
            map.setCenter(newBounds.getCenter());
            map.setZoom(17);
        }
    }

    private void drawBoats(Map<CompetitorDTO, GPSFixDTO> competitorAndLastFix) {

        Set<CompetitorDTO> competitors = competitorAndLastFix.keySet();

        for (CompetitorDTO competitorDTO : competitors) {
            BoatOverlay boatOverlay = createBoatOverlay(RaceMapOverlaysZIndexes.BOATS_ZINDEX, competitorDTO, false);
            boatOverlay.setSelected(false);
            boatOverlay.setBoatFix(competitorAndLastFix.get(competitorDTO), 10);
            boatOverlay.addToMap();
        }
    }

    private BoatOverlay createBoatOverlay(int zIndex, final CompetitorDTO competitorDTO, boolean highlighted) {
        final BoatOverlay boatCanvas = new BoatOverlay(map, zIndex, competitorDTO, competitorDTO.getColor());
        boatCanvas.setSelected(false);
        return boatCanvas;
    }

    private void drawTails(Map<CompetitorDTO, List<GPSFixDTO>> competitorFixes) {

        Set<CompetitorDTO> competitors = competitorFixes.keySet();
        for (CompetitorDTO competitor : competitors) {
            Polyline competitorPolyline = createPolylineFromGPSFixDTOs(competitor, competitorFixes.get(competitor));
            competitorPolyline.setMap(map);
        }

    }

    private Polyline createPolylineFromGPSFixDTOs(CompetitorDTO competitorDTO, List<GPSFixDTO> fixes) {
        List<LatLng> points = new ArrayList<LatLng>();
        int indexOfFirst = -1;
        int indexOfLast = -1;
        int i = 0;
        for (Iterator<GPSFixDTO> fixIter = fixes.iterator(); fixIter.hasNext() && indexOfLast == -1;) {
            GPSFixDTO fix = fixIter.next();
            if (!fix.timepoint.before(fixes.get(fixes.size() - 1).timepoint)) {
                indexOfLast = i - 1;
            } else {
                LatLng point = null;
                if (indexOfFirst == -1) {
                    if (!fix.timepoint.before(fixes.get(0).timepoint)) {
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
        Polyline result = createTail(competitorDTO, points);
        return result;
    }

    @Override
    public Polyline createTail(CompetitorDTO competitor, List<LatLng> points) {
        PolylineOptions options = createTailStyle(competitor, false);
        Polyline result = Polyline.newInstance(options);

        MVCArray<LatLng> pointsAsArray = MVCArray.newInstance(points.toArray(new LatLng[0]));
        result.setPath(pointsAsArray);

        return result;
    }

    @Override
    public PolylineOptions createTailStyle(CompetitorDTO competitor, boolean isHighlighted) {
        PolylineOptions options = PolylineOptions.newInstance();
        options.setClickable(true);
        options.setGeodesic(true);
        options.setStrokeOpacity(1.0);
        options.setStrokeColor(competitor.getColor().getAsHtml());
        options.setStrokeWeight(1);
        options.setZindex(RaceMapOverlaysZIndexes.BOATTAILS_ZINDEX);
        return options;
    }
}
