package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;

/**
 * A google map overlay based on a HTML5 canvas for drawing a short textual information close to some object on a map
 * with a given position.
 * 
 * @author Frank Mittag
 * @author Axel Uhl
 */
public class SmallTransparentInfoOverlay extends CanvasOverlayV3 {

    /**
     * The text to display in the canvas
     */
    private String infoText;

    /**
     * The current GPS fix of the boat position of the competitor.
     */
    private Position position;

    private int canvasWidth;
    private int canvasHeight;
    private int infoBoxHeight;
    private int infoBoxWidth;
    private double cornerRadius;

    public SmallTransparentInfoOverlay(MapWidget map, int zIndex, String infoText) {
        super(map, zIndex);
        this.infoText = infoText;
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

//        setCanvasSize(canvasWidth, canvasHeight);
    }

    @Override
    protected void draw() {
        if (mapProjection != null && position != null) {
            LatLng latLngPosition = LatLng.newInstance(position.getLatDeg(), position.getLngDeg());
            Context2d context2d = getCanvas().getContext2d();
            CssColor grayTransparentColor = CssColor.make("rgba(255,255,255,0.75)");

            context2d.setFont("12px bold Verdana sans-serif");
            TextMetrics measureText = context2d.measureText(infoText);
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
            context2d.fillText(infoText, 8, 14);
            context2d.stroke();

            Point objectPositionInPx = mapProjection.fromLatLngToDivPixel(latLngPosition);
            setCanvasPosition(objectPositionInPx.getX(), objectPositionInPx.getY() - canvasHeight);
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

    /**
     * @param position the new position of the overlay
     * @param timeForPositionTransitionMillis use -1 to not animate the position transition, e.g., during map zoom or non-play
     */

    public void setPosition(Position position, long timeForPositionTransitionMillis) {
        if (timeForPositionTransitionMillis == -1) {
            removeCanvasPositionAndRotationTransition();
        } else {
            setCanvasPositionAndRotationTransition(timeForPositionTransitionMillis);
        }
        this.position = position;
    }
    
    /**
     * Updates the text to show and re-draws the canvas
     */
    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }
}