package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public abstract class AbstractCourseCreationPlace extends AbstractFilterablePlace {

    public String getVerticalTabName() {
        return AdminConsoleViewImpl.COURSE_CREATION;
    }

}
