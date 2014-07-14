package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

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
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.simulator.StreamletParameters;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
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
 * @author Nidhi Sawhney (D054070)
 * @author Axel Uhl (D043530)
 * 
 */
public class WindStreamletsRaceboardOverlay extends FullCanvasOverlay {
    public static final String LODA_WIND_STREAMLET_DATA_CATEGORY = "loadWindStreamletData";
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
            AsyncActionsExecutor asyncActionsExecutor, StringMessages stringMessages) {
        super(map, zIndex);
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
        this.windField = new WindInfoForRaceVectorField(windInfoForRace);
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
                    latitudeSum += wind.position.latDeg;
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
     * @param runWhenDone TODO
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
        Date beginningOfTime = null;
        Date endOfTime = null;
        for (final Entry<WindSource, WindTrackInfoDTO> e : windInfoForRace.windTrackInfoByWindSource.entrySet()) {
            if (!Util.contains(windInfoForRace.windSourcesToExclude, e.getKey())) {
                final Date timeOfLastFixOfSource = (e.getValue().windFixes != null && !e.getValue().windFixes.isEmpty())
                        ? new Date(e.getValue().windFixes.get(e.getValue().windFixes.size()-1).measureTimepoint+1)
                        : beginningOfTime;
                GetWindInfoAction getWind = new GetWindInfoAction(sailingService, raceIdentifier, timeOfLastFixOfSource, endOfTime,
                        RESOLUTION_IN_MILLIS, Arrays.asList(new String[] { e.getKey().getType().name() }), /* onlyUpToNewestEvent */ true);
                asyncActionsExecutor.execute(getWind, LODA_WIND_STREAMLET_DATA_CATEGORY+" "+e.getKey().name(), new MarkedAsyncCallback<>(
                        new AsyncCallback<WindInfoForRaceDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Window.setStatus(stringMessages.errorFetchingWindStreamletData(caught.getMessage()));
                            }

                            @Override
                            public void onSuccess(WindInfoForRaceDTO result) {
                                updateAverageLatitudeDeg(result);
                                // merge the new wind fixes into the existing WindInfoForRaceDTO structure, updating min/max confidences
                                if (e.getValue().windFixes == null) {
                                    e.getValue().windFixes = result.windTrackInfoByWindSource.get(e.getKey()).windFixes;
                                } else {
                                    e.getValue().windFixes.addAll(result.windTrackInfoByWindSource.get(e.getKey()).windFixes);
                                }
                                if (result.windTrackInfoByWindSource.get(e.getKey()).maxWindConfidence > e.getValue().maxWindConfidence) {
                                    e.getValue().maxWindConfidence = result.windTrackInfoByWindSource.get(e.getKey()).maxWindConfidence;
                                }
                                if (result.windTrackInfoByWindSource.get(e.getKey()).minWindConfidence < e.getValue().minWindConfidence) {
                                    e.getValue().minWindConfidence = result.windTrackInfoByWindSource.get(e.getKey()).minWindConfidence;
                                }
                            }
                        }));
            }
        }
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

    public void onBoundsChanged() {
        if (swarm != null) {
            swarm.onBoundsChanged();
        }
    }
}
