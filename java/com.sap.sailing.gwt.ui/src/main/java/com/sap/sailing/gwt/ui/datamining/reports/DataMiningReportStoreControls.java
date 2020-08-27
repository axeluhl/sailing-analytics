package com.sap.sailing.gwt.ui.datamining.reports;

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
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;

/**
 * UI Panel with the three buttons to load, store and remove and the suggest box to select a stored data mining query by
 * name.
 */
public class DataMiningReportStoreControls extends Composite {

    private static PersistDataMiningReportStoreControlsUiBinder uiBinder = GWT.create(PersistDataMiningReportStoreControlsUiBinder.class);

    interface PersistDataMiningReportStoreControlsUiBinder extends UiBinder<Widget, DataMiningReportStoreControls> {
    }

    @UiField
    Button saveQueryButtonUi;

    @UiField
    Button loadQueryButtonUi;

    @UiField
    Button removeQueryButtonUi;

    @UiField(provided = true)
    SuggestBox suggestBoxUi;


    private final StoredDataMiningReportsProvider reportsProvider;
    private final MultiWordSuggestOracle oracle;


    public DataMiningReportStoreControls(StoredDataMiningReportsProvider reportsProvider) {
        this.reportsProvider = reportsProvider;
        oracle = new MultiWordSuggestOracle();
        suggestBoxUi = new SuggestBox(oracle, new TextBox(), new DefaultSuggestionDisplay() {
            @Override
            public void hideSuggestions() {
                updateSaveLoadButtons();
                super.hideSuggestions();
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        saveQueryButtonUi.setText(StringMessages.INSTANCE.save());
        loadQueryButtonUi.setText(StringMessages.INSTANCE.load());
        removeQueryButtonUi.setText(StringMessages.INSTANCE.remove());
        suggestBoxUi.getValueBox().getElement().setPropertyString("placeholder",
                StringMessages.INSTANCE.dataMiningStoredReportPlaceholder());
        suggestBoxUi.getValueBox().addClickHandler(e -> suggestBoxUi.showSuggestionList());
        suggestBoxUi.getValueBox().addKeyUpHandler(e -> updateSaveLoadButtons());
        suggestBoxUi.getValueBox().addBlurHandler(e -> updateSaveLoadButtons());
    }

    private void updateSaveLoadButtons() {
        String text = suggestBoxUi.getValueBox().getText();
        saveQueryButtonUi.setEnabled(text != null && !"".equals(text.trim()));
        loadQueryButtonUi.setEnabled(reportsProvider.containsQueryName(text));
        removeQueryButtonUi.setEnabled(reportsProvider.containsQueryName(text));
    }

    @UiHandler("saveQueryButtonUi")
    void onSaveClick(ClickEvent e) {
        String value = suggestBoxUi.getValue().trim();
        if (reportsProvider.addOrUpdateQuery(value, reportsProvider.getCurrentQuery())) {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredQueryUpdateSuccessful(value),
                    NotificationType.SUCCESS);
        } else {
            Notification.notify(
                    StringMessages.INSTANCE.dataMiningStoredQueryCreationSuccessful(value),
                    NotificationType.SUCCESS);
        }
    }

    @UiHandler("loadQueryButtonUi")
    void onLoadClick(ClickEvent e) {
        if (reportsProvider.applyQuery(suggestBoxUi.getValue())) {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredQueryLoadedSuccessful(suggestBoxUi.getValue()),
                    NotificationType.SUCCESS);
        } else {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredQueryLoadedFailed(suggestBoxUi.getValue()),
                    NotificationType.ERROR);
        }
    }

    @UiHandler("removeQueryButtonUi")
    void onRemoveClick(ClickEvent e) {
        if (reportsProvider.removeQuery(suggestBoxUi.getValue())) {
            suggestBoxUi.setValue("");
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredQueryRemovedSuccessful(suggestBoxUi.getValue()),
                    NotificationType.SUCCESS);
        } else {
            Notification.notify(StringMessages.INSTANCE.dataMiningStoredQueryRemovedFailed(suggestBoxUi.getValue()),
                    NotificationType.ERROR);
        }
    }

    /** Updates the oracle of the suggest box with the names of the stored queries. */
    public void updateOracle(Collection<String> collection) {
        oracle.clear();
        oracle.addAll(collection);
        oracle.setDefaultSuggestionsFromText(collection);

        loadQueryButtonUi.setEnabled(!collection.isEmpty());
        removeQueryButtonUi.setEnabled(!collection.isEmpty());
        updateSaveLoadButtons();
    }
}
