package com.sap.sailing.gwt.ui.datamining.settings;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

public class DataMiningSettingsDialogComponent implements SettingsDialogComponent<DataMiningSettings> {
    
    private DataMiningSettings initialSettings;
    private StringMessages stringMessages;
    
    private CheckBox runAutimaticallyBox;

    public DataMiningSettingsDialogComponent(DataMiningSettings initialSettings, StringMessages stringMessages) {
        super();
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel additionalWidget = new FlowPanel();
        
        runAutimaticallyBox = dialog.createCheckbox(stringMessages.runAutomatically());
        runAutimaticallyBox.setValue(initialSettings.isRunAutomatically());
        additionalWidget.add(runAutimaticallyBox);
        
        return additionalWidget;
    }

    @Override
    public DataMiningSettings getResult() {
        return new DataMiningSettings(runAutimaticallyBox.getValue());
    }

    @Override
    public Validator<DataMiningSettings> getValidator() {
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }

}
