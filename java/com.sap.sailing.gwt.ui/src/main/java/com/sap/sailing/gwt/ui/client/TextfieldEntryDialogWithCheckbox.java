package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class TextfieldEntryDialogWithCheckbox extends DataEntryDialog<Pair<String, Boolean>> {
    private final CheckBox checkbox;
    private final TextBox entryField;

    public TextfieldEntryDialogWithCheckbox(String title, String message, String okButtonName, String cancelButtonName,
            String checkboxLabel, String initialValue, Validator<Pair<String, Boolean>> validator, DialogCallback<Pair<String, Boolean>> callback) {
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
    protected Pair<String, Boolean> getResult() {
        return new Pair<String, Boolean>(entryField.getValue(), checkbox.getValue());
    }

    @Override
    public void show() {
        super.show();
        entryField.setFocus(true);
    }
}
