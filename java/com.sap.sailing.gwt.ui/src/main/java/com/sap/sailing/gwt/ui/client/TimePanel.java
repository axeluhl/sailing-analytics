package com.sap.sailing.gwt.ui.client;

import java.util.Date;

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
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;

public class TimePanel extends FormPanel implements Component<TimePanelSettings>, TimeListener, PlayStateListener, RequiresResize {
    private final Timer timer;
    private final Button playPauseButton;
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
        playControlPanel.add(playPauseButton);
        
        // current date and time control
        FlowPanel timeControlPanel = new FlowPanel();
        timeControlPanel.setStyleName("timePanel-controls-time");
        dateLabel = new Label();
        timeLabel = new Label();
        timeControlPanel.add(dateLabel);
        timeControlPanel.add(timeLabel);
        controlsPanel.add(timeControlPanel);
        timeLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        timeLabel.getElement().getStyle().setPadding(5, Style.Unit.PX);
        timeLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
        dateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        dateLabel.getElement().getStyle().setPadding(5, Style.Unit.PX);

        FlowPanel playModeControlPanel = new FlowPanel();
        playModeControlPanel.setStyleName("timePanel-controls-playmode");
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
        Label playSpeedLabel = new Label(stringMessages.playSpeed() +":");
        playSpeedControlPanel.add(playSpeedLabel);
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

        playSpeedLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        playSpeedLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        playSpeedBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        playSpeedBox.getElement().getStyle().setPadding(3, Style.Unit.PX);
        speedUpButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        speedUpButton.getElement().getStyle().setPadding(3, Style.Unit.PX);
        slowDownButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        slowDownButton.getElement().getStyle().setPadding(3, Style.Unit.PX);

        // time delay
        if(timer.getPlayMode() == PlayModes.Live) {
            FlowPanel timeDelayPanel = new FlowPanel();
            timeDelayPanel.setStyleName("timePanel-controls-timeDelay");

            final Label delayTextLabel = new Label(stringMessages.timeDelay() + ":"); 
            timeDelayPanel.add(delayTextLabel);
            timeDelayLabel = new Label();
            timeDelayPanel.add(timeDelayLabel);
            controlsPanel.add(timeDelayPanel);

            delayTextLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
            delayTextLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
            timeDelayLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
            timeDelayLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        } else {
            timeDelayLabel = null;
        }

        ImageResource settingsIcon = resources.settingsIcon();
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(settingsIcon).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringMessages));

        controlsPanel.add(settingsAnchor);
        
        setWidget(vp);
    }

    @Override
    public void timeChanged(Date time) {
        long t = time.getTime();
        // handle the case where time advances beyond slider's end
        if(sliderBar.isMinMaxInitialized() &&  t > sliderBar.getMaxValue()) {
            sliderBar.setMaxValue(new Double(t));
        }
        
        sliderBar.setCurrentValue(new Double(t), false);
        timeLabel.setText(timeFormatter.format(time));
        dateLabel.setText(dateFormatter.format(time));
        switch(timer.getPlayMode()) {
            case Live: 
                playModeLabel.setText(stringMessages.playModeLive()); 
                timeDelayLabel.setText(((int) timer.getCurrentDelay() / 1000) + " s");
                break;
            case Replay: 
                playModeLabel.setText(stringMessages.playModeReplay()); 
                break;
        }
        playModeLabel.setText(timer.getPlayMode().name());
    }

    public void setMinMax(Date min, Date max) {
        int numTicks = 8;
        sliderBar.setMinValue(new Double(min.getTime()));
        sliderBar.setMaxValue(new Double(max.getTime()));
        if(sliderBar.getCurrentValue() == null)
            sliderBar.setCurrentValue(new Double(min.getTime()));
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

    public void setLegMarkers() {
        if(sliderBar.isMinMaxInitialized()) {
            sliderBar.clearMarkers();
            
            Double minValue = sliderBar.getMinValue();
            Double maxValue = sliderBar.getMaxValue();
            int legCount = 5;
            
            double diff = (maxValue - minValue) / (double) legCount;
            
            for(int i = 0; i < legCount; i++)
                sliderBar.addMarker("L" + (i + 1), minValue + i * diff);
            
            sliderBar.redraw();
        }
    }
    
    private void delayChanged() {
        Integer delayToLivePlay = settings.getDelayToLivePlayInSeconds();
        if (delayToLivePlay != null) {
            timer.setDelay(1000l * delayToLivePlay.longValue());
        }
    }

    @Override
    public void playStateChanged(boolean isPlaying) {
        playPauseButton.setText(isPlaying ? "||" : ">");
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
        if(delayChanged && timer.getPlayMode() == PlayModes.Live) {
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
}
