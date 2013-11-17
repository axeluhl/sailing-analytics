package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.client.StringMessages;

public final class ListBoxUtils {


    public static void setupCourseDesignerModeListBox(ListBox listBox, CourseDesignerMode selectedCourseDesignerMode, 
            StringMessages stringMessages) {
        for (CourseDesignerMode mode : CourseDesignerMode.values()) {
            if (mode == CourseDesignerMode.UNKNOWN) {
                listBox.addItem(stringMessages.none(), mode.name());
            } else {
                listBox.addItem(mode.toString(), mode.name());
            }
            if (mode == selectedCourseDesignerMode) {
                listBox.setSelectedIndex(listBox.getItemCount() - 1);
            }
        }
    }

    public static void setupRacingProcedureTypeListBox(ListBox listBox, RacingProcedureType selectedRacingProcedureType, 
            StringMessages stringMessages) {
        listBox.clear();
        for (RacingProcedureType type : RacingProcedureType.values()) {
            if (type == RacingProcedureType.UNKNOWN) {
                listBox.addItem(stringMessages.none(), type.name());
            } else {
                listBox.addItem(type.toString(), type.name());
            }
            if (type == selectedRacingProcedureType) {
                listBox.setSelectedIndex(listBox.getItemCount() - 1);
            }
        }
    }
}
