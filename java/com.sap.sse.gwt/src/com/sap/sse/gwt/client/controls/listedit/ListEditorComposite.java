package com.sap.sse.gwt.client.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.sap.sse.common.Util;

/**
 * A widget that lets users edit a list of <code>ValueType</code> objects. How the values are displayed and made
 * editable is controlled by the {@link ListEditorUiStrategy} passed to the constructor. There are two standard UI
 * strategies: a "collapsed" strategy provided by the abstract class {@link CollapsedListEditorUi} and an "expanded"
 * strategy, provided by the abstract class {@link ExpandedListEditorUi}. The collapsed strategy displays the values
 * separated by commas in a single text box and has an "edit' button that pops up a dialog with an "expanded" editing
 * view for the list. The "expanded" strategy has a widget at its top that can be used to add an element, including an
 * "Add" button, and beneath it a grid with one row per value where each row holds a (potentially editable) widget
 * displaying a value, and a "remove" button next to it. The popup that displays the "expanded" strategy editor has a
 * "Save" and a "Cancel" button at its bottom.
 * <p>
 * 
 * To use this micro-framework for list editing, you can either provide only an "expanded" editor, or you can provide a
 * "collapsed" and an "expanded" editor which are then used as a cascade. If your values are of type {@link String}, you
 * may use the pre-defined {@link StringListEditorComposite} or {@link StringListInlineEditorComposite} classes. A
 * "collapsed" editor UI strategy requires the corresponding "expanded" strategy (which is used for the "edit" pop-up
 * dialog) as a constructor argument.
 * <p>
 * 
 * The iterable structure of initial values passed to the constructor is copied into a new list structure, so temporary
 * modifications applied through this UI component are not immediately reflected in the original iterable of initial values.
 * Using {@link #getValue()} will return the list of values that this component has assembled.
 * 
 * @author Lukas Niemeier
 * @author Axel Uhl (D043530)
 * 
 * @param <ValueType>
 */
public class ListEditorComposite<ValueType> extends Composite implements HasValue<Iterable<ValueType>> {

    protected final ListEditorUiStrategy<ValueType> activeUi;

    private List<ValueType> values;

    /**
     * @param initialValues
     *            will be copied into an internal collection so that any modification performed through this UI
     *            component do not alter this <code>initialValues</code> structure. Use {@link #getValue()} to obtain
     *            the current sequence of values as edited by this UI component.
     */
    public ListEditorComposite(Iterable<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
        this.values = new ArrayList<ValueType>();
        Util.addAll(initialValues, this.values);

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
    public void setValue(Iterable<ValueType> newValues) {
        setValue(newValues, true);
    }

    @Override
    public void setValue(Iterable<ValueType> newValues, boolean fireEvents) {
        this.values = new ArrayList<>();
        Util.addAll(newValues, this.values);
        activeUi.refresh();
        if (fireEvents) {
            onChange();
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Iterable<ValueType>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void onChange() {
        ValueChangeEvent.fire(this, getValue());
    }
}
