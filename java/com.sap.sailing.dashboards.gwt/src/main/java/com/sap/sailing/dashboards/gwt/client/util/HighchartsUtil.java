package com.sap.sailing.dashboards.gwt.client.util;

import org.moxieapps.gwt.highcharts.client.Chart;

import com.google.gwt.user.client.Timer;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class HighchartsUtil {

    /**
     * The {@link Chart} spacing is not consistent
     * so there is a need to correct the top, bottom and right spacing 
     * of the chart to make it look visually right.
     * */
    public static void correctSpacingOfChart(Chart chart) {
        chart.setSpacingTop(17)
        .setSpacingBottom(13)
        .setSpacingRight(35);
    }

    /**
     * In cases where it is necessary to initialize a {@link Chart} with dynamic size during the same browser loop, where it´s dynamic container is initialized,
     * the {@link Chart.setSizeToMatchContainer()} method will not work. The solution to that problem is to call {@link Chart#setSizeToMatchContainer()} later,
     * when the container of the chart is completely initialized and its size is available to the {@link Chart}.
     * */
    public static void setSizeToMatchContainerDelayed(final Chart chart) {
        Timer elapsedTimer = new Timer() {
            public void run() {
                chart.setSizeToMatchContainer();
            }
        };
        elapsedTimer.schedule(500);
    }
}
