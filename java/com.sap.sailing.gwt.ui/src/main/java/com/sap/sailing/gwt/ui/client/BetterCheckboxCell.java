package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * A checkbox cell that can be styled to a certain degree.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class BetterCheckboxCell extends CheckboxCell {
    public final static String DEFAULT_CSS_CLASS = "betterCheckboxCell";
    
    /**
     * An html string representation of a checked input box.
     */
    private final SafeHtml INPUT_CHECKED;

    /**
     * An html string representation of an unchecked input box.
     */
    private final SafeHtml INPUT_UNCHECKED;

    protected BetterCheckboxCell() {
        this(false, false);
    }

    public BetterCheckboxCell(boolean dependsOnSelection, boolean handlesSelection) {
        this(dependsOnSelection, handlesSelection, DEFAULT_CSS_CLASS);
    }
    
    public BetterCheckboxCell(boolean dependsOnSelection, boolean handlesSelection, String cssClass) {
        super(dependsOnSelection, handlesSelection);
        INPUT_CHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" checked class=\""+cssClass+"\"/>");
        INPUT_UNCHECKED = SafeHtmlUtils.fromSafeConstant("<input type=\"checkbox\" tabindex=\"-1\" class=\""+cssClass+"\"/>");
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        Boolean viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
          clearViewData(key);
          viewData = null;
        }

        if (value != null && ((viewData != null) ? viewData : value)) {
          sb.append(INPUT_CHECKED);
        } else {
          sb.append(INPUT_UNCHECKED);
        }
    }

    
}
