package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.resources.client.CssResource;

public interface Css extends CssResource {

    String loginPanel();
    
    @ClassName("loginPanel-titlePanel")
    String loginPanelTitlePanel();
    
    @ClassName("loginPanel-infoPanel")
    String loginPanelInfoPanel();
    
    @ClassName("loginPanel-expanded")
    String loginPanelExpanded();
    
    @ClassName("loginPanel-collapsed")
    String loginPanelCollapsed();
}
