package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel.FullscreenViewer;

public class SailingFullscreenViewer extends FullscreenContainer<SailingGalleryPlayer> implements
        FullscreenViewer<SailingImageDTO> {
    private final Image autoRefreshControl = new Image("images/home/reload.svg");

    public SailingFullscreenViewer() {
        SailingFullscreenViewerResources.INSTANCE.css().ensureInjected();
    }

    public void show(SailingImageDTO selected, Collection<SailingImageDTO> images) {
        final SailingGalleryPlayer player = new SailingGalleryPlayer(selected, images);
        showContent(player);
        autoRefreshControl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                player.toggleAutoplay();
                if (player.isAutoplaying()) {
                    autoRefreshControl.addStyleName(SailingFullscreenViewerResources.INSTANCE.css().is_autoplaying());
                } else {
                    autoRefreshControl
                            .removeStyleName(SailingFullscreenViewerResources.INSTANCE.css().is_autoplaying());
                }
            }
        });
        addToolbarAction(autoRefreshControl);
    }
}
