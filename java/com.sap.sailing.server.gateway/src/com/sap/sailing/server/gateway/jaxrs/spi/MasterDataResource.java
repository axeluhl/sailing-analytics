package com.sap.sailing.server.gateway.jaxrs.spi;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.security.shared.User;

@Path("/v1/masterdata/leaderboardgroups")
public class MasterDataResource extends AbstractSailingServerResource {
    
    private static final Logger logger = Logger.getLogger(MasterDataResource.class.getName());
    
    @GET
    @Produces("application/x-java-serialized-object")
    public Response getMasterDataByLeaderboardGroups(@QueryParam("names[]") List<String> requestedLeaderboardGroups,
            @QueryParam("compress") Boolean compress, @QueryParam("exportWind") Boolean exportWind, @QueryParam("exportDeviceConfigs") Boolean exportDeviceConfigs)
            throws UnsupportedEncodingException {

        User user = getSecurityService().getCurrentUser();
        if (user == null) {
            return Response.status(Status.FORBIDDEN).build();
        }

        final long startTime = System.currentTimeMillis();
        logger.info("Masterdataexport has started");
        if (compress == null) {
            compress = false;
        }
        if (exportWind == null) {
            exportWind = true;
        }
        if (exportDeviceConfigs == null) {
            exportDeviceConfigs = false;
        }
        logger.info(String.format("Masterdataexport gzip compression is turned %s", compress ? "on" : "off"));
        Map<String, LeaderboardGroup> allLeaderboardGroups = getService().getLeaderboardGroups();

        Set<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();

        if (requestedLeaderboardGroups.isEmpty()) {
            // add all visible
            for (LeaderboardGroup group : allLeaderboardGroups.values()) {
                if (getSecurityService().hasCurrentUserReadPermission(group)) {
                    groupsToExport.add(group);
                }
            }
        } else {
            // add all requested, that are visible
            for (String name : requestedLeaderboardGroups) {
                LeaderboardGroup group = allLeaderboardGroups.get(name);
                if (group != null) {
                    if (getSecurityService().hasCurrentUserReadPermission(group)) {
                        groupsToExport.add(group);
                    }
                }
            }
        }

        final List<Serializable> competitorIds = new ArrayList<Serializable>();

        for (LeaderboardGroup lg : groupsToExport) {
            for (Leaderboard leaderboard : lg.getLeaderboards()) {
                if (getSecurityService().hasCurrentUserReadPermission(leaderboard)) {
                    for (Competitor competitor : leaderboard.getAllCompetitors()) {
                        competitorIds.add(competitor.getId());
                    }
                }
            }
        }
        Map<DeviceConfigurationMatcher, DeviceConfiguration> deviceConfigurations = new HashMap<>();
        if (exportDeviceConfigs) {
            for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> deviceconfig : getAllDeviceConfigs()
                    .entrySet()) {
                // FIXME here permission check?
                deviceConfigurations.put(deviceconfig.getKey(), deviceconfig.getValue());
            }
        }

        ArrayList<Event> events = new ArrayList<>();
        for (Event event : getService().getAllEvents()) {
            if (getSecurityService().hasCurrentUserReadPermission(event)) {
                events.add(event);
            }
        }

        ArrayList<MediaTrack> mediaTracks = new ArrayList<>();
        for (MediaTrack mediaTrack : getService().getAllMediaTracks()) {
            if (getSecurityService().hasCurrentUserReadPermission(mediaTrack)) {
                mediaTracks.add(mediaTrack);
            }
        }

        Map<String, Regatta> regattaRaceIds = new HashMap<>();
        for (Entry<String, Regatta> regattaRaceMap : getService().getPersistentRegattasForRaceIDs().entrySet()) {
            if (getSecurityService().hasCurrentUserReadPermission(regattaRaceMap.getValue())) {
                regattaRaceIds.put(regattaRaceMap.getKey(), regattaRaceMap.getValue());
            }
        }
        final TopLevelMasterData masterData = new TopLevelMasterData(groupsToExport,
                events, regattaRaceIds, mediaTracks,
                getService().getSensorFixStore(), exportWind, deviceConfigurations);
        final StreamingOutput streamingOutput;
        if (compress) {
            streamingOutput = new CompressingStreamingOutput(masterData, competitorIds, startTime);
        } else {
            streamingOutput = new NonCompressingStreamingOutput(masterData, competitorIds, startTime);
        }
        final ResponseBuilder resp = Response.ok(streamingOutput);
        if (compress) {
            resp.header("Content-Encoding", "gzip");
        }
        Response builtResponse = resp.build();
        long timeToExport = System.currentTimeMillis() - startTime;
        logger.info(String.format("Took %s ms to start masterdataexport-streaming.", timeToExport));
        return builtResponse;
    }
    
    private Map<DeviceConfigurationMatcher, DeviceConfiguration> getAllDeviceConfigs() {
        Map<DeviceConfigurationMatcher, DeviceConfiguration> configs = getService().getAllDeviceConfigurations();
        return configs;
    }

    private abstract class AbstractStreamingOutput implements StreamingOutput {
        private final TopLevelMasterData masterData;
        private final List<Serializable> competitorIds;
        private final long startTime;

        protected AbstractStreamingOutput(TopLevelMasterData masterData, List<Serializable> competitorIds, long startTime) {
            super();
            this.masterData = masterData;
            this.competitorIds = competitorIds;
            this.startTime = startTime;
        }
        
        protected abstract OutputStream wrapOutputStream(OutputStream outputStream) throws IOException;

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            ObjectOutputStream objectOutputStream = null;
            try {
                OutputStream gzipOrNot = wrapOutputStream(output);
                OutputStream outputStreamWithByteCounter = new ByteCountOutputStreamDecorator(gzipOrNot);
                objectOutputStream = new ObjectOutputStream(outputStreamWithByteCounter);
                masterData.setMasterDataExportFlagOnRaceColumns(true);
                // Actual start of streaming
                writeObjects(competitorIds, masterData, objectOutputStream);
            } finally {
                objectOutputStream.close();
                masterData.setMasterDataExportFlagOnRaceColumns(false);
            }
            long timeToExport = System.currentTimeMillis() - startTime;
            logger.info(String.format("Took %s ms to finish masterdataexport", timeToExport));
        }
    }
    
    private class NonCompressingStreamingOutput extends AbstractStreamingOutput {
        protected NonCompressingStreamingOutput(TopLevelMasterData masterData, List<Serializable> competitorIds,
                long startTime) {
            super(masterData, competitorIds, startTime);
        }

        @Override
        protected OutputStream wrapOutputStream(OutputStream outputStream) {
            return outputStream;
        }
    }
    
    private class CompressingStreamingOutput extends AbstractStreamingOutput {
        protected CompressingStreamingOutput(TopLevelMasterData masterData, List<Serializable> competitorIds,
                long startTime) {
            super(masterData, competitorIds, startTime);
        }

        @Override
        protected OutputStream wrapOutputStream(OutputStream outputStream) throws IOException {
            return new GZIPOutputStream(outputStream);
        }
    }

    private void writeObjects(final List<Serializable> competitorIds, final TopLevelMasterData masterData,
            ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(competitorIds);
        objectOutputStream.writeObject(masterData.getAllRegattas());
        objectOutputStream.writeObject(masterData);
    }

    private class ByteCountOutputStreamDecorator extends FilterOutputStream {

        private long byteCount = 0;

        public ByteCountOutputStreamDecorator(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b) throws IOException {
            byteCount++;
            super.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            byteCount = byteCount + len;
            super.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            byteCount++;
            super.write(b);
        }

        @Override
        public void close() throws IOException {
            logger.info(String.format("Uncompressed data size of masterdataexport: %s bytes", byteCount));
            super.close();
        }

    }

}
