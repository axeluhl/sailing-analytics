package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ViewerToolbar extends ClientBundle {
    @Source("ViewerToolbar.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String viewerToolbar();

        @ClassName("viewerToolbar-settingsButtonPanel")
        String viewerToolbarSettingsButtonPanel();

        @ClassName("viewerToolbar-settingsButton")
        String viewerToolbarSettingsButton();

        @ClassName("viewerToolbar-toolbaritem")
        String viewerToolbarToolbaritem();

        @ClassName("viewerToolbar-innerElement")
        String viewerToolbarInnerElement();
    }
}
