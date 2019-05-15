package com.sap.sse.gwt.client.controls.listedit;

import com.google.gwt.resources.client.ImageResource;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.StringMessages;

/**
 * A simple string list editor for {@link String} values with trivial implementations for parsing and
 * serializing to strings.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (d043530)
 *
 */
public class StringListEditorComposite extends GenericStringListEditorComposite<String> {
    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> suggestValues) {
        this(initialValues, stringMessages, popupDialogTitle, removeImage, suggestValues, /* placeholderTextForAddTextbox */ null);
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            String popupDialogTitle, ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
        super(initialValues, new CollapsedUi(stringMessages, popupDialogTitle,
                new ExpandedUi<String>(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox)));
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues) {
        super(initialValues, new ExpandedUi<String>(stringMessages, removeImage, suggestValues));
    }

    public StringListEditorComposite(Iterable<String> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues, String placeholderTextForAddTextbox) {
        super(initialValues, new ExpandedUi<String>(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox));
    }

    public StringListEditorComposite(Iterable<String> initialValues, ListEditorUiStrategy<String> activeUi) {
        super(initialValues, activeUi);
    }

    public static class CollapsedUi extends GenericStringListEditorComposite.CollapsedUi<String> {
        public CollapsedUi(StringMessages stringMessages, String dialogTitle, ExpandedListEditorUi<String> expandedUi) {
            super(stringMessages, dialogTitle, expandedUi);
        }

        @Override
        protected ListEditorComposite<String> createExpandedUi(Iterable<String> initialValues, ExpandedListEditorUi<String> ui) {
            return new StringListEditorComposite(initialValues, ui);
        }
    }

    @Override
    protected String parse(String s) {
        return s;
    }

    @Override
    protected String parse(String s, String valueToUpdate) {
        return Util.equalsWithNull(valueToUpdate, s) ? valueToUpdate : s; // try to preserve identity where possible
    }

    @Override
    protected String toString(String value) {
        return value;
    }

    public void setEnabled(final boolean enabled) {
        // is always of type CollapsedUi (see constructor)
        ((CollapsedUi) activeUi).setEnabled(enabled);
    }
}
