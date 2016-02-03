package com.sap.sailing.gwt.common.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;


public interface SAPHeaderWithAuthenticationResources extends ClientBundle {
    public static final SAPHeaderWithAuthenticationResources INSTANCE = GWT.create(SAPHeaderWithAuthenticationResources.class);
    
    @Source("header-with-authentication.gss")
    RaceBoardMainCss css();
    
    public interface RaceBoardMainCss extends CssResource {
        String header_right_wrapper();
        
        String usermanagement_icon();
        String usermanagement_loggedin();
        String usermanagement_view();
        String usermanagement_open();
    }
}