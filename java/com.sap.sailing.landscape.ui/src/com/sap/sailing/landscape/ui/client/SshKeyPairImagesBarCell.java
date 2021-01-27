package com.sap.sailing.landscape.ui.client;

import java.util.Arrays;

import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.landscape.ui.client.i18n.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class SshKeyPairImagesBarCell extends ImagesBarCell {
    static final String ACTION_REMOVE = "ACTION_REMOVE";
    public static final String ACTION_CHANGE_OWNERSHIP = DefaultActions.CHANGE_OWNERSHIP.name();
    public static final String ACTION_CHANGE_ACL = DefaultActions.CHANGE_ACL.name();

    private final StringMessages stringMessages;

    public SshKeyPairImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public SshKeyPairImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_REMOVE, stringMessages.remove(),
                        IconResources.INSTANCE.removeIcon()),
                new ImageSpec(ACTION_CHANGE_OWNERSHIP, stringMessages.actionChangeOwnership(),
                        IconResources.INSTANCE.changeOwnershipIcon()),
                new ImageSpec(ACTION_CHANGE_ACL, stringMessages.actionChangeACL(),
                        IconResources.INSTANCE.changeACLIcon()));
    }
}