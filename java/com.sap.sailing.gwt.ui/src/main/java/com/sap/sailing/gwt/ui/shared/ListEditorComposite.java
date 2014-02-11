package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.ui.DataEntryDialog;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class ListEditorComposite<ValueType> extends Composite implements HasValue<List<ValueType>>,
        HasValueChangeHandlers<List<ValueType>> {

    public interface ListEditorUiStrategy<ValueType> {
        Widget initWidget();

        void refresh();

        void setContext(ListEditorComposite<ValueType> context);

        boolean isCollapsed();
    }

    public abstract static class ListEditorUi<ValueType> implements ListEditorUiStrategy<ValueType> {
        protected final StringMessages stringMessages;
        protected ListEditorComposite<ValueType> context;

        public ListEditorUi(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public void setContext(ListEditorComposite<ValueType> context) {
            this.context = context;
        }
    }

    public abstract static class CollapsedListEditorUi<ValueType> extends ListEditorUi<ValueType> {
        private static final int collapsedTitleMaxLength = 20;

        private final String popupDialogTitle;
        private final ExpandedListEditorUi<ValueType> expandedUi;

        public CollapsedListEditorUi(StringMessages stringMessages, String popupDialogTitle,
                ExpandedListEditorUi<ValueType> expandedUi) {
            super(stringMessages);
            this.popupDialogTitle = popupDialogTitle;
            this.expandedUi = expandedUi;
        }

        private TextBox collapsedValuesBox;
        private Button collapsedEditButton;
        
        @Override
        public void setContext(ListEditorComposite<ValueType> context) {
            super.setContext(context);
        }

        @Override
        public Widget initWidget() {
            HorizontalPanel panel = new HorizontalPanel();
            panel.ensureDebugId("CollapsedListEditorWidget");
            
            collapsedValuesBox = new TextBox();
            collapsedValuesBox.ensureDebugId("CollapsedValuesTextBox");
            collapsedValuesBox.setReadOnly(true);

            collapsedEditButton = new Button(stringMessages.edit());
            collapsedEditButton.ensureDebugId("CollapsedEditButton");
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

        protected abstract String getCollapsedValueText(List<ValueType> value);

        protected abstract ListEditorComposite<ValueType> createExpandedUi(List<ValueType> initialValues,
                ExpandedListEditorUi<ValueType> ui);

        private class PopupEditDialog extends DataEntryDialog<List<ValueType>> {

            private ListEditorComposite<ValueType> expandedComposite;

            public PopupEditDialog(List<ValueType> initialValues, ExpandedListEditorUi<ValueType> ui, String dialogTitle,
                    DataEntryDialog.DialogCallback<List<ValueType>> callback) {
                super(dialogTitle, "", stringMessages.save(), stringMessages.cancel(), null, callback);
                expandedComposite = createExpandedUi(initialValues, ui);
                expandedComposite.ensureDebugId("ExpandedListEditorComposite");
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
    }

    public abstract static class ExpandedListEditorUi<ValueType> extends ListEditorUi<ValueType> {

        private final ImageResource removeImage;

        private Grid expandedValuesGrid;

        public ExpandedListEditorUi(StringMessages stringMessages, ImageResource removeImage) {
            super(stringMessages);
            this.removeImage = removeImage;
        }

        @Override
        public Widget initWidget() {
            VerticalPanel panel = new VerticalPanel();
            
            Widget addWidget = createAddWidget();
            addWidget.ensureDebugId("AddWidget");
            panel.add(addWidget);
            
            expandedValuesGrid = new Grid(0, 2);
            expandedValuesGrid.ensureDebugId("ExpandedValuesGrid");
            panel.add(expandedValuesGrid);

            return panel;
        }

        @Override
        public void refresh() {
            expandedValuesGrid.clear();
            for (ValueType value : context.getValue()) {
                addRow(value);
            }
        }

        @Override
        public boolean isCollapsed() {
            return false;
        }

        private void addRow(ValueType newValue) {
            int rowIndex = expandedValuesGrid.insertRow(expandedValuesGrid.getRowCount());

            PushButton removeButton = new PushButton(new Image(removeImage));
            removeButton.ensureDebugId("RemoveButton");
            removeButton.setTitle(stringMessages.remove());
            removeButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    int rowToRemove = expandedValuesGrid.getCellForEvent(event).getRowIndex();
                    expandedValuesGrid.removeRow(rowToRemove);
                    context.getValue().remove(rowToRemove);
                    context.onChange();
                }
            });
            
            Widget widget = createValueWidget(newValue);
            widget.ensureDebugId("ValueWidget");
            
            expandedValuesGrid.setWidget(rowIndex, 0, widget);
            expandedValuesGrid.setWidget(rowIndex, 1, removeButton);
        }

        protected void addValue(ValueType newValue) {
            context.getValue().add(newValue);
            addRow(newValue);
            context.onChange();
        }

        protected abstract Widget createAddWidget();

        protected abstract Widget createValueWidget(ValueType newValue);
    }

    private final ListEditorUiStrategy<ValueType> activeUi;

    private List<ValueType> values;

    protected ListEditorComposite(List<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
        this.values = new ArrayList<ValueType>(initialValues);
        this.activeUi = activeUi;
        this.activeUi.setContext(this);

        initWidget(activeUi.initWidget());
        activeUi.refresh();
    }

    @Override
    public List<ValueType> getValue() {
        return values;
    }

    @Override
    public void setValue(List<ValueType> newValues) {
        setValue(newValues, true);
    }

    @Override
    public void setValue(List<ValueType> newValues, boolean fireEvents) {
        this.values = newValues;
        activeUi.refresh();
        if (fireEvents) {
            onChange();
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ValueType>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void onChange() {
        ValueChangeEvent.fire(this, getValue());
    }

    public boolean isCollapsed() {
        return activeUi.isCollapsed();
    }

}
