package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.spectator.SpectatorViewTableResources.SpectatorViewTableStyle;

public interface LeaderboardGroupFullTableResources extends CellTable.Resources {
    @Source({ CellTable.Style.DEFAULT_CSS, "LeaderboardGroupFullTable.css" })
    SpectatorViewTableStyle cellTableStyle();
    
    interface LeaderboardGroupTableStyle extends CellTable.Style {
    }
}
