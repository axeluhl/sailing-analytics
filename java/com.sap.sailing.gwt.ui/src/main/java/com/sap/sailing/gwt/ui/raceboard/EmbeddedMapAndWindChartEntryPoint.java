
package com.sap.sailing.gwt.ui.raceboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.AbstractSailingEntryPoint;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.TimePanel;
import com.sap.sailing.gwt.ui.client.TimePanelSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.common.Duration;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomModel;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.shared.GwtHttpRequestUtils;

public class EmbeddedMapAndWindChartEntryPoint extends AbstractSailingEntryPoint {
    private static final String PARAM_REGATTA_LIKE_NAME = "regattaLikeName";
    private static final String PARAM_RACE_COLUMN_NAME = "raceColumnName";
    private static final String PARAM_FLEET_NAME = "fleetName";
    private static final String PARAM_EVENT_ID = "eventId";
    
    private String regattaLikeName;
    private String raceColumnName;
    private String fleetName;
    private UUID eventId;
    
    private static final RaceMapResources raceMapResources = GWT.create(RaceMapResources.class);
    private static final int DEFAULT_WIND_CHART_HEIGHT = 200;

    @Override
    protected void doOnModuleLoad() {
        GWT.debugger();
        super.doOnModuleLoad();
        // read mandatory parameters
        regattaLikeName = Window.Location.getParameter(PARAM_REGATTA_LIKE_NAME);
        raceColumnName = Window.Location.getParameter(PARAM_RACE_COLUMN_NAME);
        fleetName = Window.Location.getParameter(PARAM_FLEET_NAME);
        String eventIdParamValue = Window.Location.getParameter(PARAM_EVENT_ID);
        if (eventIdParamValue != null && !eventIdParamValue.isEmpty()) {
            eventId = UUID.fromString(eventIdParamValue);
        }
        if (regattaLikeName == null || regattaLikeName.isEmpty() || raceColumnName == null || raceColumnName.isEmpty() ||
                fleetName == null || fleetName.isEmpty()) {
            createErrorPage("This page requires a valid regatta, race column and fleet name to identify the race to show.");
            return;
        }
        
        // read optional parameters
        final boolean showWindChart = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_WINDCHART, true /* default*/);
        final boolean showViewStreamlets = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_STREAMLETS, false /* default*/);
        final boolean showViewSimulation = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_SIMULATION, true /* default*/);
        final boolean showMapControls = GwtHttpRequestUtils.getBooleanParameter(RaceBoardViewConfiguration.PARAM_VIEW_SHOW_MAPCONTROLS, true /* default*/);
        if (eventId != null) {
            sailingService.getEventById(eventId, /* withStatisticalData */ false, new AsyncCallback<EventDTO>() {
                @Override
                public void onSuccess(EventDTO eventDTO) {
                    checkUrlParameters(eventDTO, showWindChart, showMapControls, showViewStreamlets, showViewSimulation);
                }

                @Override
                public void onFailure(Throwable t) {
                    reportError("Error trying to create the raceboard: " + t.getMessage());
                }
            });
        } else {
            checkUrlParameters(/* event */ null, showWindChart, showMapControls, showViewStreamlets, showViewSimulation);
        }
    }
    
    private void createErrorPage(String message) {
        final DockLayoutPanel vp = new DockLayoutPanel(Unit.PX);
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(getStringMessages(), this, getUserService());
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        RootLayoutPanel.get().add(vp);
        vp.addNorth(logoAndTitlePanel, 100);
        vp.add(new Label(message));
    }

    private void checkUrlParameters(final EventDTO event, final boolean showWindChart,
            final boolean showMapControls, final boolean showViewStreamlets, final boolean showViewSimulation) {
        if (eventId != null && event == null) {
            createErrorPage(getStringMessages().noSuchEvent());
        }
        sailingService.getRaceIdentifier(regattaLikeName, raceColumnName, fleetName, new AsyncCallback<RegattaAndRaceIdentifier>() {
            @Override
            public void onFailure(Throwable caught) {
                createErrorPage("Could not obtain a race with name " + raceColumnName + " for fleet "+fleetName+" for a regatta with name " + regattaLikeName+
                        ": "+caught.getMessage());
            }

            @Override
            public void onSuccess(final RegattaAndRaceIdentifier selectedRaceIdentifier) {
                if (selectedRaceIdentifier == null) {
                    createErrorPage("Could not obtain a race with name " + raceColumnName + " for fleet "+fleetName+" for a regatta with name " + regattaLikeName);
                } else {
                    final StringBuilder title = new StringBuilder(regattaLikeName);
                    title.append('/');
                    title.append(raceColumnName);
                    if (!fleetName.equals(LeaderboardNameConstants.DEFAULT_FLEET_NAME)) {
                        title.append('/');
                        title.append(fleetName);
                    }
                    Window.setTitle(title.toString());
                    final RaceSelectionModel raceSelectionModel = new RaceSelectionModel();
                    final List<RegattaAndRaceIdentifier> raceList = Collections.singletonList(selectedRaceIdentifier);
                    raceSelectionModel.setSelection(raceList);
                    final long refreshInterval = Duration.ONE_SECOND.times(3).asMillis();
                    final Timer timer = new Timer(PlayModes.Live);
                    AsyncActionsExecutor asyncActionsExecutor = new AsyncActionsExecutor();
                    final TimeRangeWithZoomProvider timeRangeWithZoomProvider = new TimeRangeWithZoomModel();
                    // Use a TimePanel to manage wind chart zoom, although the TimePanel itself is not being displayed;
                    // let the time panel always return to "live" mode if the user wants that
                    final TimePanel<TimePanelSettings> timePanel = new TimePanel<TimePanelSettings>(
                            timer, timeRangeWithZoomProvider, getStringMessages(), /* canReplayWhileLive */ false) {
                        protected boolean isLiveModeToBeMadePossible() {
                            return true;
                        }
                    };
                    final Button backToLivePlayButton = timePanel.getBackToLiveButton();
                    final TimePanelSettings timePanelSettings = timePanel.getSettings();
                    timePanelSettings.setRefreshInterval(refreshInterval);
                    timePanel.updateSettings(timePanelSettings);
                    raceMapResources.combinedWindPanelStyle().ensureInjected();
                    final CompetitorSelectionProvider competitorSelection = createEmptyFilterCompetitorModel(); // show no competitors
                    final RaceMap raceMap = new RaceMap(sailingService, asyncActionsExecutor, /* errorReporter */ EmbeddedMapAndWindChartEntryPoint.this, timer,
                            competitorSelection, getStringMessages(), showMapControls, showViewStreamlets,
                            showViewSimulation, selectedRaceIdentifier, raceMapResources.combinedWindPanelStyle(), /* showHeaderPanel */ false) {
                        @Override
                        protected void showAdditionalControls(MapWidget map) {
                            backToLivePlayButton.removeFromParent();
                            map.setControls(ControlPosition.RIGHT_BOTTOM, backToLivePlayButton);
                        }
                    };
                    final RaceMapSettings mapSettings = raceMap.getSettings();
                    mapSettings.setZoomSettings(new RaceMapZoomSettings(Arrays.asList(ZoomTypes.BUOYS), /* zoom to selection */ false));
                    raceMap.updateSettings(mapSettings);
                    raceMap.onRaceSelectionChange(raceList);
                    final WindChart windChart;
                    if (showWindChart) {
                        windChart = new WindChart(sailingService, raceSelectionModel, timer,
                                timeRangeWithZoomProvider, new WindChartSettings(), getStringMessages(),
                                asyncActionsExecutor, /* errorReporter */
                                EmbeddedMapAndWindChartEntryPoint.this, /* compactChart */ true);
                        windChart.onRaceSelectionChange(raceList);
                    } else {
                        windChart = null;
                    }
                    createRaceBoardInOneScreenMode(raceMap, windChart);
                }
            }
        });
    }  

    private CompetitorSelectionProvider createEmptyFilterCompetitorModel() {
        final CompetitorSelectionModel result = new CompetitorSelectionModel(/* hasMultiSelection */ true);
        final FilterSet<CompetitorDTO, Filter<CompetitorDTO>> filterSet = result.getOrCreateCompetitorsFilterSet("Empty");
        filterSet.addFilter(new Filter<CompetitorDTO>() {
            @Override public boolean matches(CompetitorDTO object) { return false; }
            @Override public String getName() { return "Never matching filter"; }
        });
        return result;
    }

    private void createRaceBoardInOneScreenMode(final RaceMap raceMap, final WindChart windChart) {
        final TouchSplitLayoutPanel p = new TouchSplitLayoutPanel(/* horizontal splitter width */ 3, /* vertical splitter height */ 25);
        RootLayoutPanel.get().add(p);
        if (windChart != null) {
            windChart.setVisible(true);
            p.insert(windChart.getEntryWidget(), windChart, Direction.SOUTH, DEFAULT_WIND_CHART_HEIGHT);
            p.setWidgetVisibility(windChart.getEntryWidget(), windChart, /* hidden */ false, DEFAULT_WIND_CHART_HEIGHT);
        }
        p.insert(raceMap.getEntryWidget(), raceMap, Direction.CENTER, 400);
        p.addStyleName("dockLayoutPanel");
    }
}
