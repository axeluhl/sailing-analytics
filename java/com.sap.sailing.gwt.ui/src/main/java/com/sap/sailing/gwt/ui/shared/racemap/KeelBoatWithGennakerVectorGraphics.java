package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class KeelBoatWithGennakerVectorGraphics extends BoatClassVectorGraphics {
    
    public KeelBoatWithGennakerVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(800, 235, 655, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {

        ctx.translate(0,-803);
        
        //draw hull
        
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
        ctx.moveTo(2.4885714,1009.9999);
        ctx.lineTo(1.4375714000000002,853.99996);
        ctx.lineTo(1.4375714000000002,832.99996);
        ctx.bezierCurveTo(1.4375714000000002,832.99996,244.42857,779.99996,427.42857000000004,830.99996);
        ctx.bezierCurveTo(579.42857,872.99996,654.42857,920.99996,654.42857,920.99996);
        ctx.bezierCurveTo(654.42857,920.99996,583.42857,961.99996,428.42857000000004,1009.9999);
        ctx.bezierCurveTo(256.42857000000004,1069.9999,2.488571400000012,1009.9999,2.488571400000012,1009.9999);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw front deck
        ctx.setFillStyle ("#FFFFFF");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(2.0);
        ctx.beginPath();
        ctx.moveTo(342.42857,982.99996);
        ctx.lineTo(347.42857,952.99996);
        ctx.lineTo(346.42857,892.99996);
        ctx.lineTo(343.42857,862.99996);
        ctx.lineTo(422.42857,870.99996);
        ctx.bezierCurveTo(446.42857,874.99996,483.42857,875.99996,493.42857,884.99996);
        ctx.bezierCurveTo(502.42857,912.99996,498.42857,932.99996,494.42857,952.99996);
        ctx.bezierCurveTo(493.42857,960.99996,449.42857,965.99996,422.42857,971.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw cockpit
        
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
        ctx.moveTo(343.42857,966.99996);
        ctx.bezierCurveTo(260.42857,989.99993,128.42856999999998,982.99996,0.4345714300000054,980.99996);
        ctx.lineTo(0.4345714300000054,860.99996);
        ctx.bezierCurveTo(129.42857,858.99996,260.42857,847.99996,342.42857,877.99996);
        ctx.lineTo(345.42857,891.99996);
        ctx.lineTo(345.42857,954.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw mast
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.beginPath();
        ctx.moveTo(400.42856,917);
        ctx.lineTo(413.42856,917);
        ctx.quadraticCurveTo(415.42856,917,415.42856,919);
        ctx.lineTo(415.42856,925);
        ctx.quadraticCurveTo(415.42856,927,413.42856,927);
        ctx.lineTo(400.42856,927);
        ctx.quadraticCurveTo(398.42856,927,398.42856,925);
        ctx.lineTo(398.42856,919);
        ctx.quadraticCurveTo(398.42856,917,400.42856,917);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
            
            //Draw Gennaker
            ctx.setFillStyle(SAIL_FILLCOLOR);
            ctx.setStrokeStyle(SAIL_FILLCOLOR);
            ctx.setLineWidth(1.0);
            ctx.beginPath();
            ctx.moveTo(395.85714,927.14281);
            ctx.bezierCurveTo(391.71428000000003,945.71424,395.42857000000004,974.5713900000001,323.14285,1025.7142000000001);
            ctx.bezierCurveTo(271.42857,1049,248.42857,1068.8571,183,1059);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();
            
            //Draw MainSail
            ctx.beginPath();
            ctx.moveTo(792.42857,921.99996);
            ctx.bezierCurveTo(630.42857,981.99996,434.42857000000004,941.99996,416.42857000000004,921.99996);
            ctx.bezierCurveTo(420.42857000000004,1009.9999,357.42857000000004,1089.9999,291.42857000000004,1129.9999);
            ctx.bezierCurveTo(558.42857,1239.9999,821.42857,1139.9999,792.42857,921.99996);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();
            
            //Draw Gennaker Boom
            ctx.setFillStyle("#000000");
            ctx.setStrokeStyle("#000000");
            ctx.setLineWidth(8.0);
            ctx.beginPath();
            ctx.moveTo(790.42857,923.99996);
            ctx.lineTo(624.42857,934.99996);
            ctx.fill();
            ctx.stroke();

            //Draw Boom
            ctx.setFillStyle("#000000");
            ctx.setStrokeStyle("#000000");
            ctx.setLineWidth(10.0);
            ctx.beginPath();
            ctx.moveTo(394.67324,922.7569);
            ctx.lineTo(182.04103,1055.2431);
            ctx.fill();
            ctx.stroke();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {
         
        //Draw Gennaker
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(792.42857,922.99996);
        ctx.bezierCurveTo(629.42857,862.99996,434.42857000000004,901.99996,416.42857000000004,921.99996);
        ctx.bezierCurveTo(419.42857000000004,829.99996,354.42857000000004,751,288.42857000000004,705);
        ctx.bezierCurveTo(552.42857,595,810.42857,679,792.42857,922.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw Mainsail
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.beginPath();
        ctx.moveTo(394.28886,917.2931);
        ctx.bezierCurveTo(385.43074,871.73691,347.71545,836.41908,306.71448,811.41908);
        ctx.bezierCurveTo(281.14193,797.58307,228.42654,777.29228,181.42541999999997,789.03868);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw Gennaker Boom
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(8.0);
        ctx.beginPath();
        ctx.moveTo(790.42857,923.99996);
        ctx.lineTo(624.42857,934.99996);
        ctx.fill();
        ctx.stroke();

        //Draw Boom
        ctx.setLineWidth(10.0);
        ctx.beginPath();
        ctx.moveTo(393.72086,920.03959);
        ctx.lineTo(181.1363,792.53176);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
       
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {

        //draw mainsail
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.beginPath();
        ctx.moveTo(628.42857,920.99996);
        ctx.bezierCurveTo(582.42857,958.99996,554.42857,972.99996,514.42857,980.99996);
        ctx.bezierCurveTo(458.42857000000004,987.99996,439.42857000000004,987.99996,395.42857000000004,980.99996);
        ctx.bezierCurveTo(471.42857000000004,972.99996,547.42857,958.99996,628.42857,920.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //draw headsail
        ctx.beginPath();
        ctx.moveTo(399.42857,924.99996);
        ctx.bezierCurveTo(362.42857,939.99996,325.42857,955.99996,277.42857,954.99996);
        ctx.bezierCurveTo(241.42856999999998,958.99996,188.42856999999998,950.99996,134.42856999999998,938.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw boom
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(10.0);
        ctx.beginPath();
        ctx.moveTo(393.42857,921.99996);
        ctx.lineTo(135.42856999999998,935.99996);
        ctx.fill();
        ctx.stroke();
       
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {

        //draw mainsail
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(397.42857,917.99996);
        ctx.bezierCurveTo(356.42857,896.99996,314.42857,884.99996,273.42857,884.99996);
        ctx.bezierCurveTo(226.42856999999998,883.99996,179.42856999999998,890.99996,132.42856999999998,902.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw headsail
        ctx.beginPath();
        ctx.moveTo(630.42857,921.99996);
        ctx.bezierCurveTo(584.42857,880.99996,556.42857,869.99996,516.42857,861.99996);
        ctx.bezierCurveTo(460.42857000000004,853.99996,441.42857000000004,847.99996,397.42857000000004,859.99996);
        ctx.bezierCurveTo(473.42857000000004,868.99996,549.42857,881.99996,630.42857,921.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw boom
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(10.0);
        ctx.beginPath();
        ctx.moveTo(394.42857,921.99996);
        ctx.lineTo(135.42856999999998,906.99996);
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
}
