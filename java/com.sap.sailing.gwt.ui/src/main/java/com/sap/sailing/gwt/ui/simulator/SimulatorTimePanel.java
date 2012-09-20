package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.sap.sailing.gwt.ui.client.RaceTimePanel;
import com.sap.sailing.gwt.ui.client.RaceTimePanelSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

public class SimulatorTimePanel extends RaceTimePanel {

    private boolean active;

    public SimulatorTimePanel(Timer timer, StringMessages stringMessages, WindFieldGenParamsDTO windParams) {

        super(timer, stringMessages, null);

        int secondsTimeStep = (int) windParams.getTimeStep().getTime() / 1000;
        this.playSpeedBox.setValue(secondsTimeStep);
        this.timer.setPlaySpeedFactor(secondsTimeStep);

        /*
         * slowDownButton = new Button("-1"); slowDownButton.addClickHandler(new ClickHandler() {
         * 
         * @Override public void onClick(ClickEvent event) { playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 :
         * playSpeedBox.getValue() - 1); TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue()); } });
         * slowDownButton.setTitle(stringMessages.slowPlaySpeedDown()); playSpeedControlPanel.add(slowDownButton);
         * 
         * speedUpButton = new Button("+1"); speedUpButton.addClickHandler(new ClickHandler() {
         * 
         * @Override public void onClick(ClickEvent event) { playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 :
         * playSpeedBox.getValue() + 1); TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue()); } });
         */

        super.playStateChanged(PlayStates.Stopped, PlayModes.Replay);
        this.setActive(false);
    }

    @Override
    protected void addPlayPauseButtonClickHandler() {

        playPauseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean playable = false;
                if ((SimulatorTimePanel.this.timer.getTime().before(SimulatorTimePanel.this.getMax()))&&(SimulatorTimePanel.this.getActive())) {
                    playable = true;
                }
                SimulatorTimePanel.this.timer.setAutoAdvance(playable);
                switch (SimulatorTimePanel.this.timer.getPlayState()) {
                case Stopped:
                    if (playable) {
                        SimulatorTimePanel.this.timer.play();
                    }
                    break;
                case Playing:
                    SimulatorTimePanel.this.timer.pause();
                    break;
                case Paused:
                    if (playable) {
                        SimulatorTimePanel.this.timer.play();
                    }
                    break;
                }
            }
        });

    }

    public void setMinMax(Date min, Date max, boolean fireEvent) {
        assert min != null && max != null;

        boolean changed = false;
        if (!max.equals(this.max)) {
            changed = true;
            this.max = max;
            timeSlider.setMaxValue(new Double(max.getTime()), fireEvent);
        }
        if (!min.equals(this.min)) {
            changed = true;
            this.min = min;
            timeSlider.setMinValue(new Double(min.getTime()), fireEvent);
            if (timeSlider.getCurrentValue() == null) {
                timeSlider.setCurrentValue(new Double(min.getTime()), fireEvent);
            }
        }
        if (changed) {
            timeSlider.setStepSize(1000, fireEvent);
        }
    }

    @Override
    public void timeChanged(Date time) {

        if (getMin() != null && getMax() != null) {

            boolean setMax = false;
            if (time.after(getMax())) {
                timer.pause();
                super.playStateChanged(PlayStates.Paused, PlayModes.Replay);
                setMax = true;
            }

            if (setMax) {
                timer.setTime(getMax().getTime()); // setTime triggers another timeChanged event, so omit label-update
                                                   // on setMax
            } else {
                // update time slider, date & time label
                long t = time.getTime();
                timeSlider.setCurrentValue(new Double(t), false);
                dateLabel.setText(dateFormatter.format(time));
                if (lastReceivedDataTimepoint == null) {
                    timeLabel.setText(timeFormatter.format(time));
                } else {
                    timeLabel.setText(timeFormatter.format(time) + " (" + timeFormatter.format(lastReceivedDataTimepoint) + ")");
                }

            }
        }
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {

        if (this.active) {
            super.playStateChanged(playState, playMode);
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

    // TODO: adapt timepanel settings dialog to strategy simulator
    @Override
    public RaceTimePanelSettings getSettings() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setDelayToLivePlayInSeconds(timer.getLivePlayDelayInMillis() / 1000);
        result.setRefreshInterval(timer.getRefreshInterval());
        RaceTimesInfoDTO rtInfo = new RaceTimesInfoDTO();
        rtInfo.startOfRace = new Date(); // initialize so that rtInfo.delayToLiveInMs is shown
        result.setRaceTimesInfo(rtInfo);
        return result;
    }

}
