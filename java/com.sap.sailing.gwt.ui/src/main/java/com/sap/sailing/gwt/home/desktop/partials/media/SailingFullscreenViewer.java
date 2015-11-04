package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.partials.fullscreen.FullscreenContainer;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel.FullscreenViewer;

public class SailingFullscreenViewer extends FullscreenContainer<SailingGalleryPlayer> implements
        FullscreenViewer<SailingImageDTO> {
    
    private static final String IS_AUTOPLAYING_STYLE = SailingFullscreenViewerResources.INSTANCE.css().is_autoplaying();
    
    private final Image autoRefreshControl = new Image("images/home/reload.svg");
    private SailingGalleryPlayer player = null;

    public SailingFullscreenViewer() {
        SailingFullscreenViewerResources.INSTANCE.css().ensureInjected();
        autoRefreshControl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SailingFullscreenViewer.this.toggleAutoplay();
            }
        });
        if (ExperimentalFeatures.SHOW_AUTOPLAY_IMAGES_ON_DESKTOP) {
            addToolbarAction(autoRefreshControl);
        }
    }

    public void show(SailingImageDTO selected, Collection<SailingImageDTO> images) {
        showContent(player = new SailingGalleryPlayer(selected, images));
        autoRefreshControl.setStyleName(IS_AUTOPLAYING_STYLE, player.isAutoplaying());
    }
    
    private void toggleAutoplay() {
        if (ExperimentalFeatures.SHOW_AUTOPLAY_IMAGES_ON_DESKTOP) {
            if (player != null) {
                player.toggleAutoplay();
                autoRefreshControl.setStyleName(IS_AUTOPLAYING_STYLE, player.isAutoplaying());
            }
        }
    }
}
