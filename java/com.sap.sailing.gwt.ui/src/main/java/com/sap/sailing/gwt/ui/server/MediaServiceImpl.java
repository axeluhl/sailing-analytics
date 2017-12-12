package com.sap.sailing.gwt.ui.server;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.UserBox;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.tools.Path;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.server.RacingEventService;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

//     private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private static final long serialVersionUID = -8917349579281305977L;

    public MediaServiceImpl() {
        super();
        BundleContext context = Activator.getDefault();
        racingEventServiceTracker = new ServiceTracker<RacingEventService, RacingEventService>(context,
                RacingEventService.class.getName(), null);
        racingEventServiceTracker.open();
    }

    private RacingEventService racingEventService() {
        return racingEventServiceTracker.getService();
    }
    
    @Override
    public Iterable<MediaTrack> getMediaTracksForRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return racingEventService().getMediaTracksForRace(regattaAndRaceIdentifier);
    }
    
    @Override
    public Iterable<MediaTrack> getMediaTracksInTimeRange(RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
        return racingEventService().getMediaTracksInTimeRange(regattaAndRaceIdentifier);
    }

    @Override
    public Iterable<MediaTrack> getAllMediaTracks() {
        return racingEventService().getAllMediaTracks();
    }

    @Override
    public String addMediaTrack(MediaTrack mediaTrack) {
        if (mediaTrack.dbId != null) {
            throw new IllegalStateException("Property dbId must not be null for newly created media track.");
        }
        racingEventService().mediaTrackAdded(mediaTrack);
        return mediaTrack.dbId;
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        racingEventService().mediaTrackDeleted(mediaTrack);
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        racingEventService().mediaTrackTitleChanged(mediaTrack);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        racingEventService().mediaTrackUrlChanged(mediaTrack);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        racingEventService().mediaTrackStartTimeChanged(mediaTrack);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        racingEventService().mediaTrackDurationChanged(mediaTrack);
    }

    @Override
    public void updateRace(MediaTrack mediaTrack) {
        racingEventService().mediaTrackAssignedRacesChanged(mediaTrack);
    }

    @Override
    public VideoMetadataDTO checkMetadata(String url) {
        boolean canDownload = false;
        boolean spherical = false;
        Date recordStartedTimer = null;
        String message = "";
        try {
            URL input = new URL(url);
            try (IsoFile isof = new IsoFile(Channels.newChannel(input.openStream()))) {
                canDownload = true;
                // MovieHeaderBox movieHeaderBox = Path.getPath(isof, "moov[0]/mvhd");
                // System.out.println(movieHeaderBox.getCreationTime());
                UserBox uuidBox = Path.getPath(isof, "moov[0]/trak[0]/uuid");

                MovieBox mbox = isof.getMovieBox();
                if (mbox != null) {
                    MovieHeaderBox mhb = mbox.getMovieHeaderBox();
                    if (mhb != null) {
                        recordStartedTimer = mhb.getCreationTime();
                    }
                }

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new ByteArrayInputStream(uuidBox.getData()));

                NodeList childs = doc.getDocumentElement().getChildNodes();
                for (int i = 0; i < childs.getLength(); i++) {
                    Node child = childs.item(i);
                    if (child.getNodeName().toLowerCase().contains(":spherical")) {
                        spherical = true;
                    }
                }
            }
            throw new RuntimeException("remote error test");
        } catch (Exception e) {
            message = e.getMessage();
        }
        return new VideoMetadataDTO(canDownload, spherical, recordStartedTimer, message);
    }
}
