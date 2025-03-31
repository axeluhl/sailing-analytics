package com.sap.sailing.shared.server.gateway.jaxrs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.UnauthorizedException;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sse.common.Util;

public abstract class AbstractSailingServerResource extends SharedAbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(AbstractSailingServerResource.class.getName());
    private Iterable<ScoreCorrectionProvider> getScoreCorrectionProviders() {
        return Arrays.asList(getServices(ScoreCorrectionProvider.class));
    }
    
    protected Optional<ScoreCorrectionProvider> getScoreCorrectionProvider(final String scoreCorrectionProviderName) {
        return StreamSupport.stream(getScoreCorrectionProviders().spliterator(), /* parallel */ false).
                filter(scp->scp.getName().equals(scoreCorrectionProviderName)).findAny();
    }
    
    /**
     * Valid combinations: user+password; bearer; nothing
     */
    protected boolean validateAuthenticationParameters(String user, String password, String bearer) {
        return (((Util.hasLength(user) && Util.hasLength(password) && !Util.hasLength(bearer))
                || (!Util.hasLength(user) && !Util.hasLength(password) && Util.hasLength(bearer)))
                || (!Util.hasLength(user) && !Util.hasLength(password) && !Util.hasLength(bearer)));
    }

    protected Response returnInternalServerError(Throwable e) {
        final Response response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
        logger.severe(e.getMessage());
        return response;
    }

    protected Response badRequest(String message) {
        final Response response = Response.status(Status.BAD_REQUEST).entity(message).build();
        return response;
    }

    protected Response returnUnauthorized(UnauthorizedException e) {
        final Response response = Response.status(Status.UNAUTHORIZED).build();
        logger.warning(e.getMessage() + " for user: " + getSecurityService().getCurrentUser());
        return response;
    }

    protected static Double roundDouble(Double value, int places) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
