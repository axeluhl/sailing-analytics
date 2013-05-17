package com.sap.sailing.simulator.impl;

import java.io.Serializable;

import com.sap.sailing.simulator.SimulatorUISelection;

public class SimulatorUISelectionImpl implements Serializable, SimulatorUISelection {

    private static final long serialVersionUID = -1495298157737907408L;

    private int boatClassIndex;
    private int raceIndex;
    private int competitorIndex;
    private int legIndex;

    public SimulatorUISelectionImpl() {
        this.boatClassIndex = -1;
        this.raceIndex = -1;
        this.competitorIndex = -1;
        this.legIndex = -1;
    }

    public SimulatorUISelectionImpl(int boatClassIndex, int raceIndex, int competitorIndex, int legIndex) {
        this.boatClassIndex = boatClassIndex;
        this.raceIndex = raceIndex;
        this.competitorIndex = competitorIndex;
        this.legIndex = legIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#getBoatClassIndex()
     */
    @Override
    public int getBoatClassIndex() {
        return this.boatClassIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#setBoatClassIndex(int)
     */
    @Override
    public void setBoatClassIndex(int boatClassIndex) {
        this.boatClassIndex = boatClassIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#getRaceIndex()
     */
    @Override
    public int getRaceIndex() {
        return this.raceIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#setRaceIndex(int)
     */
    @Override
    public void setRaceIndex(int raceIndex) {
        this.raceIndex = raceIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#getCompetitorIndex()
     */
    @Override
    public int getCompetitorIndex() {
        return this.competitorIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#setCompetitorIndex(int)
     */
    @Override
    public void setCompetitorIndex(int competitorIndex) {
        this.competitorIndex = competitorIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#getLegIndex()
     */
    @Override
    public int getLegIndex() {
        return this.legIndex;
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.simulator.impl.SimulatorUISelection#setLegIndex(int)
     */
    @Override
    public void setLegIndex(int legIndex) {
        this.legIndex = legIndex;
    }

}
