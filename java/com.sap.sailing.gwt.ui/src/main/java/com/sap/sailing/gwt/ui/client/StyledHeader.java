package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;

public abstract class StyledHeader<H> extends Header<H> {

    /**
     * {@link SafeHtmlTemplates} for header rendering.
     */
    interface Template extends SafeHtmlTemplates {
        @Template("<div class=\"{0}\">{1}</div>")
        SafeHtml header(String classes, SafeHtml contents);
    }

    /** {@link Template} instance. */

    /** Style classes. */
    private final List<String> styleNames;

    /**
     * Construct a new StyledHeader.
     * 
     * @param cell
     *            the {@link Cell} responsible for rendering items in the header
     */
    public StyledHeader(Cell<H> cell) {
        super(cell);
        styleNames = new ArrayList<String>();
    }

    /**
     * Adds a style name to this object.
     */
    public void addStyleName(String style) {
        if (!styleNames.contains(style)) {
            styleNames.add(style);
        }
    }

    /**
     * Removes a style name from this object.
     */
    public void removeStyleName(String style) {
        styleNames.remove(style);
    }

    /**
     * Returns all of the object's style names, as a space-separated list.
     * 
     * @return the objects's space-separated style names
     */
    public String getStyleName() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String styleName : styleNames) {
            if (first) {
                first = false;
            } else {
                result.append(' ');
            }
            result.append(styleName);
        }
        return result.toString();
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div class=\""+getStyleName()+"\">");
        super.render(context, sb);
        sb.appendHtmlConstant("</div>");
    }

}
