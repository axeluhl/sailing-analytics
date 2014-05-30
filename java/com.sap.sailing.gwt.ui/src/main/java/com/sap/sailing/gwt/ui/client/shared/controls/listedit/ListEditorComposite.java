package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

/**
 * A widget that lets users edit a list of <code>ValueType</code> objects. How the values are displayed and made
 * editable is controlled by the {@link ListEditorUiStrategy} passed to the constructor. There are two standard UI
 * strategies: a "collapsed" strategy provided by the abstract class {@link CollapsedListEditorUi} and an "expanded"
 * strategy, provided by the abstract class {@link ExpandedListEditorUi}. The collapsed strategy displays the values
 * separated by commas in a single text box and has an "edit' button that pops up a dialog with an "expanded" editing
 * view for the list. The "expanded" strategy has a widget at its top that can be used to add an element, including an
 * "Add" button, and beneath it a grid with one row per value where each row holds a (potentially editable) widget
 * displaying a value, and a "remove" button next to it. The popup that displays the "expanded" strategy editor has a
 * "Save" and a "Cancel" button at its bottom.<p>
 * 
 * 
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (D043530)
 * 
 * @param <ValueType>
 */
public class ListEditorComposite<ValueType> extends Composite implements HasValue<List<ValueType>>,
        HasValueChangeHandlers<List<ValueType>> {

    private final ListEditorUiStrategy<ValueType> activeUi;

    private List<ValueType> values;

    public ListEditorComposite(List<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
        this.values = new ArrayList<ValueType>(initialValues);
        this.activeUi = activeUi;
        this.activeUi.setContext(this);

        initWidget(activeUi.initWidget());
        activeUi.refresh();
    }

    @Override
    public List<ValueType> getValue() {
        return values;
    }

    @Override
    public void setValue(List<ValueType> newValues) {
        setValue(newValues, true);
    }

    @Override
    public void setValue(List<ValueType> newValues, boolean fireEvents) {
        this.values = newValues;
        activeUi.refresh();
        if (fireEvents) {
            onChange();
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ValueType>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void onChange() {
        ValueChangeEvent.fire(this, getValue());
    }
}
