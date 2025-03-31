package com.sap.sse.datamining.ui.client.settings;

import java.util.UUID;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.settings.QueryRunnerSettings.OtherChangedQueriesRunStrategy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class QueryRunnerSettingsDialogComponent implements SettingsDialogComponent<QueryRunnerSettings> {

    private QueryRunnerSettings initialSettings;
    private StringMessages stringMessages;

    private CheckBox runAutomaticallyBox;
    private RadioButton otherChangedQueriesRunStrategy_no;
    private RadioButton otherChangedQueriesRunStrategy_ask;
    private RadioButton otherChangedQueriesRunStrategy_automatically;

    public QueryRunnerSettingsDialogComponent(QueryRunnerSettings initialSettings, StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        final VerticalPanel additionalWidget = new VerticalPanel();
        runAutomaticallyBox = dialog.createCheckbox(stringMessages.runAutomatically());
        runAutomaticallyBox.setTitle(stringMessages.runAutomaticallyTooltip());
        runAutomaticallyBox.setValue(initialSettings.isRunAutomatically());
        final String OTHER_CHANGED_QUERIES_RUN_STRATEGY_RADIO_BUTTON_GROUP = "OTHER_CHANGED_QUERIES_RUN_STRATEGY_RADIO_BUTTON_GROUP-"+UUID.randomUUID();
        otherChangedQueriesRunStrategy_no = new RadioButton(OTHER_CHANGED_QUERIES_RUN_STRATEGY_RADIO_BUTTON_GROUP, stringMessages.no());
        otherChangedQueriesRunStrategy_ask = new RadioButton(OTHER_CHANGED_QUERIES_RUN_STRATEGY_RADIO_BUTTON_GROUP, stringMessages.askUser());
        otherChangedQueriesRunStrategy_automatically = new RadioButton(OTHER_CHANGED_QUERIES_RUN_STRATEGY_RADIO_BUTTON_GROUP, stringMessages.runAutomatically());
        otherChangedQueriesRunStrategy_no.setValue(initialSettings.getOtherChangedQueriesRunStrategy() == OtherChangedQueriesRunStrategy.NO);
        otherChangedQueriesRunStrategy_ask.setValue(initialSettings.getOtherChangedQueriesRunStrategy() == OtherChangedQueriesRunStrategy.ASK);
        otherChangedQueriesRunStrategy_automatically.setValue(initialSettings.getOtherChangedQueriesRunStrategy() == OtherChangedQueriesRunStrategy.AUTOMATICALLY);
        additionalWidget.add(runAutomaticallyBox);
        final HorizontalPanel otherChangedQueriesRunStrategyPanel = new HorizontalPanel();
        additionalWidget.add(otherChangedQueriesRunStrategyPanel);
        otherChangedQueriesRunStrategyPanel.add(new Label(stringMessages.runOtherChangedQueriesInBackground()));
        final VerticalPanel radioButtons = new VerticalPanel();
        otherChangedQueriesRunStrategyPanel.add(radioButtons);
        radioButtons.add(otherChangedQueriesRunStrategy_no);
        radioButtons.add(otherChangedQueriesRunStrategy_ask);
        radioButtons.add(otherChangedQueriesRunStrategy_automatically);
        return additionalWidget;
    }

    @Override
    public QueryRunnerSettings getResult() {
        return new QueryRunnerSettings(runAutomaticallyBox.getValue(),
                otherChangedQueriesRunStrategy_no.getValue() ? OtherChangedQueriesRunStrategy.NO :
                    otherChangedQueriesRunStrategy_ask.getValue() ? OtherChangedQueriesRunStrategy.ASK :
                        otherChangedQueriesRunStrategy_automatically.getValue() ? OtherChangedQueriesRunStrategy.AUTOMATICALLY :
                            // default:
                            OtherChangedQueriesRunStrategy.ASK);
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
