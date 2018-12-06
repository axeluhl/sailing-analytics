package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class BoatConfigImagesBarCell extends ImagesBarCell {
    static final String ACTION_UPDATE = DefaultActions.UPDATE.name();
    static final String ACTION_REFRESH = "ACTION_REFRESH";
    public static final String ACTION_CHANGE_OWNERSHIP = DefaultActions.CHANGE_OWNERSHIP.name();
    public static final String ACTION_CHANGE_ACL = DefaultActions.CHANGE_ACL.name();

    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public BoatConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public BoatConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(ACTION_UPDATE, stringMessages.actionEdit(),
                        makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(ACTION_REFRESH, stringMessages.reload(), makeImagePrototype(resources.reloadIcon())),
                new ImageSpec(ACTION_CHANGE_OWNERSHIP, stringMessages.actionChangeOwnership(),
                        IconResources.INSTANCE.changeOwnershipIcon()),
                new ImageSpec(ACTION_CHANGE_ACL, stringMessages.actionChangeACL(),
                        IconResources.INSTANCE.changeACLIcon()));
    }
}