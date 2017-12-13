package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class RegattaConfigImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    private final StringMessages stringMessages;

    public RegattaConfigImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    public RegattaConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
    }
}