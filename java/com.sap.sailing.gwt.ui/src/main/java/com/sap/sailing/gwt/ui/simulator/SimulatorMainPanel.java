package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;

public class SimulatorMainPanel extends SplitLayoutPanel {


    private FlowPanel leftPanel = new FlowPanel();
    private FlowPanel rightPanel = new FlowPanel();
    
    Button updateButton;
    Button courseInputButton;
    
    RadioButton summaryButton = new RadioButton("Map Display Options", "Summary");
    RadioButton replayButton = new RadioButton("Map Display Options", "Replay");
    RadioButton windDisplayButton = new RadioButton("Map Display Options", "Wind Display");
    
    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
    
    private MapWidget mapw;
    private SimulatorServiceAsync simulatorSvc;
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");
   
    
    public SimulatorMainPanel(MapWidget mapw, StringMessages stringMessages,SimulatorServiceAsync svc) {
        //splitPanel  = new SplitLayoutPanel();
        super();
        this.mapw = mapw;
        this.simulatorSvc = svc;
        
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel("Simulator", stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        this.addNorth(logoAndTitlePanel,68);
        
        //leftPanel.getElement().getStyle().setBackgroundColor("#4f4f4f");
        createOptionsPanelTop();
        createOptionsPanel();
        createMapOptionsPanel();
       
        
        this.addWest(leftPanel, 400);
        //leftPanel.getElement().getStyle().setFloat(Style.Float.LEFT);
        rightPanel.getElement().getStyle().setBackgroundColor("#e0e0e0");
        this.add(rightPanel);
        //rightPanel.getElement().getStyle().setFloat(Style.Float.RIGHT);
        addOverlays();
        
    }
    
    private void createOptionsPanelTop() {
        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        
        optionsPanel.setTitle("Optionsbar");
        Label options = new Label("Optionsbar");
        optionsPanel.setSize("100%", "7%");
        optionsPanel.add(options);
        optionsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        initUpdateButton();
        //updateButton.getElement().getStyle().setFloat(Style.Float.RIGHT);
        optionsPanel.add(updateButton);
        
        leftPanel.add(optionsPanel);
    }
    
    private void createOptionsPanel() {
        FlowPanel controlPanel = new FlowPanel();
        controlPanel.setSize("100%", "92%");
        controlPanel.setTitle("Control Settings");
        controlPanel.getElement().getStyle().setBackgroundColor(" #e0e0e0");
        Label windSetupLabel = new Label("Wind Setup");
       
        controlPanel.add(windSetupLabel);
        leftPanel.add(controlPanel);
        
    }
    
    private void createMapOptionsPanel() {
        HorizontalPanel mapOptions = new HorizontalPanel();
        mapOptions.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        mapOptions.setSize("100%", "7%");
        mapOptions.setTitle("Maps");
        Label mapsLabel = new Label("Maps");
        mapOptions.add(mapsLabel);
        
        mapOptions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        
        initCourseInputButton();
        
        mapOptions.add(courseInputButton);
        rightPanel.add(mapOptions);
        
        initDisplayOptions(mapOptions);
        
        FlowPanel mapPanel = new FlowPanel();
        mapPanel.setTitle("Map");
        
        mapPanel.add(mapw);
        //mapPanel.setSize("100%", "80%");
        rightPanel.add(mapPanel);
    }
    
    private void addOverlays() {
        raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        mapw.addOverlay(raceCourseCanvasOverlay);
        
        windFieldCanvasOverlay = new WindFieldCanvasOverlay();
        //mapw.addOverlay(windFieldCanvasOverlay);   
    }
    
    private void generateWindField() {
        logger.info("In generateWindField");
        WindFieldGenParamsDTO params = new WindFieldGenParamsDTO();
        
        PositionDTO startPointDTO = new PositionDTO(raceCourseCanvasOverlay.startPoint.getLatitude(), raceCourseCanvasOverlay.startPoint.getLongitude());
        PositionDTO endPointDTO =  new PositionDTO(raceCourseCanvasOverlay.endPoint.getLatitude(), raceCourseCanvasOverlay.endPoint.getLongitude());
       
        params.setNorthWest(startPointDTO);
        params.setSouthEast(endPointDTO);
        params.setxRes(5);
        params.setyRes(5);
        params.setWindBearing(45);
        params.setWindSpeed(7.2);
        
        simulatorSvc.getWindField(params, new AsyncCallback<WindFieldDTO>() {
            @Override
            public void onFailure(Throwable message) {
                Window.alert("Failed servlet call to SimulatorService\n" + message);
            }

            @Override
            public void onSuccess(WindFieldDTO wl) {
                logger.info("Number of windDTO : " + wl.getMatrix().size());
                refreshWindFieldOverlay(wl);
            }
        });
        
    }
    
    private void refreshWindFieldOverlay(WindFieldDTO wl) {
        windFieldCanvasOverlay.setWindField(wl);
        windFieldCanvasOverlay.redraw(true);
   
     }
    
    private void initUpdateButton() {
        updateButton = new Button("Update");
        updateButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
              
            
                if (raceCourseCanvasOverlay.isCourseSet()) {
                    if (windDisplayButton.isChecked()) {
                        mapw.addOverlay(windFieldCanvasOverlay);   
                        generateWindField();    
                    //raceCourseCanvasOverlay.setVisible(true);
                    }
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            
            }
        });
        
    }
    
    private void initCourseInputButton() {
       courseInputButton = new Button("Start-End");
        
        courseInputButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
 
                mapw.removeOverlay(windFieldCanvasOverlay);   
                
                //raceCourseCanvasOverlay.setSelected(true);
                //raceCourseCanvasOverlay.setVisible(true);
                raceCourseCanvasOverlay.reset();
                raceCourseCanvasOverlay.redraw(true);   
                
            }
            });
        
    }
    
    private void initDisplayOptions(Panel mapOptions) {
        
       
        summaryButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                mapw.removeOverlay(windFieldCanvasOverlay); 
                
            }
            
        });
        
        replayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                mapw.removeOverlay(windFieldCanvasOverlay); 
                
            }
            
        });
        
        windDisplayButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                if (raceCourseCanvasOverlay.isCourseSet()) {
                    if (windDisplayButton.isChecked()) {
                        mapw.addOverlay(windFieldCanvasOverlay);   
                        generateWindField();    
                    //raceCourseCanvasOverlay.setVisible(true);
                    }
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            }
            
        });
        
        HorizontalPanel p = new HorizontalPanel();
        
        DecoratorPanel d = new DecoratorPanel();
        p.add(summaryButton);
        p.add(replayButton);
        p.add(windDisplayButton);
        d.add(p);
        mapOptions.add(d);
      
    }
}
