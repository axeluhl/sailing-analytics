package com.sap.sailing.gwt.ui.client.shared.racemap.windladder;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewMethods;
import com.google.gwt.maps.client.overlays.overlayhandlers.OverlayViewOnAddHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.common.impl.RadianPosition;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.client.shared.racemap.ManeuverAngleCache;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.racemap.MovingCanvasOverlay;
import com.sap.sse.common.Bearing;

/**
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class WindLadderOverlay extends FullCanvasOverlay {
    protected static final WindLadderResources RESOURCES = GWT.create(WindLadderResources.class);
    protected static final double TEXTURE_ALPHA = 0.6d;
    protected static final double CANVAS_RESERVE = 0.1;

    protected double canvasRotationDegrees;

    protected ImageTileGenerator tileGen = new ImageTileGenerator(RESOURCES.windLadderTexture());

    protected Double windBearing;
    protected Position fixPosition;

    protected Double drawnWindBearing;
    protected Double drawnPatternSize;

    protected Double previousOffset;

    public WindLadderOverlay(MapWidget map, int zIndex, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
    }

    public void update(WindDTO windFix, Position fixPosition, long timeForPositionTransitionMillis) {
        if (windFix != null) {
            windBearing = Math.toRadians(coordinateSystem.mapDegreeBearing(windFix.trueWindFromDeg)); //TODO every draw?
        }
        if (fixPosition != null) {
            this.fixPosition = fixPosition;
        }
        updateTransition(timeForPositionTransitionMillis);
        draw();
    }

    protected boolean isRedrawNeeded() {
        return drawnWindBearing == null || drawnPatternSize == null || Math.abs(drawnWindBearing - windBearing) > Math.toRadians(5);
    }

    protected void forceRedraw() {
        drawnWindBearing = null;
    }

    private double calculatePatternScale(int patternSize) {
        Position pos1 = coordinateSystem.getPosition(mapProjection.fromDivPixelToLatLng(Point.newInstance(0, 0)));
        Position pos2 = coordinateSystem
                .getPosition(mapProjection.fromDivPixelToLatLng(Point.newInstance(patternSize, 0)));
        final double patternSizeMeters = pos1.getDistance(pos2).getMeters();
        final double boatLength = 6.4d;
        double scale = boatLength * 10 / patternSizeMeters; //TODO Adaptive
        GWT.log("Scale: " + patternSize + " -> " + scale);
        return scale;
    }

    /**
     * Resets the canvas into a neutral position and rotation
     */
    @Override
    public void setCanvasSettings() {
        if (mapWidth == null) {
            mapWidth = getMap().getDiv().getClientWidth();
        }
        if (mapHeight == null) {
            mapHeight = getMap().getDiv().getClientHeight();
        }
        int canvasWidthReserve = (int) (mapWidth * CANVAS_RESERVE);
        int canvasHeightReserve = (int) (mapHeight * CANVAS_RESERVE);
        int canvasWidth = mapWidth + canvasWidthReserve;
        int canvasHeight = mapHeight + canvasHeightReserve;

        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);

        Point sw = mapProjection.fromLatLngToDivPixel(getMap().getBounds().getSouthWest());
        Point ne = mapProjection.fromLatLngToDivPixel(getMap().getBounds().getNorthEast());
        setWidgetPosLeft(Math.min(sw.getX(), ne.getX()) - canvasWidthReserve / 2);
        setWidgetPosTop(Math.min(sw.getY(), ne.getY()) - canvasHeightReserve / 2);

        setCanvasPosition(getWidgetPosLeft(), getWidgetPosTop());
        setCanvasRotation(0.0);
    }

    @Override
    protected void draw() {
        if (mapProjection != null && windBearing != null && fixPosition != null && tileGen.getReady()) {

            if (isRedrawNeeded()) {
                GWT.log("redraw");
                updateTransition(-1);
                setCanvasSettings();
                drawToCanvas();
            } else {
                // Rotation
                setCanvasRotation(Math.toDegrees(windBearing - drawnWindBearing));
            }
            // Offset from centered position
            Point fixPointInMap = mapProjection.fromLatLngToDivPixel(coordinateSystem.toLatLng(fixPosition));
            GWT.log(fixPointInMap.toString());
            Point windUnitVector = Point.newInstance(-Math.sin(-windBearing), -Math.cos(-windBearing));
            // Dot product of the two vectors above
            final double fixPointWindwardDistance = fixPointInMap.getX() * windUnitVector.getX() + fixPointInMap.getY() * windUnitVector.getY();;
            double offset = fixPointWindwardDistance % drawnPatternSize;
            offset = offset < 0.0 ? offset + drawnPatternSize : offset; // Full modulus instead of remainder
            offset = offset > drawnPatternSize / 2.0 ? offset - drawnPatternSize : offset; // Center around 0
            Point offsetVector = Point.newInstance(offset * windUnitVector.getX(), offset * windUnitVector.getY());
            // Detect pattern jump
            if (previousOffset != null && Math.abs(offset - previousOffset) > drawnPatternSize / 2.0) {
                updateTransition(-1);
            }
            previousOffset = offset;
            setCanvasPosition(getWidgetPosLeft() + offsetVector.getX(), getWidgetPosTop() + offsetVector.getY());
        }
    }

    protected void drawToCanvas() {
        // Prepare canvas
        final int canvasWidth = canvas.getCoordinateSpaceWidth();
        final int canvasHeight = canvas.getCoordinateSpaceHeight();
        final double patternScale = calculatePatternScale(tileGen.getHeight());
        Context2d ctx = canvas.getContext2d();
        ctx.clearRect(0, 0, canvasWidth, canvasHeight);
        // Change composite mode
        ctx.save();
        ctx.setGlobalAlpha(TEXTURE_ALPHA);
        //ctx.setGlobalCompositeOperation(Composite.SOURCE_IN);
        // Prepare pattern texture
        ctx.setFillStyle(ctx.createPattern(tileGen.getTile(), Repetition.REPEAT));
        ctx.rect(0, 0, canvasWidth, canvasHeight);
        ctx.translate(canvasWidth / 2, canvasHeight / 2);
        ctx.rotate(windBearing);
        ctx.scale(patternScale, patternScale);
        // Draw pattern onto mask
        ctx.fill();
        // DEBUG
        ctx.restore();
        ctx.beginPath();
        ctx.translate(canvasWidth / 2, canvasHeight / 2);
        ctx.rect(-2, -2, 4, 4);
        ctx.setFillStyle("red");
        ctx.fill();
        drawnWindBearing = windBearing;
        drawnPatternSize = tileGen.getHeight() * patternScale;
    }

    @Override
    protected void setCanvasRotation(double rotationInDegrees) {
        super.setCanvasRotation(rotationInDegrees);
        canvasRotationDegrees = rotationInDegrees;
    }

    protected double getCanvasRotation() {
        return canvasRotationDegrees;
    }

    @Override
    public void onResize() {
        forceRedraw();
        super.onResize();
    }

    @Override
    protected void drawCenterChanged() {
        updateTransition(-1);
        GWT.log("drawCenterChanged");
        draw();
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
