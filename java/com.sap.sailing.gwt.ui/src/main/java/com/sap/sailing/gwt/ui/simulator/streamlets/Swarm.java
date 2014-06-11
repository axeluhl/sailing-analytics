package com.sap.sailing.gwt.ui.simulator.streamlets;

import java.util.Date;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.simulator.WindStreamletsCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sse.common.Util.Pair;

public class Swarm {
    private final FullCanvasOverlay fullcanvas;
    private final Canvas canvas;
    private final MapWidget map;

    private Timer loopTimer;
    private Mercator projection;
    private VectorField field;
    private int nParticles;
    private Particle[] particles;
    private boolean swarmOffScreen = false;
    private int swarmPause = 0;
    private boolean swarmContinue = true;
    private Position visNE;
    private Position visSW;

    public Swarm(FullCanvasOverlay fullcanvas, MapWidget map) {
        this.fullcanvas = fullcanvas;
        this.canvas = fullcanvas.getCanvas();
        this.map = map;
    }

    public void start(int animationIntervalMillis, WindFieldDTO windField) {
        projection = new Mercator(fullcanvas, map);
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
            Vector v = field.getVector(particle.currentPosition);
            double weight = field.particleWeight(particle.currentPosition, v);
            if (weight >= Math.random()) {
                if (v == null || v.length() == 0) {
                    particle.stepsToLive = 0;
                } else {
                    particle.stepsToLive = 1 + (int) Math.round(Math.random() * 40);
                }
                particle.currentPixelCoordinate = projection.latlng2pixel(particle.currentPosition);
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
        double latDeg = rndY * this.visSW.getLatDeg() + (1 - rndY) * this.visNE.getLatDeg();
        double lngDeg = rndX * this.visSW.getLngDeg() + (1 - rndX) * this.visNE.getLngDeg();
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
        Position mapNE = new DegreePosition(map.getBounds().getNorthEast().getLatitude(), map.getBounds()
                .getNorthEast().getLongitude());
        Position mapSW = new DegreePosition(map.getBounds().getSouthWest().getLatitude(), map.getBounds()
                .getSouthWest().getLongitude());
        Position[] fieldCorners = this.field.getFieldCorners();
        Position fieldNE = fieldCorners[1];
        Position fieldSW = fieldCorners[0];
        Pair<Boolean, Boolean> visibleNE = this.isVisible(fieldNE);
        Pair<Boolean, Boolean> visibleSW = this.isVisible(fieldSW);
        boolean useBoundsNorth = visibleNE.getB();
        boolean useBoundsEast = visibleNE.getA();
        boolean useBoundsSouth = visibleSW.getB();
        boolean useBoundsWest = visibleSW.getA();
        swarmOffScreen = !visibleNE.getB() || !visibleNE.getA();
        final Position boundsNE;
        final Position boundsSW;
        if (swarmOffScreen) {
            boundsNE = fieldNE;
            boundsSW = fieldSW;
        } else {
            if ((!useBoundsNorth) && (!useBoundsEast)) {
                boundsNE = mapNE;
            } else if (!useBoundsNorth) {
                boundsNE = new DegreePosition(mapNE.getLatDeg(), fieldNE.getLngDeg());
            } else if (!useBoundsEast) {
                boundsNE = new DegreePosition(fieldNE.getLatDeg(), mapNE.getLngDeg());
            } else {
                boundsNE = fieldNE;
            }
            if ((!useBoundsSouth) && (!useBoundsWest)) {
                boundsSW = mapSW;
            } else if (!useBoundsSouth) {
                boundsSW = new DegreePosition(mapSW.getLatDeg(), fieldSW.getLngDeg());
            } else if (!useBoundsWest) {
                boundsSW = new DegreePosition(fieldSW.getLatDeg(), mapSW.getLngDeg());
            } else {
                boundsSW = fieldSW;
            }
        }
        this.setVisSW(boundsSW);
        this.setVisNE(boundsNE);
        Vector boundsSWpx = this.projection.latlng2pixel(boundsSW);
        Vector boundsNEpx = this.projection.latlng2pixel(boundsNE);
        double boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
        double boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);
        this.nParticles = (int) Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.getParticleFactor());
    };

    private void setVisNE(Position visNE) {
        this.visNE = visNE;
    }

    private void setVisSW(Position visSW) {
        this.visSW = visSW;
    }

    /**
     * Tells if <code>pos</code> is within the bounds of {@link #canvas}, for X and Y coordinate.
     */
    private Pair<Boolean, Boolean> isVisible(Position pos) {
        // test for visibility of swarm
        Vector proj = this.projection.latlng2pixel(pos);
        final boolean xVisible = proj.x >= 0 && proj.x <= canvas.getOffsetWidth();
        final boolean yVisible = proj.y >= 0 && proj.y <= canvas.getOffsetHeight();
        return new Pair<Boolean, Boolean>(xVisible, yVisible);
    }

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
            double particleSpeed = particle.v.length();
            ctxt.setLineWidth(field.lineWidth(particleSpeed));
            ctxt.setStrokeStyle(field.getColor(particleSpeed));
            ctxt.beginPath();
            ctxt.moveTo(particle.previousPixelCoordinate.x, particle.previousPixelCoordinate.y);
            ctxt.lineTo(particle.currentPixelCoordinate.x, particle.currentPixelCoordinate.y);
            ctxt.stroke();
        }
    }

    /**
     * Moves each particle by its vector {@link Particle#v} multiplied by the speed which is 0.01 times the
     * {@link VectorField#motionScale(int)} at the map's current zoom level.
     */
    private boolean execute() {
        double speed = 0.01 * field.motionScale(map.getZoom());
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
                    particle.v = field.getVector(particle.currentPosition);
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
}
