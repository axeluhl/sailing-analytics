package com.sap.sailing.gwt.home.client.shared.media;

import java.util.List;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;

public class FullscreenViewer {
    private PopupPanel popup = new PopupPanel(true);
    private final List<ImageMetadataDTO> images;

    public FullscreenViewer(ImageMetadataDTO selected, List<ImageMetadataDTO> images) {
        this.images = images;
        final GalleryPlayer viewer = new GalleryPlayer(selected, images);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                viewer.setHeight(Window.getClientHeight() + "px");
                viewer.setWidth(Window.getClientWidth() + "px");
                viewer.onResize();
            }
        });

        RootPanel.get().getElement().getStyle().setOverflow(Overflow.HIDDEN);
        viewer.setHeight(Window.getClientHeight() + "px");
        viewer.setWidth(Window.getClientWidth() + "px");
        viewer.onResize();
        popup.setWidget(viewer);

        popup.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {

                popup.center();
            }
        });
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                RootPanel.get().getElement().getStyle().setOverflow(Overflow.AUTO);
            }
        });
        viewer.setCloseCommand(new Command() {
            @Override
            public void execute() {
                popup.hide();
            }
        });
    }
}
