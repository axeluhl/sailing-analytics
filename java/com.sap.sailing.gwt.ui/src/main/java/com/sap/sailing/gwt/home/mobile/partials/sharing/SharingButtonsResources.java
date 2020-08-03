package com.sap.sailing.gwt.home.mobile.partials.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface SharingButtonsResources extends SharedHomeResources {
    public static final SharingButtonsResources INSTANCE = GWT.create(SharingButtonsResources.class);

    @Source("SharingButtons.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String eventheader_sharing();
        String eventheader_sharing_item();
        String eventheader_sharing_itemcopytoclipboard();
        String eventheader_sharing_itemshare();
    }

}
