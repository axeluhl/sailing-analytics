package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.ListBox;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.SWCRacingProcedureConstants;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public final class ListBoxUtils {

    public static void setupCourseDesignerModeListBox(ListBox box, CourseDesignerMode selectedCourseDesignerMode, 
            String noItemText) {
        for (CourseDesignerMode mode : CourseDesignerMode.values()) {
            if (mode != CourseDesignerMode.ADMIN_CONSOLE) {
                if (mode == CourseDesignerMode.UNKNOWN) {
                    box.addItem(noItemText, mode.name());
                } else {
                    box.addItem(mode.toString(), mode.name());
                }
                if (mode == selectedCourseDesignerMode) {
                    box.setSelectedIndex(box.getItemCount() - 1);
                }
            }
        }
    }

    public static void setupRacingProcedureTypeListBox(ListBox box, RacingProcedureType selectedRacingProcedureType, 
            String noItemText) {
        box.clear();
        for (RacingProcedureType type : RacingProcedureType.values()) {
            if (type == RacingProcedureType.UNKNOWN) {
                box.addItem(noItemText, type.name());
            } else {
                box.addItem(type.toString(), type.name());
            }
            if (type == selectedRacingProcedureType) {
                box.setSelectedIndex(box.getItemCount() - 1);
            }
        }
    }

    public static void setupFlagsListBox(ListBox box, Flags selectedFlag, String noItemText) {
        box.clear();
        for (Flags flag : Flags.values()) {
            if (flag == Flags.NONE) {
                box.addItem(noItemText, flag.name());
            } else {
                box.addItem(flag.toString(), flag.name());
            }
            if (flag == selectedFlag) {
                box.setSelectedIndex(box.getItemCount() -1);
            }
        }
    }

    private static void setupFlagsListBox(ListBox box, List<Flags> selectedFlags, Flags... availableFlags) {
        box.clear();
        for (Flags flag : availableFlags) {            
            box.addItem(flag.toString(), flag.name());
            if (selectedFlags.contains(flag)) {
                box.setItemSelected(box.getItemCount() - 1, true);
            }
        }
    }

    public static void setupFlagsListBox(ListBox box, List<Flags> selectedFlags) {
        setupFlagsListBox(box, selectedFlags, Flags.validValues());
    }

    public static void setupRRS26StartmodeFlagsListBox(ListBox box, List<Flags> selectedFlags) {
        setupFlagsListBox(box, selectedFlags, Flags.getStartModeFlags());
    }
    
    public static void setupSWCStartmodeFlagsListBox(ListBox box, List<Flags> selectedFlags) {
        // the SWC racing procedure restricts the possible start modes to BLACK and UNIFORM, eliminating INDIA / ZULU combinations as well as PAPA
        setupFlagsListBox(box, selectedFlags, SWCRacingProcedureConstants.DEFAULT_START_MODE_FLAGS.toArray(new Flags[0]));
    }

}
