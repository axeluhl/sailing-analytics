package com.sap.sailing.gwt.home.client.shared.videoPlayer;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

public class PlayButton extends Widget {
    
    private final PlayButtonResources.LocalCss style = PlayButtonResources.INSTANCE.css();
    
    public PlayButton() {
        style.ensureInjected();
        setElement(Document.get().createDivElement());
        getElement().addClassName(style.playButton());
    }
}
