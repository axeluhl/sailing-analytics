package com.sap.sse.gwt.client.controls;

import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Allows cycling through text cells in a CellTable using TAB by setting a decent {@code tabIndex} instead of -1.
 * 
 * To use, also set {@code cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);}.
 * 
 * @author Fredrik Teschke
 *
 */
public class TabbingTextInputCell extends TextInputCell {
    interface Template extends SafeHtmlTemplates {
        @Template("<input type=\"text\" value=\"{0}\"></input>")
        SafeHtml input(String value);
    }

    private static Template template;

    public TabbingTextInputCell() {
        super();
        if (template == null) {
            template = GWT.create(Template.class);
        }
    }

    @Override
    /**
     * Do everything like the {@link TextInputCell#render() super implementation}, but do not set the {@code tabIndex}.
     */
    public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        ViewData viewData = getViewData(key);
        if (viewData != null && viewData.getCurrentValue().equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        String s = (viewData != null) ? viewData.getCurrentValue() : value;
        if (s != null) {
            sb.append(template.input(s));
        } else {
            sb.appendHtmlConstant("<input type=\"text\"></input>");
        }
    }
}
