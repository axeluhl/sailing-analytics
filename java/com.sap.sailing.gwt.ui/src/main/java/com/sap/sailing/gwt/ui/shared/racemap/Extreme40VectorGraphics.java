package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class Extreme40VectorGraphics extends BoatClassVectorGraphics {
    
    public Extreme40VectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(1600, 760, 1220, compatibleBoatClasses);
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
            ctx.setFillStyle (color);
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
        ctx.moveTo(195,72);
        ctx.lineTo(744,72);
        ctx.quadraticCurveTo(744,72,744,72);
        ctx.lineTo(744,622);
        ctx.quadraticCurveTo(744,622,744,622);
        ctx.lineTo(195,622);
        ctx.quadraticCurveTo(195,622,195,622);
        ctx.lineTo(195,72);
        ctx.quadraticCurveTo(195,72,195,72);
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
        ctx.moveTo(271,163);
        ctx.lineTo(671,163);
        ctx.quadraticCurveTo(671,163,671,163);
        ctx.lineTo(671,543);
        ctx.quadraticCurveTo(671,543,671,543);
        ctx.lineTo(271,543);
        ctx.quadraticCurveTo(271,543,271,543);
        ctx.lineTo(271,163);
        ctx.quadraticCurveTo(271,163,271,163);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        // hull - left and right part
        
        switch (displayMode){
        case DEFAULT:
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#000000");
            break;
        case SELECTED:
            ctx.setFillStyle (color);
            ctx.setStrokeStyle(color);
            break;
        case NOT_SELECTED:
            ctx.setGlobalAlpha(BOAT_NOT_SELECTED_OPACITY);
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#000000");
            break;
        }
        
        ctx.setLineWidth(5.0);
        ctx.setLineJoin(LineJoin.ROUND);
        ctx.beginPath();
        ctx.moveTo(3.54,2);
        ctx.lineTo(801,2);
        ctx.lineTo(1170,37);
        ctx.lineTo(801,72);
        ctx.lineTo(3.54,72);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.setLineWidth(5.0);
        ctx.setLineJoin(LineJoin.ROUND);
        ctx.beginPath();
        ctx.moveTo(3.54,622);
        ctx.lineTo(798,622);
        ctx.lineTo(1170,657);
        ctx.lineTo(798,692);
        ctx.lineTo(3.54,692);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        // Ausleger
        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.setLineWidth(3.0);

        ctx.beginPath();
        ctx.moveTo(1580,356);
        ctx.lineTo(747,356);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(1050,651);
        ctx.lineTo(1050,49);
        ctx.fill();
        ctx.stroke();
        
        ctx.beginPath();
        ctx.moveTo(1170,37);
        ctx.lineTo(1580,356);
        ctx.lineTo(1170,657);
        ctx.lineTo(1170,657);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(1580,355);
        ctx.lineTo(778,-229);
        ctx.bezierCurveTo(778,-229,1220,-175,1350,-47.6);
        ctx.bezierCurveTo(1490,81.4,1580,355,1580,355);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(1050,354);
        ctx.lineTo(791,183);
        ctx.bezierCurveTo(791,183,997,236,1030,277);
        ctx.bezierCurveTo(1070,314,1050,354,1050,354);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(744,355);
        ctx.lineTo(199,247);
        ctx.bezierCurveTo(199,247,542,185,646,238);
        ctx.bezierCurveTo(749,294,744,355,744,355);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.beginPath();
        ctx.moveTo(479,675);
        ctx.lineTo(567,55.2);
        ctx.lineTo(780,-225);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(744,357);
        ctx.lineTo(200,465);
        ctx.bezierCurveTo(200,465,542,527,646,474);
        ctx.bezierCurveTo(749,418,744,357,744,357);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(1050,359);
        ctx.lineTo(792,529);
        ctx.bezierCurveTo(792,529,998,476,1030,436);
        ctx.bezierCurveTo(1070,399,1050,359,1050,359);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(1580,360);
        ctx.lineTo(780,944);
        ctx.bezierCurveTo(780,944,1210,890,1340,763);
        ctx.bezierCurveTo(1490,634,1580,360,1580,360);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("rgba(0, 0, 0, 0)");
        ctx.setStrokeStyle("rgba(0, 0, 0, 255)");
        ctx.beginPath();
        ctx.moveTo(479,40.9);
        ctx.lineTo(567,661);
        ctx.lineTo(780,944);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(1050,355);
        ctx.lineTo(751,307);
        ctx.bezierCurveTo(751,307,959,268,1010,293);
        ctx.bezierCurveTo(1070,303,1050,355,1050,355);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(745,357);
        ctx.lineTo(194,297);
        ctx.bezierCurveTo(194,297,529,205,637,249);
        ctx.bezierCurveTo(745,296,745,357,745,357);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {
        ctx.beginPath();
        ctx.moveTo(1050,360);
        ctx.lineTo(751,408);
        ctx.bezierCurveTo(751,408,959,447,1010,422);
        ctx.bezierCurveTo(1070,412,1050,360,1050,360);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(744,358);
        ctx.lineTo(193,418);
        ctx.bezierCurveTo(193,418,528,510,636,466);
        ctx.bezierCurveTo(744,419,744,358,744,358);
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

