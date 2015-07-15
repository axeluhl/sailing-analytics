package com.sap.sailing.gwt.home.client.shared.videoPlayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface PlayButtonResources extends ClientBundle {
    public static final PlayButtonResources INSTANCE = GWT.create(PlayButtonResources.class);

    @Source("PlayButton.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String playButton();
    }
}
