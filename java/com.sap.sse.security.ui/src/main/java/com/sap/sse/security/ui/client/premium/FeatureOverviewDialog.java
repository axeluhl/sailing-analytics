package com.sap.sse.security.ui.client.premium;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.DialogBox;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class FeatureOverviewDialog extends DialogBox{
    
    private CellTable<String> table;
    private final StringMessages stringMessages;
    private final PayWallResolver payWallResolver;
    
    
    public FeatureOverviewDialog(PayWallResolver payWallResolver) {
        this.stringMessages = StringMessages.INSTANCE;
        this.payWallResolver = payWallResolver;
        createTable();
    }


    private void createTable() {
        this.table = new CellTable<String>();
    }
    
}
