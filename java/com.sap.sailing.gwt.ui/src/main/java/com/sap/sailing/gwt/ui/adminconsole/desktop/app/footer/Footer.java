package com.sap.sailing.gwt.ui.adminconsole.desktop.app.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }
    
    public Footer() {
        FooterResources.INSTANCE.css().ensureInjected();
        
        initWidget(uiBinder.createAndBindUi(this));  
    }
    
}
