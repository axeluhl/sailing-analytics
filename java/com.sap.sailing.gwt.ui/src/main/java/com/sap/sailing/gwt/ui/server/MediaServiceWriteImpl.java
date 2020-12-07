package com.sap.sailing.gwt.ui.server;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.MediaServiceWrite;
import com.sap.sse.security.Action;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.ui.server.SecurityDTOUtil;

public class MediaServiceWriteImpl extends MediaServiceImpl implements MediaServiceWrite {
    private static final long serialVersionUID = 7177987884609485859L;

    @Override
    public MediaTrackWithSecurityDTO addMediaTrack(MediaTrack mediaTrack) {
        if (mediaTrack.dbId != null) {
            throw new IllegalStateException("Property dbId must not be null for newly created media track.");
        }
        racingEventService().mediaTrackAdded(mediaTrack);
        final SecurityService securityService = racingEventService().getSecurityService();
        final QualifiedObjectIdentifier identifier = mediaTrack.getIdentifier();
        securityService.setDefaultOwnershipIfNotSet(identifier);
        if (!SecurityUtils.getSubject().isPermitted(identifier.getStringPermission(DefaultActions.CREATE))) {
            // the user was not permitted to create the object; remove it again
            racingEventService().mediaTrackDeleted(mediaTrack);
            securityService.deleteOwnership(identifier);
            throw new UnauthenticatedException("Not authorized to create media track object");
        } else {
            final MediaTrackWithSecurityDTO mediaTrackWithSecurity = new MediaTrackWithSecurityDTO(mediaTrack);
            SecurityDTOUtil.addSecurityInformation(racingEventService().getSecurityService(), mediaTrackWithSecurity,
                    mediaTrackWithSecurity.getIdentifier());
            return mediaTrackWithSecurity;
        }
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        racingEventService().getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(mediaTrack,
                new Action() {

                    @Override
                    public void run() throws Exception {
                        racingEventService().mediaTrackDeleted(mediaTrack);
                    }
                });
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        ensureUserCanUpdateMediaTrack(mediaTrack);
        racingEventService().mediaTrackTitleChanged(mediaTrack);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        ensureUserCanUpdateMediaTrack(mediaTrack);
        racingEventService().mediaTrackUrlChanged(mediaTrack);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        ensureUserCanUpdateMediaTrack(mediaTrack);
        racingEventService().mediaTrackStartTimeChanged(mediaTrack);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        ensureUserCanUpdateMediaTrack(mediaTrack);
        racingEventService().mediaTrackDurationChanged(mediaTrack);
    }

    @Override
    public void updateRace(MediaTrack mediaTrack) {
        ensureUserCanUpdateMediaTrack(mediaTrack);
        racingEventService().mediaTrackAssignedRacesChanged(mediaTrack);
    }

    private void ensureUserCanUpdateMediaTrack(MediaTrack mediaTrack) {
        SecurityUtils.getSubject().checkPermission(
                SecuredDomainType.MEDIA_TRACK.getStringPermissionForObject(DefaultActions.UPDATE, mediaTrack));
    }

}
