package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class LaserVectorGraphics extends BoatClassVectorGraphics {
    
    public LaserVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(423, 137, 423, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {
        // outer part of the hull
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        case SELECTED:
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        }
        
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

        // inner part of the hull
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle (color);
            ctx.setStrokeStyle(color);
            break;
        case SELECTED:
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle (color);
            ctx.setStrokeStyle(color);
            break;
        }

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
        ctx.beginPath();
        ctx.moveTo(300,71.2);
        ctx.lineTo(182,312);
        ctx.bezierCurveTo(182,312,319,188,326,144);
        ctx.bezierCurveTo(332,99,300,71.2,300,71.2);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(300,69.8);
        ctx.lineTo(182,-171);
        ctx.bezierCurveTo(182,-171,319,-47.0,326,-3.15);
        ctx.bezierCurveTo(332,41.6,300,69.8,300,69.8);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(301, 71.4);
        ctx.lineTo(40.3, 134);
        ctx.bezierCurveTo(40.3, 134, 221, 149, 259, 126);
        ctx.bezierCurveTo(297, 102, 301, 71.4, 301, 71.4);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(300, 68.9);
        ctx.lineTo(39.4, 5.94);
        ctx.bezierCurveTo(39.4, 5.94, 220, -9.1, 258, 13.9);
        ctx.bezierCurveTo(296, 37.9, 300, 68.9, 300, 68.9);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawReachingPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(301,72.1);
        ctx.lineTo(92.1,240);
        ctx.bezierCurveTo(92.1,240,269,186,294,149);
        ctx.bezierCurveTo(318,111,301,72.1,301,72.1);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawReachingStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(301, 70);
        ctx.lineTo(92.2, -97.1);
        ctx.bezierCurveTo(92.2, -97.1, 269, -43.6, 294, -6.7);
        ctx.bezierCurveTo(318, 31.1, 301, 70, 301, 70);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
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
