package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.Duration;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapEvent;
import com.google.gwt.maps.client.events.bounds.BoundsChangeMapHandler;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoundsUtil;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.simulator.StreamletParameters;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sse.common.ColorMapper;
import com.sap.sse.common.ValueRangeFlexibleBoundaries;
import com.sap.sse.gwt.client.player.TimeListener;

public class Swarm implements TimeListener {
    private final FullCanvasOverlay fullcanvas;
    private final Canvas canvas;
    private final MapWidget map;
    private StreamletParameters parameters;

    private Timer loopTimer;
    private Mercator projection;
    private final VectorField field;

    private boolean zoomChanged = false;
    private Vector diffPx;

    /**
     * The number of particles to show. After {@link #updateBounds()} has been run, this also reflects the size of the
     * {@link #particles} array. Note that since elements in {@link #particles} can be <code>null</code>, this number
     * not necessarily represents the exact number of particles visible.
     */
    private int nParticles;

    /**
     * The particles shown in this swarm. Elements in the array may be <code>null</code>.
     */
    private Particle[] particles;

    /**
     * Tells if nothing of this swarm is currently visible on the {@link #map}. This is the case when there is no
     * intersection between the {@link #field vector field's} {@link VectorField#getFieldCorners() bounds} and the
     * visible map area.
     */
    private boolean swarmOffScreen = false;

    private int swarmPause = 0;

    /**
     * Bounds in the map's coordinate system which may have undergone rotation and translation. See the
     * {@link CoordinateSystem} instance in place which facilitates the mapping.
     */
    private LatLngBounds visibleBoundsOfField;

    private Date timePoint;
    private double cosineOfAverageLatitude;

    private final Map<BoundsChangeMapHandler, HandlerRegistration> boundsChangeHandlers = new HashMap<>();

    private boolean colored = false;
    private final ValueRangeFlexibleBoundaries valueRange;
    private final ColorMapper colorMapper;

    private boolean clearNextFrame = false;
    
    /**
     * For a windfield that has no wind speed spread, e.g. only one wind speed source, this value will be used to define
     * a {@link ValueRangeFlexibleBoundaries} for this swarm.
     */
    private static double WINDSPEED_DEFAULT_SPREAD_IN_KNOTS = 0.1;

    public Swarm(FullCanvasOverlay fullcanvas, MapWidget map, com.sap.sse.gwt.client.player.Timer timer,
            VectorField vectorField, StreamletParameters streamletPars) {
        this.field = vectorField;
        this.fullcanvas = fullcanvas;
        this.canvas = fullcanvas.getCanvas();
        this.map = map;
        timer.addTimeListener(this);
        this.parameters = streamletPars;
        timePoint = timer.getTime();
        cosineOfAverageLatitude = 1.0; // default to equator
        diffPx = new Vector(0, 0);
        valueRange = new ValueRangeFlexibleBoundaries(/* wind speed in knots */ 0.0, /* wind speed in knots */ 60.0,
                /* percentage */ 0.15);
        colorMapper = new ColorMapper(valueRange, !colored);
    }

    public void start(final int animationIntervalMillis) {
        removeBoundsChangeHandler();
        fullcanvas.setCanvasSettings();
        if (loopTimer == null) {
            loopTimer = new com.google.gwt.user.client.Timer() {
                public void run() {
                    updateSwarmOneTick(animationIntervalMillis);
                }
            };
        }

        // the map already exists, ensure swam is started
        if (map.getBounds() != null) {
            startSwarmIfNecessaryAndUpdateProjection();
        }
        // add handler, so the swarm updates itself without calls from the map required
        BoundsChangeMapHandler handler = new BoundsChangeMapHandler() {
            @Override
            public void onEvent(BoundsChangeMapEvent event) {
                startSwarmIfNecessaryAndUpdateProjection();
            }
        };
        boundsChangeHandlers.put(handler, map.addBoundsChangeHandler(handler));
        // run timer as soon as possible for the first frame
        loopTimer.schedule(0);
    }

    private void startSwarmIfNecessaryAndUpdateProjection() {
        if (projection == null) {
            projection = new Mercator(fullcanvas, map);
        }
        // ensure projection fits the map
        projection.calibrate();
        // ensure canvas fits the map
        updateBounds();
        if (particles == null) {
            // if this is the first start, also createParticles
            createParticles();
        } else {
            // clear all tails, as else there will be long smears over the map
            clearNextFrame = true;
        }

        // start the timer that will update the swarm if not running
    }

    private void updateSwarmOneTick(int animationIntervalMillis) {
        double time0 = Duration.currentTimeMillis();
        // upon zooming the swarm is shortly paused, to ensure no strange rendering during the css zoom
        // animations
        if (swarmPause > 1) {
            swarmPause--;
        } else if (swarmPause == 1) {
            // ensure bounds and projections are up to date
            startSwarmIfNecessaryAndUpdateProjection();
            if (zoomChanged) {
                // ensure amount of particles is updated
                diffPx = new Vector(0, 0);
                recycleParticles();
                zoomChanged = false;
            } else {
                // process the offset
                diffPx = fullcanvas.getDiffPx();
            }
            swarmPause = 0;
        }
        // update the swarm if it is not paused
        if ((!swarmOffScreen) && (swarmPause == 0)) {
            execute(diffPx);
            diffPx = new Vector(0, 0);
        }
        double time1 = Duration.currentTimeMillis();
        // wait at least 10ms for the next iteration; try to get one iteration done every
        // animationIntervalMillis if possible
        double timeDelta = time1 - time0;
        // log("fps: "+(1000.0/timeDelta));
        loopTimer.schedule((int) Math.max(10, animationIntervalMillis - timeDelta));
    }

    private void removeBoundsChangeHandler() {
        for (HandlerRegistration reg : boundsChangeHandlers.values()) {
            reg.removeHandler();
        }
        boundsChangeHandlers.clear();

    }

    public void stop() {
        removeBoundsChangeHandler();
        projection.clearCanvas();
        loopTimer.cancel();
    }

    private Particle recycleOrCreateParticle(Particle particle) {
        boolean done = false;
        int attempts = 10;
        // try a few times to create a particle at a random position until the weight is high enough for it to be
        // displayed
        while (!done && attempts-- > 0) {
            LatLng newRandomPosition = getRandomPosition();
            if (field.inBounds(newRandomPosition)) {
                Vector v = field.getVector(newRandomPosition, timePoint);
                double weight = field.getParticleWeight(newRandomPosition, v);
                if (weight >= Math.random()) {
                    if (particle == null) {
                        particle = new Particle();
                    }
                    particle.currentPosition = newRandomPosition;
                    if (v == null || v.length() == 0) {
                        particle.stepsToLive = 0;
                    } else {
                        particle.stepsToLive = 1 + (int) Math.round(Math.random() * 40);
                    }
                    particle.currentPixelCoordinate = projection.latlng2pixel(particle.currentPosition);
                    particle.previousPixelCoordinate = particle.currentPixelCoordinate;
                    particle.v = v;
                    done = true;
                } else {
                    particle = null;
                }
            } else {
                particle = null; // out of bounds
            }
        }
        return particle;
    }

    private LatLng getRandomPosition() {
        final LatLng result;
        double rndY = Math.random();
        double rndX = Math.random();
        double latDeg = rndY * this.visibleBoundsOfField.getSouthWest().getLatitude()
                + (1 - rndY) * this.visibleBoundsOfField.getNorthEast().getLatitude();
        double lngDeg = rndX * this.visibleBoundsOfField.getSouthWest().getLongitude()
                + (1 - rndX) * this.visibleBoundsOfField.getNorthEast().getLongitude();
        result = LatLng.newInstance(latDeg, lngDeg);
        return result;
    }

    private void recycleParticles() {
        for (int idx = 0; idx < particles.length; idx++) {
            particles[idx] = this.recycleOrCreateParticle(particles[idx]);
        }
    }

    private void createParticles() {
        this.particles = new Particle[nParticles];
        recycleParticles();
    }

    public void onBoundsChanged(boolean zoomChanged, int swarmPause) {
        this.zoomChanged |= zoomChanged;
        if (this.zoomChanged) {
            projection.clearCanvas();
        } else {
            fullcanvas.setCanvasSettings();
        }
        this.swarmPause = swarmPause;
    }

    private void updateBounds() {
        LatLngBounds fieldBounds = this.field.getFieldCorners();
        final LatLngBounds mapBounds = map.getBounds();
        swarmOffScreen = !fieldBounds.intersects(mapBounds);
        visibleBoundsOfField = BoundsUtil.intersect(fieldBounds, mapBounds);
        Vector boundsSWpx = this.projection.latlng2pixel(visibleBoundsOfField.getSouthWest());
        Vector boundsNEpx = this.projection.latlng2pixel(visibleBoundsOfField.getNorthEast());
        double boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
        double boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);
        int newNParticles = (int) Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.getParticleFactor()
                * this.parameters.swarmScale);
        if (newNParticles > this.nParticles) {
            this.nParticles = newNParticles;
            createParticles();
        }
        this.nParticles = newNParticles;
        cosineOfAverageLatitude = Math.cos((visibleBoundsOfField.getSouthWest().getLatitude() / 180. * Math.PI
                + visibleBoundsOfField.getNorthEast().getLatitude() / 180. * Math.PI) / 2);
    }

    private void drawSwarm() {
        Context2d ctxt = canvas.getContext2d();
        // clearing neds to be done with a little over zero, thanks IE
        ctxt.setGlobalAlpha(0.06);
        if (clearNextFrame) {
            // skip this frame, as it will contain an extreme delta due to the map movement
            clearNextFrame = false;
        } else {
            ctxt.setGlobalCompositeOperation("destination-out");
            ctxt.setFillStyle("black");
            ctxt.fillRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());
            ctxt.setGlobalAlpha(1.0);
            ctxt.setGlobalCompositeOperation("source-over");
            ctxt.setFillStyle("white");
            for (int idx = 0; idx < nParticles && idx < particles.length; idx++) {
                Particle particle = particles[idx];
                if (particle == null || particle.stepsToLive == 0) {
                    continue;
                }
                double particleSpeed = particle.v == null ? 0 : particle.v.length();
                ctxt.setLineWidth(field.getLineWidth(particleSpeed));
                ctxt.setStrokeStyle(colorMapper.getColor(particleSpeed));
                ctxt.beginPath();
                ctxt.moveTo(particle.previousPixelCoordinate.x, particle.previousPixelCoordinate.y);
                ctxt.lineTo(particle.currentPixelCoordinate.x, particle.currentPixelCoordinate.y);
                ctxt.stroke();
            }
        }
    }

    public void setColors(boolean isColored) {
        this.colored = isColored;
        colorMapper.setGrey(!isColored);
    }

    public boolean isColored() {
        return colored;
    }

    /**
     * Moves each particle by its vector {@link Particle#v} multiplied by the speed which is 0.01 times the
     * {@link VectorField#getMotionScale(int)} at the map's current zoom level.
     */
    private boolean execute(Vector diffPx) {
        double minSpeedInKnots = 120.0;
        double maxSpeedInKnots = 0.0;
        double speed = field.getMotionScale(map.getZoom());
        for (int idx = 0; idx < particles.length && idx < nParticles; idx++) {
            Particle particle = particles[idx];
            if (particle != null && particle.stepsToLive > 0 && particle.v != null) {
                // move the particle one step in the direction and with the speed indicated by particle.v and
                // update its currentPosition, currentPixelCoordinate and previousPixelCoordinate fields;
                // also, its particle.v field is updated based on its new position from the vector field
                particle.previousPixelCoordinate = particle.currentPixelCoordinate;
                if (diffPx.x != 0) {
                    particle.previousPixelCoordinate.x += diffPx.x;
                }
                if (diffPx.y != 0) {
                    particle.previousPixelCoordinate.y += diffPx.y;
                }
                double latDeg = particle.currentPosition.getLatitude() + speed * particle.v.y;
                double lngDeg = particle.currentPosition.getLongitude()
                        + speed * particle.v.x / cosineOfAverageLatitude;
                particle.currentPosition = LatLng.newInstance(latDeg, lngDeg);
                particle.currentPixelCoordinate = projection.latlng2pixel(particle.currentPosition);
                particle.stepsToLive--;
                if ((particle.stepsToLive > 0) && (this.field.inBounds(particle.currentPosition))) {
                    particle.v = field.getVector(particle.currentPosition, timePoint);
                } else {
                    particle.v = null;
                }
            } else {
                // particle timed out (age became 0) or was never created (e.g., weight too low); try to create a new
                // one
                particles[idx] = this.recycleOrCreateParticle(particles[idx]);
            }
            if (particles[idx] != null && particles[idx].v != null) {
                final double length = particles[idx].v.length();
                if (length > maxSpeedInKnots) {
                    maxSpeedInKnots = length;
                }
                if (length < minSpeedInKnots) {
                    minSpeedInKnots = length;
                }
            }
        }
        if (minSpeedInKnots <= maxSpeedInKnots) {
            if (Math.abs(maxSpeedInKnots - minSpeedInKnots) <= WINDSPEED_DEFAULT_SPREAD_IN_KNOTS) {
                valueRange.setMinMax(minSpeedInKnots - WINDSPEED_DEFAULT_SPREAD_IN_KNOTS, maxSpeedInKnots + WINDSPEED_DEFAULT_SPREAD_IN_KNOTS);
            } else {
                valueRange.setMinMax(minSpeedInKnots, maxSpeedInKnots);
            }
        }
        drawSwarm();
        return true;
    }

    public ValueRangeFlexibleBoundaries getValueRange() {
        return valueRange;
    }

    public ColorMapper getColorMapper() {
        return colorMapper;
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        timePoint = newTime;
    }

    public void pause(int swarmPause) {
        this.swarmPause = swarmPause;
    }
}
