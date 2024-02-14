package com.sap.sailing.gwt.ui.adminconsole.coursecreation;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.courseCreation.CourseConfigurationDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * A dialog that lets the user edit a {@link CourseConfigurationDTO}. It may be initialized from a server-side
 * {@code CourseConfiguration} which may in turn have been initialized from a {@code Regatta}'s course, or
 * from a {@code CourseTemplate}. At any time the user can select a course template from the library and
 * set up a course with the {@code MarkProperties} from the library, or generate new {@code Mark}s from
 * the {@code MarkTemplate}s coming with the {@code CourseTemplate}. All marks placed in the course configuration
 * may be selected for storing in the "inventory" as {@code MarkProperties} objects for future use.<p>
 * 
 * Positioning information for marks may be provided, either as a device ID
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CourseConfigurationEditDialog extends DataEntryDialog<CourseConfigurationDTO> {
    private static class Validator implements DataEntryDialog.Validator<CourseConfigurationDTO> {
        @Override
        public String getErrorMessage(CourseConfigurationDTO valueToValidate) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public CourseConfigurationEditDialog(StringMessages stringMessages, DialogCallback<CourseConfigurationDTO> callback) {
        super(stringMessages.configureCourse(), stringMessages.configureCourse(), stringMessages.ok(), stringMessages.cancel(),
                new Validator(), callback);
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid grid = new Grid(1, 2);
        
        // TODO Auto-generated method stub
        return super.getAdditionalWidget();
    }

    @Override
    protected CourseConfigurationDTO getResult() {
        // TODO Auto-generated method stub
        return null;
    }
}
