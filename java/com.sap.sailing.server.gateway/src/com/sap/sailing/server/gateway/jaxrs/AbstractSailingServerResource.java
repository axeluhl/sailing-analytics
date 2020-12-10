package com.sap.sailing.server.gateway.jaxrs;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.shared.server.gateway.jaxrs.SharedAbstractSailingServerResource;

public abstract class AbstractSailingServerResource extends SharedAbstractSailingServerResource {
    protected <T> T[] getServices(Class<T> clazz) {
        final ServiceTracker<T, T> tracker = getServiceTracker(clazz);
        final Object[] objectServices = tracker.getServices();
        @SuppressWarnings("unchecked")
        final T[] services = (T[]) Array.newInstance(clazz, objectServices.length);
        System.arraycopy(objectServices, 0, services, 0, services.length);
        tracker.close();
        return services;
    }

    protected <T> ServiceTracker<T, T> getServiceTracker(Class<T> clazz) {
        final BundleContext context = getBundleContext();
        final ServiceTracker<T, T> tracker = new ServiceTracker<T, T>(context, clazz, null);
        tracker.open();
        return tracker;
    }

    private Iterable<ScoreCorrectionProvider> getScoreCorrectionProviders() {
        return Arrays.asList(getServices(ScoreCorrectionProvider.class));
    }
    
    protected Optional<ScoreCorrectionProvider> getScoreCorrectionProvider(final String scoreCorrectionProviderName) {
        return StreamSupport.stream(getScoreCorrectionProviders().spliterator(), /* parallel */ false).
                filter(scp->scp.getName().equals(scoreCorrectionProviderName)).findAny();
    }
    
    protected TrackedRace findTrackedRace(Regatta regatta, String raceName) {
        final TrackedRace trackedRace;
        final RaceDefinition race = findRaceByName(regatta, raceName);
        if (race != null) {
            DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRace = trackedRegatta.getExistingTrackedRace(race);
            } else {
                trackedRace = null;
            }
        } else {
            trackedRace = null;
        }
        return trackedRace;
    }
    
    protected static Double roundDouble(Double value, int places) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
    
    public RaceLogTrackingAdapter getRaceLogTrackingAdapter() {
        return getService(RaceLogTrackingAdapterFactory.class).getAdapter(getService().getBaseDomainFactory());
    }
}
