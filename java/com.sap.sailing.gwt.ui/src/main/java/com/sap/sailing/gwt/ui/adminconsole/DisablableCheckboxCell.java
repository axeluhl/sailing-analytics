package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DisablableCheckboxCell extends CheckboxCell {
    public static interface IsEnabled {
        boolean isEnabled();
    }
    
    private final IsEnabled isEnabled;
    
    public DisablableCheckboxCell(IsEnabled isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Boolean value, SafeHtmlBuilder sb) {
        if (isEnabled.isEnabled()) {
            super.render(context, value, sb);
        } else {
            sb.appendEscaped(value ? "x" : "");
        }
    }

}
