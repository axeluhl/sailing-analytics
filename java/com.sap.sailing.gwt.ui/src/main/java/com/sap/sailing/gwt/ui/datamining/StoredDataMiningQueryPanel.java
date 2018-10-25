package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

public class StoredDataMiningQueryPanel extends Composite {

    private static PersistDataMiningQueryPanelUiBinder uiBinder = GWT.create(PersistDataMiningQueryPanelUiBinder.class);

    interface PersistDataMiningQueryPanelUiBinder extends UiBinder<Widget, StoredDataMiningQueryPanel> {
    }

    @UiField
    Button saveQueryButtonUi;

    @UiField
    Button loadQueryButtonUi;

    @UiField
    Button removeQueryButtonUi;

    @UiField(provided = true)
    SuggestBox suggestBoxUi;

    private final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();

    private StoredDataMiningQueryDataProvider dataProvider;

    public StoredDataMiningQueryPanel() {
        suggestBoxUi = new SuggestBox(oracle);
        initWidget(uiBinder.createAndBindUi(this));
        saveQueryButtonUi.setText(StringMessages.INSTANCE.save());
        loadQueryButtonUi.setText(StringMessages.INSTANCE.load());
        removeQueryButtonUi.setText(StringMessages.INSTANCE.remove());
        suggestBoxUi.getValueBox().getElement().setPropertyString("placeholder",
                StringMessages.INSTANCE.dataMiningStoredQueryPlaceholder());
        suggestBoxUi.getValueBox().addClickHandler(e -> suggestBoxUi.showSuggestionList());
    }

    public StoredDataMiningQueryPanel(StoredDataMiningQueryDataProvider dataProvider) {
        this();
        this.dataProvider = dataProvider;
        dataProvider.setUiPanel(this);
    }

    @UiHandler("saveQueryButtonUi")
    void onSaveClick(ClickEvent e) {
        if (dataProvider.addOrUpdateQuery(suggestBoxUi.getValue(), dataProvider.getCurrentQuery())) {
            Notification.notify("Updated stored query '" + suggestBoxUi.getValue() + "'.", NotificationType.SUCCESS);
        } else {
            Notification.notify("Created new stored query '" + suggestBoxUi.getValue() + "'.",
                    NotificationType.SUCCESS);
        }
    }

    @UiHandler("loadQueryButtonUi")
    void onLoadClick(ClickEvent e) {
        if (dataProvider.applyQuery(suggestBoxUi.getValue())) {
            Notification.notify("Loaded stored query '" + suggestBoxUi.getValue() + "'.", NotificationType.SUCCESS);
        } else {
            Notification.notify("Could not find stored query '" + suggestBoxUi.getValue() + "'.",
                    NotificationType.ERROR);
        }
    }

    @UiHandler("removeQueryButtonUi")
    void onRemoveClick(ClickEvent e) {
        if (dataProvider.removeQuery(suggestBoxUi.getValue())) {
            suggestBoxUi.setValue("");
            Notification.notify("Removed stored query '" + suggestBoxUi.getValue() + "'.", NotificationType.SUCCESS);
        } else {
            Notification.notify("Could not remove stored query '" + suggestBoxUi.getValue() + "'.",
                    NotificationType.ERROR);
        }
    }

    public void updateOracle(Collection<String> collection) {
        oracle.clear();
        oracle.addAll(collection);
        oracle.setDefaultSuggestionsFromText(collection);
    }
}
