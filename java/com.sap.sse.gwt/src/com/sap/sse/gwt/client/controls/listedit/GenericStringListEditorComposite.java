package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.StringMessages;

/**
 * A list editor that renders values of type {@code ValueType} as {@link String}s and lets the user edit them
 * as strings. For this to work, implementing subclasses need to specify how strings are parsed to objects
 * of type {@code ValueType} and vice versa.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (d043530)
 *
 * @param <ValueType>
 */
public abstract class GenericStringListEditorComposite<ValueType> extends ListEditorComposite<ValueType> {
    protected GenericStringListEditorComposite(Iterable<ValueType> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues) {
        super(initialValues, new ExpandedUi<ValueType>(stringMessages, removeImage, suggestValues));
    }

    protected GenericStringListEditorComposite(Iterable<ValueType> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
        super(initialValues, new ExpandedUi<ValueType>(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox));
    }

    protected GenericStringListEditorComposite(Iterable<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
        super(initialValues, activeUi);
    }
    
    /**
     * Parses a new value provided as string {@code s} into a new object of type {@code ValueType}
     */
    abstract protected ValueType parse(String s);

    /**
     * Tries to update an existing value {@code valueToUpdate} from the string specification {@code s}.
     * If an in-place update works, {@code valueToUpdate} is returned, otherwise a new {@code ValueType}
     * object is created using {@link #parse(String)}.
     */
    abstract protected ValueType parse(String s, ValueType valueToUpdate);
    
    /**
     * Renders a {@code value} as {@link String} for representation in labels and text input fields. It
     * must be symmetric with {@link #parse(String, Object)} such that if in-place updates are generally supported
     * by the implementation then {@code toString(parse(s, v)).equals(s)} must be satisfied.
     */
    abstract protected String toString(ValueType value);

    public abstract static class CollapsedUi<ValueType> extends CollapsedListEditorUi<ValueType> {

        public CollapsedUi(StringMessages stringMessages, String dialogTitle, ExpandedListEditorUi<ValueType> expandedUi) {
            super(stringMessages, dialogTitle, expandedUi);
        }

        @Override
        protected String getCollapsedValueText(Iterable<ValueType> value) {
            StringBuilder valuesText = new StringBuilder();
            for (int i = 0; i < Util.size(value); i++) {
                if (i > 0) {
                    valuesText.append(',');
                }
                valuesText.append(Util.get(value, i));
            }
            String condensedValue = valuesText.toString();
            return condensedValue;
        }
    }

    public static class ExpandedUi<ValueType> extends ExpandedListEditorUi<ValueType> {
        protected final MultiWordSuggestOracle inputOracle;
        protected final String placeholderTextForAddTextbox;
        protected final Integer inputBoxSize;
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> suggestValues) {
            this(stringMessages, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null);
        }
        
        /**
         * @param suggestValues must not be null but may be empty
         * @param placeholderTextForAddTextbox may be null
         */
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> suggestValues,
                String placeholderTextForAddTextbox) {
            this(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox, /* inputBoxSize */ null);
            
        }
        /**
         * @param inputBoxSize The size of the input box in EM Unit.
         */
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, Iterable<String> suggestValues,
                String placeholderTextForAddTextbox, Integer inputBoxSize) {
            super(stringMessages, removeImage, /* canRemoveItems */true);
            this.placeholderTextForAddTextbox = placeholderTextForAddTextbox;
            this.inputOracle = new MultiWordSuggestOracle();
            for (String suggestValue : suggestValues) {
                inputOracle.add(suggestValue);
            }
            List<String> defaultSuggestions = new ArrayList<>();
            Util.addAll(suggestValues, defaultSuggestions);
            this.inputOracle.setDefaultSuggestionsFromText(defaultSuggestions);
            this.inputBoxSize = inputBoxSize;
        }
               
        protected GenericStringListEditorComposite<ValueType> getContext() {
            return (GenericStringListEditorComposite<ValueType>) context;
        }
        
        @Override
        public void setContext(ListEditorComposite<ValueType> context) {
            super.setContext(context);
            for (final ValueType value : context.getValue()) {
                inputOracle.add(getContext().toString(value));
            }
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
            if (inputBoxSize != null) {
                inputBox.setWidth(Integer.toString(inputBoxSize) + Unit.EM);
            }
            final Button addButton = new Button(getStringMessages().add());
            addButton.ensureDebugId("AddButton");
            addButton.setEnabled(false);
            addButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addValue(getContext().parse(inputBox.getValue()));
                    inputBox.setText("");
                }
            });
            inputBox.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    addButton.setEnabled(!inputBox.getValue().isEmpty());
                }
            });
            // Add addition handler for selection because cannot use one handler for changed SuggestBox value by reason
            // of gwt bug https://github.com/gwtproject/gwt/issues/1642
            inputBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
                @Override
                public void onSelection(SelectionEvent<Suggestion> event) {
                    addButton.setEnabled(!inputBox.getValue().isEmpty());
                }
            });
            HorizontalPanel panel = new HorizontalPanel();
            panel.add(inputBox);
            panel.add(addButton);
            return panel;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, ValueType newValue) {
            return new Label(getContext().toString(newValue));
        }
    }
}
