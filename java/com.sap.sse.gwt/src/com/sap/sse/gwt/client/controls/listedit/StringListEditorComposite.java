package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

public class StringListEditorComposite extends ListEditorComposite<String> {
    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> suggestValues) {
        this(initialValues, stringMessages, popupDialogTitle, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null);
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
        super(initialValues, new CollapsedUi(stringMessages, popupDialogTitle,
                new ExpandedUi(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox)));
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage, suggestValues));
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox));
    }

    public StringListEditorComposite(Iterable<String> initialValues, ListEditorUiStrategy<String> activeUi) {
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
            return new StringListEditorComposite(initialValues, ui);
        }
    }

    public static class ExpandedUi extends ExpandedListEditorUi<String> {

        protected final MultiWordSuggestOracle inputOracle;
        protected final String placeholderTextForAddTextbox;


        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> suggestValues) {
            this(stringMessages, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null);
        }
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
            super(stringMessages, removeImage, /*canRemoveItems*/true);
            this.placeholderTextForAddTextbox = placeholderTextForAddTextbox;
            this.inputOracle = new MultiWordSuggestOracle();
            for (String suggestValue : suggestValues) {
                inputOracle.add(suggestValue);
            }
        }
        
        @Override
        public void setContext(ListEditorComposite<String> context) {
            super.setContext(context);
            inputOracle.addAll(context.getValue());
        }

        protected SuggestBox createSuggestBox() {
            final SuggestBox result = new SuggestBox(inputOracle);
            if (placeholderTextForAddTextbox != null) {
                result.getElement().setAttribute("placeholder", placeholderTextForAddTextbox);
            }
            return result;
        }

        @Override
        protected Widget createAddWidget() {
            final SuggestBox inputBox = createSuggestBox();
            inputBox.ensureDebugId("InputSuggestBox");
            final Button addButton = new Button(getStringMessages().add());
            addButton.ensureDebugId("AddButton");
            addButton.setEnabled(false);
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addValue(inputBox.getValue());
                    inputBox.setText("");
                }
            });
            inputBox.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    addButton.setEnabled(!inputBox.getValue().isEmpty());
                }
            });
            HorizontalPanel panel = new HorizontalPanel();
            panel.add(inputBox);
            panel.add(addButton);
            return panel;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, String newValue) {
            return new Label(newValue);
        }
    }
}
