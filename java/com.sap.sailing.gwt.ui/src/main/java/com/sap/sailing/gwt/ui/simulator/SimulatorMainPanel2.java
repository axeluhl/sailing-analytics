package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
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
import com.sap.sailing.gwt.ui.shared.BoatClassDTOsAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.PolarDiagramDTOAndNotificationMessage;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import com.sap.sailing.gwt.ui.shared.WindPatternDTO;
import com.sap.sailing.gwt.ui.shared.controls.slider.SliderBar;
import com.sap.sailing.gwt.ui.shared.panels.SimpleBusyIndicator;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternDisplay;
import com.sap.sailing.gwt.ui.shared.windpattern.WindPatternSetting;
import com.sap.sailing.simulator.util.SailingSimulatorUtil;

public class SimulatorMainPanel2 extends SplitLayoutPanel {

    private FlowPanel leftPanel;
    private FlowPanel rightPanel;
    private VerticalPanel windPanel;

    private Button updateButton;
    private Button courseInputButton;

    private RadioButton summaryButton;
    private RadioButton replayButton;
    private RadioButton windDisplayButton;
    // private TimePanel<TimePanelSettings> timePanel;

    private Map<String, WindPatternDisplay> patternDisplayMap;
    private Map<String, Panel> patternPanelMap;

    private WindPatternDisplay currentWPDisplay;
    private Panel currentWPPanel;

    private ListBox patternSelector;
    private PatternSelectorHandler patternSelectorHandler;
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
    private final char mode;
    private final boolean showGrid;

    private SimulatorTimePanel timePanel;

    private CheckBox isOmniscient;
    private CheckBox isOpportunistic;
    private Chart chart;
    // private final Timer timer;
    
    //I077899 - Mihai Bogdan Eugen    
    private boolean warningAlreadyShown = false;
    
    //I077899 - Mihai Bogdan Eugen
    private DialogBox polarDiagramDialogBox;
    
    //I077899 - Mihai Bogdan Eugen
    private Button polarDiagramDialogCloseButton;

    //I077899 - Mihai Bogdan Eugen
    private VerticalPanel polarDiv;
    
    //I077899 - Mihai Bogdan Eugen
    private BoatClassDTO[] boatClasses = new BoatClassDTO[0];

    //I077899 - Mihai Bogdan Eugen
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
                update(boatSelector.getSelectedIndex());
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
            int xRes, int yRes, boolean autoUpdate, char mode, boolean showGrid) {

        super();

        this.simulatorSvc = svc;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.xRes = xRes;
        this.yRes = yRes;
        this.autoUpdate = autoUpdate;
        this.mode = mode;
        this.showGrid = showGrid;
        this.isOmniscient = new CheckBox(this.stringMessages.omniscient(), true);
        this.isOmniscient.setValue(true);

        this.isOpportunistic = new CheckBox(this.stringMessages.opportunistic(), true);
        this.isOpportunistic.setValue(true);

        leftPanel = new FlowPanel();
        rightPanel = new FlowPanel();
        patternSelector = new ListBox();
        patternSelectorHandler = new PatternSelectorHandler();
        patternSelector.addChangeHandler(patternSelectorHandler);
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
        windParams.setMode(mode);
        timer = new Timer(PlayModes.Replay, 1000l);
        timer.setAutoAdvance(false);
        // timer.setTime(windParams.getStartTime().getTime());
        // TO DO make it work for no time panel display
        FlowPanel timeSliderWrapperPanel = new FlowPanel();

        /*
         * timePanel = new TimePanel<TimePanelSettings>(timer, stringMessages);
         * timeSliderWrapperPanel.getElement().getStyle().setProperty("height", "20%");
         * timeSliderWrapperPanel.getElement().setClassName("timeSliderWrapperPanel" );
         * timeSliderWrapperPanel.add(timePanel); timePanel.setVisible(false);
         */
        timePanel = new SimulatorTimePanel(timer, stringMessages, windParams);
        initTimer();
        timer.setTime(windParams.getStartTime().getTime());
        timePanel.setActive(false);

        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        // LogoAndTitlePanel logoAndTitlePanel = new
        // LogoAndTitlePanel(stringMessages.simulator(), stringMessages);
        // logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        // this.addNorth(logoAndTitlePanel, 68);

        // leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();

        rightPanel.add(timeSliderWrapperPanel);

        this.addWest(leftPanel, 470);
        // leftPanel.getElement().getStyle().setFloat(Style.Float.LEFT);

        this.add(rightPanel);
        // rightPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);
        
        this.polarDiagramDialogBox = this.createPolarDiagramDialogBox();

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
        this.createSailingSetup(controlPanelInnerWrapper);
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
                    if ((mode != SailingSimulatorUtil.freestyle)||(!p.name.equals("MEASURED"))) {
                        patternSelector.addItem(p.getDisplayName());
                        patternNameDTOMap.put(p.getDisplayName(), p);
                    }
                }
                if (mode == SailingSimulatorUtil.measured) {
                    patternSelector.setItemSelected(patternSelector.getItemCount()-1, true);
                    patternSelectorHandler.onChange(null);
                }
            }

        });
        hp.add(patternSelector);
        windPanel.add(hp);
        hp.getElement().setClassName("choosePattern");

        // addSlider(windPanel, stringMessages.strength(), 1, 10,
        // wControls.windSpeedInKnots, new WindSpeedCapture());

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

        sliderBar.setStepSize(Math.round((maxValue - minValue) / 10.), false);
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

        if (mode != SailingSimulatorUtil.measured) {
            mapOptions.add(courseInputButton);
        }
        mapOptions.add(busyIndicator);
        rightPanel.add(mapOptions);

        initDisplayOptions(mapOptions);

        simulatorMap = new SimulatorMap(simulatorSvc, stringMessages, errorReporter, xRes, yRes, timer, timePanel,
                windParams, busyIndicator, mode, showGrid);
        simulatorMap.setSize("100%", "100%");

        this.rightPanel.add(this.simulatorMap);
    }

    // initialize timer with a default time span based on windParams
    private void initTimer() {
        Date startDate = windParams.getStartTime();
        if (timePanel != null) {
            timePanel.setMinMax(startDate, windParams.getEndTime(), false);
        }
    }
    
    private void initCourseInputButton() {
        courseInputButton = new Button(stringMessages.startEnd());

        courseInputButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.reset();
                // TODO fix so that resetting the course displays the current
                // selection
                // For now disable all button & force user to select
                summaryButton.setValue(false);
                replayButton.setValue(false);
                windDisplayButton.setValue(false);
            }
        });

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
        Label label = new Label(stringMessages.strategies());
        label.getElement().getStyle().setFloat(Style.Float.LEFT);

        FlowPanel fp = new FlowPanel();
        fp.add(label);

        VerticalPanel vp = new VerticalPanel();
        vp.getElement().getStyle().setProperty("width", "215px");

        vp.add(this.isOmniscient);
        vp.add(this.isOpportunistic);
        vp.getElement().getStyle().setFloat(Style.Float.RIGHT);
        fp.add(vp);

        return fp;

    }

    public Widget getTimeWidget() {
        return timePanel;
    }

    //I077899 - Mihai Bogdan Eugen
    private Panel createPolarSelector() {
    	
        HorizontalPanel polarDiagramPanel = new HorizontalPanel();
        polarDiagramPanel.getElement().setClassName("boatClassPanel");
        polarDiagramPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

        Label polarShowLabel = new Label(stringMessages.showHideComponent(""));
        polarShowLabel.getElement().setClassName("boatClassLabel");
        polarDiagramPanel.add(polarShowLabel);

        final CheckBox cb = new CheckBox("");
        cb.setValue(false);
        cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean checked = ((CheckBox) event.getSource()).getValue();

                polarDiv.setVisible(checked);
                
                if(checked) {
                	
                	//TODO: change the hardcoded values bellow...
                	
                	polarDiagramDialogBox.setPopupPositionAndShow(new PositionCallback() {
                		public void setPosition(int offsetWidth, int offsetHeight) {
                	        
                			int width = Window.getClientWidth() - 550;
                			int height = Window.getClientHeight() - 525;

                	        polarDiagramDialogBox.setPopupPosition(width, height);
                	      }
                	});
                	
                    polarDiagramDialogCloseButton.setFocus(true);
                    cb.setValue(false);
                }
            }
        });

        polarDiagramPanel.add(cb);

        return polarDiagramPanel;
    }
    
    //I077899 - Mihai Bogdan Eugen
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

        this.simulatorSvc.getBoatClasses(new AsyncCallback<BoatClassDTOsAndNotificationMessage>() {
            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }

            @Override
            public void onSuccess(BoatClassDTOsAndNotificationMessage response) {
            	String notificationMessage = response.getNotificationMessage();
            	if(notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
        			errorReporter.reportNotification(response.getNotificationMessage());
        			warningAlreadyShown = true;
            	}
            	
            	boatClasses = response.getBoatClassDTOs();
                for (int i = 0; i < boatClasses.length; ++i) {
                    boatSelector.addItem(boatClasses[i].name);
                }
                boatSelector.setItemSelected(3, true); // polar diagram 49er STG
                loadPolarDiagramData(3);
            }
        });

        this.boatSelector.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent evnet) {
                int selectedIndex = boatSelector.getSelectedIndex();
                loadPolarDiagramData(selectedIndex);
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
        // I077721
        String polarString = stringMessages.simulatorPolarHeader();
        final Label polarSetup = new Label(polarString);
        polarSetup.getElement().setClassName("innerHeadline");
        sailingPanel.add(polarSetup);

        Panel polarShow = createPolarSelector();
        sailingPanel.add(polarShow);
        
        this.polarDiagramDialogCloseButton = new Button("Close"); 
        this.polarDiagramDialogCloseButton.getElement().setId("closeButton");
        
        this.polarDiv = new VerticalPanel();
        this.polarDiv.getElement().setClassName("polarDiv");
        this.polarDiv.setVisible(false);

        //this.loadPolarDiagramData(0);

        sailingPanel.add(polarDiv);
    }

    //I077899 - Mihai Bogdan Eugen
    private void loadPolarDiagramData(final int selectedBoatClass) {

        this.simulatorSvc.getBoatClasses(new AsyncCallback<BoatClassDTOsAndNotificationMessage>() {
            @Override
            public void onFailure(Throwable error) {
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }
            @Override
            public void onSuccess(BoatClassDTOsAndNotificationMessage boatClassesAndMsg) {
            	String notificationMessage = boatClassesAndMsg.getNotificationMessage();
            	if(notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
        			errorReporter.reportNotification(boatClassesAndMsg.getNotificationMessage());
        			warningAlreadyShown = true;
            	}
            	
            	boatClasses = boatClassesAndMsg.getBoatClassDTOs();
            	chart.setChartTitleText(boatClasses[selectedBoatClass].name);
            }
        });
        
    	
        if (this.chart != null) {
        	this.polarDiv.remove(this.chart);
        }
        this.chart = new Chart()
        		.setType(Series.Type.LINE)
        		.setChartTitleText("Polar diagram test")
        		.setWidth(450)
                .setHeight(300)
                .setOption("/chart/polar", true)
                .setOption("pane/startAngle", 0)
                .setOption("pane/endAngle", 360)
                .setOption("exporting/enableImages", true)
                .setOption("plotOptions/line/lineWidth", 1)
                .setOption("plotOptions/line/marker/enabled", false)
                .setMarginRight(2);

        this.simulatorSvc.getPolarDiagramDTO(5.0, selectedBoatClass, new AsyncCallback<PolarDiagramDTOAndNotificationMessage>() {
            @Override
            public void onFailure(Throwable error) {
            		
                errorReporter.reportError("Failed to initialize boat classes!\r\n" + error.getMessage());
            }
            @Override
            public void onSuccess(PolarDiagramDTOAndNotificationMessage polar) {
            	String notificationMessage = polar.getNotificationMessage();
            	if(notificationMessage != "" && notificationMessage.length() != 0 && warningAlreadyShown == false) {
        			errorReporter.reportNotification(polar.getNotificationMessage());
        			warningAlreadyShown = true;
            	}
            	
                Number[][] Nseries = polar.getPolarDiagramDTO().getNumberSeries();
                int[] windSpeedCatalog = new int[] { 6, 8, 10, 12, 14, 16, 20 };
                PolarChartColorRange cc = new PolarChartColorRange(Nseries.length+1);
                ArrayList<String> windSpeedColor = cc.GetColors();
                Series ser = chart.createSeries();

                for (int i = 0; i < Nseries.length; i++) {
                    ser = chart.createSeries();
                    ser.setName("" + windSpeedCatalog[i] + " kn");
                    ser.setPoints(Nseries[i]);
                    ser.setOption("color", windSpeedColor.get(i));
                    chart.addSeries(ser);
                }
                
                chart.getXAxis().setTickInterval(10);
                chart.getYAxis().setMin(0);
                chart.setOption("plotOptions/series/pointInterval", 360.0 / (Nseries[0].length));
                chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        String labelD = "";
                        if (axisLabelsData.getValueAsLong() % 30 == 0) {
                            labelD = axisLabelsData.getValueAsLong() + "\u00B0";
                        }
                        return labelD;
                    }
                }));

                polarDiv.add(chart);
                polarDiv.add(polarDiagramDialogCloseButton);
            }
        });
    }

    //I077899 - Mihai Bogdan Eugen
    private void initUpdateButton() {
        this.updateButton = new Button(stringMessages.update());
        this.updateButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                update(boatSelector.getSelectedIndex());
            }
        });
    }

    //I077899 - Mihai Bogdan Eugen
    private void update(int boatClassIndex) {
        if (this.windDisplayButton.getValue()) {
            this.timePanel.setActive(true);
            this.simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, this.currentWPDisplay, boatClassIndex, true);
        } else if (this.summaryButton.getValue()) {
        	this.timePanel.setActive(false);
        	this.simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, this.currentWPDisplay, boatClassIndex, true);
        } else if (this.replayButton.getValue()) {
        	this.timePanel.setActive(true);
        	this.simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, this.currentWPDisplay, boatClassIndex, true);
        } else {
            if (this.mode == SailingSimulatorUtil.measured) {
            	this.timePanel.setActive(false);
            	this.simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, this.currentWPDisplay, boatClassIndex, true);
            }
        }
    }    
    
    //I077899 - Mihai Bogdan Eugen
    private void initDisplayOptions(Panel mapOptions) {
    	
        this.summaryButton = new RadioButton("Map Display Options", stringMessages.summary());
        this.summaryButton.getElement().setClassName("MapDisplayOptions");
        this.summaryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                // timePanel.setVisible(false);    
                timePanel.setActive(false);
                simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, currentWPDisplay, boatSelector.getSelectedIndex(),false);
            }
        });

        this.replayButton = new RadioButton("Map Display Options", stringMessages.replay());
        this.replayButton.getElement().setClassName("MapDisplayOptions");
        this.replayButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, currentWPDisplay, boatSelector.getSelectedIndex(),false);
                timePanel.setActive(true);
            }
        });

        this.windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " " + stringMessages.display());
        this.windDisplayButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, currentWPDisplay, boatSelector.getSelectedIndex(),false);
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

    //I077899 - Mihai Bogdan Eugen
    private DialogBox createPolarDiagramDialogBox() {
    	
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Polar Diagram"); 
        
        dialogBox.setAnimationEnabled(true);
        dialogBox.setAutoHideEnabled(false);
        dialogBox.setModal(false);        
        
        dialogBox.setWidget(this.polarDiv);


        this.polarDiagramDialogCloseButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	dialogBox.hide();
            }
        });
        
        return dialogBox;
    }
}
