package com.sap.sailing.selenium.api.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;

public class ApiContext {

    private static final Logger logger = Logger.getLogger(ApiContext.class.getName());

    protected final Client client;
    protected final String token;
    private final String contextRoot;
    private final String context;

    private ApiContext(String contextRoot, String context, String token) {
        this.contextRoot = contextRoot;
        this.token = token;
        this.context = context;
        client = new Client();
    }

    public static ApiContext createApiContext(String contextRoot, String context, String username, String password) {
        Authenticator authenticator = new Authenticator(contextRoot);
        String token = authenticator.authForToken(username, password);
        return new ApiContext(contextRoot, context, token);
    }

    public static ApiContext createAnonymousApiContext(String contextRoot, String context) {
        return new ApiContext(contextRoot, context, null);
    }

    private WebResource getWebResource() {
        return client.resource(contextRoot + context);
    }

    public JSONObject post(String url, Map<String, String> queryParams) {
        WebResource wres = getWebResource().path(url);
        wres = addQueryParams(wres, queryParams);
        String result;
        try {
            result = auth(wres.getRequestBuilder()).post(String.class);
        } catch (UniformInterfaceException e) {
            String error = "API POST request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw new RuntimeException(error);
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public JSONObject post(String url, Map<String, String> queryParams, Map<String, String> formParams) {
        WebResource wres = getWebResource().path(url);
        wres = addQueryParams(wres, queryParams);
        Form form = new Form();
        if (formParams != null) {
            formParams.forEach(form::putSingle);
        }
        String result;
        try {
            result = auth(wres.getRequestBuilder()).entity(form).post(String.class);
        } catch (UniformInterfaceException e) {
            String error = "API POST request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw new RuntimeException(error);
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public JSONObject post(String url, Map<String, String> queryParams, JSONObject body) {
        WebResource wres = getWebResource().path(url);
        wres = addQueryParams(wres, queryParams);
        String result;
        try {
            result = auth(wres.getRequestBuilder()).entity(body.toJSONString(), MediaType.APPLICATION_JSON).post(String.class);
        } catch (UniformInterfaceException e) {
            String error = "API POST request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw new RuntimeException(error);
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public JSONObject put(String url, Map<String, String> queryParams, JSONObject body) {
        WebResource wres = getWebResource().path(url);
        wres = addQueryParams(wres, queryParams);
        String result;
        try {
            result = auth(wres.getRequestBuilder()).entity(body.toJSONString(), MediaType.APPLICATION_JSON)
                    .put(String.class);
        } catch (UniformInterfaceException e) {
            String error = "API PUT request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw new RuntimeException(error);
        }
        return (JSONObject) JSONValue.parse(result);
    }

    public void delete(String url) {
        WebResource wres = getWebResource().path(url);
        try {
            auth(wres.getRequestBuilder()).delete();
        } catch (UniformInterfaceException e) {
            String error = "API DELETE request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw new RuntimeException(error);
        }
    }

    public JSONObject get(String url) {
        return (JSONObject) getObject(url, null);
    }

    public JSONObject get(String url, Map<String, String> queryParams) {
        return (JSONObject) getObject(url, queryParams);
    }

    public JSONArray getList(String url) {
        return (JSONArray) getObject(url, null);
    }

    public JSONArray getList(String url, Map<String, String> queryParams) {
        return (JSONArray) getObject(url, queryParams);
    }

    private Object getObject(String url, Map<String, String> queryParams) {
        String result;
        WebResource wres = getWebResource().path(url);
        wres = addQueryParams(wres, queryParams);
        try {
            result = auth(wres.getRequestBuilder()).get(String.class);
        } catch (UniformInterfaceException e) {
            int rc = e.getResponse().getStatus();
            if (rc == 204) {
                logger.info("API GET request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                        + "<no content>");
                return null;
            } else {
                String error = "API GET request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                        + e.getResponse().getEntity(String.class);
                logger.severe(error);
                throw new RuntimeException(error);
            }

        }
        return JSONValue.parse(result);
    }

    private WebResource addQueryParams(WebResource wres, Map<String, String> queryParams) {
        if (queryParams != null) {
            for (Entry<String, String> e : queryParams.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    wres = wres.queryParam(e.getKey(), e.getValue());
                }
            }
        }
        return wres;
    }

    private Builder auth(Builder builder) {
        if (token != null) {
            return builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

}