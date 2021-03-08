package com.sap.sailing.gwt.managementconsole.partials.inputs.listofstrings;

import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;

class DeletableTextInput extends Composite implements HasValue<String>, HasEnabled {

    interface DeletableTextInputUiBinder extends UiBinder<HTMLPanel, DeletableTextInput> {}
    private static DeletableTextInputUiBinder uiBinder = GWT.create(DeletableTextInputUiBinder.class);
    
    @UiField
    ListOfStringsInputResources local_res;
    @UiField
    HTMLPanel panel;
    @UiField
    Anchor delete;
    @UiField
    InputElement input;
    @UiField
    DivElement deleteWrapper;
    
    private Consumer<DeletableTextInput> onRemove;
    
    DeletableTextInput(String value, boolean notDeletable, Consumer<DeletableTextInput> onRemove) {
        this(value);
        this.onRemove = onRemove;
        if (notDeletable) {
            setNotDeletable();
        }
    }
    
    public DeletableTextInput() {
        initWidget(uiBinder.createAndBindUi(this));
        delete.addClickHandler(e -> {this.removeFromParent(); onRemove.accept(this);});
    }
    
    public DeletableTextInput(String value) {
        this();
        input.setValue(value);
    }

    public void setPlaceholder(String placeholder) {
        input.setAttribute("placeholder", placeholder);
    }
    
    public void setNotDeletable() {
        deleteWrapper.removeFromParent();
    }
    
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return null;
    }

    @Override
    public String getValue() {
        return input.getValue();
    }

    @Override
    public void setValue(String value) {
        input.setValue(value);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        input.setValue(value);   
    }

    @Override
    public boolean isEnabled() {
        return !input.isDisabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        input.setDisabled(!enabled);
    }
}

