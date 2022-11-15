package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class ORCCertificatesConfigImagesBarCell extends ImagesBarCell {
    static final String ACTION_SHOW = "SHOW";

    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public ORCCertificatesConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_SHOW, stringMessages.details(), resources.magnifierIcon()));
    }
}