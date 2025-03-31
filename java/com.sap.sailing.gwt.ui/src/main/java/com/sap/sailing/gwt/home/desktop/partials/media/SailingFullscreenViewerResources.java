package com.sap.sailing.gwt.home.desktop.partials.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SailingFullscreenViewerResources extends ClientBundle {
    public static final SailingFullscreenViewerResources INSTANCE = GWT.create(SailingFullscreenViewerResources.class);

    @Source("SailingFullscreenViewer.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {

        String is_autoplaying();
    }
}
