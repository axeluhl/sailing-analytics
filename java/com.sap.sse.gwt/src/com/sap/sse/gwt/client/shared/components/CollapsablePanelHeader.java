package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.shared.components.CollapsablePanel.Imager;

/**
 * The default header widget used within a {@link CollapsablePanel}.
 */
public class CollapsablePanelHeader extends FlowPanel implements HasText, OpenHandler<CollapsablePanel>,
        CloseHandler<CollapsablePanel> {

    private final Label textLabel;
    private final Image iconImage;
    private final Imager imager;
    private final CollapsablePanel collapsablePanel;
    private final boolean hasToolbar;
    private final SimplePanel toolbarPanel;
    private final ClickableHeader clickableHeaderPanel;

    private static final String STYLENAME_TOOLBAR = "collapsablePanel-toolbar";

    private static final String STYLENAME_HEADER_COLLAPSE_AREA = "collapsablePanel-header-collapse";

    private static final String STYLENAME_HEADER_TEXT = "collapsablePanel-header-text";

    private static final String STYLENAME_HEADER_IMAGE = "collapsablePanel-header-image";

    protected CollapsablePanelHeader(CollapsablePanel collapsablePanel, Imager imager, String text, boolean hasToolbar) {
        this.imager = imager;
        this.hasToolbar = hasToolbar;
        this.collapsablePanel = collapsablePanel;
        iconImage = imager.makeImage();
        iconImage.addStyleName(STYLENAME_HEADER_IMAGE);
        textLabel= new Label(text);
        textLabel.addStyleName(STYLENAME_HEADER_TEXT);

        clickableHeaderPanel = new ClickableHeader(collapsablePanel);
        clickableHeaderPanel.setStyleName(STYLENAME_HEADER_COLLAPSE_AREA);
        
        add(clickableHeaderPanel);
            
        FlowPanel clickableInnerPanel = new FlowPanel();
        clickableHeaderPanel.setWidget(clickableInnerPanel);
        
        clickableInnerPanel.add(iconImage);
        clickableInnerPanel.add(textLabel);

        if(hasToolbar) {
            toolbarPanel = new SimplePanel();
            add(toolbarPanel);
        } else {
            toolbarPanel = null;
        }
        
        collapsablePanel.addOpenHandler(this);
        collapsablePanel.addCloseHandler(this);
        setStyle();
    }

    protected CollapsablePanelHeader(CollapsablePanel collapsablePanel, final ImageResource openImage,
            final ImageResource closedImage, String text, boolean hasToolbar) {
        this(collapsablePanel, new Imager() {
            public Image makeImage() {
                return new Image(closedImage);
            }

            public void updateImage(boolean open, Image image) {
                if (open) {
                    image.setResource(openImage);
                } else {
                    image.setResource(closedImage);
                }
            }
        }, text, hasToolbar);
    }

    public void setToolbar(Widget toolbar) {
        if (hasToolbar && toolbarPanel != null) {
            toolbarPanel.setWidget(toolbar);
            toolbarPanel.setStyleName(STYLENAME_TOOLBAR);
        }
    }

    protected void setCollapsingEnabled(boolean enabled)
    {
        clickableHeaderPanel.setClickable(enabled);
    }
    
    public final String getText() {
        return textLabel.getText();
    }

    public final void onClose(CloseEvent<CollapsablePanel> event) {
        setStyle();
    }

    public final void onOpen(OpenEvent<CollapsablePanel> event) {
        setStyle();
    }

    public final void setText(String text) {
        textLabel.setText(text);
    }

    private void setStyle() {
        imager.updateImage(collapsablePanel.isOpen(), iconImage);
    }
    
    /**
     * Used to wrap widgets in the header to provide click support. Effectively wraps the widget in an
     * <code>anchor</code> to get automatic keyboard access.
     */
    private final class ClickableHeader extends SimplePanel {
        private final CollapsablePanel panel;
        
        private boolean isClickable = true;
        
        private ClickableHeader(final CollapsablePanel panel) {
            // Anchor is used to allow keyboard access.
            super(DOM.createAnchor());
            this.panel = panel;
            Element elem = getElement();
            elem.setPropertyString("href", "javascript:void(0);");
            // Avoids layout problems from having blocks in inlines.
            //DOM.setStyleAttribute(elem, "display", "block");
            sinkEvents(Event.ONCLICK);
        }

        protected void setClickable(boolean enabled) {
            this.isClickable = enabled;
        }
        
        @Override
        public void onBrowserEvent(Event event) {
            // no need to call super.
            switch (DOM.eventGetType(event)) {
            case Event.ONCLICK:
                // Prevent link default action.
                event.preventDefault();
                if(isClickable) {
                    panel.setOpen(!panel.isOpen());
                }
            }
        }
    }

}