package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class CompetitorConfigImagesBarCell extends ImagesBarCell {
    static final String ACTION_EDIT = "ACTION_EDIT";
    static final String ACTION_REFRESH = "ACTION_REFRESH";
    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public CompetitorConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public CompetitorConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_EDIT, stringMessages.actionEdit(), makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(ACTION_REFRESH, stringMessages.reload(), makeImagePrototype(resources.reloadIcon()))
                );
    }
}