package com.sap.sailing.gwt.home.desktop.partials.useraccountDetails;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface UseraccountDetailsResources extends ClientBundle {
    public static final UseraccountDetailsResources INSTANCE = GWT.create(UseraccountDetailsResources.class);

    @Source("UseraccountDetails.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String useraccountdetails();
        String useraccountdetails_image();
        String useraccountdetails_image_editbutton();
        String useraccountdetails_content();
        String useraccountdetails_content_detail();
        String useraccountdetails_content_detail_label();
        String useraccountdetails_content_detail_input();
        String useraccountdetails_content_submit();
    }
}
