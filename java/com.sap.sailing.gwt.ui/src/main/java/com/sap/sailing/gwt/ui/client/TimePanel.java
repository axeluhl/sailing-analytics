package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.ui.commons.client.SliderWidget;
import com.sap.ui.core.client.event.Event;
import com.sap.ui.core.client.event.EventListener;

public class TimePanel extends FormPanel implements TimeListener, PlayStateListener {
    private final Timer timer;
    private final SliderWidget slider;
    private final Button playPauseButton;
    private final DoubleBox accelerationBox;
    private final DoubleBox delayBox;
    private boolean delayBoxHasFocus;
    private final Label timeLabel;

    public TimePanel(StringConstants stringConstants, Timer timer) {
        this.timer = timer;
        timer.addTimeListener(this);
        timer.addPlayStateListener(this);
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");
        vp.setSpacing(5);
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSize("100%", "100%");
        vp.add(hp);
        playPauseButton = new Button("&gt;");
        playPauseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (TimePanel.this.timer.isPlaying()) {
                    TimePanel.this.timer.pause();
                } else {
                    TimePanel.this.timer.resume();
                }
            }
        });
        hp.add(playPauseButton);
        Button slowDownButton = new Button("-1");
        slowDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                accelerationBox.setValue(accelerationBox.getValue() == null ? 0 : accelerationBox.getValue() - 1);
                TimePanel.this.timer.setAccelerationFactor(accelerationBox.getValue());
            }
        });
        hp.add(slowDownButton);
        accelerationBox = new DoubleBox();
        accelerationBox.setVisibleLength(3);
        accelerationBox.setValue(1.0);
        accelerationBox.setTitle(stringConstants.accelerationHelp());
        hp.add(new Label(stringConstants.acceleration()));
        hp.add(accelerationBox);
        Button speedUpButton = new Button("+1");
        speedUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                accelerationBox.setValue(accelerationBox.getValue() == null ? 0 : accelerationBox.getValue() + 1);
                TimePanel.this.timer.setAccelerationFactor(accelerationBox.getValue());
            }
        });
        hp.add(speedUpButton);
        hp.add(new Label(stringConstants.delay()));
        delayBox = new DoubleBox();
        delayBox.setVisibleLength(10);
        delayBox.setText("");
        delayBox.setTitle(stringConstants.delayHelp());
        delayBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent e) {
                delayBoxHasFocus = true;
            }
        });
        delayBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                delayBoxHasFocus = false;
                delayChanged();
            }
        });
        hp.add(delayBox);
        hp.add(new Label(stringConstants.time()));
        timeLabel = new Label();
        hp.add(timeLabel);
        slider = new SliderWidget();
        slider.setTotalUnits(10);
        EventListener sliderListener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                TimePanel.this.timer.setTime((long) slider.getValue());
            }
        };
        slider.attachChangeEvent(sliderListener);
        slider.attachLiveChangeEvent(sliderListener);
        vp.add(slider);
        setWidget(vp);
    }

    @Override
    public void timeChanged(Date time) {
        long t = time.getTime();
        // handle the case where time advances beyond slider's end
        if (t > slider.getMax()) {
            slider.setMax(t);
        }
        slider.setValue(t);
        slider.setTitle(new Date((long) slider.getValue()).toString());
        timeLabel.setText(time.toString());
        if (!delayBoxHasFocus) {
            delayBox.setValue((double) (timer.getDelay() / 1000));
        }
    }

    public void setMin(Date min) {
        slider.setMin(min.getTime());
        if (timer.getTime().before(min)) {
            timer.setTime((long) min.getTime());
        }
    }

    public void setMax(Date max) {
        slider.setMax(max.getTime());
        if (timer.getTime().after(max)) {
            timer.setTime((long) max.getTime());
        }
    }

    private void delayChanged() {
        Double delay = delayBox.getValue();
        if (delay != null) {
            timer.setDelay(1000l * delay.longValue());
        }
    }

    @Override
    public void playStateChanged(boolean isPlaying) {
        playPauseButton.setText(isPlaying ? "||" : ">");
    }

}
