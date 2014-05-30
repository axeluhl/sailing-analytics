package com.sap.sailing.gwt.ui.client.shared.controls.listedit;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

public abstract class ListEditorComposite<ValueType> extends Composite implements HasValue<List<ValueType>>,
        HasValueChangeHandlers<List<ValueType>> {

    private final ListEditorUiStrategy<ValueType> activeUi;

    private List<ValueType> values;

    protected ListEditorComposite(List<ValueType> initialValues, ListEditorUiStrategy<ValueType> activeUi) {
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
