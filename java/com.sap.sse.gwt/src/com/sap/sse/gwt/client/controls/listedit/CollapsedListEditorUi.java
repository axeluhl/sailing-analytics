package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Base class for "collapsed" list editing UIs for use in a {@link ListEditorComposite}. An editing strategy of this
 * type shows the list of values in a single character string, together with an "Edit" button which pops up a dialog
 * that contains an "expanded" editor of type {@link ExpandedListEditorUi}.
 * <p>
 * 
 * To implement a "collapsed" editor, subclasses have to provide the {@link String} representation of a value list in
 * method {@link #getCollapsedValueText(List)} and need to provide the widget used to edit the list. This widget will be
 * displayed in the pop-up dialog, together with a "Save" and a "Cancel" button. It is a common pattern to use a
 * {@link ListEditorComposite} with the respective {@link ExpandedListEditorUi} strategy that will be passed through
 * from this class's constructor to the {@link #createExpandedUi(List, ExpandedListEditorUi)} method.<p>
 * 
 * Implementing subclasses may choose to override the {@link #onRowAdded()} and/or the {@link #onRowRemoved(int)} method(s)
 * to be notified of changes to the list.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (D043530)
 * 
 * @param <ValueType>
 */
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

    protected abstract String getCollapsedValueText(Iterable<ValueType> value);

    protected abstract ListEditorComposite<ValueType> createExpandedUi(Iterable<ValueType> initialValues,
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

        collapsedEditButton = new Button(getStringMessages().edit());
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

    private class PopupEditDialog extends DataEntryDialog<List<ValueType>> {

        private ListEditorComposite<ValueType> expandedComposite;
        
        public PopupEditDialog(Iterable<ValueType> initialValues, ExpandedListEditorUi<ValueType> ui, String dialogTitle,
                DataEntryDialog.DialogCallback<List<ValueType>> callback) {
            super(dialogTitle, "", getStringMessages().save(), getStringMessages().cancel(), null, callback);
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
    public void onRowRemoved(int rowIndex) {
    }

    public void setEnabled(final boolean enabled) {
        collapsedEditButton.setEnabled(enabled);
    }
}

