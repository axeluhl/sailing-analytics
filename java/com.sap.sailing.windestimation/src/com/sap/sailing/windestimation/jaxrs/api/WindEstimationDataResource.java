package com.sap.sailing.windestimation.jaxrs.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.shiro.SecurityUtils;

import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.windestimation.integration.ExportedModels;
import com.sap.sailing.windestimation.integration.WindEstimationModelsUpdateOperation;
import com.sap.sailing.windestimation.jaxrs.AbstractWindEstimationDataResource;
import com.sap.sse.ServerInfo;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;

/**
 * Resource which manages wind estimation model metadata. Data export and import are supported.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
@Path("/windestimation_data")
public class WindEstimationDataResource extends AbstractWindEstimationDataResource {
    @GET
    @Produces("application/octet-stream;charset=UTF-8")
    public Response getInternalModelData() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getWindEstimationFactoryServiceImpl().serializeForInitialReplication(bos);
        bos.close();
        return Response.ok(new ByteArrayInputStream(bos.toByteArray()))
                .header("Content-Type", "application/octet-stream").build();
    }

    @POST
    @Produces("text/plain")
    public Response postInternalModelData(InputStream inputStream) throws Exception {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.WIND_ESTIMATION_MODELS.getStringPermissionForTypeRelativeIdentifier(
                DefaultActions.UPDATE, new TypeRelativeObjectIdentifier(ServerInfo.getName())));
        ObjectInputStream ois = getWindEstimationFactoryServiceImpl()
                .createObjectInputStreamResolvingAgainstCache(inputStream);
        ExportedModels exportedModels = (ExportedModels) ois.readObject();
        WindEstimationModelsUpdateOperation windEstimationModelsUpdateOperation = new WindEstimationModelsUpdateOperation(
                exportedModels);
        getWindEstimationFactoryServiceImpl().apply(windEstimationModelsUpdateOperation);
        return Response.ok("Wind estimation models accepted").build();
    }
}
