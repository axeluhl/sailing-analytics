package com.sap.sailing.gwt.ui.regattaoverview;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;

public class RegattaRaceStatesSettingsDialogComponent implements SettingsDialogComponent<RegattaRaceStatesSettings> {

    private final StringMessages stringMessages;
    private final RegattaRaceStatesSettings initialSettings;

    private TextBox test;
    
    public RegattaRaceStatesSettingsDialogComponent(RegattaRaceStatesSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        vp.add(new Label("Hallo Settings"));
        
        test = dialog.createTextBox("???");
        vp.add(test);
        
        return vp;
    }

    @Override
    public RegattaRaceStatesSettings getResult() {
        RegattaRaceStatesSettings result = new RegattaRaceStatesSettings();
        
        // TODO: set the values of the settings from the UI widgets here
        return result;
    }

    @Override
    public Validator<RegattaRaceStatesSettings> getValidator() {
        return new Validator<RegattaRaceStatesSettings>() {
            @Override
            public String getErrorMessage(RegattaRaceStatesSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return test;
    }

}
