package com.sap.sailing.gwt.regattaoverview.client;

import com.google.gwt.user.cellview.client.CellTable;

public interface RegattaRaceStatesTableResources extends CellTable.Resources {
    interface AdminConsoleTableStyle extends CellTable.Style {
        /**
         * Applied to header cells of race columns
         */
        String cellTableRaceNameColumnHeader();

        /**
         * Applied to the totals columns
         */
        String cellTableRaceNameColumn();
        
        String flagColumn();

    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "RegattaRaceStatesTable.css" })
    AdminConsoleTableStyle cellTableStyle();
}
