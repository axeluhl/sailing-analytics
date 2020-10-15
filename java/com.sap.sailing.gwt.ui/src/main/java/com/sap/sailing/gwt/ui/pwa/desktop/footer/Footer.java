package com.sap.sailing.gwt.ui.pwa.desktop.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }
    
    @UiField
    HTMLPanel footerPanel;
    
    public Footer() {
        FooterResources.INSTANCE.css().ensureInjected();
        
        Anchor classicViewAnchor = new Anchor();
        classicViewAnchor.setHref("AdminConsole.html");
        classicViewAnchor.setText("Switch to classic view");
        footerPanel.add(classicViewAnchor);
        
        initWidget(uiBinder.createAndBindUi(this));  
    }
    
}
