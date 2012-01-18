package com.sap.sailing.gwt.ui.shared.components;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel.Imager;

/**
 * The default header widget used within a {@link CollapsablePanel}.
 */
public class CollapsablePanelDefaultHeader extends Widget implements HasText, OpenHandler<CollapsablePanel>,
        CloseHandler<CollapsablePanel> {

    /**
     * imageTD holds the image for the icon, not null. labelTD holds the text for the label.
     */
    private final Element labelTD;

    private final Image iconImage;
    private final Imager imager;
    private final CollapsablePanel collapsablePanel;

    protected CollapsablePanelDefaultHeader(CollapsablePanel collapsablePanel, Imager imager, String text) {
        this.imager = imager;
        this.collapsablePanel = collapsablePanel;
        iconImage = imager.makeImage();

        // I do not need any Widgets here, just a DOM structure.
        Element root = DOM.createTable();
        Element tbody = DOM.createTBody();
        Element tr = DOM.createTR();
        final Element imageTD = DOM.createTD();
        labelTD = DOM.createTD();

        setElement(root);

        DOM.appendChild(root, tbody);
        DOM.appendChild(tbody, tr);
        DOM.appendChild(tr, imageTD);
        DOM.appendChild(tr, labelTD);

        // set image TD to be same width as image.
        DOM.setElementProperty(imageTD, "align", "center");
        DOM.setElementProperty(imageTD, "valign", "middle");
        DOM.setStyleAttribute(imageTD, "width", iconImage.getWidth() + "px");

        DOM.appendChild(imageTD, iconImage.getElement());

        setText(text);

        collapsablePanel.addOpenHandler(this);
        collapsablePanel.addCloseHandler(this);
        setStyle();
    }

    protected CollapsablePanelDefaultHeader(CollapsablePanel collapsablePanel, final ImageResource openImage,
            final ImageResource closedImage, String text) {
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
        }, text);
    }

    public final String getText() {
        return DOM.getInnerText(labelTD);
    }

    public final void onClose(CloseEvent<CollapsablePanel> event) {
        setStyle();
    }

    public final void onOpen(OpenEvent<CollapsablePanel> event) {
        setStyle();
    }

    public final void setText(String text) {
        DOM.setInnerText(labelTD, text);
    }

    private void setStyle() {
        imager.updateImage(collapsablePanel.isOpen(), iconImage);
    }
}
