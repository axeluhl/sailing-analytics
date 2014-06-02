package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.UtilNew;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class TextfieldEntryDialogWithCheckbox extends DataEntryDialog<UtilNew.Pair<String, Boolean>> {
    private final CheckBox checkbox;
    private final TextBox entryField;

    public TextfieldEntryDialogWithCheckbox(String title, String message, String okButtonName, String cancelButtonName,
            String checkboxLabel, String initialValue, Validator<UtilNew.Pair<String, Boolean>> validator, DialogCallback<UtilNew.Pair<String, Boolean>> callback) {
        super(title, message, okButtonName, cancelButtonName, validator, callback);
        checkbox = createCheckbox(checkboxLabel);
        entryField = createTextBox(initialValue);
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        panel.add(entryField);
        panel.add(checkbox);
        return panel;
    }
    
    @Override
    protected UtilNew.Pair<String, Boolean> getResult() {
        return new UtilNew.Pair<String, Boolean>(entryField.getValue(), checkbox.getValue());
    }

    @Override
    public void show() {
        super.show();
        entryField.setFocus(true);
    }
}
