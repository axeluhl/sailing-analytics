package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.ui.commons.client.SliderWidget;
import com.sap.ui.core.client.event.Event;
import com.sap.ui.core.client.event.EventListener;

public class TimePanel extends FormPanel {
    private final SliderWidget slider;
    private final Set<TimeListener> listeners;
    private final Button playPauseButton;
    private final DoubleBox accelerationBox;
    private final Label timeLabel;
    private Date time;
    private boolean playing;
    
    public TimePanel(StringConstants stringConstants) {
        listeners = new HashSet<TimeListener>();
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSize("100%", "100%");
        vp.add(hp);
        playPauseButton = new Button("&gt;");
        playPauseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                togglePlay();
            }
        });
        hp.add(playPauseButton);
        playing = false;
        Button slowDownButton = new Button("-1");
        slowDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                accelerationBox.setValue(accelerationBox.getValue() == null ? 0 : accelerationBox.getValue() - 1);
            }
        });
        hp.add(slowDownButton);
        accelerationBox = new DoubleBox();
        accelerationBox.setValue(1.0);
        accelerationBox.setTitle(stringConstants.accelerationHelp());
        hp.add(new Label(stringConstants.acceleration()));
        hp.add(accelerationBox);
        Button speedUpButton = new Button("+1");
        speedUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                accelerationBox.setValue(accelerationBox.getValue() == null ? 0 : accelerationBox.getValue() + 1);
            }
        });
        hp.add(speedUpButton);
        hp.add(new Label(stringConstants.time()));
        timeLabel = new Label();
        hp.add(timeLabel);
        slider = new SliderWidget();
        slider.setStepLabels(true);
        slider.setTotalUnits(10);
        EventListener sliderListener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                slider.setTitle(new Date((long) slider.getValue()).toString());
                setTime((long) slider.getValue());
            }
        };
        slider.attachChangeEvent(sliderListener);
        slider.attachLiveChangeEvent(sliderListener);
        vp.add(slider);
        setWidget(vp);
    }
    
    public void addTimeListener(TimeListener listener) {
        listeners.add(listener);
    }
    
    public void removeTimeListener(TimeListener listener) {
        listeners.remove(listener);
    }
    
    private void setTime(long timePointAsMillis) {
        time = new Date(timePointAsMillis);
        timeLabel.setText(time.toString());
        for (TimeListener listener : listeners) {
            listener.timeChanged(time);
        }
    }


    public void setMin(Date min) {
        slider.setMin(min.getTime());
        if (time == null || time.before(min)) {
            slider.setValue((long) min.getTime());
            setTime((long) min.getTime());
        }
    }


    public void setMax(Date max) {
        slider.setMax(max.getTime());
        if (time == null || time.after(max)) {
            slider.setValue((long) max.getTime());
            setTime((long) max.getTime());
        }
    }


    private void togglePlay() {
        playing = !playing;
        playPauseButton.setText(playing ? "||" : ">");
    }
    
}
