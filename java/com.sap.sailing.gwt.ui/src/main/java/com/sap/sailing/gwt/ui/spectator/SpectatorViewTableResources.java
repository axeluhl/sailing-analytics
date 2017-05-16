package com.sap.sailing.gwt.ui.spectator;

import com.google.gwt.user.cellview.client.CellTable;

public interface SpectatorViewTableResources extends CellTable.Resources {
    interface SpectatorViewTableStyle extends CellTable.Style {
    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "SpectatorViewTable.css" })
    SpectatorViewTableStyle cellTableStyle();
}
