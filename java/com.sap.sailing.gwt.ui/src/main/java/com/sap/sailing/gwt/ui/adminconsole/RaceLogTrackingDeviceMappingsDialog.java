package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Global;
import org.moxieapps.gwt.highcharts.client.Highcharts;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEvent;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class RaceLogTrackingDeviceMappingsDialog extends RaceLogTrackingDialog {
    public static final double PERCENTAGE_OF_TIMESPAN_TO_EXTEND_OPEN_ENDS = 0.1;
    public static final String FIELD_INDEX = "index";
    public static final int CHART_WIDTH = 500;
    public static final String SERIES_COLOR = "#fcb913";
    
    private List<DeviceMappingDTO> allMappings = new ArrayList<DeviceMappingDTO>();
    
    private Point[] data;
    
    private Date latest;
    private Date earliest;
    
    private Chart chart;
    
    public RaceLogTrackingDeviceMappingsDialog(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter, final String leaderboardName,
            final String raceColumnName, final String fleetName) {
        super(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName, false);
        
        refresh();
    }

    @Override
    protected void addButtons(Panel buttonPanel) {
        Button addCompetitorButton = new Button(stringMessages.add());
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addMapping();
            }
        });
        buttonPanel.add(addCompetitorButton);

        super.addButtons(buttonPanel);
    }
    
    @Override
    protected void addMainContent(Panel mainPanel) {
        super.addMainContent(mainPanel);
        
        chart = new Chart()
        .setType(Series.Type.COLUMN_RANGE)
        .setChartTitleText(stringMessages.deviceMappings())
        .setLegend(new Legend().setEnabled(false))
        .setInverted(true);
        
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setPointMouseOverEventHandler(new PointMouseOverEventHandler() {
            @Override
            public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
                pointMouseOverEvent.getXAsLong();
                return true;
            }
        }).setColor(SERIES_COLOR));
        
        Highcharts.setOptions(
                new Highcharts.Options().setGlobal(
                        new Global()
                        .setUseUTC(false)
                        ));
        
        chart.getXAxis().setOption("labels/enabled", false)
        .setGridLineWidth(0)
        .setMinorGridLineWidth(0);
        
        chart.getYAxis()
        .setAxisTitle(new AxisTitle().setText(stringMessages.time()))
        .setType(Axis.Type.DATE_TIME)
        .setGridLineWidth(0)
        .setMinorGridLineWidth(0);
        
        mainPanel.add(chart);

        chart.setWidth(CHART_WIDTH + "px");
        chart.setHeight("400px");
        
        chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                int index = (int) toolTipData.getXAsLong();
                DeviceMappingDTO mapping = allMappings.get(index);
                String itemType = mapping.mappedTo instanceof MarkDTO ? stringMessages.mark() : stringMessages.competitor();
                
                return "<b>" + stringMessages.device() + ":</b> " + mapping.deviceType + " - " + mapping.deviceId + "<br/>" +
                    "<b>" + stringMessages.mappedTo() + ":</b> " + itemType + " - " + mapping.mappedTo + "<br/>" +
                    "<b>" + stringMessages.from() + ":</b> " + DateAndTimeFormatterUtil.formatDateAndTime(mapping.from) + "<br/>" +
                    "<b>" + stringMessages.to() + ":</b> " + DateAndTimeFormatterUtil.formatDateAndTime(mapping.to);
            }
        }));
    }

    @Override
    protected void save() {
        // TODO Auto-generated method stub
        
    }
    
    private void updateChart() {
        data = new Point[allMappings.size()];
        long earliestMillis = earliest.getTime();
        long latestMillis = latest.getTime();
        long extension = (long) ((latestMillis - earliestMillis) * PERCENTAGE_OF_TIMESPAN_TO_EXTEND_OPEN_ENDS);
        long yMin = earliestMillis - extension;
        long yMax = latestMillis + extension;
        
        int i = 0;
        for (DeviceMappingDTO mapping : allMappings) {
            JSONObject userData = new JSONObject();
            userData.put(FIELD_INDEX, userData);
            
            long from = mapping.from == null ? yMin - extension : mapping.from.getTime();
            long to = mapping.to == null ? yMax + extension : mapping.to.getTime();
            
            data[i] = new Point(i, from, to);
            
            i++;
        }
        
        chart.removeAllSeries(false);
        
        chart.addSeries(chart.createSeries()
                .setName(stringMessages.deviceMappings())
                .setPoints(data));
        
        chart.getYAxis().setExtremes(yMin, yMax);
    }
    
    private void updateExtremes(DeviceMappingDTO mapping) {
        if (mapping.from != null && earliest.after(mapping.from)) earliest = mapping.from;
        if (mapping.to != null && latest.before(mapping.to)) latest = mapping.to;
        if (mapping.to != null && earliest.after(mapping.to)) earliest = mapping.to;
        if (mapping.from != null && latest.before(mapping.from)) latest = mapping.from;
    }
    
    private void refresh() {
        allMappings.clear();
        earliest = new Date(Long.MAX_VALUE);
        latest = new Date(Long.MIN_VALUE);
        
        sailingService.getDeviceMappingsFromRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<List<DeviceMappingDTO>>() {
            @Override
            public void onSuccess(List<DeviceMappingDTO> result) {
                for (DeviceMappingDTO mapping : result) {
                    updateExtremes(mapping);
                    allMappings.add(mapping);
                }
                
                updateChart();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load mappings for marks: " + caught.getMessage());
            }
        });
    }
    
    private void addMapping() {
        new AddDeviceMappingDialog(sailingService, errorReporter, stringMessages,
                leaderboardName, raceColumnName, fleetName, new DialogCallback<DeviceMappingDTO>() {
            @Override
            public void ok(DeviceMappingDTO mapping) {
                sailingService.addDeviceMappingToRaceLog(leaderboardName, raceColumnName, fleetName, mapping, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        refresh();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Could not add mapping: " + caught.getMessage());
                    }
                });
            }

            @Override
            public void cancel() {}
        }).show();
    }
}
