package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
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
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
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
    private Canvas streamletLegend;
    private boolean firstColoring = true;
    
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
        createStreamletLegend(map);
        canvas.getElement().getStyle().setBorderColor("BLUE");
        canvas.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        canvas.getElement().getStyle().setBorderWidth(10, Unit.PX);

        Event.sinkEvents(canvas.getElement(), Event.ONCHANGE);
        Event.setEventListener(canvas.getElement(), new EventListener() {

            @Override
            public void onBrowserEvent(Event event) {
                GWT.log("Event: " + event.getType());
            }
        });
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
    
    private void createStreamletLegend(MapWidget map) {
        streamletLegend = Canvas.createIfSupported();
        streamletLegend.addStyleName("MapStreamletLegend");
        //streamletLegend.setTitle(stringMessages.simulationLegendTooltip());
        /*streamletLegend.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                int clickPixelY = event.getRelativeY(streamletLegend.getElement());
                int legendRow = clickPixelY / ((int) rectHeight);
                int pathRow = legendRow - (racePath!=null ? 1 : 0);
                //Window.alert("clickPixelY: " + clickPixelY + "\nlegendRow: " + legendRow);
                visiblePaths[pathRow] = !visiblePaths[pathRow];
                clearCanvas();
                drawPaths();
            }
        });*/
        map.setControls(ControlPosition.LEFT_BOTTOM, streamletLegend);
        int canvasWidth = 500;
        int canvasHeight = 60;
        streamletLegend.getParent().addStyleName("MapStreamletLegendParentDiv");
        streamletLegend.setWidth(String.valueOf(canvasWidth));
        streamletLegend.setHeight(String.valueOf(canvasHeight));
        streamletLegend.setCoordinateSpaceWidth(canvasWidth);
        streamletLegend.setCoordinateSpaceHeight(canvasHeight);    }

    private void drawLegend() {
        double x, y;
        x=100;
        y=16;
        double w = 1;
        double h = 20;
        int maxIdx = 300;
        Context2d context2d = streamletLegend.getContext2d();
        context2d.setFillStyle("rgba(0,0,0,.3)");
        context2d.setLineWidth(1.0);
        context2d.beginPath();
        context2d.fillRect(x-7.0, y-16.0, w*maxIdx+15.0, 56.0);
        context2d.closePath();
        context2d.stroke();
        context2d.setFillStyle("white");
        String label = stringMessages.windSpeedInKnots();
        TextMetrics txtmet;
        txtmet = context2d.measureText(label);
        context2d.fillText(label, x + (w*maxIdx - txtmet.getWidth())/2.0, y - 5.0);
        for(int idx=0; idx <= maxIdx; idx++) {
            //double speed = idx * 24.0 / maxIdx;
            double speed = 4.0 + idx * 16.0 / maxIdx;
            context2d.setFillStyle(windField.getColor(speed));
            context2d.beginPath();
            context2d.fillRect(x + idx*w, y, w, h);
            context2d.closePath();
            context2d.stroke();
            if (Math.abs(speed % 2.0) < (12.0/maxIdx)) {
                context2d.setStrokeStyle("white");
                context2d.setLineWidth(1.0);
                context2d.beginPath();
                context2d.moveTo(x + idx*w, y + h);
                context2d.lineTo(x + idx*w, y + h + 7.0);
                context2d.closePath();
                context2d.stroke();
                context2d.setFillStyle("white");
                label = ""+Math.round(speed);
                txtmet = context2d.measureText(label);
                context2d.fillText(label, x + idx*w - txtmet.getWidth()/2.0, y + h + 8.0 + 8.0);
            }
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
                        Notification.notify(stringMessages.errorFetchingWindStreamletData(caught.getMessage()),
                                NotificationType.WARNING);
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
                        Notification.notify(stringMessages.errorFetchingWindStreamletData(caught.getMessage()),
                                NotificationType.WARNING);
                    }

                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        updateAverageLatitudeDeg(result);
                        // merge the new wind fixes into the existing WindInfoForRaceDTO structure, updating min/max
                        // confidences
                        for (Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
                            WindTrackInfoDTO windTrackForSource = windInfoForRace.windTrackInfoByWindSource.get(e.getKey());
                            if (windTrackForSource != null) {
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
                if (this.windField.getColors()) {
                    this.streamletLegend.setVisible(true);
                }
                this.startStreamlets();
                this.visible = isVisible;
            } else {
                if (this.windField.getColors()) {
                    this.streamletLegend.setVisible(false);
                }
                this.stopStreamlets();
                this.visible = isVisible;
            }
        }
    }

    public void setColors(boolean isColored) {
        this.windField.setColors(isColored);
        if ((isColored) && (firstColoring)) {
            firstColoring = false;
            this.drawLegend();
        }
        this.streamletLegend.setVisible(isColored);
    }
    
    @Override
    public void removeFromMap() {
        this.setVisible(false);
    }

    @Override
    protected void drawCenterChanged() {
    }

    @Override
    protected void setCanvasPosition(double x, double y) {
        GWT.log("moving windstreamcanvas to " + x + "/" + y);
        super.setCanvasPosition(x, y);
    }

    public void onDragEnd() {
        // Scheduler.get().scheduleDeferred(() -> {
        // GWT.log("setting canvas settings...");
        // setCanvasSettings();
        // GWT.log("set canvas settings.");
        // });
    }

    public void onBoundsChanged(boolean zoomChanged) {
        if (swarm != null) {
            int swarmPause = (zoomChanged ? 5 : 1);
            swarm.onBoundsChanged(zoomChanged, swarmPause);
        }
    }
}
