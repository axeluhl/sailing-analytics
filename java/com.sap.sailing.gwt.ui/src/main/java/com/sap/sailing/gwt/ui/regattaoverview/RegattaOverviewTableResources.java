package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.user.cellview.client.CellTable;

public interface RegattaOverviewTableResources extends CellTable.Resources {
    interface AdminConsoleTableStyle extends CellTable.Style {
        /**
         * Applied to header cells of race columns
         */
        String cellTableRaceNameColumnHeader();

        /**
         * Applied to the totals columns
         */
        String cellTableRaceNameColumn();

    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "RegattaOverviewTable.css" })
    AdminConsoleTableStyle cellTableStyle();
}
