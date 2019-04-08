package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.ui.Button;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceCompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * MultiCompetitorRaceChart is a GWT panel that can show competitor data (e.g. current speed over ground, windward distance to
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
public class MultiCompetitorRaceChart extends AbstractCompetitorRaceChart<MultiCompetitorRaceChartSettings> implements Component<MultiCompetitorRaceChartSettings> {
    
    private final MultiCompetitorRaceChartLifecycle lifecycle;
    
    /**
     * Creates a Chart used for example in the Raceboard to display various additional data.
     * Cannot be used without a lifecycle, as the allowedDetailTypes are determined via the lifecycle
     */
    public MultiCompetitorRaceChart(Component<?> parent, ComponentContext<?> context,
            MultiCompetitorRaceChartLifecycle lifecycle,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            RaceCompetitorSelectionProvider competitorSelectionProvider, RegattaAndRaceIdentifier selectedRaceIdentifier,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            final ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust,
            final String leaderboardGroupName, String leaderboardName) {
        super(parent, context, sailingService, asyncActionsExecutor, competitorSelectionProvider,
                selectedRaceIdentifier, timer,
                timeRangeWithZoomProvider, stringMessages, errorReporter,
                /* show initially */DetailType.CHART_WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD, null, compactChart,
                allowTimeAdjust, leaderboardGroupName, leaderboardName);
        this.lifecycle = lifecycle;
    }
    
    @Override
    protected Button createSettingsButton() {
        Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        return settingsButton;
    }
    
    @Override
    public MultiCompetitorRaceChartSettings getSettings() {
        return new MultiCompetitorRaceChartSettings(getAbstractSettings(), getSelectedFirstDetailType(), getSelectedSecondDetailType());
    }
    
    @Override
    public SettingsDialogComponent<MultiCompetitorRaceChartSettings> getSettingsDialogComponent(
            MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettingsComponent(settings, getStringMessages(),
                lifecycle.getAllowedDetailTypes());
    }

    @Override
    /**
     * {@see com.sap.sse.gwt.client.shared.components.Component} filters the first and second detailtype using the
     * lifecycles allowedTypes. If non allowed types are found, then they are replaced by either
     * WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD or none for the first and second type. What types are allowed is
     * determined by the environment, for example if additional foiling sensor data is imported
     */
    public void updateSettings(MultiCompetitorRaceChartSettings newSettings) {
        boolean settingsChanged = updateSettingsOnly(newSettings);
        DetailType firstType = newSettings.getFirstDetailType();
        DetailType secondType = newSettings.getSecondDetailType();
        if (!Util.contains(lifecycle.getAllowedDetailTypes(), firstType)) {
            // if the first type is not allowed here, choose a different valid value
            firstType = DetailType.CHART_WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD;
        }
        if (!Util.contains(lifecycle.getAllowedDetailTypes(), secondType)) {
            //if the second type is not allowed here, do not set it.
            secondType = null;
        }
        boolean selectedDetailTypeChanged = setSelectedDetailTypes(firstType, secondType);
        if (selectedDetailTypeChanged || settingsChanged) {
            clearChart();
            timeChanged(timer.getTime(), null);
        }
    }

    @Override
    protected Component<MultiCompetitorRaceChartSettings> getComponent() {
        return this;
    }

    @Override
    public String getLocalizedShortName() {
        return lifecycle.getLocalizedShortName();
    }

    @Override
    public String getDependentCssClassName() {
        return "multiCompetitorRaceChart";
    }

    @Override
    public String getId() {
        return lifecycle.getComponentId();
    }
}
