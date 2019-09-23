package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public class WaypointImagesBarCell extends DefaultActionsImagesBarCell {
    public static final String ACTION_ORC_PCS_DEFINE_LEG = "ACTION_DEFINE_LEG";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    
    public WaypointImagesBarCell(final StringMessages stringMessages) {
        super(stringMessages);
        this.stringMessages = stringMessages;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(getDeleteImageSpec(),
                new ImageSpec(ACTION_ORC_PCS_DEFINE_LEG, stringMessages.actionDefineLegForOrcPcs(), resources.orcPcsDefineLegIcon()));
    }
}