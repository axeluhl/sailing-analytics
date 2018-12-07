package com.sap.sailing.server.gateway.test.jaxrs;

import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class RegattasResourceClient {
    
    private static final String REGATTA_NAME = "TestTwiese";
    private static final String REGATTA_SECRET = "NKKL2034WN3YHTVJGYCW";
    private static final String COMPETITOR_DEVICE_UUID1 = "00000001-1111-2222-3333-444444444444";
    private static final String COMPETITOR_DEVICE_UUID2 = "00000002-1111-2222-3333-444444444444";
    
    private String serverUrl = "http://localhost:8888/sailingserver";
    private String secServerUrl = "http://localhost:8888/security";
    private final WebResource webResource;
    private final WebResource secResource;
    
    private String token; 

    public static void main(String... args) {
        RegattasResourceClient client = new RegattasResourceClient();
        //client.auth("admin", "admin");
        //client.printRegatta(REGATTA_NAME);
        client.auth("twiese", "twiese");
        //client.printEvents();
        //client.printEvent("5af41a71-834d-4f88-a432-fae993a1f3c8");
        Set<String> competitorIds = client.readCompetitors(REGATTA_NAME);
        competitorIds.forEach(c -> System.out.println(competitorIds));
        //competitorIds.forEach(c -> client.removeCompetitor(REGATTA_NAME, c));
        
        //client.auth("admin", "admin");
        //client.addCompetitorToRegatta(REGATTA_NAME, REGATTA_SECRET, COMPETITOR_DEVICE_UUID1);
        //client.addCompetitorToRegatta(REGATTA_NAME, REGATTA_SECRET, COMPETITOR_DEVICE_UUID2);
        
    }
    
    public RegattasResourceClient() {
        Client client = Client.create();
        this.webResource = client.resource(serverUrl);
        this.secResource = client.resource(secServerUrl);
    }
    
    public void printEvents() {
        String eventsURI = "/api/v1/events";
        WebResource wres = webResource.path(eventsURI);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.get(String.class);
        System.out.println(result);
    }
    
    public void printEvent(String id) {
        String eventsURI = "/api/v1/events";
        WebResource wres = webResource.path(eventsURI + "/" + id);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.get(String.class);
        System.out.println(result);
    }
    
    public void printRegattas() {
        String regattasURI = "/api/v1/regattas";
        WebResource wres = webResource.path(regattasURI);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.get(String.class);
        System.out.println(result);
    }
    
    public void printRegatta(String name) {
        String regattaURI = "api/v1/regattas/{regattaName}";
        WebResource wres =  webResource.path(regattaURI.replace("{regattaName}", name));
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.get(String.class);
        System.out.println(result);
    }
    
    public void addCompetitorToRegatta(String regattaName, String regattaSecret, String deviceUuid) {
        String addAndCreateCompetitorURI = "api/v1/regattas/{regattaName}/competitors/createandadd";
        WebResource wres = webResource.path(addAndCreateCompetitorURI.replace("{regattaName}", regattaName))
                .queryParam("boatclass", "Cadet")
                .queryParam("deviceUuid", deviceUuid)
                .queryParam("competitorName", "TestClient User")  //is required, when not authenticated
                .queryParam("secret", regattaSecret)
                .queryParam("nationalityIOC", "GER")
                .queryParam("displayColor", "#00FF00")
                .queryParam("flagImageURI", "file:///tmp/flag.jpg")
                .queryParam("teamImageURI", "file:///tmp/flag.jpg")
                .queryParam("competitorShortName", "xxx")
                .queryParam("competitorEmail", "mail@thomaswiese.de");
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.post(String.class);
        System.out.println(result);
    }
    
    public Set<String> readCompetitors(String regattaName) {
        String competitorsURI = "api/v1/regattas/{regattaName}/competitors";
        WebResource wres = webResource.path(competitorsURI.replace("{regattaName}", regattaName));
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.get(String.class);
        System.out.println(result);
        return getCompetitorIdsFromCompetitorsResponse(result);
    }
    
    public void removeCompetitor(String regattaName, String competitorId) {
        System.out.println(competitorId);
        String competitorsRemoveURI = "api/v1/regattas/{regattaName}/competitors/{competitorId}/remove";
        WebResource wres = webResource.path(competitorsRemoveURI.replace("{regattaName}", regattaName).replace("{competitorId}", competitorId))
                .queryParam("secret", REGATTA_SECRET);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.post(String.class);
        System.out.println(result);
    }
    
    public void hello() {
        String helloURI = "/api/restsecurity/hello";
        String result = secResource.path(helloURI).get(String.class);
        System.out.println(result);
    }
    
    public void auth(String username, String password) {
        //String loginURI = "/api/restsecurity/login";
        String tokenURI = "/api/restsecurity/access_token";
        Form form = new Form();
        form.putSingle("username", username);
        form.putSingle("password", password);
        String tokenJson = secResource.path(tokenURI).entity(form).post(String.class);
        JSONObject obj = (JSONObject) JSONValue.parse(tokenJson);
        String token = (String) obj.get("access_token");
        System.out.println(token);
        this.token = token;
    }
    
    public Set<String> getCompetitorIdsFromCompetitorsResponse(String comeptitorsJson) {
        JSONArray arr = (JSONArray) JSONValue.parse(comeptitorsJson);
        return arr.stream().map(a -> ((JSONObject) a).get("id").toString()).collect(Collectors.toSet());
    }
    
    public String getCompetitorsIdFromCompetitorJson(JSONObject competitorJson) {
        return (String) competitorJson.get("id");
    }
    
}
