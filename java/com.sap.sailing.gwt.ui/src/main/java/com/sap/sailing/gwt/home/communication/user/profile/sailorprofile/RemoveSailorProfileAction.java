package com.sap.sailing.gwt.home.communication.user.profile.sailorprofile;

import java.util.UUID;

/**
 * {@link UpdateSailorProfileAction} implementation to remove a sailor profile identified by the UUID for the currently
 * logged in user.
 */
public class RemoveSailorProfileAction extends UpdateSailorProfileAction {

    public RemoveSailorProfileAction(UUID uuid) {
        super(uuid);
    }

    protected RemoveSailorProfileAction() {
    }
}
