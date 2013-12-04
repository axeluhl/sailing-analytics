package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaWithSeriesAndFleetsEditDialog extends RegattaWithSeriesAndFleetsDialog {

    protected CheckBox proceduresConfigurationCheckbox;
    protected Button proceduresConfigurationButton;
    
    private RegattaConfigurationDTO currentProceduresConfiguration;
    
    public RegattaWithSeriesAndFleetsEditDialog(RegattaDTO regatta, Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, final StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(regatta, existingEvents, stringMessages.editRegatta(), stringMessages.ok(), stringMessages,
                null, callback);
        currentProceduresConfiguration = regatta.configuration;
        
        nameEntryField.setEnabled(false);
        boatClassEntryField.setEnabled(false);
        scoringSchemeListBox.setEnabled(false);
        sailingEventsListBox.setEnabled(true);
        courseAreaListBox.setEnabled(true);
        
        proceduresConfigurationCheckbox = createCheckbox(stringMessages.setRacingProcedureConfiguration());
        proceduresConfigurationCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() { 
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                proceduresConfigurationButton.setEnabled(event.getValue());
            }
        });
        proceduresConfigurationButton = new Button(stringMessages.edit());
        proceduresConfigurationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new RegattaConfigurationDialog(currentProceduresConfiguration, stringMessages, new DialogCallback<DeviceConfigurationDTO.RegattaConfigurationDTO>() {
                    @Override
                    public void ok(RegattaConfigurationDTO newProcedures) {
                        currentProceduresConfiguration = newProcedures;
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();;
            }
        });
        proceduresConfigurationCheckbox.setValue(regatta.configuration != null);
        proceduresConfigurationButton.setEnabled(regatta.configuration != null);
    }

    @Override
    public void show() {
        super.show();
        courseAreaListBox.setFocus(true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel) {
        VerticalPanel content = new VerticalPanel();
        
        Grid proceduresGrid = new Grid(1,2);
        proceduresGrid.setWidget(0, 0, proceduresConfigurationCheckbox);
        proceduresGrid.setWidget(0, 1, proceduresConfigurationButton);
        
        content.add(proceduresGrid);
        panel.add(content);
    }
    
    @Override
    protected RegattaDTO getResult() {
        RegattaDTO regatta = super.getResult();
        
        if (proceduresConfigurationCheckbox.getValue()) {
            regatta.configuration = currentProceduresConfiguration;
        } else {
            regatta.configuration = null;
        }
        return regatta;
    }

}
