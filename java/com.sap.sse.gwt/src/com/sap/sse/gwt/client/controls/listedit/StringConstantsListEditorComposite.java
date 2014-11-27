package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.StringMessages;

public class StringConstantsListEditorComposite extends ListEditorComposite<String> {
    public StringConstantsListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> availableConstantValuesToAdd) {
        this(initialValues, stringMessages, popupDialogTitle, removeImage, availableConstantValuesToAdd, /* placeholderTextForAddTextbox */ null);
    }

    public StringConstantsListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> availableConstantValuesToAdd, String placeholderTextForAddTextbox) {
        super(initialValues, new CollapsedUi(stringMessages, popupDialogTitle,
                new ExpandedUi(stringMessages, removeImage, availableConstantValuesToAdd, placeholderTextForAddTextbox)));
    }

    public StringConstantsListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> availableConstantValuesToAdd) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage, availableConstantValuesToAdd));
    }

    public StringConstantsListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> availableConstantValuesToAdd, String placeholderTextForAddTextbox) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage, availableConstantValuesToAdd, placeholderTextForAddTextbox));
    }

    public StringConstantsListEditorComposite(Iterable<String> initialValues, ListEditorUiStrategy<String> activeUi) {
        super(initialValues, activeUi);
    }

    public static class CollapsedUi extends CollapsedListEditorUi<String> {

        public CollapsedUi(StringMessages stringMessages, String dialogTitle, ExpandedListEditorUi<String> expandedUi) {
            super(stringMessages, dialogTitle, expandedUi);
        }

        @Override
        protected String getCollapsedValueText(List<String> value) {
            StringBuilder valuesText = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                if (i > 0) {
                    valuesText.append(',');
                }
                valuesText.append(value.get(i));
            }
            String condensedValue = valuesText.toString();
            return condensedValue;
        }

        @Override
        protected ListEditorComposite<String> createExpandedUi(Iterable<String> initialValues, ExpandedListEditorUi<String> ui) {
            return new StringConstantsListEditorComposite(initialValues, ui);
        }
    }

    public static class ExpandedUi extends ExpandedListEditorUi<String> {

        @Override
        public void setContext(final ListEditorComposite<String> context) {
            super.setContext(context);
            context.addValueChangeHandler(new ValueChangeHandler<Iterable<String>>() {
                @Override
                public void onValueChange(ValueChangeEvent<Iterable<String>> event) {
                    if(selectionBox != null) {
                        // add a removed value item back to the list of available items
                        List<String> diffList = new ArrayList<>();
                        Util.addAll(availableConstantValuesToAdd, diffList);
                        diffList.removeAll(context.getValue());
                        updateSelectionListBox(selectionBox, diffList);
                    }
                }
            });
        }

        protected final String placeholderTextForAddListbox;
        protected final Iterable<String> availableConstantValuesToAdd;
        private ListBox selectionBox;
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> availableConstantValuesToAdd) {
            this(stringMessages, removeImage, availableConstantValuesToAdd, /* placeholderTextForAddTextbox */ null);
        }
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> availableConstantValuesToAdd, String placeholderTextForAddListbox) {
            super(stringMessages, removeImage, /*canRemoveItems*/true);
            this.availableConstantValuesToAdd = availableConstantValuesToAdd;
            this.placeholderTextForAddListbox = placeholderTextForAddListbox;
            this.selectionBox = null;
        }

        private void updateSelectionListBox(ListBox listBox, Iterable<String> values) {
            listBox.clear();
            if(placeholderTextForAddListbox != null) {
                listBox.addItem(placeholderTextForAddListbox);
            } else {
                listBox.addItem("Please select");
            }
            for (String value : values) {
                listBox.addItem(value);
            }
        }
        
        protected ListBox createListBox() {
            ListBox result = new ListBox(false);
            updateSelectionListBox(result, availableConstantValuesToAdd);
            return result;
        }
        
        @Override
        protected Widget createAddWidget() {
            selectionBox = createListBox();
            selectionBox.ensureDebugId("SelectionListBox");
            final Button addButton = new Button(getStringMessages().add());
            addButton.ensureDebugId("AddButton");
            addButton.setEnabled(false);
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    int selIndex = selectionBox.getSelectedIndex();
                    String seletedItemText = selectionBox.getItemText(selIndex);
                    addValue(seletedItemText);
                    selectionBox.setSelectedIndex(0);
                    addButton.setEnabled(false);
                }
            });
            
            selectionBox.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    int selIndex = selectionBox.getSelectedIndex();
                    addButton.setEnabled(selIndex > 0);
                }
            });
            HorizontalPanel panel = new HorizontalPanel();
            panel.add(selectionBox);
            panel.add(addButton);
            return panel;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, String newValue) {
            return new Label(newValue);
        }
    }
}
