package com.sap.sailing.gwt.ui.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.Channels;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.shiro.SecurityUtils;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.UserBox;
import org.mp4parser.boxes.iso14496.part12.MediaDataBox;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.MovieHeaderBox;
import org.mp4parser.tools.Path;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {

    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    private static final int METADATA_CONNECTION_TIMEOUT = 10000;

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private static final int REQUIRED_SIZE_IN_BYTES = 10000000;
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
    
    private void ensureUserCanManageMedia() {
        SecurityUtils.getSubject().checkPermission(Permission.MANAGE_MEDIA.getStringPermissionForObjects(Mode.UPDATE));
    }

    @Override
    public String addMediaTrack(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        if (mediaTrack.dbId != null) {
            throw new IllegalStateException("Property dbId must not be null for newly created media track.");
        }
        racingEventService().mediaTrackAdded(mediaTrack);
        return mediaTrack.dbId;
    }

    @Override
    public void deleteMediaTrack(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackDeleted(mediaTrack);
    }

    @Override
    public void updateTitle(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackTitleChanged(mediaTrack);
    }

    @Override
    public void updateUrl(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackUrlChanged(mediaTrack);
    }

    @Override
    public void updateStartTime(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackStartTimeChanged(mediaTrack);
    }

    @Override
    public void updateDuration(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackDurationChanged(mediaTrack);
    }

    @Override
    public void updateRace(MediaTrack mediaTrack) {
        ensureUserCanManageMedia();
        racingEventService().mediaTrackAssignedRacesChanged(mediaTrack);
    }

    @Override
    public VideoMetadataDTO checkMetadata(String url) {
        ensureUserCanManageMedia();
        VideoMetadataDTO response = null;
        try {
            URL input = new URL(url);
            // check size and do rangerequests if possible
            long fileSize = determineFileSize(input);
            if (fileSize > 2 * REQUIRED_SIZE_IN_BYTES) {
                response = checkMetadataByPartialDownloads(input, fileSize);
            } else {
                response = checkMetadataByFullFileDownload(input);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in video analysis ", e);
            response = new VideoMetadataDTO(false, null, false, null, e.getMessage());
        }
        return response;
    }

    private long determineFileSize(URL input) throws IOException, ProtocolException {
        //we need the size for efficient downloading
        HttpURLConnection connection = (HttpURLConnection) input.openConnection();
        connection.setConnectTimeout(METADATA_CONNECTION_TIMEOUT);
        connection.setRequestMethod("HEAD");
        long fileSize = -1;
        try (InputStream inStream = connection.getInputStream()) {
            fileSize = connection.getContentLengthLong();
        } finally {
            connection.disconnect();
        }
        
        connection = (HttpURLConnection) input.openConnection();
        connection.setConnectTimeout(METADATA_CONNECTION_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes=0-100");
        try (InputStream inStream = connection.getInputStream()) {
            if(connection.getResponseCode() != 206){
                fileSize = -1;
            }
        } finally {
            connection.disconnect();
        }
        return fileSize;
    }

    private VideoMetadataDTO checkMetadataByPartialDownloads(URL input, long fileSize)
            throws IOException, ProtocolException {
        byte[] start = new byte[REQUIRED_SIZE_IN_BYTES];
        byte[] end = new byte[REQUIRED_SIZE_IN_BYTES];
        downloadPartOfFile(input, start, "bytes=0-" + REQUIRED_SIZE_IN_BYTES);
        downloadPartOfFile(input, end, "bytes=" + (fileSize - REQUIRED_SIZE_IN_BYTES) + "-");
        long skipped = fileSize - start.length - end.length;
        return checkMetadata(start, end, skipped);
    }

    private void downloadPartOfFile(URL input, byte[] store, String range) throws IOException, ProtocolException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) input.openConnection();
        connection.setConnectTimeout(METADATA_CONNECTION_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", range);
        try (InputStream inStream = connection.getInputStream()) {
            DataInputStream dataInStream = new DataInputStream(inStream);
            dataInStream.readFully(store);
        } finally {
            connection.disconnect();
        }
    }

    private VideoMetadataDTO checkMetadataByFullFileDownload(URL input)
            throws ParserConfigurationException, SAXException, IOException {
        try (IsoFile isof = new IsoFile(Channels.newChannel(input.openStream()))) {
            boolean canDownload = true;
            Date recordStartedTimer = determineRecordingStart(isof);
            Duration duration = determineDuration(isof);
            boolean spherical = determine360(isof);
            removeTempFiles(isof);
            return new VideoMetadataDTO(canDownload, duration, spherical, recordStartedTimer, "");
        }
    }

    @Override
    public VideoMetadataDTO checkMetadata(byte[] start, byte[] end, Long skipped) {
        ensureUserCanManageMedia();
        File tmp = null;
        boolean spherical = false;
        Duration duration = null;
        Date recordStartedTimer = null;
        String message = "";
        try {
            tmp = createFileFromData(start, end, skipped);
            try (IsoFile isof = new IsoFile(tmp)) {
                recordStartedTimer = determineRecordingStart(isof);
                spherical = determine360(isof);
                duration = determineDuration(isof);
                removeTempFiles(isof);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in video analysis ", e);
            message = e.getMessage();
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
        return new VideoMetadataDTO(true, duration, spherical, recordStartedTimer, message);
    }

    /**
     * Some boxes (we don't actually need to read) create internal tempfiles, we delete them here, as the VM could run
     * quite long
     */
    private void removeTempFiles(IsoFile isof) {
        List<MediaDataBox> boxesWithTempFiles = isof.getBoxes(MediaDataBox.class, true);
        for (MediaDataBox box : boxesWithTempFiles) {
            try {
                Field field = box.getClass().getDeclaredField("dataFile");
                field.setAccessible(true);
                File data = (File) field.get(box);
                data.delete();
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                logger.log(Level.WARNING, "Could not delete mp4 temp files", e);
            }
        }
    }

    /**
     * Creates a dummy mp4 file, only containing the start and the end, the middle is filled with 0 so that all file
     * internal pointers from start to end are working
     */
    private File createFileFromData(byte[] start, byte[] end, Long skipped) throws IOException, FileNotFoundException {
        File tmp;
        tmp = File.createTempFile("upload", "metadataCheck");
        try (FileOutputStream fw = new FileOutputStream(tmp)) {
            fw.write(start);
            while (skipped > 0) {
                // ensure that no absurd amount of memory is required, and that more then 4gb files can be analysed
                if (skipped > 10000000) {
                    byte[] dummy = new byte[10000000];
                    fw.write(dummy);
                    skipped = skipped - 10000000;
                } else {
                    byte[] dummy = new byte[skipped.intValue()];
                    fw.write(dummy);
                    skipped = skipped - skipped.intValue();
                }
            }
            fw.write(end);
        }
        return tmp;
    }

    private boolean determine360(IsoFile isof) throws ParserConfigurationException, SAXException, IOException {
        boolean spherical = false;
        UserBox uuidBox = Path.getPath(isof, "moov[0]/trak[0]/uuid");
        if (uuidBox != null) {
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
        return spherical;
    }

    private Duration determineDuration(IsoFile isof) {
        Duration duration = null;
        MovieBox mbox = isof.getMovieBox();
        if (mbox != null) {
            MovieHeaderBox mhb = mbox.getMovieHeaderBox();
            if (mhb != null) {
                duration = new MillisecondsDurationImpl(mhb.getDuration()*1000 / mhb.getTimescale());
            }
        }
        return duration;
    }
    
    private Date determineRecordingStart(IsoFile isof) {
        Date creationTime = null;
        MovieBox mbox = isof.getMovieBox();
        if (mbox != null) {
            MovieHeaderBox mhb = mbox.getMovieHeaderBox();
            if (mhb != null) {
                creationTime = mhb.getCreationTime();
            }
        }
        return creationTime;
    }

    @Override
    public MediaTrack getMediaTrackByUrl(String url) {
        MediaTrack result = null;
        for (MediaTrack mtrack : racingEventService().getAllMediaTracks()) {
            if (url.equals(mtrack.url)) {
                result = mtrack;
                break;
            }
        }
        return result;
    }
}
