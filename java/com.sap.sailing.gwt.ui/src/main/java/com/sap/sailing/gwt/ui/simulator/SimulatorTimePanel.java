package com.sap.sailing.gwt.ui.simulator;

import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;

public class SimulatorTimePanel extends RaceTimePanel {

    private boolean active;

    public SimulatorTimePanel(Timer timer, StringMessages stringMessages, RaceTimesInfoProvider raceTimesInfoProvider) {
        super(timer, stringMessages, raceTimesInfoProvider);
        super.playStateChanged(PlayStates.Stopped, PlayModes.Replay);
        this.setActive(true);
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        if (this.active) {
            super.playStateChanged(playState, playMode);
            // this.timer.stop();
        } else {
            // super.playStateChanged(PlayStates.Stopped, PlayModes.Replay);
        }

    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean act) {
        this.active = act;
        this.timeSlider.setEnabled(act);
        if (this.getMin() != null) {
            this.timer.setTime(this.getMin().getTime());
        }
        this.timer.setAutoAdvance(act);
        this.resetTimeSlider();
    }

}
