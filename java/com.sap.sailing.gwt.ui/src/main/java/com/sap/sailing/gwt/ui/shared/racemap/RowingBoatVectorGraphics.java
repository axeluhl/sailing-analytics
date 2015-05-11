package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.RowingBoatClassMasterdata;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class RowingBoatVectorGraphics extends RowingBoatClassVectorGraphics {
    
    private final static double OVERALL_LENGHT_IN_PIXEL = 988;
    private final static double BEAM_IN_PIXEL = 106;
    
    public RowingBoatVectorGraphics(RowingBoatClassMasterdata... compatibleBoatClasses) {
        super(OVERALL_LENGHT_IN_PIXEL, BEAM_IN_PIXEL, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, boolean isSelected, String color) {

        //draw hull
        if(isSelected) {
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
        } else {
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
        }
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(2.153454,57.161882);
        ctx.bezierCurveTo(288.47233,125.92963,699.08468,125.92963,989.8426,55.1639);
        ctx.bezierCurveTo(701.3042,-12.76749,288.47233,-13.92911,2.153454,57.161882);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw cockpit
        if(isSelected) {
            ctx.setFillStyle ("#FFFFFF");
            ctx.setStrokeStyle("#FFFFFF");
        } else {
            ctx.setFillStyle(color);
            ctx.setStrokeStyle(color);
        }
        ctx.beginPath();
        ctx.moveTo(294,89);
        ctx.bezierCurveTo(435.93372999999997,101.44578,574.06582,101.21940000000001,708,87);
        ctx.lineTo(708,24);
        ctx.bezierCurveTo(570,7.7507774,432,7.2656434,294,23);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawOars(Context2d ctx, boolean isPullingOars, String color) {
        
        if (isPullingOars) {
            ctx.rotate(180 * Math.PI / 180);
            ctx.translate(-OVERALL_LENGHT_IN_PIXEL, -BEAM_IN_PIXEL);
        }
        
        //draw oars
        ctx.setFillStyle(color);
        ctx.setStrokeStyle("#FFFFFF");
        ctx.setLineWidth(15.0);
        ctx.setLineCap("butt");
        ctx.setLineJoin("miter");
        
        ctx.beginPath();
        ctx.moveTo(319.02901,47.970988);
        ctx.bezierCurveTo(483.97099,-235.97099,483.97099,-235.97099,483.97099,-235.97099);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(508.02901,47.970988);
        ctx.bezierCurveTo(672.97099,-235.97099,672.97099,-235.97099,672.97099,-235.97099);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(318.04192,55.52116);
        ctx.bezierCurveTo(482.95808,360.47884,482.95808,360.47884,482.95808,360.47884);
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(508.03826,59.51926);
        ctx.bezierCurveTo(672.96174,358.48074,672.96174,358.48074,672.96174,358.48074);
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle(color);
        ctx.setStrokeStyle(color);
        
        ctx.beginPath();
        ctx.moveTo(472,-245);
        ctx.lineTo(495,-229);
        ctx.lineTo(525,-275);
        ctx.lineTo(503,-290);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(660,-245);
        ctx.lineTo(683,-229);
        ctx.lineTo(713,-275);
        ctx.lineTo(691,-290);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(471.00666,367.72838);
        ctx.lineTo(494.00088,351.41616);
        ctx.lineTo(523.99334,398.3138);
        ctx.lineTo(501.99887,413.60651);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(663.00666,364.72838);
        ctx.lineTo(686.00088,348.41616);
        ctx.lineTo(715.9933400000001,395.3138);
        ctx.lineTo(693.9988700000001,410.60651);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }
}
