package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.ComponentConstructionParameters;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class WindChartLifecycle implements ComponentLifecycle<WindChart, WindChartSettings, WindChartSettingsDialogComponent, WindChartLifecycle.WindChartConstructorArgs> {
    private final StringMessages stringMessages;
    
    public static class ConstructionParameters extends ComponentConstructionParameters<WindChart, WindChartSettings, WindChartSettingsDialogComponent, WindChartLifecycle.WindChartConstructorArgs> {
        public ConstructionParameters(WindChartLifecycle componentLifecycle,
                WindChartConstructorArgs componentConstructorArgs, WindChartSettings settings) {
            super(componentLifecycle, componentConstructorArgs, settings);
        }
    }

    public WindChartLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public WindChartSettingsDialogComponent getSettingsDialogComponent(WindChartSettings settings) {
        return new WindChartSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public WindChartSettings createDefaultSettings() {
        return new WindChartSettings();
    }

    @Override
    public WindChartSettings cloneSettings(WindChartSettings settings) {
        return new WindChartSettings(settings.isShowWindSpeedSeries(), settings.getWindSpeedSourcesToDisplay(),
                settings.isShowWindDirectionsSeries(), settings.getWindDirectionSourcesToDisplay(),
                settings.getResolutionInMilliseconds());
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
    
    @Override
    public WindChart createComponent(WindChartConstructorArgs windChartContructorArgs, WindChartSettings settings) {
        return windChartContructorArgs.createComponent(settings);
    }

    public class WindChartConstructorArgs implements ComponentConstructorArgs<WindChart, WindChartSettings> {
        private final WindChartLifecycle windChartLifecycle;
        private final SailingServiceAsync sailingService; 
        private final RegattaAndRaceIdentifier selectedRaceIdentifier; 
        private final Timer timer;
        private final TimeRangeWithZoomProvider timeRangeWithZoomProvider; 
        private final WindChartSettings settings;
        private final StringMessages stringMessages; 
        private final AsyncActionsExecutor asyncActionsExecutor;
        private final ErrorReporter errorReporter; 
        private final boolean compactChart;
        
        public WindChartConstructorArgs(WindChartLifecycle windChartLifecycle, SailingServiceAsync sailingService, RegattaAndRaceIdentifier selectedRaceIdentifier, Timer timer,
                TimeRangeWithZoomProvider timeRangeWithZoomProvider, WindChartSettings settings, final StringMessages stringMessages, 
                AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, boolean compactChart) {
            this.windChartLifecycle = windChartLifecycle;
            this.sailingService = sailingService;
            this.selectedRaceIdentifier = selectedRaceIdentifier;
            this.timer = timer;
            this.timeRangeWithZoomProvider = timeRangeWithZoomProvider;
            this.settings = settings;
            this.stringMessages = stringMessages;
            this.asyncActionsExecutor = asyncActionsExecutor;
            this.errorReporter = errorReporter;
            this.compactChart = compactChart;
        }
        
        @Override
        public WindChart createComponent(WindChartSettings newSettings) {
            WindChart windChart = new WindChart(windChartLifecycle, sailingService, selectedRaceIdentifier, timer,
                    timeRangeWithZoomProvider, settings, stringMessages, 
                    asyncActionsExecutor, errorReporter, compactChart);
            if (newSettings != null) {
                windChart.updateSettings(newSettings);
            }
            return windChart;
        }
    }
}
