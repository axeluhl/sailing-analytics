package com.sap.sailing.server.gateway.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.server.gateway.deserialization.impl.DataImportProgressJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.MasterDataImportResultJsonDeserializer;
import com.sap.sailing.server.gateway.interfaces.CompareServersResult;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sailing.server.gateway.interfaces.SailingServer;
import com.sap.sailing.server.gateway.jaxrs.api.EventsResource;
import com.sap.sailing.server.gateway.jaxrs.api.LeaderboardGroupsResource;
import com.sap.sailing.server.gateway.jaxrs.api.MasterDataImportResource;
import com.sap.sailing.server.gateway.serialization.LeaderboardGroupConstants;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MasterDataImportResultJsonSerializer;
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
    public URL getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getBearerToken() {
        return bearerToken;
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
    public Iterable<UUID> getEventIds() throws ClientProtocolException, IOException, ParseException {
        final URL eventsUrl = new URL(baseUrl, GATEWAY_URL_PREFIX+EventsResource.V1_EVENTS);
        final HttpGet getEvents = new HttpGet(eventsUrl.toString());
        final JSONArray jsonResponse = (JSONArray) getJsonParsedResponse(getEvents);
        return Util.map(jsonResponse, o->UUID.fromString(((JSONObject) o).get(EventBaseJsonSerializer.FIELD_ID).toString()));
    }

    @Override
    public MasterDataImportResult importMasterData(SailingServer from, Iterable<UUID> leaderboardGroupIds,
            boolean override, boolean compress, boolean exportWind, boolean exportDeviceConfigs,
            boolean exportTrackedRacesAndStartTracking, Optional<UUID> progressTrackingUuid) throws ClientProtocolException, IOException, ParseException {
        final URL mdiUrl = new URL(baseUrl, GATEWAY_URL_PREFIX + MasterDataImportResource.V1_MASTERDATAIMPORT);
        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(MasterDataImportResource.REMOTE_SERVER_URL_FORM_PARAM, from.getBaseUrl().toString()));
        params.add(new BasicNameValuePair(MasterDataImportResource.REMOTE_SERVER_BEARER_TOKEN_FORM_PARAM, from.getBearerToken()));
        for (final UUID leaderboardGroupId : leaderboardGroupIds) {
            params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.LEADERBOARDGROUP_UUID_FORM_PARAM, leaderboardGroupId.toString()));
        }
        params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.OVERRIDE_FORM_PARAM, Boolean.toString(override)));
        params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.COMPRESS_FORM_PARAM, Boolean.toString(compress)));
        params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.EXPORT_WIND_FORM_PARAM, Boolean.toString(exportWind)));
        params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.EXPORT_DEVICE_CONFIGS_FORM_PARAM, Boolean.toString(exportDeviceConfigs)));
        params.add(new BasicNameValuePair(MasterDataImportResultJsonSerializer.EXPORT_TRACKED_RACES_AND_START_TRACKING_FORM_PARAM, Boolean.toString(exportTrackedRacesAndStartTracking)));
        progressTrackingUuid.ifPresent(ptid->params.add(new BasicNameValuePair(MasterDataImportResource.PROGRSS_TRACKING_UUID_FORM_PARAM, ptid.toString())));
        final HttpPost importMasterData = new HttpPost(mdiUrl.toString());
        importMasterData.setEntity(EntityBuilder.create().setParameters(params).build());
        final JSONObject jsonResponse = (JSONObject) getJsonParsedResponse(importMasterData);
        return new MasterDataImportResultJsonDeserializer().deserialize(jsonResponse);
    }

    @Override
    public DataImportProgress getMasterDataImportProgress(UUID progressTrackingUuid) throws ClientProtocolException, IOException, ParseException {
        final URL progressUrl = new URL(baseUrl, GATEWAY_URL_PREFIX + MasterDataImportResource.V1_MASTERDATAIMPORT + MasterDataImportResource.PROGRESS+
                "?"+MasterDataImportResource.PROGRESS_TRACKING_UUID+"="+progressTrackingUuid);
        final HttpPost getMasterDataImportProgress = new HttpPost(progressUrl.toString());
        final JSONObject jsonResponse = (JSONObject) getJsonParsedResponse(getMasterDataImportProgress);
        return new DataImportProgressJsonDeserializer().deserialize(jsonResponse);
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
