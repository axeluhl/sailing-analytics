package com.sap.sailing.server.gateway.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardGroupsResource;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sse.common.Util;
import com.sap.sse.util.LaxRedirectStrategyForAllRedirectResponseCodes;

public class SailingServerImpl implements SailingServer {
    private final String GATEWAY_URL_PREFIX = "sailingserver/api";
    private final String bearerToken;
    private final URL baseUrl;
    
    public SailingServerImpl(URL baseUrl, String bearerToken) {
        super();
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
    }

    @Override
    public Iterable<UUID> getLeaderboardGroupIds() throws ClientProtocolException, IOException, ParseException {
        final URL leaderboardGroupsUrl = new URL(baseUrl, GATEWAY_URL_PREFIX+LeaderboardGroupsResource.V1_LEADERBOARDGROUPS+LeaderboardGroupsResource.IDENTIFIABLE);
        final HttpGet getLeaderboards = new HttpGet(leaderboardGroupsUrl.toString());
        final JSONArray jsonResponse = (JSONArray) getJsonParsedResponse(getLeaderboards);
        return Util.map(jsonResponse, o->UUID.fromString(((JSONObject) o).get(LeaderboardGroupConstants.ID).toString()));
    }

    private Object getJsonParsedResponse(final HttpUriRequest request) throws IOException, ClientProtocolException, ParseException {
        authenticate(request);
        final HttpClient client = createHttpClient();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        client.execute(request).getEntity().writeTo(bos);
        final Object jsonParseResult = new JSONParser().parse(new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
        return jsonParseResult;
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategyForAllRedirectResponseCodes()).build();
    }
    
    private void authenticate(HttpRequest request) {
        if (bearerToken != null) {
            request.setHeader("Authorization", "Bearer: "+bearerToken);
        }
    }

    @Override
    public Iterable<UUID> getEventIds() {
        // TODO Implement SailingServerImpl.getEventIds(...)
        return null;
    }

    @Override
    public MasterDataImportResult importMasterData(SailingServer from, Iterable<UUID> leaderboardGroupIds) {
        // TODO Implement SailingServerImpl.importMasterData(...)
        return null;
    }

    @Override
    public CompareServersResult compareServers(SailingServer a, Optional<SailingServer> b) {
        // TODO Implement SailingServerImpl.compareServers(...)
        return null;
    }

    @Override
    public void addRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds) {
        // TODO Implement SailingServerImpl.addRemoteServerReference(...)

    }

    @Override
    public void removeRemoteServerReference(SailingServer referencedServer, Optional<Set<UUID>> eventIds) {
        // TODO Implement SailingServerImpl.removeRemoteServerReference(...)

    }

}
