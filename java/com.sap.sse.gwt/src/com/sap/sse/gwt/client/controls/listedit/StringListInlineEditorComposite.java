package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

public class StringListInlineEditorComposite extends StringListEditorComposite {
    public StringListInlineEditorComposite(List<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
        super(initialValues, new CollapsedUi(stringMessages, popupDialogTitle,
                new ExpandedUi(stringMessages, removeImage, suggestValues, textBoxSize)));
    }

    public StringListInlineEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
        super(initialValues, new ExpandedUi(stringMessages, removeImage, suggestValues, textBoxSize));
    }

    public StringListInlineEditorComposite(Iterable<String> initialValues, ListEditorUiStrategy<String> activeUi) {
        super(initialValues, activeUi);
    }

    public static class CollapsedUi extends StringListEditorComposite.CollapsedUi {
        public CollapsedUi(StringMessages stringMessages, String dialogTitle, ExpandedListEditorUi<String> expandedUi) {
            super(stringMessages, dialogTitle, expandedUi);
        }

        @Override
        protected ListEditorComposite<String> createExpandedUi(Iterable<String> initialValues, ExpandedListEditorUi<String> ui) {
            return new StringListInlineEditorComposite(initialValues, ui);
        }
    }

    public static class ExpandedUi extends ExpandedListEditorUi<String> {
        private final MultiWordSuggestOracle inputOracle;
        private final int textBoxSize;

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
            super(stringMessages, removeImage, /*canRemoveItems*/true);
            this.inputOracle = new MultiWordSuggestOracle();
            this.textBoxSize = textBoxSize;
            inputOracle.addAll(suggestValues);
        }
        
        @Override
        public void setContext(ListEditorComposite<String> context) {
            super.setContext(context);
            inputOracle.addAll(context.getValue());
        }

        @Override
        protected Widget createAddWidget() {
            final SuggestBox inputBox = new SuggestBox(inputOracle);
            
            InputElement inputElement = inputBox.getElement().cast();
            inputElement.setSize(textBoxSize);

            final Button addButton = new Button(getStringMessages().add());
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
        protected Widget createValueWidget(final int rowIndex, String newValue) {
            final TextBox textBox = new TextBox();
            textBox.setVisibleLength(textBoxSize);
            textBox.ensureDebugId("ValueTextBox");
            textBox.setValue(newValue);

            textBox.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    textBox.setFocus(false);
                    // this ensures that the value is copied into the TextBox.getValue() result and a ChangeEvent is fired
                    textBox.setFocus(true);
                }
            });
            
            textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    String newValue = event.getValue();
                    setValueFromValueWidget(textBox, newValue, true);
                }
            });
            return textBox;
        }
    }
}
