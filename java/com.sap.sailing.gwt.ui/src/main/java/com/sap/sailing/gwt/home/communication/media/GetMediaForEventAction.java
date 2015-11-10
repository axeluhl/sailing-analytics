package com.sap.sailing.gwt.home.communication.media;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.media.MediaConstants;
import com.sap.sse.common.media.ImageDescriptor;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.common.media.VideoDescriptor;

public class GetMediaForEventAction implements SailingAction<MediaDTO>, IsClientCacheable {
    
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetMediaForEventAction() {
    }

    public GetMediaForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public MediaDTO execute(SailingDispatchContext ctx) throws DispatchException {
        Event event = ctx.getRacingEventService().getEvent(eventId);
        EventReferenceDTO eventRef = new EventReferenceDTO(event);

        String eventName = event.getName();
        MediaDTO media = new MediaDTO();
        for(ImageDescriptor image : HomeServiceUtil.getPhotoGalleryImages(event)) {
            SailingImageDTO imageDTO = new SailingImageDTO(eventRef, image.getURL().toString(), image.getCreatedAtDate().asDate());
            imageDTO.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
            imageDTO.setTitle(image.getTitle() != null ? image.getTitle(): eventName);
            imageDTO.setSubtitle(image.getSubtitle());
            imageDTO.setTags(image.getTags());
            imageDTO.setCopyright(image.getCopyright());
            imageDTO.setLocale(image.getLocale() != null ? image.getLocale().toString() : null);
            media.addPhoto(imageDTO);
        }
        for(VideoDescriptor video : event.getVideos()) {
            MimeType type = video.getMimeType();
            if (MediaConstants.SUPPORTED_VIDEO_TYPES.contains(type)) {
                SailingVideoDTO videoDTO = HomeServiceUtil.toSailingVideoDTO(eventRef, video);
                media.addVideo(videoDTO);
            }
        }
        return media;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }

}
