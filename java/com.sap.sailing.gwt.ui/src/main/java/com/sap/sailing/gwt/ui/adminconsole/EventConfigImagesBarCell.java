package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public class EventConfigImagesBarCell extends DefaultActionsImagesBarCell {

    public EventConfigImagesBarCell(final StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(getUpdateImageSpec(), getDeleteImageSpec(), getChangeOwnershipImageSpec(),
                getChangeACLImageSpec(), getMigrateGroupOwnershipForHierarchyImageSpec());
    }
}