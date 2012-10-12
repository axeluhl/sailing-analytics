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
    private int infoBoxHeight;
    private int infoBoxWidth;
    private double cornerRadius;
    
    public CompetitorInfoOverlay(CompetitorDTO competitorDTO, RaceMapImageManager raceMapImageManager) {
        super();
        this.competitorDTO = competitorDTO;
        this.raceMapImageManager = raceMapImageManager;
        
        canvasWidth = 20;
        canvasHeight = 45;
        infoBoxWidth = 20;
        infoBoxHeight = 20;
        cornerRadius = 4;

        if(getCanvas() != null) {
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setHeight(String.valueOf(canvasHeight));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            getCanvas().setCoordinateSpaceHeight(canvasHeight);
        }
    }

    @Override
    protected Overlay copy() {
        return new CompetitorInfoOverlay(competitorDTO, raceMapImageManager);
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
            CssColor grayTransparentColor = CssColor.make("rgba(255,255,255,0.75)");

            context2d.setFont("12px bold Verdana sans-serif");
            TextMetrics measureText = context2d.measureText(competitorDTO.sailID);
            double textWidth = measureText.getWidth();

            canvasWidth = (int) textWidth + 17;
            infoBoxWidth = canvasWidth;
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            
            // Change origin and dimensions to match true size (a stroke makes the shape a bit larger)
            context2d.setFillStyle(grayTransparentColor);
            drawRoundedRect(context2d, cornerRadius/2, cornerRadius/2, infoBoxWidth-cornerRadius, infoBoxHeight-cornerRadius, cornerRadius);
            
            // this translation is important for drawing lines with a real line width of 1 pixel
            context2d.translate(-0.5, -0.5);
            context2d.setStrokeStyle("gray");
            context2d.setLineWidth(1.0);
            context2d.beginPath();
            context2d.moveTo(cornerRadius/2, infoBoxHeight/2);
            context2d.lineTo(cornerRadius/2, canvasHeight);
            context2d.stroke(); 
            context2d.translate(0.0, 0.0);

            context2d.beginPath();
            context2d.setFillStyle("black");
            context2d.fillText(competitorDTO.sailID, 8, 14);
            context2d.stroke(); 

            Point boatPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(), boatPositionInPx.getX(), boatPositionInPx.getY() - canvasHeight);
        }
    }
    
	public static void drawRoundedRect(Context2d context, double x, double y, double w, double h, double r) {
		context.beginPath();
		context.moveTo(x + r, y);
		context.lineTo(x + w - r, y);
		context.quadraticCurveTo(x + w, y, x + w, y + r);
		context.lineTo(x + w, y + h - r);
		context.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
		context.lineTo(x + r, y + h);
		context.quadraticCurveTo(x, y + h, x, y + h - r);
		context.lineTo(x, y + r);
		context.quadraticCurveTo(x, y, x + r, y);
		context.stroke();
		context.fill();
	}
    
    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }    
}
