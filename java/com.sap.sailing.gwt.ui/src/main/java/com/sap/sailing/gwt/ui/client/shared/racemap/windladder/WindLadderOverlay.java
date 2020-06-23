package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnAddHandler;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.racemap.MovingCanvasOverlay;
import com.sap.sse.common.Bearing;

/**
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class WindLadderOverlay extends MovingCanvasOverlay { //TODO Or FullCanvasOverlay
    protected static final WindLadderResources RESOURCES = GWT.create(WindLadderResources.class);
    protected static final double PADDING_MULT = 1.2d;
    protected static final double TEXTURE_ALPHA = 0.8d;

    protected static WindLadderMaskGenerator maskGen = new EdgeFeatherMaskGenerator(0.2d);
    protected static ImageTileGenerator tileGen = new ImageTileGenerator(RESOURCES.windLadderTexture());

    protected WindDTO windFix;

    private Position legEnd;
    private Position legStart;
    private Point center;
    private Point patternFixPoint;
    private int width;
    private int height;
    private double rotation;

    public WindLadderOverlay(MapWidget map, int zIndex, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
    }

    public void update(WaypointDTO legStart, WaypointDTO legEnd, WindDTO windFix) {
        this.legStart = averageMarkPositions(legStart.controlPoint.getMarks());
        this.legEnd = averageMarkPositions(legEnd.controlPoint.getMarks());
        if (windFix != null) {
            this.windFix = windFix;
        }
        //updateTransition(-1);
        draw(); //TODO Force additional draw?
    }

    private Position averageMarkPositions(Iterable<MarkDTO> marks) {
        double sumLat = 0d;
        double sumLng = 0d;
        long size = marks.spliterator().getExactSizeIfKnown();
        if (size == -1) {
            size = 0;
            for (@SuppressWarnings("unused") MarkDTO mark : marks) {
                size++;
            }
        }
        if (size == 0) {
            return null;
        }
        for (MarkDTO mark : marks) {
            sumLat += mark.position.getLatRad();
            sumLng += mark.position.getLngRad();
        }
        return new RadianPosition(sumLat / size, sumLng / size);
    }

    private void calculateMaskSize(Position pos1, Position pos2) {
        Point p1 = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(pos1));
        Point p2 = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(pos2));
        height = (int) Math.ceil(Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2))
                * PADDING_MULT);
        width = (int) Math.ceil(height * 0.75d); //TODO Get width of field
    }

    private double calculatePatternScale(int patternSize) {
        Position pos1 = coordinateSystem.getPosition(mapProjection.fromDivPixelToLatLng(Point.newInstance(0, 0)));
        Position pos2 = coordinateSystem
                .getPosition(mapProjection.fromDivPixelToLatLng(Point.newInstance(patternSize, 0)));
        final double patternSizeMeters = pos1.getDistance(pos2).getMeters();
        final double boatLength = 6.4d;
        double scale = boatLength * 10 / patternSizeMeters; //TODO Adaptive
        GWT.log("Scale: " + scale);
        return scale;
    }

    @Override
    protected void draw() {
        if (legStart == null || legEnd == null || windFix == null || mapProjection == null) {
            return;
        }
        boolean redraw = true; //TODO
        calculateMaskSize(legStart, legEnd);
        Bearing bearing = legEnd.getBearingGreatCircle(legStart);
        Position middle = legEnd.translateGreatCircle(bearing, legEnd.getDistance(legStart).scale(0.5d));
        center = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(middle));
        patternFixPoint = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(legEnd));
        rotation = bearing.getRadians();
        if (redraw) {
            drawToCanvas();
        }
    }

    protected void drawToCanvas() {
        // Prepare canvas
        super.setCanvasSettings(); //This clears the canvas //TODO Call on every resize/move
        final int canvasWidth = getMap().getDiv().getClientWidth();
        final int canvasHeight = getMap().getDiv().getClientHeight();
        final double patternScale = calculatePatternScale(tileGen.getHeight());
        Context2d ctx = getCanvas().getContext2d();
        // Move to area of interest
        ctx.save();
        ctx.translate(center.getX() + canvasWidth / 2, center.getY() + canvasHeight / 2);
        ctx.rotate(rotation);
        ctx.translate(-width / 2, -height / 2);
        // Draw transparency mask
        maskGen.drawMask(width, height, ctx);
        ctx.restore();
        // Change composite mode
        ctx.save();
        ctx.setGlobalAlpha(TEXTURE_ALPHA);
        ctx.setGlobalCompositeOperation(Composite.SOURCE_IN);
        // Draw pattern onto mask
        ctx.setFillStyle(ctx.createPattern(tileGen.getTile(), Repetition.REPEAT));
        ctx.rect(0, 0, canvasWidth, canvasHeight); //TODO Find a way to reduce the fill size
        ctx.translate(patternFixPoint.getX() + canvasWidth / 2, patternFixPoint.getY() + canvasHeight / 2);
        ctx.rotate(Math.toRadians(windFix.trueWindFromDeg));
        ctx.scale(patternScale, patternScale);
        ctx.fill();
        ctx.restore();

        // Debug arrow
        ctx.setStrokeStyle("magenta");
        ctx.beginPath();
        ctx.moveTo(200, 200);
        ctx.lineTo(center.getX() + canvasWidth / 2, center.getY() + canvasHeight / 2);
        ctx.stroke();

        ctx.setStrokeStyle("green");
        Point start = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(legStart));
        ctx.beginPath();
        ctx.moveTo(200, 200);
        ctx.lineTo(start.getX() + canvasWidth / 2, start.getY() + canvasHeight / 2);
        ctx.stroke();

        ctx.setStrokeStyle("red");
        Point end = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(legEnd));
        ctx.beginPath();
        ctx.moveTo(200, 200);
        ctx.lineTo(end.getX() + canvasWidth / 2, end.getY() + canvasHeight / 2);
        ctx.stroke();
        // Debug outline
        //ctx.setStrokeStyle("magenta");
        //ctx.strokeRect(0, 0, width, height);
        //ctx.setStrokeStyle("red");
        //ctx.strokeRect(0, 0, width, 0);
    }

    @Override
    protected OverlayViewOnAddHandler getOnAddHandler() {
        return new OverlayViewOnAddHandler() {
            @Override
            public void onAdd(OverlayViewMethods methods) {
                methods.getPanes().getMapPane().appendChild(canvas.getElement());
                WindLadderOverlay.this.onAttach();
            }
        };
    }
}
