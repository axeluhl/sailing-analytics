package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaWithSeriesAndFleetsEditDialog extends RegattaWithSeriesAndFleetsDialog {

    protected ListBox racingProcedureListBox;
    protected ListBox designerModeEntryListBox;
    
    public RegattaWithSeriesAndFleetsEditDialog(RegattaDTO regatta, Collection<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, StringMessages stringMessages, DialogCallback<RegattaDTO> callback) {
        super(regatta, existingEvents, stringMessages.editRegatta(), stringMessages.ok(), stringMessages,
                null, callback);
        nameEntryField.setEnabled(false);
        boatClassEntryField.setEnabled(false);
        scoringSchemeListBox.setEnabled(false);
        sailingEventsListBox.setEnabled(true);
        courseAreaListBox.setEnabled(true);
        
        racingProcedureListBox = createListBox(false);
        for (RacingProcedureType type : RacingProcedureType.values()) {
            if (type == RacingProcedureType.UNKNOWN) {
                racingProcedureListBox.addItem(stringMessages.none(), type.name());
            } else {
                racingProcedureListBox.addItem(type.name(), type.name());
            }
            if (type == regatta.defaultRacingProcedureType) {
                racingProcedureListBox.setSelectedIndex(racingProcedureListBox.getItemCount() - 1);
            }
        }
        designerModeEntryListBox = createListBox(false);
        for (CourseDesignerMode mode : CourseDesignerMode.values()) {
            if (mode == CourseDesignerMode.UNKNOWN) {
                designerModeEntryListBox.addItem(stringMessages.none(), mode.name());
            } else {
                designerModeEntryListBox.addItem(mode.name(), mode.name());
            }
            if (mode == regatta.defaultCourseDesignerMode) {
                designerModeEntryListBox.setSelectedIndex(designerModeEntryListBox.getItemCount() - 1);
            }
        }
        
    }

    @Override
    public void show() {
        super.show();
        courseAreaListBox.setFocus(true);
    }

    @Override
    protected void setupAdditionalWidgetsOnPanel(VerticalPanel panel) {
        Grid formGrid = new Grid(2, 2);
        formGrid.setWidget(0, 0, new Label(stringMessages.racingProcedure() + ":"));
        formGrid.setWidget(0, 1, racingProcedureListBox);
        formGrid.setWidget(1, 0, new Label(stringMessages.courseDesignerMode() + ":"));
        formGrid.setWidget(1, 1, designerModeEntryListBox);
        panel.add(formGrid);
    }
    
    @Override
    protected RegattaDTO getResult() {
        RegattaDTO regatta = super.getResult();
        
        regatta.defaultRacingProcedureType = getSelectedDefaultRacingProcedureType();
        regatta.defaultCourseDesignerMode = getSelectedDefaultCourseDesignerModeType();
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
