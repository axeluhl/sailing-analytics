package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimePanel;
import com.sap.sailing.gwt.ui.client.TimePanelSettings;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;

public class SimulatorMainPanel extends SplitLayoutPanel {

    private FlowPanel leftPanel;
    private FlowPanel rightPanel;
    private VerticalPanel windPanel;

    private Button updateButton;
    private Button courseInputButton;

    private RadioButton summaryButton;
    private RadioButton replayButton;
    private RadioButton windDisplayButton;
    private TimePanel<TimePanelSettings> timePanel;

    private WindControlParameters wControls;
    private Map<String, WindPatternDisplay> patternDisplayMap;
    private Map<String, Panel> patternPanelMap;
   
    private WindPatternDisplay currentWPDisplay;
    private Panel currentWPPanel;

    private ListBox patternSelector;
    private Map<String, WindPatternDTO> patternNameDTOMap;
    private ListBox boatSelector;

    private final Timer timer;
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    private SimulatorMap simulatorMap;
    private final StringMessages stringMessages;
    private final SimulatorServiceAsync simulatorSvc;
    private final ErrorReporter errorReporter;

    private class WindControlCapture implements ValueChangeHandler<Double> {

        private WindPatternSetting<?> setting;

        public WindControlCapture(WindPatternSetting<?> setting) {
            this.setting = setting;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Double> arg0) {
            logger.info("Slider value : " + arg0.getValue());
            setting.setValue(arg0.getValue());
            //update();
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
                currentWPPanel.setWidth("80%");
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
                currentWPPanel.setWidth("80%");
            } else {
                WindPatternDTO pattern = patternNameDTOMap.get(windPattern);
                simulatorSvc.getWindPatternDisplay(pattern, new PatternRetriever(windPattern));
            }

        }

    }

    public SimulatorMainPanel(SimulatorServiceAsync svc, StringMessages stringMessages, ErrorReporter errorReporter) {

        super();

        this.simulatorSvc = svc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;

        leftPanel = new FlowPanel();
        rightPanel = new FlowPanel();
        wControls = new WindControlParameters(1, 0);
        patternSelector = new ListBox();
        patternSelector.addChangeHandler(new PatternSelectorHandler());
        patternNameDTOMap = new HashMap<String, WindPatternDTO>();
        patternDisplayMap = new HashMap<String, WindPatternDisplay>();
        patternPanelMap = new HashMap<String, Panel>();
        currentWPDisplay = null;
        currentWPPanel = null;

        boatSelector = new ListBox();
        timer = new Timer(PlayModes.Replay, 1000l);
        timer.setPlaySpeedFactor(30);
        timePanel = new TimePanel<TimePanelSettings>(timer, stringMessages);
        resetTimer();

        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.simulator(), stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        this.addNorth(logoAndTitlePanel, 68);

        // leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();

        rightPanel.add(timePanel);

        this.addWest(leftPanel, 400);
        // leftPanel.getElement().getStyle().setFloat(Style.Float.LEFT);
        rightPanel.getElement().getStyle().setBackgroundColor("#e0e0e0");
        this.add(rightPanel);
        // rightPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);

    }

    private void createOptionsPanelTop() {
        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        optionsPanel.setTitle("Optionsbar");
        Label options = new Label(stringMessages.optionsBar());
        optionsPanel.setSize("100%", "7%");
        optionsPanel.add(options);
        optionsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        initUpdateButton();
        // updateButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        optionsPanel.add(updateButton);

        leftPanel.add(optionsPanel);
    }

    private void createOptionsPanel() {
        FlowPanel controlPanel = new FlowPanel();
        controlPanel.setSize("100%", "92%");
        controlPanel.setTitle("Control Settings");
        controlPanel.getElement().getStyle().setBackgroundColor(" #e0e0e0");
        createWindSetup(controlPanel);
        createSailingSetup(controlPanel);
        leftPanel.add(controlPanel);

    }

    private void createWindSetup(Panel controlPanel) {
        windPanel = new VerticalPanel();

        controlPanel.add(windPanel);
        windPanel.setSize("100%", "50%");
        String windSetup = stringMessages.wind() + " " + stringMessages.setup();
        Label windSetupLabel = new Label(windSetup);

        windPanel.add(windSetupLabel);

        HorizontalPanel hp = new HorizontalPanel();
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
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
        hp.setSize("80%", "10%");

        // addSlider(windPanel, stringMessages.strength(), 1, 10, wControls.windSpeedInKnots, new WindSpeedCapture());

    }

    private Panel getWindControlPanel() {
        assert (currentWPDisplay != null);
        VerticalPanel windControlPanel = new VerticalPanel();
        //windControlPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        for (WindPatternSetting<?> s : currentWPDisplay.getSettings()) {
            switch (s.getDisplayWidgetType()) {
            case SLIDERBAR:
                Panel sliderPanel = getSliderPanel(windControlPanel, s.getName(), (Double) s.getMin(), (Double) s.getMax(),
                        (Double) s.getDefault(), new WindControlCapture(s));
                //windControlPanel.add(sliderPanel);
                //sliderPanel.getElement().getStyle().setFloat(Style.Float.NONE);
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

    private Panel getSliderPanel(Panel parentPanel, String labelName, double minValue, double maxValue, double defaultValue,
            final ValueChangeHandler<Double> handler) {

        FlowPanel vp = new FlowPanel();
        Label label = new Label(labelName);
        label.getElement().getStyle().setVerticalAlign(VerticalAlign.TEXT_BOTTOM);
        vp.add(label);
        label.setWordWrap(true);

        final SliderBar sliderBar = new SliderBar(minValue, maxValue);

        sliderBar.setStepSize(maxValue/10, false);
        sliderBar.setNumTicks(10);
        sliderBar.setNumTickLabels(1);
        sliderBar.setTitle(labelName);
        sliderBar.setEnabled(true);
        sliderBar.addValueChangeHandler(handler);

        sliderBar.setLabelFormatter(new SliderBar.LabelFormatter() {
            @Override
            public String formatLabel(SliderBar slider, double value) {
                return String.valueOf(Math.round(value));
            }
        });

        sliderBar.setCurrentValue(defaultValue);
        vp.add(sliderBar);
      
        parentPanel.add(vp);
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
       
        sliderBar.getElement().getStyle().setFloat(Style.Float.RIGHT);
        sliderBar.setWidth("60%");
        sliderBar.setHeight("25px");
        
      
        return vp;
    }

    private void createSailingSetup(Panel controlPanel) {

        VerticalPanel sailingPanel = new VerticalPanel();
        controlPanel.add(sailingPanel);
        sailingPanel.setSize("100%", "50%");
        String sailingSetup = stringMessages.sailing() + " " + stringMessages.setup();
        Label sailingSetupLabel = new Label(sailingSetup);

        sailingPanel.add(sailingSetupLabel);

        Label boatClassLabel = new Label(stringMessages.boatClass());
        HorizontalPanel hp = new HorizontalPanel();
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
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
        hp.setSize("80%", "10%");

    }

    private void createMapOptionsPanel() {
        HorizontalPanel mapOptions = new HorizontalPanel();
        mapOptions.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        mapOptions.setSize("100%", "7%");
        mapOptions.setTitle("Maps");
        Label mapsLabel = new Label(stringMessages.maps());
        mapOptions.add(mapsLabel);

        mapOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

        initCourseInputButton();

        mapOptions.add(courseInputButton);
        rightPanel.add(mapOptions);

        initDisplayOptions(mapOptions);

        simulatorMap = new SimulatorMap(simulatorSvc, stringMessages, errorReporter, timer);

        // FlowPanel mapPanel = new FlowPanel();
        // mapPanel.setTitle("Map");
        // mapPanel.setSize("100%", "92%");
        // mapPanel.add(mapw);
        // mapw.setSize("100%", "100%");

        simulatorMap.setSize("100%", "82%");
        rightPanel.add(simulatorMap);

    }

    // TODO Get the right dates and times
    private void resetTimer() {
        Date startDate = new Date(0);
        timer.setTime(startDate.getTime());
        if (timePanel != null) {
            timePanel.reset();

            Date now = timer.getTime();
            Date maxTime = new Date(now.getTime() + 10 * 60 * 1000);

            timePanel.setMinMax(now, maxTime, false);
            timePanel.setVisible(false);
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
        resetTimer();

        if (windDisplayButton.getValue()) {
            timePanel.setVisible(true);
            simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, wControls, currentWPDisplay);
        } else if (summaryButton.getValue()) {
            timePanel.setVisible(false);
            simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls, currentWPDisplay);
        } else if (replayButton.getValue()) {
            timePanel.setVisible(true);
            simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls, currentWPDisplay);
        }

    }

    private void initCourseInputButton() {
        courseInputButton = new Button(stringMessages.startEnd());

        courseInputButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.reset();

            }
        });

    }

    private void initDisplayOptions(Panel mapOptions) {
        summaryButton = new RadioButton("Map Display Options", stringMessages.summary());

        summaryButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                timePanel.setVisible(false);
                simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls, currentWPDisplay);
            }

        });

        replayButton = new RadioButton("Map Display Options", stringMessages.replay());

        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                resetTimer();
                timePanel.setVisible(true);
                simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls, currentWPDisplay);
            }

        });

        windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " "
                + stringMessages.display());
        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                resetTimer();
                timePanel.setVisible(true);
                simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, wControls, currentWPDisplay);
            }

        });

        HorizontalPanel p = new HorizontalPanel();

        DecoratorPanel d = new DecoratorPanel();
        p.add(summaryButton);
        p.add(replayButton);
        p.add(windDisplayButton);
        // windDisplayButton.setValue(true);
        d.add(p);
        mapOptions.add(d);

    }

}
