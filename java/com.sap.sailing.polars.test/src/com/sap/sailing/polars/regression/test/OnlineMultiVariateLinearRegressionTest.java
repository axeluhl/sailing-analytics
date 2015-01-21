package com.sap.sailing.polars.regression.test;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.polars.regression.OnlineMultiVariateRegression;
import com.sap.sailing.polars.regression.impl.OnlineMultiVariateRegressionImpl;

public class OnlineMultiVariateLinearRegressionTest {

    private static double EPSILON = 0.5;
    
    @Test
    public void testRegressionWithPolarData() throws NotEnoughDataHasBeenAddedException {
        OnlineMultiVariateRegression regression = new OnlineMultiVariateRegressionImpl(3);
        
        double[] x1  = {1, 1, 1};
        regression.addData(-0.646600, x1);
        double[] x2  = {2, 4, 8};
        regression.addData(0.764900, x2);
        double[] x3  = {3, 9, 27};
        regression.addData(2.105400, x3);
        double[] x4  = {4, 16, 64};
        regression.addData(3.376700, x4);
        double[] x5  = {5, 25, 125};
        regression.addData(4.580600, x5);
        double[] x6  = {6, 36, 216};
        regression.addData(5.718900, x6);
        double[] x7  = {7, 49, 343};
        regression.addData(6.793400, x7);
        double[] x8  = {8, 64, 512};
        regression.addData(7.805900, x8);
        double[] x9  = {9, 81, 729};
        regression.addData(8.758200, x9);
        double[] x10  = {10, 100, 1000};
        regression.addData(9.652100, x10);
        double[] x11  = {11, 121, 1331};
        regression.addData(10.489400, x11);
        double[] x12  = {12, 144, 1728};
        regression.addData(11.271900, x12);
        double[] x13  = {13, 169, 2197};
        regression.addData(12.001400, x13);
        double[] x14  = {14, 196, 2744};
        regression.addData(12.679700, x14);
        double[] x15  = {15, 225, 3375};
        regression.addData(13.308600, x15);
        double[] x16  = {16, 256, 4096};
        regression.addData(13.889900, x16);
        double[] x17  = {17, 289, 4913};
        regression.addData(14.425400, x17);
        double[] x18  = {18, 324, 5832};
        regression.addData(14.916900, x18);
        double[] x19  = {19, 361, 6859};
        regression.addData(15.366200, x19);
        double[] x20  = {20, 400, 8000};
        regression.addData(15.775100, x20);
        double[] x21  = {21, 441, 9261};
        regression.addData(16.145400, x21);
        double[] x22  = {22, 484, 10648};
        regression.addData(16.478900, x22);
        double[] x23  = {23, 529, 12167};
        regression.addData(16.777400, x23);
        double[] x24  = {24, 576, 13824};
        regression.addData(17.042700, x24);
        double[] x25  = {25, 625, 15625};
        regression.addData(17.276600, x25);
        double[] x26  = {26, 676, 17576};
        regression.addData(17.480900, x26);
        double[] x27  = {27, 729, 19683};
        regression.addData(17.657400, x27);
        double[] x28  = {28, 784, 21952};
        regression.addData(17.807900, x28);
        double[] x29  = {29, 841, 24389};
        regression.addData(17.934200, x29);
        double[] x30  = {30, 900, 27000};
        regression.addData(18.038100, x30);
        double[] x31  = {31, 961, 29791};
        regression.addData(18.121400, x31);
        double[] x32  = {32, 1024, 32768};
        regression.addData(18.185900, x32);
        double[] x33  = {33, 1089, 35937};
        regression.addData(18.233400, x33);
        double[] x34  = {34, 1156, 39304};
        regression.addData(18.265700, x34);
        double[] x35  = {35, 1225, 42875};
        regression.addData(18.284600, x35);
        double[] x36  = {36, 1296, 46656};
        regression.addData(18.291900, x36);
        double[] x37  = {37, 1369, 50653};
        regression.addData(18.289400, x37);
        double[] x38  = {38, 1444, 54872};
        regression.addData(18.278900, x38);
        double[] x39  = {39, 1521, 59319};
        regression.addData(18.262200, x39);
        double[] x40  = {40, 1600, 64000};
        regression.addData(18.241100, x40);
        
        double estimatedY = regression.estimateY(x27);
        assertThat(estimatedY, closeTo(17.657400, EPSILON));

    }

}
