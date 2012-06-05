package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaDialog extends DataEntryDialog<RegattaDTO> {

    protected StringMessages stringConstants;
    protected RegattaDTO regatta;

    protected TextBox nameEntryField;
    protected ListBox boatClassEntryField;

    protected static class RegattaParameterValidator implements Validator<RegattaDTO> {

        private StringMessages stringConstants;
        private ArrayList<RegattaDTO> existingRegattas;

        public RegattaParameterValidator(StringMessages stringConstants, Collection<RegattaDTO> existingRegattas) {
            this.stringConstants = stringConstants;
            this.existingRegattas = new ArrayList<RegattaDTO>(existingRegattas);
        }

        @Override
        public String getErrorMessage(RegattaDTO regattaToValidate) {
            String errorMessage = null;
            boolean nameNotEmpty = regattaToValidate.name != null && regattaToValidate.name.length() > 0;
            boolean boatClassNotEmpty = regattaToValidate.boatClass != null;

            boolean unique = true;
            for (RegattaDTO regatta : existingRegattas) {
                if (regatta.name.equals(regattaToValidate.name)) {
                    unique = false;
                    break;
                }
            }

            if (!nameNotEmpty) {
                errorMessage = stringConstants.pleaseEnterNonEmptyName();
            } else if (!boatClassNotEmpty) {
                errorMessage = stringConstants.pleaseSelectABoatClass();
            } else if (!unique) {
                errorMessage = stringConstants.regattaWithThisNameAlreadyExists();
            }

            return errorMessage;
        }

    }

    public RegattaDialog(RegattaDTO regatta, RegattaParameterValidator validator, StringMessages stringConstants,
            AsyncCallback<RegattaDTO> callback) {
        super(stringConstants.regatta(), null, stringConstants.ok(), stringConstants.cancel(), validator,
                callback);
        this.stringConstants = stringConstants;
        this.regatta = regatta;
    }

    @Override
    protected RegattaDTO getResult() {
        int selectedIndex = boatClassEntryField.getSelectedIndex();
        regatta.name = nameEntryField.getText();
        if(selectedIndex > 0) {
            String boatClass = boatClassEntryField.getItemText(selectedIndex);
            regatta.boatClass = new BoatClassDTO(boatClass, 0.0);
        }
        return regatta;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {
            panel.add(additionalWidget);
        }
        
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        
        formGrid.setWidget(0,  0, new Label(stringConstants.name() + ":"));
        formGrid.setWidget(0, 1, nameEntryField);
        formGrid.setWidget(1, 0, new Label(stringConstants.boatClass() + ":"));
        formGrid.setWidget(1, 1, boatClassEntryField);
        return panel;
    }

    @Override
    public void show() {
        super.show();
        nameEntryField.setFocus(true);
    }

}
