package com.sap.sse.security.ui.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

/**
 * Resources used by the authentication framework.
 */
public interface UserManagementResources extends ClientBundle {
    
    public static final UserManagementResources INSTANCE = GWT.create(UserManagementResources.class);

    @Source("UserManagement.gss")
    LocalCss css();
    
    @Source("facebook.svg")
    @MimeType("image/svg+xml")
    DataResource facebook();
    
    @Source("google.svg")
    @MimeType("image/svg+xml")
    DataResource google();

    public interface LocalCss extends CssResource {
        String flyover();
        String flyover_small_hidden();
        String flyover_position_grid();
        String flyover_content_wrapper();
        String flyover_content();
        String form_title();
        String form_subtitle();
        String form_description();
        String form_errors();
        String form_label();
        String form_input();
        String form_link();
        String form_actions();
        String form_action_button();
        String form_social_seperator();
        String form_social_seperator_text();
        String form_social_button();
        String form_social_button_facebook();
        String form_social_button_google();
        String info_user();
        String info_user_image();
        String info_user_image_item();
        String info_user_realname();
        String info_user_username();
    }
}
