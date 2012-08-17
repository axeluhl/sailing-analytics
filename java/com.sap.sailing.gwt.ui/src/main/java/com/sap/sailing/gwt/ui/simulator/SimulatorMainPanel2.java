package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;
import com.sap.sailing.gwt.ui.shared.panels.SimpleBusyIndicator;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class SimulatorMainPanel2 extends SplitLayoutPanel {

    private FlowPanel leftPanel;
    private FlowPanel rightPanel;
    private VerticalPanel windPanel;

    private Button updateButton;
    private Button courseInputButton;

    private RadioButton summaryButton;
    private RadioButton replayButton;
    private RadioButton windDisplayButton;
    //private TimePanel<TimePanelSettings> timePanel;

    private Map<String, WindPatternDisplay> patternDisplayMap;
    private Map<String, Panel> patternPanelMap;

    private WindPatternDisplay currentWPDisplay;
    private Panel currentWPPanel;

    private ListBox patternSelector;
    private Map<String, WindPatternDTO> patternNameDTOMap;
    private ListBox boatSelector;
    private ListBox directionSelector;

    private WindFieldGenParamsDTO windParams;
    private final Timer timer;
    private SimpleBusyIndicator busyIndicator;
    private static Logger logger = Logger.getLogger(SimulatorMainPanel2.class.getName());

    private SimulatorMap simulatorMap;
    private final StringMessages stringMessages;
    private final SimulatorServiceAsync simulatorSvc;
    private final ErrorReporter errorReporter;
    private final int xRes;
    private final int yRes;
    private final boolean autoUpdate;

    private SimulatorTimePanel timePanel;
    //private final Timer timer;

    private class WindControlCapture implements ValueChangeHandler<Double> {

        private SliderBar sliderBar;
        private WindPatternSetting<?> setting;

        public WindControlCapture(SliderBar sliderBar, WindPatternSetting<?> setting) {
            this.sliderBar = sliderBar;
            this.setting = setting;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Double> arg0) {
            sliderBar.setTitle(String.valueOf(Math.round(sliderBar.getCurrentValue())));
            logger.info("Slider value : " + arg0.getValue());
            setting.setValue(arg0.getValue());
            if (autoUpdate) {
                update();
            }
        }

    }

    private class PatternSelectorHandler implements ChangeHandler {

        private class PatternRetriever implements AsyncCallback<WindPatternDisplay> {

            private String windPattern;

            public PatternRetriever(String windPattern) {
                this.windPattern = windPattern;
            }

            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Error retreiving wind patterns" + message.getMessage());
            }

            @Override
            public void onSuccess(WindPatternDisplay display) {
                logger.info(display.getSettings().toString());
                patternDisplayMap.put(windPattern, display);
                currentWPDisplay = display;
                Panel wPanel = getWindControlPanel();
                if (currentWPPanel != null) {
                    currentWPPanel.removeFromParent();
                }
                currentWPPanel = wPanel;
                patternPanelMap.put(windPattern, currentWPPanel);
                windPanel.add(currentWPPanel);
                currentWPPanel.setWidth("100%");
                currentWPPanel.getElement().setClassName("currentWPPanel");
            }

        }

        @Override
        public void onChange(ChangeEvent arg0) {

            String windPattern = patternSelector.getItemText(patternSelector.getSelectedIndex());
            logger.info(windPattern);

            if (patternDisplayMap.containsKey(windPattern)) {
                currentWPDisplay = patternDisplayMap.get(windPattern);
                if (currentWPPanel != null) {
                    currentWPPanel.removeFromParent();
                }
                currentWPPanel = patternPanelMap.get(windPattern);
                windPanel.add(currentWPPanel);

            } else {
                WindPatternDTO pattern = patternNameDTOMap.get(windPattern);
                simulatorSvc.getWindPatternDisplay(pattern, new PatternRetriever(windPattern));
            }
            simulatorMap.removeOverlays();
        }

    }

    public SimulatorMainPanel2(SimulatorServiceAsync svc, StringMessages stringMessages, ErrorReporter errorReporter,
            int xRes, int yRes, boolean autoUpdate) {

        super();

        this.simulatorSvc = svc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.xRes = xRes;
        this.yRes = yRes;
        this.autoUpdate = autoUpdate;

        leftPanel = new FlowPanel();
        rightPanel = new FlowPanel();
        patternSelector = new ListBox();
        patternSelector.addChangeHandler(new PatternSelectorHandler());
        patternSelector.getElement().getStyle().setProperty("width", "215px");
        patternNameDTOMap = new HashMap<String, WindPatternDTO>();
        patternDisplayMap = new HashMap<String, WindPatternDisplay>();
        patternPanelMap = new HashMap<String, Panel>();
        currentWPDisplay = null;
        currentWPPanel = null;

        boatSelector = new ListBox();
        boatSelector.getElement().getStyle().setProperty("width", "215px");
        directionSelector = new ListBox();
        directionSelector.getElement().getStyle().setProperty("width", "215px");
        windParams = new WindFieldGenParamsDTO();
        timer = new Timer(PlayModes.Replay, 1000l);
        timer.setAutoAdvance(false);
        //timer.setTime(windParams.getStartTime().getTime());
        // TO DO make it work for no time panel display
        FlowPanel timeSliderWrapperPanel = new FlowPanel();
        
        /*timePanel = new TimePanel<TimePanelSettings>(timer, stringMessages);
        timeSliderWrapperPanel.getElement().getStyle().setProperty("height", "20%");
        timeSliderWrapperPanel.getElement().setClassName("timeSliderWrapperPanel");
        timeSliderWrapperPanel.add(timePanel);
        timePanel.setVisible(false);*/
        timePanel = new SimulatorTimePanel(timer, stringMessages, windParams);
        initTimer();
        timer.setTime(windParams.getStartTime().getTime());
        timePanel.setActive(false);

        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        //LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.simulator(), stringMessages);
        //logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        //this.addNorth(logoAndTitlePanel, 68);

        // leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();

        rightPanel.add(timeSliderWrapperPanel);

        this.addWest(leftPanel, 470);
        // leftPanel.getElement().getStyle().setFloat(Style.Float.LEFT);

        this.add(rightPanel);
        // rightPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);

    }

    private void createOptionsPanelTop() {
        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        optionsPanel.setTitle("Optionsbar");
        optionsPanel.getElement().setClassName("optionsPanel");
        Label options = new Label(stringMessages.optionsBar());
        options.getElement().setClassName("sectorHeadline");
        optionsPanel.setSize("100%", "45px");
        optionsPanel.add(options);
        optionsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        initUpdateButton();
        // updateButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        optionsPanel.add(updateButton);

        leftPanel.add(optionsPanel);
    }

    private void createOptionsPanel() {
        FlowPanel controlPanel = new FlowPanel();
        FlowPanel controlPanelInnerWrapper = new FlowPanel();
        controlPanelInnerWrapper.getElement().setClassName("controlPanelInnerWrapper");
        controlPanel.setTitle("Control Settings");
        controlPanel.getElement().setClassName("controlPanel");
        controlPanel.getElement().setId("masterPanelLeft");
        controlPanel.add(controlPanelInnerWrapper);
        createWindSetup(controlPanelInnerWrapper);
        createSailingSetup(controlPanelInnerWrapper);
        leftPanel.add(controlPanel);

    }

    private void createWindSetup(Panel controlPanel) {
        windPanel = new VerticalPanel();

        controlPanel.add(windPanel);
        windPanel.getElement().setClassName("windPanel");
        String windSetup = stringMessages.wind() + " " + stringMessages.setup();
        Label windSetupLabel = new Label(windSetup);
        windSetupLabel.getElement().setClassName("innerHeadline");
        windPanel.add(windSetupLabel);

        HorizontalPanel hp = new HorizontalPanel();

        Label pattern = new Label(stringMessages.pattern());
        hp.add(pattern);

        simulatorSvc.getWindPatterns(new AsyncCallback<List<WindPatternDTO>>() {

            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed to initialize wind patterns\n" + message.getMessage());
            }

            @Override
            public void onSuccess(List<WindPatternDTO> patterns) {
                for (WindPatternDTO p : patterns) {
                    patternSelector.addItem(p.getDisplayName());
                    patternNameDTOMap.put(p.getDisplayName(), p);
                }
            }

        });
        // patternSelector.setItemSelected(0, true);
        hp.add(patternSelector);
        windPanel.add(hp);
        hp.getElement().setClassName("choosePattern");

        // addSlider(windPanel, stringMessages.strength(), 1, 10, wControls.windSpeedInKnots, new WindSpeedCapture());

    }

    private Panel getWindControlPanel() {
        assert (currentWPDisplay != null);
        VerticalPanel windControlPanel = new VerticalPanel();
        windControlPanel.getElement().setClassName("windControLPanel");
        // windControlPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        for (WindPatternSetting<?> s : currentWPDisplay.getSettings()) {
            switch (s.getDisplayWidgetType()) {
            case SLIDERBAR:
                @SuppressWarnings("unused")
                Panel sliderPanel = getSliderPanel(windControlPanel, s);
                // windControlPanel.add(sliderPanel);
                // sliderPanel.getElement().getStyle().setFloat(Style.Float.NONE);
                break;
            case LISTBOX:
                logger.info("We have a listbox " + s);
                break;
            default:
                break;
            }
        }
        return windControlPanel;
    }

    private Panel getSliderPanel(Panel parentPanel, WindPatternSetting<?> s) {

        String labelName = s.getDisplayName();
        double minValue = (Double) s.getMin();
        double maxValue = (Double) s.getMax();
        double defaultValue = (Double) s.getDefault();

        FlowPanel vp = new FlowPanel();
        vp.getElement().setClassName("sliderWrapper");
        Label label = new Label(labelName);
        label.setWordWrap(true);

        vp.add(label);
        label.getElement().setClassName("sliderLabel");

        final SliderBar sliderBar = new SliderBar(minValue, maxValue);

        sliderBar.getElement().getStyle().setProperty("width", "216px");

        sliderBar.setStepSize(Math.round(maxValue / 10.), false);
        sliderBar.setNumTicks(10);
        sliderBar.setNumTickLabels(1);

        sliderBar.setEnabled(true);
        WindControlCapture handler = new WindControlCapture(sliderBar, s);
        sliderBar.addValueChangeHandler(handler);

        sliderBar.setLabelFormatter(new SliderBar.LabelFormatter() {

            @Override
            public String formatLabel(SliderBar slider, Double value, Double previousValue) {
                return String.valueOf(Math.round(value));
            }
        });

        sliderBar.setCurrentValue(defaultValue);
        sliderBar.setTitle(String.valueOf(Math.round(sliderBar.getCurrentValue())));
        vp.add(sliderBar);

        parentPanel.add(vp);
        label.getElement().getStyle().setFloat(Style.Float.LEFT);

        sliderBar.getElement().getStyle().setFloat(Style.Float.RIGHT);

        return vp;
    }

    private void createSailingSetup(Panel controlPanel) {

        VerticalPanel sailingPanel = new VerticalPanel();
        controlPanel.add(sailingPanel);
        sailingPanel.getElement().setClassName("sailingPanel");
        String sailingSetup = stringMessages.sailing() + " " + stringMessages.setup();
        Label sailingSetupLabel = new Label(sailingSetup);
        sailingSetupLabel.getElement().setClassName("innerHeadline");

        sailingPanel.add(sailingSetupLabel);

        Label boatClassLabel = new Label(stringMessages.boatClass());
        boatClassLabel.getElement().setClassName("boatClassLabel");
        HorizontalPanel hp = new HorizontalPanel();
        hp.getElement().setClassName("boatClassPanel");
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        hp.add(boatClassLabel);

        simulatorSvc.getBoatClasses(new AsyncCallback<BoatClassDTO[]>() {

            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed to initialize boat classes\n" + message.getMessage());
            }

            @Override
            public void onSuccess(BoatClassDTO[] boatClasses) {
                for (int i = 0; i < boatClasses.length; ++i) {
                    boatSelector.addItem(boatClasses[i].toString());
                }
            }

        });
        hp.add(boatSelector);

        sailingPanel.add(hp);
        // hp.setSize("80%", "10%");
        // hp.setWidth("80%");
        Panel raceDirection = createRaceDirectionSelector();
        sailingPanel.add(raceDirection);
        // raceDirection.setWidth("80%");

        Panel strategySelector = createStrategySelector();
        sailingPanel.add(strategySelector);
        // strategySelector.setWidth("80%");
    }

    private void createMapOptionsPanel() {
        HorizontalPanel mapOptions = new HorizontalPanel();
        mapOptions.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        mapOptions.setSize("100%", "45px");
        mapOptions.setTitle("Maps");
        mapOptions.getElement().setClassName("mapOptions");

        Label mapsLabel = new Label(stringMessages.maps());
        mapsLabel.getElement().setClassName("sectorHeadline");
        mapOptions.add(mapsLabel);

        mapOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        initCourseInputButton();

        mapOptions.add(courseInputButton);
        mapOptions.add(busyIndicator);
        rightPanel.add(mapOptions);

        initDisplayOptions(mapOptions);

        simulatorMap = new SimulatorMap(simulatorSvc, stringMessages, errorReporter, xRes, yRes, timer, timePanel, windParams,
                busyIndicator);

        // FlowPanel mapPanel = new FlowPanel();
        // mapPanel.setTitle("Map");
        // mapPanel.setSize("100%", "92%");
        // mapPanel.add(mapw);
        // mapw.setSize("100%", "100%");

        simulatorMap.setSize("100%", "100%");
        rightPanel.add(simulatorMap);

    }

    // initialize timer with a default time span based on windParams
    private void initTimer() {
        Date startDate = windParams.getStartTime();
        if (timePanel != null) {
            timePanel.setMinMax(startDate, windParams.getEndTime(), false);
        }
    }

    private void initUpdateButton() {
        updateButton = new Button(stringMessages.update());
        updateButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                update();
            }
        });

    }

    private void update() {

        if (windDisplayButton.getValue()) {
            timePanel.setActive(true);
            simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, currentWPDisplay);
        } else if (summaryButton.getValue()) {
            timePanel.setActive(false);
            simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, currentWPDisplay);
        } else if (replayButton.getValue()) {
            timePanel.setActive(true);
            simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, currentWPDisplay);
        }

    }

    private void initCourseInputButton() {
        courseInputButton = new Button(stringMessages.startEnd());

        courseInputButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.reset();
                // TODO fix so that resetting the course displays the current selection
                // For now disable all button & force user to select
                summaryButton.setValue(false);
                replayButton.setValue(false);
                windDisplayButton.setValue(false);
            }
        });

    }

    private void initDisplayOptions(Panel mapOptions) {
        
        summaryButton = new RadioButton("Map Display Options", stringMessages.summary());
        summaryButton.getElement().setClassName("MapDisplayOptions");

        summaryButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                //timePanel.setVisible(false);
                timePanel.setActive(false);
                simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, currentWPDisplay);
            }

        });

        replayButton = new RadioButton("Map Display Options", stringMessages.replay());
        replayButton.getElement().setClassName("MapDisplayOptions");

        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, currentWPDisplay);
                timePanel.setActive(true);
            }

        });

        windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " "
                + stringMessages.display());

        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, currentWPDisplay);
                timePanel.setActive(true);
            }

        });

        HorizontalPanel p = new HorizontalPanel();
        // p.add(busyIndicator);
        DecoratorPanel d = new DecoratorPanel();
        p.add(windDisplayButton);
        p.add(summaryButton);
        p.add(replayButton);
        // windDisplayButton.setValue(true);
        d.add(p);
        mapOptions.add(d);

    }

    private Panel createRaceDirectionSelector() {
        Label raceDirectionLabel = new Label(stringMessages.raceDirection());
        raceDirectionLabel.getElement().setClassName("boatClassLabel");
        HorizontalPanel hp = new HorizontalPanel();
        hp.getElement().setClassName("boatClassPanel");
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        hp.add(raceDirectionLabel);

        if (directionSelector != null) {
            directionSelector.addItem(stringMessages.upWind());
            directionSelector.addItem(stringMessages.downWind());
            hp.add(directionSelector);
        }
        return hp;
    }

    private Panel createStrategySelector() {
        FlowPanel fp = new FlowPanel();
        Label label = new Label(stringMessages.strategies());
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        fp.add(label);
        VerticalPanel vp = new VerticalPanel();
        vp.getElement().getStyle().setProperty("width", "215px");
        CheckBox cb = new CheckBox(stringMessages.omniscient());
        cb.setValue(true);
        // cb.getElement().getStyle().setFloat(Style.Float.RIGHT);
        vp.add(cb);
        cb = new CheckBox(stringMessages.opportunistic());
        cb.setValue(true);
        vp.add(cb);
        vp.getElement().getStyle().setFloat(Style.Float.RIGHT);
        fp.add(vp);
        return fp;

    }
    
    public Widget getTimeWidget() {
        return timePanel; 
    }

}
