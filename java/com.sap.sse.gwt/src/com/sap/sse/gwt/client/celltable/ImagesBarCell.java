package com.sap.sse.gwt.client.celltable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * A cell type for use in a {@link CellTable} which renders a horizontal sequence of images. Use by subclassing and
 * overriding the {@link #getImageSpecs()} method where action name, tool tip and image prototype can be provided for
 * each image to be rendered. Add a {@link FieldUpdater} to the column rendered using this cell type; the updater will
 * receive the clicked image's action name as the <code>value</code> parameter. The image rendering style can be adjusted by
 * overriding the default {@link #getImageStyle()} implementation.
 */
public abstract class ImagesBarCell extends AbstractSafeHtmlCell<String> {
    /**
     * Subclasses use this template to render a single image tag
     */
    private final static ImagesBarTemplates imageTemplate = GWT.create(ImagesBarTemplates.class);
    
    private Context context;
    
    protected class ImageSpec {
        private final AbstractImagePrototype imagePrototype;
        private final String actionName;
        private final String tooltip;

        public ImageSpec(String actionName, String tooltip, ImageResource imageResource) {
            this(actionName, tooltip, AbstractImagePrototype.create(imageResource));
        }

        public ImageSpec(String actionName, String tooltip, AbstractImagePrototype imagePrototype) {
            super();
            this.imagePrototype = imagePrototype;
            this.actionName = actionName;
            this.tooltip = tooltip;
        }

        public AbstractImagePrototype getImagePrototype() {
            return imagePrototype;
        }

        public String getActionName() {
            return actionName;
        }

        public String getTooltip() {
            return tooltip;
        }
    }

    interface ImagesBarTemplates extends SafeHtmlTemplates {
        /**
         * @param name
         * @param title
         *            the tool-tip to display for the image on mouse-over
         * @param value
         *            how to render the image; this needs to be an &lt;img&gt; tag, not enclosed by any other element,
         *            as returned by {@link ImagesBarCell#makeImagePrototype(ImageResource)}
         */
        @SafeHtmlTemplates.Template("<div name=\"{0}\" style=\"{1}\" title=\"{2}\">{3}</div>")
        SafeHtml cell(String name, SafeStyles styles, String title, SafeHtml value);
    }

    public ImagesBarCell() {
        super(SimpleSafeHtmlRenderer.getInstance(), "click", "keydown");
    }

    public ImagesBarCell(SafeHtmlRenderer<String> renderer) {
        super(renderer, "click", "keydown");
    }

    protected static ImagesBarTemplates getImageTemplate() {
        return imageTemplate;
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
    }

    /**
     * onEnterKeyDown is called when the user presses the ENTER key while the Cell is selected. You are not required to
     * override this method, but it is a common convention that allows your cell to respond to key events.
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
        if (valueUpdater != null) {
            valueUpdater.update(value);
        }
    }

    /**
     * Make icons available as image prototype
     */
    protected static AbstractImagePrototype makeImagePrototype(ImageResource resource) {
        return AbstractImagePrototype.create(resource);
    }
    
    protected abstract Iterable<ImageSpec> getImageSpecs();
    
    protected Context getContext() {
        return context;
    }

    
    /**
     * @param data Has to contain a list of allowed actions or '*' if everything is permitted. The list has to be 
     * comma separated. All commas in the allowed actions have to be escaped with a \\ as well as every \\. 
     * The allowed actions are compared to the action name of the {@link ImageSpec} and only those where the 
     * action name corresponds to one allowed action from the list will be displayed.
     */
    @Override
    protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
        this.context = context;
        
        /*
         * Always do a null check on the value. Cell widgets can pass null to
         * cells if the underlying data contains a null, or if the data arrives
         * out of order.
         */
        if (data != null) {
            List<String> allowedActions = new ArrayList<>();
            String currentAction = "";
            String dataAsString = data.asString();
            boolean escaped = false;
            for (int i = 0; i < dataAsString.length(); i++) {
                char character = dataAsString.charAt(i);
                if (escaped) {
                    if (character == ',' || character == '\\') {
                        currentAction += character;
                        escaped = false;
                    } else {
                        throw new IllegalArgumentException("A character other than ',' or '\\' was illegaly escaped.");
                    }
                } else {
                    if (character == '\\') {
                        escaped = true;
                    } else if (character == ',') {
                        allowedActions.add(currentAction);
                        currentAction = "";
                    } else {
                        currentAction += character;
                    }
                }
            }
            if (escaped) {
                throw new IllegalArgumentException("A '\\' was illegaly found at the end of the data string.");
            }
            allowedActions.add(currentAction);
            SafeStyles imgStyle = getImageStyle();
            for (ImageSpec imageSpec : getImageSpecs()) {
                if (dataAsString.equals("*") || allowedActions.contains(imageSpec.getActionName())) {
                    SafeHtml rendered;
                    rendered = getImageTemplate().cell(imageSpec.getActionName(), imgStyle, imageSpec.getTooltip(),
                            imageSpec.getImagePrototype().getSafeHtml());
                    sb.append(rendered);
                }
            }
        }
    }

    protected SafeStyles getImageStyle() {
        return SafeStylesUtils.fromTrustedString("float:left;cursor:hand;cursor:pointer;padding-right:5px;");
    }
}