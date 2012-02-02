package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleTableResources.AdminConsoleTableStyle;

public interface LeaderboardGroupFullTableResources extends CellTable.Resources {
    @Source({ CellTable.Style.DEFAULT_CSS, "LeaderboardGroupCompactTable.css" })
    AdminConsoleTableStyle cellTableStyle();
    
    interface LeaderboardGroupTableStyle extends CellTable.Style {
    }
}
