package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultModes;

public class RegattaConfigImagesBarCell extends ImagesBarCell {
    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RegattaConfigImagesBarCell(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    public RegattaConfigImagesBarCell(SafeHtmlRenderer<String> renderer, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(
                new ImageSpec(DefaultModes.UPDATE.name(), stringMessages.actionEdit(),
                        makeImagePrototype(IconResources.INSTANCE.editIcon())),
                new ImageSpec(DefaultModes.DELETE.name(), stringMessages.actionRemove(),
                        makeImagePrototype(IconResources.INSTANCE.removeIcon())),
                new ImageSpec(DefaultModes.CHANGE_OWNERSHIP.name(), stringMessages.changeOwnership(),
                        makeImagePrototype(resources.competitorsIcon())));
    }
}