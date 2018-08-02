package com.sap.sse.gwt.client.controls.listedit;

import java.util.List;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
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
        private Label checkBoxTopLabel;
        private ValueChangeHandler<Boolean> checkBoxValueChangeHandler;
        
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues,
                String placeholderTextForAddTextbox, int textBoxSize, List<CheckBox> checkBoxes, Label checkBoxTopLabel, ValueChangeHandler<Boolean> checkBoxValueChangeHandler) {
            super(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox, textBoxSize);
            this.checkBoxes = checkBoxes;
            this.checkBoxTopLabel = checkBoxTopLabel;
            this.checkBoxValueChangeHandler = checkBoxValueChangeHandler;
        }
        

        @Override
        protected void addRow(ValueType newValue) {
            super.addRow(newValue);
            CheckBox checkBox = new CheckBox();
            checkBoxes.add(checkBox);
            expandedValuesGrid.setWidget(expandedValuesGrid.getRowCount()-1, 2, checkBox);
            checkBox.setVisible(false);
            checkBox.getElement().getStyle().setBackgroundColor("red");
            checkBox.addValueChangeHandler(checkBoxValueChangeHandler);
            /*checkBox.addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    if(checkBox.getValue()) {
                        checkBox.getElement().getStyle().clearBackgroundColor();
                    }else {
                        checkBox.getElement().getStyle().setBackgroundColor("red");
                    }                    
                }
            }, ClickEvent.getType());*/
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
        protected Widget createAddWidget() {
            HorizontalPanel panel = (HorizontalPanel) super.createAddWidget();
            panel.add(checkBoxTopLabel);
            return panel;
        }
        
        
    }
}
