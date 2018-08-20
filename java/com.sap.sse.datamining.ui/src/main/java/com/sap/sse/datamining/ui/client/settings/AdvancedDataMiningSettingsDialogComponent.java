package com.sap.sse.datamining.ui.client.settings;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class AdvancedDataMiningSettingsDialogComponent implements SettingsDialogComponent<AdvancedDataMiningSettings> {

    private AdvancedDataMiningSettings initialSettings;
    private StringMessages stringMessages;

    private CheckBox developerOptionsCheckBox;

    public AdvancedDataMiningSettingsDialogComponent(AdvancedDataMiningSettings initialSettings,
            StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel additionalWidget = new FlowPanel();

        developerOptionsCheckBox = dialog.createCheckbox(stringMessages.developerOptions());
        developerOptionsCheckBox.setValue(initialSettings.isDeveloperOptions());
        additionalWidget.add(developerOptionsCheckBox);

        return additionalWidget;
    }

    @Override
    public AdvancedDataMiningSettings getResult() {
        return new AdvancedDataMiningSettings(developerOptionsCheckBox.getValue());
    }

    @Override
    public Validator<AdvancedDataMiningSettings> getValidator() {
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }

}
