package com.sap.sailing.gwt.home.shared.partials.videoplayer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface PlayButtonResources extends ClientBundle {
    public static final PlayButtonResources INSTANCE = GWT.create(PlayButtonResources.class);

    @Source("PlayButton.gss")
    LocalCss css();
    
    @Source("play.png")
    ImageResource play();

    public interface LocalCss extends CssResource {
        String playButton();
    }
}
