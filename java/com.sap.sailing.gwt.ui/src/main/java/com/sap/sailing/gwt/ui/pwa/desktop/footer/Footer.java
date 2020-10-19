package com.sap.sailing.gwt.ui.pwa.desktop.footer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.pwa.AdminConsoleSwitchingEntryPoint;

public class Footer extends Composite {
    private static FooterPanelUiBinder uiBinder = GWT.create(FooterPanelUiBinder.class);

    interface FooterPanelUiBinder extends UiBinder<Widget, Footer> {
    }
    
    @UiField
    HTMLPanel footerPanel;
    
    @UiField
    Anchor mobileViewAnchor;
    
    public Footer() {
        FooterResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));  
        
        mobileViewAnchor.getElement().getStyle().setMarginRight(5, Unit.EM); 
        
        Anchor classicViewAnchor = new Anchor();
        classicViewAnchor.setHref("AdminConsole.html");
        classicViewAnchor.setText("Switch to classic view");
        footerPanel.add(classicViewAnchor);
    }
    
    @UiHandler("mobileViewAnchor")
    void onClickMobileAnchor(ClickEvent event) {
        event.preventDefault();
        AdminConsoleSwitchingEntryPoint.switchToMobile();   
    }
    
}
