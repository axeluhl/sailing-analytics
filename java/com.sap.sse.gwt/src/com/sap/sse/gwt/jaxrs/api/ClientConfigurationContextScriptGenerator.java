package com.sap.sse.gwt.jaxrs.api;

import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.simple.JSONObject;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sse.branding.BrandingConfigurationService;
import com.sap.sse.branding.BrandingConfigurationService.BrandingConfigurationProperty;
import com.sap.sse.gwt.jaxrs.RestServletContainer;

@Path("/client_configuration_context")
public class ClientConfigurationContextScriptGenerator {
    @Context
    ServletContext servletContext;

    private ServiceTracker<BrandingConfigurationService, BrandingConfigurationService> getBrandingConfigurationServiceTracker() {
        @SuppressWarnings("unchecked")
        ServiceTracker<BrandingConfigurationService, BrandingConfigurationService> tracker = (ServiceTracker<BrandingConfigurationService, BrandingConfigurationService>) servletContext
                .getAttribute(RestServletContainer.BRANDING_CONFIGURATION_SERVICE_TRACKER_NAME);
        return tracker;
    }
    
    @GET
    @Produces("text/javascript;charset=UTF-8")
    public Response generateClientConfigurationContextScript() {
        final Response result;
        final BrandingConfigurationService brandingService = getBrandingConfigurationServiceTracker().getService();
        if (brandingService == null) {
            result = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Branding configuration service is not available.")
                           .build();
        } else {
            final StringBuilder scriptBuilder = new StringBuilder();
            scriptBuilder.append("document.clientConfigurationContext=");
            final JSONObject jsonObject = new JSONObject();
            for (final Entry<BrandingConfigurationProperty, Object> brandingConfigurationPropertyAndValue : brandingService.getBrandingConfigurationPropertiesForJspContext().entrySet()) {
                jsonObject.put(brandingConfigurationPropertyAndValue.getKey().getPropertyName(), brandingConfigurationPropertyAndValue.getValue());
            }
            scriptBuilder.append(jsonObject.toJSONString());
            scriptBuilder.append(";");
            result = Response.ok(scriptBuilder.toString()).build();
        }
        return result;
    }
}
