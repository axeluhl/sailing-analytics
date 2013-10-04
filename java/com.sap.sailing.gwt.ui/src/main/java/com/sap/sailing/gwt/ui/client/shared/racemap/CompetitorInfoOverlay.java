package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.MapCanvasProjection;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;

/**
 * A google map overlay based on a HTML5 canvas for drawing competitor information close to the boat
 */
public class CompetitorInfoOverlay extends CanvasOverlayV3 {

    /**
     * The competitor.
     */
    private final CompetitorDTO competitorDTO;

    /**
     * The current GPS fix of the boat position of the competitor.
     */
    private GPSFixDTO boatFix;

    private int canvasWidth;
    private int canvasHeight;
    private int infoBoxHeight;
    private int infoBoxWidth;
    private double cornerRadius;

    public CompetitorInfoOverlay(MapWidget map, int zIndex, CompetitorDTO competitorDTO, RaceMapImageManager raceMapImageManager) {
        super(map, zIndex);
        this.competitorDTO = competitorDTO;

        canvasWidth = 20;
        canvasHeight = 45;
        infoBoxWidth = 20;
        infoBoxHeight = 20;
        cornerRadius = 4;

        if (getCanvas() != null) {
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setHeight(String.valueOf(canvasHeight));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            getCanvas().setCoordinateSpaceHeight(canvasHeight);
        }
    }

    @Override
    protected void draw(OverlayViewMethods methods) {
        MapCanvasProjection projection = methods.getProjection();

        if (boatFix != null) {
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            String infoText = competitorDTO.getSailID();
            if (infoText == null || infoText.isEmpty()) {
                infoText = competitorDTO.getName();
            }

            Context2d context2d = getCanvas().getContext2d();
            CssColor grayTransparentColor = CssColor.make("rgba(255,255,255,0.75)");

            context2d.setFont("12px bold Verdana sans-serif");
            TextMetrics measureText = context2d.measureText(competitorDTO.getSailID());
            double textWidth = measureText.getWidth();

            canvasWidth = (int) textWidth + 17;
            infoBoxWidth = canvasWidth;
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);

            // Change origin and dimensions to match true size (a stroke makes the shape a bit larger)
            context2d.setFillStyle(grayTransparentColor);
            drawRoundedRect(context2d, cornerRadius / 2, cornerRadius / 2, infoBoxWidth - cornerRadius, infoBoxHeight
                    - cornerRadius, cornerRadius);

            // this translation is important for drawing lines with a real line width of 1 pixel
            context2d.translate(-0.5, -0.5);
            context2d.setStrokeStyle("gray");
            context2d.setLineWidth(1.0);
            context2d.beginPath();
            context2d.moveTo(cornerRadius / 2, infoBoxHeight / 2);
            context2d.lineTo(cornerRadius / 2, canvasHeight);
            context2d.stroke();
            context2d.translate(0.0, 0.0);

            context2d.beginPath();
            context2d.setFillStyle("black");
            context2d.fillText(competitorDTO.getSailID(), 8, 14);
            context2d.stroke();

            Point boatPositionInPx = projection.fromLatLngToDivPixel(latLngPosition);
            setCanvasPosition(boatPositionInPx.getX(), boatPositionInPx.getY() - canvasHeight);
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