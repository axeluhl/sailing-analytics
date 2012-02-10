package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.LegTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;

public class TimePanel extends FormPanel implements Component<TimePanelSettings>, TimeListener, PlayStateListener, RequiresResize {
    private final Timer timer;
    private final IntegerBox playSpeedBox;
    private final Label timeDelayLabel;
    private final Label timeLabel;
    private final Label dateLabel;
    private final Label playModeLabel;
    private final SliderBar sliderBar;
    private final TimePanelSettings settings;
    private final StringMessages stringMessages;
    private final DateTimeFormat dateFormatter = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL); 
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss"); 
    private final ImageResource playButtonImg;
    private final ImageResource pauseButtonImg;
    private final ImageResource playSpeedImg;
    private final ImageResource playModeLiveActiveImg;
    private final ImageResource playModeReplayActiveImg;
    private final ImageResource playModeInactiveImg;
    private final Image playPauseImage;
    private final Image playModeImage;
    private Date lastReceivedDataTimepoint;
    
    private static ClientResources resources = GWT.create(ClientResources.class);

    private class SettingsClickHandler implements ClickHandler {
        private final StringMessages stringConstants;

        private SettingsClickHandler(StringMessages stringConstants) {
            this.stringConstants = stringConstants;
        }

        @Override
        public void onClick(ClickEvent event) {
            new SettingsDialog<TimePanelSettings>(TimePanel.this, stringConstants).show();
        }
    }

    public TimePanel(Timer timer, StringMessages stringMessages) {
        this.timer = timer;
        this.stringMessages = stringMessages;
        this.settings = new TimePanelSettings();
        timer.addTimeListener(this);
        timer.addPlayStateListener(this);
        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        playButtonImg = resources.timesliderPlayActiveIcon();
        pauseButtonImg = resources.timesliderPauseIcon();
        playSpeedImg = resources.timesliderPlaySpeedIcon();
        playPauseImage = new Image(playButtonImg);

        playModeLiveActiveImg = resources.timesliderPlayStateLiveActiveIcon();
        playModeReplayActiveImg = resources.timesliderPlayStateReplayActiveIcon();
        playModeInactiveImg = resources.timesliderPlayStateLiveInactiveIcon();
        playModeImage = new Image(playModeInactiveImg);

        sliderBar = new SliderBar();
        sliderBar.setEnabled(true);
        sliderBar.setLabelFormatter(new SliderBar.LabelFormatter() {
            final DateTimeFormat formatter = DateTimeFormat.getFormat("HH:mm"); 
            @Override
            public String formatLabel(SliderBar slider, double value) {
                Date date = new Date();
                date.setTime((long) value);
                return formatter.format(date);
            }
        });

        sliderBar.addValueChangeHandler(new ValueChangeHandler<Double>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Double> newValue) {
                if(sliderBar.getCurrentValue() != null) {
                    TimePanel.this.timer.setTime(sliderBar.getCurrentValue().longValue());
                }
            }
        });
        
        vp.add(sliderBar);

        HorizontalPanel controlsPanel = new HorizontalPanel();
        controlsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        controlsPanel.setStyleName("timePanel-controls");
        controlsPanel.setSize("100%", "25px");
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
                        TimePanel.this.timer.resume();
                        break;
                }
            }
        });
        playControlPanel.add(playPauseImage);
        
        // current date and time control
        FlowPanel timeControlPanel = new FlowPanel();
        timeControlPanel.setStyleName("timePanel-controls-time");
        dateLabel = new Label();
        timeLabel = new Label();
        timeControlPanel.add(dateLabel);
        timeControlPanel.add(timeLabel);
        controlsPanel.add(timeControlPanel);
        dateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        dateLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        timeLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        timeLabel.getElement().getStyle().setPaddingBottom(3, Style.Unit.PX);
        timeLabel.getElement().getStyle().setPaddingTop(3, Style.Unit.PX);
        timeLabel.getElement().getStyle().setPaddingLeft(100, Style.Unit.PX);
        timeLabel.getElement().getStyle().setPaddingRight(3, Style.Unit.PX);
        timeLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

        FlowPanel playModeControlPanel = new FlowPanel();
        playModeControlPanel.setStyleName("timePanel-controls-playmode");
        playModeControlPanel.add(playModeImage);
        playModeImage.getElement().getStyle().setFloat(Style.Float.LEFT);
        
        playModeLabel = new Label();
        playModeControlPanel .add(playModeLabel);
        controlsPanel.add(playModeControlPanel );
        playModeLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        playModeLabel.getElement().getStyle().setPadding(5, Style.Unit.PX);
        
        // play speed controls
        FlowPanel playSpeedControlPanel = new FlowPanel();
        playSpeedControlPanel.setStyleName("timePanel-controls-playSpeed");
        controlsPanel.add(playSpeedControlPanel);

        playSpeedBox = new IntegerBox();
        playSpeedBox.setVisibleLength(3);
        playSpeedBox.setWidth("25px");
        playSpeedBox.setValue(1);
        playSpeedBox.setTitle(stringMessages.playSpeedHelp());
        Image playSpeedImage = new Image(playSpeedImg);
        playSpeedControlPanel.add(playSpeedImage);
        playSpeedControlPanel.add(playSpeedBox);

        Button slowDownButton = new Button("-1");
        slowDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 : playSpeedBox.getValue() - 1);
                TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue());
            }
        });
        playSpeedControlPanel.add(slowDownButton);

        Button speedUpButton = new Button("+1");
        speedUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                playSpeedBox.setValue(playSpeedBox.getValue() == null ? 0 : playSpeedBox.getValue() + 1);
                TimePanel.this.timer.setPlaySpeedFactor(playSpeedBox.getValue());
            }
        });
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
        controlsPanel.add(timeDelayPanel);
        timeDelayLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        timeDelayLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        
        // settings
        ImageResource settingsIcon = resources.settingsIcon();
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(settingsIcon).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringMessages));
        controlsPanel.add(settingsAnchor);
        setWidget(vp);
        
        playStateChanged(timer.getPlayState(), timer.getPlayMode());
    }

    @Override
    public void timeChanged(Date time) {
        long t = time.getTime();
        // handle the case where time advances beyond slider's end
        if(sliderBar.isMinMaxInitialized() &&  t > sliderBar.getMaxValue()) {
            sliderBar.setMaxValue(new Double(t));
        }
        
        sliderBar.setCurrentValue(new Double(t), false);
        dateLabel.setText(dateFormatter.format(time));
        if(lastReceivedDataTimepoint == null)
            timeLabel.setText(timeFormatter.format(time));
        else
            timeLabel.setText(timeFormatter.format(time) + " (" + timeFormatter.format(lastReceivedDataTimepoint) + ")");
    }

    public void setMinMax(Date min, Date max) {
        int numTicks = 8;
        sliderBar.setMinValue(new Double(min.getTime()));
        sliderBar.setMaxValue(new Double(max.getTime()));
        if(sliderBar.getCurrentValue() == null) {
            sliderBar.setCurrentValue(new Double(min.getTime()));
        }
        sliderBar.setNumTickLabels(numTicks);
        sliderBar.setNumTicks(numTicks);
        sliderBar.setStepSize(60000);
    }

    public void changeMax(Date max) {
        sliderBar.setMaxValue(new Double(max.getTime()));
        if (timer.getTime().after(max)) {
            timer.setTime((long) max.getTime());
        }
    }

    public void changeMin(Date min) {
        sliderBar.setMinValue(new Double(min.getTime()));
        if (timer.getTime().before(min)) {
            timer.setTime((long) min.getTime());
        }
    }

    public void setLegMarkers(List<LegTimesInfoDTO> legTimepoints) {
        if(sliderBar.isMinMaxInitialized()) {
            sliderBar.clearMarkers();
            
            for (LegTimesInfoDTO legTimepointDTO : legTimepoints) {
              sliderBar.addMarker(legTimepointDTO.name, new Double(legTimepointDTO.firstPassingDate.getTime()));
            }
            sliderBar.redraw();
        }
    }
    
    public void reset() {
        sliderBar.clearMarkers();
        sliderBar.redraw();
    }
    
    private void delayChanged() {
        Integer delayToLivePlay = settings.getDelayToLivePlayInSeconds();
        if (delayToLivePlay != null) {
            timer.setDelay(1000l * delayToLivePlay.longValue());
        }
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        switch(playState) {
            case Playing:
                playPauseImage.setResource(pauseButtonImg);
                if(playMode == PlayModes.Live)
                    playModeImage.setResource(playModeLiveActiveImg);
                else
                    playModeImage.setResource(playModeReplayActiveImg);
                break;
            case Paused:
            case Stopped:
                playPauseImage.setResource(playButtonImg);
                playModeImage.setResource(playModeInactiveImg);
                break;
        }
        
        switch(playMode) {
            case Live: 
                playModeLabel.setText(stringMessages.playModeLive());
                timeDelayLabel.setText(stringMessages.timeDelay() + ": " + ((int) timer.getCurrentDelay() / 1000) + " s");
                timeDelayLabel.setVisible(true);
                sliderBar.setEnabled(false);
                break;
            case Replay: 
                timeDelayLabel.setVisible(false);
                timeDelayLabel.setText("");
                playModeLabel.setText(stringMessages.playModeReplay()); 
                sliderBar.setEnabled(true);
                break;
        }
    }

    public TimePanelSettings getSettings() {
        return settings;
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
    public SettingsDialogComponent<TimePanelSettings> getSettingsDialogComponent() {
        return new TimePanelSettingsDialogComponent(getSettings(), stringMessages);
    }

    @Override
    public void updateSettings(TimePanelSettings newSettings) {
        boolean delayChanged = newSettings.getDelayToLivePlayInSeconds() != getSettings().getDelayToLivePlayInSeconds();
        if (delayChanged && timer.getPlayMode() == PlayModes.Live) {
            getSettings().setDelayToLivePlayInSeconds(newSettings.getDelayToLivePlayInSeconds());
            timeDelayLabel.setText(String.valueOf(newSettings.getDelayToLivePlayInSeconds()) + " s");
            delayChanged();
        }
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

        sliderBar.onResize();
    }

    public Date getLastReceivedDataTimepoint() {
        return lastReceivedDataTimepoint;
    }

    public void setLastReceivedDataTimepoint(Date lastReceivedDataTimepoint) {
        this.lastReceivedDataTimepoint = lastReceivedDataTimepoint;
    }
}
