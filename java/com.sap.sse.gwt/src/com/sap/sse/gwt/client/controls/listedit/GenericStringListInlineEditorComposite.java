package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

/**
 * In its expanded view, as opposed to its superclass which renders the values only as {@link Label}, this
 * class renders the values as {@link TextBox} and allows users to edit the values in place, without having
 * to remove and add them.
 */
public abstract class GenericStringListInlineEditorComposite<ValueType> extends GenericStringListEditorComposite<ValueType> {
    public GenericStringListInlineEditorComposite(Iterable<ValueType> initialValues, StringMessages stringMessages,
            ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
        super(initialValues, new ExpandedUi<ValueType>(stringMessages, removeImage, suggestValues, textBoxSize));
    }

    public GenericStringListInlineEditorComposite(Iterable<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
        super(initialValues, activeUi);
    }

    public static class ExpandedUi<ValueType> extends GenericStringListEditorComposite.ExpandedUi<ValueType> {
        private final int textBoxSize;

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
            this(stringMessages, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null, textBoxSize);
        }
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues,
                String placeholderTextForAddTextbox, int textBoxSize) {
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
        protected Widget createValueWidget(final int rowIndex, ValueType newValue) {
            final TextBox textBox = new TextBox();
            textBox.setVisibleLength(textBoxSize);
            textBox.ensureDebugId("ValueTextBox");
            textBox.setValue(getContext().toString(newValue));
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
                    ValueType oldValue = getContext().getValue().get(rowIndex);
                    ValueType newValue = getContext().parse(event.getValue(), oldValue);
                    setValueFromValueWidget(textBox, newValue, /* fireEvents */ true);
                }
            });
            return textBox;
        }
    }
}
