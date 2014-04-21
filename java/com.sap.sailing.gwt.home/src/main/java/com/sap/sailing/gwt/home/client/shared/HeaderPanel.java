package com.sap.sailing.gwt.home.client.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class HeaderPanel extends Composite {
//    @UiField Label titleLabel;
//    
//    @UiField LocaleSelection localeSelection;

    private static HeaderPanelUiBinder uiBinder = GWT.create(HeaderPanelUiBinder.class);

    interface HeaderPanelUiBinder extends UiBinder<Widget, HeaderPanel> {
    }

    public HeaderPanel() {
        super();
 
        initWidget(uiBinder.createAndBindUi(this));
        
//        titleLabel.setText("SAP Sailing Analytics");
    }

    public void setTitle(String title) {
//        titleLabel.setText(title);
    }
    
}
