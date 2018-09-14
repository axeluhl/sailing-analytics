package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultModes;

public class EventConfigImagesBarCell extends ImagesBarCell {
    private final StringMessages stringMessages;

    public EventConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public EventConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(DefaultModes.UPDATE.name(), stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(DefaultModes.DELETE.name(), stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon()))
                );
    }
}