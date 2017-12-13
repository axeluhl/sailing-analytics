package com.sap.sse.security.ui.client;

import com.google.gwt.user.cellview.client.CellTable;

public interface SecurityTableResources extends CellTable.Resources {
    @Override
    @Source({CellTable.Style.DEFAULT_CSS, "com/sap/sse/security/ui/client/SecurityTableResources.css"})
    SecurityTableStyle cellTableStyle();
}
