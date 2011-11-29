package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorWithRaceDAO;

public class CompareCompetitorsPanel extends FormPanel {
    private final List<CompetitorDAO> competitors;
    private CompetitorWithRaceDAO[][] chartData = null;
    private LineChart chart;
    private final SailingServiceAsync sailingService;
    private HorizontalPanel mainPanel;
    private VerticalPanel chartPanel;
    private String raceName;
    private DateTimeFormat dateTimeFormat;
    public static final int SHOW_CURRENT_SPEED_OVER_GROUND = 0;
    public static final int SHOW_VELOCITY_MADE_GOOD = 1;
    public static final int SHOW_GAP_TO_LEADER = 2;
    public static final int SHOW_WINDWARD_DISTANCE_TO_GO = 3;
    private int dataToShow = SHOW_CURRENT_SPEED_OVER_GROUND;

    public CompareCompetitorsPanel(SailingServiceAsync sailingService, final List<CompetitorDAO> competitors,
            String raceName, final String leaderboardName) {
        this.sailingService = sailingService;
        this.competitors = competitors;
        this.raceName = raceName;
        this.dateTimeFormat = DateTimeFormat.getFormat("HH:mm:ss");
        mainPanel = new HorizontalPanel();
        chartPanel = new VerticalPanel();
        chartPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        chartPanel.setSpacing(5);

        final CaptionPanel configCaption = new CaptionPanel("Configuration");
        configCaption.setHeight("100%");
        configCaption.setVisible(false);
        VerticalPanel configPanel = new VerticalPanel();
        configCaption.setContentWidget(configPanel);
        configPanel.setSpacing(7);
        Anchor showConfigAnchor = new Anchor(new SafeHtmlBuilder().appendHtmlConstant(
                "<img class=\"linkNoBorder\" src=\"/images/settings.png\"/>").toSafeHtml());
        showConfigAnchor.setTitle("Configuration");
        showConfigAnchor.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                configCaption.setVisible(!configCaption.isVisible());
            }
        });
        chartPanel.add(showConfigAnchor);
        Label lblChart = new Label("Choose chart:");
        configPanel.add(lblChart);
        final ListBox dataSelection = new ListBox();
        dataSelection.addItem("Speed over ground", "" + SHOW_CURRENT_SPEED_OVER_GROUND);
        dataSelection.addItem("Velocity made good", "" + SHOW_VELOCITY_MADE_GOOD);
        dataSelection.addItem("Gap to leader", "" + SHOW_GAP_TO_LEADER);
        dataSelection.addItem("Windward distance to go", "" + SHOW_WINDWARD_DISTANCE_TO_GO);
        dataSelection.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                dataToShow = Integer.parseInt(dataSelection.getValue(dataSelection.getSelectedIndex()));
                if (chart != null) {
                    chart.draw(prepareTableData(), getOptions());
                }
            }
        });
        configPanel.add(dataSelection);
        Label lblSteps = new Label("Points to load:");
        configPanel.add(lblSteps);
        final TextBox txtbSteps = new TextBox();
        configPanel.add(txtbSteps);
        Button bttSteps = new Button("Refresh");
        bttSteps.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                CompareCompetitorsPanel.this.sailingService.getCompetitorRaceData(leaderboardName,
                        CompareCompetitorsPanel.this.raceName, competitors, Integer.parseInt(txtbSteps.getText()),
                        new AsyncCallback<CompetitorWithRaceDAO[][]>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void onSuccess(CompetitorWithRaceDAO[][] result) {
                                fireEvent(new DataLoadedEvent());
                                chartData = result;
                                if (chart != null) {
                                    chart.draw(prepareTableData(), getOptions());
                                }
                            }
                        });
            }
        });
        configPanel.add(bttSteps);

        final Runnable onLoadCallback = new Runnable() {

            @Override
            public void run() {

                chart = new LineChart(prepareTableData(), getOptions());
                chartPanel.add(chart);
                fireEvent(new DataLoadedEvent());
                // chartLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);

        this.sailingService.getCompetitorRaceData(leaderboardName, this.raceName, competitors, 150,
                new AsyncCallback<CompetitorWithRaceDAO[][]>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failed to laod race data.");
                    }

                    @Override
                    public void onSuccess(CompetitorWithRaceDAO[][] result) {
                        fireEvent(new DataLoadedEvent());
                        chartData = result;
                        if (chart != null) {
                            chart.draw(prepareTableData(), getOptions());
                        }
                    }
                });
        // while (!chartLoaded){}
        mainPanel.add(chartPanel);
        mainPanel.add(configCaption);
        this.add(mainPanel);
    }

    private Options getOptions() {
        Options opt = Options.create();
        opt.setWidth(800);
        opt.setHeight(600);
        switch (dataToShow) {
        case SHOW_VELOCITY_MADE_GOOD:
            opt.setTitle("Velocity made good");
            break;
        case SHOW_GAP_TO_LEADER:
            opt.setTitle("Gap to leader");
            break;
        case SHOW_WINDWARD_DISTANCE_TO_GO:
            opt.setTitle("Windward distance to go");
            break;
        default:
            opt.setTitle("Speed over ground");
        }
        AxisOptions hAxisOptions = AxisOptions.create();
        hAxisOptions.setTitle("time");
        opt.setHAxisOptions(hAxisOptions);

        AxisOptions vAxisOptions = AxisOptions.create();
        switch (dataToShow) {
        case SHOW_VELOCITY_MADE_GOOD:
            vAxisOptions.setTitle("speed in kts");
            break;
        case SHOW_GAP_TO_LEADER:
            vAxisOptions.setTitle("time in s");
            break;
        case SHOW_WINDWARD_DISTANCE_TO_GO:
            vAxisOptions.setTitle("distance in m");
            break;
        default:
            vAxisOptions.setTitle("speed in kts");
            break;
        }
        opt.setVAxisOptions(vAxisOptions);
        return opt;
    }

    private AbstractDataTable prepareTableData() {
        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, "Time");
        for (int i = 0; i < competitors.size(); i++) {
            data.addColumn(ColumnType.NUMBER, competitors.get(i).name);
        }

        if (chartData == null) {
            return data;
        }
        if (chartData[0] != null) {
            int length = 0;
            for (int i = 0; i < chartData.length; i++) {
                length = (length < chartData[i].length) ? chartData[i].length : length;
            }
            data.addRows(length);
            for (int n = 0; n < chartData[0].length; n++) {
                Date d = new Date(chartData[0][n].getLegEntry().timeInMilliseconds);
                String time = "" + ((chartData[0][n] == null) ? 0 : dateTimeFormat.format(d));
                data.setValue(n, 0, time);
            }
            for (int i = 0; i < chartData.length; i++) {
                for (int j = 0; j < chartData[i].length; j++) {
                    Double value = null;
                    switch (dataToShow) {
                    case SHOW_GAP_TO_LEADER:
                        value = chartData[i][j].getLegEntry().gapToLeaderInSeconds;
                        break;
                    case SHOW_VELOCITY_MADE_GOOD:
                        value = chartData[i][j].getLegEntry().velocityMadeGoodInKnots;
                        break;
                    case SHOW_WINDWARD_DISTANCE_TO_GO:
                        value = chartData[i][j].getLegEntry().windwardDistanceToGoInMeters;
                        break;
                    default:
                        value = chartData[i][j].getLegEntry().currentSpeedOverGroundInKnots;
                    }
                    if (value != null) {
                        data.setValue(j, (i + 1), value);
                    }
                }
            }
        }

        return data;
    }

    // DataLoaded event handling.
    public void addDataLoadedHandler(DataLoadedHandler handler) {
        this.addHandler(handler, DataLoadedEvent.TYPE);
    }

    interface DataLoadedHandler extends com.google.gwt.event.shared.EventHandler {
        public void onDataLoaded(DataLoadedEvent event);
    }

    public static class DataLoadedEvent extends GwtEvent<DataLoadedHandler> {
        public static Type<DataLoadedHandler> TYPE = new Type<DataLoadedHandler>();

        public DataLoadedEvent() {
            super();
        }

        @Override
        protected void dispatch(DataLoadedHandler handler) {
            handler.onDataLoaded(this);
        }

        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<DataLoadedHandler> getAssociatedType() {
            return TYPE;
        }

    }

}
