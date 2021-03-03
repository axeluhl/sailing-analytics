package com.sap.sailing.landscape.ui.client;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class ApplicationReplicaSetsImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = DefaultActions.DELETE.name();
    static final String ACTION_UPGRADE = "UPGRADE";
    static final String ACTION_ARCHIVE = "ARCHIVE";
    static final String ACTION_DEFINE_LANDING_PAGE = "DEFINE_LANDING_PAGE";

    private final StringMessages stringMessages;

    public ApplicationReplicaSetsImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public ApplicationReplicaSetsImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_ARCHIVE, stringMessages.archive(),
                        IconResources.INSTANCE.archiveIcon()),
                new ImageSpec(ACTION_REMOVE, stringMessages.remove(),
                        IconResources.INSTANCE.removeIcon()),
                new ImageSpec(ACTION_DEFINE_LANDING_PAGE, stringMessages.defineLandingPage(),
                        IconResources.INSTANCE.editIcon()),
                new ImageSpec(ACTION_UPGRADE, stringMessages.upgrade(),
                        IconResources.INSTANCE.refreshIcon())
                );
    }
}
