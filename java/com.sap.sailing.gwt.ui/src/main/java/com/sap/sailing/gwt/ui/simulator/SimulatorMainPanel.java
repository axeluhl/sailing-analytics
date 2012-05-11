package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
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

import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO.WindPattern;

public class SimulatorMainPanel extends SplitLayoutPanel {

    private FlowPanel leftPanel = new FlowPanel();
    private FlowPanel rightPanel = new FlowPanel();

    private Button updateButton;
    private Button courseInputButton;

    private RadioButton summaryButton;
    private RadioButton replayButton;
    private RadioButton windDisplayButton;

    private WindControlParameters wControls = new WindControlParameters();

    private ListBox patternSelector = new ListBox();
    private ListBox boatSelector = new ListBox();

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    /**
     * Temporary place holders for parameters
     */
    private final double windSpeed = 7.2;
    private final double windBearing = 45;

    private SimulatorMap simulatorMap;
    private final StringMessages stringMessages;
    private final SimulatorServiceAsync simulatorSvc;

    public SimulatorMainPanel(SimulatorServiceAsync svc, StringMessages stringMessages) {
        // splitPanel = new SplitLayoutPanel();
        super();

        this.stringMessages = stringMessages;
        this.simulatorSvc = svc;

        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel(stringMessages.simulator(), stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        this.addNorth(logoAndTitlePanel, 68);

        // leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();

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
        String windSetup = stringMessages.wind() + " " + stringMessages.setup();
        Label windSetupLabel = new Label(windSetup);

        controlPanel.add(windSetupLabel);

        Label pattern = new Label(stringMessages.pattern());
        controlPanel.add(pattern);

        simulatorSvc.getWindPatterns(new AsyncCallback<WindPattern[]>() {

            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed to initialize wind patterns\n" + message);

            }

            @Override
            public void onSuccess(WindPattern[] patterns) {
                for (int i = 0; i < patterns.length; ++i) {
                    patternSelector.addItem(patterns[i].toString());
                }
            }

        });
        controlPanel.add(patternSelector);
        leftPanel.add(controlPanel);

    }

    private void createSailingSetup(Panel controlPanel) {

        String sailingSetup = stringMessages.sailing() + " " + stringMessages.setup();
        Label sailingSetupLabel = new Label(sailingSetup);

        controlPanel.add(sailingSetupLabel);

        Label boatClassLabel = new Label(stringMessages.boatClass());
        controlPanel.add(boatClassLabel);

        simulatorSvc.getWindPatterns(new AsyncCallback<WindPattern[]>() {

            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed to initialize wind patterns\n" + message);

            }

            @Override
            public void onSuccess(WindPattern[] patterns) {
                for (int i = 0; i < patterns.length; ++i) {
                    patternSelector.addItem(patterns[i].toString());
                }
            }

        });
        controlPanel.add(patternSelector);
        leftPanel.add(controlPanel);

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
        
        simulatorMap = new SimulatorMap(simulatorSvc, stringMessages);
        
        //FlowPanel mapPanel = new FlowPanel();
        //mapPanel.setTitle("Map");
        //mapPanel.setSize("100%", "92%");
        //mapPanel.add(mapw);
        //mapw.setSize("100%", "100%");
      
        simulatorMap.setSize("100%", "92%");
        rightPanel.add(simulatorMap);
    }

    private void initUpdateButton() {
        updateButton = new Button(stringMessages.update());
        updateButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if (windDisplayButton.getValue()) {
                    simulatorMap.refreshView(SimulatorMap.ViewName.WINDDISPLAY, wControls);
                } else if (summaryButton.getValue()) {
                    simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls);
                } else if (replayButton.getValue()) {
                    simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls);
                }

            }
        });

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
                simulatorMap.refreshView(SimulatorMap.ViewName.SUMMARY, wControls);
            }

        });

        replayButton = new RadioButton("Map Display Options", stringMessages.replay());

        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                simulatorMap.refreshView(SimulatorMap.ViewName.REPLAY, wControls);
            }

        });

        windDisplayButton = new RadioButton("Map Display Options", stringMessages.wind() + " "
                + stringMessages.display());
        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

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
