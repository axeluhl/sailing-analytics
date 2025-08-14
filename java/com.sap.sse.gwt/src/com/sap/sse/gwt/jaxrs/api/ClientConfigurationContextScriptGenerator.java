package com.sap.sse.gwt.jaxrs.api;

import java.util.Map.Entry;
import java.util.Optional;

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
import com.sap.sse.gwt.client.context.data.ClientConfigurationContextDataJSO;
import com.sap.sse.gwt.client.context.impl.ClientConfigurationContextDataFactoryImpl;
import com.sap.sse.gwt.jaxrs.RestServletContainer;
import com.sap.sse.gwt.shared.ClientConfiguration;

/**
 * Generates a JavaScript snippet that assigns an object to {@code document.clientConfigurationContext} from where it is
 * read by the GWT client code to configure the client application. The reading counterpart is the
 * {@link ClientConfigurationContextDataFactoryImpl} class in conjunction with the {@link ClientConfiguration} and the
 * {@link ClientConfigurationContextDataJSO} class.<p>
 * 
 * The script should be used in a {@code script} tag in the HTML page that starts the GWT client code, like this:
 * <pre>
 *     &lt;script type="text/javascript" src="/gwt-base/api/client_configuration_context"&gt;&lt;/script&gt;
 * </pre>
 * The {@link ClientConfiguration} instance can then be obtained from the GWT client code using
 * {@link ClientConfiguration#getInstance()}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
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
            for (final Entry<BrandingConfigurationProperty, Object> brandingConfigurationPropertyAndValue : brandingService
                    .getBrandingConfigurationPropertiesForJspContext(Optional.empty() /* TODO */ ).entrySet()) {
                jsonObject.put(brandingConfigurationPropertyAndValue.getKey().getPropertyName(), brandingConfigurationPropertyAndValue.getValue());
            }
            scriptBuilder.append(jsonObject.toJSONString());
            scriptBuilder.append(";");
            result = Response.ok(scriptBuilder.toString()).build();
        }
        return result;
    }
}
