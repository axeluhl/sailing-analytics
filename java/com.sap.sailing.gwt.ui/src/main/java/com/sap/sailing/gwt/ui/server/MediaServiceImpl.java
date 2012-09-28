package com.sap.sailing.gwt.ui.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.persistence.media.DBMediaTrack;
import com.sap.sailing.domain.persistence.media.MediaDB;
import com.sap.sailing.domain.persistence.media.MediaDBFactory;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaSubType;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack.MediaType;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private static final long serialVersionUID = -8917349579281305977L;

    public MediaServiceImpl() {
        super();
    }

    @Override
    public Collection<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return getAllMediaTracks();
    }

    @Override
    public List<MediaTrack> getAllMediaTracks() {
        List<DBMediaTrack> allDBMediaTracks = mediaDB().loadAllMediaTracks();
        List<MediaTrack> result = new ArrayList<MediaTrack>();
        for (DBMediaTrack dbMediaTrack : allDBMediaTracks) {
            MediaTrack mediaTrack = createMediaTrackFromDB(dbMediaTrack);
            result.add(mediaTrack );
        }
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

    private MediaTrack createMediaTrackFromDB(DBMediaTrack dbMediaTrack) {
        MediaType mediaType = MediaType.valueOf(dbMediaTrack.mimeType);
        MediaSubType mediaSubType = MediaSubType.valueOf(dbMediaTrack.mimeSubType);
        MediaTrack mediaTrack = new MediaTrack(dbMediaTrack.title, dbMediaTrack.url, dbMediaTrack.startTime, mediaType, mediaSubType);
        return mediaTrack;
    }

    @Override
    public void addMediaTrack(MediaTrack mediaTrack) {
        mediaDB().insertMediaTrack(mediaTrack.title, mediaTrack.url, mediaTrack.startTime, mediaTrack.type.name(), mediaTrack.subType.name());
    }

    private MediaDB mediaDB() {
        return MediaDBFactory.INSTANCE.getDefaultMediaDB();
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        mediaDB().deleteMediaTrack(mediaTrack.title);
    }
}
