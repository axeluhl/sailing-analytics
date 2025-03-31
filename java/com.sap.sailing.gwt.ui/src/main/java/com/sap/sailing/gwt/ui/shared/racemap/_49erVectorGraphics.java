package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class _49erVectorGraphics extends BoatClassVectorGraphics {
    
    public _49erVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(499, 290, 499, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {
        // draw hull
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        case SELECTED:
            ctx.setFillStyle (color);
            ctx.setStrokeStyle(color);
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        }

        ctx.setLineJoin(LineJoin.ROUND);
        ctx.beginPath();
        ctx.moveTo(2.04,2.47);
        ctx.lineTo(232,2.47);
        ctx.lineTo(235,36.5);
        ctx.lineTo(239,68.5);
        ctx.bezierCurveTo(239,68.5,296,81.4,325,88.5);
        ctx.bezierCurveTo(363,98,439,119,439,119);
        ctx.lineTo(499,137);
        ctx.lineTo(499,151);
        ctx.bezierCurveTo(499,151,448,169,422,177);
        ctx.bezierCurveTo(391,186,360,195,328,202);
        ctx.bezierCurveTo(299,209,239,219,239,219);
        ctx.lineTo(234,258);
        ctx.lineTo(230,288);
        ctx.lineTo(2.04,288);
        ctx.bezierCurveTo(2.04,288,27.9,282,42.5,266);
        ctx.bezierCurveTo(51.7,255,56.2,210,56.2,210);
        ctx.lineTo(30.3,192);
        ctx.lineTo(30.3,93.5);
        ctx.lineTo(56.2,76.5);
        ctx.bezierCurveTo(56.2,76.5,53.9,37.5,45,24.5);
        ctx.bezierCurveTo(34.9,9.5,2.04,2.47,2.04,2.47);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");

        ctx.beginPath();
        ctx.moveTo(65.1,220);
        ctx.lineTo(224,220);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(66.7,68.5);
        ctx.lineTo(226,68.5);
        ctx.fill();
        ctx.stroke();
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
            break;
        case SELECTED:
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
            break;
        }

        ctx.beginPath();
        ctx.moveTo(59.6,98);
        ctx.lineTo(92.4,70.2);
        ctx.lineTo(239,70.2);
        ctx.lineTo(298,83.9);
        ctx.bezierCurveTo(298,83.9,277,121,277,142);
        ctx.bezierCurveTo(277,165,299,207,299,207);
        ctx.lineTo(238,218);
        ctx.lineTo(91.9,218);
        ctx.lineTo(59.6,190);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        // drawAlien(ctx);  idea for a game: draw an alien into the boat once per race for 1 second -> find the alien
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {

        ctx.beginPath();
        ctx.moveTo(499,148);
        ctx.lineTo(286,250);
        ctx.bezierCurveTo(286,250,444,231,472,202);
        ctx.bezierCurveTo(499,176,499,148,499,148);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(275,150);
        ctx.lineTo(44.8,285);
        ctx.bezierCurveTo(44.8,285,217,248,248,214);
        ctx.bezierCurveTo(277,180,275,150,275,150);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(655,148);
        ctx.lineTo(316,304);
        ctx.bezierCurveTo(316,304,509,336,564,300);
        ctx.bezierCurveTo(627,262,655,148,655,148);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.beginPath();
        ctx.moveTo(315,304);
        ctx.lineTo(179,257);
        ctx.lineTo(179,40.2);
        ctx.fill();
        ctx.stroke();
        ctx.restore();
        ctx.save();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(499,146);
        ctx.lineTo(286,43.5);
        ctx.bezierCurveTo(286,43.5,444,62.4,472,91.6);
        ctx.bezierCurveTo(499,118,499,146,499,146);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(275,144);
        ctx.lineTo(43.8,8.4);
        ctx.bezierCurveTo(43.8,8.4,217,45.5,248,79.5);
        ctx.bezierCurveTo(277,114,275,144,275,144);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(655,146);
        ctx.lineTo(316,-10.5);
        ctx.bezierCurveTo(316,-10.5,509,-42.1,564,-6.49);
        ctx.bezierCurveTo(627,31.5,655,146,655,146);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.beginPath();
        ctx.moveTo(316,-10.5);
        ctx.lineTo(180,36.5);
        ctx.lineTo(180,253);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(496,148);
        ctx.lineTo(284,183);
        ctx.bezierCurveTo(284,183,427,207,457,189);
        ctx.bezierCurveTo(489,172,496,148,496,148);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(275,149);
        ctx.lineTo(23.2,191);
        ctx.bezierCurveTo(23.2,191,192,218,230,198);
        ctx.bezierCurveTo(269,176,275,149,275,149);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(496,147);
        ctx.lineTo(284,112);
        ctx.bezierCurveTo(284,112,427,88.4,457,106);
        ctx.bezierCurveTo(489,123,496,147,496,147);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(275,146);
        ctx.lineTo(23.3,104);
        ctx.bezierCurveTo(23.3,104,192,77.4,230,97.4);
        ctx.bezierCurveTo(269,119,275,146,275,146);
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
        drawUpwindStarboardTackSails(ctx);
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

    protected void drawAlien(Context2d ctx) {
        ctx.save();
        ctx.setFillStyle("#88bd64");
        ctx.transform(0.015,1,-1,0.015,722,-135);
        ctx.beginPath();
        ctx.moveTo(268,564);
        ctx.bezierCurveTo(283.1878306203468,564,295.5,583.699471007445,295.5,608);
        ctx.bezierCurveTo(295.5,632.300528992555,283.1878306203468,652,268,652);
        ctx.bezierCurveTo(252.81216937965317,652,240.5,632.300528992555,240.5,608);
        ctx.bezierCurveTo(240.5,583.699471007445,252.81216937965317,564,268,564);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle("#fafafa");
        ctx.transform(0.015,1,-1,0.015,722,-135);
        ctx.beginPath();
        ctx.moveTo(255,578.5);
        ctx.bezierCurveTo(260.10863393593485,578.5,264.25,587.2304473782996,264.25,598);
        ctx.bezierCurveTo(264.25,608.7695526217004,260.10863393593485,617.5,255,617.5);
        ctx.bezierCurveTo(249.89136606406515,617.5,245.75,608.7695526217004,245.75,598);
        ctx.bezierCurveTo(245.75,587.2304473782996,249.89136606406515,578.5,255,578.5);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle("#fafafa");
        ctx.transform(0.015,1,-1,0.015,722,-135);
        ctx.beginPath();
        ctx.moveTo(282,580.5);
        ctx.bezierCurveTo(287.10863393593485,580.5,291.25,589.2304473782996,291.25,600);
        ctx.bezierCurveTo(291.25,610.7695526217004,287.10863393593485,619.5,282,619.5);
        ctx.bezierCurveTo(276.89136606406515,619.5,272.75,610.7695526217004,272.75,600);
        ctx.bezierCurveTo(272.75,589.2304473782996,276.89136606406515,580.5,282,580.5);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle("#010202");
        ctx.transform(0.011,0.741,-0.783,0.011,594,-59.8);
        ctx.beginPath();
        ctx.moveTo(281,580);
        ctx.bezierCurveTo(285.69442037356174,580,289.5,583.5817220013537,289.5,588);
        ctx.bezierCurveTo(289.5,592.4182779986463,285.69442037356174,596,281,596);
        ctx.bezierCurveTo(276.30557962643826,596,272.5,592.4182779986463,272.5,588);
        ctx.bezierCurveTo(272.5,583.5817220013537,276.30557962643826,580,281,580);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.setFillStyle("#010202");
        ctx.beginPath();
        ctx.moveTo(94.1,129);
        ctx.bezierCurveTo(94.1,129,88.1,155,97.1,159);
        ctx.bezierCurveTo(106,163,79.1,148,94.1,129);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("#88bd64");
        ctx.beginPath();
        ctx.moveTo(59.1,98);
        ctx.bezierCurveTo(59.1,98,54.1,188,65.1,185);
        ctx.bezierCurveTo(76.1,182,80.1,160,82.1,147);
        ctx.bezierCurveTo(84.1,134,68.1,99,59.099999999999994,98);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("#010202");
        ctx.beginPath();
        ctx.moveTo(146,144);
        ctx.bezierCurveTo(146,144,189,168,144,187);
        ctx.bezierCurveTo(99,205,184,180,146,144);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(148,140);
        ctx.bezierCurveTo(148,140,190,114,144,97);
        ctx.bezierCurveTo(99,79,185,102,148,140);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.save();
        ctx.transform(0.011,0.741,-0.783,0.011,595,-87.8);
        ctx.beginPath();
        ctx.moveTo(281,580);
        ctx.bezierCurveTo(285.69442037356174,580,289.5,583.5817220013537,289.5,588);
        ctx.bezierCurveTo(289.5,592.4182779986463,285.69442037356174,596,281,596);
        ctx.bezierCurveTo(276.30557962643826,596,272.5,592.4182779986463,272.5,588);
        ctx.bezierCurveTo(272.5,583.5817220013537,276.30557962643826,580,281,580);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }
}
