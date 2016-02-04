package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.maps.client.mvc.MVCArray;
import com.google.gwt.maps.client.overlays.Polyline;
import com.google.gwt.maps.client.overlays.PolylineOptions;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.FixType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.AbstractRaceChart;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.FixOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.raceboard.SideBySideComponentViewer;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;

public class EditMarkPositionPanel extends AbstractRaceChart implements Component<AbstractSettings>, RequiresResize, SelectionChangeEvent.Handler {    
    private final RaceMap raceMap;
    private final LeaderboardPanel leaderboardPanel;
    private MarksPanel marksPanel;
    private Series markSeries;
    private final Label noMarkSelectedLabel;

    private Set<HandlerRegistration> courseMarkHandlers;
    private Set<HandlerRegistration> fixHandlers;

    private Set<String> selectedMarks;

    private MapWidget map;
    private Set<HandlerRegistration> mapListeners;

    private boolean visible;
    
    private Map<MarkDTO, Map<GPSFixDTO, FixOverlay>> marks;
    private Map<MarkDTO, Polyline> polylines;
    private final ListDataProvider<MarkDTO> markDataProvider;
    private SideBySideComponentViewer sideBySideComponentViewer;

    public EditMarkPositionPanel(final RaceMap raceMap, final LeaderboardPanel leaderboardPanel, 
            RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName, final StringMessages stringMessages,
            SailingServiceAsync sailingService, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider,
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        super(sailingService, timer, timeRangeWithZoomProvider, stringMessages, asyncActionsExecutor, errorReporter);
        this.raceMap = raceMap;
        this.leaderboardPanel = leaderboardPanel;
        this.polylines = new HashMap<MarkDTO, Polyline>();
        this.markDataProvider = new ListDataProvider<>();
        this.noMarkSelectedLabel = new Label("Please select a mark from the list on the left.");
        this.noMarkSelectedLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.courseMarkHandlers = new HashSet<>();
        this.selectedMarks = new HashSet<>();
        this.mapListeners = new HashSet<>();
        this.getEntryWidget().setTitle(stringMessages.editMarkPositions());
        this.setVisible(false);
        
        chart = new Chart()
                .setPersistent(true)
                .setZoomType(BaseChart.ZoomType.X)
                .setMarginLeft(65)
                .setMarginRight(65)
                .setWidth100()
                .setHeight100()
                .setBorderWidth(0)
                .setBorderRadius(0)
                .setBackgroundColor(new Color("#FFFFFF"))
                .setPlotBackgroundColor("#f8f8f8")
                .setPlotBorderWidth(0)
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText("Mark Fixes"))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setPersistent(true);
        
        timePlotLine = chart.getXAxis().createPlotLine().setColor("#656565").setWidth(1.5).setDashStyle(DashStyle.SOLID);

        chart.setClickEventHandler(new ChartClickEventHandler() {
            @Override
            public boolean onClick(ChartClickEvent chartClickEvent) {
                return EditMarkPositionPanel.this.onClick(chartClickEvent);
            }
        });
        
        chart.setSelectionEventHandler(new ChartSelectionEventHandler() {
            @Override
            public boolean onSelection(ChartSelectionEvent chartSelectionEvent) {
                return EditMarkPositionPanel.this.onXAxisSelectionChange(chartSelectionEvent);
            }
        });
        
        chart.getXAxis().setType(Axis.Type.DATE_TIME)
                .setMaxZoom(60 * 1000) // 1 minute
                .setAxisTitleText(stringMessages.time());
        chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return dateFormatHoursMinutes.format(new Date(axisLabelsData.getValueAsLong()));
            }
        }));
        chart.getYAxis().setAxisTitleText("").setOption("labels/enabled", false);
        timePlotLine = chart.getXAxis().createPlotLine().setColor("#656565").setWidth(1)
                .setDashStyle(DashStyle.SOLID);
        markSeries = chart.createSeries().setType(Series.Type.SCATTER).setYAxis(0)
                .setPlotOptions(new LinePlotOptions().setSelected(true).setShowInLegend(false));
        chart.addSeries(markSeries, false, false);
    }
    
    private interface ServiceMock {
        public Map<MarkDTO, List<GPSFixDTO>> getMarkTracks(String leaderboardName, String raceColumnName, String fleetName);
        public void removeDeviceMappingForFix(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark, GPSFixDTO fix);
        public void addMarkFix(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark, GPSFixDTO newFix);
        public void editMarkFixPosition(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark, GPSFixDTO fix, Position newPosition);
    }
    
    private class ServiceMockImpl implements ServiceMock {
        private Map<MarkDTO, List<GPSFixDTO>> markTracks;
        
        public ServiceMockImpl() {
            markTracks = new HashMap<MarkDTO, List<GPSFixDTO>>();
            MarkDTO mark = new MarkDTO("test1", "Three Fixes");
            List<GPSFixDTO> fixes = new ArrayList<>();
            fixes.add(new GPSFixDTO(new Date(1453141600000l), new DegreePosition(53.531, 9.99), null, new WindDTO(), null, null, false));
            fixes.add(new GPSFixDTO(new Date(1453142600000l), new DegreePosition(53.5323, 10), null, new WindDTO(), null, null, false));
            fixes.add(new GPSFixDTO(new Date(1453142400000l), new DegreePosition(53.54, 10.01), null, new WindDTO(), null, null, false));
            markTracks.put(mark, fixes);
            mark = new MarkDTO("test2", "No Fix");
            fixes = new ArrayList<>();
            markTracks.put(mark, fixes);
        }
        
        @Override
        public Map<MarkDTO, List<GPSFixDTO>> getMarkTracks(String leaderboardName, String raceColumnName,
                String fleetName) {
            return markTracks;
        }

        @Override
        public void removeDeviceMappingForFix(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark,
                GPSFixDTO fix) {
            markTracks.get(mark).remove(fix);
        }

        @Override
        public void addMarkFix(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark,
                GPSFixDTO newFix) {
            markTracks.get(mark).add(newFix);
        }

        @Override
        public void editMarkFixPosition(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark, GPSFixDTO fix,
                Position newPosition) {
            markTracks.get(mark).get(markTracks.get(mark).indexOf(fix)).position = newPosition;
        }
    }
    
    private ServiceMock serviceMock = new ServiceMockImpl();
    
    private void loadData(final Date from, final Date to) {
        if (selectedRaceIdentifier != null && from != null && to != null && marks == null) {
            setWidget(chart);
            showLoading("Loading mark fixes...");
            Map<MarkDTO, List<GPSFixDTO>> result = serviceMock.getMarkTracks("", "", "");
            marks = new HashMap<MarkDTO, Map<GPSFixDTO, FixOverlay>>();
            for (final Map.Entry<MarkDTO, List<GPSFixDTO>> fixes : result.entrySet()) {
                Map<GPSFixDTO, FixOverlay> fixOverlayMap = new HashMap<GPSFixDTO, FixOverlay>();
                for (final GPSFixDTO fix : fixes.getValue()) {
                    final FixOverlay overlay = new FixOverlay(map, 1, fix, FixType.BUOY, "#505050", raceMap.getCoordinateSystem());
                    fixOverlayMap.put(fix, overlay);
                    overlay.setVisible(false);
                    overlay.addClickHandler(new ClickMapHandler() {
                        @Override
                        public void onEvent(ClickMapEvent event) {
                            final PopupPanel popup = new PopupPanel(true);
                            popup.setStyleName("EditMarkPositionPopup");
                            MenuBar menu = new MenuBar(true);
                            MenuItem move = new MenuItem("Move fix", new ScheduledCommand() {
                                @Override
                                public void execute() {
                                    overlay.setVisible(false);
                                    new FixPositionChooser(map, overlay, "Confirm Move", new Callback<Position, Exception>() {
                                        @Override
                                        public void onFailure(Exception reason) {
                                            overlay.setVisible(true);
                                            // TODO Auto-generated method stub
                                        }
                                        @Override
                                        public void onSuccess(Position result) {
                                            editMarkFix(fixes.getKey(), fix, result);
                                            overlay.setVisible(true);
                                        }
                                    });
                                    popup.hide();
                                }
                            });
                            MenuItem delete = new MenuItem("Delete fix", new ScheduledCommand() { // TODO: Can delete?
                                @Override
                                public void execute() {
                                    removeMarkFix(fixes.getKey(), fix);
                                    popup.hide();
                                }
                            });
                            menu.addItem(move);
                            menu.addItem(delete);
                            popup.setWidget(menu);
                            popup.setPopupPosition(overlay.getCanvas().getAbsoluteLeft() + (int) overlay.getFixScaleAndSize().getB().getWidth() / 2 + 5, 
                                    overlay.getCanvas().getAbsoluteTop() + (int) overlay.getFixScaleAndSize().getB().getHeight() / 2 + 5);
                            popup.show();
                        }
                    });
                }
                marks.put(fixes.getKey(), fixOverlayMap);
                PolylineOptions options = PolylineOptions.newInstance();
                if (map != null) {
                    options.setMap(map);
                }
                options.setVisible(false);
                polylines.put(fixes.getKey(), Polyline.newInstance(options));
                updatePolylinePoints(fixes.getKey());
            }
            List<MarkDTO> markList = new ArrayList<MarkDTO>();
            markList.addAll(marks.keySet());
            markDataProvider.setList(markList);
            hideLoading();
            onSelectionChange(null);
            onResize();
        }
    }
    
    public void addMarkFix(MarkDTO mark, Date timepoint, Position fixPosition) {
        GPSFixDTO fix = new GPSFixDTO(timepoint, fixPosition, null, new WindDTO(), null, null, false);
        serviceMock.addMarkFix("", "", "", mark, fix);
        FixOverlay overlay = new FixOverlay(map, 1, fix, FixType.BUOY, "#505050", raceMap.getCoordinateSystem());
        marks.get(mark).put(fix, overlay);
        updatePolylinePoints(mark);
        setSeriesPoints(mark);
        onResize();
    }
    
    private void editMarkFix(MarkDTO mark, GPSFixDTO fix, Position newPosition) {
        serviceMock.editMarkFixPosition("", "", "", mark, fix, newPosition);
        fix.position = newPosition;
        marks.get(mark).get(fix).setGPSFixDTO(fix);
        updatePolylinePoints(mark);
    }
    
    private void removeMarkFix(MarkDTO mark, GPSFixDTO fix) {
        serviceMock.removeDeviceMappingForFix("", "", "", mark, fix);
        FixOverlay overlay = marks.get(mark).remove(fix);
        overlay.removeFromMap();
        updatePolylinePoints(mark);
        setSeriesPoints(mark);
        onResize();
    }
    
    private void updatePolylinePoints(MarkDTO mark) {
        MVCArray<LatLng> path = polylines.get(mark).getPath();
        path.clear();
        for (GPSFixDTO fix : marks.get(mark).keySet()) {
            path.push(LatLng.newInstance(fix.position.getLatDeg(), fix.position.getLngDeg()));
        }
    }
    
    private void setSeriesPoints(MarkDTO mark) {
        Point[] points = new Point[marks.get(mark).keySet().size()];
        int i = 0;
        for (GPSFixDTO fix : marks.get(mark).keySet()) {
            points[i++] = new Point(fix.timepoint.getTime(), 1);
        }
        setSeriesPoints(markSeries, points);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (map == null) {
            map = raceMap.getMap();
            if (map != null) {
                setMap(map);
            }
        }
        if (this.visible) {
            if (sideBySideComponentViewer != null) {
                sideBySideComponentViewer.setLeftComponent(marksPanel);
                sideBySideComponentViewer.setLeftComponentToggleButtonVisible(false);
            }
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers();
            registerCourseMarkListeners();
        } else {
            if (sideBySideComponentViewer != null) {
                sideBySideComponentViewer.setLeftComponent(leaderboardPanel);
                sideBySideComponentViewer.setLeftComponentToggleButtonVisible(true);
            }
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            raceMap.registerAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers();
        }
        super.setVisible(this.visible);
    }

    private void unregisterCourseMarkHandlers() {
        Iterator<HandlerRegistration> iterator = courseMarkHandlers.iterator();
        while (iterator.hasNext()) {
            HandlerRegistration handler = iterator.next();
            handler.removeHandler();
            iterator.remove();
        }
    }

    private void registerCourseMarkListeners() {
        if (map != null) {
            Map<String, CourseMarkOverlay> courseMarkOverlays = raceMap.getCourseMarkOverlays();
            for (final Map.Entry<String, CourseMarkOverlay> courseMark : courseMarkOverlays.entrySet()) {
                courseMarkHandlers.add(courseMark.getValue().addMouseDownHandler(new MouseDownMapHandler() {
                    @Override
                    public void onEvent(MouseDownMapEvent event) {
                        selectedMarks.add(courseMark.getKey());
                        MapOptions mapOptions = MapOptions.newInstance(false);
                        mapOptions.setDraggable(false);
                        map.setOptions(mapOptions);
                    }
                }));
            }
        }
    }
    
    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
        // in the BaseChart class would not be called 
        chart.redraw();
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    public void setMap(final MapWidget map) {
        this.map = map;
        this.marksPanel = new MarksPanel(this, map, raceMap.getCoordinateSystem(), markDataProvider, stringMessages);
        for (HandlerRegistration listener : mapListeners) {
            listener.removeHandler();
        }
        mapListeners.add(this.map.addMouseMoveHandler(new MouseMoveMapHandler() {
            @Override
            public void onEvent(MouseMoveMapEvent event) {
                for (final String markName : selectedMarks) {
                    CourseMarkOverlay overlay = raceMap.getCourseMarkOverlays().get(markName);
                    if (overlay != null) {
                        overlay.setMarkPosition(event.getMouseEvent().getLatLng());
                    }
                }
            }
        }));
        mapListeners.add(this.map.addClickHandler(new ClickMapHandler() {
            @Override
            public void onEvent(ClickMapEvent event) {
                selectedMarks.removeAll(selectedMarks);
                MapOptions mapOptions = MapOptions.newInstance(false);
                mapOptions.setDraggable(true);
                map.setOptions(mapOptions);
            }
        }));
        for (Map.Entry<MarkDTO, Polyline> polyline : polylines.entrySet()) {
            polyline.getValue().setMap(map);
        }
    }
    
    public Date getTimepoint() {
       return timer.getTime();
    }

    @Override
    protected Button createSettingsButton() {
        Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        return settingsButton;
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (isVisible()) {
            loadData(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime());
            updateTimePlotLine(newTime);
        }
    }
    
    private void updateTimePlotLine(Date date) {
        chart.getXAxis().removePlotLine(timePlotLine);
        timePlotLine.setValue(date.getTime());
        chart.getXAxis().addPlotLines(timePlotLine);
    }

    public void setComponentViewer(SideBySideComponentViewer sideBySideComponentViewer) {
        this.sideBySideComponentViewer = sideBySideComponentViewer;
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
        List<MarkDTO> selected = marksPanel.getSelectedMarks();
        if (selected.size() > 0) {
            setWidget(chart);
            setSeriesPoints(selected.get(0));
            onResize(); // redraw chart
            for (Map.Entry<String, CourseMarkOverlay> overlay : raceMap.getCourseMarkOverlays().entrySet()) {
                if (!overlay.getKey().equals(selected.get(0).getName())) {
                    overlay.getValue().setVisible(false);
                }
            }
            for (FixOverlay overlay : marks.get(selected.get(0)).values()) {
                overlay.setVisible(true);
            }
            polylines.get(selected.get(0)).setVisible(true);
        } else {
            setWidget(noMarkSelectedLabel);
            showAllCourseMarkOverlays();
            hideAllFixOverlays();
            hideAllPolylines();
        }
    }
    
    public void showAllCourseMarkOverlays() {
        for (Map.Entry<String, CourseMarkOverlay> overlay : raceMap.getCourseMarkOverlays().entrySet()) {
            overlay.getValue().setVisible(true);
        }
    }
    
    public void hideAllFixOverlays() {
        for (Map.Entry<MarkDTO, Map<GPSFixDTO, FixOverlay>> mark : marks.entrySet()) {
            for (FixOverlay overlay : mark.getValue().values()) {
                overlay.setVisible(false);
            }
        }
    }
    
    public void hideAllPolylines() {
        for (Map.Entry<MarkDTO, Polyline> polyline : polylines.entrySet()) {
            polyline.getValue().setVisible(false);
        }
    }
}
