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
        dataProvider.addOrUpdateQuery(suggestBoxUi.getValue(), dataProvider.getCurrentQuery());
    }

    @UiHandler("loadQueryButtonUi")
    void onLoadClick(ClickEvent e) {
        dataProvider.applyQuery(suggestBoxUi.getValue());
    }

    @UiHandler("removeQueryButtonUi")
    void onRemoveClick(ClickEvent e) {
        if (dataProvider.removeQuery(suggestBoxUi.getValue())) {
            suggestBoxUi.setValue("");
        }
    }

    public void updateOracle(Collection<String> collection) {
        oracle.clear();
        oracle.addAll(collection);
        oracle.setDefaultSuggestionsFromText(collection);
    }
}
