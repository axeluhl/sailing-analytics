package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public abstract class ImagesBarCell extends AbstractSafeHtmlCell<String> {

    interface ImagesBarTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<div name=\"{0}\" style=\"{1}\" title=\"{2}\">{3}</div>")
        SafeHtml cell(String name, SafeStyles styles, String title, SafeHtml value);
    }

    public ImagesBarCell() {
        super(SimpleSafeHtmlRenderer.getInstance(), "click", "keydown");
    }

    public ImagesBarCell(SafeHtmlRenderer<String> renderer) {
        super(renderer, "click", "keydown");
    }

    /**
     * Called when an event occurs in a rendered instance of this Cell. The
     * parent element refers to the element that contains the rendered cell, NOT
     * to the outermost element that the Cell rendered.
     */
    @Override
    public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, 
            String value, NativeEvent event, com.google.gwt.cell.client.ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Handle the click event.
        if ("click".equals(event.getType())) {
            // Ignore clicks that occur outside of the outermost element.
            EventTarget eventTarget = event.getEventTarget();

            if (parent.isOrHasChild(Element.as(eventTarget))) {

                // if (parent.getFirstChildElement().isOrHasChild(
                // Element.as(eventTarget))) {
                // use this to get the selected element!!
                Element el = Element.as(eventTarget);

                // check if we really click on the image
                if (el.getNodeName().equalsIgnoreCase("IMG")) {
                    doAction(el.getParentElement().getAttribute("name"), valueUpdater);
                }
            }
        }
    };

    /**
     * onEnterKeyDown is called when the user presses the ENTER key will the
     * Cell is selected. You are not required to override this method, but its a
     * common convention that allows your cell to respond to key events.
     */
    @Override
    protected void onEnterKeyDown(Context context, Element parent,
    String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
        doAction(value, valueUpdater);
    }

    /**
     * Intern action
     * @param value
     *            selected value
     * @param valueUpdater
     *            value updater or the custom value update to be called
     */
    protected void doAction(String value, ValueUpdater<String> valueUpdater) {
        // Trigger a value updater. In this case, the value doesn't actually
        // change, but we use a ValueUpdater to let the app know that a value
        // was clicked.
        if (valueUpdater != null)
            valueUpdater.update(value);
    }

    /**
     * Make icons available as SafeHtml
     * @param resource
     * @return
     */
    protected static SafeHtml makeImage(ImageResource resource) {
        return AbstractImagePrototype.create(resource).getSafeHtml();
    }
}