package com.sap.sailing.gwt.home.mobile.places.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface MediaViewResources extends SharedHomeResources {
    public static final MediaViewResources INSTANCE = GWT.create(MediaViewResources.class);


    @Source("MediaViewImpl.gss")
    LocalCss css();
    
    public interface LocalCss extends CssResource {
        String popup();
        String button();
        String addButton();
        String deleteButton();
        String editButton();
    }
    
}
