package com.sap.sailing.landscape.ui.client;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;

public class MongoEndpointsImagesBarCell extends ImagesBarCell {
    static final String ACTION_SCALE = "SCALE";

    private final StringMessages stringMessages;

    public MongoEndpointsImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public MongoEndpointsImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_SCALE, stringMessages.scale(),
                        IconResources.INSTANCE.scaleIcon())
                );
    }
}