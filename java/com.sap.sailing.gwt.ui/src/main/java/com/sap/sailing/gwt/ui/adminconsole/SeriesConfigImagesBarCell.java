package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class SeriesConfigImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = "ACTION_REMOVE";
    static final String ACTION_EDIT = "ACTION_EDIT";
    private final StringMessages stringMessages;

    public SeriesConfigImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    public SeriesConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
    }
}