package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class SmallMultihullVectorGraphics extends BoatClassVectorGraphics {
    
    public SmallMultihullVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(710, 260, 525, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {
        // outer net
        
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
        
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(88.6,37.7);
        ctx.lineTo(275.6,37.7);
        ctx.quadraticCurveTo(275.6,37.7,275.6,37.7);
        ctx.lineTo(275.6,224.7);
        ctx.quadraticCurveTo(275.6,224.7,275.6,224.7);
        ctx.lineTo(88.6,224.7);
        ctx.quadraticCurveTo(88.6,224.7,88.6,224.7);
        ctx.lineTo(88.6,37.7);
        ctx.quadraticCurveTo(88.6,37.7,88.6,37.7);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // inner net
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle (color);
            ctx.setStrokeStyle(color);
            break;
        case SELECTED:
            ctx.setFillStyle("#FFFFFF");
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
        ctx.moveTo(114,71.7);
        ctx.lineTo(250,71.7);
        ctx.quadraticCurveTo(250,71.7,250,71.7);
        ctx.lineTo(250,199.7);
        ctx.quadraticCurveTo(250,199.7,250,199.7);
        ctx.lineTo(114,199.7);
        ctx.quadraticCurveTo(114,199.7,114,199.7);
        ctx.lineTo(114,71.7);
        ctx.quadraticCurveTo(114,71.7,114,71.7);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        // hull - left and right part
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle("#FFFFFF");
            ctx.setStrokeStyle("#000000");
            break;
        case SELECTED:
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle("#FFFFFF");
            ctx.setStrokeStyle("#000000");
            break;
        }
        
        ctx.setLineWidth(5.0);
        ctx.setLineJoin(LineJoin.ROUND);
        ctx.beginPath();
        ctx.moveTo(1.98,1.67);
        ctx.lineTo(362,1.67);
        ctx.lineTo(529,21.7);
        ctx.lineTo(362,41.7);
        ctx.lineTo(1.98,41.7);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.setLineWidth(5.0);
        ctx.setLineJoin(LineJoin.ROUND);
        ctx.beginPath();
        ctx.moveTo(1.98,222);
        ctx.lineTo(361,222);
        ctx.lineTo(529,242);
        ctx.lineTo(361,262);
        ctx.lineTo(1.98,262);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // Ausleger
        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.setLineWidth(3.0);

        ctx.beginPath();
        ctx.moveTo(707,132);
        ctx.lineTo(266,132);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(475,242);
        ctx.lineTo(475,27.7);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(529,21.7);
        ctx.lineTo(709,132);
        ctx.lineTo(529,242);
        ctx.lineTo(529,242);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(336,132);
        ctx.lineTo(90.6,182);
        ctx.bezierCurveTo(90.6,182,246,212,291,192);
        ctx.bezierCurveTo(339,162,336,132,336,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(473,132);
        ctx.lineTo(356,202);
        ctx.bezierCurveTo(356,202,448,182,464,162);
        ctx.bezierCurveTo(482,142,473,132,473,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(706,132);
        ctx.lineTo(321,355);
        ctx.bezierCurveTo(321,355,517,355,581,302);
        ctx.bezierCurveTo(656,242,706,132,706,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(706,132);
        ctx.lineTo(320,-88.3);
        ctx.bezierCurveTo(320,-88.3,521,-88.3,585,-38.3);
        ctx.bezierCurveTo(656,21.7,706,132,706,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(473,132);
        ctx.lineTo(355,61.7);
        ctx.bezierCurveTo(355,61.7,448,81.7,464,102);
        ctx.bezierCurveTo(482,122,473,132,473,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(336,132);
        ctx.lineTo(90.6,91.7);
        ctx.bezierCurveTo(90.6,91.7,246,61.7,291,81.7);
        ctx.bezierCurveTo(339,112,336,132,336,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(473,132);
        ctx.lineTo(337,152);
        ctx.bezierCurveTo(337,152,431,162,455,152);
        ctx.bezierCurveTo(482,152,473,132,473,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(336,132);
        ctx.lineTo(87.6,152);
        ctx.bezierCurveTo(87.6,152,239,192,288,172);
        ctx.bezierCurveTo(336,152,336,132,336,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {   
        ctx.beginPath();
        ctx.moveTo(473,132);
        ctx.lineTo(337,112);
        ctx.bezierCurveTo(337,112,431,91.7,455,112);
        ctx.bezierCurveTo(482,112,473,132,473,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(337,132);
        ctx.lineTo(87.6,112);
        ctx.bezierCurveTo(87.6,112,239,71.7,288,91.7);
        ctx.bezierCurveTo(337,112,337,132,337,132);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawReachingPortTackSails(Context2d ctx) {
        drawUpwindPortTackSails(ctx);
    }

    @Override
    protected void drawReachingStarboardTackSails(Context2d ctx) {
        drawUpwindStarboardTackSails(ctx);;
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

