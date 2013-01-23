package com.sap.sailing.gwt.ui.polarsheets;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsChartPanel extends SimplePanel {

    private VerticalPanel mainPanel;
    private StringMessages stringMessages;

    public PolarSheetsChartPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        mainPanel = new VerticalPanel();
        mainPanel.setSize("100%", "100%");
        setWidget(mainPanel);
        Label polarSheetsChartLabel = new Label(stringMessages.polarSheetChart());
        mainPanel.add(polarSheetsChartLabel);
        mainPanel.add(createPolarSheetChart());
    }

    private Widget createPolarSheetChart() {
        Chart polarSheetChart = new Chart();
        polarSheetChart.setType(Series.Type.LINE);
        polarSheetChart.setOption("/chart/polar", true);

        return polarSheetChart;
    }

}
