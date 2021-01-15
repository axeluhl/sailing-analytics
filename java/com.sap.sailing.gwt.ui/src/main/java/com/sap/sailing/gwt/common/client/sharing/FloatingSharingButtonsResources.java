package com.sap.sailing.gwt.common.client.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface FloatingSharingButtonsResources {
    public static final FloatingSharingButtonsResources INSTANCE = GWT.create(FloatingSharingButtonsResources.class);

    @Source("SharingButtons.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String sharing_item();
        String sharing_itemcopytoclipboard();
        String sharing_itemshare();
        String sharing_container();
        String sharing_faded_out();
        String sharing_faded_in();
    }
    
    @Source("../../ui/client/images/share.svg")
    @MimeType("image/svg+xml")
    DataResource sharingIcon();
    
    @Source("../../ui/client/images/flat_copy.svg")
    @MimeType("image/svg+xml")
    DataResource copyIcon();

}
