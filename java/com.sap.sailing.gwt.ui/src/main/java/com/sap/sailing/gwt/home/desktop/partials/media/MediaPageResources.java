package com.sap.sailing.gwt.home.desktop.partials.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sailing.gwt.home.desktop.resources.SharedDesktopResources;

public interface MediaPageResources extends SharedDesktopResources {
    public static final MediaPageResources INSTANCE = GWT.create(MediaPageResources.class);

    @Source("MediaPage.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String media();
        String dark();
        String photolist();
        String imggalleryitem();
        String media_settings();
        String thumbnail_edit_button();
        String thumbnail_delete_button();
        String active();
        String button();
        String manageMedia();
        String addButton();
        String popup();
    }
}
