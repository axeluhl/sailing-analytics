package com.sap.sailing.gwt.ui.datamining.presentation;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ExportChartAsCsvToClipboardButton extends HorizontalPanel {
    
    private Chart chartToExport;
    
    public ExportChartAsCsvToClipboardButton(StringMessages stringMessages) {
        setHorizontalAlignment(ALIGN_LEFT);
        setVerticalAlignment(ALIGN_MIDDLE);
        setSpacing(10);
        Button exportButton = new Button(stringMessages.exportStatisticsCurveToCsv());
        exportButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                String csvContentExport = createCsvExportContentForStatisticsCurve();
                copyTextToClipboard(csvContentExport);
                Window.alert(stringMessages.csvCopiedToClipboard());
            }

        });
        add(exportButton);
    }
    
    private String createCsvExportContentForStatisticsCurve() {
        if(chartToExport != null && chartToExport.getSeries().length > 0) {
            StringBuilder csvStr = new StringBuilder("Series name");
            for (Point point : chartToExport.getSeries()[0].getPoints()) {
                csvStr.append(';');
                csvStr.append(point.getX());
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
    
    public Chart getChartToExport() {
        return chartToExport;
    }
    
    public void setChartToExport(Chart chartToExport) {
        this.chartToExport = chartToExport;
    }
    
    public static native void copyTextToClipboard(String text) /*-{
        var textArea = document.createElement("textarea");
        //
        // *** This styling is an extra step which is likely not required. ***
        //
        // Why is it here? To ensure:
        // 1. the element is able to have focus and selection.
        // 2. if element was to flash render it has minimal visual impact.
        // 3. less flakyness with selection and copying which **might** occur if
        //    the textarea element is not visible.
        //
        // The likelihood is the element won't even render, not even a flash,
        // so some of these are just precautions. However in IE the element
        // is visible whilst the popup box asking the user for permission for
        // the web page to copy to the clipboard.
        //
    
        // Place in top-left corner of screen regardless of scroll position.
        textArea.style.position = 'fixed';
        textArea.style.top = 0;
        textArea.style.left = 0;
    
        // Ensure it has a small width and height. Setting to 1px / 1em
        // doesn't work as this gives a negative w/h on some browsers.
        textArea.style.width = '2em';
        textArea.style.height = '2em';
    
        // We don't need padding, reducing the size if it does flash render.
        textArea.style.padding = 0;
    
        // Clean up any borders.
        textArea.style.border = 'none';
        textArea.style.outline = 'none';
        textArea.style.boxShadow = 'none';
    
        // Avoid flash of white box if rendered for any reason.
        textArea.style.background = 'transparent';
    
    
        textArea.value = text;
    
        document.body.appendChild(textArea);
    
        textArea.select();
    
        try {
            var successful = document.execCommand('copy');
        } catch (err) {
            console.log('Unable to copy');
        }
        document.body.removeChild(textArea);
    }-*/;
    
}
