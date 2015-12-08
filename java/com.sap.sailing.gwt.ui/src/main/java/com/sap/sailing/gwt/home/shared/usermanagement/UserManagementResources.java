package com.sap.sailing.gwt.home.shared.usermanagement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UserManagementResources extends ClientBundle {
    
    public static final UserManagementResources INSTANCE = GWT.create(UserManagementResources.class);

    @Source("UserManagement.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String flyover();
        String flyover_content();
        String form_title();
        String form_label();
        String form_input();
        String form_errors();
        String form_link();
        String form_actions();
        String form_action_button();
        String form_social_seperator();
        String form_social_seperator_text();
        String form_social_button();
        String form_social_button_facebook();
        String form_social_button_google();
    }
    
}
