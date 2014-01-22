package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.LineJoin;

public class SmallMultihullVectorGraphics extends BoatClassVectorGraphics {
    
    public SmallMultihullVectorGraphics(String... compatibleBoatClassNames) {
        super("Multihull", 5.25, 2.6, 2.62, compatibleBoatClassNames);
    }

    @Override
    protected void drawBoat(Context2d ctx, boolean isSelected, String color) {
        // outer net
        if(isSelected) {
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
        } else {
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
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
        if(isSelected) {
            ctx.setFillStyle("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
        } else {
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
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
        if(isSelected) {
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
        } else {
            ctx.setFillStyle("#FFFFFF");
            ctx.setStrokeStyle("#000000");
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
