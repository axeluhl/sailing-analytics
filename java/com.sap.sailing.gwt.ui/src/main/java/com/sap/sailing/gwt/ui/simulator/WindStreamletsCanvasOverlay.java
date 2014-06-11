package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.streamlets.Swarm;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney (D054070)
 * 
 */
public class WindStreamletsCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {
    /** The wind field that is to be displayed in the overlay */
    private WindFieldDTO windFieldDTO;
    private final WindFieldGenParamsDTO windParams;
    private final SimulatorMap simulatorMap;

    private boolean visible = false;
    private final Timer timer;
    private Date endDate;

    private int nParticles;
    private Swarm swarm;

    public WindStreamletsCanvasOverlay(SimulatorMap simulatorMap, int zIndex, final Timer timer,
            final WindFieldGenParamsDTO windParams) {
        super(simulatorMap.getMap(), zIndex);
        this.simulatorMap = simulatorMap;
        this.timer = timer;
        this.windParams = windParams;
        this.nParticles = this.simulatorMap.getMainPanel().particles;
        windFieldDTO = null;
        getCanvas().getElement().setId("swarm-display");
    }

    public WindFieldDTO getWindFieldDTO() {
        return this.windFieldDTO;
    }

    public WindFieldGenParamsDTO getWindParams() {
        return this.windParams;
    }

    public WindStreamletsCanvasOverlay(SimulatorMap simulatorMap, int zIndex) {
        this(simulatorMap, zIndex, null, null);
    }

    public void startStreamlets() {
        if (swarm == null) {
            this.swarm = new Swarm(this, map);
        }
        this.swarm.start(40, windFieldDTO);
    }

    public void setStreamletsStep(int step) {
        this.swarm.getField().setStep(step);
    }

    public void stopStreamlets() {
        if (swarm != null) {
            this.swarm.stop();
        }
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setWindField(final WindFieldDTO windFieldDTO) {
        this.windFieldDTO = windFieldDTO;
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
                if (timer.getTime().compareTo(this.windParams.getStartTime()) != 0) {
                    this.timeChanged(timer.getTime(), null);
                }
                this.visible = isVisible;
            } else {
                this.stopStreamlets();
                this.visible = isVisible;
            }
        }
    }

    @Override
    public void addToMap() {
        if (timer != null) {
            timer.addTimeListener(this);
        }
    }

    @Override
    public void removeFromMap() {
        if (timer != null) {
            timer.removeTimeListener(this);
        }
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
                this.swarm = new Swarm(this, map);
                this.swarm.start(40, null);
            }
            if (windFieldDTO != null) {
                // drawing is done by external JavaScript for Streamlets
            }
        }
    }

    public Swarm getSwarm() {
        return this.swarm;
    }

    @Override
    public void timeChanged(final Date newDate, Date oldDate) {
        int step = (int) ((newDate.getTime() - this.windParams.getStartTime().getTime()) / this.windParams.getTimeStep().asMillis());
        this.setStreamletsStep(step);
    }

    @Override
    public boolean shallStop() {
        if (timer.getTime().getTime() >= this.endDate.getTime()) {
            return true;
        } else {
            return false;
        }
    }
}
