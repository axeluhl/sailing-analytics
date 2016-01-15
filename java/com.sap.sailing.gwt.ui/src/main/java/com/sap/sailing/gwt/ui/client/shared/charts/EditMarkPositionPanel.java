package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.StockChart;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.events.click.ClickMapEvent;
import com.google.gwt.maps.client.events.click.ClickMapHandler;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapEvent;
import com.google.gwt.maps.client.events.mousedown.MouseDownMapHandler;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CourseMarkOverlay;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;

public class EditMarkPositionPanel  extends AbstractRaceStockChart implements Component<AbstractSettings> {
    private final RaceMap raceMap;
    
    private Map<String, CourseMarkOverlay> courseMarkOverlays;
    private Set<HandlerRegistration> courseMarkHandlers;
    
    private Set<String> selectedMarks;

    private MapWidget map;
    private Set<HandlerRegistration> mapListeners;
    
    private boolean visible;
    
    public EditMarkPositionPanel(final RaceMap raceMap, final StringMessages stringMessages, SailingServiceAsync sailingService, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, 
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        super(sailingService, timer, timeRangeWithZoomProvider, stringMessages, asyncActionsExecutor, errorReporter);
        this.raceMap = raceMap;
        courseMarkHandlers = new HashSet<>();
        selectedMarks = new HashSet<>();
        mapListeners = new HashSet<>();
        this.getEntryWidget().setTitle(stringMessages.editMarkPositions());
        setVisible(false);
        
        chart = new StockChart()
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
            .setLinePlotOptions(new LinePlotOptions().setLineWidth(0).setMarker(
                    new Marker().setEnabled(true).setRadius(4)).setShadow(false));
        
        chart.getXAxis().setType(Axis.Type.DATE_TIME)
                .setMaxZoom(60 * 1000) // 1 minute
        .setAxisTitleText(stringMessages.time());
        chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return dateFormatHoursMinutes.format(new Date(axisLabelsData.getValueAsLong()));
            }
        }));
        timePlotLine = chart.getXAxis().createPlotLine().setColor("#656565").setWidth(1.5).setDashStyle(DashStyle.SOLID);
        setSize("100%", "100%");
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName("Test")
                .setYAxis(0)
                .setPlotOptions(new LinePlotOptions().setSelected(true));
        Point[] points = new Point[3];
        points[0] = new Point(1, 0);
        points[1] = new Point(2, 0);
        points[2] = new Point(5, 0);
        newSeries.setPoints(points);
        chart.addSeries(newSeries, true, false);
        setWidget(chart);
        chart.setSizeToMatchContainer();
        chart.redraw();
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers();
            registerCourseMarkListeners();
        } else {
            raceMap.unregisterAllCourseMarkInfoWindowClickHandlers();
            raceMap.registerAllCourseMarkInfoWindowClickHandlers();
            unregisterCourseMarkHandlers(); 
        }
        super.setVisible(visible);
    }
    
    private void unregisterCourseMarkHandlers() {
        Iterator<HandlerRegistration> iterator = courseMarkHandlers.iterator();
        while(iterator.hasNext()) {
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
        for( HandlerRegistration listener : mapListeners ) {
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
        // TODO Auto-generated method stub
    }
}
