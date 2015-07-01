package com.sap.sailing.gwt.home.client.shared.videoPlayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface VideoPlayerResources extends ClientBundle {
    public static final VideoPlayerResources INSTANCE = GWT.create(VideoPlayerResources.class);

    @Source("VideoPlayer.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String videoPlayer();
    }
}
