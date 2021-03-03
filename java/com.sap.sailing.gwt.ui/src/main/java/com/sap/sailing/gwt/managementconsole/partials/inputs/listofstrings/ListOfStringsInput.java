package com.sap.sailing.gwt.managementconsole.partials.inputs.listofstrings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.sap.sailing.gwt.managementconsole.places.UiUtils;

public class ListOfStringsInput extends Composite implements HasValue<List<String>>, HasEnabled {

    interface ListOfStringsInputUiBinder extends UiBinder<HTMLPanel, ListOfStringsInput> {}
    private static ListOfStringsInputUiBinder uiBinder = GWT.create(ListOfStringsInputUiBinder.class);
    
    @UiField
    ListOfStringsInputResources local_res;
    @UiField
    HTMLPanel panel, textInputsWrapper;
    @UiField
    Anchor addAnchor;
    
    private boolean enabled = true;
    
    List<DeletableTextInput> textInputs = new ArrayList<>();

    public ListOfStringsInput() {
        initWidget(uiBinder.createAndBindUi(this));
        addInput(false);
        addAnchor.addClickHandler(e -> addInput());
    }

    private void addInput() {
        addInput(true);
    }
    
    private void addInput(String value) {
        addInput(true, value);
    }
    
    private void addInput(boolean deletable) {
        addInput(deletable, "");
    }
    
    private void addInput(boolean deletable, String value) {
        DeletableTextInput input = new DeletableTextInput(value, !deletable, e -> textInputs.remove(e));
        textInputs.add(input);
        textInputsWrapper.add(input);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<String>> handler) {
        return null;
    }

    @Override
    public List<String> getValue() {
        List<String> values = new ArrayList<>();
        for (DeletableTextInput textInput : textInputs) {
            values.add(textInput.getValue());
        }
        return values;
    }

    public List<String> getNotEmptyValues() {
        return getValue().stream().filter(name -> UiUtils.isNotBlank(name)).collect(Collectors.toList());
    }

    @Override
    public void setValue(List<String> listOfStrings) {
        for (int index = 0; index < listOfStrings.size(); index++) {
            if (textInputs.size() > index) {
                textInputs.get(index).setValue(listOfStrings.get(index));
            } else {
                addInput(listOfStrings.get(index));
            }
        }
    }

    @Override
    public void setValue(List<String> value, boolean fireEvents) {
        setValue(value);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (DeletableTextInput textInput : textInputs) {
            textInput.setEnabled(enabled);
        }
        if (!enabled) {
            addAnchor.removeFromParent();
        }
    }
}

