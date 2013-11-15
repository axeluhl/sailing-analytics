package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;

import com.sap.sailing.domain.base.CourseDesignerMode;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByLabelCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ByMapCourseDesignDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.coursedesign.ESSCourseDesignDialog;

public class CourseDesignerChooser {
    
    public static RaceDialogFragment choose(Context context, CourseDesignerMode mode) {
        // TODO check override in user settings
        switch (mode) {
        case BY_MAP:
            return new ByMapCourseDesignDialog();
        case BY_MARKS:
            return new ESSCourseDesignDialog();
        case BY_NAME:
        default:
            return new ByLabelCourseDesignDialog();
        }
    }

}
