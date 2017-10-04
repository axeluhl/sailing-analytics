package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.ImagesBarCell;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.security.shared.PermissionBuilder.DefaultActions;

public class EventConfigImagesBarCell extends ImagesBarCell {
    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

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
                new ImageSpec(DefaultActions.EDIT, stringMessages.actionEdit(), makeImagePrototype(resources.editIcon())),
                new ImageSpec(DefaultActions.REMOVE, stringMessages.actionRemove(), makeImagePrototype(IconResources.INSTANCE.removeIcon()))
                );
    }
}