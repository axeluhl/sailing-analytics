package com.sap.sse.security.ui.client.component.usergroup.users;

import java.util.Arrays;

import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * RoleDefinition ImagesBarCell for use in {@link RoleDefinitionTableWrapper} which contains just the delete action.
 */
class UserGroupUsersImagesBarCell extends DefaultActionsImagesBarCell {

    UserGroupUsersImagesBarCell(StringMessages stringMessages) {
        super(stringMessages);
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        return Arrays.asList(getDeleteImageSpec());
    }
}
