package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.celltable.ImagesBarCell;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

public class RegattaConfigImagesBarCell extends ImagesBarCell {

    public static final String ACTION_UPDATE = DefaultActions.UPDATE.name();
    public static final String ACTION_DELETE = DefaultActions.DELETE.name();
    public static final String ACTION_CHANGE_OWNERSHIP = DefaultActions.CHANGE_OWNERSHIP.name();
    public static final String ACTION_CHANGE_ACL = DefaultActions.CHANGE_ACL.name();
    public final static String ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING";

    private final StringMessages stringMessages;
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    public RegattaConfigImagesBarCell(StringMessages stringConstants) {
        super();
        this.stringMessages = stringConstants;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        final List<ImageSpec> result = new ArrayList<ImageSpec>();
        GWT.log(getContext().getKey().getClass().getName());
        if (getContext().getKey() instanceof RaceDTO) {
            final RaceDTO object = (RaceDTO) getContext().getKey();
            final TrackedRaceStatusEnum status = object.status.status;
            if (status != TrackedRaceStatusEnum.ERROR && status != TrackedRaceStatusEnum.REMOVED
                    && status != TrackedRaceStatusEnum.FINISHED) {
                result.add(new ImageSpec(ACTION_STOP_TRACKING, stringMessages.stopTracking(),
                        makeImagePrototype(resources.stopRaceLogTracking())));
            }
        }
        result.add(new ImageSpec(ACTION_UPDATE, stringMessages.actionEdit(),
                makeImagePrototype(IconResources.INSTANCE.editIcon())));
        result.add(new ImageSpec(ACTION_DELETE, stringMessages.actionRemove(),
                makeImagePrototype(IconResources.INSTANCE.removeIcon())));
        result.add(new ImageSpec(ACTION_CHANGE_OWNERSHIP, stringMessages.actionChangeOwnership(),
                makeImagePrototype(IconResources.INSTANCE.changeOwnershipIcon())));
        result.add(new ImageSpec(ACTION_CHANGE_ACL, stringMessages.actionChangeACL(),
                makeImagePrototype(IconResources.INSTANCE.changeACLIcon())));
        return result;
    }
}