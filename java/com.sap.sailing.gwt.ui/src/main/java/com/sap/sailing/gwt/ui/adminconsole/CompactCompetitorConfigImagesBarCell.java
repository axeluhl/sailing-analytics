package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class CompactCompetitorConfigImagesBarCell extends ImagesBarCell {
    public final static String ACTION_UNLINK = "ACTION_UNLINK";
    
    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public CompactCompetitorConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }
 
    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        List<ImageSpec> result = new ArrayList<ImageSpec>();
        result.add(new ImageSpec(ACTION_UNLINK, stringMessages.actionBoatUnlink(), makeImagePrototype(resources.unlinkIcon())));
        
        return result;
    }
}