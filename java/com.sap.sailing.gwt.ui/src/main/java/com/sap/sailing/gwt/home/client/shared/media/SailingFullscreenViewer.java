package com.sap.sailing.gwt.home.client.shared.media;

import java.util.Collection;

import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel.FullscreenViewer;

public class SailingFullscreenViewer extends FullscreenContainer implements FullscreenViewer<SailingImageDTO> {
    public void show(SailingImageDTO selected, Collection<SailingImageDTO> images) {
        showContent(new SailingGalleryPlayer(selected, images));
    }
}
