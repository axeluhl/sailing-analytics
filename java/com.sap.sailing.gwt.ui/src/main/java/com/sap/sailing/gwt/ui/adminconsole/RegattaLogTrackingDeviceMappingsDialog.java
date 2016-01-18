package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
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

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.sap.sailing.gwt.ui.adminconsole.DeviceMappingTableWrapper.FilterChangedHandler;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattaLogTrackingDeviceMappingsDialog extends DataEntryDialog<Void> {
    protected final String leaderboardName;

    public static final double PERCENTAGE_OF_TIMESPAN_TO_EXTEND_OPEN_ENDS = 0.1;
    public static final String FIELD_INDEX = "index";
    public static final int CHART_WIDTH = 500;
    public static final String SERIES_COLOR = "#fcb913";
    
    protected ErrorReporter errorReporter;
    protected SailingServiceAsync sailingService;
    protected StringMessages stringMessages;
    protected List<DeviceMappingDTO> mappings = new ArrayList<DeviceMappingDTO>();
    protected DeviceMappingTableWrapper deviceMappingTable;
    
    private Point[] data;
    
    private Date latest;
    private Date earliest;
    private Chart chart;
    
    public RegattaLogTrackingDeviceMappingsDialog(final SailingServiceAsync sailingService,
            final StringMessages stringMessages, final ErrorReporter errorReporter, final String leaderboardName, DialogCallback<Void> callback) {
        super(stringMessages.mapDevices(), /*message*/ null, stringMessages.ok(), stringMessages.cancel(), /*validator*/ null, callback);
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.leaderboardName = leaderboardName;
        refresh();
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        FlowPanel mainPanel = new FlowPanel();
        
        HorizontalPanel buttonPanel = new HorizontalPanel();
        Button addMappingButton = new Button(stringMessages.add());
        addMappingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addMapping();
            }
        });
        buttonPanel.add(addMappingButton);
        
        Button importBtn = new Button(stringMessages.importFixes());
        importBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                importFixes();
            }
        });
        buttonPanel.add(importBtn);
        mainPanel.add(buttonPanel);
        
        deviceMappingTable = new DeviceMappingTableWrapper(sailingService, stringMessages, errorReporter);
        
        deviceMappingTable.addFilterChangedHandler(new FilterChangedHandler() {
            @Override
            public void onFilterChanged(List<DeviceMappingDTO> filteredList) {
                mappings = filteredList;
                updateChart();
            }
        });
        deviceMappingTable.getTable().addCellPreviewHandler(new CellPreviewEvent.Handler<DeviceMappingDTO>() {
            @Override
            public void onCellPreview(CellPreviewEvent<DeviceMappingDTO> event) {
                int i = mappings.indexOf(event.getValue());
                chart.getSeries()[0].getPoints()[i].select(true, false);
            }
        });
        ImagesBarColumn<DeviceMappingDTO, RaceLogTrackingDeviceMappingsImagesBarCell> actionCol =
                new ImagesBarColumn<DeviceMappingDTO, RaceLogTrackingDeviceMappingsImagesBarCell>(
                new RaceLogTrackingDeviceMappingsImagesBarCell(stringMessages));
        actionCol.setFieldUpdater(getActionColFieldUpdater());
        deviceMappingTable.getTable().addColumn(actionCol, stringMessages.actions());
        
        HorizontalPanel deviceMappingPannel = new HorizontalPanel();
        mainPanel.add(deviceMappingPannel);
        
        chart = new Chart()
        .setType(Series.Type.COLUMN_RANGE)
        .setChartTitleText(stringMessages.deviceMappings())
        .setLegend(new Legend().setEnabled(false))
        .setInverted(true);
        
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setPointMouseOverEventHandler(new PointMouseOverEventHandler() {
            @Override
            public boolean onMouseOver(PointMouseOverEvent pointMouseOverEvent) {
                int i = (int) pointMouseOverEvent.getXAsLong();
                deviceMappingTable.getSelectionModel().setSelected(mappings.get(i), true);
                chart.getSeries()[0].getPoints()[i].select(true, false);
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

        chart.setWidth(CHART_WIDTH + "px");
        chart.setHeight("400px");
        
        chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                int index = (int) toolTipData.getXAsLong();
                DeviceMappingDTO mapping = mappings.get(index);
                String itemType = mapping.mappedTo instanceof MarkDTO ? stringMessages.mark() : stringMessages.competitor();
                
                return "<b>" + stringMessages.device() + ":</b> " + mapping.deviceIdentifier.deviceType + " - " + mapping.deviceIdentifier.deviceId + "<br/>" +
                    "<b>" + stringMessages.mappedTo() + ":</b> " + itemType + " - " + mapping.mappedTo + "<br/>" +
                    "<b>" + stringMessages.from() + ":</b> " + DateAndTimeFormatterUtil.formatDateAndTime(mapping.from) + "<br/>" +
                    "<b>" + stringMessages.to() + ":</b> " + DateAndTimeFormatterUtil.formatDateAndTime(mapping.to);
            }
        }));

        deviceMappingPannel.add(chart);
        deviceMappingPannel.add(deviceMappingTable);
        
        return mainPanel;
    }

    void updateChart() {
        earliest = new Date(Long.MAX_VALUE);
        latest = new Date(Long.MIN_VALUE);
        for (DeviceMappingDTO mapping : mappings) {
            updateExtremes(mapping);
        }
        
        data = new Point[mappings.size()];
        long earliestMillis = earliest.getTime();
        long latestMillis = latest.getTime();
        long range = latestMillis - earliestMillis;
        long extension = (long) (range * PERCENTAGE_OF_TIMESPAN_TO_EXTEND_OPEN_ENDS);
        long yMin = earliestMillis - extension;
        long yMax = latestMillis + extension;
        
        int i = 0;
        for (DeviceMappingDTO mapping : mappings) {
            JSONObject userData = new JSONObject();
            userData.put(FIELD_INDEX, userData);
            
            long from = mapping.from == null ? yMin - range : mapping.from.getTime();
            long to = mapping.to == null ? yMax + range : mapping.to.getTime();
            
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
    
    private void addMapping() {
        showAddMappingDialog(null);
    }
    
    private void refresh() {
        sailingService.getDeviceMappings(leaderboardName, new AsyncCallback<List<DeviceMappingDTO>>() {
            @Override
            public void onSuccess(List<DeviceMappingDTO> result) {
                mappings = result;
                updateChart();
                deviceMappingTable.refresh(mappings);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load mappings for marks: " + caught.getMessage());
            }
        });
    }

    private void showAddMappingDialog(DeviceMappingDTO mapping) {
        new RegattaLogAddDeviceMappingDialog(sailingService, errorReporter, stringMessages, leaderboardName,
                new DataEntryDialog.DialogCallback<DeviceMappingDTO>() {
                    @Override
                    public void ok(final DeviceMappingDTO mapping) {
                        sailingService.addDeviceMappingToRegattaLog(leaderboardName, mapping,
                                new AsyncCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        refresh();
                                    }

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        showAddMappingDialog(mapping);
                                        errorReporter.reportError("Could not add mapping: " + caught.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void cancel() {
                        refresh();
                    }
                }, mapping).show();
    }

    private void importFixes() {
        new RegattaLogImportFixesAndAddMappingsDialog(sailingService, errorReporter, stringMessages, leaderboardName,
                new DataEntryDialog.DialogCallback<Collection<DeviceMappingDTO>>() {

                    @Override
                    public void ok(Collection<DeviceMappingDTO> editedObject) {
                        for (DeviceMappingDTO mapping : editedObject) {
                            sailingService.addDeviceMappingToRegattaLog(leaderboardName, mapping,
                                    new AsyncCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            refresh();
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError(caught.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();
    }

    private FieldUpdater<DeviceMappingDTO, String> getActionColFieldUpdater() {
        return new FieldUpdater<DeviceMappingDTO, String>() {
            @Override
            public void update(int index, final DeviceMappingDTO dto, String value) {
                if (RaceLogTrackingDeviceMappingsImagesBarCell.ACTION_CLOSE.equals(value)) {
                    new SetTimePointDialog(stringMessages, stringMessages.setClosingTimePoint(),
                            new DataEntryDialog.DialogCallback<java.util.Date>() {
                                @Override
                                public void ok(java.util.Date editedObject) {
                                    sailingService.closeOpenEndedDeviceMapping(leaderboardName, dto, editedObject,
                                            new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    refresh();
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    errorReporter.reportError("Could not close open ended mapping: "
                                                            + caught.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void cancel() {
                                }
                            }).show();
                } else if (RaceLogTrackingDeviceMappingsImagesBarCell.ACTION_REMOVE.equals(value)) {
                    sailingService.revokeRaceAndRegattaLogEvents(leaderboardName, dto.originalRaceLogEventIds,
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Could not remove mappings: " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    refresh();
                                }

                            });
                }

            }
        };
    }

    @Override
    protected Void getResult() {
        return null;
    }
}