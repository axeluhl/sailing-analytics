package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.StringMessages;

public abstract class GenericStringListInlineEditorWithCheckboxesComposite<ValueType>
        extends GenericStringListInlineEditorComposite<ValueType> {

    public GenericStringListInlineEditorWithCheckboxesComposite(Iterable<ValueType> initialValues,
            StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
        super(initialValues, stringMessages, removeImage, suggestValues, textBoxSize);
    }

    public static class ExpandedUi<ValueType> extends GenericStringListInlineEditorComposite.ExpandedUi<ValueType> {

        private List<CheckBox> checkBoxes;
        private ClickHandler checkBoxClickHandler;
        private String checkBoxText;
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues,
                String placeholderTextForAddTextbox, int textBoxSize, List<CheckBox> checkBoxes, String checkBoxText, ClickHandler clickHandler) {
            super(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox, textBoxSize);
            this.checkBoxes = checkBoxes;
            this.checkBoxClickHandler = clickHandler;
            this.checkBoxText = checkBoxText;
        }

        @Override
        protected void addRow(ValueType newValue) {
            super.addRow(newValue);
            CheckBox checkBox = new CheckBox(checkBoxText);
            checkBoxes.add(checkBox);
            expandedValuesGrid.setWidget(expandedValuesGrid.getRowCount()-1, 2, checkBox);
            checkBox.setVisible(false);
            checkBox.getElement().getStyle().setBackgroundColor("red");
            checkBox.addClickHandler(checkBoxClickHandler);
        }
        
        @Override
        public Widget initWidget() {
            expandedValuesGrid = new Grid(0, 3);
            expandedValuesGrid.ensureDebugId("ExpandedValuesGrid");
            
            VerticalPanel panel = new VerticalPanel();
            panel.add(createAddWidget());
            panel.add(expandedValuesGrid);
            return panel;
            
        }
        
        @Override
        public void onRowRemoved(int rowIndex) {
            checkBoxes.remove(rowIndex);
        }
    }
}
