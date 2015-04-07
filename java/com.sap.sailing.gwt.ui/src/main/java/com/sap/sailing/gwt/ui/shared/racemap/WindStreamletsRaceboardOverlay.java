package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.simulator.StreamletParameters;
import com.sap.sailing.gwt.ui.simulator.racemap.MovingCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.streamlets.Swarm;
import com.sap.sailing.gwt.ui.simulator.streamlets.VectorField;
import com.sap.sailing.gwt.ui.simulator.streamlets.WindInfoForRaceVectorField;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A Google Maps overlay based on an HTML5 canvas for drawing a wind field. The {@link VectorField} implementation used is the
 * {@link WindInfoForRaceVectorField} which takes a {@link WindInfoForRaceDTO} as its basis.
 * 
 * @author Christopher Ronnewinkel (D036654)
 * @author Axel Uhl (D043530)
 * 
 */
public class WindStreamletsRaceboardOverlay extends MovingCanvasOverlay {
    public static final String LOAD_WIND_STREAMLET_DATA_CATEGORY = "loadWindStreamletData";
    private static final int animationIntervalMillis = 40;
    private static final long RESOLUTION_IN_MILLIS = 5000;
    private static final int WIND_FETCH_INTERVAL_IN_MILLIS = 10000;
    private static final int CHECK_WIND_SOURCE_INTERVAL_IN_MILLIS = 60000;
    
    private boolean visible = false;
    private final Timer timer;
    private Swarm swarm;
    private final WindInfoForRaceVectorField windField;
    private final WindInfoForRaceDTO windInfoForRace;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final SailingServiceAsync sailingService;
    private final StringMessages stringMessages;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final Scheduler scheduler;
    
    private long latitudeCount;
    private double latitudeSum;

    public WindStreamletsRaceboardOverlay(MapWidget map, int zIndex, final Timer timer,
            RegattaAndRaceIdentifier raceIdentifier, SailingServiceAsync sailingService,
            AsyncActionsExecutor asyncActionsExecutor, StringMessages stringMessages, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
        this.scheduler = Scheduler.get();
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.stringMessages = stringMessages;
        this.raceIdentifier = raceIdentifier;
        this.sailingService = sailingService;
        this.windInfoForRace = new WindInfoForRaceDTO();
        windInfoForRace.raceIsKnownToStartUpwind = true; // default
        windInfoForRace.windSourcesToExclude = new HashSet<>();
        windInfoForRace.windTrackInfoByWindSource = new HashMap<>();
        updateAverageLatitudeDeg(windInfoForRace);
        this.windField = new WindInfoForRaceVectorField(windInfoForRace, /* frames per second */ 1000.0/animationIntervalMillis, coordinateSystem);
        this.timer = timer;
        getCanvas().getElement().setId("swarm-display");
    }

    public double getAverageLatitudeDeg() {
        return latitudeCount > 0 ? latitudeSum/latitudeCount : 0;
    }
    
    private void updateAverageLatitudeDeg(WindInfoForRaceDTO windInfoForRace) {
        for (Entry<WindSource, WindTrackInfoDTO> windSourceAndTrack : windInfoForRace.windTrackInfoByWindSource.entrySet()) {
            for (WindDTO wind : windSourceAndTrack.getValue().windFixes) {
                if (wind.position != null) {
                    latitudeSum += wind.position.getLatDeg();
                    latitudeCount++;
                }
            }
        }
        if (latitudeCount > 0) {
            windField.setAverageLatitudeDeg(latitudeSum/latitudeCount);
        }
    }

    public void startStreamlets() {
        scheduleWindDataRefresh();
        if (swarm == null) {
            setCanvasSettings();
            this.swarm = new Swarm(this, map, timer, windField, new StreamletParameters());
        }
        initCanvasOrigin();
        this.swarm.start(animationIntervalMillis);
    }

    private void scheduleWindDataRefresh() {
        scheduler.scheduleFixedPeriod(new RepeatingCommand() {
            @Override
            public boolean execute() {
                updateWindField();
                return visible;
            }
        }, WIND_FETCH_INTERVAL_IN_MILLIS);
        scheduler.scheduleFixedPeriod(new RepeatingCommand() {
            @Override
            public boolean execute() {
                updateWindSourcesToObserve(/* runWhenDone */ null);
                return visible;
            }
        }, CHECK_WIND_SOURCE_INTERVAL_IN_MILLIS);
        // Now run things once, first updating the wind sources, then grabbing the wind from those sources:
        updateWindSourcesToObserve(new Runnable() { @Override public void run() { updateWindField(); } });
    }

    private void stopStreamlets() {
        if (swarm != null) {
            this.swarm.stop();
        }
    }
    
    /**
     * Check which wind sources are available for the race identified by {@link #raceIdentifier}. For any wind source
     * not yet observed (contained in the keys of {@link #windInfoForRace}'s
     * {@link WindInfoForRaceDTO#windTrackInfoByWindSource windTrackInfoByWindSource} map, the wind source is added to that map
     * unless it's the {@link WindSourceType#COMBINED} wind source or the wind source is marked as excluded.
     */
    private void updateWindSourcesToObserve(final Runnable runWhenDone) {
        sailingService.getWindSourcesInfo(raceIdentifier, new MarkedAsyncCallback<>(new AsyncCallback<WindInfoForRaceDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.setStatus(stringMessages.errorFetchingWindStreamletData(caught.getMessage()));
            }

            @Override
            public void onSuccess(WindInfoForRaceDTO result) {
                windInfoForRace.raceIsKnownToStartUpwind = result.raceIsKnownToStartUpwind;
                windInfoForRace.windSourcesToExclude = result.windSourcesToExclude;
                for (Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
                    if (!windInfoForRace.windTrackInfoByWindSource.containsKey(e.getKey()) &&
                            !Util.contains(result.windSourcesToExclude, e.getKey()) && e.getKey().getType() != WindSourceType.COMBINED) {
                        windInfoForRace.windTrackInfoByWindSource.put(e.getKey(), e.getValue());
                    }
                }
                if (runWhenDone != null) {
                    runWhenDone.run();
                }
            }
        }));
    }
    
    private void updateWindField() {
        Date timeOfLastFixOfSource = null;
        Set<String> windSourceTypeNames = new HashSet<>();
        for (final Entry<WindSource, WindTrackInfoDTO> e : windInfoForRace.windTrackInfoByWindSource.entrySet()) {
            if (!Util.contains(windInfoForRace.windSourcesToExclude, e.getKey())) {
                windSourceTypeNames.add(e.getKey().getType().name());
                if (e.getValue().windFixes != null && !e.getValue().windFixes.isEmpty()) {
                    // TODO this should better be a per wind source time range; furthermore, only real fixes should be requested / transmitted
                    timeOfLastFixOfSource = new Date(
                            e.getValue().windFixes.get(e.getValue().windFixes.size() - 1).measureTimepoint + 1);
                }
            }
        }
        GetWindInfoAction getWind = new GetWindInfoAction(sailingService, raceIdentifier, timeOfLastFixOfSource,
                /* endOfTime */ null, RESOLUTION_IN_MILLIS, windSourceTypeNames, /* onlyUpToNewestEvent */ true);
        asyncActionsExecutor.execute(getWind, LOAD_WIND_STREAMLET_DATA_CATEGORY,
                new MarkedAsyncCallback<>(new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.setStatus(stringMessages.errorFetchingWindStreamletData(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        updateAverageLatitudeDeg(result);
                        // merge the new wind fixes into the existing WindInfoForRaceDTO structure, updating min/max
                        // confidences
                        for (Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
                            WindTrackInfoDTO windTrackForSource = windInfoForRace.windTrackInfoByWindSource.get(e.getKey());
                            final WindTrackInfoDTO resultWindTrackInfoDTO = result.windTrackInfoByWindSource.get(e.getKey());
                            windTrackForSource.resolutionOutsideOfWhichNoFixWillBeReturned = resultWindTrackInfoDTO.resolutionOutsideOfWhichNoFixWillBeReturned;
                            if (windTrackForSource.windFixes == null) {
                                windTrackForSource.windFixes = resultWindTrackInfoDTO.windFixes;
                            } else {
                                windTrackForSource.windFixes.addAll(resultWindTrackInfoDTO.windFixes);
                            }
                            if (resultWindTrackInfoDTO.maxWindConfidence > windTrackForSource.maxWindConfidence) {
                                windTrackForSource.maxWindConfidence = resultWindTrackInfoDTO.maxWindConfidence;
                            }
                            if (resultWindTrackInfoDTO.minWindConfidence < windTrackForSource.minWindConfidence) {
                                windTrackForSource.minWindConfidence = resultWindTrackInfoDTO.minWindConfidence;
                            }
                        }
                    }
                }));
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (getCanvas() != null) {
            if (isVisible) {
                this.startStreamlets();
                this.visible = isVisible;
            } else {
                this.stopStreamlets();
                this.visible = isVisible;
            }
        }
    }

    @Override
    public void removeFromMap() {
        this.setVisible(false);
    }

    @Override
    protected void drawCenterChanged() {
    }

    public void onBoundsChanged(boolean zoomChanged) {
        if (swarm != null) {
            int swarmPause = (zoomChanged ? 5 : 1);
            swarm.onBoundsChanged(zoomChanged, swarmPause);
        }
    }
}
