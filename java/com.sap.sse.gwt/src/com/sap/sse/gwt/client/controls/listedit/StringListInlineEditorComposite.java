package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
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

    public static class ExpandedUi extends StringListEditorComposite.ExpandedUi {
        private final int textBoxSize;

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
            this(stringMessages, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null, textBoxSize);
        }
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, String placeholderTextForAddTextbox, int textBoxSize) {
            super(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox);
            this.textBoxSize = textBoxSize;
        }
        
        @Override
        protected SuggestBox createSuggestBox() {
            SuggestBox result = super.createSuggestBox();
            InputElement inputElement = result.getElement().cast();
            inputElement.setSize(textBoxSize);
            return result;
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
