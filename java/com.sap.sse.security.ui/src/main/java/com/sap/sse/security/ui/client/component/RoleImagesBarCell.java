package com.sap.sse.security.ui.client.component;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class RoleImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_EDIT = "ACTION_EDIT";
    private final StringMessages stringMessages;

    public RoleImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public RoleImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        this(stringMessages);
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(ACTION_REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon())));
    }
}