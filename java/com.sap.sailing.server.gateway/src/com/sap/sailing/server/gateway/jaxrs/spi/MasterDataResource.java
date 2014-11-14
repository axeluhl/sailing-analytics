package com.sap.sailing.server.gateway.jaxrs.spi;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.StreamingOutput;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;

@Path("/v1/masterdata/leaderboardgroups")
public class MasterDataResource extends AbstractSailingServerResource {
    
    private static final Logger logger = Logger.getLogger(MasterDataResource.class.getName());
    
    @GET
    @Produces("application/x-java-serialized-object")
    public Response getMasterDataByLeaderboardGroups(@QueryParam("names[]") List<String> leaderboardGroupNames,
            @QueryParam("compress") Boolean compress, @QueryParam("exportWind") Boolean exportWind)
            throws UnsupportedEncodingException {
        final long startTime = System.currentTimeMillis();
        logger.info("Masterdataexport has started");
        if (compress == null) {
            compress = false;
        }
        if (exportWind == null) {
            exportWind = true;
        }
        logger.info(String.format("Masterdataexport gzip compression is turned %s", compress ? "on" : "off"));
        Map<String, LeaderboardGroup> allLeaderboardGroups = getService().getLeaderboardGroups();

        Set<LeaderboardGroup> groupsToExport = new HashSet<LeaderboardGroup>();

        if (leaderboardGroupNames.size() > 0) {
            for (String name : leaderboardGroupNames) {
                LeaderboardGroup group = allLeaderboardGroups.get(name);
                if (group != null) {
                    groupsToExport.add(group);
                }
            }
        } else {
            groupsToExport = new HashSet<LeaderboardGroup>();
            groupsToExport.addAll(allLeaderboardGroups.values());
        }

        final List<Serializable> competitorIds = new ArrayList<Serializable>();

        for (LeaderboardGroup lg : groupsToExport) {
            for (Leaderboard leaderboard : lg.getLeaderboards()) {
                for (Competitor competitor : leaderboard.getAllCompetitors()) {
                    competitorIds.add(competitor.getId());
                }
            }
        }

        final TopLevelMasterData masterData = new TopLevelMasterData(groupsToExport,
                getService().getAllEvents(), getService().getPersistentRegattasForRaceIDs(), getService()
                .getAllMediaTracks(), exportWind);

        ResponseBuilder resp;
        if (compress) {
            StreamingOutput streamingOutput = new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    ObjectOutputStream objectOutputStream = null;
                    try {
                        GZIPOutputStream gzip = new GZIPOutputStream(output);
                        OutputStream outputStreamWithByteCounter = new ByteCountOutputStreamDecorator(gzip);
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
            };
            resp = Response.ok(streamingOutput).header("Content-Encoding", "gzip");
        } else {
            StreamingOutput streamingOutput = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    ObjectOutputStream objectOutputStream = null;
                    try {
                        OutputStream outputStreamWithByteCounter = new ByteCountOutputStreamDecorator(output);
                        objectOutputStream = new ObjectOutputStream(outputStreamWithByteCounter);
                        masterData.setMasterDataExportFlagOnRaceColumns(true);

                        writeObjects(competitorIds, masterData, objectOutputStream);
                    } finally {
                        objectOutputStream.close();
                        masterData.setMasterDataExportFlagOnRaceColumns(false);
                        long timeToExport = System.currentTimeMillis() - startTime;
                        logger.info(String.format("Took %s ms to finish masterdataexport", timeToExport));
                    }
                }
            };
            resp = Response.ok(streamingOutput);
        }

        Response builtResponse = resp.build();
        long timeToExport = System.currentTimeMillis() - startTime;
        logger.info(String.format("Took %s ms to start masterdataexport-streaming.", timeToExport));
        return builtResponse;
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
