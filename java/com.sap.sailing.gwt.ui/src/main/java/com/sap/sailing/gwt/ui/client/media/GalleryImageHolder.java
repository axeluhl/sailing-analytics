package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.desktop.partials.media.MediaPageResources;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;
import com.sap.sse.gwt.client.media.ImageDTO;

public class GalleryImageHolder extends Composite implements HasClickHandlers {

    private static VideoThumbnailUiBinder uiBinder = GWT.create(VideoThumbnailUiBinder.class);
    private final String imageSourceRef;
    private final Date imageCreateAt;

    interface VideoThumbnailUiBinder extends UiBinder<Widget, GalleryImageHolder> {
    }

    @UiField
    DivElement imageHolderUi;
    
    @UiField
    Anchor deleteAnchor;
    @UiField
    Anchor editAnchor;
    @UiField
    DivElement overlay;

    public GalleryImageHolder(ImageDTO image, ClickHandler deleteHandler) {
        initWidget(uiBinder.createAndBindUi(this));
        SharedResources.INSTANCE.mainCss().ensureInjected();
        MediaPageResources.INSTANCE.css().ensureInjected();
        SharedHomeResources.INSTANCE.sharedHomeCss().ensureInjected();
        this.imageSourceRef = image.getSourceRef();
        this.imageCreateAt = image.getCreatedAtDate();
        deleteAnchor.addClickHandler(deleteHandler);
        // TODO: activate after implementing edit logic
        editAnchor.setVisible(false);
        //editAnchor.addClickHandler(editHandler);
        imageHolderUi.getStyle().setBackgroundImage("url(\"" + image.getSourceRef() + "\")");
        
    }
    
    public boolean isImage(ImageDTO image) {
        return image != null && image.getSourceRef().equals(imageSourceRef) && image.getCreatedAtDate().equals(imageCreateAt);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
    
    public void setManageable(boolean manageable) {
        if (manageable) {
            overlay.getStyle().setDisplay(Display.BLOCK);
        } else {
            overlay.getStyle().setDisplay(Display.NONE);
        }
    }

}
