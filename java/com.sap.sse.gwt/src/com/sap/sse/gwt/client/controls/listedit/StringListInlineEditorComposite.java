package com.sap.sse.gwt.client.controls.listedit;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sse.gwt.client.StringMessages;

/**
 * In its expanded view, as opposed to its superclass which renders the values only as {@link Label}, this
 * class renders the values as {@link TextBox} and allows users to edit the values in place, without having
 * to remove and add them.
 */
public class StringListInlineEditorComposite extends StringListEditorComposite {
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
}
