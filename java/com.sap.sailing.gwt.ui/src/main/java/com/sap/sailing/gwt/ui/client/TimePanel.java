package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
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

public class TimePanel extends FormPanel {
    private final SliderWidget slider;
    private final Set<TimeListener> listeners;
    private final Button playPauseButton;
    private final DoubleBox accelerationBox;
    private final DoubleBox delayBox;
    private boolean delayBoxHasFocus;
    private final Label timeLabel;
    private Date time;
    private boolean playing;
    private final long delayBetweenAutoAdvancesInMilliseconds;
    
    public TimePanel(StringConstants stringConstants, long delayBetweenAutoAdvancesInMilliseconds) {
        this.delayBetweenAutoAdvancesInMilliseconds = delayBetweenAutoAdvancesInMilliseconds;
        listeners = new HashSet<TimeListener>();
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
        if (!delayBoxHasFocus) {
            delayBox.setValue((double) Math.round((double) (new Date().getTime() - timePointAsMillis)/1000.));
        }
        for (TimeListener listener : listeners) {
            listener.timeChanged(time);
        }
    }

    public Date getTime() {
        return time;
    }

    public void setMin(Date min) {
        slider.setMin(min.getTime());
        if (time == null || time.before(min)) {
            setTimeIncludingSlider(min);
        }
    }


    public void setMax(Date max) {
        slider.setMax(max.getTime());
        if (time == null || time.after(max)) {
            setTimeIncludingSlider(max);
        }
    }

    private void setTimeIncludingSlider(Date t) {
        slider.setValue((long) t.getTime());
        setTime((long) t.getTime());
    }


    private void togglePlay() {
        playing = !playing;
        playPauseButton.setText(playing ? "||" : ">");
        if (playing) {
            startAutoAdvance();
        }
    }

    private void startAutoAdvance() {
        RepeatingCommand command = new RepeatingCommand( ) {
            @Override
            public boolean execute() {
                if (time != null) {
                    setTimeIncludingSlider(new Date((long) (time.getTime() + accelerationBox.getValue()
                            * delayBetweenAutoAdvancesInMilliseconds)));
                }
                return playing;
            }
        };
        Scheduler.get().scheduleFixedPeriod(command, (int) delayBetweenAutoAdvancesInMilliseconds);
    }
    
    private void delayChanged() {
        Double delay = delayBox.getValue();
        if (delay != null) {
            setTimeIncludingSlider(new Date(System.currentTimeMillis()-delayBox.getValue().longValue()*1000));
        }
    }
    
}
