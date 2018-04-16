package com.sap.sse.gwt.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface HighChartsRessource extends ClientBundle {
    /**
     * Package common controls css files as text resource so it can be injected when required. We need to move away from
     * this css file.
     * 
     * @return
     */
    @Source("css/CommonControls.css")
    TextResource commonControls();

    @Source("highcharts/highcharts.js")
    TextResource highcharts();
    
    @Source("highcharts/js/themes/grid.js")
    TextResource highchartsThemeGrid();

    @Source("highcharts/js/modules/exporting.js")
    TextResource highchartsModuleExport();

    @Source("highcharts/js/highcharts-more.js")
    TextResource highchartsMore();
}
