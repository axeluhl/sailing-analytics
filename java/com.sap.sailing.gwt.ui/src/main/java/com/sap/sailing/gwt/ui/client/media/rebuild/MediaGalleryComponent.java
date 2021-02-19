package com.sap.sailing.gwt.ui.client.media.rebuild;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentWithoutSettings;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MediaGalleryComponent extends ComponentWithoutSettings {
//    private final Button button;
//    private final DockLayoutPanel docklayoutpanel;
    private final Widget widget;
    
    private static MediaGalleryComponentUiBinder uiBinder = GWT.create(MediaGalleryComponentUiBinder.class);

    interface MediaGalleryComponentUiBinder extends UiBinder<Widget, MediaGalleryComponent> {
    }
    
    public MediaGalleryComponent(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
        this.widget = uiBinder.createAndBindUi(this);
//        this.docklayoutpanel = new DockLayoutPanel(Unit.PX);
//        this.button = new Button("TEST XXX");
//        docklayoutpanel.add(button);
    }

    @Override
    public void setVisible(boolean visibility) {
        widget.setVisible(visibility);
    }

    @Override
    public boolean isVisible() {
        return widget.isVisible();
    }

    @Override
    public String getLocalizedShortName() {
        return "Media Gallery";
    }

    @Override
    public String getId() {
        return "media-component";
    }

    @Override
    public Widget getEntryWidget() {
        return widget;
    }

    @Override
    public String getDependentCssClassName() {
        return "";
    }
}