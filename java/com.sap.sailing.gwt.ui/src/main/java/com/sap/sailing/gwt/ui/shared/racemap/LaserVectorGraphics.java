package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;

public class LaserVectorGraphics extends BoatClassVectorGraphics {
    
    public LaserVectorGraphics(String... compatibleBoatClassNames) {
        super("LASER", 4.23, 1.37, compatibleBoatClassNames);
    }

    @Override
    protected void drawBoat(Context2d ctx, boolean isSelected, String color) {
        if(isSelected) {
            ctx.setFillStyle("#FF0000");
        } else {
            ctx.setFillStyle("#FFFFFF");
        }
        ctx.setStrokeStyle(color);
        ctx.setLineWidth(2.0);
        ctx.beginPath();
        ctx.moveTo(1.72,27);
        ctx.lineTo(1.44,105);
        ctx.bezierCurveTo(1.44,105,82.6,133,171,133);
        ctx.bezierCurveTo(258,133,422,81.5,422,70.1);
        ctx.bezierCurveTo(422,55.7,261,1.11,171,1.11);
        ctx.bezierCurveTo(85.8,1.11,1.72,27,1.72,27);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle(color);
        ctx.setStrokeStyle(color);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(48.2,44.2);
        ctx.lineTo(47.9,90.1);
        ctx.bezierCurveTo(47.9,90.1,107,107,171,107);
        ctx.bezierCurveTo(235,107,354,78.7,354,70.1);
        ctx.bezierCurveTo(352,61.4,235,29.9,171,29.9);
        ctx.bezierCurveTo(109,29.9,48.2,44.2,48.2,44.2);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_STROKECOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(304,64.3);
        ctx.lineTo(81.5,274);
        ctx.bezierCurveTo(81.5,274,273,188,296,143);
        ctx.bezierCurveTo(319,95.9,304,64.3,304,64.3);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
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
