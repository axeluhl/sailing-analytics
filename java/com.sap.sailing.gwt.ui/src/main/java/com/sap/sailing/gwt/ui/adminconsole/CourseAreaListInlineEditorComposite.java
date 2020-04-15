package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.controls.listedit.ExpandedListEditorUi;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.GenericStringListInlineEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.ListEditorUiStrategy;

public class CourseAreaListInlineEditorComposite extends GenericStringListInlineEditorComposite<CourseAreaDTO> {

    public CourseAreaListInlineEditorComposite(Iterable<CourseAreaDTO> initialValues,
            ListEditorUiStrategy<CourseAreaDTO> activeUi) {
        super(initialValues, activeUi);
    }

    public CourseAreaListInlineEditorComposite(Iterable<CourseAreaDTO> initialValues, StringMessages stringMessages,
            ImageResource removeImage, Iterable<String> suggestValues, int textBoxSize) {
        super(initialValues, stringMessages, removeImage, suggestValues, textBoxSize);
    }

    public static class CollapsedUi extends GenericStringListEditorComposite.CollapsedUi<CourseAreaDTO> {
        public CollapsedUi(StringMessages stringMessages, String dialogTitle, ExpandedListEditorUi<CourseAreaDTO> expandedUi) {
            super(stringMessages, dialogTitle, expandedUi);
        }

        @Override
        protected ListEditorComposite<CourseAreaDTO> createExpandedUi(Iterable<CourseAreaDTO> initialValues, ExpandedListEditorUi<CourseAreaDTO> ui) {
            return new CourseAreaListInlineEditorComposite(initialValues, ui);
        }
    }

    @Override
    protected CourseAreaDTO parse(String s) {
        return new CourseAreaDTO(s);
    }

    @Override
    protected CourseAreaDTO parse(String s, CourseAreaDTO valueToUpdate) {
        final CourseAreaDTO result;
        if (Util.equalsWithNull(valueToUpdate.getName(), s)) {
            result = valueToUpdate;
        } else {
            result = parse(s);
        }
        return result;
    }

    @Override
    protected String toString(CourseAreaDTO value) {
        return value.getName();
    }
}
