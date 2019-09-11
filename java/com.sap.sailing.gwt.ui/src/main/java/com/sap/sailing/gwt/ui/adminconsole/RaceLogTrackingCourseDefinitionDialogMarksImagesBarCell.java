package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell extends ImagesBarCell {
    public static final String ACTION_PING = "ACTION_PING";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    
    public RaceLogTrackingCourseDefinitionDialogMarksImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        ArrayList<ImageSpec> result = new ArrayList<ImageSpec>();
        result.add(new ImageSpec(ACTION_PING, stringMessages.pingPosition(stringMessages.mark()), makeImagePrototype(resources.ping())));

        return result;
    }
}