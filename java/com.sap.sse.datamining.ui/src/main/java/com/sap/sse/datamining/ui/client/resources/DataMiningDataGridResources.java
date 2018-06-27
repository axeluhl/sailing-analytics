package com.sap.sse.datamining.ui.client.resources;

import com.google.gwt.user.cellview.client.DataGrid;

public interface DataMiningDataGridResources extends DataGrid.Resources {

    @Source({ DataGrid.Style.DEFAULT_CSS, "DataMiningDataGrid.css" })
    DataMiningDataGridStyle dataGridStyle();
    
    interface DataMiningDataGridStyle extends DataGrid.Style {
        
        String dataGridSubHeader();
        
        String dataGridSpacedSubHeader();

        String dataGridSubHeaderLabel();

        String dataGridClearFocus();
        
    }
}
