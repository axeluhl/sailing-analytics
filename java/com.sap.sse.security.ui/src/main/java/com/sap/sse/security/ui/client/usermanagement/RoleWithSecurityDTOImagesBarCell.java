package com.sap.sse.security.ui.client.usermanagement;

import java.util.Arrays;

import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * RoleWithSecurityDTO ImagesBarCell for use in {@link RoleWithSecurityDTOTableWrapper} which contains just the delete
 * action.
 */
public class RoleWithSecurityDTOImagesBarCell extends DefaultActionsImagesBarCell {

    public RoleWithSecurityDTOImagesBarCell(StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(getDeleteImageSpec(), getChangeOwnershipImageSpec(), getChangeACLImageSpec());
    }
}
