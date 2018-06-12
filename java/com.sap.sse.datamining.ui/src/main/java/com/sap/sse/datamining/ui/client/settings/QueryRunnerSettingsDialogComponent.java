package com.sap.sse.datamining.ui.client.settings;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class QueryRunnerSettingsDialogComponent implements SettingsDialogComponent<QueryRunnerSettings> {

    private QueryRunnerSettings initialSettings;
    private StringMessages stringMessages;

    private CheckBox runAutomaticallyBox;

    public QueryRunnerSettingsDialogComponent(QueryRunnerSettings initialSettings, StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel additionalWidget = new FlowPanel();

        runAutomaticallyBox = dialog.createCheckbox(stringMessages.runAutomatically());
        runAutomaticallyBox.setTitle(stringMessages.runAutomaticallyTooltip());
        runAutomaticallyBox.setValue(initialSettings.isRunAutomatically());
        additionalWidget.add(runAutomaticallyBox);

        return additionalWidget;
    }

    @Override
    public QueryRunnerSettings getResult() {
        return new QueryRunnerSettings(runAutomaticallyBox.getValue());
    }

    @Override
    public Validator<QueryRunnerSettings> getValidator() {
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return null;
    }

}
