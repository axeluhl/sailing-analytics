package com.sap.sailing.gwt.ui.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.shiro.SecurityUtils;
import org.mp4parser.AbstractBoxParser;
import org.mp4parser.IsoFile;
import org.mp4parser.PropertyBoxParserImpl;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.SAXException;

import com.google.gwt.thirdparty.json.JSONArray;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.VideoMetadataDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
import com.sap.sailing.gwt.ui.client.MediaService;
import com.sap.sailing.media.mp4.MP4MediaParser;
import com.sap.sailing.media.mp4.MP4ParserFakeFile;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class MediaServiceImpl extends RemoteServiceServlet implements MediaService {
    private String YOUTUBE_V3_API_KEY = "AIzaSyBzCJ9cxb9_PPzuYfrHIEdSRtR631b64Xs";

    private static final Logger logger = Logger.getLogger(MediaServiceImpl.class.getName());

    private static final int METADATA_CONNECTION_TIMEOUT = 10000;

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
            if (fileSize > 2 * MP4MediaParser.REQUIRED_SIZE_IN_BYTES) {
                response = checkMetadataByPartialDownloads(input, fileSize);
            } else {
                response = checkMetadataByFullFileDownload(input);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in video analysis ", e);
            response = new VideoMetadataDTO(false, null, false, null, "");
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
        byte[] start = new byte[MP4MediaParser.REQUIRED_SIZE_IN_BYTES];
        byte[] end = new byte[MP4MediaParser.REQUIRED_SIZE_IN_BYTES];
        downloadPartOfFile(input, start, "bytes=0-" + MP4MediaParser.REQUIRED_SIZE_IN_BYTES);
        downloadPartOfFile(input, end, "bytes=" + (fileSize - MP4MediaParser.REQUIRED_SIZE_IN_BYTES) + "-");
        long skipped = fileSize - start.length - end.length;
        return checkMetadata(start, end, skipped);
    }

    private void downloadPartOfFile(URL input, byte[] store, String range) throws IOException, ProtocolException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) input.openConnection();
        connection.setConnectTimeout(METADATA_CONNECTION_TIMEOUT);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", range);
        try (final InputStream inStream = connection.getInputStream()) {
            try (final DataInputStream dataInStream = new DataInputStream(inStream)) {
                dataInStream.readFully(store);
            }
        } finally {
            connection.disconnect();
        }
    }

    private VideoMetadataDTO checkMetadataByFullFileDownload(URL input)
            throws ParserConfigurationException, SAXException, IOException {
        final File tmp = File.createTempFile("upload", "metadataCheck");
        VideoMetadataDTO result;
        try {
            try (final ReadableByteChannel rbc = Channels.newChannel(input.openStream())) {
                try (final FileOutputStream fos = new FileOutputStream(tmp)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    try (final MP4ParserFakeFile inputFile = new MP4ParserFakeFile(tmp)) {
                        Files.delete(tmp.toPath());
                        result = checkMetadata(inputFile);
                    } catch (Exception e) {
                        result = new VideoMetadataDTO(true, null, false, null, e.getMessage());
                    }
                } catch (Exception e) {
                    result = new VideoMetadataDTO(false, null, false, null, e.getMessage());
                }
            }
        } finally {
            // delete the file if not already deleted in main path
            tmp.delete();
        }
        return result;
    }

    @Override
    public VideoMetadataDTO checkMetadata(byte[] start, byte[] end, Long skipped) {
        ensureUserCanManageMedia();
        VideoMetadataDTO result;
        try (MP4ParserFakeFile input = new MP4ParserFakeFile(start, end, skipped)) {
            result = checkMetadata(input);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in video analysis ", e);
            result = new VideoMetadataDTO(true, null, false, null, e.getMessage());
        }
        return result;
    }

    private VideoMetadataDTO checkMetadata(MP4ParserFakeFile input)
            throws ParserConfigurationException, SAXException, IOException {
        AbstractBoxParser boxParserImpl = new PropertyBoxParserImpl();
        boxParserImpl.skippingBoxes(new String[] { "mdat" });
        try (IsoFile isof = new IsoFile(input, boxParserImpl)) {
            Date recordStartedTimer = MP4MediaParser.determineRecordingStart(isof);
            boolean spherical = MP4MediaParser.determine360(isof);
            Duration duration = new MillisecondsDurationImpl(MP4MediaParser.determineDurationInMillis(isof));
            return new VideoMetadataDTO(true, duration, spherical, recordStartedTimer, "");
        }
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

    @Override
    public VideoMetadataDTO checkYoutubeMetadata(String videoId) throws UnsupportedEncodingException {
        ensureUserCanManageMedia();
        boolean canDownload = false;
        String message = "";
        Duration duration = null;
        if (videoId.isEmpty()) {
            message = "Empty id";
        } else {
            // sanitize, as we inject it into an url with our api key!
            videoId = URLEncoder.encode(videoId, StandardCharsets.UTF_8.name());
            try {
                URL apiURL = new URL(
                        "https://www.googleapis.com/youtube/v3/videos?id=" + videoId + "&key=" + YOUTUBE_V3_API_KEY
                                + "&part=snippet,contentDetails&fields=items(snippet/title,contentDetails/duration)");
                URLConnection connection = apiURL.openConnection();
                connection.setRequestProperty("Referer", "http://mediaservice.sapsailing.com/");
                connection.setConnectTimeout(METADATA_CONNECTION_TIMEOUT);
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String pageText = reader.lines().collect(Collectors.joining("\n"));
                    JSONObject jsonAnswer = new JSONObject(pageText);
                    final JSONArray dataArray = jsonAnswer.getJSONArray("items");
                    if (dataArray.length() > 0) {
                        final JSONObject item = dataArray.getJSONObject(0);
                        message = item.getJSONObject("snippet").getString("title");
                        String rawDuration = item.getJSONObject("contentDetails").getString("duration");
                        duration = new MillisecondsDurationImpl(java.time.Duration.parse(rawDuration).toMillis());
                    }
                    canDownload = true;
                } catch (JSONException e) {
                    message = e.getMessage();
                    logger.log(Level.WARNING, "Error in youtube metadata call", e);
                }
            } catch (IOException e) {
                message = e.getMessage();
                logger.log(Level.WARNING, "Error in youtube metadata call", e);
            }
        }
        return new VideoMetadataDTO(canDownload, duration, false, null, message);
    }
}
