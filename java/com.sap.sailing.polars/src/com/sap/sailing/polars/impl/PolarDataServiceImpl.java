package com.sap.sailing.polars.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import org.apache.commons.math.analysis.polynomials.PolynomialFunction;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.confidence.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationSettingsImpl;
import com.sap.sailing.domain.common.impl.WindSpeedSteppingWithMaxDistance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.polars.PolarsChangedListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.PolarDataOperation;
import com.sap.sailing.polars.aggregation.PolarFixAggregator;
import com.sap.sailing.polars.aggregation.SimplePolarFixRaceInterval;
import com.sap.sailing.polars.data.PolarFix;
import com.sap.sailing.polars.generation.PolarSheetGenerator;
import com.sap.sailing.polars.mining.BearingClusterGroup;
import com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor;
import com.sap.sailing.polars.mining.MovingAverageProcessorImpl;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.mining.SpeedClusterGroupFromWindSteppingCreator;
import com.sap.sailing.polars.mining.SpeedRegressionPerAngleClusterProcessor;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.replication.OperationExecutionListener;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.impl.OperationWithResultWithIdWrapper;
import com.sap.sse.replication.impl.ReplicableWithObjectInputStream;
import com.sap.sse.util.SmartFutureCache;

/**
 * Uses two chained {@link SmartFutureCache}s. One to store {@link PolarFix}es extracted from {@link TrackedRace}s and
 * the other one for storing one polar sheet per boat class. This enables quick access to desired measures like optimal
 * beat angles.
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class PolarDataServiceImpl implements PolarDataService, ReplicableWithObjectInputStream<PolarDataServiceImpl, PolarDataOperation<?>>{

    private static final Logger logger = Logger.getLogger(PolarDataServiceImpl.class.getSimpleName());
    
    private PolarDataMiner polarDataMiner;
    
    private final ConcurrentHashMap<OperationExecutionListener<PolarDataServiceImpl>, OperationExecutionListener<PolarDataServiceImpl>> operationExecutionListeners;
    
    /**
     * The master from which this replicable is currently replicating, or <code>null</code> if this replicable is not
     * currently replicated from any master.
     */
    private ReplicationMasterDescriptor replicatingFromMaster;

    private final Set<OperationWithResult<PolarDataServiceImpl, ?>> operationsSentToMasterForReplication;
    
    private ThreadLocal<Boolean> currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster = ThreadLocal
            .withInitial(() -> false);

    private DomainFactory domainFactory;
    
    public PolarDataServiceImpl() {
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createBackendPolarSettings();
        ClusterGroup<Bearing> angleClusterGroup = createAngleClusterGroup();
        WindSpeedSteppingWithMaxDistance stepping = settings.getWindSpeedStepping();
        final ClusterGroup<Speed> speedClusterGroup = SpeedClusterGroupFromWindSteppingCreator
                .createSpeedClusterGroupFrom(stepping);
        MovingAverageProcessorImpl movingAverageProcessor = new MovingAverageProcessorImpl(speedClusterGroup);
        CubicRegressionPerCourseProcessor cubicRegressionPerCourseProcessor = new CubicRegressionPerCourseProcessor();
        SpeedRegressionPerAngleClusterProcessor speedRegressionPerAngleClusterProcessor = new SpeedRegressionPerAngleClusterProcessor(
                angleClusterGroup);
        this.polarDataMiner = new PolarDataMiner(settings, movingAverageProcessor, cubicRegressionPerCourseProcessor,
                speedRegressionPerAngleClusterProcessor, speedClusterGroup, angleClusterGroup);
        this.operationsSentToMasterForReplication = new HashSet<>();
        this.operationExecutionListeners = new ConcurrentHashMap<>();
    }

    private ClusterGroup<Bearing> createAngleClusterGroup() {
        return new BearingClusterGroup(0, 180, 5);
    }

    @Override
    public SpeedWithConfidence<Void> getSpeed(BoatClass boatClass, Speed windSpeed, Bearing trueWindAngle) throws NotEnoughDataHasBeenAddedException {
        if (polarDataMiner == null) {
            throw new NotEnoughDataHasBeenAddedException("Polar Data Miner is currently unavailable. Maybe we are in the process of replication initial load?");
        }
        return polarDataMiner.estimateBoatSpeed(boatClass, windSpeed, trueWindAngle);
    }

    @Override
    public Set<SpeedWithBearingWithConfidence<Void>> getAverageTrueWindSpeedAndAngleCandidates(BoatClass boatClass,
            Speed speedOverGround, LegType legType, Tack tack) {
        return polarDataMiner.estimateTrueWindSpeedAndAngleCandidates(boatClass, speedOverGround, legType, tack);
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack, boolean useRegressionForSpeed) throws NotEnoughDataHasBeenAddedException {
        if (polarDataMiner == null) {
            throw new NotEnoughDataHasBeenAddedException("Polar Data Miner is currently unavailable. Maybe we are in the process of replication initial load?");
        }
        SpeedWithBearingWithConfidence<Void> averageSpeedAndCourseOverGround = polarDataMiner
                .getAverageSpeedAndCourseOverGround(boatClass, windSpeed, legType, useRegressionForSpeed);
        if (tack == Tack.PORT) {
            //Negative twa
            DegreeBearingImpl bearing = new DegreeBearingImpl(-averageSpeedAndCourseOverGround.getObject().getBearing()
                    .getDegrees());
            KnotSpeedWithBearingImpl speed = new KnotSpeedWithBearingImpl(averageSpeedAndCourseOverGround.getObject()
                    .getKnots(), bearing);
            averageSpeedAndCourseOverGround = new SpeedWithBearingWithConfidenceImpl<Void>(speed,
                    averageSpeedAndCourseOverGround.getConfidence(), null);
        }
        return averageSpeedAndCourseOverGround;
    }


    @Override
    public PolarSheetsData generatePolarSheet(Set<TrackedRace> trackedRaces, PolarSheetGenerationSettings settings,
            Executor executor) throws InterruptedException, ExecutionException {
        Set<PolarFix> fixes;
        PolarFixAggregator aggregator = new PolarFixAggregator(new SimplePolarFixRaceInterval(trackedRaces), settings,
                executor);
        aggregator.startPolarFixAggregation();
        fixes = aggregator.getAggregationResultAsSingleList();
        PolarSheetGenerator generator = new PolarSheetGenerator(fixes, settings);
        return generator.generate();
    }

    @Override
    public PolarSheetsData getPolarSheetForBoatClass(BoatClass boatClass) {
        return polarDataMiner.createFullSheetForBoatClass(boatClass);
    }

    @Override
    public Set<BoatClass> getAllBoatClassesWithPolarSheetsAvailable() {
        return polarDataMiner.getAvailableBoatClasses();
    }

    @Override
    public void competitorPositionChanged(final GPSFixMoving fix, final Competitor competitor,
            final TrackedRace createdTrackedRace) {
        polarDataMiner.addFix(fix, competitor, createdTrackedRace);
    }

    @Override
    public int[] getDataCountsForWindSpeed(BoatClass boatClass, Speed windSpeed, int startAngleInclusive, int endAngleExclusive) {
        return polarDataMiner.getDataCountsForWindSpeed(boatClass, windSpeed, startAngleInclusive, endAngleExclusive);
    }

    @Override
    public double getConfidenceForTackJibeSpeedRatio(Speed intoTackSpeed, Speed intoJibeSpeed, BoatClass boatClass) {
        return Math.min(1., 0.5*intoJibeSpeed.getKnots()/intoTackSpeed.getKnots());
    }

    @Override
    public Pair<Double, SpeedWithBearingWithConfidence<Void>> getManeuverLikelihoodAndTwsTwa(BoatClass boatClass, Speed speedAtManeuverStart, double courseChangeDeg,
            ManeuverType maneuverType) {
        assert maneuverType == ManeuverType.TACK || maneuverType == ManeuverType.JIBE;
        SpeedWithBearingWithConfidence<Void> closestTwsTwa = getClosestTwaTws(maneuverType, speedAtManeuverStart, courseChangeDeg, boatClass);
        final Pair<Double, SpeedWithBearingWithConfidence<Void>> result;
        if (closestTwsTwa == null) {
            result = new Pair<>(0.0, null);
        } else {
            double minDiffDeg = Math.abs(Math.abs(Math.abs(closestTwsTwa.getObject().getBearing().getDegrees() * 2)
                    - Math.abs(courseChangeDeg)));
            result = new Pair<>(1. / (1. + (minDiffDeg / 10.) * (minDiffDeg / 10.)), closestTwsTwa);
        }
        return result;
    }

    private SpeedWithBearingWithConfidence<Void> getClosestTwaTws(ManeuverType type, Speed speedAtManeuverStart, double courseChangeDeg, BoatClass boatClass) {
        assert type == ManeuverType.TACK || type == ManeuverType.JIBE;
        double minDiff = Double.MAX_VALUE;
        SpeedWithBearingWithConfidence<Void> closestTwsTwa = null;
        for (SpeedWithBearingWithConfidence<Void> trueWindSpeedAndAngle : getAverageTrueWindSpeedAndAngleCandidates(
                boatClass, speedAtManeuverStart,
                type == ManeuverType.TACK ? LegType.UPWIND : LegType.DOWNWIND,
                type == ManeuverType.TACK ? courseChangeDeg >= 0 ? Tack.PORT : Tack.STARBOARD
                                          : courseChangeDeg >= 0 ? Tack.STARBOARD : Tack.PORT)) {
            double diff = Math.abs(trueWindSpeedAndAngle.getObject().getBearing().getDegrees()*2)-Math.abs(courseChangeDeg);
            if (diff < minDiff) {
                minDiff = diff;
                closestTwsTwa = trueWindSpeedAndAngle;
            }
        }
        return closestTwsTwa;
    }

    @Override
    public PolynomialFunction getSpeedRegressionFunction(BoatClass boatClass, LegType legType)
            throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.getSpeedRegressionFunction(boatClass, legType);
    }
    
    @Override
    public PolynomialFunction getAngleRegressionFunction(BoatClass boatClass, LegType legType)
            throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.getAngleRegressionFunction(boatClass, legType);
    }
    
    @Override
    public PolynomialFunction getSpeedRegressionFunction(BoatClass boatClass, double trueWindAngle)
            throws NotEnoughDataHasBeenAddedException {
        return polarDataMiner.getSpeedRegressionFunction(boatClass, Math.abs(trueWindAngle));
    }

    @Override
    public void raceFinishedLoading(TrackedRace race) {
        polarDataMiner.raceFinishedTracking(race);
    }

    @Override
    public BearingWithConfidence<Void> getManeuverAngle(BoatClass boatClass, ManeuverType maneuverType, Speed windSpeed)
            throws NotEnoughDataHasBeenAddedException {
        if (maneuverType != ManeuverType.TACK && maneuverType != ManeuverType.JIBE) {
            throw new IllegalArgumentException("ManeuverType needs to be tack or jibe.");
        }
        LegType legType = maneuverType == ManeuverType.TACK ? LegType.UPWIND : LegType.DOWNWIND;
        if (boatClass == null || windSpeed == null) {
            throw new IllegalArgumentException("Boatclass and windspeed cannot be null.");
        }
        if (polarDataMiner == null) {
            throw new NotEnoughDataHasBeenAddedException("Polar Data Miner is currently unavailable. Maybe we are in the process of replication initial load?");
        }
        SpeedWithBearingWithConfidence<Void> speed = polarDataMiner.getAverageSpeedAndCourseOverGround(boatClass, windSpeed, legType, true);
        Bearing bearing = new DegreeBearingImpl(speed.getObject().getBearing().getDegrees() * 2);
        BearingWithConfidence<Void> bearingWithConfidence = new BearingWithConfidenceImpl<Void>(bearing, speed.getConfidence(),
                null);
        return bearingWithConfidence;
    }

    @Override
    public SpeedWithBearingWithConfidence<Void> getAverageSpeedWithBearing(BoatClass boatClass, Speed windSpeed,
            LegType legType, Tack tack) throws NotEnoughDataHasBeenAddedException {
        return getAverageSpeedWithBearing(boatClass, windSpeed, legType, tack, true);
    }

    @Override
    public void insertExistingFixes(TrackedRace trackedRace) {
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            for (GPSFixMoving fix : trackedRace.getTrack(competitor).getFixes()) {
                competitorPositionChanged(fix, competitor, trackedRace);
            }
        }
    }

    @Override
    public void registerListener(BoatClass boatClass, PolarsChangedListener listener) {
        polarDataMiner.registerListener(boatClass, listener);
    }

    @Override
    public void unregisterListener(BoatClass boatClass, PolarsChangedListener listener) {
        polarDataMiner.unregisterListener(boatClass, listener);
    }

    @Override
    public void addOperationExecutionListener(OperationExecutionListener<PolarDataServiceImpl> listener) {
        operationExecutionListeners.put(listener, listener);
    }

    @Override
    public void removeOperationExecutionListener(OperationExecutionListener<PolarDataServiceImpl> listener) {
        operationExecutionListeners.remove(listener);
    }

    @Override
    public void clearReplicaState() throws MalformedURLException, IOException, InterruptedException {
        polarDataMiner = null;
    }
    
    @Override
    public boolean isCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster() {
        return currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.get();
    }

    @Override
    public void setCurrentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster(boolean b) {
        currentlyFillingFromInitialLoadOrApplyingOperationReceivedFromMaster.set(b);
        
    }

    @Override
    public Serializable getId() {
        return getClass().getName();
    }

    @Override
    public ObjectInputStream createObjectInputStreamResolvingAgainstCache(InputStream is) throws IOException {
        ObjectInputStream ois;
        if (domainFactory != null) {
            ois = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(is);
        } else {
            // TODO ensure that domainfactory is set here. Otherwise there can be issues with duplicate domain objects
            logger.warning("PolarDataService didn't have a domain factory attached. Replication to this service could fail.");
            ois = new ObjectInputStream(is);
        }
        return ois;
    }

    @Override
    public void initiallyFillFromInternal(ObjectInputStream is) throws IOException, ClassNotFoundException,
            InterruptedException {
        PolarSheetGenerationSettings backendPolarSettings = (PolarSheetGenerationSettings) is.readObject();
        //MovingAverageProcessor movingAverageProcessor = (MovingAverageProcessor) is.readObject();
        PolarSheetGenerationSettings settings = PolarSheetGenerationSettingsImpl.createBackendPolarSettings();
        WindSpeedSteppingWithMaxDistance stepping = settings.getWindSpeedStepping();
        final ClusterGroup<Speed> speedClusterGroup = SpeedClusterGroupFromWindSteppingCreator
                .createSpeedClusterGroupFrom(stepping);
        MovingAverageProcessorImpl movingAverageProcessor = new MovingAverageProcessorImpl(speedClusterGroup);
        
        CubicRegressionPerCourseProcessor cubicRegressionPerCourseProcessor = (CubicRegressionPerCourseProcessor) is.readObject();
        SpeedRegressionPerAngleClusterProcessor speedRegressionPerAngleClusterProcessor = (SpeedRegressionPerAngleClusterProcessor) is.readObject();

        polarDataMiner = new PolarDataMiner(backendPolarSettings, movingAverageProcessor,
                cubicRegressionPerCourseProcessor, speedRegressionPerAngleClusterProcessor,
                movingAverageProcessor.getSpeedCluster(), speedRegressionPerAngleClusterProcessor.getAngleCluster());
    }

    @Override
    public void serializeForInitialReplicationInternal(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(polarDataMiner.getPolarSheetGenerationSettings());
        //objectOutputStream.writeObject(polarDataMiner.getMovingAverageProcessor());
        objectOutputStream.writeObject(polarDataMiner.getCubicRegressionPerCourseProcessor());
        objectOutputStream.writeObject(polarDataMiner.getSpeedRegressionPerAngleClusterProcessor());
    }

    @Override
    public Iterable<OperationExecutionListener<PolarDataServiceImpl>> getOperationExecutionListeners() {
        return operationExecutionListeners.keySet();
    }

    @Override
    public ReplicationMasterDescriptor getMasterDescriptor() {
        return replicatingFromMaster;
    }

    @Override
    public void startedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = master;
    }

    @Override
    public void stoppedReplicatingFrom(ReplicationMasterDescriptor master) {
        this.replicatingFromMaster = null;
    }

    @Override
    public boolean hasSentOperationToMaster(OperationWithResult<PolarDataServiceImpl, ?> operation) {
        return this.operationsSentToMasterForReplication.contains(operation);
    }


    @Override
    public void addOperationSentToMasterForReplication(
            OperationWithResultWithIdWrapper<PolarDataServiceImpl, ?> operation) {
        this.operationsSentToMasterForReplication.add(operation);
    }

    @Override
    public void registerDomainFactory(DomainFactory domainFactory) {
        this.domainFactory = domainFactory;
    }

    @Override
    public void unregisterDomainFactory(DomainFactory domainFactory) {
        if (this.domainFactory == domainFactory) {
            this.domainFactory = null;
        }
    }

}
