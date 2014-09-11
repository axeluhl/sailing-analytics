package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.actions.GetSimulationAction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMap;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.ColorPalette;
import com.sap.sailing.gwt.ui.simulator.util.ColorPaletteGenerator;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

/**
 * A Google Maps overlay based on an HTML5 canvas for drawing simulation results on the {@link RaceMap}.
 * 
 * @author Christopher Ronnewinkel (D036654)
 * 
 */
public class RaceSimulationOverlay extends FullCanvasOverlay {

    public static final String GET_SIMULATION_CATEGORY = "getSimulation";
    
    private boolean visible = false;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final SailingServiceAsync sailingService;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ColorPalette colors;
    
    public RaceSimulationOverlay(MapWidget map, int zIndex, RegattaAndRaceIdentifier raceIdentifier, SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor) {
        super(map, zIndex);
        this.raceIdentifier = raceIdentifier;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.colors = new ColorPaletteGenerator();
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (getCanvas() != null) {
            if (isVisible) {
                this.visible = isVisible;
                this.simulate();
            } else {
                this.visible = isVisible;
            }
        }
    }

    @Override
    public void removeFromMap() {
        this.setVisible(false);
    }

    @Override
    protected void drawCenterChanged() {
    }

    @Override
    protected void draw() {
        // calibrate canvas
        super.draw();
        // do nothing
    }    
    
    public void onBoundsChanged(boolean zoomChanged) {
        // calibrate projection
    }
    
    public void drawPaths(SimulatorResultsDTO results) {
        double w = this.getCanvas().getOffsetWidth();
        double h = this.getCanvas().getOffsetHeight();
        Context2d g = this.getCanvas().getContext2d();
        g.clearRect(0, 0, w, h);

        // calibrate canvas
        super.draw();

        Context2d ctxt = canvas.getContext2d();
        PathDTO[] paths = results.getPaths();
        boolean first = true;
        int colorIdx = paths.length - 1;
        for(PathDTO path : paths) {
            List<SimulatorWindDTO> points = path.getPoints();
            ctxt.setLineWidth(3.0);
            ctxt.setGlobalAlpha(0.7);
            ctxt.setStrokeStyle(this.colors.getColor(colorIdx));
            ctxt.beginPath();
            for(SimulatorWindDTO point : points) {
                Point px = mapProjection.fromLatLngToContainerPixel(LatLng.newInstance(point.position.latDeg, point.position.lngDeg));
                if (first) {
                    ctxt.moveTo(px.getX(), px.getY());
                    first = false;
                } else {
                    ctxt.lineTo(px.getX(), px.getY());
                }
            }
            ctxt.stroke();
            ctxt.setGlobalAlpha(1.0);
            colorIdx--;
        }
        
    }
    
    public void simulate() {
        GetSimulationAction getSimulation = new GetSimulationAction(sailingService, raceIdentifier, null /* start-of-tracking */);
        asyncActionsExecutor.execute(getSimulation, GET_SIMULATION_CATEGORY,
                new MarkedAsyncCallback<>(new AsyncCallback<SimulatorResultsDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // TODO: add corresponding message to string-messages
                        // Window.setStatus(stringMessages.errorFetchingWindStreamletData(caught.getMessage()));
                        Window.setStatus(GET_SIMULATION_CATEGORY);
                    }

                    @Override
                    public void onSuccess(SimulatorResultsDTO result) {
                        // draw results
                        if (mapProjection != null) {
                            //Context2d ctxt = canvas.getContext2d();
                            //ctxt.setFillStyle("red");
                            //ctxt.fillRect(10, 10, 50, 50);
                            drawPaths(result);
                        }
                    }
                }));

    }
}
