package com.sap.sailing.gwt.ui.simulator;

import java.util.Date;
import java.util.List;
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


public class SimulatorMainPanel extends SplitLayoutPanel {

    private FlowPanel leftPanel;
    private FlowPanel rightPanel;

    private Button updateButton;
    private Button courseInputButton;

    private RadioButton summaryButton;
    private RadioButton replayButton;
    private RadioButton windDisplayButton;
    private TimePanel<TimePanelSettings> timePanel;
    
    private WindControlParameters wControls;

    private ListBox patternSelector;
    private ListBox boatSelector;
    
    private final Timer timer;
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    private SimulatorMap simulatorMap;
    private final StringMessages stringMessages;
    private final SimulatorServiceAsync simulatorSvc;
    private final ErrorReporter errorReporter;
    
    private class WindSpeedCapture implements ValueChangeHandler<Double> {

        @Override
        public void onValueChange(ValueChangeEvent<Double> arg0) {
            logger.info("Slider value : " + arg0.getValue());
            wControls.windSpeedInKnots = arg0.getValue();
            update();
        }

    }
    
    private class PatternSelectorHandler implements ChangeHandler {

        @Override
        public void onChange(ChangeEvent arg0) {
            logger.info(patternSelector.getItemText(patternSelector.getSelectedIndex()));
            WindPatternDTO pattern = new WindPatternDTO(patternSelector.getItemText(patternSelector.getSelectedIndex()));
            
            simulatorSvc.getWindPatternDisplay(pattern, new AsyncCallback<WindPatternDisplay>() {

                @Override
                public void onFailure(Throwable message) {
                   errorReporter.reportError("Error retreiving wind patterns" + message.getMessage());
                    
                }

                @Override
                public void onSuccess(final WindPatternDisplay display) {
                    logger.info(display.getSettings().toString());
                }
                
            });
        
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
        VerticalPanel windPanel = new VerticalPanel();

        controlPanel.add(windPanel);
        windPanel.setSize("100%", "50%");
        String windSetup = stringMessages.wind() + " " + stringMessages.setup();
        Label windSetupLabel = new Label(windSetup);

        windPanel.add(windSetupLabel);

        HorizontalPanel hp = new HorizontalPanel();
        hp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        Label pattern = new Label(stringMessages.pattern());
        hp.add(pattern);

        simulatorSvc.getWindPatterns(new AsyncCallback<List<WindPatternDTO>> () {

            @Override
            public void onFailure(Throwable message) {
                errorReporter.reportError("Failed to initialize wind patterns\n" + message.getMessage());
            }

            @Override
            public void onSuccess(List<WindPatternDTO> patterns) {
                for (WindPatternDTO p : patterns) {
                    patternSelector.addItem(p.name);
                }
            }

        });
        hp.add(patternSelector);
        windPanel.add(hp);
        hp.setSize("80%", "10%");

        addSlider(windPanel, stringMessages.strength(), 1, 10, new WindSpeedCapture());

    }

    private void addSlider(Panel parentPanel, String labelName, double minValue, double maxValue,
            final ValueChangeHandler<Double> handler) {
       FlowPanel vp = new FlowPanel();
        //vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        Label label = new Label(labelName);
        vp.add(label);
        //FlowPanel fp = new FlowPanel();
        final SliderBar sliderBar = new SliderBar(minValue, maxValue);

        sliderBar.setStepSize(1, false);
        sliderBar.setNumTicks((int) (maxValue - minValue));
        sliderBar.setNumTickLabels(1);
        sliderBar.setTitle(labelName);
        sliderBar.setEnabled(true);
        sliderBar.addValueChangeHandler(handler);
        /*
         * sliderBar.addValueChangeHandler(new ValueChangeHandler<Double>() {
         * 
         * @Override public void onValueChange(ValueChangeEvent<Double> arg0) { logger.info("Slider value : " +
         * arg0.getValue()); }
         * 
         * });
         */
        sliderBar.setLabelFormatter(new SliderBar.LabelFormatter() {
            @Override
            public String formatLabel(SliderBar slider, double value) {
                return String.valueOf(Math.round(value));
            }
        });
        // sliderBar.setMinValue(1.0, false);
        // sliderBar.setMaxValue(10.0, false);
        sliderBar.setCurrentValue(wControls.windSpeedInKnots);
        vp.add(sliderBar);
    //    sliderBar.setWidth("100%");
        //vp.add(fp);
        parentPanel.add(vp);
        label.getElement().getStyle().setFloat(Style.Float.LEFT);
        sliderBar.getElement().getStyle().setFloat(Style.Float.RIGHT);
        sliderBar.setWidth("60%");
        sliderBar.setHeight("25px");
    //    vp.setWidth("80%");
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
    

    
    //TODO Get the right dates and times
    private void resetTimer() {
        Date startDate = new Date(0); 
        timer.setTime(startDate.getTime());
        if (timePanel != null) {
            timePanel.reset();
       
            Date now = timer.getTime();
            Date maxTime = new Date(now.getTime()+10*60*1000);
       
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
            simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, wControls);
        } else if (summaryButton.getValue()) {
            timePanel.setVisible(false);
            simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls);
        } else if (replayButton.getValue()) {
            timePanel.setVisible(true);
            simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls);
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
                simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls);
            }

        });

        replayButton = new RadioButton("Map Display Options", stringMessages.replay());

        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                resetTimer();
                timePanel.setVisible(true);
                simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls);
            }

        });

        windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " "
                + stringMessages.display());
        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                resetTimer();
                timePanel.setVisible(true);
                simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, wControls);
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
