package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.ImagesBarCell;

public class CourseManagementWidgetWaypointsImagesBarCell extends ImagesBarCell {
    public static final String ACTION_DELETE = "ACTION_DELETE";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    
    public CourseManagementWidgetWaypointsImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        ArrayList<ImageSpec> result = new ArrayList<ImageSpec>();
        result.add(new ImageSpec(ACTION_DELETE, stringMessages.remove(), makeImagePrototype(resources.removeIcon())));
        return result;
    }
}