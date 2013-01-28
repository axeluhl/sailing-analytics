package com.sap.sailing.gwt.ui.test;

import static org.junit.Assert.*;

import static org.hamcrest.core.IsEqual.*;
import org.junit.Test;
import org.moxieapps.gwt.highcharts.client.Point;

import com.sap.sailing.gwt.ui.shared.charts.WindChartPointRecalculator;

public class TestWindChartPointRecalculation {

    @Test
    public void testRecalculaion() {
        Point notToBeRecalculated = new Point(10, 100);
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(90.0, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(null, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(90.0, null, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(100.27458, 110.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(90.0, 100.75639, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        
        //Test for special case, which caused a wrong recalculation, because of the wrong use of Math.abs()
        notToBeRecalculated = new Point(10, 179.8537);
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(180.12356, 190.0, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(170.0, 179.5372, notToBeRecalculated).getY(), equalTo(notToBeRecalculated.getY()));
        
        Point toBeMovedDown = new Point(10, 356);
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(4.0, 23.0, toBeMovedDown).getY(), equalTo(new Point(10, 356.0 - 360.0).getY()));
        
        Point toBeMovedUp = new Point(10, 5);
        assertThat(WindChartPointRecalculator.recalculateDirectionPoint(356.0, 359.83364, toBeMovedUp).getY(), equalTo(new Point(10, 5.0 + 360.0).getY()));
    }

}
