package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.gwt.ui.client.shared.racemap.BoatOverlay.DisplayMode;

public class DinghyWithSpinnakerVectorGraphics extends BoatClassVectorGraphics {
    
    public DinghyWithSpinnakerVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(520, 290, 580, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, DisplayMode displayMode, String color) {

        ctx.translate(0,-823);

        //draw hull
        
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
        ctx.moveTo(7,871);
        ctx.bezierCurveTo(203.08186,821.33557,387.86061,795.01762,518,938);
        ctx.bezierCurveTo(398.86654,1076.7397,202.1551,1045.3186,9,1000);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw cockpit
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
        ctx.moveTo(15,970);
        ctx.lineTo(14,899);
        ctx.bezierCurveTo(104,880.09176,194,873.28046,284,873);
        ctx.lineTo(310,938);
        ctx.lineTo(289,1000);
        ctx.bezierCurveTo(200.13659,1004.8195,109.19085999999999,997.14515,15,970);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw mast
        ctx.setFillStyle ("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.beginPath();
        ctx.moveTo(310.42856,932);
        ctx.lineTo(323.42856,932);
        ctx.quadraticCurveTo(325.42856,932,325.42856,934);
        ctx.lineTo(325.42856,940);
        ctx.quadraticCurveTo(325.42856,942,323.42856,942);
        ctx.lineTo(310.42856,942);
        ctx.quadraticCurveTo(308.42856,942,308.42856,940);
        ctx.lineTo(308.42856,934);
        ctx.quadraticCurveTo(308.42856,932,310.42856,932);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

    }

    @Override
    protected void drawDownwindPortTackSails(Context2d ctx) {
        
        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        
        ctx.beginPath();
        ctx.moveTo(483.39385, 940.24997);
        ctx.bezierCurveTo(493.41256, 992.75855, 460.41989, 1017.7084, 441.43329, 1032.3219);
        ctx.bezierCurveTo(412.4518, 1047.8829999999998, 344.45204, 1048.657, 288.46314, 1015.8399);
        ctx.bezierCurveTo(339.64457, 1018.8109999999999, 446.11047, 1052.666, 483.39385000000004, 940.24997);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.beginPath();
        ctx.moveTo(491, 756);
        ctx.bezierCurveTo(466.59067, 843.59728, 411.74259, 903.40265, 326, 935);
        ctx.bezierCurveTo(378.86232, 1011.5883, 392.18243, 1091.7712999999999, 328, 1179);
        ctx.bezierCurveTo(561.60771, 1114.4904, 656.52336, 990.80552, 491, 756);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        ctx.beginPath();
        ctx.moveTo(307.81752,937.22012);
        ctx.bezierCurveTo(327.345,973.93511,320.56174,1038,293.42062,1072.3229);
        ctx.bezierCurveTo(295.15614999999997,1075.7939999999999,224.69723999999997,1140.4414,180.78139,1133.9968);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        

        // Draw boom
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(8.0);
        ctx.beginPath();
        ctx.moveTo(303.89922, 935.5301);
        ctx.lineTo(176.81505, 1133.4699);
        ctx.fill();
        ctx.stroke();

        // Draw spinnaker boom
        ctx.setLineWidth(6.0);
        ctx.beginPath();
        ctx.moveTo(327.08094, 930.88705);
        ctx.lineTo(488.7762, 755.11286);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawDownwindStarboardTackSails(Context2d ctx) {

        ctx.setFillStyle(SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(308.21838,933.17535);
        ctx.bezierCurveTo(316.60510000000005,897.97361,329.70538000000005,858.99857,296.72357000000005,799.37622);
        ctx.bezierCurveTo(259.51855000000006,749.4476,211.11345000000006,737.28885,174.32941000000005,729.90564);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(483.38386,939.9212);
        ctx.bezierCurveTo(493.40153000000004,884.506,460.41228,858.1751,441.42765,842.7526);
        ctx.bezierCurveTo(412.44916,826.3301,344.45645,825.5133000000001,288.47335000000004,860.147);
        ctx.bezierCurveTo(339.64947000000006,857.0114000000001,446.10434000000004,821.2824,483.38386,939.9212);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(489.77217,1118.8474);
        ctx.bezierCurveTo(465.78351,1029.7929,411.8807,968.99104,327.61588,936.86809);
        ctx.bezierCurveTo(379.56713,859.0052300000001,392.65768,777.4879800000001,329.58141,688.80739);
        ctx.bezierCurveTo(559.163,754.39085,652.44283,880.1341100000001,489.77217,1118.8474);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw spinnaker boom
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(6.0);
        ctx.beginPath();
        ctx.moveTo(489.44644,1119.9788);
        ctx.lineTo(327.4107,940.02111);
        ctx.fill();
        ctx.stroke();

        //Draw  boom
        ctx.setLineWidth(8.0);
        ctx.beginPath();
        ctx.moveTo(302.80389,933.12438);
        ctx.lineTo(173.05327,732.44697);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindPortTackSails(Context2d ctx) {

        //Draw main sail
        ctx.setFillStyle (SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(309.42857,939.99996);
        ctx.bezierCurveTo(284.42857,965.99996,272.42857,991.99996,224.42856999999998,990.99996);
        ctx.bezierCurveTo(188.42856999999998,994.99996,98.42856999999998,965.99996,44.42856999999998,953.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Draw head sail
        ctx.moveTo(329,939);
        ctx.lineTo(482,939);
        ctx.bezierCurveTo(431.2033,992.12409,365.02878,1010.6481,293,1016);
        ctx.bezierCurveTo(308.37324,991.77921,330.90565,970.62611,329,939);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw boom
        ctx.setFillStyle ("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(8.0);
        ctx.beginPath();
        ctx.moveTo(304.42857,936.99996);
        ctx.lineTo(46.42856999999998,950.99996);
        ctx.fill();
        ctx.stroke();
    }

    @Override
    protected void drawUpwindStarboardTackSails(Context2d ctx) {

        //Draw main sail
        ctx.setFillStyle (SAIL_FILLCOLOR);
        ctx.setStrokeStyle(SAIL_FILLCOLOR);
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(308,933);
        ctx.bezierCurveTo(276,900,260,882,219,882);
        ctx.bezierCurveTo(172,881,90.4,906,43.4,918);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Draw head sail
        ctx.beginPath();
        ctx.moveTo(331,936);
        ctx.lineTo(482,940);
        ctx.bezierCurveTo(428.25579,880.50234,358.31076,864.65787,292,860);
        ctx.bezierCurveTo(309.42057,879.09774,329.05764,903.69536,331,936);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //Draw boom
        ctx.setFillStyle ("#000000");
        ctx.setStrokeStyle("#000000");
        ctx.setLineWidth(8.0);
        ctx.beginPath();
        ctx.moveTo(305.42857,936.99996);
        ctx.lineTo(46.42856999999998,921.99996);
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
