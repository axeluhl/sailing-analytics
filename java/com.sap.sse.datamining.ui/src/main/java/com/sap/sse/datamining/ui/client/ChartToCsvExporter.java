package com.sap.sse.datamining.ui.client;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.sap.sse.gwt.shared.TextExporter;

/**
 * Provides a service for export of chart data into CSV format.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ChartToCsvExporter {

    private final String exportedMessage;

    public ChartToCsvExporter(String exportedMessage) {
        this.exportedMessage = exportedMessage;
    }

    /**
     * Converts the data of the chart into CSV format and stores it in clipboard. Finally, window alert gets shown
     * to the user with information that there is CSV content in his clipboard.
     * 
     * @param chartToExport
     *            Chart with data to export
     */
    public void exportChartAsCsvToClipboard(Chart chartToExport) {
        String csvContentExport = createCsvExportContentForStatisticsCurve(chartToExport);
        TextExporter.exportToClipboard(csvContentExport);
        Window.alert(exportedMessage);
    }

    private static String createCsvExportContentForStatisticsCurve(Chart chartToExport) {
        if (chartToExport != null && chartToExport.getSeries().length > 0) {
            StringBuilder csvStr = new StringBuilder("Series name");
            for (Point point : chartToExport.getSeries()[0].getPoints()) {
                String name = point.getName();
                csvStr.append(';');
                if (name != null && !name.isEmpty()) {
                    csvStr.append(name);
                } else {
                    csvStr.append(point.getX());
                }
            }
            csvStr.append("\r\n");
            for (Series series : chartToExport.getSeries()) {
                csvStr.append(series.getName());
                for (Point point : series.getPoints()) {
                    csvStr.append(';');
                    csvStr.append(NumberFormat.getDecimalFormat().format(point.getY()));
                }
                csvStr.append("\r\n");
            }
            String csvContentExport = csvStr.toString();
            return csvContentExport;
        }
        return "Statistics are empty";
    }

}
