package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.BoatClassMasterdata;

public class DinghyWithSpinnakerVectorGraphics extends BoatClassVectorGraphics {
    
    public DinghyWithSpinnakerVectorGraphics(BoatClassMasterdata... compatibleBoatClasses) {
        super(499, 290, 499, compatibleBoatClasses);
    }

    @Override
    protected void drawBoat(Context2d ctx, boolean isSelected, String color) {

        ctx.translate(0,-823);

        //draw hull
        ctx.setFillStyle ("#FFFFFF");
        ctx.setStrokeStyle("#FFFFFF");
        ctx.setLineWidth(1.0);
        ctx.beginPath();
        ctx.moveTo(7,871);
        ctx.bezierCurveTo(203.08186,821.33557,387.86061,795.01762,518,938);
        ctx.bezierCurveTo(398.86654,1076.7397,202.1551,1045.3186,9,1000);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
        
        //draw cockpit
        ctx.setFillStyle (color);
        ctx.setStrokeStyle(color);
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
        ctx.moveTo(307.81752, 937.22012);
        ctx.bezierCurveTo(314.345, 964.9351099999999, 316.56174, 997.0000399999999, 283.42062, 1048.3229);
        ctx.bezierCurveTo(283.42062, 1048.3229, 259.83009, 1093.7070999999999, 180.78139, 1133.9968);
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
        ctx.bezierCurveTo(316.60510000000005,897.97361,304.70538000000005,871.99857,281.72357,823.37622);
        ctx.bezierCurveTo(268.51855,799.4476,218.11345,743.28885,174.32941,729.90564);
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
        ctx.bezierCurveTo(272.42857,954.99996,235.42856999999998,970.99996,187.42856999999998,969.99996);
        ctx.bezierCurveTo(151.42856999999998,973.99996,98.42856999999998,965.99996,44.42856999999998,953.99996);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Draw head sail
        ctx.beginPath();
        ctx.moveTo(485.38382,936.09069);
        ctx.bezierCurveTo(439.4015,987.37151,411.41227000000003,1006.2644,371.42765,1017.0605);
        ctx.bezierCurveTo(315.44918,1026.5069,296.45649000000003,1026.5069,252.47341000000003,1017.0605);
        ctx.bezierCurveTo(328.44418,1006.2644,404.41496000000006,987.3715100000001,485.38382,936.09069);
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
        ctx.moveTo(485.38386,937.92122);
        ctx.bezierCurveTo(439.40153000000004,884.50601,411.41228,870.1750999999999,371.42765,859.75262);
        ctx.bezierCurveTo(315.44916,849.33014,296.45645,841.51328,252.47335000000004,857.1469999999999);
        ctx.bezierCurveTo(328.44416,868.8722899999999,404.41497000000004,885.80882,485.38386,937.92122);
        ctx.closePath();
        ctx.fill();
        ctx.stroke();

        //Draw head sail
        ctx.beginPath();
        ctx.moveTo(308.42857,932.99996);
        ctx.bezierCurveTo(267.42857,911.99996,225.42856999999998,899.99996,184.42856999999998,899.99996);
        ctx.bezierCurveTo(137.42856999999998,898.99996,90.42856999999998,905.99996,43.42856999999998,917.99996);
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
