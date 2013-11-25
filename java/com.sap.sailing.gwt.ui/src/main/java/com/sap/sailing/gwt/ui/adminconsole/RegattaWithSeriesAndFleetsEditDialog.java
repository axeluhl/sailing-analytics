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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RacingProceduresConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaWithSeriesAndFleetsEditDialog extends RegattaWithSeriesAndFleetsDialog {

    protected ListBox racingProcedureListBox;
    protected ListBox designerModeEntryListBox;
    protected CheckBox proceduresConfigurationCheckbox;
    protected Button proceduresConfigurationButton;
    
    private RacingProceduresConfigurationDTO currentProceduresConfiguration;
    
    public RegattaWithSeriesAndFleetsEditDialog(RegattaDTO regatta, Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, final StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(regatta, existingEvents, stringMessages.editRegatta(), stringMessages.ok(), stringMessages,
                null, callback);
        currentProceduresConfiguration = regatta.racingProceduresConfiguration;
        
        nameEntryField.setEnabled(false);
        boatClassEntryField.setEnabled(false);
        scoringSchemeListBox.setEnabled(false);
        sailingEventsListBox.setEnabled(true);
        courseAreaListBox.setEnabled(true);
        
        racingProcedureListBox = createListBox(false);
        RacingProcedureType selectedRacingProcedureType = regatta.defaultRacingProcedureType;
        ListBoxUtils.setupRacingProcedureTypeListBox(racingProcedureListBox, selectedRacingProcedureType, stringMessages.dontoverwrite());
        designerModeEntryListBox = createListBox(false);
        CourseDesignerMode selectedCourseDesignerMode = regatta.defaultCourseDesignerMode;
        ListBoxUtils.setupCourseDesignerModeListBox(designerModeEntryListBox, selectedCourseDesignerMode, stringMessages.dontoverwrite());
        
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
                new RacingProceduresConfigurationDialog(currentProceduresConfiguration, stringMessages, new DialogCallback<DeviceConfigurationDTO.RacingProceduresConfigurationDTO>() {
                    @Override
                    public void ok(RacingProceduresConfigurationDTO newProcedures) {
                        currentProceduresConfiguration = newProcedures;
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();;
            }
        });
        proceduresConfigurationCheckbox.setValue(regatta.racingProceduresConfiguration != null);
        proceduresConfigurationButton.setEnabled(regatta.racingProceduresConfiguration != null);
    }

    @Override
    public void show() {
        super.show();
        courseAreaListBox.setFocus(true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel) {
        VerticalPanel content = new VerticalPanel();
        Grid formGrid = new Grid(2, 2);
        formGrid.setWidget(0, 0, new Label(stringMessages.racingProcedure() + ":"));
        formGrid.setWidget(0, 1, racingProcedureListBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.courseDesignerMode() + ":"));
        formGrid.setWidget(1, 1, designerModeEntryListBox);
        
        Grid proceduresGrid = new Grid(1,2);
        proceduresGrid.setWidget(0, 0, proceduresConfigurationCheckbox);
        proceduresGrid.setWidget(0, 1, proceduresConfigurationButton);
        
        content.add(formGrid);
        content.add(proceduresGrid);
        panel.add(content);
    }
    
    @Override
    protected RegattaDTO getResult() {
        RegattaDTO regatta = super.getResult();
        
        regatta.defaultRacingProcedureType = getSelectedDefaultRacingProcedureType();
        regatta.defaultCourseDesignerMode = getSelectedDefaultCourseDesignerModeType();
        if (proceduresConfigurationCheckbox.getValue()) {
            regatta.racingProceduresConfiguration = currentProceduresConfiguration;
        } else {
            regatta.racingProceduresConfiguration = null;
        }
        return regatta;
    }
    
    private CourseDesignerMode getSelectedDefaultCourseDesignerModeType() {
        int index = designerModeEntryListBox.getSelectedIndex();
        if (index >= 0) {
            CourseDesignerMode mode = CourseDesignerMode.valueOf(designerModeEntryListBox.getValue(index));
            return mode == CourseDesignerMode.UNKNOWN ? null : mode;
        }
        return null;
    }

    protected RacingProcedureType getSelectedDefaultRacingProcedureType() {
        int index = racingProcedureListBox.getSelectedIndex();
        if (index >= 0) {
            RacingProcedureType type = RacingProcedureType.valueOf(racingProcedureListBox.getValue(index));
            return type == RacingProcedureType.UNKNOWN ? null : type;
        }
        return null;
    }

}
