package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.client.LogoAndTitlePanel;
import com.sap.sailing.gwt.ui.client.SimulatorService;
import com.sap.sailing.gwt.ui.client.SimulatorServiceAsync;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldGenParamsDTO;
import java.util.logging.*;

public class SimulatorEntryPoint extends AbstractEntryPoint {
    private final SimulatorServiceAsync simulatorSvc = GWT.create(SimulatorService.class);

    //private final VerticalPanel panel = new VerticalPanel(); 
    private final DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
    private final ListBox raceSelector = new ListBox();
    private final CheckBox checkBox = new CheckBox("Show Grid");
    private RadioButton windDisplayButton;
    private RadioButton courseInputButton;
    private RadioButton summaryButton;
    private RadioButton replayButton;
    private MapWidget mapw;

    
    private List<PositionDTO> locations = new ArrayList<PositionDTO>();

    private WindFieldCanvasOverlay windFieldCanvasOverlay;
    private RaceCourseCanvasOverlay raceCourseCanvasOverlay;
    
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    @Override
    public void onModuleLoad() {
        super.onModuleLoad();
       
        /*
         * Asynchronously loads the Maps API.
         * 
         * The first parameter should be a valid Maps API Key to deploy this application on a public server, but a blank
         * key will work for an application served from localhost.
         */
        Maps.loadMapsApi("", "2", false, new Runnable() {
            public void run() {
                buildUi();
            }
        });
    }

    private void buildUi() {
        logger.severe("Logger Name : " + logger.getName() + " Logging level " + logger.getLevel());
        logger.fine("In buildUi");
        loadRaceLocations();
        
        initMap();
        initRaceSelector();
        initDisplayOptions();
        initButton();
        initPanel();

        addOverlays();
        
        
        // Add the map to the HTML host page
        RootLayoutPanel.get().add(panel);
        if (locations != null && locations.size() > 2) {
            selectRaceLocation(locations.get(2));
        }
        
    }

    private void addOverlays() {
        raceCourseCanvasOverlay = new RaceCourseCanvasOverlay();
        mapw.addOverlay(raceCourseCanvasOverlay);
        
        windFieldCanvasOverlay = new WindFieldCanvasOverlay();
        //mapw.addOverlay(windFieldCanvasOverlay);   
    }
    
    private void initMap() {
        logger.fine("In initMap");
        mapw = new MapWidget();
        mapw.setUI(SimulatorMapOptions.newInstance());
        mapw.setZoomLevel(15);
        mapw.setSize("100%", "650px");        
    }

    private void loadRaceLocations() {
        logger.fine("In loadRaceLocations");
        simulatorSvc.getRaceLocations(new AsyncCallback<PositionDTO[]>() {
            @Override
            public void onFailure(Throwable caught) {
                String message = caught.getMessage();
                for (StackTraceElement ste : caught.getStackTrace()) {
                    message += ste.toString() + "\n\t";
                }
                Window.alert("Failed servlet call to SimulatorService\n" + message);
            }

            @Override
            public void onSuccess(PositionDTO[] result) {
                locations.clear();
                int i = 0;
                for (PositionDTO rl : result) {
                    locations.add(rl);
                    raceSelector.addItem(Integer.toString(++i));
                }
            }
        });
    }

    private void initRaceSelector() {
        logger.fine("In initRaceSelector");
        raceSelector.setVisibleItemCount(1);
        raceSelector.setTitle("Race Location Selection");
        raceSelector.setSize("100%", "20px");

       
        raceSelector.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PositionDTO rl = locations.get(raceSelector.getSelectedIndex());
                selectRaceLocation(rl);
            }
        });
    }

    private void initPanel() {
        logger.fine("In initPanel");
      
        LogoAndTitlePanel logoAndTitlePanel = new LogoAndTitlePanel("Simulator", stringMessages);
        logoAndTitlePanel.addStyleName("LogoAndTitlePanel");
        panel.addNorth(logoAndTitlePanel,68);
        
        FlowPanel controlPanel = new FlowPanel();
        controlPanel.add(raceSelector);
        panel.addWest(controlPanel,200);
        
        VerticalPanel mapDisplayPanel = new VerticalPanel();
        mapDisplayPanel.add(courseInputButton);
        mapDisplayPanel.add(windDisplayButton);
        mapDisplayPanel.add(summaryButton);
        mapDisplayPanel.add(replayButton);
        courseInputButton.setChecked(true);
        panel.addEast(mapDisplayPanel,200);
       
        //panel.addWest(checkBox,200);
        panel.add(mapw);
        
        //TabPanel tabPanel = new TabPanel();
        //FlowPanel flowPanel = new FlowPanel();
        //flowPanel.add(mapw);
       /* tabPanel.add(flowPanel, "Summary");
        flowPanel = new FlowPanel();
        flowPanel.add(mapw);
        tabPanel.add(flowPanel, "Replay");
        flowPanel = new FlowPanel();
        flowPanel.add(mapw);
        tabPanel.add(flowPanel, "Wind Display");
        tabPanel.selectTab(0);
        tabPanel.addStyleName("table-center");
        tabPanel.setSize("100%","100%");*/
        //panel.add(tabPanel);
        //panel.add(flowPanel);
        
       
        
        panel.setSize("100%", "100%");
    }

    private void selectRaceLocation(PositionDTO rl) {
        logger.fine("In selectRaceLocation");
        //mapw.clearOverlays();
        LatLng position = LatLng.newInstance(rl.latDeg, rl.lngDeg);
        mapw.panTo(position);
    }

    private void generateWindField() {
        logger.info("In generateWindField");
        WindFieldGenParamsDTO params = new WindFieldGenParamsDTO();
        /*
        LatLngBounds bounds = mapw.getBounds();
   
        PositionDTO ne = new PositionDTO(bounds.getNorthEast().getLatitude(),
                bounds.getNorthEast().getLongitude());
        PositionDTO sw = new PositionDTO(bounds.getSouthWest().getLatitude(),
                bounds.getSouthWest().getLongitude());
        int width = mapw.getSize().getWidth();
  
        
        Point nePoint = mapw.convertLatLngToDivPixel(LatLng.newInstance(ne.latDeg, ne.lngDeg));
        Point nwPoint = Point.newInstance(nePoint.getX()-width, nePoint.getY());
        LatLng nwLatLng = mapw.convertDivPixelToLatLng(nwPoint);
        PositionDTO nw = new PositionDTO(nwLatLng.getLatitude(),nwLatLng.getLongitude());
        
        Point swPoint = mapw.convertLatLngToDivPixel(LatLng.newInstance(sw.latDeg, sw.lngDeg));
        Point sePoint = Point.newInstance(swPoint.getX()+width, swPoint.getY());
        LatLng seLatLng = mapw.convertDivPixelToLatLng(sePoint);
        PositionDTO se = new PositionDTO(seLatLng.getLatitude(),seLatLng.getLongitude());;
        Window.alert("NE : " + ne + " NW : " + nw + " SW : " + sw + " SE : " + se);
        */
        
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
        /*
         if (wl != null && wl.getMatrix()!= null && wl.getMatrix().size() > 0) {
       
            PositionDTO position = wl.getMatrix().get(0).position;
            mapw.panTo(LatLng.newInstance(position.latDeg, position.lngDeg));
        }*/
     }
    
    

    private void initDisplayOptions() {
        logger.fine("In initDisplayOptions");
        checkBox.setSize("100%", "20px");
        checkBox.setTitle("Grid Selector");
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                generateWindField();
            }
        });
        /*
         * if (checkBox.length > 1) { checkBox[0].setText("Grid"); checkBox[1].setText("Wind Direction"); }
         */
    }
    
    private void initButton() {
        windDisplayButton = new RadioButton("Map Display Options", "Wind Display");
        windDisplayButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                
                if (raceCourseCanvasOverlay.isCourseSet()) {
                   mapw.addOverlay(windFieldCanvasOverlay);   
                   generateWindField();    
                   raceCourseCanvasOverlay.setVisible(true);
                } else {
                    Window.alert("No course set, please initialize the course with Start-End Input");
                }
            }
            });
        
        courseInputButton = new RadioButton("Map Display Options","Start End Input");
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
        
        replayButton = new RadioButton("Map Display Options","Replay");
        summaryButton = new RadioButton("Map Display Options","Summary");
    }
}
