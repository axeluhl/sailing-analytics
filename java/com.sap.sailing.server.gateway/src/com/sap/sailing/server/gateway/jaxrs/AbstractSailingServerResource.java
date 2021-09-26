package com.sap.sailing.server.gateway.jaxrs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;

public abstract class AbstractSailingServerResource extends SharedAbstractSailingServerResource {
    private Iterable<ScoreCorrectionProvider> getScoreCorrectionProviders() {
        return Arrays.asList(getServices(ScoreCorrectionProvider.class));
    }
    
    protected Optional<ScoreCorrectionProvider> getScoreCorrectionProvider(final String scoreCorrectionProviderName) {
        return StreamSupport.stream(getScoreCorrectionProviders().spliterator(), /* parallel */ false).
                filter(scp->scp.getName().equals(scoreCorrectionProviderName)).findAny();
    }
    
    protected static Double roundDouble(Double value, int places) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
