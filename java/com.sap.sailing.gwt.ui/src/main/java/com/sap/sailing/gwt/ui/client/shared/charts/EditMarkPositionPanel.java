package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Date;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.AbstractRaceChart;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.raceboard.SideBySideComponentViewer;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
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
    private final MarksPanel marksPanel;
    private Series markSeries;
    private final Label noMarkSelectedLabel;

    private Map<String, CourseMarkOverlay> courseMarkOverlays;
    private Set<HandlerRegistration> courseMarkHandlers;

    private Set<String> selectedMarks;

    private MapWidget map;
    private Set<HandlerRegistration> mapListeners;

    private boolean visible;
    
    private String leaderboardName;    
    private String raceColumnName;
    private String fleetName;
    private Map<MarkDTO, List<GPSFixDTO>> marks;
    private final ListDataProvider<MarkDTO> markDataProvider;
    private SideBySideComponentViewer sideBySideComponentViewer;

    public EditMarkPositionPanel(final RaceMap raceMap, final LeaderboardPanel leaderboardPanel, 
            RegattaAndRaceIdentifier selectedRaceIdentifier, String leaderboardName, final StringMessages stringMessages,
            SailingServiceAsync sailingService, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider,
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        super(sailingService, timer, timeRangeWithZoomProvider, stringMessages, asyncActionsExecutor, errorReporter);
        this.raceMap = raceMap;
        this.leaderboardPanel = leaderboardPanel;
        this.markDataProvider = new ListDataProvider<>();
        this.marksPanel = new MarksPanel(this, markDataProvider);
        this.noMarkSelectedLabel = new Label("Please select a mark from the list on the left.");
        this.noMarkSelectedLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.leaderboardName = leaderboardName;
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
    
    private void loadData(final Date from, final Date to) {
        if (selectedRaceIdentifier != null && from != null && to != null && marks == null) {
            setWidget(chart);
            showLoading("Loading mark fixes...");
            if (leaderboardName != null) {
                sailingService.getLeaderboard(leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        //TODO
                    }
                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        raceColumnName = null;
                        fleetName = null;
                        FleetDTO fleet = null;
                        for (RaceColumnDTO raceColumn : result.getRaceList()) {
                            fleet = raceColumn.getFleet(selectedRaceIdentifier);
                            if (fleet != null && raceColumn.getRace(fleet).getName() == selectedRaceIdentifier.getRaceName()) {
                                raceColumnName = raceColumn.getName();
                                fleetName = fleet.getName();
                                break;
                            }
                        }
                        if (raceColumnName != null && fleetName != null) {
                            sailingService.getMarkTracks(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Map<MarkDTO,List<GPSFixDTO>>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    // TODO
                                }
                                @Override
                                public void onSuccess(Map<MarkDTO, List<GPSFixDTO>> result) {
                                    marks = result;
                                    List<MarkDTO> markList = new ArrayList<MarkDTO>();
                                    markList.addAll(marks.keySet());
                                    markDataProvider.setList(markList);
                                    hideLoading();
                                    onSelectionChange(null);
                                    onResize();
                                }
                            });
                        }
                    }
                });
            }
        }
    }
    
    private void setSeriesPoints(MarkDTO mark) {
        List<GPSFixDTO> fixes = marks.get(mark);
        Point[] points = new Point[fixes.size()];
        int i = 0;
        for (GPSFixDTO fix : fixes) {
            points[i++] = new Point(fix.timepoint.getTime(), 1);
        }
        setSeriesPoints(markSeries, points);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
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
        super.setVisible(visible);
    }

    private void unregisterCourseMarkHandlers() {
        Iterator<HandlerRegistration> iterator = courseMarkHandlers.iterator();
        while (iterator.hasNext()) {
            HandlerRegistration handler = iterator.next();
            handler.removeHandler();
            iterator.remove();
        }
    }

    public void setMarkOverlays(final Map<String, CourseMarkOverlay> courseMarkOverlays) {
        this.courseMarkOverlays = courseMarkOverlays;
        if (visible) {
            registerCourseMarkListeners();
        }
    }

    private void registerCourseMarkListeners() {
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
        for (HandlerRegistration listener : mapListeners) {
            listener.removeHandler();
        }
        mapListeners.add(this.map.addMouseMoveHandler(new MouseMoveMapHandler() {
            @Override
            public void onEvent(MouseMoveMapEvent event) {
                for (final String markName : selectedMarks) {
                    courseMarkOverlays.get(markName).setMarkPosition(event.getMouseEvent().getLatLng());
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
            onResize();
        } else {
            setWidget(noMarkSelectedLabel);
        }
    }
}
