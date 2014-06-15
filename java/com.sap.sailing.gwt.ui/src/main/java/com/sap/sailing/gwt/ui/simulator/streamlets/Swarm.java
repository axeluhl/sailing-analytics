package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.domain.common.Bounds;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.BoundsImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.simulator.WindStreamletsCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sse.gwt.client.player.TimeListener;

public class Swarm implements TimeListener {
    private final FullCanvasOverlay fullcanvas;
    private final Canvas canvas;
    private final MapWidget map;

    private Timer loopTimer;
    private Mercator projection;
    private VectorField field;
    
    /**
     * The number of particles to show. After {@link #updateBounds()} has been run, this also reflects the size of the
     * {@link #particles} array.
     */
    private int nParticles;
    
    /**
     * The particles shown in this swarm.
     */
    private Particle[] particles;
    
    /**
     * Tells if nothing of this swarm is currently visible on the {@link #map}. This is the case when there is no
     * intersection between the {@link #field vector field's} {@link VectorField#getFieldCorners() bounds} and the
     * visible map area.
     */
    private boolean swarmOffScreen = false;
    
    private int swarmPause = 0;
    private boolean swarmContinue = true;
    private Bounds visibleBoundsOfField;
    private Date timePoint;

    public Swarm(FullCanvasOverlay fullcanvas, MapWidget map, com.sap.sse.gwt.client.player.Timer timer) {
        this.fullcanvas = fullcanvas;
        this.canvas = fullcanvas.getCanvas();
        this.map = map;
        timer.addTimeListener(this);
        timePoint = timer.getTime();
    }

    public void start(int animationIntervalMillis, WindFieldDTO windField) {
        projection = new Mercator(fullcanvas, map);
        // TODO make the VectorField a parameter of Swarm
        if (windField == null) {
            SimulatorJSBundle bundle = GWT.create(SimulatorJSBundle.class);
            String jsonStr = bundle.windStreamletsDataJS().getText();
            RectField f = RectField.read(jsonStr.substring(19, jsonStr.length() - 1), false);
            field = f;
            map.setZoom(5);
            map.panTo(f.getCenter());
            projection.calibrate();
        } else {
            field = new SimulatorField(((WindStreamletsCanvasOverlay) fullcanvas).getWindFieldDTO(),
                    ((WindStreamletsCanvasOverlay) fullcanvas).getWindParams());
            fullcanvas.setCanvasSettings();
            projection.calibrate();
        }
        this.updateBounds();
        Context2d ctxt = canvas.getContext2d();
        ctxt.setFillStyle("red");
        particles = this.createParticles();
        this.swarmContinue = true;
        startLoop(animationIntervalMillis);
    }

    public void stop() {
        this.swarmContinue = false;
    }

    private Particle createParticle() {
        Particle particle = new Particle();
        boolean done = false;
        // try to create a particle at a random position until the weight is high enough for it to be displayed
        while (!done) {
            particle.currentPosition = getRandomPosition();
            Vector v = field.getVector(particle.currentPosition, timePoint);
            double weight = field.getParticleWeight(particle.currentPosition, v);
            if (weight >= Math.random()) {
                if (v == null || v.length() == 0) {
                    particle.stepsToLive = 0;
                } else {
                    particle.stepsToLive = 1 + (int) Math.round(Math.random() * 40);
                }
                particle.currentPixelCoordinate = projection.latlng2pixel(particle.currentPosition);
                particle.previousPixelCoordinate = particle.currentPixelCoordinate;
                particle.v = v;
                done = true;
            }
        }
        return particle;
    }
    
    private Position getRandomPosition() {
        final Position result;
        double rndY = Math.random();
        double rndX = Math.random();
        double latDeg = rndY * this.visibleBoundsOfField.getSouthWest().getLatDeg() + (1 - rndY) * this.visibleBoundsOfField.getNorthEast().getLatDeg();
        double lngDeg = rndX * this.visibleBoundsOfField.getSouthWest().getLngDeg() + (1 - rndX) * this.visibleBoundsOfField.getNorthEast().getLngDeg();
        result = new DegreePosition(latDeg, lngDeg);
        return result;
    }

    private Particle[] createParticles() {
        Particle[] newParticles = new Particle[nParticles];
        for (int idx = 0; idx < newParticles.length; idx++) {
            newParticles[idx] = this.createParticle();
        }
        return newParticles;
    }

    public void onBoundsChanged() {
        projection.clearCanvas();
        swarmPause = 5;
    }

    private void updateBounds() {
        Bounds fieldBounds = this.field.getFieldCorners();
        final BoundsImpl mapBounds = new BoundsImpl(
                new DegreePosition(map.getBounds().getSouthWest().getLatitude(), map.getBounds().getSouthWest().getLongitude()),
                new DegreePosition(map.getBounds().getNorthEast().getLatitude(), map.getBounds().getNorthEast().getLongitude()));
        swarmOffScreen = !fieldBounds.intersects(mapBounds);
        visibleBoundsOfField = fieldBounds.intersect(mapBounds);
        Vector boundsSWpx = this.projection.latlng2pixel(visibleBoundsOfField.getSouthWest());
        Vector boundsNEpx = this.projection.latlng2pixel(visibleBoundsOfField.getNorthEast());
        double boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
        double boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);
        this.nParticles = (int) Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.getParticleFactor());
    };

    private void startLoop(final int animationIntervalMillis) {
        // Create animation-loop based on timer timeout
        loopTimer = new com.google.gwt.user.client.Timer() {
            public void run() {
                Date time0 = new Date();
                if (swarmPause > 1) {
                    swarmPause--;
                } else if (swarmPause == 1) {
                    fullcanvas.setCanvasSettings();
                    projection.calibrate();
                    updateBounds();
                    particles = createParticles();
                    swarmPause = 0;
                }
                if ((!swarmOffScreen) && (swarmPause == 0)) {
                    execute();
                }
                Date time1 = new Date();
                if (swarmContinue) {
                    // wait at least 10ms for the next iteration; try to get one iteration done every animationIntervalMillis if possible
                    loopTimer.schedule((int) Math.max(10, animationIntervalMillis - (time1.getTime() - time0.getTime())));
                } else {
                    projection.clearCanvas();
                }
            }
        };
        loopTimer.schedule(animationIntervalMillis);
    }

    private void drawSwarm() {
        Context2d ctxt = canvas.getContext2d();
        ctxt.setGlobalAlpha(0.08);
        ctxt.setGlobalCompositeOperation("destination-out");
        ctxt.setFillStyle("black");
        ctxt.fillRect(0, 0, canvas.getOffsetWidth(), canvas.getOffsetHeight());
        ctxt.setGlobalAlpha(1.0);
        ctxt.setGlobalCompositeOperation("source-over");
        ctxt.setFillStyle("white");
        for (int idx = 0; idx < particles.length; idx++) {
            Particle particle = particles[idx];
            if (particle.stepsToLive == 0) {
                continue;
            }
            double particleSpeed = particle.v == null ? 0 : particle.v.length();
            ctxt.setLineWidth(field.getLineWidth(particleSpeed));
            ctxt.setStrokeStyle(field.getColor(particleSpeed));
            ctxt.beginPath();
            ctxt.moveTo(particle.previousPixelCoordinate.x, particle.previousPixelCoordinate.y);
            ctxt.lineTo(particle.currentPixelCoordinate.x, particle.currentPixelCoordinate.y);
            ctxt.stroke();
        }
    }

    /**
     * Moves each particle by its vector {@link Particle#v} multiplied by the speed which is 0.01 times the
     * {@link VectorField#getMotionScale(int)} at the map's current zoom level.
     */
    private boolean execute() {
        double speed = 0.01 * field.getMotionScale(map.getZoom());
        for (int idx = 0; idx < particles.length; idx++) {
            Particle particle = particles[idx];
            if ((particle.stepsToLive > 0) && (particle.v != null)) {
                // move the particle one step in the direction and with the speed indicated by particle.v and
                // update its currentPosition, currentPixelCoordinate and previousPixelCoordinate fields;
                // also, its particle.v field is updated based on its new position from the vector field
                particle.previousPixelCoordinate = particle.currentPixelCoordinate;
                double latDeg = particle.currentPosition.getLatDeg() + speed * particle.v.y;
                double lngDeg = particle.currentPosition.getLngDeg() + speed * particle.v.x;
                particle.currentPosition = new DegreePosition(latDeg, lngDeg);
                particle.currentPixelCoordinate = projection.latlng2pixel(particle.currentPosition);
                particle.stepsToLive--;
                if ((particle.stepsToLive > 0) && (this.field.inBounds(particle.currentPosition))) {
                    particle.v = field.getVector(particle.currentPosition, timePoint);
                } else {
                    particle.v = null;
                }
            } else {
                // particle timed out (age became 0); create a new one
                particles[idx] = this.createParticle();
            }
        }
        drawSwarm();
        return true;
    }

    public VectorField getField() {
        return field;
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        timePoint = newTime;
    }
}
