package com.sap.sailing.selenium.api.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.selenium.core.TestEnvironmentConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;

public class ApiRequest<I, O> {

    private static final Logger logger = Logger.getLogger(ApiRequest.class.getName());

    private final Context ctx;
    private final String url;
    private final RequestMethod requestMethod;
    private I payload;
    private Map<String, Iterable<String>> queryParams;
    private Map<String, String> requestHeaders;

    private Class<?> resultType;
    private String authorizationToken;

    private ApiRequest(final Context ctx, final String url, final RequestMethod requestMethod) {
        this.ctx = ctx;
        this.url = url;
        this.requestMethod = requestMethod;
    }

    public ApiRequest<I, O> auth(final String userName, final String passwd) {
        auth(() -> {
            Authenticator authenticator = new Authenticator(ctx.getRootContext());
            return authenticator.authForToken(userName, passwd);
        });
        return this;
    }

    public ApiRequest<I, O> auth(final String authorizationToken) {
        auth(() -> authorizationToken);
        return this;
    }

    public ApiRequest<I, O> auth(final Supplier<String> tokenSupplier) {
        this.authorizationToken = tokenSupplier.get();
        return this;
    }

    public ApiRequest<I, O> payload(final I payload) {
        this.payload = payload;
        return this;
    }

    @SuppressWarnings("unchecked")
    public ApiRequest<Form, O> formParam(final String field, final String value) {
        if (this.payload == null) {
            this.payload = (I) new Form();
        }
        ((Form) this.payload).putSingle(field, value);
        return (ApiRequest<Form, O>) this;
    }

    public ApiRequest<I, O> queryParam(final String key, final String value) {
        if (queryParams == null) {
            queryParams = new TreeMap<>();
        }
        if (!queryParams.containsKey(key)) {
            queryParams.put(key, new ArrayList<String>());
        }
        ((List<String>) queryParams.get(key)).add(value);
        return this;
    }

    public ApiRequest<I, O> header(final String headerName, final String headerValue) {
        if (requestHeaders == null) {
            requestHeaders = new TreeMap<>();
        }
        requestHeaders.put(headerName, headerValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <J extends JsonWrapper> ApiRequest<I, J> wrapJsonResult(Class<J> jsonWrapper) {
        this.resultType = jsonWrapper;
        return (ApiRequest<I, J>) this;
    }

    @SuppressWarnings("unchecked")
    public O run() {
        WebResource wres = ctx.getWebResource().path(url);
        if (queryParams != null) {
            for (String key : queryParams.keySet()) {
                for (String value : queryParams.get(key)) {
                    wres = wres.queryParam(key, value);
                }
            }
        }
        String result = null;
        Builder requestBuilder = wres.getRequestBuilder();
        try {
            if (authorizationToken != null) {
                requestBuilder.header("Authorization", "Bearer " + authorizationToken);
            }
            if (payload != null) {
                if (payload instanceof JsonWrapper) {
                    requestBuilder = requestBuilder.entity(((JsonWrapper) payload).getJson().toJSONString(),
                            MediaType.APPLICATION_JSON_TYPE);
                } else if (payload instanceof JSONObject) {
                    requestBuilder = requestBuilder.entity(((JSONObject) payload).toJSONString(),
                            MediaType.APPLICATION_JSON_TYPE);
                } else {
                    requestBuilder = requestBuilder.entity(payload);
                }
            }
            if (requestHeaders != null) {
                for (Entry<String, String> header : requestHeaders.entrySet()) {
                    requestBuilder.header(header.getKey(), header.getValue());
                }
            }
            logger.info("request: " + requestMethod.name() + " " + wres.getURI().toString());
            switch (requestMethod) {
            case GET:
                result = requestBuilder.get(String.class);
                break;
            case PUT:
                result = requestBuilder.put(String.class);
                break;
            case POST:
                result = requestBuilder.post(String.class);
                break;
            case DELETE:
                result = requestBuilder.delete(String.class);
                break;
            default:
                break;
            }
        } catch (UniformInterfaceException e) {
            String error = "API POST request " + url + " failed (rc=" + e.getResponse().getStatus() + "): "
                    + e.getResponse().getEntity(String.class);
            logger.severe(error);
            throw HttpException.forResponse(e.getResponse(), error).orElse(e);
        }
        if (result == null) {
            return null;
        } else if (isOfType(this.resultType, JsonWrapper.class)) {
            try {
                return (O) resultType.getConstructor(JSONObject.class).newInstance(JSONValue.parse(result));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                logger.severe("JSONWrapper type '" + resultType.getName() + "' lacks constructor "
                        + resultType.getName() + "(JSONObject)");
                throw new RuntimeException("cannot construct " + resultType.getName(), e);
            }
        } else {
            return (O) result;
        }

    }

    private static boolean isOfType(Class<?> typeToCheck, Class<?> superType) {
        if (typeToCheck == superType) {
            return true;
        } else if (typeToCheck == null) {
            return false;
        } else {
            return isOfType(typeToCheck.getSuperclass(), superType);
        }
    }

    public enum Context {
        SAILING("sailingserver"), SECURITY("security"), SHARED_SAILING("sharedsailingserver");

        protected final String contextRoot = TestEnvironmentConfiguration.getInstance().getContextRoot();

        private final String contextUrl;

        private final Client client = new Client();

        private Context(final String contextUrl) {
            this.contextUrl = contextUrl.replaceAll("/", "");
        }

        public String url() {
            return getRootContext() + this.contextUrl;
        }

        public <I, O> ApiRequest<I, O> get(final String url) {
            return new ApiRequest<I, O>(this, url, RequestMethod.GET);
        }

        public <I, O> ApiRequest<I, O> post(final String url, final I payload) {
            return new ApiRequest<I, O>(this, url, RequestMethod.POST).payload(payload);
        }

        public <I, O> ApiRequest<I, O> post(final String url) {
            return new ApiRequest<I, O>(this, url, RequestMethod.POST);
        }

        public <I, O> ApiRequest<I, O> put(final String url, final I payload) {
            return new ApiRequest<I, O>(this, url, RequestMethod.PUT).payload(payload);
        }

        public <I, O> ApiRequest<I, O> delete(final String url) {
            return new ApiRequest<I, O>(this, url, RequestMethod.DELETE);
        }

        private String getRootContext() {
            return contextRoot;
        }

        public WebResource getWebResource() {
            return client.resource(url());
        }
    }

    public enum RequestMethod {
        GET, PUT, POST, DELETE;
    }
}
