package com.sap.sse.security.ui.client.usermanagement;

import java.util.Arrays;

import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.usermanagement.permissions.WildcardPermissionWithSecurityDTOTableWrapper;
import com.sap.sse.security.ui.client.usermanagement.roles.RoleWithSecurityDTOTableWrapper;

/**
 * RoleWithSecurityDTO ImagesBarCell for use in {@link RoleWithSecurityDTOTableWrapper} and
 * {@link WildcardPermissionWithSecurityDTOTableWrapper} which contains the edit ownership, the edit ACL and the delete
 * action.
 */
public class PermissionAndRoleImagesBarCell extends DefaultActionsImagesBarCell {

    public PermissionAndRoleImagesBarCell(StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(getDeleteImageSpec(), getChangeOwnershipImageSpec(), getChangeACLImageSpec());
    }
}
