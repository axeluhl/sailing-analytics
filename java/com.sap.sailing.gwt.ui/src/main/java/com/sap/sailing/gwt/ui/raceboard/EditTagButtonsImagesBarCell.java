package com.sap.sailing.gwt.ui.raceboard;

import java.util.Arrays; 

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.gwt.client.IconResources;

public class EditTagButtonsImagesBarCell extends ImagesBarCell {
    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    private static RaceBoardResources resources = GWT.create(RaceBoardResources.class);
    private final StringMessages stringMessages;
    
    public EditTagButtonsImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }


    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
    }
}