package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;
import com.sap.sailing.gwt.ui.shared.controls.slider.TimeSlider;

public class TimePanel<T extends TimePanelSettings> extends FormPanel implements Component<T>, TimeListener, TimeZoomChangeListener,
    PlayStateListener, RequiresResize {
    protected final Timer timer;
    protected boolean isTimeZoomed;
    
    /**
     * The start time point of the time interval visualized by this time panel. May be <code>null</code> if not yet initialized.
     * 
     * @see #setMinMax(Date, Date, boolean)
     */
    private Date min;
    
    /**
     * The end time point of the time interval visualized by this time panel. May be <code>null</code> if not yet initialized.
     * 
     * @see #setMinMax(Date, Date, boolean)
     */
    private Date max;
    
    private final IntegerBox playSpeedBox;
    private final Label timeDelayLabel;
    private final Label timeLabel;
    private final Label dateLabel;
    private final Label playModeLabel;
    protected final TimeSlider timeSlider;
    private final Button backToLivePlayButton;
    protected final StringMessages stringMessages;
    protected final DateTimeFormat dateFormatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL); 
    protected final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss"); 
    private final ImageResource playButtonImg;
    private final ImageResource pauseButtonImg;
    private final ImageResource playSpeedImg;
    private final ImageResource playModeLiveActiveImg;
    private final ImageResource playModeReplayActiveImg;
    private final ImageResource playModeInactiveImg;
    private final Image playPauseImage;
    private final Image playModeImage;
    protected Date lastReceivedDataTimepoint;
    private final Button slowDownButton;
    private final Button speedUpButton;

    /**
     * The live delay may be adjusted automatically if the server decides so. However, if the user explicitly sets a live delay,
     * server-side updates to the delay should be suppressed. This flag records whether the user has performed a manual delay
     * override.
     */
    private boolean userExplicitlyChangedLivePlayDelay;

    /** 
     * the minimum time the slider extends it's time when the end of the slider is reached
     */
    private long MINIMUM_AUTO_ADVANCE_TIME_IN_MS = 5 * 60 * 1000; // 5 minutes
    
    private static ClientResources resources = GWT.create(ClientResources.class);

    private class SettingsClickHandler implements ClickHandler {
        private final StringMessages stringConstants;

        private SettingsClickHandler(StringMessages stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public void onClick(ClickEvent event) {
            new SettingsDialog<T>(TimePanel.this, stringConstants).show();
        }
    }

    public TimePanel(Timer timer, StringMessages stringMessages) {
        this.timer = timer;
        this.stringMessages = stringMessages;
        isTimeZoomed = false;
        timer.addTimeListener(this);
        timer.addPlayStateListener(this);
        userExplicitlyChangedLivePlayDelay = false;
        FlowPanel vp = new FlowPanel();
        vp.setStyleName("timePanelInnerWrapper");
        vp.setSize("100%", "100%");
        
        SimplePanel s = new SimplePanel();
        s.setStyleName("timePanelSlider");
        s.getElement().getStyle().setPaddingLeft(66, Unit.PX);
        s.getElement().getStyle().setPaddingRight(66, Unit.PX);

        playButtonImg = resources.timesliderPlayActiveIcon();
        pauseButtonImg = resources.timesliderPauseIcon();
        playSpeedImg = resources.timesliderPlaySpeedIcon();
        playPauseImage = new Image(playButtonImg);

        playModeLiveActiveImg = resources.timesliderPlayStateLiveActiveIcon();
        playModeReplayActiveImg = resources.timesliderPlayStateReplayActiveIcon();
        playModeInactiveImg = resources.timesliderPlayStateLiveInactiveIcon();
        playModeImage = new Image(playModeInactiveImg);

        timeSlider = new TimeSlider();
        timeSlider.setEnabled(true);
        timeSlider.setLabelFormatter(new SliderBar.LabelFormatter() {
            final DateTimeFormat timeWithMinutesFormatter = DateTimeFormat.getFormat("HH:mm"); 
            @Override
            public String formatLabel(SliderBar slider, Double value, Double previousValue) {
                Date date = new Date(value.longValue());
                return timeWithMinutesFormatter.format(date); 
            }
        });

        timeSlider.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> newValue) {
                if(timeSlider.getCurrentValue() != null) {
                    if (TimePanel.this.timer.getPlayMode() == PlayModes.Live) {
                        // put timer into replay mode when user explicitly adjusts time; avoids having to press pause first
                        TimePanel.this.timer.setPlayMode(PlayModes.Replay);
                    }
                    TimePanel.this.timer.setTime(timeSlider.getCurrentValue().longValue());
                }
            }
        });
        
        vp.add(s);
        s.add(timeSlider);

        FlowPanel controlsPanel = new FlowPanel();
        
        controlsPanel.setStyleName("timePanel-controls");
        vp.add(controlsPanel);
        
        // play button control
        FlowPanel playControlPanel = new FlowPanel();
        playControlPanel.setStyleName("timePanel-controls-play");
        controlsPanel.add(playControlPanel);
        
        playPauseImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switch(TimePanel.this.timer.getPlayState()) {
                    case Stopped:
                        TimePanel.this.timer.play();
                        break;
                    case Playing:
                        TimePanel.this.timer.pause();
                        break;
                    case Paused:
                    TimePanel.this.timer.play();
                        break;
                }
            }
        });
        playPauseImage.getElement().getStyle().setFloat(Style.Float.LEFT);
        playPauseImage.getElement().getStyle().setPadding(3, Style.Unit.PX);
        playPauseImage.setTitle(stringMessages.startStopPlaying());
        playPauseImage.getElement().addClassName("playPauseImage");
        playControlPanel.add(playPauseImage);

        backToLivePlayButton = new Button(stringMessages.playModeLive());
        backToLivePlayButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TimePanel.this.timer.setPlayMode(PlayModes.Live);
                TimePanel.this.timer.play();
            }
        });
        backToLivePlayButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        backToLivePlayButton.getElement().getStyle().setPadding(3, Style.Unit.PX);
        backToLivePlayButton.setTitle(stringMessages.backToLive());
        playControlPanel.add(backToLivePlayButton);

        // current date and time control
        FlowPanel timeControlPanel = new FlowPanel();
        timeControlPanel.setStyleName("timePanel-controls-time");
        dateLabel = new Label();
        timeLabel = new Label();
        timeControlPanel.add(dateLabel);
        timeControlPanel.add(timeLabel);
        
        dateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        dateLabel.getElement().setClassName("dateLabel");
        timeLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        timeLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        timeLabel.getElement().setClassName("timeLabel");
        
        FlowPanel playModeControlPanel = new FlowPanel();
        playModeControlPanel.setStyleName("timePanel-controls-playmode");
        playModeControlPanel.add(playModeImage);
        playModeImage.getElement().getStyle().setFloat(Style.Float.LEFT);
        
        playModeLabel = new Label();
        playModeControlPanel .add(playModeLabel);

        playModeLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        playModeLabel.getElement().setClassName("playModeLabel");
        
        // play speed controls
        FlowPanel playSpeedControlPanel = new FlowPanel();
        playSpeedControlPanel.setStyleName("timePanel-controls-playSpeed");
        

        playSpeedBox = new IntegerBox();
        playSpeedBox.setVisibleLength(3);
        playSpeedBox.setWidth("25px");
        playSpeedBox.setHeight("14px");
        playSpeedBox.setValue(1);
        playSpeedBox.setTitle(stringMessages.playSpeedHelp());
        Image playSpeedImage = new Image(playSpeedImg);
        playSpeedControlPanel.add(playSpeedImage);
        playSpeedControlPanel.add(playSpeedBox);

        slowDownButton = new Button("-1");
        slowDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 : playSpeedBox.getValue() - 1);
                TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue());
            }
        });
        slowDownButton.setTitle(stringMessages.slowPlaySpeedDown());
        playSpeedControlPanel.add(slowDownButton);

        speedUpButton = new Button("+1");
        speedUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 : playSpeedBox.getValue() + 1);
                TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue());
            }
        });
        speedUpButton.setTitle(stringMessages.speedPlaySpeedUp());
        playSpeedControlPanel.add(speedUpButton);

        playSpeedImage.getElement().getStyle().setFloat(Style.Float.LEFT);
        playSpeedImage.getElement().getStyle().setPadding(3, Style.Unit.PX);
       
        playSpeedBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        playSpeedBox.getElement().getStyle().setPadding(3, Style.Unit.PX);
        speedUpButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        speedUpButton.getElement().getStyle().setPadding(3, Style.Unit.PX);
        slowDownButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        slowDownButton.getElement().getStyle().setPadding(3, Style.Unit.PX);

        // time delay
        FlowPanel timeDelayPanel = new FlowPanel();
        timeDelayPanel.setStyleName("timePanel-controls-timeDelay");

        timeDelayLabel = new Label();
        timeDelayPanel.add(timeDelayLabel);
        
        timeDelayLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        timeDelayLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        
        // settings
        ImageResource settingsIcon = resources.settingsIcon();
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(settingsIcon).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.setStyleName("timePanelSettings");
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringMessages));
        controlsPanel.add(settingsAnchor);
        setWidget(vp);
        playStateChanged(timer.getPlayState(), timer.getPlayMode());
        
        
        controlsPanel.add(playSpeedControlPanel);
        controlsPanel.add(playModeControlPanel );
        controlsPanel.add(timeDelayPanel);
        controlsPanel.add(timeControlPanel);
    }

    @Override
    public void timeChanged(Date time) {
        if (getMin() != null && getMax() != null) {
            // Handle also the case where time advances beyond slider's end.
            // Handle it equally for replay and live mode for robustness reasons. This at least allows a user
            // to watch on even if the time panel was off in its assumptions about race end and end of tracking.
            if (time.after(getMax())) {
                Date newMaxTime = new Date(time.getTime());
                if (newMaxTime.getTime() - getMax().getTime() < MINIMUM_AUTO_ADVANCE_TIME_IN_MS) {
                    newMaxTime.setTime(getMax().getTime() + MINIMUM_AUTO_ADVANCE_TIME_IN_MS); 
                }
                setMinMax(getMin(), newMaxTime, /* fireEvent */ false); // no event because we guarantee that time is between min/max
            }
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

    protected Date getMin() {
        return min;
    }
    
    protected Date getMax() {
        return max;
    }
    
    /**
     * @param min must not be <code>null</code>
     * @param max must not be <code>null</code>
     * @param fireEvent TODO
     */
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
            int numSteps = timeSlider.getElement().getClientWidth();
            if (numSteps > 0) {
                timeSlider.setStepSize(numSteps, fireEvent);
            } else {
                timeSlider.setStepSize(1000, fireEvent);
            }
        }
    }

    public void resetTimeSlider() {
        timeSlider.clearMarkers();
        timeSlider.redraw();
    }
    
    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        boolean liveModeToBeMadePossible = isLiveModeToBeMadePossible();
        setLiveGenerallyPossible(liveModeToBeMadePossible);
        setJumpToLiveEnablement(liveModeToBeMadePossible && playMode != PlayModes.Live);
        switch (playState) {
        case Playing:
            playPauseImage.setResource(pauseButtonImg);
            playPauseImage.getElement().addClassName("playPauseImagePause");
            if (playMode == PlayModes.Live) {
                playModeImage.setResource(playModeLiveActiveImg);
            } else {
                playModeImage.setResource(playModeReplayActiveImg);
            }
            break;
        case Paused:
        	playPauseImage.getElement().removeClassName("playPauseImagePause");
        case Stopped:
            playPauseImage.setResource(playButtonImg);
            playModeImage.setResource(playModeInactiveImg);
            break;
        }
        
        switch (playMode) {
        case Live:
            playModeLabel.setText(stringMessages.playModeLive());
            timeDelayLabel.setText(stringMessages.timeDelay() + ": " + timer.getCurrentDelayInMillis() / 1000 + " s");
            timeDelayLabel.setVisible(true);
            timeSlider.setEnabled(true);
            playSpeedBox.setEnabled(false);
            slowDownButton.setEnabled(false);
            speedUpButton.setEnabled(false);
            break;
        case Replay:
            timeDelayLabel.setVisible(false);
            timeDelayLabel.setText("");
            playModeLabel.setText(stringMessages.playModeReplay());
            timeSlider.setEnabled(true);
            playSpeedBox.setEnabled(true);
            slowDownButton.setEnabled(true);
            speedUpButton.setEnabled(true);
            break;
        }
    }
    
    @Override
    public void onTimeZoom(Date zoomStartTimepoint, Date zoomEndTimepoint) {
    }

    @Override
    public void onTimeZoomReset() {
    }
    
    protected boolean isLiveModeToBeMadePossible() {
        return false;
    }
    
    /**
     * Enables the {@link #backToLivePlayButton} if and only if <code>enabled</code> is <code>true</code>.
     */
    protected void setJumpToLiveEnablement(boolean enabled) {
        backToLivePlayButton.setEnabled(enabled);
    }
    
    /**
     * Iff <code>possible</code>, makes the {@link #backToLivePlayButton} button visible. 
     */
    protected void setLiveGenerallyPossible(boolean possible) {
        backToLivePlayButton.setVisible(possible);
    }

    @SuppressWarnings("unchecked")
    public T getSettings() {
        TimePanelSettings result = new TimePanelSettings();
        result.setDelayToLivePlayInSeconds(timer.getLivePlayDelayInMillis()/1000);
        result.setRefreshInterval(timer.getRefreshInterval());
        return (T) result;
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<T> getSettingsDialogComponent() {
        return new TimePanelSettingsDialogComponent<T>(getSettings(), stringMessages);
    }

    protected boolean isUserExplicitlyChangedLivePlayDelay() {
        return userExplicitlyChangedLivePlayDelay;
    }
    
    @Override
    public void updateSettings(T newSettings) {
        boolean delayChanged = newSettings.getDelayToLivePlayInSeconds() != getSettings().getDelayToLivePlayInSeconds();
        if (delayChanged) {
            userExplicitlyChangedLivePlayDelay = true;
            timer.setLivePlayDelayInMillis(1000l * newSettings.getDelayToLivePlayInSeconds());
            if (timer.getPlayMode() == PlayModes.Live) {
                timeDelayLabel.setText(String.valueOf(newSettings.getDelayToLivePlayInSeconds()) + " s");
            }
        }
        timer.setRefreshInterval(newSettings.getRefreshInterval());
    }

    @Override
    public String getLocalizedShortName() {
        return "Time control";
    }
    
    @Override
    public void onResize() {
        // handle what is required by @link{ProvidesResize}
        Widget child = getWidget();
        if (child instanceof RequiresResize) {
            ((RequiresResize) child).onResize();
        }
        timeSlider.onResize();
    }

    public Date getLastReceivedDataTimepoint() {
        return lastReceivedDataTimepoint;
    }

    public void setLastReceivedDataTimepoint(Date lastReceivedDataTimepoint) {
        this.lastReceivedDataTimepoint = lastReceivedDataTimepoint;
    }
}
