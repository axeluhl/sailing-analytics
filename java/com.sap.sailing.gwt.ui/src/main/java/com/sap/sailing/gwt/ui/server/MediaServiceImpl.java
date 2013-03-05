package com.sap.sailing.gwt.ui.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MimeType;
import com.sap.sailing.server.RacingEventService;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

//    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private static final long serialVersionUID = -8917349579281305977L;

    public MediaServiceImpl() {
        super();
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
    }

    @Override
    public Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        TrackedRace trackedRace = racingEventServiceTracker.getService().getExistingTrackedRace(regattaAndRaceIdentifier);
        if (trackedRace != null) {
            Date raceStart = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
            Date raceEnd = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
            return createMediaTracksFromDB(mediaDB().queryOverlappingMediaTracks(raceStart , raceEnd));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<MediaTrack> getAllMediaTracks() {
        List<DBMediaTrack> allDBMediaTracks = mediaDB().loadAllMediaTracks();
        Collection<MediaTrack> result = createMediaTracksFromDB(allDBMediaTracks);
        return result;
//        try {
//            Date date = dateFormat.parse("2012-09-15 14:42:10 +0100");
//            return Arrays
//                    .asList(new MediaTrack[] { new MediaTrack(
//                            MediaType.VIDEO,
//                            "Day 2 Final - 49er Europeans 2012",
//                            "http://localhost:8888/media/HTML5/1809147112001_1842870496001_SAP-Regatta-Day02-Final_libtheora.ogv",
//                            date) });
//        } catch (ParseException e) {
//            throw new RuntimeException(e); // just due to annoying date parse error potential
//        }
    }

    private Collection<MediaTrack> createMediaTracksFromDB(Collection<DBMediaTrack> allDBMediaTracks) {
        Collection<MediaTrack> result = new ArrayList<MediaTrack>();
        for (DBMediaTrack dbMediaTrack : allDBMediaTracks) {
            MediaTrack mediaTrack = createMediaTrackFromDB(dbMediaTrack);
            result.add(mediaTrack );
        }
        return result;
    }

    private MediaTrack createMediaTrackFromDB(DBMediaTrack dbMediaTrack) {
        MimeType mimeType = dbMediaTrack.mimeType != null ? MimeType.valueOf(dbMediaTrack.mimeType) : null;
        MediaTrack mediaTrack = new MediaTrack(dbMediaTrack.dbId, dbMediaTrack.title, dbMediaTrack.url, dbMediaTrack.startTime, dbMediaTrack.durationInMillis, mimeType);
        return mediaTrack;
    }

    @Override
    public void addMediaTrack(MediaTrack mediaTrack) {
        String mimeType = mediaTrack.mimeType != null ? mediaTrack.mimeType.name() : null;
        mediaDB().insertMediaTrack(mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.durationInMillis, mimeType);
    }

    private MediaDB mediaDB() {
        return MediaDBFactory.INSTANCE.getDefaultMediaDB();
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        mediaDB().deleteMediaTrack(mediaTrack.dbId);
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        mediaDB().updateTitle(mediaTrack.dbId, mediaTrack.title);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        mediaDB().updateUrl(mediaTrack.dbId, mediaTrack.url);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        mediaDB().updateStartTime(mediaTrack.dbId, mediaTrack.startTime);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        mediaDB().updateDuration(mediaTrack.dbId, mediaTrack.durationInMillis);
    }

    @Override
    public void saveChanges(MediaTrack mediaTrack) {
        mediaDB().saveChanges(mediaTrack.dbId, mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.durationInMillis);
    }
}
