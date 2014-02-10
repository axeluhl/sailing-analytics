package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.ui.DataEntryDialog;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public abstract class CollapsedListEditorUi<ValueType> extends ListEditorUi<ValueType> {
    private static final int collapsedTitleMaxLength = 20;

    private final String popupDialogTitle;
    private final ExpandedListEditorUi<ValueType> expandedUi;
    private TextBox collapsedValuesBox;
    private Button collapsedEditButton;

    public CollapsedListEditorUi(StringMessages stringMessages, String popupDialogTitle,
            ExpandedListEditorUi<ValueType> expandedUi) {
        super(stringMessages);
        this.popupDialogTitle = popupDialogTitle;
        this.expandedUi = expandedUi;
    }

    protected abstract String getCollapsedValueText(List<ValueType> value);

    protected abstract ListEditorComposite<ValueType> createExpandedUi(List<ValueType> initialValues,
            ExpandedListEditorUi<ValueType> ui);

    @Override
    public void setContext(ListEditorComposite<ValueType> context) {
        super.setContext(context);
    }

    @Override
    public Widget initWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        collapsedValuesBox = new TextBox();
        collapsedValuesBox.setReadOnly(true);

        collapsedEditButton = new Button(stringMessages.edit());
        collapsedEditButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new PopupEditDialog(context.getValue(), expandedUi, popupDialogTitle,
                        new DialogCallback<List<ValueType>>() {

                            @Override
                            public void ok(List<ValueType> editedObject) {
                                context.setValue(editedObject);
                            }

                            @Override
                            public void cancel() {

                            }
                        }).show();
            }
        });
        panel.add(collapsedValuesBox);
        panel.add(collapsedEditButton);
        return panel;
    }

    @Override
    public void refresh() {
        String text = getCollapsedValueText(context.getValue());
        String shortText = text;
        if (shortText.length() > collapsedTitleMaxLength) {
            shortText = shortText.substring(0, collapsedTitleMaxLength) + "...";
        }
        collapsedValuesBox.setText(shortText);
        collapsedValuesBox.setTitle(text);
    }

    @Override
    public boolean isCollapsed() {
        return true;
    }

    private class PopupEditDialog extends DataEntryDialog<List<ValueType>> {

        private ListEditorComposite<ValueType> expandedComposite;
        
        public PopupEditDialog(List<ValueType> initialValues, ExpandedListEditorUi<ValueType> ui, String dialogTitle,
                DataEntryDialog.DialogCallback<List<ValueType>> callback) {
            super(dialogTitle, "", stringMessages.save(), stringMessages.cancel(), null, callback);
            expandedComposite = createExpandedUi(initialValues, ui);
        }

        @Override
        protected Widget getAdditionalWidget() {
            return expandedComposite;
        }

        @Override
        protected List<ValueType> getResult() {
            return expandedComposite.getValue();
        }
    }

    @Override
    public void onRowAdded() {
    }

    @Override
    public void onRowRemoved() {
    }
}

