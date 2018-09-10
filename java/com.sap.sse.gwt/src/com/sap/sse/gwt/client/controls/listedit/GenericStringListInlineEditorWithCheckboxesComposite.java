package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.observer.Observable;
import com.sap.sse.common.observer.Observer;
import com.sap.sse.gwt.client.StringMessages;

public abstract class GenericStringListInlineEditorWithCheckboxesComposite<ValueType>
        extends GenericStringListInlineEditorComposite<ValueType> {

    public GenericStringListInlineEditorWithCheckboxesComposite(Iterable<ValueType> initialValues,
            StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues, int textBoxSize) {
        super(initialValues, stringMessages, removeImage, suggestValues, textBoxSize);
    }

    public static class ExpandedUi<ValueType> extends GenericStringListInlineEditorComposite.ExpandedUi<ValueType> implements Observable{

        private List<CheckBox> checkBoxes;
        private String checkBoxText;
        private List<Observer> observer = new ArrayList<Observer>();
        static final ListEditorResources ress = GWT.create(ListEditorResources.class);

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, List<String> suggestValues,
                String placeholderTextForAddTextbox, int textBoxSize, List<CheckBox> checkBoxes, String checkBoxText) {
            super(stringMessages, removeImage, suggestValues, placeholderTextForAddTextbox, textBoxSize);
            this.checkBoxes = checkBoxes;
            this.checkBoxText = checkBoxText;
            ress.css().ensureInjected();
        }

        interface ListEditorResources extends ClientBundle {
            @Source("ListEditor.gss")
            ListEditorCSS css();
        }
        
        interface ListEditorCSS extends CssResource {
            String checkBoxInvisible();
            String checkBoxNormal();
            String checkBoxError();
        }

        @Override
        protected void addRow(ValueType newValue) {
            super.addRow(newValue);
            CheckBox checkBox = new CheckBox(checkBoxText);
            checkBox.setStylePrimaryName(getInvisibleStyle());
            checkBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CheckBox source = (CheckBox) event.getSource();
                    if (source.getValue()) {
                        source.setStylePrimaryName(getNormalStyle());
                    } else {
                        source.setStylePrimaryName(getErrorStyle());
                    }
                    notifyObserver();
                }
            });
            checkBoxes.add(checkBox);
            expandedValuesGrid.setWidget(expandedValuesGrid.getRowCount() - 1, 2, checkBox);
        }
        
        public static String getInvisibleStyle() {
            return ress.css().checkBoxInvisible();
        }
        
        public static String getErrorStyle() {
            return ress.css().checkBoxError();
        }
        
        public static String getNormalStyle() {
            return ress.css().checkBoxNormal();
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

        @Override
        public void notifyObserver() {
            for(Observer observer : this.observer){
                observer.getNotified();
            }
        }

        @Override
        public void registerObserver(Observer observer) {
            this.observer.add(observer);
        }

        @Override
        public void unregisterObserver(Observer observer) {
            this.observer.remove(observer);
        }
    }
}
