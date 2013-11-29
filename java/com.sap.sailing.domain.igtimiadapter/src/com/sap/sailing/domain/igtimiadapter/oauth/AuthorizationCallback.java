package com.sap.sailing.domain.igtimiadapter.oauth;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.igtimiadapter.impl.Activator;

@Path(AuthorizationCallback.V1)
public class AuthorizationCallback {
    private static final Logger logger = Logger.getLogger(AuthorizationCallback.class.getName());
    
    private static final String AUTHORIZATIONCALLBACK = "/authorizationcallback";
    static final String V1 = "/v1";
    static final String V1_AUTHORIZATIONCALLBACK = V1 + AUTHORIZATIONCALLBACK;
    private final IgtimiConnectionFactory connectionFactory;
    
    public AuthorizationCallback() {
        connectionFactory = Activator.getInstance().getConnectionFactory();
    }

    public static String getRedirectUrl() {
        // TODO how to find out the external address of this server?
        return "http://127.0.0.1:8888/igtimi"+V1_AUTHORIZATIONCALLBACK;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path(AUTHORIZATIONCALLBACK)
    public Response obtainAccessToken(@QueryParam("code") String code) throws ClientProtocolException, IOException, IllegalStateException, ParseException, URISyntaxException {
        Response result;
        try {
            connectionFactory.obtainAccessTokenFromAuthorizationCode(code);
            result = Response.ok().build();
        } catch (Exception e) {
            result = Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
            logger.log(Level.SEVERE, "Error trying to obtain access token from authentication code "+code, e);
        }
        return result;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("uri")
    public Response getWithUri(@Context UriInfo info) {
        return Response.ok().build();
    }
}
