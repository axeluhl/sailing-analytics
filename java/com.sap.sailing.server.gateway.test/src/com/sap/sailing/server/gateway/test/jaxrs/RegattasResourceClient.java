package com.sap.sailing.server.gateway.test.jaxrs;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

@SuppressWarnings("unused")
public class RegattasResourceClient {
    
    private static final String REGATTA_NAME = "TestTwiese";
    private static final String REGATTA_SECRET = "6e318fa0-fece-11e8-8d04-1d7aaf7786a1";
    private static final String COMPETITOR_DEVICE_UUID1 = "00000001-1111-2222-3333-444444444444";
    private static final String COMPETITOR_DEVICE_UUID2 = "00000002-1111-2222-3333-444444444444";
    private static final String COMPETITOR_DEVICE_UUID3 = "944cc19c-9676-5f50-af6e-e3eb4f12886d";

    
    //private String serverUrl = "http://localhost:8888/sailingserver";
    private String serverUrl = "https://my.sapsailing.com/sailingserver";
    private String secServerUrl = "https://my.sapsailing.com/security";
    private final WebResource webResource;
    private final WebResource secResource;
    
    private String token; 

    public static void main(String... args) {
        RegattasResourceClient client = new RegattasResourceClient();
        //client.auth("admin", "admin");
        client.auth("<mail@thomaswiese.de>", "orange");
        //String name = "TestEventOpen_" + createRandomString(5);
        //System.out.println(name);
        //client.createEvent(name);
        //client.updateOrCreateSeries(name, name);
        //client.addRaceColumns(name, "Default");
        
        //client.token = null;
        client.addCompetitorToRegatta("<Kai> openRegattaTest1", REGATTA_SECRET, COMPETITOR_DEVICE_UUID3);
        
        //client.printRegatta("<Kai> openRegattaTest1");
        
        //client.printEvents();
        //client.printEvent("5af41a71-834d-4f88-a432-fae993a1f3c8");
        //Set<String> competitorIds = client.readCompetitors(REGATTA_NAME);
        //competitorIds.forEach(c -> System.out.println(competitorIds));
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
                .queryParam("competitorName", regattaName)  //is required, when not authenticated
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
        try {
            String result = wresBuilder.post(String.class);
            System.out.println(result);
        } catch (UniformInterfaceException e) {
            System.out.println(e.getResponse().getEntity(String.class));
            throw e;
        }
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
    
    public void createEvent(String name) {
        String createEventURI = "api/v1/events/createEvent";
        WebResource wres = webResource.path(createEventURI);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        
        Form form = new Form();
        form.putSingle("eventName", name);
        form.putSingle("venuename", "Test");
        form.putSingle("ispublic", "true");
        form.putSingle("createleaderboardgroup", "true");
        form.putSingle("createregatta", "true");
        form.putSingle("boatclassname", "TP52");
        form.putSingle("numberofraces", "1");
        form.putSingle("canBoatsOfCompetitorsChangePerRace", "false");
        form.putSingle("competitorRegistrationType", CompetitorRegistrationType.OPEN_UNMODERATED.name());
        form.putSingle("secret", REGATTA_SECRET);
        try {
            String result = wresBuilder.entity(form).post(String.class);
            System.out.println(result);
        } catch (UniformInterfaceException e) {
            System.out.println(e.getResponse().toString());
            throw e;
        }
        
    }
    
    
    public void addRaceColumns(String regattaname, String seriesname) {
        
        String createEventURI = "api/v1/regatta/{regattaname}/addracecolumns";
        WebResource wres = webResource.path(createEventURI.replace("{regattaname}", regattaname))
                .queryParam("numberofraces", "1")
                .queryParam("toseries", seriesname);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        try {
            String result = wresBuilder.post(String.class);
            System.out.println(result);
        } catch (UniformInterfaceException e) {
            System.out.println(e.getResponse().getEntity(String.class));
            throw e;
        }
    }
    
    public void updateOrCreateSeries(String reaggataName, String seriesname) {
        String json = "{'regattaName': '" + reaggataName + "', 'seriesName': '" + seriesname + "', 'seriesNameNew': '" + seriesname + "', 'isMedal': 'false', 'isFleetsCanRunInParallel': 'false', 'startsWithZeroScore': 'false', 'firstColumnIsNonDiscardableCarryForward': 'false', 'hasSplitFleetContiguousScoring': 'false', fleets: []}";
        System.out.println(json);
        String createEventURI = "api/v1/regatta/updateOrCreateSeries";
        WebResource wres = webResource.path(createEventURI);
        WebResource.Builder wresBuilder = wres.getRequestBuilder();
        if (token != null) {
            wresBuilder = wresBuilder.header("Authorization", "Bearer " + token);
        }
        String result = wresBuilder.entity(json).post(String.class);
        System.out.println(result);
    }
    
    
    public static String createRandomString(int length) {
        String randomString = Stream.generate(Math::random).map(r -> (int) (r * 100))
                .filter(i -> (i > 47 && i < 58 || i > 64 && i < 90)).limit(length)
                .map(i -> String.valueOf((char) i.intValue())).collect(Collectors.joining());
        return randomString;
    }
}
