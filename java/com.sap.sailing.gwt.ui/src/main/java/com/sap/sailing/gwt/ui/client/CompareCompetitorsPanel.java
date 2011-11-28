package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;

public class CompareCompetitorsPanel extends FormPanel {
    private final List<CompetitorDAO> competitors;
    private LineChart chart;
    private final SailingServiceAsync sailingService;
    private VerticalPanel mainPanel;
    private String raceName;

    public CompareCompetitorsPanel(SailingServiceAsync sailingService,
            final List<CompetitorDAO> competitors, String raceName, String leaderboardName) {
        this.sailingService = sailingService;
        this.competitors = competitors;
        this.raceName = raceName;
        mainPanel = new VerticalPanel();
        

        final Runnable onLoadCallback = new Runnable() {

            @Override
            public void run() {
                
                chart = new LineChart(prepareTableData(CompareCompetitorsPanel.this.raceName, null), getOptions());
                mainPanel.add(chart);
                fireEvent(new DataLoadedEvent());
                // chartLoaded = true;
            }
        };
        VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);

        this.sailingService.getLeaderboardRowDAOOfRace(leaderboardName, raceName, competitors, 50,
                new AsyncCallback<LeaderboardRowDAO[][]>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failed to laod race data.");
                    }

                    @Override
                    public void onSuccess(LeaderboardRowDAO[][] result) {
                        fireEvent(new DataLoadedEvent());
                        insertData(result);
                    }
                });
        // while (!chartLoaded){}
        this.add(mainPanel);
    }
    
    private void insertData(LeaderboardRowDAO[][] rows){
        if (chart != null){
            chart.draw(prepareTableData(raceName, rows), getOptions());
        }
    }

    private Options getOptions() {
        Options opt = Options.create();
        opt.setWidth(800);
        opt.setHeight(600);
        opt.setTitle("Speed over ground");
        
        return opt;
    }

    private AbstractDataTable prepareTableData(String raceName, LeaderboardRowDAO[][] competitorData) {
        DataTable data = DataTable.create();
        data.addColumn(ColumnType.STRING, "Time");
        for (int i = 0; i < competitors.size(); i++) {
            data.addColumn(ColumnType.NUMBER, competitors.get(i).name);
        }
        
        
        if (competitorData == null){
            return data;
        }
        if (competitorData[0] != null){
            data.addRows(competitorData[0].length);
            for (int n = 0; n < competitorData[0].length; n++){
                LegEntryDAO leg = competitorData[0][0].fieldsByRaceName.get(raceName).legDetails.get(0);
                data.setValue(n, 0, "" + ((leg == null)? 0 : leg.estimatedTimeToNextWaypointInSeconds));
            }
            for (int i = 0; i< competitorData.length; i++){
                for (int j = 0; j < competitorData[i].length; j++){
                    LegEntryDAO leg = competitorData[i][j].fieldsByRaceName.get(raceName).legDetails.get(0);
                    data.setValue(j, (i+1), ((leg == null)? 0 : leg.currentSpeedOverGroundInKnots));
                }
            }
        }

        return data;
    }
    
    //DataLoaded event handling.
    public void addDataLoadedHandler(DataLoadedHandler handler){
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
