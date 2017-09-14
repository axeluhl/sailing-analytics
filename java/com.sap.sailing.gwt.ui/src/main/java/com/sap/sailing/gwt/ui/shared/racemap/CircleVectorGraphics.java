package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;
import com.sap.sse.common.Color;

public class CircleVectorGraphics extends BoatClassVectorGraphics {
    private static final double doublePi = 2 * Math.PI;

    public CircleVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(100, 100, 100, compatibleBoatClasses);
    }

    @Override
    public void drawBoatToCanvas(Context2d ctx, LegType legType, Tack tack, DisplayMode displayMode, double width,
            double height, Size scaleFactor, Color color) {
        ctx.save();
        ctx.clearRect(0, 0, width, height);
        ctx.translate(width / 2.0, height / 2.0);
        ctx.scale(scaleFactor.getHeight(), scaleFactor.getHeight()); // the scale factor MUST be the same for x and y,
                                                                     // otherwise we don't get a circle
        ctx.translate(-getHullLengthInPx() / 2.0, -getBeamInPx() / 2.0);
        drawBoat(ctx, displayMode, color.getAsHtml());
        ctx.restore();
    }

    public double getMinHullLengthInPx() {
        return 10;
    }

    public double getMinBeamLengthInPx() {
        return 10;
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {
        ctx.save();
        // draw the circle
        switch (displayMode) {
        case DEFAULT:
            ctx.setFillStyle(color);
            ctx.setStrokeStyle("#FFFFFF");
            break;
        case SELECTED:
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle(color);
            ctx.setStrokeStyle("#FFFFFF");
            break;
        }
        ctx.setLineWidth(5);
        ctx.beginPath();
        ctx.arc(50, 50, 50, 0, doublePi);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {
    }

    @Override
    protected void drawReachingPortTackSails(Context2d ctx) {
    }

    @Override
    protected void drawReachingStarboardTackSails(Context2d ctx) {
    }

    @Override
    protected void drawUnknownLegTypeStarboardTackSails(Context2d ctx) {
    }

    @Override
    protected void drawUnknownLegTypePortTackSails(Context2d ctx) {
    }

    @Override
    protected void drawKillingSails(Context2d ctx) {
    }
}
