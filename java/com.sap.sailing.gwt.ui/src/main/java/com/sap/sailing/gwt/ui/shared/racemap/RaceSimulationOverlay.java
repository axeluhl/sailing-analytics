package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
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
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;

/**
 * A Google Maps overlay based on an HTML5 canvas for drawing simulation results on the {@link RaceMap}.
 * 
 * @author Christopher Ronnewinkel (D036654)
 * 
 */
public class RaceSimulationOverlay extends FullCanvasOverlay  implements TimeListener {

    public static final String GET_SIMULATION_CATEGORY = "getSimulation";
    private final String textColor = "Black";
    private final String textFont = "10pt OpenSansRegular";
    private int xOffset = 10;
    private int yOffset = 150;
    private double rectWidth = 20;
    private double rectHeight = 20;
    
    private final Timer timer;
    private Date lastSimulationTime;
    private final long deltaSimulationTime = 15000;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final SailingServiceAsync sailingService;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ColorPalette colors;
    private SimulatorResultsDTO simulationResult;
    private PathDTO racePath;
    private Date prevStartTime;
    
    public RaceSimulationOverlay(MapWidget map, int zIndex, final Timer timer, RegattaAndRaceIdentifier raceIdentifier, SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor) {
        super(map, zIndex);
        this.timer = timer;
        this.raceIdentifier = raceIdentifier;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.colors = new ColorPaletteGenerator();
    }
    
    @Override
    public void timeChanged(final Date newTime, final Date oldTime) {
        if (lastSimulationTime == null) {
            lastSimulationTime = new Date(newTime.getTime() - deltaSimulationTime);
        }
        if (Math.abs(newTime.getTime() - lastSimulationTime.getTime()) >= deltaSimulationTime) {
            lastSimulationTime = newTime;
            this.simulate(newTime);
        }
    }
    
    @Override
    public void addToMap() {
        if (timer != null) {
            timer.addTimeListener(this);
        }
    }

    @Override
    public void removeFromMap() {
        if (timer != null) {
            timer.removeTimeListener(this);
        }
        this.setVisible(false);
    }

    @Override
    protected void drawCenterChanged() {
    }

    @Override
    protected void draw() {
    }    
    
    public void onBoundsChanged(boolean zoomChanged) {
        // calibrate canvas
        super.draw();
        // draw simulation paths
        this.drawPaths();
        this.drawLegend();
    }
    
    public void clearCanvas() {
        double w = this.getCanvas().getOffsetWidth();
        double h = this.getCanvas().getOffsetHeight();
        Context2d g = this.getCanvas().getContext2d();
        g.clearRect(0, 0, w, h);
    }
    
    public void drawPaths() {
             
        if (simulationResult == null) {
            return;
        }
        if (simulationResult.getPaths() == null) {
            return;
        }
        
        // calibrate canvas
        super.draw();

        Context2d ctxt = canvas.getContext2d();
        PathDTO[] paths = simulationResult.getPaths();
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
    
    public void drawLegend() {
        if (this.simulationResult == null) {
            return;
        }
        int index = 0;
        Context2d context2d = canvas.getContext2d();
        context2d.setFont(textFont);
        TextMetrics txtmet;
        txtmet = context2d.measureText("00:00:00");
        double timewidth = txtmet.getWidth();
        double txtmaxwidth = 0.0;
        txtmet = context2d.measureText(racePath.getName().split("#")[1]);
        txtmaxwidth = Math.max(txtmaxwidth, txtmet.getWidth());
        PathDTO[] paths = this.simulationResult.getPaths();
        for (PathDTO path : paths) {
            txtmet = context2d.measureText(path.getName());
            txtmaxwidth = Math.max(txtmaxwidth, txtmet.getWidth());
        }
        drawRectangleWithText(xOffset, yOffset, "rgba(255,255,255,0.8);",
                racePath.getName().split("#")[1], getFormattedTime(racePath.getPathTime()), txtmaxwidth, timewidth);
        for (PathDTO path : paths) {
            drawRectangleWithText(xOffset, yOffset + (paths.length-index) * rectHeight, this.colors.getColor(paths.length-1-index),
                path.getName().split("#")[1], getFormattedTime(path.getPathTime()), txtmaxwidth, timewidth);
            index++;
        }
    }
    
    protected void drawRectangle(double x, double y, String color) {
        Context2d context2d = canvas.getContext2d();
        context2d.setFillStyle(color);
        context2d.setLineWidth(3);
        context2d.fillRect(x, y, rectWidth, rectHeight);
    }

    protected void drawRectangleWithText(double x, double y, String color, String text, String time, double textmaxwidth, double timewidth) {
        double offset = 3.0;
        Context2d context2d = canvas.getContext2d();
        context2d.setFont(textFont);
        drawRectangle(x, y, color);
        context2d.setGlobalAlpha(0.80);
        context2d.setFillStyle("white");
        context2d.fillRect(x + rectWidth, y, 15.0 + textmaxwidth + timewidth, rectHeight);
        context2d.setGlobalAlpha(1.0);
        context2d.setFillStyle(textColor);
        context2d.fillText(text, x + rectWidth + 5.0, y + 12.0 + offset);
        context2d.fillText(time, x + rectWidth + textmaxwidth + 10.0, y + 12.0 + offset);
    }
    
    protected String getFormattedTime(long pathTime) {
        TimeZone gmt = TimeZone.createTimeZone(0);
        Date timeDiffDate = new Date(pathTime);
        String pathTimeStr = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND).format(
                timeDiffDate, gmt);
        return pathTimeStr;
    }
    
    public void simulate(Date from) {
        GetSimulationAction getSimulation = new GetSimulationAction(sailingService, raceIdentifier, from, prevStartTime);
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
                        // store results
                        if (result != null) {
                            prevStartTime = result.getStartTime();
                            if (result.getPaths() != null) {
                                simulationResult = result;
                                PathDTO[] paths = result.getPaths();
                                racePath = new PathDTO();
                                List<SimulatorWindDTO> racePathPoints = new ArrayList<SimulatorWindDTO>();
                                racePathPoints.add(new SimulatorWindDTO(null, 0, 0, paths[0].getPoints().get(0).timepoint));
                                racePathPoints.add(new SimulatorWindDTO(null, 0, 0, paths[0].getPoints().get(0).timepoint + result.getLegDuration()));
                                racePath.setPoints(racePathPoints);
                                racePath.setName("0#Race");
                                clearCanvas();
                                drawPaths();
                                drawLegend();
                            }
                        }
                    }
                }));

    }
}
