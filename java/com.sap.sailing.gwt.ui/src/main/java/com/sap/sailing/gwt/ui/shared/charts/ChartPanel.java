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
 * ChartPanel is a GWT panel that can show one sort of competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the sailing service returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if u know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author D056866 Benjamin Ebling
 * 
 */
public class ChartPanel extends AbstractChartPanel<ChartSettings> implements Component<ChartSettings> {

    public ChartPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, CompetitorSelectionProvider competitorSelectionProvider,
            RaceSelectionProvider raceSelectionProvider, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, DetailType dataToShow, StringMessages stringMessages,
            ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust) {
        super(sailingService, asyncActionsExecutor, competitorSelectionProvider, raceSelectionProvider, timer, timeRangeWithZoomProvider, stringMessages, errorReporter,
                dataToShow, compactChart, allowTimeAdjust);
    }

    @Override
    protected Component<ChartSettings> getComponent() {
        return this;
    }

    @Override
    public SettingsDialogComponent<ChartSettings> getSettingsDialogComponent() {
        return new ChartSettingsComponent(getAbstractSettings(), getStringMessages());
    }

    @Override
    public void updateSettings(ChartSettings newSettings) {
        updateSettingsOnly(newSettings);
        clearChart();
        loadData(true);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }
}
