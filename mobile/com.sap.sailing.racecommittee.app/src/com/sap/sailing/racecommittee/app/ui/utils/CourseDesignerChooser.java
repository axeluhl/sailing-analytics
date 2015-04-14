package com.sap.sailing.racecommittee.app.ui.utils;

import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByMapCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByNameCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ESSCourseDesignDialog;

public class CourseDesignerChooser {

    public static RaceDialogFragment choose(ManagedRace race) {
        RegattaConfiguration configuration = race.getState().getConfiguration();
        CourseDesignerMode mode = configuration.getDefaultCourseDesignerMode();

        switch (mode) {
        case BY_MAP:
            return new ByMapCourseDesignDialog();
        case BY_MARKS:
            return new ESSCourseDesignDialog();
        case BY_NAME:
        default:
            return new ByNameCourseDesignDialog();
        }
    }

}
