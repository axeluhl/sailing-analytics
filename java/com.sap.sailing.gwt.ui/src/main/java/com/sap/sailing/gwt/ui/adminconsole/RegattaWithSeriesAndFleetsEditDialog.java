package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaWithSeriesAndFleetsEditDialog extends RegattaWithSeriesAndFleetsDialog {
    protected CheckBox regattaConfigurationCheckbox;
    protected Button regattaConfigurationButton;

    private RegattaConfigurationDTO currentRegattaConfiguration;

    protected static class RegattaParameterValidator extends AbstractRegattaParameterValidator {
        public RegattaParameterValidator(StringMessages stringMessages) {
            super(stringMessages);
        }
    }
    
    public RegattaWithSeriesAndFleetsEditDialog(RegattaDTO regatta, Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, EventDTO correspondingEvent, final SailingServiceAsync sailingService,
            final StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(regatta, regatta.series, existingEvents, correspondingEvent, stringMessages.editRegatta(),
                stringMessages.ok(), sailingService, stringMessages, new RegattaParameterValidator(stringMessages), callback);
        ensureDebugId("RegattaWithSeriesAndFleetsEditDialog");
        currentRegattaConfiguration = regatta.configuration;

        nameEntryField.setEnabled(false);
        boatClassEntryField.setEnabled(false);
        canBoatsOfCompetitorsChangePerRaceCheckBox.setEnabled(false);
        scoringSchemeListBox.setEnabled(false);
        sailingEventsListBox.setEnabled(true);
        courseAreaListBox.setEnabled(true);
        competitorRegistrationTypeListBox.setEnabled(false);

        regattaConfigurationCheckbox = createCheckbox(stringMessages.setRacingProcedureConfiguration());
        regattaConfigurationCheckbox.ensureDebugId("RacingProcedureConfigurationCheckBox");
        regattaConfigurationCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                regattaConfigurationButton.setEnabled(event.getValue());
            }
        });
        regattaConfigurationButton = new Button(stringMessages.edit());
        regattaConfigurationButton.ensureDebugId("RacingProcedureConfigurationEditButton");
        regattaConfigurationButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new RegattaConfigurationDialog(currentRegattaConfiguration == null ? new RegattaConfigurationDTO()
                        : currentRegattaConfiguration, stringMessages,
                        new DialogCallback<DeviceConfigurationDTO.RegattaConfigurationDTO>() {
                            @Override
                            public void ok(RegattaConfigurationDTO newConfiguration) {
                                currentRegattaConfiguration = newConfiguration;
                            }

                            @Override
                            public void cancel() {
                            }
                        }).show();
            }
        });
        regattaConfigurationCheckbox.setValue(regatta.configuration != null);
        regattaConfigurationButton.setEnabled(regatta.configuration != null);
    }

    @Override
    protected boolean isEnableFleetRemoval() {
        return false;
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return courseAreaListBox;
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel, Grid formGrid) {
        super.setupAdditionalWidgetsOnPanel(panel, formGrid);
        VerticalPanel content = new VerticalPanel();

        Grid proceduresGrid = new Grid(1, 2);
        proceduresGrid.setWidget(0, 0, regattaConfigurationCheckbox);
        proceduresGrid.setWidget(0, 1, regattaConfigurationButton);

        content.add(proceduresGrid);
        panel.add(content);

        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(getSeriesEditor(), stringMessages.series());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
    }

    @Override
    protected RegattaDTO getResult() {
        RegattaDTO regatta = super.getResult();
        if (regattaConfigurationCheckbox.getValue()) {
            regatta.configuration = currentRegattaConfiguration;
        } else {
            regatta.configuration = null;
        }
        return regatta;
    }

}
