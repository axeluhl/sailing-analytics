package com.sap.sailing.gwt.ui.polarmining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarBackendData;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;


public class PolarBackendResultsPresenter extends AbstractResultsPresenter<Settings> {

    private final DockLayoutPanel dockLayoutPanel;
    
    private final Chart polarChart;
    private final SimpleLayoutPanel polarChartWrapperPanel;
    
    private final Chart speedChart;
    private final Chart angleChart;
    private final DockLayoutPanel speedAndAngleChart;
    
    public PolarBackendResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        
        polarChart = createPolarChart();
        polarChartWrapperPanel = new SimpleLayoutPanel() {
            @Override
            public void onResize() {
                polarChart.setSizeToMatchContainer();
                polarChart.redraw();
            }
        };
        polarChartWrapperPanel.add(polarChart);
        
        speedChart = createSpeedChart();
        angleChart = createAngleChart();
        speedAndAngleChart = new DockLayoutPanel(Unit.PCT) {
            @Override
            public void onResize() {
                speedChart.setSizeToMatchContainer();
                speedChart.redraw();
                angleChart.setSizeToMatchContainer();
                angleChart.redraw();
            }
        };
        speedAndAngleChart.addNorth(speedChart, 50);
        speedAndAngleChart.addSouth(angleChart, 50);
        
        dockLayoutPanel = new DockLayoutPanel(Unit.PCT);
        dockLayoutPanel.addWest(polarChartWrapperPanel, 40);
        dockLayoutPanel.addEast(speedAndAngleChart, 60);
    }
   

    private Chart createSpeedChart() {
        Chart speedChart = new Chart().setType(Type.LINE).setHeight100().setWidth100();
        speedChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        speedChart.setExporting(new Exporting().setEnabled(false));
        return speedChart;
    }
    
    private Chart createAngleChart() {
        Chart speedChart = new Chart().setType(Type.LINE).setHeight100().setWidth100();
        speedChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        speedChart.setExporting(new Exporting().setEnabled(false));
        return speedChart;
    }

    private Chart createPolarChart() {
        LinePlotOptions linePlotOptions = new LinePlotOptions().setLineWidth(1).setMarker(new Marker().setEnabled(false));
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(linePlotOptions)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        polarSheetChart.getYAxis().setMin(0);
        polarSheetChart.getXAxis().setMin(-179).setMax(180).setTickInterval(45);
        polarSheetChart.setOption("/pane/startAngle", 180);
        polarSheetChart.setExporting(new Exporting().setEnabled(false));
        return polarSheetChart;
    }

    @Override
    protected Widget getPresentationWidget() {
        return dockLayoutPanel;
    }

    @Override
    protected void onDataSelectionValueChange() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void internalShowResults(QueryResultDTO<?> result) {
        final Set<Series> seriesToHideAfterRendering = new HashSet<>();
        Map<GroupKey, ?> results = result.getResults();
        List<GroupKey> sortedNaturally = new ArrayList<GroupKey>(results.keySet());
        Collections.sort(sortedNaturally, new Comparator<GroupKey>() {
            @Override
            public int compare(GroupKey o1, GroupKey o2) {
                Comparator<String> naturalComparator = new NaturalComparator();
                return naturalComparator.compare(o1.asString(), o2.asString());
            }
        });
        for (GroupKey key : sortedNaturally) {
            PolarBackendData aggregation = (PolarBackendData) results.get(key);
            if (aggregation.hasUpwindSpeedData()) {
                Series upwindSpeedSeries = speedChart.createSeries();
                upwindSpeedSeries.setName(key.asString() + "-" + stringMessages.upWind());
                double[] upwindSpeedOverWindSpeed = aggregation.getUpwindSpeedOverWindSpeed();
                for (int i = 0; i < 30; i++) {
                    upwindSpeedSeries.addPoint(i, upwindSpeedOverWindSpeed[i], false, false, false);
                    
                }
                speedChart.addSeries(upwindSpeedSeries, false, false);
            }
            if (aggregation.hasDownwindSpeedData()) {
                Series downwindSpeedSeries = speedChart.createSeries();
                downwindSpeedSeries.setName(key.asString() + "-" + stringMessages.downWind());
                double[] downwindSpeedOverWindSpeed = aggregation.getDownwindSpeedOverWindSpeed();
                for (int i = 0; i < 30; i++) {
                    downwindSpeedSeries.addPoint(i, downwindSpeedOverWindSpeed[i], false, false, false);
                    
                }
                speedChart.addSeries(downwindSpeedSeries, false, false);
            }
            speedChart.redraw();
            if (aggregation.hasUpwindAngleData()) {
                Series upwindAngleSeries = speedChart.createSeries();
                upwindAngleSeries.setName(key.asString() + "-" + stringMessages.upWind());
                double[] upwindAngleOverWindSpeed = aggregation.getUpwindAngleOverWindSpeed();
                for (int i = 0; i < 30; i++) {
                    upwindAngleSeries.addPoint(i, upwindAngleOverWindSpeed[i], false, false, false);
                    
                }
                angleChart.addSeries(upwindAngleSeries, false, false);
            }
            if (aggregation.hasDownwindAngleData()) {
                Series downwindAngleSeries = speedChart.createSeries();
                downwindAngleSeries.setName(key.asString() + "-" + stringMessages.downWind());
                double[] downwindAngleOverWindSpeed = aggregation.getDownwindAngleOverWindSpeed();
                for (int i = 0; i < 30; i++) {
                    downwindAngleSeries.addPoint(i, downwindAngleOverWindSpeed[i], false, false, false);
                    
                }
                angleChart.addSeries(downwindAngleSeries, false, false);
            }
            angleChart.redraw();
            boolean[] hasDataForAngle = aggregation.getDataForAngleBooleanArray();
            for (int i = 5; i < 30; i = i + 3) {
                Series polarSeries = polarChart.createSeries();
                polarSeries.setName(key.asString() + "-" + i + "kn");
                double[][] data = aggregation.getPolarDataPerWindspeedAndAngle();
                for (int j = 0; j < 360; j++) {
                    int convertedAngle = j  > 180 ? j  - 360 : j ;
                    polarSeries.addPoint(convertedAngle, hasDataForAngle[j] ? data[j][i] : 0, false, false, false);
                }
                if (i!=11) {
                    seriesToHideAfterRendering.add(polarSeries);
                }
                polarChart.addSeries(polarSeries, false, false);
            }
            polarChart.redraw();
            
        }
        //Initially resize the chart. Otherwise it's too big. FIXME with a better solution
        Timer timer = new Timer() {

            @Override
            public void run() {
                polarChart.setSizeToMatchContainer();
                speedChart.setSizeToMatchContainer();
                angleChart.setSizeToMatchContainer();
                for (Series seriesToHide : seriesToHideAfterRendering) {
                    seriesToHide.setVisible(false, false);
                }
                polarChart.redraw();
            }
        };
        timer.schedule(200);
    }

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().polarResultsPresenter();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return "polarResultsPresenter";
    }

}
