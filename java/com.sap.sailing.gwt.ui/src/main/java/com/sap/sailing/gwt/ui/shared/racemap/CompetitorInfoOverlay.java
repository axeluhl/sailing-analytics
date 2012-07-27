package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing competitor information close to the boat
 */
public class CompetitorInfoOverlay extends CanvasOverlay {

    /**
     * The competitor.
     */
    private final CompetitorDTO competitorDTO;

    /**
     * The current GPS fix of the boat position of the competitor.
     */
    private GPSFixDTO boatFix;

    private final RaceMapImageManager raceMapImageManager;

    private int canvasWidth;
    private int canvasHeight;
    private double cornerRadius;
    
    public CompetitorInfoOverlay(CompetitorDTO competitorDTO, RaceMapImageManager raceMapImageManager) {
        super();
        this.competitorDTO = competitorDTO;
        this.raceMapImageManager = raceMapImageManager;
        
        canvasWidth = 20;
        canvasHeight = 25;
        cornerRadius = 10;

        if(getCanvas() != null) {
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setHeight(String.valueOf(canvasHeight));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            getCanvas().setCoordinateSpaceHeight(canvasHeight);
        }
    }

    @Override
    protected Overlay copy() {
        return new BoatCanvasOverlay(competitorDTO, raceMapImageManager);
    }

    @Override
    protected void redraw(boolean force) {
        if (boatFix != null) {
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            String infoText = competitorDTO.sailID;
            if(infoText == null || infoText.isEmpty()) {
            	infoText = competitorDTO.name;
            }

            Context2d context2d = getCanvas().getContext2d();

            context2d.setFont("12px bold Verdana sans-serif");
            TextMetrics measureText = context2d.measureText(competitorDTO.sailID);
            double textWidth = measureText.getWidth();

            canvasWidth = (int) textWidth + 17;
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            
            // Change origin and dimensions to match true size (a stroke makes the shape a bit larger)
            context2d.setFillStyle("gray");
//            context2d.strokeRect(cornerRadius/2, cornerRadius/2, canvasWidth-cornerRadius, canvasHeight-cornerRadius);
            context2d.setFillStyle(CssColor.make("rgba(255,255,255,0.75)"));
            context2d.fillRect(cornerRadius/2, cornerRadius/2, canvasWidth-cornerRadius, canvasHeight-cornerRadius);
            
            context2d.beginPath();
            context2d.setFillStyle("black");
            
            context2d.fillText(competitorDTO.sailID, 8, 17);
            context2d.stroke(); 

            Point boatPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(), boatPositionInPx.getX(), boatPositionInPx.getY());
        }
    }
    
    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }    
}
