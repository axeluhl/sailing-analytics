package com.sap.sailing.gwt.ui.shared.charts;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeRangeWithZoomProvider;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

/**
 * MultiChartPanel is a GWT panel that can show competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart. The chart type can be selected from the settings.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the SailingService returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if you know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866), Axel Uhl (d043530)
 * 
 */
public class MultiChartPanel extends AbstractChartPanel<MultiChartSettings> implements Component<MultiChartSettings> {
    
    public MultiChartPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust) {
        this(sailingService, asyncActionsExecutor, competitorSelectionProvider, raceSelectionProvider, timer,
                timeRangeWithZoomProvider, stringMessages, errorReporter, compactChart, allowTimeAdjust, null, null);
    }
    
    public MultiChartPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust,
            String leaderboardGroupName, String leaderboardName) {
        super(sailingService, asyncActionsExecutor, competitorSelectionProvider, raceSelectionProvider, timer, timeRangeWithZoomProvider, stringMessages, errorReporter,
                /*show initially*/ DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER, compactChart, allowTimeAdjust, leaderboardGroupName, leaderboardName);
    }

    @Override
    public SettingsDialogComponent<MultiChartSettings> getSettingsDialogComponent() {
        return new MultiChartSettingsComponent(new MultiChartSettings(getAbstractSettings(), getSelectedDetailType()), getStringMessages());
    }

    @Override
    public void updateSettings(MultiChartSettings newSettings) {
        boolean settingsChanged = updateSettingsOnly(newSettings);
        boolean selectedDetailTypeChanged = setSelectedDetailType(newSettings.getDetailType());
        if (selectedDetailTypeChanged || settingsChanged) {
            clearChart();
            timeChanged(timer.getTime());
        }
    }

    @Override
    protected Component<MultiChartSettings> getComponent() {
        return this;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

}
