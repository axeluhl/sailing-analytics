package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.streamlets.RectField;
import com.sap.sailing.gwt.ui.simulator.streamlets.SimulatorJSBundle;
import com.sap.sailing.gwt.ui.simulator.streamlets.Swarm;
import com.sap.sailing.gwt.ui.simulator.streamlets.VectorField;
import com.sap.sailing.gwt.ui.simulator.streamlets.WindInfoForRaceVectorField;
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
    private static final int nParticles = 5000;
    private static final int animationIntervalMillis = 40;
    private boolean visible = false;
    private final Timer timer;
    private Swarm swarm;
    private final WindInfoForRaceVectorField windField;

    public WindStreamletsRaceboardOverlay(MapWidget map, int zIndex, final Timer timer, WindInfoForRaceDTO windInfoForRace) {
        super(map, zIndex);
        this.windField = new WindInfoForRaceVectorField(windInfoForRace);
        this.timer = timer;
        getCanvas().getElement().setId("swarm-display");
    }

    public void startStreamlets() {
        if (swarm == null) {
            setCanvasSettings();
            this.swarm = new Swarm(this, map, timer, windField);
        }
        this.swarm.start(animationIntervalMillis);
    }

    public void stopStreamlets() {
        if (swarm != null) {
            this.swarm.stop();
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

    @Override
    protected void draw() {
        super.draw();
        if (mapProjection != null) {
            if ((nParticles > 0) && (swarm == null)) {
                SimulatorJSBundle bundle = GWT.create(SimulatorJSBundle.class);
                String jsonStr = bundle.windStreamletsDataJS().getText();
                RectField f = RectField.read(jsonStr.substring(19, jsonStr.length() - 1), false);
                map.setZoom(5);
                map.panTo(f.getCenter());
                this.swarm = new Swarm(this, map, timer, f);
                this.swarm.start(/* animationIntervalMillis */ 40);
            }
        }
    }

    public Swarm getSwarm() {
        return this.swarm;
    }
}
