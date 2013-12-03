package com.sap.sailing.racecommittee.app.ui.utils;

import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByMapCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByNameCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ESSCourseDesignDialog;

public class CourseDesignerChooser {
    
    public static RaceDialogFragment choose(AppPreferences preferences, ManagedRace race) {
        CourseDesignerMode mode;
        if (preferences.isDefaultCourseDesignerModeOverridden()) {
            mode = preferences.getDefaultCourseDesignerMode();
        } else {
            mode = race.getRaceGroup().getDefaultCourseDesignerMode();
        }
        
        switch (mode) {
        case BY_MAP:
            return new ByMapCourseDesignDialog();
        case BY_MARKS:
            return new ESSCourseDesignDialog();
        case BY_NAME:
        default:
            // might be UNKNOWN... let's take this one
            return new ByNameCourseDesignDialog();
        }
    }

}
