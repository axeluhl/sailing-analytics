package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class MediaActionBarCell extends ImagesBarCell {

    public static final String ACTION_REMOVE = "ACTION_REMOVE";
    private final StringMessages stringMessages;

    public MediaActionBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_REMOVE,
                        stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
    }
}
