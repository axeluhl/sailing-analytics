package com.sap.sailing.gwt.home.desktop.partials.sharing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sse.gwt.common.CommonIcons;

public interface EventHeaderSharingButtonsResources extends CommonIcons {
    public static final EventHeaderSharingButtonsResources INSTANCE = GWT.create(EventHeaderSharingButtonsResources.class);

    @Source("EventHeaderSharingButtons.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String eventheader_sharing();
        String eventheader_sharing_item();
        String eventheader_sharing_itememail();
        String eventheader_sharing_itemtwitter();
        String eventheader_sharing_itemfacebook();
        String eventheader_sharing_itemcopytoclipboard();
    }
    
    @Source("../../../../ui/client/images/flat_copy.svg")
    @MimeType("image/svg+xml")
    DataResource copyIcon();
    
}
