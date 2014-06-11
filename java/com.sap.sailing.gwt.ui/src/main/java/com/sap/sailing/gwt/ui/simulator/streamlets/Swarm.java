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
    private Position boundsNE;
    private Position boundsSW;
    private Position visNE;
    private boolean visFull;
    private Position visSW;

    public Swarm(FullCanvasOverlay fullcanvas, MapWidget map) {
        this.fullcanvas = fullcanvas;
        this.canvas = fullcanvas.getCanvas();
        this.map = map;
    }

    public void start(int animationDuration, WindFieldDTO windField) {
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
        startLoop(animationDuration);
    }

    public void stop() {
        this.swarmContinue = false;
    }

    private Particle createParticle() {
        Particle particle = new Particle();
        boolean done = false;
        while (!done) {
            particle.pos = getRandomPosition();
            Vector v = field.getVector(particle.pos);
            double weight = field.particleWeight(particle.pos, v);
            if (weight >= Math.random()) {
                if (v.length() == 0) {
                    particle.age = 0;
                } else {
                    particle.age = 1 + (int) Math.round(Math.random() * 40);
                }
                particle.pxOld = projection.latlng2pixel(particle.pos);
                particle.speed = 0;
                particle.v = v;
                done = true;
            }
        }
        return particle;
    }
    
    private Position getRandomPosition() {
        final Position result;
        if (isVisFull()) {
            double rndY = Math.random();
            double rndX = Math.random();
            double latDeg = rndY * this.visSW.getLatDeg() + (1 - rndY) * this.visNE.getLatDeg();
            double lngDeg = rndX * this.visSW.getLngDeg() + (1 - rndX) * this.visNE.getLngDeg();
            result = new DegreePosition(latDeg, lngDeg);
        } else {
            double rndY = Math.random();
            double rndX = Math.random() - 0.5;
            result = field.getInnerPosition(rndX, rndY, isVisFull());
        }
        return result;
    }


    private boolean isVisFull() {
        return visFull;
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
        Position[] fieldCorners = this.field.getFieldCorners(visFull);
        Position fieldNE = fieldCorners[1];
        Position fieldSW = fieldCorners[0];
        Vector visibleNE = this.isVisible(fieldNE);
        Vector visibleSW = this.isVisible(fieldSW);
        boolean useBoundsNorth = (visibleNE.y == 0);
        boolean useBoundsEast = (visibleNE.x == 0);
        boolean useBoundsSouth = (visibleSW.y == 0);
        boolean useBoundsWest = (visibleSW.x == 0);
        swarmOffScreen = (visibleNE.y > 0) || (visibleSW.y < 0) || (visibleNE.x < 0) || (visibleSW.x > 0);
        if (swarmOffScreen) {
            this.boundsNE = fieldNE;
            this.boundsSW = fieldSW;
        } else {
            if ((!useBoundsNorth) && (!useBoundsEast)) {
                this.boundsNE = mapNE;
            } else if (!useBoundsNorth) {
                this.boundsNE = new DegreePosition(mapNE.getLatDeg(), fieldNE.getLngDeg());
            } else if (!useBoundsEast) {
                this.boundsNE = new DegreePosition(fieldNE.getLatDeg(), mapNE.getLngDeg());
            } else {
                this.boundsNE = fieldNE;
            }
            if ((!useBoundsSouth) && (!useBoundsWest)) {
                this.boundsSW = mapSW;
            } else if (!useBoundsSouth) {
                this.boundsSW = new DegreePosition(mapSW.getLatDeg(), fieldSW.getLngDeg());
            } else if (!useBoundsWest) {
                this.boundsSW = new DegreePosition(fieldSW.getLatDeg(), mapSW.getLngDeg());
            } else {
                this.boundsSW = fieldSW;
            }
        }
        this.setVisSW(this.boundsSW);
        this.setVisNE(this.boundsNE);
        this.setVisFullCanvas((!useBoundsNorth) && (!useBoundsEast) && (!useBoundsSouth) && (!useBoundsWest));
        Vector boundsSWpx = this.projection.latlng2pixel(this.boundsSW);
        Vector boundsNEpx = this.projection.latlng2pixel(this.boundsNE);
        double boundsWidthpx = Math.abs(boundsNEpx.x - boundsSWpx.x);
        double boundsHeightpx = Math.abs(boundsSWpx.y - boundsNEpx.y);
        this.nParticles = (int) Math.round(Math.sqrt(boundsWidthpx * boundsHeightpx) * this.field.getParticleFactor());
    };

    private void setVisFullCanvas(boolean visFull) {
        this.visFull = visFull;
    }

    private void setVisNE(Position visNE) {
        this.visNE = visNE;
    }

    private void setVisSW(Position visSW) {
        this.visSW = visSW;
    }

    private Vector isVisible(Position pos) {
        // test for visibility of swarm
        Vector proj = this.projection.latlng2pixel(pos);
        Vector result = new Vector();
        result.x = (proj.x < 0 ? -1 : 0) + (proj.x > canvas.getOffsetWidth() ? 1 : 0);
        result.y = (proj.y < 0 ? -1 : 0) + (proj.y > canvas.getOffsetHeight() ? 1 : 0);
        return result;
    }

    private void startLoop(final int millis) {
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
                // console("swarmOffScreen:"+swarmOffScreen);
                if ((!swarmOffScreen) && (swarmPause == 0)) {
                    execute();
                }
                Date time1 = new Date();
                // console("delta:"+(time1.getTime()-time0.getTime())+"/"+millis);
                if (swarmContinue) {
                    loopTimer.schedule((int) Math.max(10, millis - (time1.getTime() - time0.getTime())));
                } else {
                    projection.clearCanvas();
                }
            }
        };
        loopTimer.schedule(millis);
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
            if (particle.age == 0) {
                continue;
            }
            ctxt.setLineWidth(field.lineWidth(particle.speed));
            ctxt.setStrokeStyle(field.getColor(particle.speed));
            ctxt.beginPath();
            ctxt.moveTo(particle.pxOld.x, particle.pxOld.y);
            particle.pxOld = projection.latlng2pixel(particle.pos);
            ctxt.lineTo(particle.pxOld.x, particle.pxOld.y);
            ctxt.stroke();
        }
    }

    private boolean execute() {
        double speed = 0.01 * field.motionScale(map.getZoom());
        for (int idx = 0; idx < particles.length; idx++) {
            Particle particle = particles[idx];
            if ((particle.age > 0) && (particle.v != null)) {
                double latDeg = particle.pos.getLatDeg() + speed * particle.v.y;
                double lngDeg = particle.pos.getLngDeg() + speed * particle.v.x;
                particle.pos = new DegreePosition(latDeg, lngDeg);
                particle.speed = particle.v.length();
                particle.age--;
                if ((particle.age > 0) && (this.field.inBounds(particle.pos, isVisFull()))) {
                    particle.v = field.getVector(particle.pos);
                } else {
                    particle.v = null;
                }
            } else {
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
