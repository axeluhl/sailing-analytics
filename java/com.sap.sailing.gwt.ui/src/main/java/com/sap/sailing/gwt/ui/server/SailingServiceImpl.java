package com.sap.sailing.gwt.ui.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import javax.imageio.metadata.IIOMetadata;
import javax.management.InvalidAttributeValueException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.competitorimport.CompetitorProvider;
import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.AbstractLogEvent;
import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.AllEventsOfTypeFinder;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEndOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningListChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFixedMarkPassingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogPathfinderEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartOfTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogSuppressedMarkPassingsEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogWindFixEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.MarkPassingDataFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.TrackingTimesEventFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.TrackingTimesFinder;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogDependentStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningConfirmedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFinishPositioningListChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFixedMarkPassingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogGateLineOpeningTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPathfinderEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogProtestStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartProcedureChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogSuppressedMarkPassingsEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogWindFixEventImpl;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.impl.RaceLogAdditionalScoringInformationEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogUseCompetitorsFromRaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogStartTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogUseCompetitorsFromRaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDefineMarkEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogRegisterCompetitorEvent;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceBoatMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorExpeditionExtendedMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.BaseRegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogDeviceMarkMappingFinder;
import com.sap.sailing.domain.abstractlog.regatta.tracking.analyzing.impl.RegattaLogOpenEndedDeviceMappingCloser;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.PairingListLeaderboardAdapter;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.SailingServerConfiguration;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.LeagueConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureWithConfigurableStartModeFlagConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.SWCStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ConfigurableStartModeFlagRacingProcedureConfiguration;
import com.sap.sailing.domain.base.impl.CompetitorWithBoatImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicCompetitorWithBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.SailingServerConfigurationImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DataImportSubProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PathType;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaFetcher;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.ServiceException;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.UnableToCloseDeviceMappingException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorAndBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.FullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTOFactory;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RaceLogTrackingInfoDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.PolarSheetsXYDiagramDataImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.common.tagging.RaceLogNotFoundException;
import com.sap.sailing.domain.common.tagging.ServiceNotFoundException;
import com.sap.sailing.domain.common.tagging.TagAlreadyExistsException;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl.PreciseCompactPosition;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboardWithEliminations;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.leaderboard.caching.LiveLeaderboardUpdater;
import com.sap.sailing.domain.leaderboard.meta.MetaLeaderboardColumn;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceStateOfSameDayHelper;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.DeviceMappingWithRegattaLogEvent;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.sharding.ShardingContext;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapter;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingAdapterFactory;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingArchiveConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingConfiguration;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.swisstimingadapter.persistence.SwissTimingAdapterPersistence;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayRace;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayService;
import com.sap.sailing.domain.swisstimingreplayadapter.SwissTimingReplayServiceFactory;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;
import com.sap.sailing.expeditionconnector.ExpeditionTrackerFactory;
import com.sap.sailing.gwt.common.client.EventWindFinderUtil;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.adminconsole.RaceLogSetTrackingTimesDTO;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTrackDTO;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTracksDTO;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorProviderDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureWithConfigurableStartModeFlagConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupBaseDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverLossDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.MigrateGroupOwnerForHierarchyDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.QuickRanksDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.GateStartInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.LineStartInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.RaceInfoExtensionDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetFinishingAndFinishTimeDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsAndBoatsDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sailing.gwt.ui.shared.SailingServiceConstants;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SidelineDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.shared.SliceRacePreperationDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.manage2sail.EventResultDescriptor;
import com.sap.sailing.manage2sail.Manage2SailEventResultsParserImpl;
import com.sap.sailing.manage2sail.RaceResultDescriptor;
import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.hierarchy.SailingHierarchyOwnershipUpdater;
import com.sap.sailing.server.hierarchy.SailingHierarchyOwnershipUpdater.GroupOwnerUpdateStrategy;
import com.sap.sailing.server.masterdata.MasterDataImporter;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddCourseAreas;
import com.sap.sailing.server.operationaltransformation.AddRemoteSailingServerReference;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.AllowBoatResetToDefaults;
import com.sap.sailing.server.operationaltransformation.AllowCompetitorResetToDefaults;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboardWithEliminations;
import com.sap.sailing.server.operationaltransformation.DisconnectLeaderboardColumnFromTrackedRace;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesDown;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesUp;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnDown;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnUp;
import com.sap.sailing.server.operationaltransformation.RemoveAndUntrackRace;
import com.sap.sailing.server.operationaltransformation.RemoveColumnFromSeries;
import com.sap.sailing.server.operationaltransformation.RemoveCourseAreas;
import com.sap.sailing.server.operationaltransformation.RemoveEvent;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboard;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.RemoveLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.RemoveRegatta;
import com.sap.sailing.server.operationaltransformation.RemoveRemoteSailingServerReference;
import com.sap.sailing.server.operationaltransformation.RemoveSeries;
import com.sap.sailing.server.operationaltransformation.RenameEvent;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboard;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.RenameLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.SetRaceIsKnownToStartUpwind;
import com.sap.sailing.server.operationaltransformation.SetSuppressedFlagForCompetitorInLeaderboard;
import com.sap.sailing.server.operationaltransformation.SetWindSourcesToExclude;
import com.sap.sailing.server.operationaltransformation.StopTrackingRace;
import com.sap.sailing.server.operationaltransformation.UpdateBoat;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitor;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitorDisplayNameInLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateEliminatedCompetitorsInLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateEvent;
import com.sap.sailing.server.operationaltransformation.UpdateIsMedalRace;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardCarryValue;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardColumnFactor;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardMaxPointsReason;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrection;
import com.sap.sailing.server.operationaltransformation.UpdateLeaderboardScoreCorrectionMetadata;
import com.sap.sailing.server.operationaltransformation.UpdateRaceDelayToLive;
import com.sap.sailing.server.operationaltransformation.UpdateSeries;
import com.sap.sailing.server.operationaltransformation.UpdateServerConfiguration;
import com.sap.sailing.server.operationaltransformation.UpdateSpecificRegatta;
import com.sap.sailing.server.security.SailingViewerRole;
import com.sap.sailing.server.simulation.SimulationService;
import com.sap.sailing.server.util.WaitForTrackedRaceUtil;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SimulationResults;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.impl.PolarDiagramGPS;
import com.sap.sailing.simulator.impl.SparseSimulationDataException;
import com.sap.sailing.util.RegattaUtil;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.xrr.structureimport.SeriesParameters;
import com.sap.sailing.xrr.structureimport.StructureImporter;
import com.sap.sailing.xrr.structureimport.buildstructure.SetRacenumberFromSeries;
import com.sap.sse.ServerInfo;
import com.sap.sse.common.Base64Utils;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.PairingListCreationException;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Timed;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.WithID;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.common.media.MimeType;
import com.sap.sse.filestorage.FileStorageManagementService;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.filestorage.OperationFailedException;
import com.sap.sse.gwt.client.ServerInfoDTO;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.gwt.dispatch.servlets.ProxiedRemoteServiceServlet;
import com.sap.sse.gwt.server.filestorage.FileStorageServiceDTOUtils;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;
import com.sap.sse.gwt.shared.replication.ReplicaDTO;
import com.sap.sse.gwt.shared.replication.ReplicationMasterDTO;
import com.sap.sse.gwt.shared.replication.ReplicationStateDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.i18n.impl.ResourceBundleStringMessagesImpl;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.impl.PairingListTemplateImpl;
import com.sap.sse.qrcode.QRCodeGenerationUtil;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.Replicable;
import com.sap.sse.replication.ReplicationFactory;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.impl.ReplicaDescriptor;
import com.sap.sse.security.Action;
import com.sap.sse.security.ActionWithResult;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.RoleImpl;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes.ServerActions;
import com.sap.sse.security.shared.impl.WildcardPermissionEncoder;
import com.sap.sse.security.ui.server.SecurityDTOUtil;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.shared.media.ImageDescriptor;
import com.sap.sse.shared.media.MediaUtils;
import com.sap.sse.shared.media.VideoDescriptor;
import com.sap.sse.shared.media.impl.ImageDescriptorImpl;
import com.sap.sse.shared.media.impl.VideoDescriptorImpl;
import com.sap.sse.util.HttpUrlConnectionHelper;
import com.sap.sse.util.ImageConverter;
import com.sap.sse.util.ImageConverter.ImageWithMetadata;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sap.sse.util.ThreadPoolUtil;
import com.sapsailing.xrr.structureimport.eventimport.RegattaJSON;


/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends ProxiedRemoteServiceServlet implements SailingService, RaceFetcher, RegattaFetcher {
    private static final Logger logger = Logger.getLogger(SailingServiceImpl.class.getName());

    private static final String STRING_MESSAGES_BASE_NAME = "stringmessages/StringMessages";

    private static final long serialVersionUID = 9031688830194537489L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private final ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;

    private final ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    private final ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> scoreCorrectionProviderServiceTracker;

    private final ServiceTracker<CompetitorProvider, CompetitorProvider> competitorProviderServiceTracker;
    
    private final ServiceTracker<WindFinderTrackerFactory, WindFinderTrackerFactory> windFinderTrackerFactoryServiceTracker;

    private final MongoObjectFactory mongoObjectFactory;

    private final ServiceTracker<ExpeditionTrackerFactory, ExpeditionTrackerFactory> expeditionConnectorTracker;

    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    
    private final ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> swissTimingAdapterTracker;

    private final ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> tractracAdapterTracker;

    private final ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiAdapterTracker;

    private final ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> raceLogTrackingAdapterTracker;
    
    private final ServiceTracker<DeviceIdentifierStringSerializationHandler, DeviceIdentifierStringSerializationHandler>
    deviceIdentifierStringSerializationHandlerTracker;
    
    private final ServiceTracker<SecurityService, SecurityService> securityServiceTracker;
    
    private final com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory tractracMongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;

    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory tractracDomainObjectFactory;

    private final com.sap.sse.common.CountryCodeFactory countryCodeFactory;

    private final Executor executor;
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private static final int LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE = 100;
    
    private static final int LEADERBOARD_DIFFERENCE_CACHE_SIZE = 50;

    private ResourceBundleStringMessages serverStringMessages;

    private final LinkedHashMap<String, LeaderboardDTO> leaderboardByNameResultsCacheById;

    private int leaderboardDifferenceCacheByIdPairHits;
    private int leaderboardDifferenceCacheByIdPairMisses;
    /**
     * Caches some results of the hard to compute difference between two {@link LeaderboardDTO}s. The objects contained as values
     * have been obtained by {@link IncrementalLeaderboardDTO#strip(LeaderboardDTO)}. The cache size is limited to
     * {@link #LEADERBOARD_DIFFERENCE_CACHE_SIZE}.
     */
    private final LinkedHashMap<com.sap.sse.common.Util.Pair<String, String>, IncrementalLeaderboardDTO> leaderboardDifferenceCacheByIdPair;

    private final SwissTimingReplayService swissTimingReplayService;

    private final QuickRanksLiveCache quickRanksLiveCache;
    
    public SailingServiceImpl() {
        BundleContext context = Activator.getDefault();
        Activator activator = Activator.getInstance();
        quickRanksLiveCache = new QuickRanksLiveCache(this);
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        windFinderTrackerFactoryServiceTracker = ServiceTrackerFactory.createAndOpen(context, WindFinderTrackerFactory.class);
        replicationServiceTracker = ServiceTrackerFactory.createAndOpen(context, ReplicationService.class);
        resultUrlRegistryServiceTracker = ServiceTrackerFactory.createAndOpen(context, ResultUrlRegistry.class);
        swissTimingAdapterTracker = ServiceTrackerFactory.createAndOpen(context, SwissTimingAdapterFactory.class);
        tractracAdapterTracker = ServiceTrackerFactory.createAndOpen(context, TracTracAdapterFactory.class);
        raceLogTrackingAdapterTracker = ServiceTrackerFactory.createAndOpen(context,
                RaceLogTrackingAdapterFactory.class);
        deviceIdentifierStringSerializationHandlerTracker = ServiceTrackerFactory.createAndOpen(context,
                DeviceIdentifierStringSerializationHandler.class);
        securityServiceTracker = ServiceTrackerFactory.createAndOpen(context, SecurityService.class);
        igtimiAdapterTracker = ServiceTrackerFactory.createAndOpen(context, IgtimiConnectionFactory.class);
        baseDomainFactory = getService().getBaseDomainFactory();
        mongoObjectFactory = getService().getMongoObjectFactory();
        domainObjectFactory = getService().getDomainObjectFactory();
        // TODO what about passing on the mongo/domain object factory to obtain an according SwissTimingAdapterPersistence instance similar to how the tractracDomainObjectFactory etc. are created below?
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingReplayService = ServiceTrackerFactory.createAndOpen(context, SwissTimingReplayServiceFactory.class)
                .getService().createSwissTimingReplayService(getSwissTimingAdapter().getSwissTimingDomainFactory(),
                /* raceLogResolver */ getService());
        expeditionConnectorTracker = ServiceTrackerFactory.createAndOpen(context, ExpeditionTrackerFactory.class);
        scoreCorrectionProviderServiceTracker = ServiceTrackerFactory.createAndOpen(context,
                ScoreCorrectionProvider.class);
        competitorProviderServiceTracker = ServiceTrackerFactory.createAndOpen(context, CompetitorProvider.class);
        tractracDomainObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.PersistenceFactory.INSTANCE
                .createDomainObjectFactory(mongoObjectFactory.getDatabase(), getTracTracAdapter()
                        .getTracTracDomainFactory());
        tractracMongoObjectFactory = com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory.INSTANCE;
        swissTimingFactory = SwissTimingFactory.INSTANCE;
        countryCodeFactory = com.sap.sse.common.CountryCodeFactory.INSTANCE;
        leaderboardDifferenceCacheByIdPair = new LinkedHashMap<com.sap.sse.common.Util.Pair<String, String>, IncrementalLeaderboardDTO>(LEADERBOARD_DIFFERENCE_CACHE_SIZE, 0.75f, /* accessOrder */ true) {
            private static final long serialVersionUID = 3775119859130148488L;
            @Override
            protected boolean removeEldestEntry(Entry<com.sap.sse.common.Util.Pair<String, String>, IncrementalLeaderboardDTO> eldest) {
                return this.size() > LEADERBOARD_DIFFERENCE_CACHE_SIZE;
            }
        };
        leaderboardByNameResultsCacheById = new LinkedHashMap<String, LeaderboardDTO>(LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE, 0.75f, /* accessOrder */ true) {
            private static final long serialVersionUID = 3775119859130148488L;
            @Override
            protected boolean removeEldestEntry(Entry<String, LeaderboardDTO> eldest) {
                return this.size() > LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE;
            }
        };
        // When many updates are triggered in a short period of time by a single thread, ensure that the single thread
        // providing the updates is not outperformed by all the re-calculations happening here. Leave at least one
        // core to other things, but by using at least three threads ensure that no simplistic deadlocks may occur.
        executor = ThreadPoolUtil.INSTANCE.getDefaultForegroundTaskThreadPoolExecutor();
        serverStringMessages = new ResourceBundleStringMessagesImpl(STRING_MESSAGES_BASE_NAME,
                this.getClass().getClassLoader(), StandardCharsets.UTF_8.name());
        if (context != null) {
            activator.setSailingService(this); // register so this service is informed when the bundle shuts down
        }
    }
    
    /**
     * Stops this service and frees its resources. In particular, caching services and threads owned by this service will be
     * notified to stop their jobs.
     */
    public void stop() {
        quickRanksLiveCache.stop();
    }

    protected SwissTimingAdapterFactory getSwissTimingAdapterFactory() {
        return swissTimingAdapterTracker.getService();
    }

    protected SwissTimingAdapter getSwissTimingAdapter() {
        return getSwissTimingAdapterFactory().getOrCreateSwissTimingAdapter(baseDomainFactory);
    }
    
    protected TracTracAdapterFactory getTracTracAdapterFactory() {
        return tractracAdapterTracker.getService();
    }

    protected TracTracAdapter getTracTracAdapter() {
        return getTracTracAdapterFactory().getOrCreateTracTracAdapter(baseDomainFactory);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    @Override
    public Iterable<String> getScoreCorrectionProviderNames() {
        List<String> result = new ArrayList<String>();
        for (ScoreCorrectionProvider scoreCorrectionProvider : getAllScoreCorrectionProviders()) {
            result.add(scoreCorrectionProvider.getName());
        }
        return result;
    }

    @Override
    public ScoreCorrectionProviderDTO getScoreCorrectionsOfProvider(String providerName) throws Exception {
        ScoreCorrectionProviderDTO result = null;
        for (ScoreCorrectionProvider scoreCorrectionProvider : getAllScoreCorrectionProviders()) {
            if (scoreCorrectionProvider.getName().equals(providerName)) {
                result = convertScoreCorrectionProviderDTO(scoreCorrectionProvider);
                break;
            }
        }
        return result;
    }

    private Iterable<ScoreCorrectionProvider> getAllScoreCorrectionProviders() {
        final ScoreCorrectionProvider[] services = scoreCorrectionProviderServiceTracker.getServices(new ScoreCorrectionProvider[0]);
        List<ScoreCorrectionProvider> result = new ArrayList<ScoreCorrectionProvider>();
        if (services != null) {
            for (final ScoreCorrectionProvider service : services) {
                result.add(service);
            }
        }
        return result;
    }

    private ScoreCorrectionProviderDTO convertScoreCorrectionProviderDTO(ScoreCorrectionProvider scoreCorrectionProvider)
            throws Exception {
        Map<String, Set<com.sap.sse.common.Util.Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName = new HashMap<String, Set<com.sap.sse.common.Util.Pair<String,Date>>>();
        for (Map.Entry<String, Set<com.sap.sse.common.Util.Pair<String, TimePoint>>> e : scoreCorrectionProvider
                .getHasResultsForBoatClassFromDateByEventName().entrySet()) {
            Set<com.sap.sse.common.Util.Pair<String, Date>> set = new HashSet<com.sap.sse.common.Util.Pair<String, Date>>();
            for (com.sap.sse.common.Util.Pair<String, TimePoint> p : e.getValue()) {
                set.add(new com.sap.sse.common.Util.Pair<String, Date>(p.getA(), p.getB().asDate()));
            }
            hasResultsForBoatClassFromDateByEventName.put(e.getKey(), set);
        }
        return new ScoreCorrectionProviderDTO(scoreCorrectionProvider.getName(), hasResultsForBoatClassFromDateByEventName);
    }

    @Override
    public Iterable<String> getCompetitorProviderNames() {
        List<String> result = new ArrayList<>();
        for (CompetitorProvider competitorProvider : getAllCompetitorProviders()) {
            result.add(competitorProvider.getName());
        }
        return result;
    }

    private Iterable<CompetitorProvider> getAllCompetitorProviders() {
        final CompetitorProvider[] services = competitorProviderServiceTracker.getServices(new CompetitorProvider[0]);
        List<CompetitorProvider> result = new ArrayList<>();
        if (services != null) {
            for (final CompetitorProvider service : services) {
                result.add(service);
            }
        }
        return result;
    }

    @Override
    public CompetitorProviderDTO getCompetitorProviderDTOByName(String providerName) throws Exception {
        for (CompetitorProvider competitorProvider : getAllCompetitorProviders()) {
            if (competitorProvider.getName().equals(providerName)) {
                return new CompetitorProviderDTO(competitorProvider.getName(),
                        new HashMap<>(competitorProvider.getHasCompetitorsForRegattasInEvent()));
            }
        }
        return null;
    }

    @Override
    public List<CompetitorDescriptor> getCompetitorDescriptors(String competitorProviderName, String eventName,
            String regattaName) throws Exception {
        for (CompetitorProvider cp : getAllCompetitorProviders()) {
            if (cp.getName().equals(competitorProviderName)) {
                final List<CompetitorDescriptor> result = new ArrayList<>();
                Util.addAll(cp.getCompetitorDescriptors(eventName, regattaName), result);
                return result;
            }
        }
        return Collections.emptyList();
    }
    
    @Override
    public Pair<PersonDTO, CountryCode> serializationDummy(PersonDTO dummy, CountryCode ccDummy, PreciseCompactPosition preciseCompactPosition) { return null; }

    /**
     * If <code>date</code> is <code>null</code>, the {@link LiveLeaderboardUpdater} for the
     * <code>leaderboardName</code> requested is obtained or created if it doesn't exist yet. The request is then passed
     * on to the live leaderboard updater which will respond with its live {@link LeaderboardDTO} if it has at least the
     * columns requested as per <code>namesOfRaceColumnsForWhichToLoadLegDetails</code>. Otherwise, the updater will add
     * the missing columns to its profile and start a synchronous computation for the requesting client, the result of
     * which will be used as live leaderboard cache update.
     * <p>
     * 
     * Otherwise, the leaderboard is computed synchronously on the fly.
     * @param previousLeaderboardId
     *            if <code>null</code> or no leaderboard with that {@link LeaderboardDTO#getId() ID} is known, a
     *            {@link FullLeaderboardDTO} will be computed; otherwise, an {@link IncrementalLeaderboardDTO} will be
     *            computed as the difference between the new, resulting leaderboard and the previous leaderboard.
     */
    @Override
    public IncrementalOrFullLeaderboardDTO getLeaderboardByName(final String leaderboardName, final Date date,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            String previousLeaderboardId, boolean fillTotalPointsUncorrected) throws NoWindException, InterruptedException, ExecutionException,
            IllegalArgumentException {
        Leaderboard leaderBoard = getService().getLeaderboardByName(leaderboardName);
        getSecurityService().checkCurrentUserReadPermission(leaderBoard);
        if (leaderBoard instanceof RegattaLeaderboard) {
            getSecurityService().checkCurrentUserReadPermission(((RegattaLeaderboard) leaderBoard).getRegatta());
        }
        return getLeaderBoardByNameInternal(leaderboardName, date, namesOfRaceColumnsForWhichToLoadLegDetails,
                addOverallDetails, previousLeaderboardId, fillTotalPointsUncorrected);
    }

    @Override
    public IncrementalOrFullLeaderboardDTO getLeaderboardForRace(final RegattaAndRaceIdentifier race,
            final String leaderboardName, final Date date,
            final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            String previousLeaderboardId, boolean fillTotalPointsUncorrected)
            throws NoWindException, InterruptedException, ExecutionException, IllegalArgumentException {
        final DynamicTrackedRace trackedRace = getService().getTrackedRace(race);
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard.getRaceColumnAndFleet(trackedRace) == null) {
            // this race does not seem to be contained in the leaderboard, also check leaderboard
            getSecurityService().checkCurrentUserReadPermission(leaderboard);
        }
        getSecurityService().checkCurrentUserReadPermission(trackedRace);
        return getLeaderBoardByNameInternal(leaderboardName, date, namesOfRaceColumnsForWhichToLoadLegDetails,
                addOverallDetails, previousLeaderboardId, fillTotalPointsUncorrected);
    }

    private IncrementalOrFullLeaderboardDTO getLeaderBoardByNameInternal(final String leaderboardName,
            final Date date, final Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            boolean addOverallDetails, String previousLeaderboardId, boolean fillTotalPointsUncorrected)
            throws NoWindException, InterruptedException, ExecutionException {
        try {
            long startOfRequestHandling = System.currentTimeMillis();
            IncrementalOrFullLeaderboardDTO result = null;
            final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard != null) {
                TimePoint timePoint;
                if (date == null) {
                    timePoint = null;
                } else {
                    timePoint = new MillisecondsTimePoint(date);
                }
                LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(timePoint,
                        namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails, getService(), baseDomainFactory, fillTotalPointsUncorrected);
                LeaderboardDTO previousLeaderboardDTO = null;
                synchronized (leaderboardByNameResultsCacheById) {
                    leaderboardByNameResultsCacheById.put(leaderboardDTO.getId(), leaderboardDTO);
                    if (previousLeaderboardId != null) {
                        previousLeaderboardDTO = leaderboardByNameResultsCacheById.get(previousLeaderboardId);
                    }
                }
                // Un-comment the following lines if you need to update the file used by LeaderboardDTODiffingTest, set a breakpoint
                // and toggle the storeLeaderboardForTesting flag if you found a good version. See also bug 1417.
                // The leaderboard that the test wants to use is that of the 505 Worlds 2013, obtained for
                // an expanded Race R9 at time 2013-05-03T19:17:09Z after the last competitor tracked has finished the last leg. The
                // total distance traveled in meters has to be expanded for the test to work.
                boolean storeLeaderboardForTesting = false;
                if (storeLeaderboardForTesting) {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("C:/data/SAP/sailing/workspace/java/com.sap.sailing.domain.test/resources/IncrementalLeaderboardDTO.ser")));
                    oos.writeObject(leaderboardDTO);
                    oos.close();
                }
                final IncrementalLeaderboardDTO cachedDiff;
                if (previousLeaderboardId != null) {
                    synchronized (leaderboardDifferenceCacheByIdPair) {
                        cachedDiff = leaderboardDifferenceCacheByIdPair.get(new com.sap.sse.common.Util.Pair<String, String>(previousLeaderboardId, leaderboardDTO.getId()));
                    }
                    if (cachedDiff == null) {
                        leaderboardDifferenceCacheByIdPairMisses++;
                    } else {
                        leaderboardDifferenceCacheByIdPairHits++;
                    }
                } else {
                    cachedDiff = null;
                }
                if (previousLeaderboardDTO == null) {
                    result = new FullLeaderboardDTO(leaderboardDTO);
                } else {
                    final IncrementalLeaderboardDTO incrementalResult;
                    if (cachedDiff == null) {
                        IncrementalLeaderboardDTO preResult = new IncrementalLeaderboardDTOCloner().clone(leaderboardDTO).strip(previousLeaderboardDTO);
                        synchronized (leaderboardDifferenceCacheByIdPair) {
                            leaderboardDifferenceCacheByIdPair.put(new com.sap.sse.common.Util.Pair<String, String>(previousLeaderboardId, leaderboardDTO.getId()), preResult);
                        }
                        incrementalResult = preResult;
                    } else {
                        incrementalResult = cachedDiff;
                    }
                    incrementalResult.setCurrentServerTime(new Date()); // may update a cached object, but we consider a reference update atomic
                    result = incrementalResult;
                }
                logger.fine("getLeaderboardByName(" + leaderboardName + ", " + date + ", "
                        + namesOfRaceColumnsForWhichToLoadLegDetails + ", addOverallDetails=" + addOverallDetails
                        + ") took " + (System.currentTimeMillis() - startOfRequestHandling)
                        + "ms; diff cache hits/misses " + leaderboardDifferenceCacheByIdPairHits + "/"
                        + leaderboardDifferenceCacheByIdPairMisses);
            }
            return result;
        } catch (NoWindException e) {
            throw e;
        } catch (InterruptedException e) {
            throw e;
        } catch (ExecutionException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Exception during SailingService.getLeaderboardByName", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RegattaDTO> getRegattas() {
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.REGATTA,
                getService().getAllRegattas(), Regatta::getName, this::convertToRegattaDTO);
    }

    @Override
    public RegattaDTO getRegattaByName(String regattaName) {
        RegattaDTO result = null;
        if (regattaName != null && !regattaName.isEmpty()) {
            Regatta regatta = getService().getRegatta(new RegattaName(regattaName));
            if (regatta != null) {
                result = convertToRegattaDTO(regatta);
            }
        }
        return result;
    }

    private MarkDTO convertToMarkDTO(Mark mark, Position position) {
        MarkDTO markDTO;
        if (position != null) {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName(), position.getLatDeg(), position.getLngDeg());
        } else {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName());
        }
        markDTO.color = mark.getColor();
        markDTO.shape = mark.getShape();
        markDTO.pattern = mark.getPattern();
        markDTO.type = mark.getType();
        return markDTO;
    }
    
    private RegattaDTO convertToRegattaDTO(Regatta regatta) {
        RegattaDTO regattaDTO = new RegattaDTO(regatta.getName(), regatta.getScoringScheme().getType());
        regattaDTO.races = convertToRaceDTOs(regatta);
        regattaDTO.series = convertToSeriesDTOs(regatta);
        regattaDTO.startDate = regatta.getStartDate() != null ? regatta.getStartDate().asDate() : null;
        regattaDTO.endDate = regatta.getEndDate() != null ? regatta.getEndDate().asDate() : null;
        BoatClass boatClass = regatta.getBoatClass();
        if (boatClass != null) {
            regattaDTO.boatClass = convertToBoatClassDTO(boatClass);
        }
        if (regatta.getDefaultCourseArea() != null) {
            regattaDTO.defaultCourseAreaUuid = regatta.getDefaultCourseArea().getId();
            regattaDTO.defaultCourseAreaName = regatta.getDefaultCourseArea().getName();
        }
        regattaDTO.buoyZoneRadiusInHullLengths = regatta.getBuoyZoneRadiusInHullLengths();
        regattaDTO.useStartTimeInference = regatta.useStartTimeInference();
        regattaDTO.controlTrackingFromStartAndFinishTimes = regatta.isControlTrackingFromStartAndFinishTimes();
        regattaDTO.canBoatsOfCompetitorsChangePerRace = regatta.canBoatsOfCompetitorsChangePerRace();
        regattaDTO.competitorRegistrationType = regatta.getCompetitorRegistrationType();
        regattaDTO.configuration = convertToRegattaConfigurationDTO(regatta.getRegattaConfiguration());
        regattaDTO.rankingMetricType = regatta.getRankingMetricType();
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), regattaDTO, regatta.getIdentifier());
        regattaDTO.registrationLinkSecret = regatta.getRegistrationLinkSecret();
        return regattaDTO;
    }

    private BoatClassDTO convertToBoatClassDTO(BoatClass boatClass) {
        return boatClass==null?null:new BoatClassDTO(boatClass.getName(), boatClass.getDisplayName(), boatClass.getHullLength(), boatClass.getHullBeam());
    }

    private List<SeriesDTO> convertToSeriesDTOs(Regatta regatta) {
        List<SeriesDTO> result = new ArrayList<SeriesDTO>();
        for (Series series : regatta.getSeries()) {
            SeriesDTO seriesDTO = convertToSeriesDTO(series);
            result.add(seriesDTO);
        }
        return result;
    }

    private SeriesDTO convertToSeriesDTO(Series series) {
        List<FleetDTO> fleets = new ArrayList<FleetDTO>();
        for (Fleet fleet : series.getFleets()) {
            fleets.add(baseDomainFactory.convertToFleetDTO(fleet));
        }
        List<RaceColumnDTO> raceColumns = convertToRaceColumnDTOs(series.getRaceColumns());
        SeriesDTO result = new SeriesDTO(series.getName(), fleets, raceColumns, series.isMedal(), series.isFleetsCanRunInParallel(),
                series.getResultDiscardingRule() == null ? null : series.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(),
                        series.isStartsWithZeroScore(), series.isFirstColumnIsNonDiscardableCarryForward(), series.hasSplitFleetContiguousScoring(),
                        series.getMaximumNumberOfDiscards());
        return result;
    }

    private void fillRaceColumnDTO(RaceColumn raceColumn, RaceColumnDTO raceColumnDTO) {
        raceColumnDTO.setName(raceColumn.getName());
        raceColumnDTO.setMedalRace(raceColumn.isMedalRace());
        raceColumnDTO.setExplicitFactor(raceColumn.getExplicitFactor());
    }
    
    private List<RaceColumnDTO> convertToRaceColumnDTOs(Iterable<? extends RaceColumn> raceColumns) {
        List<RaceColumnDTO> raceColumnDTOs = new ArrayList<RaceColumnDTO>();
        RaceColumnDTOFactory columnFactory = RaceColumnDTOFactory.INSTANCE;
        for (RaceColumn raceColumn : raceColumns) {
            final RaceColumnDTO raceColumnDTO = columnFactory.createRaceColumnDTO(raceColumn.getName(),
                    raceColumn.isMedalRace(), raceColumn.getExplicitFactor(),
                    raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getRegatta().getName() : null,
                    raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getSeries().getName() : null,
                    raceColumn instanceof MetaLeaderboardColumn);
            raceColumnDTOs.add(raceColumnDTO);
        }
        return raceColumnDTOs;
    }
    
    private RaceInfoDTO createRaceInfoDTO(String seriesName, RaceColumn raceColumn, Fleet fleet) {
        RaceInfoDTO raceInfoDTO = new RaceInfoDTO();
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
        raceInfoDTO.isTracked = trackedRace != null ? true : false;
        if (raceLog != null) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.getOrCreate(getService(), raceLog);
            TimePoint startTime = state.getStartTime();
            if (startTime != null) {
                raceInfoDTO.startTime = startTime.asDate();
            }
            raceInfoDTO.lastStatus = state.getStatus();
            if (raceLog.getLastRawFix() != null) {
                raceInfoDTO.lastUpdateTime = raceLog.getLastRawFix().getCreatedAt().asDate();
            }
            TimePoint finishedTime = state.getFinishedTime();
            if (finishedTime != null) {
                raceInfoDTO.finishedTime = finishedTime.asDate();
            } else {
                raceInfoDTO.finishedTime = null;
                if (raceInfoDTO.isTracked) {
                    TimePoint endOfRace = trackedRace.getEndOfRace();
                    raceInfoDTO.finishedTime = endOfRace != null ? endOfRace.asDate() : null;
                }
            }

            final TimePoint now = MillisecondsTimePoint.now();
            if (startTime != null) {
                FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, now);
                List<FlagPole> activeFlags = activeFlagState.getCurrentState();
                FlagPoleState previousFlagState = activeFlagState.getPreviousState(state.getRacingProcedure(), startTime);
                List<FlagPole> previousFlags = previousFlagState.getCurrentState();
                FlagPole mostInterestingFlagPole = FlagPoleState.getMostInterestingFlagPole(previousFlags, activeFlags);

                // TODO: adapt the LastFlagFinder#getMostRecent method!
                if (mostInterestingFlagPole != null) {
                    raceInfoDTO.lastUpperFlag = mostInterestingFlagPole.getUpperFlag();
                    raceInfoDTO.lastLowerFlag = mostInterestingFlagPole.getLowerFlag();
                    raceInfoDTO.lastFlagsAreDisplayed = mostInterestingFlagPole.isDisplayed();
                    raceInfoDTO.lastFlagsDisplayedStateChanged = previousFlagState.hasPoleChanged(mostInterestingFlagPole);
                }
            }
            
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
            if (abortingFlagEvent != null) {
                raceInfoDTO.isRaceAbortedInPassBefore = true;
                raceInfoDTO.abortingTimeInPassBefore = abortingFlagEvent.getLogicalTimePoint().asDate();
                
                if (raceInfoDTO.lastStatus == RaceLogRaceStatus.UNSCHEDULED || raceInfoDTO.lastStatus == RaceLogRaceStatus.PRESCHEDULED) {
                    raceInfoDTO.lastUpperFlag = abortingFlagEvent.getUpperFlag();
                    raceInfoDTO.lastLowerFlag = abortingFlagEvent.getLowerFlag();
                    raceInfoDTO.lastFlagsAreDisplayed = abortingFlagEvent.isDisplayed();
                    raceInfoDTO.lastFlagsDisplayedStateChanged = true;
                }
            }
            
            CourseBase lastCourse = state.getCourseDesign();
            if (lastCourse != null) {
                raceInfoDTO.lastCourseDesign = convertToRaceCourseDTO(lastCourse, new TrackedRaceMarkPositionFinder(trackedRace), now);
                raceInfoDTO.lastCourseName = lastCourse.getName();
            }
            
            if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                if (state.getProtestTime() != null) {
                    final TimePoint protestEndTime = state.getProtestTime().to();
                    if (protestEndTime != null) {
                        final TimePoint protestStartTime = state.getProtestTime().from();
                        raceInfoDTO.protestStartTime = protestStartTime == null ? null : protestStartTime.asDate();
                        raceInfoDTO.protestFinishTime = protestEndTime.asDate();
                        raceInfoDTO.lastUpperFlag = Flags.BRAVO;
                        raceInfoDTO.lastLowerFlag = Flags.NONE;
                        raceInfoDTO.lastFlagsAreDisplayed = true;
                        raceInfoDTO.lastFlagsDisplayedStateChanged = true;
                    }
                }
            }
            
            Wind wind = state.getWindFix();
            if (wind != null) {
                raceInfoDTO.lastWind = createWindDTOFromAlreadyAveraged(wind, now);
            }

            fillStartProcedureSpecifics(raceInfoDTO, state);
        }
        raceInfoDTO.seriesName = seriesName;
        raceInfoDTO.raceName = raceColumn.getName();
        raceInfoDTO.fleetName = fleet.getName();
        raceInfoDTO.fleetOrdering = fleet.getOrdering();
        raceInfoDTO.raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        return raceInfoDTO;
    }
    
    private void fillStartProcedureSpecifics(RaceInfoDTO raceInfoDTO, ReadonlyRaceState state) {
        RaceInfoExtensionDTO info = null;
        raceInfoDTO.startProcedure = state.getRacingProcedure().getType();
        switch (raceInfoDTO.startProcedure) {
        case GateStart:
            ReadonlyGateStartRacingProcedure gateStart = state.getTypedReadonlyRacingProcedure();
            info = new GateStartInfoDTO(gateStart.getPathfinder(), gateStart.getGateLaunchStopTime());
            break;
        case RRS26:
        case SWC:
            ConfigurableStartModeFlagRacingProcedure linestart = state.getTypedReadonlyRacingProcedure();
            info = new LineStartInfoDTO(linestart.getStartModeFlag());
        case UNKNOWN:
        default:
            break;
        }
        raceInfoDTO.startProcedureDTO = info;
    }

    private List<RaceWithCompetitorsAndBoatsDTO> convertToRaceDTOs(Regatta regatta) {
        List<RaceWithCompetitorsAndBoatsDTO> result = new ArrayList<RaceWithCompetitorsAndBoatsDTO>();
        for (RaceDefinition r : regatta.getAllRaces()) {
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), r.getName());
            TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
            TrackedRaceDTO trackedRaceDTO = null; 
            if (trackedRace != null) {
                trackedRaceDTO = getBaseDomainFactory().createTrackedRaceDTO(trackedRace);
            }
            Map<CompetitorDTO, BoatDTO> competitorAndBoatDTOs = baseDomainFactory.convertToCompetitorAndBoatDTOs(r.getCompetitorsAndTheirBoats());
            RaceWithCompetitorsAndBoatsDTO raceDTO = new RaceWithCompetitorsAndBoatsDTO(raceIdentifier, competitorAndBoatDTOs,
                    trackedRaceDTO, getService().isRaceBeingTracked(regatta, r));
            if (trackedRace != null) {
                getBaseDomainFactory().updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
            }
            raceDTO.boatClass = regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName(); 
            result.add(raceDTO);
        }
        return result;
    }

    /**
     * Converts the {@link Competitor} objects passed as {@code iterable} to {@link CompetitorDTO} objects.
     * The iteration order in the result matches that of the {@code iterable} passed.
     */
    private List<CompetitorDTO> convertToCompetitorDTOs(Iterable<? extends Competitor> iterable) {
        List<CompetitorDTO> result = new ArrayList<>();
        for (Competitor c : iterable) {
            CompetitorDTO competitorDTO = convertToCompetitorDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }
    
    private CompetitorDTO convertToCompetitorDTO(Competitor competitor) {
        CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), competitorDTO, competitor.getIdentifier());
        return competitorDTO;
    }

    /**
     * Converts the {@link Competitor} objects passed as {@code iterable} to {@link CompetitorWithBoatDTO} objects with an empty boat.
     * The iteration order in the result matches that of the {@code iterable} passed.
     */
    private List<CompetitorAndBoatDTO> convertToCompetitorAndBoatDTOs(Map<? extends Competitor, ? extends Boat> competitorsAndTheirBoats) {
        List<CompetitorAndBoatDTO> result = new ArrayList<>();
        for (final Entry<? extends Competitor, ? extends Boat> c : competitorsAndTheirBoats.entrySet()) {
            CompetitorAndBoatDTO competitorAndBoatDTO = baseDomainFactory.convertToCompetitorAndBoatDTO(c.getKey(), c.getValue());
            result.add(competitorAndBoatDTO);
        }
        return result;
    }

    /**
     * Converts the {@link Competitor} objects passed as {@code iterable} to {@link CompetitorWithBoatDTO} objects with an empty boat.
     * The iteration order in the result matches that of the {@code iterable} passed.
     */
    private List<CompetitorWithBoatDTO> convertToCompetitorWithBoatDTOs(Iterable<? extends CompetitorWithBoat> iterable) {
        List<CompetitorWithBoatDTO> result = new ArrayList<>();
        for (CompetitorWithBoat c : iterable) {
            CompetitorWithBoatDTO competitorDTO = baseDomainFactory.convertToCompetitorWithBoatDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }

    /**
     * Converts the {@link Boat} objects passed as {@code iterable} to {@link BoatDTO} objects.
     * The iteration order in the result matches that of the {@code iterable} passed.
     */
    private List<BoatDTO> convertToBoatDTOs(Iterable<? extends Boat> iterable) {
        List<BoatDTO> result = new ArrayList<BoatDTO>();
        for (Boat b : iterable) {
            BoatDTO boatDTO = baseDomainFactory.convertToBoatDTO(b);
            SecurityDTOUtil.addSecurityInformation(getSecurityService(), boatDTO, b.getIdentifier());
            result.add(boatDTO);
        }
        return result;
    }
    
    private BoatDTO convertToBoatDTO(Boat boat) {
        BoatDTO boatDTO = baseDomainFactory.convertToBoatDTO(boat);
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), boatDTO, boat.getIdentifier());
        return boatDTO;
    }

    @Override
    public com.sap.sse.common.Util.Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sse.common.Util.Pair<String,List<RaceRecord>> raceRecords;
        raceRecords = getTracTracAdapter().getTracTracRaceRecords(new URL(eventJsonURL), /*loadClientParam*/ false);
        List<TracTracRaceRecordDTO> result = new ArrayList<TracTracRaceRecordDTO>();
        for (RaceRecord raceRecord : raceRecords.getB()) {
            if (listHiddenRaces == false && raceRecord.getRaceVisibility().equals(TracTracConnectionConstants.HIDDEN_VISIBILITY)) {
                continue;
            }
            
            result.add(new TracTracRaceRecordDTO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getTrackingStartTime().asDate(), 
                    raceRecord.getTrackingEndTime().asDate(), raceRecord.getRaceStartTime() == null ? null : raceRecord.getRaceStartTime().asDate(),
                    raceRecord.getBoatClassNames(), raceRecord.getRaceStatus(), raceRecord.getRaceVisibility(), raceRecord.getJsonURL().toString(),
                    hasRememberedRegatta(raceRecord.getID())));
        }
        return new com.sap.sse.common.Util.Pair<String, List<TracTracRaceRecordDTO>>(raceRecords.getA(), result);
    }

    private boolean hasRememberedRegatta(Serializable raceID) {
        return getService().getRememberedRegattaForRace(raceID) != null;
    }

    @Override
    public void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI, String storedURI,
            String courseDesignUpdateURI, boolean trackWind, final boolean correctWindByDeclination,
            final Duration offsetToStartTimeOfSimulatedRace, final boolean useInternalMarkPassingAlgorithm, String tracTracUsername, String tracTracPassword)
            throws Exception {
        logger.info("tracWithTracTrac for regatta " + regattaToAddTo + " for race records " + rrs + " with liveURI " + liveURI
                + " and storedURI " + storedURI);
        for (TracTracRaceRecordDTO rr : rrs) {
            try {
                // reload JSON and load clientparams.php
                RaceRecord record = getTracTracAdapter().getSingleTracTracRaceRecord(new URL(rr.jsonURL), rr.id, /*loadClientParams*/true);
                logger.info("Loaded race " + record.getName() + " in " + record.getEventName() + " start:" + record.getRaceStartTime() +
                        " trackingStart:" + record.getTrackingStartTime() + " trackingEnd:" + record.getTrackingEndTime());
                // note that the live URI may be null for races that were put into replay mode
                final String effectiveLiveURI;
                if (!record.getRaceStatus().equals(TracTracConnectionConstants.REPLAY_STATUS)) {
                    if (liveURI == null || liveURI.trim().length() == 0) {
                        effectiveLiveURI = record.getLiveURI() == null ? null : record.getLiveURI().toString();
                    } else {
                        effectiveLiveURI = liveURI;
                    }
                } else {
                    effectiveLiveURI = null;
                }
                final String effectiveStoredURI;
                if (storedURI == null || storedURI.trim().length() == 0) {
                    effectiveStoredURI = record.getStoredURI().toString();
                } else {
                    effectiveStoredURI = storedURI;
                }
                getTracTracAdapter().addTracTracRace(getService(), regattaToAddTo,
                        record.getParamURL(), effectiveLiveURI == null ? null : new URI(effectiveLiveURI),
                        new URI(effectiveStoredURI), new URI(courseDesignUpdateURI),
                        new MillisecondsTimePoint(record.getTrackingStartTime().asMillis()),
                        new MillisecondsTimePoint(record.getTrackingEndTime().asMillis()), getRaceLogStore(),
                        getRegattaLogStore(), RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS,
                        offsetToStartTimeOfSimulatedRace, useInternalMarkPassingAlgorithm, tracTracUsername,
                        tracTracPassword, record.getRaceStatus(), record.getRaceVisibility(), trackWind,
                        correctWindByDeclination);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to load race " + rrs+". Continuing with remaining races...", e);
            }
        }
    }

    @Override
    public List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception {
        final Iterable<TracTracConfiguration> configs = tractracDomainObjectFactory.getTracTracConfigurations();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.TRACTRAC_ACCOUNT,
                configs, TracTracConfiguration::getJSONURL,
                ttConfig -> new TracTracConfigurationDTO(ttConfig.getName(), ttConfig.getJSONURL().toString(),
                        ttConfig.getLiveDataURI().toString(), ttConfig.getStoredDataURI().toString(),
                        ttConfig.getCourseDesignUpdateURI().toString(), ttConfig.getTracTracUsername().toString(),
                        ttConfig.getTracTracPassword().toString()));
    }

    @Override
    public void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) throws Exception {

        getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.TRACTRAC_ACCOUNT, jsonURL, name,
                () -> tractracMongoObjectFactory.storeTracTracConfiguration(
                        getTracTracAdapter().createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI,
                                courseDesignUpdateURI, tracTracUsername, tracTracPassword)));
    }

    private RaceDefinition getRaceByName(Regatta regatta, String raceName) {
        if (regatta != null) {
            return regatta.getRaceByName(raceName);
        } else {
            return null;
        }
    }
    
    @Override
    public void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) throws Exception {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {            
            getService().apply(new StopTrackingRace(regattaAndRaceIdentifier));
        }
    }

    @Override
    public void removeAndUntrackRaces(Iterable<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            getService().apply(new RemoveAndUntrackRace(regattaAndRaceIdentifier));
        }
    }

    @Override
    public WindInfoForRaceDTO getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources) {
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;

            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            if (windSources != null) {
                windSourcesToDeliver.addAll(windSources);
            } else {
                windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.EXPEDITION));
                windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.WEB));
            }
            for (WindSource windSource : windSourcesToDeliver) {
                if(windSource.getType() == WindSourceType.WEB) {
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    windTrackInfoDTO.resolutionOutsideOfWhichNoFixWillBeReturned = windTrack
                            .getResolutionOutsideOfWhichNoFixWillBeReturned();
                    windTrack.lockForRead();
                    try {
                        Iterator<Wind> windIter = windTrack.getRawFixes().iterator();
                        while (windIter.hasNext()) {
                            Wind wind = windIter.next();
                            if(wind != null) {
                                WindDTO windDTO = createWindDTO(wind, windTrack);
                                windTrackInfoDTO.windFixes.add(windDTO);
                            }
                        }
                    } finally {
                        windTrack.unlockAfterRead();
                    }

                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                }
            }
        }
        return result;
    }

    protected WindDTO createWindDTO(Wind wind, WindTrack windTrack) {
        WindDTO windDTO = new WindDTO();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = wind.getPosition();
        }
        if (wind.getTimePoint() != null) {
            windDTO.measureTimepoint = wind.getTimePoint().asMillis();
            Wind estimatedWind = windTrack
                    .getAveragedWind(wind.getPosition(), wind.getTimePoint());
            if (estimatedWind != null) {
                windDTO.dampenedTrueWindBearingDeg = estimatedWind.getBearing().getDegrees();
                windDTO.dampenedTrueWindFromDeg = estimatedWind.getBearing().reverse().getDegrees();
                windDTO.dampenedTrueWindSpeedInKnots = estimatedWind.getKnots();
                windDTO.dampenedTrueWindSpeedInMetersPerSecond = estimatedWind.getMetersPerSecond();
            }
        }
        return windDTO;
    }

    /**
     * Uses <code>wind</code> for both, the non-dampened and dampened fields of the {@link WindDTO} object returned
     */
    protected WindDTO createWindDTOFromAlreadyAveraged(Wind wind, TimePoint requestTimepoint) {
        WindDTO windDTO = new WindDTO();
        windDTO.requestTimepoint = requestTimepoint.asMillis();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        windDTO.dampenedTrueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.dampenedTrueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.dampenedTrueWindSpeedInKnots = wind.getKnots();
        windDTO.dampenedTrueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = wind.getPosition();
        }
        if (wind.getTimePoint() != null) {
            windDTO.measureTimepoint = wind.getTimePoint().asMillis();
        }
        return windDTO;
    }

    /**
     * Fetches the {@link WindTrack#getAveragedWind(Position, TimePoint) average wind} from all wind tracks or those identified
     * by <code>windSourceTypeNames</code>
     */
    @Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSourceTypeNames)
                    throws NoWindException {
        Position position = new DegreePosition(latDeg, lngDeg);
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
            windSourcesToDeliver.add(new WindSourceImpl(WindSourceType.COMBINED));
            for (WindSource windSource : windSourcesToDeliver) {
                if (windSourceTypeNames == null || windSourceTypeNames.contains(windSource.getType().name())) {
                    TimePoint fromTimePoint = new MillisecondsTimePoint(from);
                    WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
                    windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                    windTrackInfoDTO.resolutionOutsideOfWhichNoFixWillBeReturned = windTrack
                            .getResolutionOutsideOfWhichNoFixWillBeReturned();
                    windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
                            .getMillisecondsOverWhichToAverageWind();
                    TimePoint timePoint = fromTimePoint;
                    Double minWindConfidence = 2.0;
                    Double maxWindConfidence = -1.0;
                    for (int i = 0; i < numberOfFixes; i++) {
                        WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack.getAveragedWindWithConfidence(position, timePoint);
                        if (averagedWindWithConfidence != null) {
                            WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), timePoint);
                            double confidence = averagedWindWithConfidence.getConfidence();
                            windDTO.confidence = confidence;
                            windTrackInfoDTO.windFixes.add(windDTO);
                            if (confidence < minWindConfidence) {
                                minWindConfidence = confidence;
                            }
                            if (confidence > maxWindConfidence) {
                                maxWindConfidence = confidence;
                            }
                        }
                        timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
                    }
                    windTrackInfoDTO.minWindConfidence = minWindConfidence; 
                    windTrackInfoDTO.maxWindConfidence = maxWindConfidence; 
                }
            }
        }
        return result;
    }

    /**
     * @param onlyUpToNewestEvent
     *            if <code>true</code>, no wind data will be returned for time points later than
     *            {@link TrackedRace#getTimePointOfNewestEvent() trackedRace.getTimePointOfNewestEvent()}. This is
     *            helpful in case the client wants to populate a chart during live mode. If <code>false</code>, the
     *            "best effort" readings are provided for the time interval requested, no matter if based on any sensor
     *            evidence or not, regardless of {@link TrackedRace#getTimePointOfNewestEvent()
     *            trackedRace.getTimePointOfNewestEvent()}.
     */
    @Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent, boolean includeCombinedWindForAllLegMiddles)
                    throws NoWindException {
        assert from != null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        WindInfoForRaceDTO result = getAveragedWindInfo(new MillisecondsTimePoint(from), millisecondsStepWidth, numberOfFixes,
                windSourceTypeNames, trackedRace, /* onlyUpToNewestEvent FIXME why not pass through onlyUpToNewestEvent here??? */ true, includeCombinedWindForAllLegMiddles);
        return result;
    }

    /**
     * @param onlyUpToNewestEvent
     *            if <code>true</code>, no wind data will be returned for time points later than
     *            {@link TrackedRace#getTimePointOfNewestEvent() trackedRace.getTimePointOfNewestEvent()}. This is
     *            helpful in case the client wants to populate a chart during live mode. If <code>false</code>, the
     *            "best effort" readings are provided for the time interval requested, no matter if based on any sensor
     *            evidence or not, regardless of {@link TrackedRace#getTimePointOfNewestEvent()
     *            trackedRace.getTimePointOfNewestEvent()}.
     * @param windSourceTypeNames
     *            if {@code null}, all wind sources delivered by {@link TrackedRace#getWindSources()} plus the
     *            {@link WindSourceType#COMBINED} wind source are delivered. Note that this does not include
     *            the {@link WindSourceType#LEG_MIDDLE} wind sources.
     * @param includeCombinedWindForAllLegMiddles
     *            if <code>true</code>, the result will return non-<code>null</code> results for calls to
     *            {@link WindInfoForRaceDTO#getCombinedWindOnLegMiddle(int)}.
     */
    private WindInfoForRaceDTO getAveragedWindInfo(TimePoint from, long millisecondsStepWidth, int numberOfFixes,
            Collection<String> windSourceTypeNames, final TrackedRace trackedRace, boolean onlyUpToNewestEvent,
            boolean includeCombinedWindForAllLegMiddles) {
        WindInfoForRaceDTO result = null;
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;
            final List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            final WindSourceImpl combinedWindSource = new WindSourceImpl(WindSourceType.COMBINED);
            if (windSourceTypeNames == null) {
                Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
                windSourcesToDeliver.add(combinedWindSource);
            } else {
                for (final String windSourceTypeToAdd : windSourceTypeNames) {
                    for (final WindSource windSource : trackedRace.getWindSources(WindSourceType.valueOf(windSourceTypeToAdd))) {
                        windSourcesToDeliver.add(windSource);
                    }
                }
            }
            for (final WindSource windSource : windSourcesToDeliver) {
                // TODO consider parallelizing
                WindTrackInfoDTO windTrackInfoDTO = createWindTrackInfoDTO(from, millisecondsStepWidth,
                        numberOfFixes, trackedRace, onlyUpToNewestEvent, newestEvent, windSource, /* use default positions */ at->null);
                windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
            }
            if (includeCombinedWindForAllLegMiddles) {
                int zeroBasedLegNumber = 0;
                for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
                    WindTrackInfoDTO windTrackInfoForLegMiddle = createWindTrackInfoDTO(from, millisecondsStepWidth,
                            numberOfFixes, trackedRace, onlyUpToNewestEvent, newestEvent, combinedWindSource,
                            new PositionAtTimeProvider() { @Override public Position getPosition(TimePoint at) { return trackedLeg.getMiddleOfLeg(at); }});
                    result.addWindOnLegMiddle(zeroBasedLegNumber, windTrackInfoForLegMiddle);
                    zeroBasedLegNumber++;
                }
            }
        }
        return result;
    }

    private interface PositionAtTimeProvider {
        Position getPosition(TimePoint at);
    }
    
    private WindTrackInfoDTO createWindTrackInfoDTO(TimePoint from, long millisecondsStepWidth, int numberOfFixes,
            TrackedRace trackedRace, boolean onlyUpToNewestEvent, TimePoint newestEvent, WindSource windSource,
            PositionAtTimeProvider positionProvider) {
        WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
        WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
        windTrackInfoDTO.resolutionOutsideOfWhichNoFixWillBeReturned = windTrack.getResolutionOutsideOfWhichNoFixWillBeReturned();
        windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
        windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack.getMillisecondsOverWhichToAverageWind();
        TimePoint timePoint = from;
        Double minWindConfidence = 2.0;
        Double maxWindConfidence = -1.0;
        for (int i = 0; i < numberOfFixes && (!onlyUpToNewestEvent ||
                (newestEvent != null && timePoint.before(newestEvent))); i++) {
            WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence =
                    windTrack.getAveragedWindWithConfidence(positionProvider.getPosition(timePoint), timePoint);
            if (averagedWindWithConfidence != null) {
                if (logger.getLevel() != null && logger.getLevel().equals(Level.FINEST)) {
                    logger.finest("Found averaged wind: " + averagedWindWithConfidence);
                }
                double confidence = averagedWindWithConfidence.getConfidence();
                WindDTO windDTO = createWindDTOFromAlreadyAveraged(averagedWindWithConfidence.getObject(), timePoint);
                windDTO.confidence = confidence;
                windTrackInfoDTO.windFixes.add(windDTO);
                if (confidence < minWindConfidence) {
                    minWindConfidence = confidence;
                }
                if (confidence > maxWindConfidence) {
                    maxWindConfidence = confidence;
                }
            } else {
                if (logger.getLevel() != null && logger.getLevel().equals(Level.FINEST)) {
                    logger.finest("Did NOT find any averaged wind for timepoint " + timePoint + " and tracked race " + trackedRace.getRaceIdentifier().getRaceName());
                }
            }
            timePoint = new MillisecondsTimePoint(timePoint.asMillis() + millisecondsStepWidth);
        }
        windTrackInfoDTO.minWindConfidence = minWindConfidence; 
        windTrackInfoDTO.maxWindConfidence = maxWindConfidence;
        return windTrackInfoDTO;
    }

    /**
     * @param to
     *            if <code>null</code>, data is returned up to end of race; if the end of race is not known and
     *            <code>null</code> is used for this parameter, <code>null</code> is returned.
     * @param onlyUpToNewestEvent
     *            if <code>true</code>, no wind data will be returned for time points later than
     *            {@link TrackedRace#getTimePointOfNewestEvent() trackedRace.getTimePointOfNewestEvent()}. This is
     *            helpful in case the client wants to populate a chart during live mode. If <code>false</code>, the
     *            "best effort" readings are provided for the time interval requested, no matter if based on any sensor
     *            evidence or not, regardless of {@link TrackedRace#getTimePointOfNewestEvent()
     *            trackedRace.getTimePointOfNewestEvent()}.
     */
    @Override
    public WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
            long resolutionInMilliseconds, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        WindInfoForRaceDTO result = null;
        if (trackedRace != null) {
            TimePoint fromTimePoint = from == null ? trackedRace.getStartOfTracking() == null ? trackedRace
                    .getStartOfRace() : trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            TimePoint toTimePoint = to == null ? trackedRace.getEndOfRace() == null ?
                    MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis()) : trackedRace.getEndOfRace() : new MillisecondsTimePoint(to);
            if (fromTimePoint != null && toTimePoint != null) {
                int numberOfFixes = Math.min(SailingServiceConstants.MAX_NUMBER_OF_WIND_FIXES_TO_DELIVER_IN_ONE_CALL,
                        (int) ((toTimePoint.asMillis() - fromTimePoint.asMillis())/resolutionInMilliseconds));
                result = getAveragedWindInfo(fromTimePoint, resolutionInMilliseconds, numberOfFixes,
                        windSourceTypeNames, trackedRace, onlyUpToNewestEvent, /* includeCombinedWindForAllLegMiddles */ false);
            }
        }
        return result;
    }

    @Override
    public boolean getPolarResults(RegattaAndRaceIdentifier raceIdentifier) {
        final boolean result;
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        final PolarDataService polarData = getService().getPolarDataService();
        if (trackedRace == null || polarData == null) {
            result = false;
        } else {
            BoatClass boatClass = trackedRace.getRace().getBoatClass();
            PolarDiagram polarDiagram;
            try {
                polarDiagram = new PolarDiagramGPS(boatClass, polarData);
            } catch (SparseSimulationDataException e) {
                polarDiagram = null;
            }
            result = polarDiagram != null;
        }
        return result;
    }

    @Override
    public SimulatorResultsDTO getSimulatorResults(LegIdentifier legIdentifier) {
        // get simulation-results from smart-future-cached simulation-service
        SimulatorResultsDTO result = null;
        SimulationService simulationService = getService().getSimulationService();
        if (simulationService == null) 
            return result;
        SimulationResults simulationResults = simulationService.getSimulationResults(legIdentifier);
        if (simulationResults == null) 
            return result;
            // prepare simulator-results-dto
        Map<PathType, Path> paths = simulationResults.getPaths();
        if (paths != null) {
            int noOfPaths = paths.size();
                PathDTO[] pathDTOs = new PathDTO[noOfPaths];
                int index = noOfPaths - 1;
            for (Entry<PathType, Path> entry : paths.entrySet()) {
                pathDTOs[index] = new PathDTO(entry.getKey());
                    // fill pathDTO with path points where speed is true wind speed
                    List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
                    for (TimedPositionWithSpeed p : entry.getValue().getPathPoints()) {
                        wList.add(createSimulatorWindDTO(p));
                    }
                    pathDTOs[index].setPoints(wList);
                pathDTOs[index].setAlgorithmTimedOut(entry.getValue().getAlgorithmTimedOut());
                pathDTOs[index].setMixedLeg(entry.getValue().getMixedLeg());
                    index--;
                }
                RaceMapDataDTO rcDTO;
                rcDTO = new RaceMapDataDTO();
                rcDTO.coursePositions = new CoursePositionsDTO();
            rcDTO.coursePositions.waypointPositions = new ArrayList<Position>();
            rcDTO.coursePositions.waypointPositions.add(simulationResults.getStartPosition());
            rcDTO.coursePositions.waypointPositions.add(simulationResults.getEndPosition());
            result = new SimulatorResultsDTO(simulationResults.getVersion().asMillis(), legIdentifier.getLegNumber()+1, simulationResults.getStartTime(), simulationResults.getTimeStep(),
                    simulationResults.getLegDuration(), rcDTO, pathDTOs, null, null);
            }
        return result;
    }
    
    private SimulatorWindDTO createSimulatorWindDTO(TimedPositionWithSpeed timedPositionWithSpeed) {

        Position position = timedPositionWithSpeed.getPosition();
        SpeedWithBearing speedWithBearing = timedPositionWithSpeed.getSpeed();
        TimePoint timePoint = timedPositionWithSpeed.getTimePoint();

        SimulatorWindDTO result = new SimulatorWindDTO();
        if (speedWithBearing == null) {
                result.trueWindBearingDeg = 0.0;
                result.trueWindSpeedInKnots = 0.0;
        } else {
                result.trueWindBearingDeg = speedWithBearing.getBearing().getDegrees();
                result.trueWindSpeedInKnots = speedWithBearing.getKnots();
        }

        if (position != null) {
            result.position = position;
        }

        if (timePoint != null) {
            result.timepoint = timePoint.asMillis();
        }

        return result;
    }

    @Override
    public void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = windDTO.position;
            }
            TimePoint at = null;
            if (windDTO.measureTimepoint != null) {
                at = new MillisecondsTimePoint(windDTO.measureTimepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDTO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.trueWindSpeedInKnots);
            } else if (windDTO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDTO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.dampenedTrueWindSpeedInKnots);
            } else if (windDTO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDTO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindBearingDeg));
                } else if (windDTO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            Iterable<WindSource> webWindSources = trackedRace.getWindSources(WindSourceType.WEB);
            if (Util.size(webWindSources) == 0) {
                // create a new WEB wind source if not available
                trackedRace.recordWind(wind, new WindSourceImpl(WindSourceType.WEB));
            } else {
                trackedRace.recordWind(wind, webWindSources.iterator().next());
            }
        }
    }

    @Override
    public Map<CompetitorDTO, BoatDTO> getCompetitorBoats(RegattaAndRaceIdentifier raceIdentifier) {
        Map<CompetitorDTO, BoatDTO> result = null;
        TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = baseDomainFactory.convertToCompetitorAndBoatDTOs(trackedRace.getRace().getCompetitorsAndTheirBoats());
        }
        return result;
    }

    @Override
    public RaceboardDataDTO getRaceboardData(String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId) {
        RaceboardDataDTO result = new RaceboardDataDTO(null, false, false, Collections.emptyList(),
                Collections.emptyList(), null);
        RaceWithCompetitorsAndBoatsDTO raceDTO = null;
        Regatta regatta = getService().getRegattaByName(regattaName);
        if (regatta != null) {
            RaceDefinition race = regatta.getRaceByName(raceName);
            if (race != null) {
                RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), race.getName());
                TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
                getSecurityService().checkCurrentUserReadPermission(trackedRace);
                if (trackedRace != null) {
                    Map<CompetitorDTO, BoatDTO> competitorsAndBoats = baseDomainFactory
                            .convertToCompetitorAndBoatDTOs(race.getCompetitorsAndTheirBoats());
                    TrackedRaceDTO trackedRaceDTO = getBaseDomainFactory().createTrackedRaceDTO(trackedRace);
                    raceDTO = new RaceWithCompetitorsAndBoatsDTO(raceIdentifier, competitorsAndBoats, trackedRaceDTO,
                            getService().isRaceBeingTracked(regatta, race));
                    if (trackedRace != null) {
                        getBaseDomainFactory().updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
                    }
                    raceDTO.boatClass = regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName();

                    Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
                    LeaderboardGroup leaderboardGroup = leaderboardGroupName != null
                            ? getService().getLeaderboardGroupByName(leaderboardGroupName)
                            : null;
                    Event event = eventId != null ? getService().getEvent(eventId) : null;
                    if (!getSecurityService().hasCurrentUserReadPermission(event)) {
                        event = null;
                    }

                    boolean isValidLeaderboardGroup = false;
                    if (leaderboardGroup != null) {
                        for (Leaderboard leaderboardInGroup : leaderboardGroup.getLeaderboards()) {
                            if (leaderboardInGroup.getName().equals(leaderboard.getName())) {
                                isValidLeaderboardGroup = true;
                                break;
                            }
                        }
                    }
                    boolean isValidEvent = event != null;
                    if (event != null && leaderboardGroup != null) {
                        isValidEvent = false;
                        for (LeaderboardGroup leaderboardGroupInEvent : event.getLeaderboardGroups()) {
                            if (leaderboardGroupInEvent.getId().equals(leaderboardGroup.getId())) {
                                isValidEvent = true;
                                break;
                            }
                        }
                    }
                    Iterable<DetailType> detailTypesForCompetitorChart = determineDetailTypesForCompetitorChart(
                            leaderboardGroupName, raceDTO.getRaceIdentifier());
                    Iterable<DetailType> availableDetailTypesForLeaderboard = getAvailableDetailTypesForLeaderboard(
                            leaderboardName, raceDTO.getRaceIdentifier());
                    StrippedLeaderboardDTO leaderboardDTO = createStrippedLeaderboardDTO(leaderboard, false, false);
                    result = new RaceboardDataDTO(raceDTO, isValidLeaderboardGroup, isValidEvent,
                            detailTypesForCompetitorChart, availableDetailTypesForLeaderboard, leaderboardDTO);
                }
            }
        }
        return result;
    }

    @Override
    public CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate, LegIdentifier simulationLegIdentifier,
            byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID,
            Date timeToGetTheEstimatedDurationFor, boolean estimatedDurationRequired) throws NoWindException {
        final HashSet<String> raceCompetitorIdsAsStrings;
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        getSecurityService().checkCurrentUserReadPermission(trackedRace);

        // if md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID is null, Arrays.equals will return false, and the
        // competitor set will be calculated and returned to the client
        if (trackedRace == null || Arrays.equals(md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID, trackedRace.getRace().getCompetitorMD5())) {
            raceCompetitorIdsAsStrings = null; // tracked race not found or still same MD5 hash, suggesting unchanged competitor set
        } else {
            raceCompetitorIdsAsStrings = new HashSet<>();
            for (final Competitor c : trackedRace.getRace().getCompetitors()) {
                raceCompetitorIdsAsStrings.add(c.getId().toString());
            }
        }
        final Duration estimatedDuration;
        if (estimatedDurationRequired) {
            estimatedDuration = getEstimationForTargetTime(timeToGetTheEstimatedDurationFor, trackedRace);
        } else {
            estimatedDuration = null;
        }
        final Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> boatPositions = getBoatPositionsInternal(raceIdentifier,
                fromPerCompetitorIdAsString, toPerCompetitorIdAsString, extrapolate);
        final CoursePositionsDTO coursePositions = getCoursePositions(raceIdentifier, date);
        final List<SidelineDTO> courseSidelines = getCourseSidelines(raceIdentifier, date);
        final QuickRanksDTO quickRanks = getQuickRanks(raceIdentifier, date);
        long simulationResultVersion = 0;
        if (simulationLegIdentifier != null) {
            SimulationService simulationService = getService().getSimulationService();
            simulationResultVersion = simulationService.getSimulationResultsVersion(simulationLegIdentifier);
        }
        return new CompactRaceMapDataDTO(boatPositions, coursePositions, courseSidelines, quickRanks,
                simulationResultVersion, raceCompetitorIdsAsStrings, estimatedDuration);
    }

    private Duration getEstimationForTargetTime(Date time, final TrackedRace trackedRace) {
        Duration estimatedDuration = null;
        if (trackedRace != null) {
            try {
                estimatedDuration = trackedRace.getEstimatedTimeToComplete(new MillisecondsTimePoint(time)).getExpectedDuration();
            } catch (NotEnoughDataHasBeenAddedException | NoWindException e) {
                logger.log(Level.WARNING, "Problem computing the estimated race duration", e);
            }
        }
        return estimatedDuration;
    }

    @Override
    public CompactBoatPositionsDTO getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        return new CompactBoatPositionsDTO(getBoatPositionsInternal(raceIdentifier, fromPerCompetitorIdAsString, toPerCompetitorIdAsString, extrapolate));
    }

    /**
     * {@link LegType}s are cached within the method with a resolution of one minute. The cache key is a pair of
     * {@link TrackedLegOfCompetitor} and {@link TimePoint}.
     * 
     * @param from
     *            for the list of competitors provided as keys of this map, requests the GPS fixes starting with the
     *            date provided as value
     * @param to
     *            for the list of competitors provided as keys (expected to be equal to the set of competitors used as
     *            keys in the <code>from</code> parameter, requests the GPS fixes up to but excluding (except
     *            {@code extrapolate} is {@code true}) the date provided as value
     * @param extrapolate
     *            if <code>true</code> and no (exact or interpolated) position is known for <code>to</code>, the last
     *            entry returned in the list of GPS fixes will be obtained by extrapolating from the competitors last
     *            known position at <code>to</code> and the estimated speed. With this, the {@code to} time point is no
     *            longer exclusive.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    private Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> getBoatPositionsInternal(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate)
            throws NoWindException {
        Map<Pair<Leg, TimePoint>, LegType> legTypeCache = new HashMap<>();
        Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> result = new HashMap<>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            getSecurityService().checkCurrentUserReadPermission(trackedRace);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                if (fromPerCompetitorIdAsString.containsKey(competitor.getId().toString())) {
                    CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                    List<GPSFixDTOWithSpeedWindTackAndLegType> fixesForCompetitor = new ArrayList<GPSFixDTOWithSpeedWindTackAndLegType>();
                    result.put(competitorDTO, fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(fromPerCompetitorIdAsString.get(competitorDTO.getIdAsString()));
                    TimePoint toTimePointExcluding = new MillisecondsTimePoint(toPerCompetitorIdAsString.get(competitorDTO.getIdAsString()));
                    // copy the fixes into a list while holding the monitor; then release the monitor to avoid deadlocks
                    // during wind estimations required for tack determination
                    List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
                    track.lockForRead();
                    try {
                        Iterator<GPSFixMoving> fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */true);
                        while (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            if (fix.getTimePoint().before(toTimePointExcluding) ||
                                    (extrapolate && fix.getTimePoint().equals(toTimePointExcluding))) {
                                logger.finest(()->""+competitor.getName()+": " + fix);
                                fixes.add(fix);
                            } else {
                                break;
                            }
                        }
                    } finally {
                        track.unlockAfterRead();
                    }
                    final Set<GPSFixMoving> extrapolatedFixes;
                    if (fixes.isEmpty()) {
                        // then there was no (smoothened) fix between fromTimePoint and toTimePointExcluding; estimate...
                        TimePoint middle = new MillisecondsTimePoint((toTimePointExcluding.asMillis()+fromTimePoint.asMillis())/2);
                        Position estimatedPosition = track.getEstimatedPosition(middle, extrapolate);
                        SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(middle);
                        if (estimatedPosition != null && estimatedSpeed != null) {
                            GPSFixMoving estimatedFix = new GPSFixMovingImpl(estimatedPosition, middle, estimatedSpeed);
                            if (logger.getLevel() != null && logger.getLevel().equals(Level.FINEST)) {
                                logger.finest(""+competitor.getName()+": " + estimatedFix+" (estimated)");
                            }
                            fixes.add(estimatedFix);
                            extrapolatedFixes = Collections.singleton(estimatedFix);
                        } else {
                            extrapolatedFixes = Collections.emptySet();
                        }
                    } else {
                        extrapolatedFixes = Collections.emptySet();
                    }
                    Iterator<GPSFixMoving> fixIter = fixes.iterator();
                    if (fixIter.hasNext()) {
                        GPSFixMoving fix = fixIter.next();
                        while (fix != null && (fix.getTimePoint().before(toTimePointExcluding) ||
                                (fix.getTimePoint().equals(toTimePointExcluding) && toTimePointExcluding.equals(fromTimePoint)))) {
                            Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
                            final SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(fix.getTimePoint());
                            Tack tack = wind == null? null : trackedRace.getTack(estimatedSpeed, wind, fix.getTimePoint());
                            TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                                    fix.getTimePoint());
                            LegType legType;
                            if (trackedLegOfCompetitor != null && trackedLegOfCompetitor.getLeg() != null) {
                                TimePoint quantifiedTimePoint = quantifyTimePointWithResolution(fix.getTimePoint(), /* resolutionInMilliseconds */60000);
                                Pair<Leg, TimePoint> cacheKey = new Pair<Leg, TimePoint>(trackedLegOfCompetitor.getLeg(), quantifiedTimePoint);
                                legType = legTypeCache.get(cacheKey);
                                if (legType == null) {
                                    try {
                                        legType = trackedRace.getTrackedLeg(trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                                        legTypeCache.put(cacheKey, legType);
                                    } catch (NoWindException nwe) {
                                        // without wind, leave the leg type null, meaning "unknown"
                                        legType = null;
                                    }
                                }
                            } else {
                                legType = null;
                            }
                            WindDTO windDTO = wind == null ? null : createWindDTOFromAlreadyAveraged(wind, toTimePointExcluding);
                            GPSFixDTOWithSpeedWindTackAndLegType fixDTO = createGPSFixDTO(fix, estimatedSpeed, windDTO, tack, legType, /* extrapolate */ extrapolatedFixes.contains(fix));
                            fixesForCompetitor.add(fixDTO);
                            if (fixIter.hasNext()) {
                                fix = fixIter.next();
                            } else {
                                // check if fix was at date and if extrapolation is requested; 
                                if (!fix.getTimePoint().equals(toTimePointExcluding) && extrapolate) {
                                    Position position = track.getEstimatedPosition(toTimePointExcluding, extrapolate);
                                    Wind wind2 = trackedRace.getWind(position, toTimePointExcluding);
                                    SpeedWithBearing estimatedSpeed2 = track.getEstimatedSpeed(toTimePointExcluding);
                                    Tack tack2 = wind2 == null ? null : trackedRace.getTack(estimatedSpeed2, wind2, toTimePointExcluding);
                                    LegType legType2;
                                    if (trackedLegOfCompetitor != null && trackedLegOfCompetitor.getLeg() != null) {
                                        TimePoint quantifiedTimePoint = quantifyTimePointWithResolution(
                                                fix.getTimePoint(), /* resolutionInMilliseconds */
                                                60000);
                                        Pair<Leg, TimePoint> cacheKey = new Pair<Leg, TimePoint>(
                                                trackedLegOfCompetitor.getLeg(), quantifiedTimePoint);
                                        legType2 = legTypeCache.get(cacheKey);
                                        if (legType2 == null) {
                                            try {
                                                legType2 = trackedRace.getTrackedLeg(trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
                                                legTypeCache.put(cacheKey, legType2);
                                            } catch (NoWindException nwe) {
                                                // no wind information; leave leg type null, meaning "unknown"
                                                legType2 = null;
                                            }
                                        }
                                    } else {
                                        legType2 = null;
                                    }
                                    WindDTO windDTO2 = wind2 == null ? null : createWindDTOFromAlreadyAveraged(wind2, toTimePointExcluding);
                                    GPSFixDTOWithSpeedWindTackAndLegType extrapolated = new GPSFixDTOWithSpeedWindTackAndLegType(
                                            toPerCompetitorIdAsString.get(competitorDTO.getIdAsString()),
                                            position==null?null:position,
                                                    estimatedSpeed2==null?null:createSpeedWithBearingDTO(estimatedSpeed2), windDTO2,
                                                            tack2, legType2, /* extrapolated */ true);
                                    fixesForCompetitor.add(extrapolated);
                                }
                                fix = null;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private TimePoint quantifyTimePointWithResolution(TimePoint timePoint, long resolutionInMilliseconds) {
        return new MillisecondsTimePoint(timePoint.asMillis() / resolutionInMilliseconds * resolutionInMilliseconds);
    }

    private SpeedWithBearingDTO createSpeedWithBearingDTO(SpeedWithBearing speedWithBearing) {
        return new SpeedWithBearingDTO(speedWithBearing.getKnots(), speedWithBearing
                .getBearing().getDegrees());
    }

    private GPSFixDTOWithSpeedWindTackAndLegType createGPSFixDTO(GPSFix fix, SpeedWithBearing speedWithBearing, WindDTO windDTO, Tack tack, LegType legType, boolean extrapolated) {
        return new GPSFixDTOWithSpeedWindTackAndLegType(fix.getTimePoint().asDate(), fix.getPosition()==null?null:fix.getPosition(),
                speedWithBearing==null?null:createSpeedWithBearingDTO(speedWithBearing), windDTO, tack, legType, extrapolated);
    }

    @Override
    public RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier) {
        RaceTimesInfoDTO raceTimesInfo = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);

        if (trackedRace != null) {
            getSecurityService().checkCurrentUserReadPermission(trackedRace);
            raceTimesInfo = new RaceTimesInfoDTO(raceIdentifier);
            List<LegInfoDTO> legInfos = new ArrayList<LegInfoDTO>();
            raceTimesInfo.setLegInfos(legInfos);
            List<MarkPassingTimesDTO> markPassingTimesDTOs = new ArrayList<MarkPassingTimesDTO>();
            raceTimesInfo.setMarkPassingTimes(markPassingTimesDTOs);

            raceTimesInfo.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
            raceTimesInfo.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
            raceTimesInfo.newestTrackingEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
            raceTimesInfo.endOfTracking = trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asDate();
            raceTimesInfo.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
            raceTimesInfo.raceFinishedTime = trackedRace.getFinishedTime() == null ? null : trackedRace.getFinishedTime().asDate();
            raceTimesInfo.delayToLiveInMs = trackedRace.getDelayToLiveInMillis();

            Iterable<com.sap.sse.common.Util.Pair<Waypoint, com.sap.sse.common.Util.Pair<TimePoint, TimePoint>>> markPassingsTimes = trackedRace.getMarkPassingsTimes();
            synchronized (markPassingsTimes) {
                int numberOfWaypoints = Util.size(markPassingsTimes);
                int wayPointNumber = 1;
                for (com.sap.sse.common.Util.Pair<Waypoint, com.sap.sse.common.Util.Pair<TimePoint, TimePoint>> markPassingTimes : markPassingsTimes) {
                    MarkPassingTimesDTO markPassingTimesDTO = new MarkPassingTimesDTO();
                    String name = "M" + (wayPointNumber - 1);
                    if (wayPointNumber == numberOfWaypoints) {
                        name = "F";
                    }
                    markPassingTimesDTO.setName(name);
                    com.sap.sse.common.Util.Pair<TimePoint, TimePoint> timesPair = markPassingTimes.getB();
                    TimePoint firstPassingTime = timesPair.getA();
                    TimePoint lastPassingTime = timesPair.getB();
                    markPassingTimesDTO.firstPassingDate = firstPassingTime == null ? null : firstPassingTime.asDate();
                    markPassingTimesDTO.lastPassingDate = lastPassingTime == null ? null : lastPassingTime.asDate();
                    markPassingTimesDTOs.add(markPassingTimesDTO);
                    wayPointNumber++;
                }
            }
            trackedRace.getRace().getCourse().lockForRead();
            try {
                Iterable<TrackedLeg> trackedLegs = trackedRace.getTrackedLegs();
                int legNumber = 1;
                for (TrackedLeg trackedLeg : trackedLegs) {
                    LegInfoDTO legInfoDTO = new LegInfoDTO(legNumber);
                    legInfoDTO.setName("L" + legNumber);
                    try {
                        MarkPassingTimesDTO markPassingTimesDTO = markPassingTimesDTOs.get(legNumber - 1);
                        if (markPassingTimesDTO.firstPassingDate != null) {
                            TimePoint p = new MillisecondsTimePoint(markPassingTimesDTO.firstPassingDate);
                            legInfoDTO.legType = trackedLeg.getLegType(p);
                            legInfoDTO.legBearingInDegrees = trackedLeg.getLegBearing(p).getDegrees();
                        }
                    } catch (NoWindException e) {
                        // do nothing
                    }
                    legInfos.add(legInfoDTO);
                    legNumber++;
                }
            } finally {
                trackedRace.getRace().getCourse().unlockAfterRead();
            }
        }   
        if (raceTimesInfo != null) {
            raceTimesInfo.currentServerTime = new Date();
        }
        return raceTimesInfo;
    }

    @Override
    public List<RaceTimesInfoDTO> getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers) {
        List<RaceTimesInfoDTO> raceTimesInfos = new ArrayList<RaceTimesInfoDTO>();
        for (RegattaAndRaceIdentifier raceIdentifier : raceIdentifiers) {
            TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
            if (trackedRace == null || getSecurityService().hasCurrentUserReadPermission(trackedRace)) {
                RaceTimesInfoDTO raceTimesInfo = getRaceTimesInfo(raceIdentifier);
                if (raceTimesInfo != null) {
                    raceTimesInfos.add(raceTimesInfo);
                }
            }
        }
        return raceTimesInfos;
    }

    private List<SidelineDTO> getCourseSidelines(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        List<SidelineDTO> result = new ArrayList<SidelineDTO>();
        final TimePoint dateAsTimePoint;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            if (date == null) {
                dateAsTimePoint = MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
            } else {
                dateAsTimePoint = new MillisecondsTimePoint(date);
            }
            for (Sideline sideline : trackedRace.getCourseSidelines()) {
                List<MarkDTO> markDTOs = new ArrayList<MarkDTO>();
                for (Mark mark : sideline.getMarks()) {
                    GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                    Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                    if (positionAtDate != null) {
                        markDTOs.add(convertToMarkDTO(mark, positionAtDate));
                    }
                }
                result.add(new SidelineDTO(sideline.getName(), markDTOs));
            }
        }
        return result;
    }
        
    @Override
    public CoursePositionsDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        CoursePositionsDTO result = new CoursePositionsDTO();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            getSecurityService().checkCurrentUserReadPermission(trackedRace);
            final TimePoint dateAsTimePoint;
            if (date == null) {
                dateAsTimePoint = MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
            } else {
                dateAsTimePoint = new MillisecondsTimePoint(date);
            }
            result.totalLegsCount = trackedRace.getRace().getCourse().getLegs().size();
            result.currentLegNumber = trackedRace.getLastLegStarted(dateAsTimePoint);
            result.marks = new HashSet<MarkDTO>();
            result.course = convertToRaceCourseDTO(trackedRace.getRace().getCourse(), new TrackedRaceMarkPositionFinder(trackedRace), dateAsTimePoint);
            // now make sure we don't duplicate the MarkDTO objects but instead use the ones from the RaceCourseDTO
            // object and amend them with the Position
            result.waypointPositions = new ArrayList<>();
            Set<Mark> marks = new HashSet<Mark>();
            Course course = trackedRace.getRace().getCourse();
            for (Waypoint waypoint : course.getWaypoints()) {
                Position waypointPosition = trackedRace.getApproximatePosition(waypoint, dateAsTimePoint);
                if (waypointPosition != null) {
                    result.waypointPositions.add(waypointPosition);
                }
                for (Mark b : waypoint.getMarks()) {
                    marks.add(b);
                }
            }
            for (final WaypointDTO waypointDTO : result.course.waypoints) {
                for (final MarkDTO markDTO : waypointDTO.controlPoint.getMarks()) {
                    if (markDTO.position != null) {
                        result.marks.add(markDTO);
                    }
                }
            }

            // set the positions of start and finish
            Waypoint firstWaypoint = course.getFirstWaypoint();
            if (firstWaypoint != null && Util.size(firstWaypoint.getMarks()) == 2) {
                final LineDetails markPositionDTOsAndLineAdvantage = trackedRace.getStartLine(dateAsTimePoint);
                if (markPositionDTOsAndLineAdvantage != null) {
                    result.startLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                    Bearing angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind = markPositionDTOsAndLineAdvantage
                            .getAngleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind();
                    result.startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind = angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind == null ? null
                            : angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind.getDegrees();
                    result.startLineAdvantageousSide = markPositionDTOsAndLineAdvantage
                            .getAdvantageousSideWhileApproachingLine();
                    Distance advantage = markPositionDTOsAndLineAdvantage.getAdvantage();
                    result.startLineAdvantageInMeters = advantage == null ? null : advantage.getMeters();
                }
            }
            Waypoint lastWaypoint = course.getLastWaypoint();
            if (lastWaypoint != null && Util.size(lastWaypoint.getMarks()) == 2) {
                final LineDetails markPositionDTOsAndLineAdvantage = trackedRace.getFinishLine(dateAsTimePoint);
                if (markPositionDTOsAndLineAdvantage != null) {
                    result.finishLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                    Bearing angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind = markPositionDTOsAndLineAdvantage
                            .getAngleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind();
                    result.finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind = angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind == null ? null
                            : angleDifferenceFromPortToStarboardWhenApproachingLineToTrueWind.getDegrees();
                    result.finishLineAdvantageousSide = markPositionDTOsAndLineAdvantage
                            .getAdvantageousSideWhileApproachingLine();
                    Distance advantage = markPositionDTOsAndLineAdvantage.getAdvantage();
                    result.finishLineAdvantageInMeters = advantage == null ? null : advantage.getMeters();
                }
            }
        }
        return result;
    }
      
    @Override
    public RaceCourseDTO getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date) {
        List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
        Map<Serializable, ControlPointDTO> controlPointCache = new HashMap<>();
        TimePoint dateAsTimePoint = new MillisecondsTimePoint(date);
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        List<MarkDTO> allMarks = new ArrayList<>();
        if (trackedRace != null) {
            getSecurityService().checkCurrentUserReadPermission(trackedRace);
            for (Mark mark : trackedRace.getMarks()) {
                Position pos = trackedRace.getOrCreateTrack(mark).getEstimatedPosition(dateAsTimePoint, false);
                allMarks.add(convertToMarkDTO(mark, pos));
            }
            Course course = trackedRace.getRace().getCourse();
            for (Waypoint waypoint : course.getWaypoints()) {
                ControlPointDTO controlPointDTO = controlPointCache.get(waypoint.getControlPoint().getId());
                if (controlPointDTO == null) {
                    controlPointDTO = convertToControlPointDTO(waypoint.getControlPoint(), new TrackedRaceMarkPositionFinder(trackedRace), dateAsTimePoint);
                    controlPointCache.put(waypoint.getControlPoint().getId(), controlPointDTO);
                }
                WaypointDTO waypointDTO = new WaypointDTO(waypoint.getName(), controlPointDTO,
                        waypoint.getPassingInstructions());
                waypointDTOs.add(waypointDTO);
            }
        }
        return new RaceCourseDTO(waypointDTOs, allMarks);
    }
    
    class TrackedRaceMarkPositionFinder implements MarkPositionFinder{
        private TrackedRace trackedRace;

        public TrackedRaceMarkPositionFinder(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }
        
        @Override
        public Position find(Mark mark, TimePoint at) {
            final TimePoint timePointToUse = trackedRace == null ? null :
                at == null ? MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis()) : at;
            final Position result;
            if (timePointToUse == null) {
                result = null;
            } else {
                result = trackedRace.getOrCreateTrack(mark).getEstimatedPosition(timePointToUse, /* extrapolate */ false);
            }
            return result;
        }
    }
    
    private interface MarkPositionFinder {
        Position find(Mark mark, TimePoint at);
    }
    
    private ControlPointDTO convertToControlPointDTO(ControlPoint controlPoint, MarkPositionFinder positionFinder, TimePoint timePoint) {
        ControlPointDTO result;
        
        if (controlPoint instanceof ControlPointWithTwoMarks) {
            final Mark left = ((ControlPointWithTwoMarks) controlPoint).getLeft();
            final Position leftPos =  positionFinder.find(left, timePoint);
            final Mark right = ((ControlPointWithTwoMarks) controlPoint).getRight();
            final Position rightPos = positionFinder.find(right, timePoint);
            result = new GateDTO(controlPoint.getId().toString(), controlPoint.getName(), convertToMarkDTO(left, leftPos), convertToMarkDTO(right, rightPos)); 
        } else {
            Mark mark = controlPoint.getMarks().iterator().next();
            final Position position = positionFinder.find(mark, timePoint);
            result = convertToMarkDTO(mark, position);
        }
        return result;
    }
    
    private ControlPoint getOrCreateControlPoint(ControlPointDTO dto) {
        String idAsString = dto.getIdAsString();
        if (idAsString == null) {
            idAsString = UUID.randomUUID().toString();
        }
        if (dto instanceof GateDTO) {
            GateDTO gateDTO = (GateDTO) dto;
            Mark left = (Mark) getOrCreateControlPoint(gateDTO.getLeft());
            Mark right = (Mark) getOrCreateControlPoint(gateDTO.getRight());
            return baseDomainFactory.getOrCreateControlPointWithTwoMarks(idAsString, gateDTO.getName(), left, right);
        } else {
            MarkDTO markDTO = (MarkDTO) dto;
            return baseDomainFactory.getOrCreateMark(idAsString, dto.getName(), markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
        }
    }

    /**
     * Creates new ControlPoints, if nThe resulting
     * list of control points is then passed to {@link Course#update(List, com.sap.sailing.domain.base.DomainFactory)} for
     * the course of the race identified by <code>raceIdentifier</code>.
     */
    @Override
    public void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier,
            List<Pair<ControlPointDTO, PassingInstruction>> courseDTO) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            getSecurityService().checkCurrentUserUpdatePermission(trackedRace);
            Course course = trackedRace.getRace().getCourse();
            List<Pair<ControlPoint, PassingInstruction>> controlPoints = new ArrayList<>();
            for (Pair<ControlPointDTO, PassingInstruction> waypointDTO : courseDTO) {
                controlPoints.add(new Pair<>(getOrCreateControlPoint(waypointDTO.getA()), waypointDTO.getB()));
            }
            try {
                course.update(controlPoints, baseDomainFactory);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @param timePoint
     *            <code>null</code> means "live" and is then replaced by "now" minus the tracked race's
     *            {@link TrackedRace#getDelayToLiveInMillis() delay}.
     */
    public QuickRanksDTO computeQuickRanks(RegattaAndRaceIdentifier raceIdentifier, TimePoint timePoint)
            throws NoWindException {
        final List<QuickRankDTO> result = new ArrayList<>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            final TimePoint actualTimePoint;
            if (timePoint == null) {
                actualTimePoint = MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
            } else {
                actualTimePoint = timePoint;
            }
            final RaceDefinition race = trackedRace.getRace();
            int oneBasedRank = 1;
            final List<Competitor> competitorsFromBestToWorst = trackedRace.getCompetitorsFromBestToWorst(actualTimePoint);
            for (Competitor competitor : competitorsFromBestToWorst) {
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, actualTimePoint);
                if (trackedLeg != null) {
                    int legNumberOneBased = race.getCourse().getLegs().indexOf(trackedLeg.getLeg()) + 1;
                    Boat boatOfCompetitor = trackedRace.getBoatOfCompetitor(competitor);
                    QuickRankDTO quickRankDTO = new QuickRankDTO(
                            baseDomainFactory.convertToCompetitorAndBoatDTO(competitor, boatOfCompetitor).getCompetitor(),
                            oneBasedRank, legNumberOneBased);
                    result.add(quickRankDTO);
                }
                oneBasedRank++;
            }
        }
        return new QuickRanksDTO(result);
    }

    private QuickRanksDTO getQuickRanks(RegattaAndRaceIdentifier raceIdentifier, Date date) throws NoWindException {
        final QuickRanksDTO result;
        if (date == null) {
            result = quickRanksLiveCache.get(raceIdentifier);
        } else {
            result = computeQuickRanks(raceIdentifier, new MillisecondsTimePoint(date));
        }
        return result;
    }

    @Override
    public void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind) {
        getService().apply(new SetRaceIsKnownToStartUpwind(raceIdentifier, raceIsKnownToStartUpwind));
    }

    @Override
    public void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude) {
        getService().apply(new SetWindSourcesToExclude(raceIdentifier, windSourcesToExclude));
    }

    @Override
    public WindInfoForRaceDTO getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier) {
        WindInfoForRaceDTO result = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            result = new WindInfoForRaceDTO();
            result.raceIsKnownToStartUpwind = trackedRace.raceIsKnownToStartUpwind();
            List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
            for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                windSourcesToExclude.add(windSourceToExclude);
            }
            result.windSourcesToExclude = windSourcesToExclude;
            Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
            result.windTrackInfoByWindSource = windTrackInfoDTOs;

            for (WindSource windSource: trackedRace.getWindSources()) {
                windTrackInfoDTOs.put(windSource, new WindTrackInfoDTO());
            }
            windTrackInfoDTOs.put(new WindSourceImpl(WindSourceType.COMBINED), new WindTrackInfoDTO());
        }
        return result;
    }

    @Override
    public void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO) {
        DynamicTrackedRace trackedRace = (DynamicTrackedRace) getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Position p = null;
            if (windDTO.position != null) {
                p = windDTO.position;
            }
            TimePoint at = null;
            if (windDTO.measureTimepoint != null) {
                at = new MillisecondsTimePoint(windDTO.measureTimepoint);
            }
            SpeedWithBearing speedWithBearing = null;
            Speed speed = null;
            if (windDTO.trueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.trueWindSpeedInKnots);
            } else if (windDTO.trueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.trueWindSpeedInMetersPerSecond * 3600. / 1000.);
            } else if (windDTO.dampenedTrueWindSpeedInKnots != null) {
                speed = new KnotSpeedImpl(windDTO.dampenedTrueWindSpeedInKnots);
            } else if (windDTO.dampenedTrueWindSpeedInMetersPerSecond != null) {
                speed = new KilometersPerHourSpeedImpl(windDTO.dampenedTrueWindSpeedInMetersPerSecond * 3600. / 1000.);
            }
            if (speed != null) {
                if (windDTO.trueWindBearingDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindBearingDeg));
                } else if (windDTO.trueWindFromDeg != null) {
                    speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), new DegreeBearingImpl(
                            windDTO.trueWindFromDeg).reverse());
                }
            }
            Wind wind = new WindImpl(p, at, speedWithBearing);
            trackedRace.removeWind(wind, trackedRace.getWindSources(WindSourceType.WEB).iterator().next());
        }
    }

    protected RacingEventService getService() {
        return racingEventServiceTracker.getService(); // grab the service
    }

    protected ReplicationService getReplicationService() {
        return replicationServiceTracker.getService();
    }
    
    protected SecurityService getSecurityService() {
        return securityServiceTracker.getService();
    }
    
    @Override
    public List<String> getLeaderboardNames() {
        return new ArrayList<String>(getService().getLeaderboards().keySet());
    }

    @Override
    public LeaderboardType getLeaderboardType(String leaderboardName) {
        final LeaderboardType result;
        final Leaderboard leaderboard = getService().getLeaderboards().get(leaderboardName);
        if (leaderboard != null) {
            getSecurityService().checkCurrentUserReadPermission(leaderboard);
            result = leaderboard.getLeaderboardType();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public StrippedLeaderboardDTO createFlexibleLeaderboard(String leaderboardName,
            String leaderboardDisplayName, int[] discardThresholds, ScoringSchemeType scoringSchemeType,
            UUID courseAreaId) {
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.LEADERBOARD,
                leaderboardName, leaderboardDisplayName, new ActionWithResult<StrippedLeaderboardDTO>() {
                    @Override
                    public StrippedLeaderboardDTO run() throws Exception {
                        return createStrippedLeaderboardDTO(getService().apply(new CreateFlexibleLeaderboard(leaderboardName, leaderboardDisplayName, discardThresholds,
                                baseDomainFactory.createScoringScheme(scoringSchemeType), courseAreaId)), false, false);
                    }
                });
    }

    @Override
    public StrippedLeaderboardDTO createRegattaLeaderboard(RegattaName regattaIdentifier, String leaderboardDisplayName,
            int[] discardThresholds) {
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.LEADERBOARD, regattaIdentifier.getRegattaName(), leaderboardDisplayName,
                new ActionWithResult<StrippedLeaderboardDTO>() {
                    @Override
                    public StrippedLeaderboardDTO run() throws Exception {
                        return createStrippedLeaderboardDTO(getService().apply(new CreateRegattaLeaderboard(
                                regattaIdentifier, leaderboardDisplayName, discardThresholds)), false, false);
                    }
                });
    }

    @Override
    public StrippedLeaderboardDTO createRegattaLeaderboardWithEliminations(String name,
            String displayName,
            String fullRegattaLeaderboardName) {
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.LEADERBOARD, name, displayName, new ActionWithResult<StrippedLeaderboardDTO>() {
                    @Override
                    public StrippedLeaderboardDTO run() throws Exception {
                        return createStrippedLeaderboardDTO(
                                getService().apply(new CreateRegattaLeaderboardWithEliminations(name, displayName,
                                        fullRegattaLeaderboardName)),
                                false, false);
                    }
                });
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboards() {
        final Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.LEADERBOARD,
                leaderboards.values(), Leaderboard::getName,
                leaderboard -> createStrippedLeaderboardDTO(leaderboard, false, false));
    }

    @Override
    public StrippedLeaderboardDTO getLeaderboard(String leaderboardName) {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        StrippedLeaderboardDTO result = null;
        Leaderboard leaderboard = leaderboards.get(leaderboardName);
        if (leaderboard != null) {
            if (leaderboard instanceof RegattaLeaderboard) {
                getSecurityService().checkCurrentUserReadPermission(((RegattaLeaderboard) leaderboard).getRegatta());
            }
            getSecurityService().checkCurrentUserReadPermission(leaderboard);
            result = createStrippedLeaderboardDTO(leaderboard, false, false);
        }
        return result;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByRaceAndRegatta(String raceName, RegattaIdentifier regattaIdentifier) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (Leaderboard leaderboard : leaderboards.values()) {
            if (leaderboard instanceof RegattaLeaderboard && ((RegattaLeaderboard) leaderboard).getRegatta().getRegattaIdentifier().equals(regattaIdentifier)) {
                Iterable<RaceColumn> races = leaderboard.getRaceColumns();
                for (RaceColumn raceInLeaderboard : races) {
                    for (Fleet fleet : raceInLeaderboard.getFleets()) {
                        TrackedRace trackedRace = raceInLeaderboard.getTrackedRace(fleet);
                        if (trackedRace != null) {
                            RaceDefinition trackedRaceDef = trackedRace.getRace();
                            if (trackedRaceDef.getName().equals(raceName)) {
                                results.add(createStrippedLeaderboardDTO(leaderboard, false, false));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Creates a {@link LeaderboardDTO} for <code>leaderboard</code> and fills in the name, race master data
     * in the form of {@link RaceColumnDTO}s, whether or not there are {@link LeaderboardDTO#hasCarriedPoints carried points}
     * and the {@link LeaderboardDTO#discardThresholds discarding thresholds} for the leaderboard. No data about the points
     * is filled into the result object. No data about the competitor display names is filled in; instead, an empty map
     * is used for {@link LeaderboardDTO#competitorDisplayNames}.<br />
     * If <code>withGeoLocationData</code> is <code>true</code> the geographical location of all races will be determined.
     */
    private StrippedLeaderboardDTO createStrippedLeaderboardDTO(Leaderboard leaderboard, boolean withGeoLocationData, boolean withStatisticalData) {
        StrippedLeaderboardDTO leaderboardDTO = new StrippedLeaderboardDTO(convertToBoatClassDTO(leaderboard.getBoatClass()));
        TimePoint startOfLatestRace = null;
        Long delayToLiveInMillisForLatestRace = null;
        leaderboardDTO.setName(leaderboard.getName());
        leaderboardDTO.displayName = leaderboard.getDisplayName();
        leaderboardDTO.competitorDisplayNames = new HashMap<>();
        leaderboardDTO.competitorsCount = Util.size(leaderboard.getCompetitors());
        leaderboardDTO.boatClassName = leaderboard.getBoatClass()==null?null:leaderboard.getBoatClass().getName();
        leaderboardDTO.type = leaderboard.getLeaderboardType();
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            Regatta regatta = regattaLeaderboard.getRegatta();
            leaderboardDTO.regattaName = regatta.getName(); 
            leaderboardDTO.scoringScheme = regatta.getScoringScheme().getType();
            leaderboardDTO.canBoatsOfCompetitorsChangePerRace = regatta.canBoatsOfCompetitorsChangePerRace();
        } else {
            leaderboardDTO.scoringScheme = leaderboard.getScoringScheme().getType();
            leaderboardDTO.canBoatsOfCompetitorsChangePerRace = false;
        }
        if (leaderboard.getDefaultCourseArea() != null) {
            leaderboardDTO.defaultCourseAreaId = leaderboard.getDefaultCourseArea().getId();
            leaderboardDTO.defaultCourseAreaName = leaderboard.getDefaultCourseArea().getName();
        }
        leaderboardDTO.setDelayToLiveInMillisForLatestRace(delayToLiveInMillisForLatestRace);
        leaderboardDTO.hasCarriedPoints = leaderboard.hasCarriedPoints();
        if (leaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) {
            leaderboardDTO.discardThresholds = ((ThresholdBasedResultDiscardingRule) leaderboard.getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces();
        } else {
            leaderboardDTO.discardThresholds = null;
        }
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceDTO raceDTO = null;
                RegattaAndRaceIdentifier raceIdentifier = null;
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                if (trackedRace != null) {
                    if (startOfLatestRace == null || (trackedRace.getStartOfRace() != null && trackedRace.getStartOfRace().compareTo(startOfLatestRace) > 0)) {
                        delayToLiveInMillisForLatestRace = trackedRace.getDelayToLiveInMillis();
                    }
                    raceIdentifier = new RegattaNameAndRaceName(trackedRace.getTrackedRegatta().getRegatta().getName(), trackedRace.getRace().getName());
                    raceDTO = baseDomainFactory.createRaceDTO(getService(), withGeoLocationData, raceIdentifier, trackedRace);
                    if(withStatisticalData) {
                        Iterable<MediaTrack> mediaTracksForRace = getService().getMediaTracksForRace(raceIdentifier);
                        raceDTO.trackedRaceStatistics = baseDomainFactory.createTrackedRaceStatisticsDTO(trackedRace, leaderboard, raceColumn, fleet, mediaTracksForRace); 
                    }
                }    
                final FleetDTO fleetDTO = baseDomainFactory.convertToFleetDTO(fleet);
                RaceColumnDTO raceColumnDTO = leaderboardDTO.addRace(raceColumn.getName(),
                        raceColumn.getExplicitFactor(), leaderboard.getScoringScheme().getScoreFactor(raceColumn),
                        raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getRegatta().getName() : null,
                        raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getSeries().getName() : null,
                        fleetDTO, raceColumn.isMedalRace(), raceIdentifier, raceDTO, raceColumn instanceof MetaLeaderboardColumn);
                final RaceLog raceLog = raceColumn.getRaceLog(fleet);
                final RaceLogTrackingState raceLogTrackingState = raceLog == null ? RaceLogTrackingState.NOT_A_RACELOG_TRACKED_RACE :
                    new RaceLogTrackingStateAnalyzer(raceLog).analyze();
                final boolean raceLogTrackerExists = raceLog == null ? false : getService().getRaceTrackerById(raceLog.getId()) != null;
                final boolean competitorRegistrationsExist = raceLog == null ? false : !Util.isEmpty(raceColumn.getAllCompetitorsAndTheirBoats(fleet).keySet());
                final boolean courseExist = raceLog == null ? false : !Util.isEmpty(raceColumn.getCourseMarks(fleet));
                final RaceLogTrackingInfoDTO raceLogTrackingInfo = new RaceLogTrackingInfoDTO(raceLogTrackerExists,
                        competitorRegistrationsExist, courseExist, raceLogTrackingState);
                raceColumnDTO.setRaceLogTrackingInfo(fleetDTO, raceLogTrackingInfo);
            }
        }
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), leaderboardDTO, leaderboard.getIdentifier());
        return leaderboardDTO;
    }

    @Override
    public StrippedLeaderboardDTO updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds, UUID newCourseAreaId) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        Leaderboard updatedLeaderboard = getService().apply(new UpdateLeaderboard(leaderboardName, newLeaderboardName, newLeaderboardDisplayName, newDiscardingThresholds, newCourseAreaId));
        return createStrippedLeaderboardDTO(updatedLeaderboard, false, false);
    }
    
    @Override
    public void removeLeaderboards(Collection<String> leaderboardNames) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.DELETE, leaderboardNames.toArray(new String[0])));
        for (String leaderoardName : leaderboardNames) {
            removeLeaderboard(leaderoardName);
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredDomainType.LEADERBOARD,
                leaderboardName, new Action() {
                    @Override
                    public void run() throws Exception {
                        getService().apply(new RemoveLeaderboard(leaderboardName));
                    }
                });
    }

    @Override
    public void renameLeaderboard(String leaderboardName, String newLeaderboardName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new RenameLeaderboard(leaderboardName, newLeaderboardName));
    }

    @Override
    public void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new AddColumnToLeaderboard(columnName, leaderboardName, medalRace));
    }

    @Override
    public void addColumnsToLeaderboard(String leaderboardName, List<com.sap.sse.common.Util.Pair<String, Boolean>> columnsToAdd) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        for(com.sap.sse.common.Util.Pair<String, Boolean> columnToAdd: columnsToAdd) {
            getService().apply(new AddColumnToLeaderboard(columnToAdd.getA(), leaderboardName, columnToAdd.getB()));
        }
    }

    @Override
    public void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        for (String columnToRemove : columnsToRemove) {
            getService().apply(new RemoveLeaderboardColumn(columnToRemove, leaderboardName));
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new RemoveLeaderboardColumn(columnName, leaderboardName));
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new RenameLeaderboardColumn(leaderboardName, oldColumnName, newColumnName));
    }

    @Override
    public void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new UpdateLeaderboardColumnFactor(leaderboardName, columnName, newFactor));
    }

    @Override
    public void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new SetSuppressedFlagForCompetitorInLeaderboard(leaderboardName, competitorIdAsString, suppressed));
    }

    @Override
    public boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            String fleetName, RegattaAndRaceIdentifier raceIdentifier) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        Object principal = SessionUtils.getPrincipal();
        if (principal != null) {
            logger.info(String.format("%s linked race column %s %s (%s) with tracked race %s.", principal.toString(),
                    leaderboardName, raceColumnName, fleetName, raceIdentifier.getRaceName()));
        } else {
            logger.info(String.format("Linked race column %s %s (%s) with tracked race %s.", leaderboardName, raceColumnName, fleetName,
                    raceIdentifier.getRaceName()));
        }
        return getService().apply(new ConnectTrackedRaceToLeaderboardColumn(leaderboardName, raceColumnName, fleetName, raceIdentifier));
    }

    @Override
    public Map<String, RegattaAndRaceIdentifier> getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.READ, leaderboardName));
        Map<String, RegattaAndRaceIdentifier> result = new HashMap<String, RegattaAndRaceIdentifier>();
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result.put(fleet.getName(), trackedRace.getRaceIdentifier());
                    } else {
                        result.put(fleet.getName(), null);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new DisconnectLeaderboardColumnFromTrackedRace(leaderboardName, raceColumnName, fleetName));
    }

    @Override
    public void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new UpdateLeaderboardCarryValue(leaderboardName, competitorIdAsString, carriedPoints));
    }

    @Override
    public com.sap.sse.common.Util.Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString, String raceColumnName,
            MaxPointsReason maxPointsReason, Date date) throws NoWindException {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        return getService().apply(
                new UpdateLeaderboardMaxPointsReason(leaderboardName, raceColumnName, competitorIdAsString,
                        maxPointsReason, new MillisecondsTimePoint(date)));
    }

    @Override
    public com.sap.sse.common.Util.Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName,
            String competitorIdAsString, String columnName, Double correctedScore, Date date) throws NoWindException {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        return getService().apply(
                new UpdateLeaderboardScoreCorrection(leaderboardName, columnName, competitorIdAsString, correctedScore,
                        new MillisecondsTimePoint(date)));
    }

    @Override
    public void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity, String comment) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(
                new UpdateLeaderboardScoreCorrectionMetadata(leaderboardName,
                        timePointOfLastCorrectionValidity == null ? null : new MillisecondsTimePoint(timePointOfLastCorrectionValidity),
                                comment));
    }

    @Override
    public void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates) throws NoWindException {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, updates.getLeaderboardName()));
        Date dateForResults = new Date(); // we don't care about the result date/time here; use current date as default
        for (Map.Entry<String, Map<String, Double>> e : updates.getScoreUpdatesForRaceColumnByCompetitorIdAsString().entrySet()) {
            for (Map.Entry<String, Double> raceColumnNameAndCorrectedScore : e.getValue().entrySet()) {
                updateLeaderboardScoreCorrection(updates.getLeaderboardName(), e.getKey(),
                        raceColumnNameAndCorrectedScore.getKey(), raceColumnNameAndCorrectedScore.getValue(), dateForResults);
            }
        }
        for (Map.Entry<String, Map<String, MaxPointsReason>> e : updates.getMaxPointsUpdatesForRaceColumnByCompetitorIdAsString().entrySet()) {
            for (Map.Entry<String, MaxPointsReason> raceColumnNameAndNewMaxPointsReason : e.getValue().entrySet()) {
                updateLeaderboardMaxPointsReason(updates.getLeaderboardName(), e.getKey(),
                        raceColumnNameAndNewMaxPointsReason.getKey(), raceColumnNameAndNewMaxPointsReason.getValue(), dateForResults);
            }
        }
    }

    @Override
    public void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAsString, String displayName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new UpdateCompetitorDisplayNameInLeaderboard(leaderboardName, competitorIdAsString, displayName));
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new MoveLeaderboardColumnUp(leaderboardName, columnName));
    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new MoveLeaderboardColumnDown(leaderboardName, columnName));
    }

    @Override
    public void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        getService().apply(new UpdateIsMedalRace(leaderboardName, columnName, isMedalRace));
    }

    @Override
    public void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs) {
        getService().apply(new UpdateRaceDelayToLive(regattaAndRaceIdentifier, delayToLiveInMs));
    }

    @Override
    public void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            getService().apply(new UpdateRaceDelayToLive(regattaAndRaceIdentifier, delayToLiveInMs));
        }
    }

    @Override
    public List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations() {
        Iterable<SwissTimingConfiguration> configs = swissTimingAdapterPersistence.getSwissTimingConfigurations();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.SWISS_TIMING_ACCOUNT,
                configs, SwissTimingConfiguration::getName,
                stConfig -> new SwissTimingConfigurationDTO(stConfig.getName(), stConfig.getJsonURL(),
                        stConfig.getHostname(), stConfig.getPort(), stConfig.getUpdateURL(),
                        stConfig.getUpdateUsername(), stConfig.getUpdatePassword()));
    }

    @Override
    public SwissTimingEventRecordDTO getRacesOfSwissTimingEvent(String eventJsonURL) 
            throws UnknownHostException, IOException, InterruptedException, ParseException {
        SwissTimingEventRecordDTO result = null; 
        List<SwissTimingRaceRecordDTO> swissTimingRaces = new ArrayList<SwissTimingRaceRecordDTO>();
        
        // TODO: delete getSwissTimingAdapter().getSwissTimingRaceRecords() method
        // TODO: delete SwissTimingDomainFactory.getRaceTypeFromRaceID(String raceID)
        URL url = new URL(eventJsonURL);
        URLConnection eventResultConn = HttpUrlConnectionHelper.redirectConnection(url);
        Manage2SailEventResultsParserImpl parser = new Manage2SailEventResultsParserImpl();
        EventResultDescriptor eventResult = parser.getEventResult((InputStream) eventResultConn.getContent());
        if (eventResult != null) {
            for (RegattaResultDescriptor regattaResult : eventResult.getRegattaResults()) {
                for (RaceResultDescriptor race : regattaResult.getRaceResults()) {
                    // add only the  tracked races
                    if (race.isTracked() != null && race.isTracked() == true) {
                        SwissTimingRaceRecordDTO swissTimingRaceRecordDTO = new SwissTimingRaceRecordDTO(race.getId(), race.getName(), 
                                regattaResult.getName(), race.getSeriesName(), race.getFleetName(), race.getStatus(), race.getStartTime(),
                                regattaResult.getXrrEntriesUrl() != null ? regattaResult.getXrrEntriesUrl().toExternalForm() : null, hasRememberedRegatta(race.getId()));
                        swissTimingRaceRecordDTO.boatClass = regattaResult.getIsafId() != null && !regattaResult.getIsafId().isEmpty() ? regattaResult.getIsafId() : regattaResult.getClassName();
                        swissTimingRaceRecordDTO.gender = regattaResult.getCompetitorGenderType().name();
                        swissTimingRaces.add(swissTimingRaceRecordDTO);
                    }
                }
            }
            result = new SwissTimingEventRecordDTO(eventResult.getId(), eventResult.getName(), eventResult.getTrackingDataHost(),
                    eventResult.getTrackingDataPort(), swissTimingRaces);
        }
        return result;
    }

    @Override
    public void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, Integer port,
            String updateURL, String updateUsername, String updatePassword) throws Exception {
        if (!jsonURL.equalsIgnoreCase("test")) {
            getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.SWISS_TIMING_ACCOUNT, configName,
                    configName,
                    () -> swissTimingAdapterPersistence
                            .storeSwissTimingConfiguration(swissTimingFactory.createSwissTimingConfiguration(configName,
                                    jsonURL, hostname, port, updateURL, updateUsername, updatePassword)));
        }
    }
    
    private RaceLogStore getRaceLogStore() {
        return MongoRaceLogStoreFactory.INSTANCE.getMongoRaceLogStore(mongoObjectFactory,
                domainObjectFactory);
    }
    
    private RegattaLogStore getRegattaLogStore() {
        return MongoRegattaLogStoreFactory.INSTANCE.getMongoRegattaLogStore(
                mongoObjectFactory, domainObjectFactory);
    }

    @Override
    public void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs, String hostname, int port,
            boolean trackWind, final boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm,
            String updateURL, String updateUsername, String updatePassword) throws InterruptedException, ParseException, Exception {
        logger.info("tracWithSwissTiming for regatta " + regattaToAddTo + " for race records " + rrs
                + " with hostname " + hostname + " and port " + port);
        Map<String, RegattaResults> cachedRegattaEntriesLists = new HashMap<String, RegattaResults>();
        for (SwissTimingRaceRecordDTO rr : rrs) {
            BoatClass boatClass = getBaseDomainFactory().getOrCreateBoatClass(rr.boatClass);
            String raceDescription = rr.regattaName != null ? rr.regattaName : ""; 
            raceDescription += rr.seriesName != null ? "/" + rr.seriesName : "";
            raceDescription += raceDescription.length() > 0 ?  "/" + rr.getName() : rr.getName();
            // try to find a cached entry list for the regatta
            RegattaResults regattaResults = cachedRegattaEntriesLists.get(rr.xrrEntriesUrl);
            if (regattaResults == null && rr.xrrEntriesUrl != null) {
            	regattaResults = getSwissTimingAdapter().readRegattaEntryListFromXrrUrl(rr.xrrEntriesUrl);
                if (regattaResults != null) {
                    cachedRegattaEntriesLists.put(rr.xrrEntriesUrl, regattaResults);
                }
            }
            StartList startList = null;
            if (regattaResults != null) {
            	startList = getSwissTimingAdapter().readStartListForRace(rr.raceId, regattaResults);
            }
            // now read the entry list for the race from the result
            getSwissTimingAdapter().addSwissTimingRace(getService(), regattaToAddTo,
                    rr.raceId, rr.getName(), raceDescription, boatClass, hostname, port, startList,
                    getRaceLogStore(), getRegattaLogStore(),
                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS, useInternalMarkPassingAlgorithm, trackWind,
                    correctWindByDeclination, updateURL, updateUsername, updatePassword);
        }
    }
    
    protected SwissTimingReplayService getSwissTimingReplayService() {
        return swissTimingReplayService;
    }

    @Override
    public List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl) {
        List<SwissTimingReplayRace> replayRaces = getSwissTimingReplayService().listReplayRaces(swissTimingUrl);
        List<SwissTimingReplayRaceDTO> result = new ArrayList<SwissTimingReplayRaceDTO>(replayRaces.size()); 
        for (SwissTimingReplayRace replayRace : replayRaces) {
            result.add(new SwissTimingReplayRaceDTO(replayRace.getFlightNumber(), replayRace.getRaceId(),
                    replayRace.getRsc(), replayRace.getName(), replayRace.getBoatClass(), replayRace.getStartTime(),
                    replayRace.getLink(), hasRememberedRegatta(replayRace.getRaceId())));
        }
        return result;
    }

    @Override
    public void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaceDTOs,
            boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm) {
        logger.info("replaySwissTimingRace for regatta "+regattaIdentifier+" for races "+replayRaceDTOs);
        for (SwissTimingReplayRaceDTO replayRaceDTO : replayRaceDTOs) {
            try {
                String boatClassName;
                if (regattaIdentifier == null) {
                    boatClassName = replayRaceDTO.boat_class;
                    for (String genderIndicator : new String[] { "Man", "Woman", "Men", "Women", "M", "W" }) {
                        Pattern p = Pattern.compile("(( - )|-| )" + genderIndicator + "$");
                        Matcher m = p.matcher(boatClassName.trim());
                        if (m.find()) {
                            boatClassName = boatClassName.trim().substring(0, m.start(1));
                            break;
                        }
                    }
                } else {
                    boatClassName = null;
                }
                getSwissTimingReplayService().loadRaceData(regattaIdentifier, replayRaceDTO.link, replayRaceDTO.getName(),
                        replayRaceDTO.race_id, boatClassName, getService(), getService(), useInternalMarkPassingAlgorithm, getRaceLogStore(), getRegattaLogStore());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error trying to load SwissTimingReplay race " + replayRaceDTO, e);
            }
        }
    }

    @Override
    public String[] getCountryCodes() {
        List<String> countryCodes = new ArrayList<String>();
        for (CountryCode cc : countryCodeFactory.getAll()) {
            if (cc.getThreeLetterIOCCode() != null && !cc.getThreeLetterIOCCode().equals("")) {
                countryCodes.add(cc.getThreeLetterIOCCode());
            }
        }
        Collections.sort(countryCodes);
        return countryCodes.toArray(new String[0]);
    }

    /**
     * Finds a competitor in a sequence of competitors that has an {@link Competitor#getId()} equal to <code>id</code>. 
     */
    private Competitor getCompetitorByIdAsString(Iterable<Competitor> competitors, String idAsString) {
        for (Competitor c : competitors) {
            if (c.getId().toString().equals(idAsString)) {
                return c;
            }
        }
        return null;
    }

    private Double getCompetitorRaceDataEntry(DetailType dataType, TrackedRace trackedRace, Competitor competitor,
            TimePoint timePoint, String leaderboardGroupName, String leaderboardName, WindLegTypeAndLegBearingCache cache) throws NoWindException {
        Double result = null;
        Course course = trackedRace.getRace().getCourse();
        course.lockForRead(); // make sure the tracked leg survives this call even if a course update is pending
        try {
            TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, timePoint);
            switch (dataType) {
            case RACE_CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                final GPSFixTrack<Competitor, GPSFixMoving> sogTrack = trackedRace.getTrack(competitor);
                if (sogTrack != null) {
                    SpeedWithBearing speedOverGround = sogTrack.getEstimatedSpeed(timePoint);
                    result = (speedOverGround == null) ? null : speedOverGround.getKnots();
                }
                break;
            case CHART_COURSE_OVER_GROUND_TRUE_DEGREES:
                final GPSFixTrack<Competitor, GPSFixMoving> cogTrack = trackedRace.getTrack(competitor);
                if (cogTrack != null) {
                    SpeedWithBearing speedOverGround = cogTrack.getEstimatedSpeed(timePoint);
                    result = (speedOverGround == null) ? null : speedOverGround.getBearing().getDegrees();
                }
                break;
            case LEG_VELOCITY_MADE_GOOD_IN_KNOTS:
                final Speed velocityMadeGood;
                if (trackedLeg != null) {
                    velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint, WindPositionMode.EXACT, cache);
                } else {
                    // check if wind information is available; if so, compute a VMG only based on wind data:
                    velocityMadeGood = trackedRace.getVelocityMadeGood(competitor, timePoint, cache);
                }
                result = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                break;
            case LEG_DISTANCE_TRAVELED:
                if (trackedLeg != null) {
                    Distance distanceTraveled = trackedRace.getDistanceTraveled(competitor, timePoint);
                    result = distanceTraveled == null ? null : distanceTraveled.getMeters();
                }
                break;
            case LEG_DISTANCE_TRAVELED_INCLUDING_GATE_START:
                if (trackedLeg != null) {
                    Distance distanceTraveledConsideringGateStart = trackedRace.getDistanceTraveledIncludingGateStart(competitor, timePoint);
                    result = distanceTraveledConsideringGateStart == null ? null : distanceTraveledConsideringGateStart.getMeters();
                }
                break;
            case LEG_GAP_TO_LEADER_IN_SECONDS:
                if (trackedLeg != null) {
                    final RankingInfo rankingInfo = trackedRace.getRankingMetric().getRankingInfo(timePoint);
                    final Duration gapToLeaderInOwnTime = trackedLeg.getTrackedLeg().getTrackedRace().getRankingMetric().getGapToLeaderInOwnTime(rankingInfo, competitor, cache);
                    result = gapToLeaderInOwnTime == null ? null : gapToLeaderInOwnTime.asSeconds();
                }
                break;
            case CHART_WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD:
                if (trackedLeg != null) {
                    final RankingInfo rankingInfo = trackedRace.getRankingMetric().getRankingInfo(timePoint);
                    Distance distanceToLeader = trackedLeg.getWindwardDistanceToCompetitorFarthestAhead(timePoint, WindPositionMode.LEG_MIDDLE, rankingInfo);
                    result = (distanceToLeader == null) ? null : distanceToLeader.getMeters();
                }
                break;
            case RACE_RANK:
                if (trackedLeg != null) {
                    result = (double) trackedLeg.getRank(timePoint, cache);
                }
                break;
            case REGATTA_RANK:
                if (leaderboardName == null || leaderboardName.isEmpty()) {
                    break;
                }
                Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
                result = leaderboard == null ? null : (double) leaderboard.getTotalRankOfCompetitor(competitor,
                        timePoint);
                break;
            case OVERALL_RANK:
                if (leaderboardGroupName == null || leaderboardGroupName.isEmpty()) {
                    break;
                }
                LeaderboardGroup group = getService().getLeaderboardGroupByName(leaderboardGroupName);
                Leaderboard overall = group.getOverallLeaderboard();
                result = overall == null ? null : (double) overall.getTotalRankOfCompetitor(competitor, timePoint);
                break;
            case CHART_DISTANCE_TO_START_LINE:
                TimePoint startOfRace = trackedRace.getStartOfRace();
                if (startOfRace == null || timePoint.before(startOfRace) || timePoint.equals(startOfRace)) {
                    Distance distanceToStartLine = trackedRace.getDistanceToStartLine(competitor, timePoint);
                    result = distanceToStartLine == null ? null : distanceToStartLine.getMeters();
                }
                break;
            case CHART_BEAT_ANGLE:
                Bearing twa = trackedRace.getTWA(competitor, timePoint, cache);
                result = twa == null? null:twa.getDegrees();
                break;
            case BRAVO_RACE_HEEL_IN_DEGREES:
            case BRAVO_LEG_CURRENT_HEEL_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getHeel, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVO_RACE_PITCH_IN_DEGREES:
            case BRAVO_LEG_CURRENT_PITCH_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getPitch, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVO_RACE_CURRENT_RIDE_HEIGHT_IN_METERS: {
                result = getBravoDistanceInMeters(BravoFixTrack::getRideHeight, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_PORT_DAGGERBOARD_RAKE: {
                result = getBravoDoubleValue(BravoFixTrack::getPortDaggerboardRakeIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_STBD_DAGGERBOARD_RAKE: {
                result = getBravoDoubleValue(BravoFixTrack::getStbdDaggerboardRakeStbdIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_PORT_RUDDER_RAKE: {
                result = getBravoDoubleValue(BravoFixTrack::getPortRudderRakeIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_STBD_RUDDER_RAKE: {
                result = getBravoDoubleValue(BravoFixTrack::getStbdRudderRakeIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_MAST_ROTATION_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getMastRotationIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_LEEWAY_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getLeewayIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_SET: {
                result = getBravoDoubleValue(BravoFixTrack::getSetIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_DRIFT_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getDriftIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_DEPTH_IN_METERS: {
                result = getBravoDistanceInMeters(BravoFixTrack::getDepthIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_RUDDER_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getRudderIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_TACK_ANGLE_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getTackAngleIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_PERCENTAGE: {
                result = getBravoDoubleValue(BravoFixTrack::getDeflectorPercentageIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_DEFLECTOR_IN_MILLIMETERS: {
                Double deflectorInMeters = getBravoDistanceInMeters(BravoFixTrack::getDeflectorIfAvailable, trackedRace, competitor, timePoint);
                result = deflectorInMeters == null ? null : (deflectorInMeters * 1000.);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_RAKE_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getRakeIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_TARGET_HEEL_ANGLE_IN_DEGREES: {
                result = getBravoBearingInDegrees(BravoFixTrack::getTargetHeelIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_FORESTAY_LOAD: {
                result = getBravoDoubleValue(BravoFixTrack::getForestayLoadIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_FORESTAY_PRESSURE: {
                result = getBravoDoubleValue(BravoFixTrack::getForestayPressureIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case BRAVOEXTENDED_RACE_CURRENT_TARGET_BOATSPEED_PERCENTAGE: {
                result = getBravoDoubleValue(BravoFixTrack::getTargetBoatspeedPIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            
            case EXPEDITION_RACE_AWA: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionAWAIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_AWS: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionAWSIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_BARO: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionBaroIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_BOAT_SPEED: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionBoatSpeedIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_COG: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionCOGIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_COURSE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionCourseDetailIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_DIST_TO_PORT_LAYLINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionDistToPortLaylineIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_DIST_TO_STB_LAYLINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionDistToStbLaylineIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_DISTANCE_BELOW_LINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionDistanceBelowLineInMetersIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_DISTANCE_TO_COMMITTEE_BOAT: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionDistanceToCommitteeBoatIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_DISTANCE_TO_PIN: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionDistanceToPinDetailIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_FORESTAY_LOAD: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionForestayLoadIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_HEADING: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionHeadingIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_JIB_CAR_PORT: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionJibCarPortIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_JIB_CAR_STBD: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionJibCarStbdIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_LINE_SQUARE_FOR_WIND_DIRECTION: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionLineSquareForWindIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_LOAD_P: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionLoadPIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_LOAD_S: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionLoadSIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_MAST_BUTT: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionMastButtIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_RAKE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionRakeIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_RATE_OF_TURN: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionRateOfTurnIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_RUDDER_ANGLE: {
                result = getBravoBearingInDegrees(BravoFixTrack::getRudderIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_SOG: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionSOGIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TARG_BOAT_SPEED: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTargBoatSpeedIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TARG_TWA: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTargTWAIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TARGET_HEEL: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTargetHeelIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_BURN_TO_COMMITTEE_BOAT: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToBurnToCommitteeBoatIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_BURN_TO_LINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToBurnToLineInSecondsIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_BURN_TO_PIN: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToBurnToPinIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_COMMITTEE_BOAT: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToCommitteeBoatIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_GUN: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToGunInSecondsIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_PIN: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToPinIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_PORT_LAYLINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToPortLaylineIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TIME_TO_STB_LAYLINE: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTimeToStbLaylineIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TWA: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTWAIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TWD: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTWDIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_TWS: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionTWSIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_VMG: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionVMGIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            case EXPEDITION_RACE_VMG_TARG_VMG_DELTA: {
                result = getBravoDoubleValue(BravoFixTrack::getExpeditionVMGTargVMGDeltaIfAvailable, trackedRace, competitor, timePoint);
                break;
            }
            default:
                throw new UnsupportedOperationException("There is currently no support for the enum value '" + dataType
                        + "' in this method.");
            }
            return result;
        } finally {
            course.unlockAfterRead();
        }
    }

    private Double getBravoDoubleValue(BiFunction<BravoFixTrack<Competitor>, TimePoint, Double> valueGetter,
            TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        return getBravoValue(valueGetter, Function.identity(), trackedRace, competitor, timePoint);
    }
    
    private Double getBravoBearingInDegrees(BiFunction<BravoFixTrack<Competitor>, TimePoint, Bearing> valueGetter,
            TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        return getBravoValue(valueGetter, Bearing::getDegrees, trackedRace, competitor, timePoint);
    }
    
    private Double getBravoDistanceInMeters(BiFunction<BravoFixTrack<Competitor>, TimePoint, Distance> valueGetter,
            TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        return getBravoValue(valueGetter, Distance::getMeters, trackedRace, competitor, timePoint);
    }
    
    private <T> Double getBravoValue(BiFunction<BravoFixTrack<Competitor>, TimePoint, T> valueGetter,
            Function<T, Double> mapperToDouble,
            TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        final Double result;
        final BravoFixTrack<Competitor> bravoFixTrack = trackedRace
                .<BravoFix, BravoFixTrack<Competitor>> getSensorTrack(competitor, BravoFixTrack.TRACK_NAME);
        if (bravoFixTrack != null) {
            final T t = valueGetter.apply(bravoFixTrack, timePoint);
            result = t == null ? null : mapperToDouble.apply(t);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            final long stepSizeInMillis, final DetailType detailType, final String leaderboardGroupName, final String leaderboardName) throws NoWindException {
        CompetitorsRaceDataDTO result = null;
        final TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            final TimePoint startTime = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            final TimePoint endTime = (to == null || to.after(newestEvent.asDate())) ? newestEvent : new MillisecondsTimePoint(to);
            final long adjustedStepSizeInMillis = (long) Math.max((double) stepSizeInMillis, startTime.until(endTime).divide(SailingServiceConstants.MAX_NUMBER_OF_FIXES_TO_QUERY).asMillis());
            result = new CompetitorsRaceDataDTO(detailType, startTime==null?null:startTime.asDate(), endTime==null?null:endTime.asDate());
            final int MAX_CACHE_SIZE = SailingServiceConstants.MAX_NUMBER_OF_FIXES_TO_QUERY;
            final ConcurrentHashMap<TimePoint, WindLegTypeAndLegBearingCache> cachesByTimePoint = new ConcurrentHashMap<>();
            Map<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>> resultFutures = new HashMap<>();
            for (final CompetitorDTO competitorDTO : competitors) {
                FutureTask<CompetitorRaceDataDTO> future = new FutureTask<CompetitorRaceDataDTO>(new Callable<CompetitorRaceDataDTO>() {
                            @Override
                            public CompetitorRaceDataDTO call() throws NoWindException {
                                Competitor competitor = getCompetitorByIdAsString(trackedRace.getRace().getCompetitors(),
                                        competitorDTO.getIdAsString());
                                ArrayList<com.sap.sse.common.Util.Triple<String, Date, Double>> markPassingsData = new ArrayList<com.sap.sse.common.Util.Triple<String, Date, Double>>();
                                ArrayList<com.sap.sse.common.Util.Pair<Date, Double>> raceData = new ArrayList<com.sap.sse.common.Util.Pair<Date, Double>>();
                                // Filling the mark passings
                                Set<MarkPassing> competitorMarkPassings = trackedRace.getMarkPassings(competitor);
                                if (competitorMarkPassings != null) {
                                    trackedRace.lockForRead(competitorMarkPassings);
                                    try {
                                        for (MarkPassing markPassing : competitorMarkPassings) {
                                            MillisecondsTimePoint time = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis());
                                            WindLegTypeAndLegBearingCache cache = cachesByTimePoint.get(time);
                                            if (cache == null) {
                                                cache = new LeaderboardDTOCalculationReuseCache(time);
                                                cachesByTimePoint.put(time, cache);
                                            }
                                            Double competitorMarkPassingsData = getCompetitorRaceDataEntry(detailType,
                                                    trackedRace, competitor, time, leaderboardGroupName, leaderboardName, cache);
                                            if (competitorMarkPassingsData != null) {
                                                markPassingsData.add(new com.sap.sse.common.Util.Triple<String, Date, Double>(markPassing
                                                        .getWaypoint().getName(), time.asDate(), competitorMarkPassingsData));
                                            }
                                        }
                                    } finally {
                                        trackedRace.unlockAfterRead(competitorMarkPassings);
                                    }
                                }
                                if (startTime != null && endTime != null) {
                                    for (long i = startTime.asMillis(); i <= endTime.asMillis(); i += adjustedStepSizeInMillis) {
                                        MillisecondsTimePoint time = new MillisecondsTimePoint(i);
                                        WindLegTypeAndLegBearingCache cache = cachesByTimePoint.get(time);
                                        if (cache == null) {
                                            cache = new LeaderboardDTOCalculationReuseCache(time);
                                            if (cachesByTimePoint.size() >= MAX_CACHE_SIZE) {
                                                final Iterator<Entry<TimePoint, WindLegTypeAndLegBearingCache>> iterator = cachesByTimePoint.entrySet().iterator();
                                                while (cachesByTimePoint.size() >= MAX_CACHE_SIZE && iterator.hasNext()) {
                                                    iterator.next();
                                                    iterator.remove();
                                                }
                                            }
                                            cachesByTimePoint.put(time, cache);
                                        }
                                        Double competitorRaceData = getCompetitorRaceDataEntry(detailType, trackedRace,
                                                competitor, time, leaderboardGroupName, leaderboardName, cache);
                                        if (competitorRaceData != null) {
                                            raceData.add(new com.sap.sse.common.Util.Pair<Date, Double>(time.asDate(), competitorRaceData));
                                        }
                                    }
                                }
                                return new CompetitorRaceDataDTO(competitorDTO, detailType, markPassingsData, raceData);
                            }
                        });
                resultFutures.put(competitorDTO, future);
                executor.execute(future);
            }
            for (Map.Entry<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>> e : resultFutures.entrySet()) {
                CompetitorRaceDataDTO competitorData;
                try {
                    competitorData = e.getValue().get();
                } catch (InterruptedException e1) {
                    competitorData = null;
                    logger.log(Level.SEVERE, "Exception while trying to compute competitor data "+detailType+" for competitor "+e.getKey().getName(), e1);
                } catch (ExecutionException e1) {
                    competitorData = null;
                    logger.log(Level.SEVERE, "Exception while trying to compute competitor data "+detailType+" for competitor "+e.getKey().getName(), e1);
                }
                result.setCompetitorData(e.getKey(), competitorData);
            }
        }
        return result;
    }

    @Override
    public List<Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, 
            Date date, DetailType detailType) throws Exception {
        List<com.sap.sse.common.Util.Triple<String, List<CompetitorDTO>, List<Double>>> result = new ArrayList<>();
        // Attention: The reason why we read the data from the LeaderboardDTO and not from the leaderboard directly is to ensure
        // the use of the leaderboard cache.
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            TimePoint timePoint;
            if (date == null) {
                timePoint = null;
            } else {
                timePoint = new MillisecondsTimePoint(date);
            }
            TimePoint effectiveTimePoint = timePoint == null ? leaderboard.getNowMinusDelay() : timePoint;
            if (detailType != null) {
                switch (detailType) {
                case REGATTA_NET_POINTS_SUM:
                    for (Entry<RaceColumn, Map<Competitor, Double>> e : leaderboard.getNetPointsSumAfterRaceColumn(effectiveTimePoint).entrySet()) {
                        List<CompetitorDTO> competitorDTOs = new ArrayList<>();
                        List<Double> pointSums = new ArrayList<>();
                        for (Entry<Competitor, Double> e2 : e.getValue().entrySet()) {
                            competitorDTOs.add(baseDomainFactory.convertToCompetitorDTO(e2.getKey()));
                            pointSums.add(e2.getValue());
                        }
                        result.add(new Triple<>(e.getKey().getName(), competitorDTOs, pointSums)); 
                    }
                    break;
                case REGATTA_RANK:
                case OVERALL_RANK:
                    Map<RaceColumn, List<Competitor>> competitorsFromBestToWorst = leaderboard
                            .getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(effectiveTimePoint);
                    for (Entry<RaceColumn, List<Competitor>> e : competitorsFromBestToWorst.entrySet()) {
                        int rank = 1;
                        List<Double> values = new ArrayList<Double>();
                        List<CompetitorDTO> competitorDTOs = new ArrayList<>();
                        for (Competitor competitor : e.getValue()) {
                            values.add(new Double(rank));
                            competitorDTOs.add(baseDomainFactory.convertToCompetitorDTO(competitor));
                            rank++;
                        }
                        result.add(new Triple<>(e.getKey().getName(), competitorDTOs, values));
                    }
                    break;
                default:
                    break;
                }
            }

        }
        return result;
    }

    @Override
    public List<com.sap.sse.common.Util.Pair<String, String>> getLeaderboardsNamesOfMetaLeaderboard(
            String metaLeaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(metaLeaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Couldn't find leaderboard named " + metaLeaderboardName);
        }
        getSecurityService().checkCurrentUserReadPermission(leaderboard);
        if (leaderboard instanceof RegattaLeaderboard) {
            getSecurityService().checkCurrentUserReadPermission(((RegattaLeaderboard) leaderboard).getRegatta());
        }
        if (!(leaderboard instanceof MetaLeaderboard)) {
            throw new IllegalArgumentException("The leaderboard " + metaLeaderboardName + " is not a metaleaderboard");
        }
        MetaLeaderboard metaLeaderboard = (MetaLeaderboard) leaderboard;
        LeaderboardGroup groupOrNull = null;
        for (LeaderboardGroup lg : getService().getLeaderboardGroups().values()) {
            if (metaLeaderboard.equals(lg.getOverallLeaderboard())) {
                if (getSecurityService().hasCurrentUserReadPermission(lg)) {
                    groupOrNull = lg;
                    break;
                }
            }
        }
        // If we could identify the associated LeaderboardGroup the Leaderboards can be sorted based on that group
        Iterable<Leaderboard> leaderBoards = groupOrNull != null
                ? HomeServiceUtil.getLeaderboardsForSeriesInOrderWithReadPermissions(groupOrNull, getService())
                : metaLeaderboard.getLeaderboards();
        List<com.sap.sse.common.Util.Pair<String, String>> result = new ArrayList<com.sap.sse.common.Util.Pair<String, String>>();
        for (Leaderboard containedLeaderboard : leaderBoards) {
            // we need to filter because metaLeaderboard.getLeaderboards might return non visible ones
            if (getSecurityService().hasCurrentUserReadPermission(containedLeaderboard)) {
                if (containedLeaderboard instanceof RegattaLeaderboard) {
                    Regatta regatta = ((RegattaLeaderboard) containedLeaderboard).getRegatta();
                    if (getSecurityService().hasCurrentUserReadPermission(regatta)) {
                        result.add(new com.sap.sse.common.Util.Pair<String, String>(containedLeaderboard.getName(),
                                containedLeaderboard.getDisplayName() != null ? containedLeaderboard.getDisplayName()
                                        : containedLeaderboard.getName()));
                    }
                } else {
                    result.add(new com.sap.sse.common.Util.Pair<String, String>(containedLeaderboard.getName(),
                            containedLeaderboard.getDisplayName() != null ? containedLeaderboard.getDisplayName()
                                    : containedLeaderboard.getName()));
                }
            }
        }
        return result;
    }

    @Override
    public Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> getDouglasPoints(
            RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, TimeRange> competitorTimeRanges, double meters)
            throws NoWindException {
        final Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> result = new HashMap<>();
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            final MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                final CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (competitorTimeRanges.containsKey(competitorDTO)) {
                    // get Track of competitor
                    final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(competitor);
                    // Distance for DouglasPeucker
                    final TimeRange timeRange = competitorTimeRanges.get(competitorDTO);
                    final Iterable<GPSFixMoving> gpsFixApproximation = trackedRace.approximate(competitor, maxDistance,
                            timeRange.from(), timeRange.to());
                    final List<GPSFixDTOWithSpeedWindTackAndLegType> gpsFixDouglasList = new ArrayList<>();
                    GPSFix fix = null;
                    for (GPSFix next : gpsFixApproximation) {
                        if (fix != null) {
                            final Bearing bearing = fix.getPosition().getBearingGreatCircle(next.getPosition());
                            final Speed speed = fix.getPosition().getDistance(next.getPosition())
                                    .inTime(next.getTimePoint().asMillis() - fix.getTimePoint().asMillis());
                            final SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), bearing);
                            gpsFixDouglasList.add(createDouglasPeuckerGPSFixDTO(trackedRace, competitor, fix, speedWithBearing));
                        }
                        fix = next;
                    }
                    if (fix != null) {
                        // add one last GPSFixDTO with no successor to calculate speed/bearing to:
                        final SpeedWithBearing speedWithBearing = gpsFixTrack.getEstimatedSpeed(fix.getTimePoint());
                        gpsFixDouglasList.add(createDouglasPeuckerGPSFixDTO(trackedRace, competitor, fix, speedWithBearing));
                    }
                    result.put(competitorDTO, gpsFixDouglasList);
                }
            }
        }
        return result;
    }

    private GPSFixDTOWithSpeedWindTackAndLegType createDouglasPeuckerGPSFixDTO(TrackedRace trackedRace, Competitor competitor, GPSFix fix,
            SpeedWithBearing speedWithBearing) throws NoWindException {
        Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                fix.getTimePoint());
        LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
        Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
        WindDTO windDTO = createWindDTOFromAlreadyAveraged(wind, fix.getTimePoint());
        GPSFixDTOWithSpeedWindTackAndLegType fixDTO = createGPSFixDTO(fix, speedWithBearing, windDTO, tack, legType, /* extrapolated */
                false);
        return fixDTO;
    }

    @Override
    public Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, TimeRange> competitorTimeRanges) throws NoWindException {
        final Map<CompetitorDTO, List<ManeuverDTO>> result = new HashMap<>();
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            final Map<CompetitorDTO, Future<List<ManeuverDTO>>> futures = new HashMap<>();
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                final CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (competitorTimeRanges.containsKey(competitorDTO)) {
                    final TimeRange timeRange = competitorTimeRanges.get(competitorDTO);
                    final TimePoint from = timeRange.from(), to = timeRange.to();
                    final RunnableFuture<List<ManeuverDTO>> future = new FutureTask<>(() -> {
                        final Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, from, to,
                                /* waitForLatest */ true);
                        return createManeuverDTOsForCompetitor(maneuvers, trackedRace, competitor);
                    });
                    executor.execute(future);
                    futures.put(competitorDTO, future);
                }
            }
            for (Map.Entry<CompetitorDTO, Future<List<ManeuverDTO>>> competitorAndFuture : futures.entrySet()) {
                try {
                    result.put(competitorAndFuture.getKey(), competitorAndFuture.getValue().get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    private List<ManeuverDTO> createManeuverDTOsForCompetitor(Iterable<Maneuver> maneuvers, TrackedRace trackedRace, Competitor competitor) {
        final List<ManeuverDTO> result = new ArrayList<ManeuverDTO>();
        for (Maneuver maneuver : maneuvers) {
            final ManeuverType type = maneuver.getType();
            final Tack newTack = maneuver.getNewTack();
            final Position position = maneuver.getPosition();
            final Date timepoint = maneuver.getTimePoint().asDate();
            final Date timePointBefore = maneuver.getManeuverBoundaries().getTimePointBefore().asDate();
            final SpeedWithBearingDTO speedBefore = createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore());
            final SpeedWithBearingDTO speedAfter = createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter());
            final double directionChangeInDegrees = maneuver.getDirectionChangeInDegrees();
            final double maxTurningRateInDegreesPerSecond = maneuver.getMaxTurningRateInDegreesPerSecond();
            final double averageTurningRateInDegreesPerSecond = maneuver.getAvgTurningRateInDegreesPerSecond();
            final double lowestSpeedInKnots = maneuver.getLowestSpeed().getKnots();
            final Date markPassingTimePoint = maneuver.isMarkPassing()
                    ? maneuver.getMarkPassing().getTimePoint().asDate() : null;
            final NauticalSide markPassingSide = maneuver.isMarkPassing() ? maneuver.getToSide() : null;
            final SpeedWithBearing speedWithBearingBeforeManeuverLoss = maneuver.getManeuverLoss() == null ? null
                    : maneuver.getManeuverLoss().getSpeedWithBearingBefore();
            final Double middleManeuverAngle = maneuver.getManeuverLoss() == null ? null : maneuver.getManeuverLoss().getMiddleManeuverAngle().getDegrees();
            final ManeuverLossDTO maneuverLoss = maneuver.getManeuverLoss() == null ? null
                    : new ManeuverLossDTO(maneuver.getManeuverLoss().getManeuverStartPosition(),
                            maneuver.getManeuverLoss().getManeuverEndPosition(), speedWithBearingBeforeManeuverLoss, middleManeuverAngle,
                            maneuver.getManeuverLoss().getManeuverDuration(), maneuver.getManeuverLoss().getProjectedDistanceLost());
            result.add(new ManeuverDTO(type, newTack, position, timepoint, timePointBefore, speedBefore,
                    speedAfter, directionChangeInDegrees, maxTurningRateInDegreesPerSecond,
                    averageTurningRateInDegreesPerSecond, lowestSpeedInKnots, markPassingTimePoint, markPassingSide, maneuverLoss));
        }
        return result;
    }

    @Override
    public RaceDefinition getRace(RegattaAndRaceIdentifier raceIdentifier) {
        Regatta regatta = getService().getRegattaByName(raceIdentifier.getRegattaName());
        RaceDefinition race = getRaceByName(regatta, raceIdentifier.getRaceName());
        return race;
    }

    @Override
    public DynamicTrackedRace getTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        Regatta regatta = getService().getRegattaByName(regattaNameAndRaceName.getRegattaName());
        RaceDefinition race = getRaceByName(regatta, regattaNameAndRaceName.getRaceName());
        DynamicTrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
        getSecurityService().checkCurrentUserReadPermission(trackedRace);
        return trackedRace;
    }

    @Override
    public TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        final DynamicTrackedRace existingTrackedRace = getService().getExistingTrackedRace(regattaNameAndRaceName);
        getSecurityService().checkCurrentUserReadPermission(existingTrackedRace);
        return existingTrackedRace;
    }

    @Override
    public Regatta getRegatta(RegattaName regattaIdentifier) {
        final Regatta regattaByName = getService().getRegattaByName(regattaIdentifier.getRegattaName());
        getSecurityService().checkCurrentUserReadPermission(regattaByName);
        return regattaByName;
    }

    /**
     * Returns a servlet context that, when asked for a resource, first tries the original servlet context's implementation. If that
     * fails, it prepends "war/" to the request because the war/ folder contains all the resources exposed externally
     * through the HTTP server.
     */
    @Override
    public ServletContext getServletContext() {
        return new DelegatingServletContext(super.getServletContext());
    }

    @Override
    /**
     * Override of function to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
     */
    protected void checkPermutationStrongName() throws SecurityException {
        //Override to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
        return;
    }

    @Override
    public List<LeaderboardGroupDTO> getLeaderboardGroups(boolean withGeoLocationData) {
        final Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.LEADERBOARD_GROUP,
                leaderboardGroups.values(), lg -> lg.getId().toString(),
                leaderboardGroup -> convertToLeaderboardGroupDTO(leaderboardGroup, withGeoLocationData, false));
    }

    @Override
    public LeaderboardGroupDTO getLeaderboardGroupByName(String groupName, boolean withGeoLocationData) {
        final LeaderboardGroup leaderboardGroupByName = getService().getLeaderboardGroupByName(groupName);
        getSecurityService().checkCurrentUserReadPermission(leaderboardGroupByName);
        return convertToLeaderboardGroupDTO(leaderboardGroupByName, withGeoLocationData, false);
    }

    private LeaderboardGroupDTO convertToLeaderboardGroupDTO(final LeaderboardGroup leaderboardGroup,
            final boolean withGeoLocationData, final boolean withStatisticalData) {
        final LeaderboardGroupDTO groupDTO = new LeaderboardGroupDTO(leaderboardGroup.getId(),
                leaderboardGroup.getName(), leaderboardGroup.getDisplayName(), leaderboardGroup.getDescription());
        groupDTO.displayLeaderboardsInReverseOrder = leaderboardGroup.isDisplayGroupsInReverseOrder();
        for (final Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            try {
                groupDTO.leaderboards.add(createStrippedLeaderboardDTO(leaderboard, withGeoLocationData, withStatisticalData));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Caught exception while reading data for leaderboard " + leaderboard.getName(), e);
            }
        }
        final Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            if (overallLeaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) {
                groupDTO.setOverallLeaderboardDiscardThresholds(((ThresholdBasedResultDiscardingRule) overallLeaderboard
                        .getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces());
            }
            groupDTO.setOverallLeaderboardScoringSchemeType(overallLeaderboard.getScoringScheme().getType());
        }
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), groupDTO, leaderboardGroup.getIdentifier());
        return groupDTO;
    }


    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        final LeaderboardGroup leaderboardGroupByName = getService().getLeaderboardGroupByName(oldName);
        getSecurityService().checkCurrentUserUpdatePermission(leaderboardGroupByName);
        getService().apply(new RenameLeaderboardGroup(oldName, newName));
    }

    @Override
    public void removeLeaderboardGroups(Set<String> groupNames) {
        for (String groupName : groupNames) {
            removeLeaderboardGroup(groupName);
        }
    }

    private void removeLeaderboardGroup(String groupName) {
        LeaderboardGroupDTO group = getLeaderboardGroupByName(groupName, false);
        if (group != null) {
            getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredDomainType.LEADERBOARD_GROUP,
                    group.getId().toString(), new Action() {
                        @Override
                        public void run() throws Exception {
                            getService().apply(new RemoveLeaderboardGroup(groupName));
                        }
                    });
        }
    }

    @Override
    public LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description,
            String displayName,
            boolean displayGroupsInReverseOrder,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        List<String> leaderBoards = new ArrayList<>();

        return doCreateLeaderboardGroup(groupName, description, displayName,
                displayGroupsInReverseOrder, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType,
                leaderBoards);
    }

    private LeaderboardGroupDTO doCreateLeaderboardGroup(String groupName, String description,
            String displayName, boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType, List<String> leaderBoards) {
        UUID newLeaderboardGroupId = UUID.randomUUID();
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.LEADERBOARD_GROUP, newLeaderboardGroupId.toString(), displayName,
                new ActionWithResult<LeaderboardGroupDTO>() {
                    @Override
                    public LeaderboardGroupDTO run() throws Exception {
                        CreateLeaderboardGroup createLeaderboardGroupOp = new CreateLeaderboardGroup(
                                newLeaderboardGroupId, groupName, description, displayName, displayGroupsInReverseOrder,
                                leaderBoards, overallLeaderboardDiscardThresholds,
                                overallLeaderboardScoringSchemeType);
                        return convertToLeaderboardGroupDTO(getService().apply(createLeaderboardGroupOp), false, false);
                    }
                });
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String newDescription, String newDisplayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD_GROUP.getStringPermissionForObjects(DefaultActions.UPDATE, oldName));
        getService().apply(
                new UpdateLeaderboardGroup(oldName, newName, newDescription, newDisplayName,
                        leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType));
    }

    @Override
    public ReplicationStateDTO getReplicaInfo() {
        ReplicationService service = getReplicationService();
        Set<ReplicaDTO> replicaDTOs = new HashSet<ReplicaDTO>();
        for (ReplicaDescriptor replicaDescriptor : service.getReplicaInfo()) {
            final Map<Class<? extends OperationWithResult<?, ?>>, Integer> statistics = service.getStatistics(replicaDescriptor);
            Map<String, Integer> replicationCountByOperationClassName = new HashMap<String, Integer>();
            for (Entry<Class<? extends OperationWithResult<?, ?>>, Integer> e : statistics.entrySet()) {
                replicationCountByOperationClassName.put(e.getKey().getName(), e.getValue());
            }
            replicaDTOs.add(new ReplicaDTO(replicaDescriptor.getIpAddress().getHostName(),
                    replicaDescriptor.getRegistrationTime().asDate(), replicaDescriptor.getUuid().toString(),
                    replicaDescriptor.getReplicableIdsAsStrings(), replicationCountByOperationClassName,
                    service.getAverageNumberOfOperationsPerMessage(replicaDescriptor),
                    service.getNumberOfMessagesSent(replicaDescriptor), service.getNumberOfBytesSent(replicaDescriptor),
                    service.getAverageNumberOfBytesPerMessage(replicaDescriptor)));
        }
        ReplicationMasterDTO master;
        ReplicationMasterDescriptor replicatingFromMaster = service.getReplicatingFromMaster();
        if (replicatingFromMaster == null) {
            master = null;
        } else {
            master = new ReplicationMasterDTO(replicatingFromMaster.getHostname(), replicatingFromMaster.getServletPort(),
                    replicatingFromMaster.getMessagingHostname(), replicatingFromMaster.getMessagingPort(), replicatingFromMaster.getExchangeName(),
                    StreamSupport.stream(replicatingFromMaster.getReplicables().spliterator(), /* parallel */ false).map(r->r.getId()).toArray(s->new String[s]));
        }
        return new ReplicationStateDTO(master, replicaDTOs, service.getServerIdentifier().toString());
    }

    /**
     * A warning shall be issued to the administration user if the {@link RacingEventService} is a replica. For all
     * other {@link Replicable}s such as the {@link SecurityService} we don't care.
     */
    @Override
    public String[] getReplicableIdsAsStringThatShallLeadToWarningAboutInstanceBeingReplica() {
        return new String[] { getService().getId().toString() };
    }
    
    @Override
    public void startReplicatingFromMaster(String messagingHost, String masterHost, String exchangeName, int servletPort, int messagingPort) throws IOException, ClassNotFoundException, InterruptedException {
        // The queue name must always be the same for this server. In order to achieve
        // this we're using the unique server identifier
        getReplicationService().startToReplicateFrom(
                ReplicationFactory.INSTANCE.createReplicationMasterDescriptor(messagingHost, masterHost, exchangeName, servletPort, messagingPort, 
                        /* use local server identifier as queue name */ getReplicationService().getServerIdentifier().toString(), getReplicationService().getAllReplicables()));
    }

    @Override
    public List<EventDTO> getEvents() throws MalformedURLException {
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(SecuredDomainType.EVENT,
                getService().getAllEvents(), event -> event.getId().toString(), event -> {
                    EventDTO eventDTO = convertToEventDTO(event, false);
                    try {
                        eventDTO.setBaseURL(getEventBaseURLFromEventOrRequest(event));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                    eventDTO.setIsOnRemoteServer(false);
                    return eventDTO;
                });
    }

    @Override
    public List<EventBaseDTO> getPublicEventsOfAllSailingServers() throws MalformedURLException {
        List<EventBaseDTO> result = new ArrayList<>();
        for (EventDTO localEvent : getEvents()) {
            if (localEvent.isPublic) {
                result.add(localEvent);
            }
        }
        for (Entry<RemoteSailingServerReference, com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception>> serverRefAndEventsOrException :
                        getService().getPublicEventsOfAllSailingServers().entrySet()) {
            final com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception> eventsOrException = serverRefAndEventsOrException.getValue();
            final RemoteSailingServerReference serverRef = serverRefAndEventsOrException.getKey();
            final Iterable<EventBase> remoteEvents = eventsOrException.getA();
            String baseURLFromServerReference = getBaseURL(serverRef.getURL()).toString();
            if (remoteEvents != null) {
                for (EventBase remoteEvent : remoteEvents) {
                    EventBaseDTO remoteEventDTO = convertToEventDTO(remoteEvent);
                    remoteEventDTO.setBaseURL(remoteEvent.getBaseURL() == null ? baseURLFromServerReference : remoteEvent.getBaseURL().toString());
                    remoteEventDTO.setIsOnRemoteServer(true);
                    result.add(remoteEventDTO);
                }
            }
        }
        return result;
    }

    /**
     * Determines the base URL (protocol, host and port parts) used for the currently executing servlet request. Defaults
     * to <code>http://sapsailing.com</code>.
     * @throws MalformedURLException 
     */
    private URL getRequestBaseURL() throws MalformedURLException {
        final URL url = new URL(getThreadLocalRequest().getRequestURL().toString());
        final URL baseURL = getBaseURL(url);
        return baseURL;
    }

    private URL getBaseURL(URL url) throws MalformedURLException {
        return new URL(url.getProtocol(), url.getHost(), url.getPort(), /* file */ "");
    }

    private RemoteSailingServerReferenceDTO createRemoteSailingServerReferenceDTO(
            final RemoteSailingServerReference serverRef,
            final com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception> eventsOrException) {
        final Iterable<EventBase> events = eventsOrException.getA();
        final Iterable<EventBaseDTO> eventDTOs;
        final RemoteSailingServerReferenceDTO sailingServerDTO;
        if (events == null) {
            eventDTOs = null;
            final Exception exception = eventsOrException.getB();
            sailingServerDTO = new RemoteSailingServerReferenceDTO(serverRef.getName(),
                    serverRef.getURL().toExternalForm(), exception==null?null:exception.getMessage());
        } else {
            eventDTOs = convertToEventDTOs(events);
            sailingServerDTO = new RemoteSailingServerReferenceDTO(
                    serverRef.getName(), serverRef
                            .getURL().toExternalForm(), eventDTOs);
        }
        return sailingServerDTO;
    }
    
    private Iterable<EventBaseDTO> convertToEventDTOs(Iterable<EventBase> events) {
        List<EventBaseDTO> result = new ArrayList<>();
        for (EventBase event : events) {
            EventBaseDTO eventDTO = convertToEventDTO(event);
            result.add(eventDTO);
        }
        return result;
    }

    @Override
    public EventDTO updateEvent(UUID eventId, String eventName, String eventDescription, Date startDate, Date endDate,
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURLString, String baseURLAsString,
            Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images, Iterable<VideoDTO> videos,
            Iterable<String> windFinderReviewedSpotCollectionIds) throws MalformedURLException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredDomainType.EVENT.getStringPermissionForObjects(DefaultActions.UPDATE, eventId.toString()))) {
            TimePoint startTimePoint = startDate != null ? new MillisecondsTimePoint(startDate) : null;
            TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
            URL officialWebsiteURL = officialWebsiteURLString != null ? new URL(officialWebsiteURLString) : null;
            URL baseURL = baseURLAsString != null ? new URL(baseURLAsString) : null;
            Map<Locale, URL> sailorsInfoWebsiteURLs = convertToLocalesAndUrls(sailorsInfoWebsiteURLsByLocaleName);
            List<ImageDescriptor> eventImages = convertToImages(images);
            List<VideoDescriptor> eventVideos = convertToVideos(videos);
            getService().apply(
                    new UpdateEvent(eventId, eventName, eventDescription, startTimePoint, endTimePoint, venue.getName(),
                        isPublic, leaderboardGroupIds, officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs, eventImages,
                        eventVideos, windFinderReviewedSpotCollectionIds));
            return getEventById(eventId, false);
        }
        throw new UnauthorizedException("You are not permitted to edit event " + eventId);
    }

    @Override
    public EventDTO createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue,
            boolean isPublic, List<String> courseAreaNames, String officialWebsiteURLAsString, String baseURLAsString,
            Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images, Iterable<VideoDTO> videos, 
            Iterable<UUID> leaderboardGroupIds)
            throws MalformedURLException, UnauthorizedException {
        final UUID eventUuid = UUID.randomUUID();

        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.EVENT,
                eventUuid.toString(), eventName, new ActionWithResult<EventDTO>() {
                    @Override
                    public EventDTO run() throws Exception {
                        TimePoint startTimePoint = startDate != null ? new MillisecondsTimePoint(startDate) : null;
                        TimePoint endTimePoint = endDate != null ? new MillisecondsTimePoint(endDate) : null;
                        URL officialWebsiteURL = officialWebsiteURLAsString != null
                                ? new URL(officialWebsiteURLAsString)
                                : null;
                        URL baseURL = baseURLAsString != null ? new URL(baseURLAsString) : null;
                        Map<Locale, URL> sailorsInfoWebsiteURLs = convertToLocalesAndUrls(
                                sailorsInfoWebsiteURLsByLocaleName);
                        List<ImageDescriptor> eventImages = convertToImages(images);
                        List<VideoDescriptor> eventVideos = convertToVideos(videos);
                        getService().apply(new CreateEvent(eventName, eventDescription, startTimePoint, endTimePoint,
                                venue, isPublic, eventUuid, officialWebsiteURL, baseURL, sailorsInfoWebsiteURLs,
                                eventImages, eventVideos, leaderboardGroupIds));
                        createCourseAreas(eventUuid, courseAreaNames.toArray(new String[courseAreaNames.size()]));
                        return getEventById(eventUuid, false);
                    }
                });
    }

    @Override
    public Pair<Integer, Integer> resolveImageDimensions(String imageUrlAsString) throws Exception {
        final Pair<Integer, Integer> imageDimensions;
        if (imageUrlAsString != null && !imageUrlAsString.isEmpty()) {
            URL imageURL = new URL(imageUrlAsString);
            imageDimensions = MediaUtils.getImageDimensions(imageURL);
        } else {
            imageDimensions = null;
        }
        return imageDimensions;
    }
    
    @Override
    public void createCourseAreas(UUID eventId, String[] courseAreaNames) {
        getSecurityService().checkCurrentUserUpdatePermission(getService().getEvent(eventId));
        final UUID[] courseAreaIDs = new UUID[courseAreaNames.length];
        for (int i=0; i<courseAreaNames.length; i++) {
            courseAreaIDs[i] = UUID.randomUUID();
        }
        getService().apply(new AddCourseAreas(eventId, courseAreaNames, courseAreaIDs));
    }

    @Override
    public void removeCourseAreas(UUID eventId, UUID[] courseAreaIds) {
        getSecurityService().checkCurrentUserDeletePermission(getService().getEvent(eventId));
        getService().apply(new RemoveCourseAreas(eventId, courseAreaIds));
    }

    @Override
    public void removeEvents(Collection<UUID> eventIds) throws UnauthorizedException {
        for (UUID eventId : eventIds) {
            removeEvent(eventId);
        }
    }

    @Override
    public void removeEvent(UUID eventId) throws UnauthorizedException {
        getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredDomainType.EVENT,
                eventId.toString(), new Action() {
                    @Override
                    public void run() throws Exception {
                        getService().apply(new RemoveEvent(eventId));
                    }
                });
    }

    @Override
    public void renameEvent(UUID eventId, String newName) throws UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredDomainType.EVENT.getStringPermissionForObjects(DefaultActions.UPDATE, eventId.toString()))) {
            getService().apply(new RenameEvent(eventId, newName));
        } else {
            throw new UnauthorizedException("You are not permitted to edit event " + eventId);
        }
    }

    @Override
    public EventDTO getEventById(UUID id, boolean withStatisticalData) throws MalformedURLException, UnauthorizedException {
        if (SecurityUtils.getSubject().isPermitted(SecuredDomainType.EVENT.getStringPermissionForObjects(DefaultActions.READ, id.toString()))) {
            EventDTO result = null;
            Event event = getService().getEvent(id);
            if (event != null) {
                result = convertToEventDTO(event, withStatisticalData);
                result.setBaseURL(getEventBaseURLFromEventOrRequest(event));
                result.setIsOnRemoteServer(false);
            }
            return result;
        }
        throw new UnauthorizedException("You are not permitted to view event " + id);
    }

    private String getEventBaseURLFromEventOrRequest(Event event) throws MalformedURLException {
        return event.getBaseURL() == null ? getRequestBaseURL().toString() : event.getBaseURL().toString();
    }

    private EventBaseDTO convertToEventDTO(EventBase event) {
        final EventBaseDTO eventDTO;
        if (event == null) {
            eventDTO = null;
        } else {
            List<LeaderboardGroupBaseDTO> lgDTOs = new ArrayList<>();
            if (event.getLeaderboardGroups() != null) {
                for (LeaderboardGroupBase lgBase : event.getLeaderboardGroups()) {
                    lgDTOs.add(convertToLeaderboardGroupBaseDTO(lgBase));
                }
            }
            eventDTO = new EventBaseDTO(event.getName(), lgDTOs);
            copyEventBaseFieldsToDTO(event, eventDTO);
        }
        return eventDTO;
    }

    private LeaderboardGroupBaseDTO convertToLeaderboardGroupBaseDTO(LeaderboardGroupBase leaderboardGroupBase) {
        return new LeaderboardGroupBaseDTO(leaderboardGroupBase.getId(), leaderboardGroupBase.getName(),
                leaderboardGroupBase.getDescription(), leaderboardGroupBase.getDisplayName(),
                leaderboardGroupBase.hasOverallLeaderboard());
    }
    
    private void copyEventBaseFieldsToDTO(EventBase event, EventBaseDTO eventDTO) {
        eventDTO.venue = new VenueDTO();
        eventDTO.venue.setName(event.getVenue() != null ? event.getVenue().getName() : null);
        eventDTO.startDate = event.getStartDate() != null ? event.getStartDate().asDate() : null;
        eventDTO.endDate = event.getStartDate() != null ? event.getEndDate().asDate() : null;
        eventDTO.isPublic = event.isPublic();
        eventDTO.id = (UUID) event.getId();
        eventDTO.setDescription(event.getDescription());
        eventDTO.setOfficialWebsiteURL(event.getOfficialWebsiteURL() != null ? event.getOfficialWebsiteURL().toString() : null);
        eventDTO.setBaseURL(event.getBaseURL() != null ? event.getBaseURL().toString() : null);
        for (Map.Entry<Locale, URL> sailorsInfoWebsiteEntry : event.getSailorsInfoWebsiteURLs().entrySet()) {
            eventDTO.setSailorsInfoWebsiteURL(sailorsInfoWebsiteEntry.getKey() == null ? null : sailorsInfoWebsiteEntry
                    .getKey().toLanguageTag(), sailorsInfoWebsiteEntry.getValue().toExternalForm());
        }
        for (ImageDescriptor image : event.getImages()) {
            eventDTO.addImage(convertToImageDTO(image));
        }
        for (VideoDescriptor video : event.getVideos()) {
            eventDTO.addVideo(convertToVideoDTO(video));
        }
    }

    private List<ImageDescriptor> convertToImages(Iterable<ImageDTO> images) throws MalformedURLException {
        List<ImageDescriptor> eventImages = new ArrayList<ImageDescriptor>();
        for (ImageDTO image : images) {
            try {
                eventImages.add(convertToImage(image));
            } catch(Exception e) {
                // broken URLs are not being stored
            }
        }
        return eventImages;
    }

    private List<VideoDescriptor> convertToVideos(Iterable<VideoDTO> videos) throws MalformedURLException {
        List<VideoDescriptor> eventVideos = new ArrayList<VideoDescriptor>();
        for (VideoDTO video : videos) {
            try {
                eventVideos.add(convertToVideo(video));
            } catch(Exception e) {
                // broken URLs are not being stored
            }
        }
        return eventVideos;
    }

    private Map<Locale, URL> convertToLocalesAndUrls(Map<String, String> sailorsInfoWebsiteURLsByLocaleName) {
        Map<Locale, URL> eventURLs = new HashMap<>();
        for (Map.Entry<String, String> entry : sailorsInfoWebsiteURLsByLocaleName.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    eventURLs.put(toLocale(entry.getKey()), new URL(entry.getValue()));
                } catch(Exception e) {
                    // broken URLs or Locales are not being stored
                }
            }
        }
        return eventURLs;
    }

    private ImageDescriptor convertToImage(ImageDTO image) throws MalformedURLException {
        ImageDescriptor result = new ImageDescriptorImpl(new URL(image.getSourceRef()), new MillisecondsTimePoint(image.getCreatedAtDate()));
        result.setCopyright(image.getCopyright());
        result.setTitle(image.getTitle());
        result.setSubtitle(image.getSubtitle());
        result.setCopyright(image.getCopyright());
        result.setSize(image.getWidthInPx(), image.getHeightInPx());
        result.setLocale(toLocale(image.getLocale()));
        for (String tag : image.getTags()) {
            result.addTag(tag);
        }
        return result;
    }

    private VideoDescriptor convertToVideo(VideoDTO video) throws MalformedURLException {
        MimeType mimeType = video.getMimeType();
        if(mimeType == null || mimeType == MimeType.unknown) {
            mimeType = MediaUtils.detectMimeTypeFromUrl(video.getSourceRef());
        }
        VideoDescriptor result = new VideoDescriptorImpl(new URL(video.getSourceRef()), mimeType, new MillisecondsTimePoint(video.getCreatedAtDate()));
        result.setCopyright(video.getCopyright());
        result.setTitle(video.getTitle());
        result.setSubtitle(video.getSubtitle());
        result.setCopyright(video.getCopyright());
        result.setLengthInSeconds(video.getLengthInSeconds());
        if(video.getThumbnailRef() != null && !video.getThumbnailRef().isEmpty())
        result.setThumbnailURL(new URL(video.getThumbnailRef()));
        result.setLocale(toLocale(video.getLocale()));
        for (String tag : video.getTags()) {
            result.addTag(tag);
        }
        return result;
    }

    private ImageDTO convertToImageDTO(ImageDescriptor image) {
        ImageDTO result = new ImageDTO(image.getURL().toString(), image.getCreatedAtDate() != null ? image.getCreatedAtDate().asDate() : null);
        result.setCopyright(image.getCopyright());
        result.setTitle(image.getTitle());
        result.setSubtitle(image.getSubtitle());
        result.setMimeType(image.getMimeType());
        result.setSizeInPx(image.getWidthInPx(), image.getHeightInPx());
        result.setLocale(toLocaleName(image.getLocale()));
        List<String> tags = new ArrayList<String>();
        for(String tag: image.getTags()) {
            tags.add(tag);
        }
        result.setTags(tags);
        return result;
    }

    private VideoDTO convertToVideoDTO(VideoDescriptor video) {
        VideoDTO result = new VideoDTO(video.getURL().toString(), video.getMimeType(), 
                video.getCreatedAtDate() != null ? video.getCreatedAtDate().asDate() : null);
        result.setCopyright(video.getCopyright());
        result.setTitle(video.getTitle());
        result.setSubtitle(video.getSubtitle());
        result.setThumbnailRef(video.getThumbnailURL() != null ? video.getThumbnailURL().toString() : null);
        result.setLengthInSeconds(video.getLengthInSeconds());
        result.setLocale(toLocaleName(video.getLocale()));
        List<String> tags = new ArrayList<String>();
        for(String tag: video.getTags()) {
            tags.add(tag);
        }
        result.setTags(tags);
        return result;
    }
    
    private Locale toLocale(String localeName) {
        if(localeName == null || localeName.isEmpty()) {
            return null;
        }
        return Locale.forLanguageTag(localeName);
    }
    
    private String toLocaleName(Locale locale) {
        if(locale == null) {
            return null;
        }
        return locale.toString();
    }

    private EventDTO convertToEventDTO(Event event, boolean withStatisticalData) {
        EventDTO eventDTO = new EventDTO(event.getName());
        copyEventBaseFieldsToDTO(event, eventDTO);
        eventDTO.venue.setCourseAreas(new ArrayList<CourseAreaDTO>());
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            CourseAreaDTO courseAreaDTO = convertToCourseAreaDTO(courseArea);
            eventDTO.venue.getCourseAreas().add(courseAreaDTO);
        }
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            eventDTO.addLeaderboardGroup(convertToLeaderboardGroupDTO(lg, /* withGeoLocationData */ false, withStatisticalData));
        }
        eventDTO.setWindFinderReviewedSpotsCollection(event.getWindFinderReviewedSpotsCollectionIds());
        final WindFinderTrackerFactory windFinderTrackerFactory = windFinderTrackerFactoryServiceTracker.getService();
        if (windFinderTrackerFactory != null) {
            eventDTO.setAllWindFinderSpotsUsedByEvent(new EventWindFinderUtil().getWindFinderSpotsToConsider(event,
                    windFinderTrackerFactory, /* useCachedSpotsForTrackedRaces */ false));
        }
        SecurityDTOUtil.addSecurityInformation(getSecurityService(), eventDTO, event.getIdentifier());
        return eventDTO;
    }

    private CourseAreaDTO convertToCourseAreaDTO(CourseArea courseArea) {
        CourseAreaDTO courseAreaDTO = new CourseAreaDTO(courseArea.getName());
        courseAreaDTO.id = courseArea.getId();
        return courseAreaDTO;
    }
    
    /** for backward compatibility with the regatta overview */
    @Override
    public List<RaceGroupDTO> getRegattaStructureForEvent(UUID eventId) {
        List<RaceGroupDTO> raceGroups = new ArrayList<RaceGroupDTO>();
        Event event = getService().getEvent(eventId);
        Map<Leaderboard, LeaderboardGroup> leaderboardWithLeaderboardGroups = new HashMap<Leaderboard, LeaderboardGroup>();
        for(LeaderboardGroup leaderboardGroup: event.getLeaderboardGroups()) {
            for(Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                leaderboardWithLeaderboardGroups.put(leaderboard, leaderboardGroup);
            }
        }
        if (event != null) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                    if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea() == courseArea) {
                        RaceGroupDTO raceGroup = new RaceGroupDTO(leaderboard.getName());
                        raceGroup.courseAreaIdAsString = courseArea.getId().toString();
                        raceGroup.displayName = getRegattaNameFromLeaderboard(leaderboard);
                        if(leaderboardWithLeaderboardGroups.containsKey(leaderboard)) {
                            raceGroup.leaderboardGroupName = leaderboardWithLeaderboardGroups.get(leaderboard).getName(); 
                        }
                        if (leaderboard instanceof RegattaLeaderboard) {
                            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                            for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
                                RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(series.getName());
                                raceGroup.getSeries().add(seriesDTO);
                                for (Fleet fleet : series.getFleets()) {
                                    FleetDTO fleetDTO = new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
                                    seriesDTO.getFleets().add(fleetDTO);
                                }
                                seriesDTO.getRaceColumns().addAll(convertToRaceColumnDTOs(series.getRaceColumns()));
                            }
                        } else {
                            RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(LeaderboardNameConstants.DEFAULT_SERIES_NAME);
                            raceGroup.getSeries().add(seriesDTO);
                            FleetDTO fleetDTO = new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null);
                            seriesDTO.getFleets().add(fleetDTO);
                            seriesDTO.getRaceColumns().addAll(convertToRaceColumnDTOs(leaderboard.getRaceColumns()));
                        }
                        raceGroups.add(raceGroup);
                    }
                }
            }
        }
        return raceGroups;
    }

    /**
     * the replacement service for {@link #getRegattaStructureForEvent(UUID)}
     */
    @Override
    public List<RaceGroupDTO> getRegattaStructureOfEvent(UUID eventId) {
        List<RaceGroupDTO> raceGroups = new ArrayList<RaceGroupDTO>();
        Event event = getService().getEvent(eventId);
        Map<Leaderboard, LeaderboardGroup> leaderboardWithLeaderboardGroups = new HashMap<Leaderboard, LeaderboardGroup>();
        for(LeaderboardGroup leaderboardGroup: event.getLeaderboardGroups()) {
            for(Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                leaderboardWithLeaderboardGroups.put(leaderboard, leaderboardGroup);
            }
        }
        if (event != null) {
            for(LeaderboardGroup leaderboardGroup: event.getLeaderboardGroups()) {
                for(Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                    RaceGroupDTO raceGroup = new RaceGroupDTO(leaderboard.getName());
                    for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                        if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea() == courseArea) {
                            raceGroup.courseAreaIdAsString = courseArea.getId().toString();
                            break;
                        }
                    }
                    raceGroup.displayName = getRegattaNameFromLeaderboard(leaderboard);
                    if(leaderboardWithLeaderboardGroups.containsKey(leaderboard)) {
                        raceGroup.leaderboardGroupName = leaderboardWithLeaderboardGroups.get(leaderboard).getName(); 
                    }
                    if (leaderboard instanceof RegattaLeaderboard) {
                        RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                        raceGroup.boatClass = regattaLeaderboard.getRegatta().getBoatClass().getDisplayName();
                        for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
                            RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(series.getName());
                            raceGroup.getSeries().add(seriesDTO);
                            for (Fleet fleet : series.getFleets()) {
                                FleetDTO fleetDTO = new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
                                seriesDTO.getFleets().add(fleetDTO);
                            }
                            seriesDTO.getRaceColumns().addAll(convertToRaceColumnDTOs(series.getRaceColumns()));
                        }
                    } else {
                        RaceGroupSeriesDTO seriesDTO = new RaceGroupSeriesDTO(LeaderboardNameConstants.DEFAULT_SERIES_NAME);
                        raceGroup.getSeries().add(seriesDTO);
                        FleetDTO fleetDTO = new FleetDTO(LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null);
                        seriesDTO.getFleets().add(fleetDTO);
                        seriesDTO.getRaceColumns().addAll(convertToRaceColumnDTOs(leaderboard.getRaceColumns()));

                        BoatClass boatClass = leaderboard.getBoatClass();
                        raceGroup.boatClass = boatClass != null ? boatClass.getDisplayName(): null;
                    }
                    raceGroups.add(raceGroup);
                }
            }
        }
        return raceGroups;
    }
    
    /**
     * The name of the regatta to be shown on the regatta overview webpage is retrieved from the name of the {@link Leaderboard}. Since regattas are
     * not always represented by a {@link Regatta} object in the Sailing Suite but need to be shown on the regatta overview page, the leaderboard is
     * used as the representative of the sailing regatta. When a display name is set for a leaderboard, this name is favored against the (mostly technical)
     * regatta name as the display name represents the publicly visible name of the regatta. 
     * <br>
     * When the leaderboard is a {@link RegattaLeaderboard} the name of the {@link Regatta} is used, otherwise the leaderboard 
     * is a {@link FlexibleLeaderboard} and it's name is used as the last option.
     * @param leaderboard The {@link Leaderboard} from which the name is be retrieved
     * @return the name of the regatta to be shown on the regatta overview page
     */
    private String getRegattaNameFromLeaderboard(Leaderboard leaderboard) {
        String regattaName;
        if (leaderboard.getDisplayName() != null && !leaderboard.getDisplayName().isEmpty()) {
            regattaName = leaderboard.getDisplayName();
        } else {
            if (leaderboard instanceof RegattaLeaderboard) {
                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                regattaName = regattaLeaderboard.getRegatta().getName();
            } else {
                regattaName = leaderboard.getName();
            }
        }
        return regattaName;
    }

    @Override
    public void removeRegattas(Collection<RegattaIdentifier> selectedRegattas) {
        for (RegattaIdentifier regatta : selectedRegattas) {
            removeRegatta(regatta);
        }
    }
    
    @Override
    public void removeRegatta(RegattaIdentifier regattaIdentifier) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            getSecurityService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredDomainType.REGATTA,
                    regatta.getName(), new Action() {
                        @Override
                        public void run() throws Exception {
                            getService().apply(new RemoveRegatta(regattaIdentifier));
                        }
                    });
        }
    }
    
    @Override
    public void removeSeries(RegattaIdentifier identifier, String seriesName) {
        getService().apply(new RemoveSeries(identifier, seriesName));
    }

    private RaceColumnInSeriesDTO convertToRaceColumnInSeriesDTO(RaceColumnInSeries raceColumnInSeries) {
        RaceColumnInSeriesDTO raceColumnInSeriesDTO = new RaceColumnInSeriesDTO(raceColumnInSeries.getSeries().getName(),
                raceColumnInSeries.getRegatta().getName());
        fillRaceColumnDTO(raceColumnInSeries, raceColumnInSeriesDTO);
        return raceColumnInSeriesDTO;
    }

    @Override
    public void updateRegatta(RegattaIdentifier regattaName, Date startDate, Date endDate, UUID defaultCourseAreaUuid, 
            RegattaConfigurationDTO configurationDTO, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            String registrationLinkSecret) {
        Regatta regatta = getService().getRegatta(regattaName);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        TimePoint startTimePoint = startDate != null ?  new MillisecondsTimePoint(startDate) : null;
        TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
        getService().apply(new UpdateSpecificRegatta(regattaName, startTimePoint, endTimePoint, defaultCourseAreaUuid,
                convertToRegattaConfiguration(configurationDTO), buoyZoneRadiusInHullLengths, useStartTimeInference,
                controlTrackingFromStartAndFinishTimes, registrationLinkSecret));
    }

    @Override
    public List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName,
            List<Pair<String, Integer>> columnNamesWithInsertIndex) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        List<RaceColumnInSeriesDTO> result = new ArrayList<RaceColumnInSeriesDTO>();
        for (Pair<String, Integer> columnNameAndInsertIndex : columnNamesWithInsertIndex) {
            RaceColumnInSeries raceColumnInSeries = getService().apply(
                    new AddColumnToSeries(columnNameAndInsertIndex.getB(), regattaIdentifier, seriesName, columnNameAndInsertIndex.getA()));
            if (raceColumnInSeries != null) {
                result.add(convertToRaceColumnInSeriesDTO(raceColumnInSeries));
            }
        }
        return result;
    }
    
    @Override
    public void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal, boolean isFleetsCanRunInParallel,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            Integer maximumNumberOfDiscards, List<FleetDTO> fleets) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        getService().apply(
                new UpdateSeries(regattaIdentifier, seriesName, newSeriesName, isMedal, isFleetsCanRunInParallel, resultDiscardingThresholds,
                        startsWithZeroScore, firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring,
                        maximumNumberOfDiscards, fleets));
    }

    @Override
    public void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        for(String columnName: columnNames) {
            getService().apply(new RemoveColumnFromSeries(regattaIdentifier, seriesName, columnName));
        }
    }

    @Override
    public void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        getService().apply(new MoveColumnInSeriesUp(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        Regatta regatta = getService().getRegatta(regattaIdentifier);
        if (regatta != null) {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        }
        getService().apply(new MoveColumnInSeriesDown(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public RegattaDTO createRegatta(String regattaName, String boatClassName,
            boolean canBoatsOfCompetitorsChangePerRace, CompetitorRegistrationType competitorRegistrationType, String registrationLinkSecret,
            Date startDate, Date endDate,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference,
            boolean controlTrackingFromStartAndFinishTimes, RankingMetrics rankingMetricType) {

        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.REGATTA,
                regattaName, regattaName, new ActionWithResult<RegattaDTO>() {

                    @Override
                    public RegattaDTO run() throws Exception {
                        TimePoint startTimePoint = startDate != null ? new MillisecondsTimePoint(startDate) : null;
                        TimePoint endTimePoint = endDate != null ? new MillisecondsTimePoint(endDate) : null;
                        Regatta regatta = getService().apply(new AddSpecificRegatta(regattaName, boatClassName,
                                canBoatsOfCompetitorsChangePerRace, competitorRegistrationType, registrationLinkSecret,
                                startTimePoint, endTimePoint, UUID.randomUUID(),
                                seriesNamesWithFleetNamesAndFleetOrderingAndMedal, persistent,
                                baseDomainFactory.createScoringScheme(scoringSchemeType), defaultCourseAreaId,
                                buoyZoneRadiusInHullLengths, useStartTimeInference,
                                controlTrackingFromStartAndFinishTimes, rankingMetricType));
                        return convertToRegattaDTO(regatta);
                    }
                });
    }
    
    @Override
    public RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName,
            String boatClassName, Date timePointWhenResultPublished) throws Exception {
        RegattaScoreCorrectionDTO result = null;
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp.getName().equals(scoreCorrectionProviderName)) {
                result = createScoreCorrection(scp.getScoreCorrections(eventName, boatClassName,
                        new MillisecondsTimePoint(timePointWhenResultPublished)));
                break;
            }
        }
        return result;
    }

    private RegattaScoreCorrectionDTO createScoreCorrection(RegattaScoreCorrections scoreCorrections) {
        // Key is the race name or number as String; values are maps whose key is the sailID.
        LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>> map = new LinkedHashMap<String, Map<String, ScoreCorrectionEntryDTO>>();
        for (ScoreCorrectionsForRace sc4r : scoreCorrections.getScoreCorrectionsForRaces()) {
            Map<String, ScoreCorrectionEntryDTO> entryMap = new HashMap<String, RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO>();
            for (String sailID : sc4r.getSailIDs()) {
                entryMap.put(sailID, createScoreCorrectionEntryDTO(sc4r.getScoreCorrectionForCompetitor(sailID)));
            }
            map.put(sc4r.getRaceNameOrNumber(), entryMap);
        }
        return new RegattaScoreCorrectionDTO(scoreCorrections.getProvider().getName(), map);
    }

    private ScoreCorrectionEntryDTO createScoreCorrectionEntryDTO(
            ScoreCorrectionForCompetitorInRace scoreCorrectionForCompetitor) {
        return new ScoreCorrectionEntryDTO(scoreCorrectionForCompetitor.getPoints(),
                scoreCorrectionForCompetitor.isDiscarded(), scoreCorrectionForCompetitor.getMaxPointsReason());
    }
    
    @Override
    public List<Pair<String, String>> getUrlResultProviderNamesAndOptionalSampleURL() {
        List<Pair<String, String>> result = new ArrayList<>();
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp instanceof ResultUrlProvider) {
                result.add(new Pair<>(scp.getName(), ((ResultUrlProvider) scp).getOptionalSampleURL()));
            }
        }
        return result;
    }

    private ResultUrlProvider getUrlBasedScoreCorrectionProvider(String resultProviderName) {
        ResultUrlProvider result = null;
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp instanceof ResultUrlProvider && scp.getName().equals(resultProviderName)) {
                result = (ResultUrlProvider) scp;
                break;
            }
        }
        return result;
    }

    @Override
    public ServerInfoDTO getServerInfo() {
        ServerInfoDTO result = new ServerInfoDTO(ServerInfo.getName(), ServerInfo.getBuildVersion());
        return result;
    }

    @Override
    public ServerConfigurationDTO getServerConfiguration() {
        SailingServerConfiguration sailingServerConfiguration = getService().getSailingServerConfiguration();
        ServerConfigurationDTO result = new ServerConfigurationDTO(sailingServerConfiguration.isStandaloneServer(),
                isPublicServer(), isSelfServiceServer());
        return result;
    }

    @Override
    public void updateServerConfiguration(ServerConfigurationDTO serverConfiguration) {
        getService().apply(new UpdateServerConfiguration(
                new SailingServerConfigurationImpl(serverConfiguration.isStandaloneServer())));
        if (serverConfiguration.isPublic() != null || serverConfiguration.isSelfService() != null) {
            final User allUser = getSecurityService().getAllUser();
            if (allUser != null) {
                final WildcardPermission createObjectOnCurrentServerPermission = SecuredSecurityTypes.SERVER
                        .getPermissionForObjects(ServerActions.CREATE_OBJECT, ServerInfo.getName());
                if (serverConfiguration.isSelfService() != null) {
                    if (serverConfiguration.isSelfService()) {
                        getSecurityService().addPermissionForUser(allUser.getName(),
                                createObjectOnCurrentServerPermission);
                    } else {
                        getSecurityService().removePermissionFromUser(allUser.getName(),
                                createObjectOnCurrentServerPermission);
                    }
                }
                if (serverConfiguration.isPublic() != null) {
                    final RoleDefinition viewerRole = getSecurityService()
                            .getRoleDefinition(SailingViewerRole.getInstance().getId());
                    final UserGroup defaultServerTenant = getSecurityService().getDefaultTenant();
                    if (viewerRole != null && defaultServerTenant != null) {
                        final RoleImpl publicAccessForServerRole = new RoleImpl(viewerRole, defaultServerTenant, null);
                        if (serverConfiguration.isPublic()) {
                            getSecurityService().addRoleForUser(allUser.getName(), publicAccessForServerRole);
                        } else {
                            getSecurityService().removeRoleFromUser(allUser.getName(), publicAccessForServerRole);
                        }
                    } else {
                        throw new IllegalArgumentException("Viewerrole or defaultServerTenant is not existing");
                    }

                }
            } else {
                throw new IllegalArgumentException("Alluser is not exiting");
            }
        }
    }

    @Override
    public List<RemoteSailingServerReferenceDTO> getRemoteSailingServerReferences() {
        List<RemoteSailingServerReferenceDTO> result = new ArrayList<RemoteSailingServerReferenceDTO>();
        for (Entry<RemoteSailingServerReference, com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception>> remoteSailingServerRefAndItsCachedEvent :
                    getService().getPublicEventsOfAllSailingServers().entrySet()) {
            RemoteSailingServerReferenceDTO dto = createRemoteSailingServerReferenceDTO(
                    remoteSailingServerRefAndItsCachedEvent.getKey(),
                    remoteSailingServerRefAndItsCachedEvent.getValue());
            result.add(dto);
        }
        return result;
    }

    @Override
    public void removeSailingServers(Set<String> namesOfSailingServersToRemove) throws Exception {
        for (String serverName : namesOfSailingServersToRemove) {
            getService().apply(new RemoveRemoteSailingServerReference(serverName));
        }
    }

    @Override
    public RemoteSailingServerReferenceDTO addRemoteSailingServerReference(RemoteSailingServerReferenceDTO sailingServer) throws MalformedURLException {
        final String expandedURL;
        if (sailingServer.getUrl().contains("//")) {
            expandedURL = sailingServer.getUrl();
        } else {
            expandedURL = "http://" + sailingServer.getUrl();
        }
        URL serverURL = new URL(expandedURL);
        RemoteSailingServerReference serverRef = getService().apply(new AddRemoteSailingServerReference(sailingServer.getName(), serverURL));
        com.sap.sse.common.Util.Pair<Iterable<EventBase>, Exception> eventsOrException = getService().updateRemoteServerEventCacheSynchronously(serverRef);
        return createRemoteSailingServerReferenceDTO(serverRef, eventsOrException);
        
    }

    @Override
    public List<String> getResultImportUrls(String resultProviderName) {
        List<String> result = new ArrayList<String>();
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        ResultUrlRegistry resultUrlRegistry = getResultUrlRegistry();
        if (urlBasedScoreCorrectionProvider != null) {
            Iterable<URL> allUrls = resultUrlRegistry.getResultUrls(resultProviderName);
            final Subject subject = SecurityUtils.getSubject();
            final WildcardPermissionEncoder wildcardPermissionEncoder = new WildcardPermissionEncoder();
            for (URL url : allUrls) {
                if (subject.isPermitted(SecuredDomainType.RESULT_IMPORT_URL.getStringPermissionForObjects(DefaultActions.READ,
                        wildcardPermissionEncoder.encodeStringList(resultProviderName, url.toString())))) {
                    result.add(url.toString());
                }
            }
        }
        return result;
    }

    @Override
    public void removeResultImportURLs(String resultProviderName, Set<String> toRemove) throws Exception {
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        ResultUrlRegistry resultUrlRegistry = getResultUrlRegistry();
        if (urlBasedScoreCorrectionProvider != null) {
            for (String urlToRemove : toRemove) {
                resultUrlRegistry.unregisterResultUrl(resultProviderName, new URL(urlToRemove));
            }
        }
    }

    @Override
    public void addResultImportUrl(String resultProviderName, String url) throws Exception {
        ResultUrlProvider urlBasedScoreCorrectionProvider = getUrlBasedScoreCorrectionProvider(resultProviderName);
        if (urlBasedScoreCorrectionProvider != null) {
            ResultUrlRegistry resultUrlRegistry = getResultUrlRegistry();
            resultUrlRegistry.registerResultUrl(resultProviderName, new URL(url));
        }
    }

    private ResultUrlRegistry getResultUrlRegistry() {
        return resultUrlRegistryServiceTracker.getService();
    }    

    @Override
    public List<String> getOverallLeaderboardNamesContaining(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Couldn't find leaderboard named "+leaderboardName);
        }
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, Leaderboard> leaderboardEntry : getService().getLeaderboards().entrySet()) {
            if (leaderboardEntry.getValue() instanceof MetaLeaderboard) {
                MetaLeaderboard metaLeaderboard = (MetaLeaderboard) leaderboardEntry.getValue();
                if (Util.contains(metaLeaderboard.getLeaderboards(), leaderboard)) {
                    result.add(leaderboardEntry.getKey());
                }
            }
        }
        return result;
    }

    @Override
    public List<SwissTimingArchiveConfigurationDTO> getPreviousSwissTimingArchiveConfigurations() {
        Iterable<SwissTimingArchiveConfiguration> configs = swissTimingAdapterPersistence
                .getSwissTimingArchiveConfigurations();
        return getSecurityService().mapAndFilterByReadPermissionForCurrentUser(
                SecuredDomainType.SWISS_TIMING_ARCHIVE_ACCOUNT, configs, SwissTimingArchiveConfiguration::getJsonUrl,
                stArchiveConfig -> new SwissTimingArchiveConfigurationDTO(stArchiveConfig.getJsonUrl()));
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(String swissTimingJsonUrl) throws Exception {
        getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredDomainType.SWISS_TIMING_ACCOUNT, swissTimingJsonUrl,
                swissTimingJsonUrl, () -> swissTimingAdapterPersistence.storeSwissTimingArchiveConfiguration(
                        swissTimingFactory.createSwissTimingArchiveConfiguration(swissTimingJsonUrl)));
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(String leaderboardName,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset, final List<String> visibleRegattas)
            throws NoWindException, InterruptedException, ExecutionException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        return getRaceStateEntriesForLeaderboard(leaderboard, showOnlyCurrentlyRunningRaces, showOnlyRacesOfSameDay, clientTimeZoneOffset, visibleRegattas);
    }

    /**
     * The client's day starts at <code>00:00:00Z - clientTimeZoneOffset</code> and ends at <code>23:59:59Z - clientTimeZoneOffset</code>.
     */
    private List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(Leaderboard leaderboard,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset, final List<String> visibleRegattas)
            throws NoWindException, InterruptedException, ExecutionException {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        Calendar dayToCheck = Calendar.getInstance();
        dayToCheck.setTime(new Date());
        CourseArea usedCourseArea = leaderboard.getDefaultCourseArea();
        if (leaderboard != null) {
            if (visibleRegattas != null && !visibleRegattas.contains(leaderboard.getName())) {
                return result;
            } 
            String regattaName = getRegattaNameFromLeaderboard(leaderboard);
            if (leaderboard instanceof RegattaLeaderboard) {
                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                Regatta regatta = regattaLeaderboard.getRegatta();
                BoatClass boatClass = regatta.getBoatClass();
                Distance buyZoneRadius = RegattaUtil.getCalculatedRegattaBuoyZoneRadius(regatta, boatClass);
                for (Series series : regatta.getSeries()) {
                    Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                    for (RaceColumn raceColumn : series.getRaceColumns()) {
                        getRegattaOverviewEntries(showOnlyRacesOfSameDay, clientTimeZoneOffset, dayToCheck,
                                usedCourseArea, leaderboard, boatClass.getName(), regattaName, buyZoneRadius,
                                series.getName(), raceColumn, entriesPerFleet);
                    }
                    result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
                }

            } else if(leaderboard instanceof FlexibleLeaderboard) {
                BoatClass boatClass = null;
                for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    boatClass = trackedRace.getRace().getBoatClass();
                    break;
                }
                Distance buyZoneRadius = RegattaUtil.getCalculatedRegattaBuoyZoneRadius(null, boatClass);
                Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    getRegattaOverviewEntries(showOnlyRacesOfSameDay, clientTimeZoneOffset, dayToCheck, usedCourseArea,
                            leaderboard, boatClass == null ? "" : boatClass.getName(), regattaName, buyZoneRadius,
                            LeaderboardNameConstants.DEFAULT_SERIES_NAME, raceColumn, entriesPerFleet);
                }
                result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
            }
        }
        return result;
    }
    
    private void createRegattaFromRegattaDTO(RegattaDTO regatta) {
        this.createRegatta(regatta.getName(), regatta.boatClass.getName(),
                regatta.canBoatsOfCompetitorsChangePerRace, regatta.competitorRegistrationType, regatta.registrationLinkSecret,
                regatta.startDate, regatta.endDate,
                new RegattaCreationParametersDTO(getSeriesCreationParameters(regatta)), 
                true, regatta.scoringScheme, regatta.defaultCourseAreaUuid, regatta.buoyZoneRadiusInHullLengths, regatta.useStartTimeInference,
                regatta.controlTrackingFromStartAndFinishTimes, regatta.rankingMetricType);
    }
    
    private SeriesParameters getSeriesParameters(SeriesDTO seriesDTO) {
        SeriesParameters series = new SeriesParameters(false, false, false, null, seriesDTO.getMaximumNumberOfDiscards());
        series.setFirstColumnIsNonDiscardableCarryForward(seriesDTO.isFirstColumnIsNonDiscardableCarryForward());
        series.setHasSplitFleetContiguousScoring(seriesDTO.hasSplitFleetContiguousScoring());
        series.setStartswithZeroScore(seriesDTO.isStartsWithZeroScore());
        series.setDiscardingThresholds(seriesDTO.getDiscardThresholds());
        return series;
    }
    
    private LinkedHashMap<String, SeriesCreationParametersDTO> getSeriesCreationParameters(RegattaDTO regattaDTO) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParams = new LinkedHashMap<String, SeriesCreationParametersDTO>();
            for (SeriesDTO series : regattaDTO.series){
                SeriesParameters seriesParameters = getSeriesParameters(series);
                seriesCreationParams.put(series.getName(), new SeriesCreationParametersDTO(series.getFleets(),
                false, true, seriesParameters.isStartswithZeroScore(), seriesParameters.isFirstColumnIsNonDiscardableCarryForward(),
                        seriesParameters.getDiscardingThresholds(), seriesParameters.isHasSplitFleetContiguousScoring(),
                        seriesParameters.getMaximumNumberOfDiscards()));
            }
        return seriesCreationParams;
    }

    @Override
    public Iterable<RegattaDTO> getRegattas(String manage2SailJsonUrl) { 
        StructureImporter structureImporter = new StructureImporter(new SetRacenumberFromSeries(), baseDomainFactory);
        Iterable<RegattaJSON> parsedEvent = structureImporter.parseEvent(manage2SailJsonUrl);
        List<RegattaDTO> regattaDTOs = new ArrayList<RegattaDTO>();
        Iterable<Regatta> regattas = structureImporter.getRegattas(parsedEvent);
        for (Regatta regatta : regattas) {
            regattaDTOs.add(convertToRegattaDTO(regatta));
        }
        return regattaDTOs;
    }

    /**
     * Uses {@link #addRaceColumnsToSeries} which also handles replication to update the regatta identified
     * by <code>regatta</code>'s {@link RegattaDTO#getRegattaIdentifier() identifier} with the race columns
     * as specified by <code>regatta</code>. The domain regatta object is assumed to have no races associated
     * when this method is called.
     */
    private void addRaceColumnsToRegattaSeries(RegattaDTO regatta) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regatta.getName()));
        for (SeriesDTO series : regatta.series) {
            List<Pair<String, Integer>> raceNamesAndInsertIndex = new ArrayList<>();
            int insertIndex = 0;
            for (RaceColumnDTO raceColumnInSeries : series.getRaceColumns()) {
                raceNamesAndInsertIndex.add(new Pair<>(raceColumnInSeries.getName(), insertIndex));
                insertIndex++;
            }
            addRaceColumnsToSeries(regatta.getRegattaIdentifier(), series.getName(), raceNamesAndInsertIndex);
        }
    }

    @Override
    public void createRegattaStructure(final Iterable<RegattaDTO> regattas,
            final EventDTO newEvent) throws MalformedURLException {
        final List<String> leaderboardNames = new ArrayList<String>();
        for (RegattaDTO regatta : regattas) {
            createRegattaFromRegattaDTO(regatta);
            addRaceColumnsToRegattaSeries(regatta);
            if (getLeaderboard(regatta.getName()) == null) {
                leaderboardNames.add(regatta.getName());
                createRegattaLeaderboard(new RegattaName(regatta.getName()), regatta.boatClass.toString(),
                        new int[0]);
            }
        }
        createAndAddLeaderboardGroup(newEvent, leaderboardNames);
        // TODO find a way to import the competitors for the selected regattas. You'll need the regattas as Iterable<RegattaResults>
        // structureImporter.setCompetitors(regattas, "");
    }

    private void createAndAddLeaderboardGroup(final EventDTO newEvent,
            List<String> leaderboardNames) throws MalformedURLException {
        LeaderboardGroupDTO leaderboardGroupDTO = null;
        String description = "";
        if (newEvent.getDescription() != null) {
            description = newEvent.getDescription();
        }
        String eventName = newEvent.getName();
        List<UUID> eventLeaderboardGroupUUIDs = new ArrayList<>();

        // create Leaderboard Group
        if (getService().getLeaderboardGroupByName(eventName) == null) {
            leaderboardGroupDTO = doCreateLeaderboardGroup(eventName, description, eventName, false,
                    null, null,
                    leaderboardNames);
            eventLeaderboardGroupUUIDs.add(leaderboardGroupDTO.getId());
        } else {
            updateLeaderboardGroup(eventName, eventName, newEvent.getDescription(), eventName, leaderboardNames, null, null);
            leaderboardGroupDTO = getLeaderboardGroupByName(eventName, false);
        }
        for (LeaderboardGroupDTO lg : newEvent.getLeaderboardGroups()) {
            eventLeaderboardGroupUUIDs.add(lg.getId());
        }
        updateEvent(newEvent.id, newEvent.getName(), description, newEvent.startDate, newEvent.endDate, newEvent.venue,
                newEvent.isPublic, eventLeaderboardGroupUUIDs, newEvent.getOfficialWebsiteURL(),
                newEvent.getBaseURL(), newEvent.getSailorsInfoWebsiteURLs(), newEvent.getImages(), newEvent.getVideos(),
                newEvent.getWindFinderReviewedSpotsCollectionIds());
    }
    
    @Override
    public List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreaIds,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset)
            throws NoWindException, InterruptedException, ExecutionException {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        Calendar dayToCheck = Calendar.getInstance();
        dayToCheck.setTime(new Date());
        Event event = getService().getEvent(eventId);
        if (event != null) {
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (visibleCourseAreaIds.contains(courseArea.getId())) {
                    for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                        final CourseArea leaderboardDefaultCourseArea = leaderboard.getDefaultCourseArea();
                        if (leaderboardDefaultCourseArea != null && leaderboardDefaultCourseArea.equals(courseArea)) {
                            result.addAll(getRaceStateEntriesForLeaderboard(leaderboard.getName(),
                                    showOnlyCurrentlyRunningRaces, showOnlyRacesOfSameDay, clientTimeZoneOffset, visibleRegattas));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * The client's day starts at <code>00:00:00Z - clientTimeZoneOffset</code> and ends at <code>23:59:59Z - clientTimeZoneOffset</code>.
     */
    private void getRegattaOverviewEntries(boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset,
            Calendar dayToCheck, CourseArea courseArea, Leaderboard leaderboard, String boatClassName,
            String regattaName, Distance buyZoneRadius, String seriesName, RaceColumn raceColumn,
            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {
        if (!raceColumn.isCarryForward()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RegattaOverviewEntryDTO entry = createRegattaOverviewEntryDTO(courseArea,
                        leaderboard, boatClassName, regattaName, buyZoneRadius, seriesName, raceColumn, fleet, 
                        showOnlyRacesOfSameDay, clientTimeZoneOffset, dayToCheck);
                if (entry != null) {
                    addRegattaOverviewEntryToEntriesPerFleet(entriesPerFleet, fleet, entry);
                }
            }
        }
    }

    private List<RegattaOverviewEntryDTO> getRegattaOverviewEntriesToBeShown(boolean showOnlyCurrentlyRunningRaces,
            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {
        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
        for (List<RegattaOverviewEntryDTO> entryList : entriesPerFleet.values()) {
            result.addAll(entryList);
            if (showOnlyCurrentlyRunningRaces) {
                List<RegattaOverviewEntryDTO> finishedEntries = new ArrayList<RegattaOverviewEntryDTO>();
                for (RegattaOverviewEntryDTO entry : entryList) {
                    if (!RaceLogRaceStatus.isActive(entry.raceInfo.lastStatus)) {
                        if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                            finishedEntries.add(entry);
                        } else if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                            //don't filter when the race is unscheduled and aborted before
                            if (!entry.raceInfo.isRaceAbortedInPassBefore) {
                                result.remove(entry);
                            }
                            
                        }
                    }
                }
                if (!finishedEntries.isEmpty()) {
                    //keep the last finished race in the list to be shown
                    int indexOfLastElement = finishedEntries.size() - 1;
                    finishedEntries.remove(indexOfLastElement);
                    
                    //... and remove all other finished races
                    result.removeAll(finishedEntries);
                }
            }
        }
        return result;
    }

    private void addRegattaOverviewEntryToEntriesPerFleet(Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet,
            Fleet fleet, RegattaOverviewEntryDTO entry) {
        if (!entriesPerFleet.containsKey(fleet.getName())) {
           entriesPerFleet.put(fleet.getName(), new ArrayList<RegattaOverviewEntryDTO>()); 
        }
        entriesPerFleet.get(fleet.getName()).add(entry);
    }

    /**
     * The client's day starts at <code>00:00:00Z - clientTimeZoneOffset</code> and ends at <code>23:59:59Z - clientTimeZoneOffset</code>.
     */
    private RegattaOverviewEntryDTO createRegattaOverviewEntryDTO(CourseArea courseArea, Leaderboard leaderboard,
            String boatClassName, String regattaName, Distance buyZoneRadius, String seriesName, RaceColumn raceColumn,
            Fleet fleet, boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset, Calendar dayToCheck) {
        RegattaOverviewEntryDTO entry = new RegattaOverviewEntryDTO();
        if (courseArea != null) {
            entry.courseAreaName = courseArea.getName();
            entry.courseAreaIdAsString = courseArea.getId().toString();
        } else {
            entry.courseAreaName = "Default";
            entry.courseAreaIdAsString = "Default";
        }
        entry.boatClassName = boatClassName;
        entry.regattaDisplayName = regattaName;
        entry.leaderboardName = leaderboard.getName();
        entry.raceInfo = createRaceInfoDTO(seriesName, raceColumn, fleet);
        entry.currentServerTime = new Date();
        entry.buyZoneRadius = buyZoneRadius;
        if (showOnlyRacesOfSameDay) {
            if (!RaceStateOfSameDayHelper.isRaceStateOfSameDay(entry.raceInfo.startTime, entry.raceInfo.finishedTime,
                    entry.raceInfo.abortingTimeInPassBefore, dayToCheck, clientTimeZoneOffset)) {
                entry = null;
            }
        }
        return entry;
    }

    @Override
    public void stopReplicatingFromMaster() {
        try {
            getReplicationService().stopToReplicateFromMaster();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception trying to stop replicating from master", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopAllReplicas() {
        try {
            getReplicationService().stopAllReplicas();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception trying to stop all replicas from receiving updates from this master", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopSingleReplicaInstance(String identifier) {
        UUID uuid = UUID.fromString(identifier);
        try {
            getReplicationService().unregisterReplica(uuid);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception trying to unregister replica with UUID "+uuid, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
        getService().reloadRaceLog(leaderboardName, raceColumnDTO.getName(), fleet.getName());
    }

    @Override
    public RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
        Leaderboard lb = getService().getLeaderboardByName(leaderboardName);
        getSecurityService().checkCurrentUserReadPermission(lb);
        if (lb instanceof RegattaLeaderboard) {
            getSecurityService().checkCurrentUserReadPermission(((RegattaLeaderboard) lb).getRegatta());
        }
        RaceLogDTO result = null;
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnDTO.getName(), fleet.getName());
        if(raceLog != null) {
            List<RaceLogEventDTO> entries = new ArrayList<RaceLogEventDTO>();
            result = new RaceLogDTO(leaderboardName, raceColumnDTO.getName(), fleet.getName(), raceLog.getCurrentPassId(), entries);
            raceLog.lockForRead();
            try {
                for(RaceLogEvent raceLogEvent: raceLog.getRawFixes()) {
                    RaceLogEventDTO entry = new RaceLogEventDTO(raceLogEvent.getPassId(), 
                            raceLogEvent.getAuthor().getName(), raceLogEvent.getAuthor().getPriority(), 
                            raceLogEvent.getCreatedAt() != null ? raceLogEvent.getCreatedAt().asDate() : null,
                            raceLogEvent.getLogicalTimePoint() != null ? raceLogEvent.getLogicalTimePoint().asDate() : null,
                            raceLogEvent.getClass().getSimpleName(), raceLogEvent.getShortInfo());
                    entries.add(entry);
                }
            } finally {
                raceLog.unlockAfterRead();
            }
        }
        return result;
    }

    @Override
    public RegattaLogDTO getRegattaLog(String leaderboardName) throws DoesNotHaveRegattaLogException {
        RegattaLogDTO result = null;
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        if (regattaLog != null) {
            List<RegattaLogEventDTO> entries = new ArrayList<>();
            result = new RegattaLogDTO(leaderboardName, entries);
            regattaLog.lockForRead();
            try {
                for(RegattaLogEvent raceLogEvent: regattaLog.getRawFixes()) {
                    RegattaLogEventDTO entry = new RegattaLogEventDTO( 
                            raceLogEvent.getAuthor().getName(), raceLogEvent.getAuthor().getPriority(), 
                            raceLogEvent.getCreatedAt() != null ? raceLogEvent.getCreatedAt().asDate() : null,
                            raceLogEvent.getLogicalTimePoint() != null ? raceLogEvent.getLogicalTimePoint().asDate() : null,
                            raceLogEvent.getClass().getSimpleName(), raceLogEvent.getShortInfo());
                    entries.add(entry);
                }
            } finally {
                regattaLog.unlockAfterRead();
            }
        }
        return result;
    }

    private String getTokenForServer(String hostname, String username, String password) {
        String token = "";
        try {
            URL base = createBaseUrl(hostname);
            String path = "/security/api/restsecurity/access_token";
            URL serverAddress = createUrl(base, path, null);
            URLConnection connection = HttpUrlConnectionHelper.redirectConnection(serverAddress, Duration.ONE_MINUTE,
                    t -> {
                        String auth = username + ":" + password;
                        String base64 = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                        t.setRequestProperty("Authorization", "Basic " + base64);
                    });

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = connection.getInputStream().read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String jsonToken = result.toString("UTF-8");
            Object requestBody = JSONValue.parseWithException(jsonToken);
            if (requestBody instanceof JSONObject) {
                JSONObject json = (JSONObject) requestBody;
                Object tokenObj = json.get("access_token");
                if (tokenObj instanceof String) {
                    token = (String) tokenObj;
                }
                System.out.println(json);
            } else {
                throw new RuntimeException("Could not obtain token for server");
            }
            System.out.println(jsonToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    @Override
    public List<String> getLeaderboardGroupNamesFromRemoteServer(String url, String username, String password) {
        String token = getTokenForServer(url, username, password);
        // FIXME: Add checks here that ensure that the current use is allowed to do MDI
        final String path = "/sailingserver/api/v1/leaderboardgroups";
        final String query = null;
        URL serverAddress = null;
        InputStream inputStream = null;
        URLConnection connection = null;
        try {
            URL base = createBaseUrl(url);
            serverAddress = createUrl(base, path, query);
            connection = HttpUrlConnectionHelper.redirectConnection(serverAddress, Duration.ONE_MINUTE,
                    t -> t.setRequestProperty("Authorization", "Bearer " + token));
            inputStream = connection.getInputStream();
            InputStreamReader in = new InputStreamReader(inputStream, "UTF-8");
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            org.json.simple.JSONArray array = (org.json.simple.JSONArray) parser.parse(in);
            List<String> names = new ArrayList<String>();
            for (Object obj : array) {
                names.add((String) obj);
            }
            return names;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // close the connection
            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Strips off trailing slash and replaces an omitted or unknown protocol by HTTP
     */
    private URL createBaseUrl(String urlAsString) throws MalformedURLException {
        final String urlAsStringWithTrailingSlashRemoved = urlAsString == null ?
                null : urlAsString.length()>0 && urlAsString.charAt(urlAsString.length()-1)=='/' ?
                        urlAsString.substring(0, urlAsString.length()-1) : urlAsString;
        URL url;
        try {
            url = new URL(urlAsStringWithTrailingSlashRemoved);
        } catch (MalformedURLException e1) {
            // trying to strip off an unknown protocol, defaulting to HTTP
            String urlAsStringAfterFormatting = urlAsStringWithTrailingSlashRemoved;
            if (urlAsStringAfterFormatting.contains("://")) {
                urlAsStringAfterFormatting = urlAsStringWithTrailingSlashRemoved.split("://")[1];
            }
            url = new URL("http://" + urlAsStringAfterFormatting);
        }
        return url;
    }

    @Override
    public UUID importMasterData(final String urlAsString, final String[] groupNames, final boolean override,
            final boolean compress, final boolean exportWind, final boolean exportDeviceConfigurations,
            String targetServerUsername, String targetServerPassword) {
        // FIXME should the targetserver also check this?
        SecurityUtils.getSubject().isPermitted(SecuredDomainType.CAN_IMPORT_MASTERDATA
                .getStringPermissionForObjects(DefaultActions.CREATE, ServerInfo.getName()));

        String token = getTokenForServer(urlAsString, targetServerUsername, targetServerPassword);

        final UUID importOperationId = UUID.randomUUID();
        getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.0, DataImportSubProgress.INIT,
                0.0);
        final User user = getSecurityService().getCurrentUser();
        final UserGroup tenant = getSecurityService().getDefaultTenantForCurrentUser();
        
        // Create a progress indicator for as long as the server gets data from the other server.
        // As soon as the server starts the import operation, a progress object will be built on every server
        Runnable masterDataImportTask = new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.01,
                        DataImportSubProgress.CONNECTION_SETUP, 0.5);
                String query;
                try {
                    query = createLeaderboardQuery(groupNames, compress, exportWind, exportDeviceConfigurations);
                } catch (UnsupportedEncodingException e1) {
                    throw new RuntimeException(e1);
                }
                URLConnection connection = null;
                URL serverAddress = null;
                InputStream inputStream = null;
                try {
                    String masterDataPath = "/sailingserver/spi/v1/masterdata/leaderboardgroups";
                    URL base = createBaseUrl(urlAsString);
                    serverAddress = createUrl(base, masterDataPath, query);
                    // the response can take a very long time for MDI that include foiling data or such
                    connection = HttpUrlConnectionHelper.redirectConnection(serverAddress, Duration.ONE_HOUR.times(2),
                            t -> t.setRequestProperty("Authorization", "Bearer " + token));
                    getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.02,
                            DataImportSubProgress.CONNECTION_ESTABLISH, 0.5);
                    if (compress) {
                        InputStream timeoutExtendingInputStream = new TimeoutExtendingInputStream(
                                connection.getInputStream(), connection);
                        inputStream = new GZIPInputStream(timeoutExtendingInputStream);
                    } else {
                        inputStream = new TimeoutExtendingInputStream(connection.getInputStream(), connection);
                    }
                    final MasterDataImporter importer = new MasterDataImporter(baseDomainFactory, getService(),
                            user,
                            tenant);
                    importer.importFromStream(inputStream, importOperationId, override);
                } catch (Throwable e) {
                    // do not assume that RuntimeException is logged properly
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    getService().setDataImportFailedWithReplication(importOperationId, e.getMessage()
                            + "\n\nHave you checked if the"
                            + " versions (commit-wise) of the importing and exporting servers are compatible with each other? "
                            + "If the error still occurs, when both servers are running the same version, please report the problem.");
                    throw new RuntimeException(e);
                } finally {
                    // close the connection, set all objects to null
                    getService().setDataImportDeleteProgressFromMapTimerWithReplication(importOperationId);
                    if (connection != null && connection instanceof HttpURLConnection) {
                        ((HttpURLConnection) connection).disconnect();
                    }
                    connection = null;
                    long timeToImport = System.currentTimeMillis() - startTime;
                    logger.info(String.format("Took %s ms overall to import master data.", timeToImport));
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        logger.log(Level.INFO, "Couldn't close input stream", e);
                    }
                }
            }
        };
        executor.execute(masterDataImportTask);
        return importOperationId;
    }
    
    private URL createUrl(URL base, String pathWithLeadingSlash, String query) throws Exception {
        URL url;
        if (query != null) {
            url = new URL(base.toExternalForm() + pathWithLeadingSlash + "?" + query);
        } else {
            url = new URL(base.toExternalForm() + pathWithLeadingSlash);
        }
        return url;
    }

    public DataImportProgress getImportOperationProgress(UUID id) {
        return getService().getDataImportLock().getProgress(id);
    }

    @Override
    public Integer getStructureImportOperationProgress() {
        return 0;
    }

    private String createLeaderboardQuery(String[] groupNames, boolean compress, boolean exportWind, boolean exportDeviceConfigurations)
            throws UnsupportedEncodingException {
        StringBuffer queryStringBuffer = new StringBuffer("");
        for (int i = 0; i < groupNames.length; i++) {
            String encodedGroupName = URLEncoder.encode(groupNames[i], "UTF-8");
            queryStringBuffer.append("names[]=" + encodedGroupName + "&");
        }
        queryStringBuffer.append(String.format("compress=%s&exportWind=%s&exportDeviceConfigs=%s", compress,
                exportWind, exportDeviceConfigurations));
        return queryStringBuffer.toString();
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitors(boolean filterCompetitorsWithBoat,
            boolean filterCompetitorsWithoutBoat) {
        final List<CompetitorDTO> result = new ArrayList<>();
        CompetitorAndBoatStore competitorStore = getService().getBaseDomainFactory().getCompetitorAndBoatStore();
        if (filterCompetitorsWithBoat == false && filterCompetitorsWithoutBoat == false) {
            getSecurityService().filterObjectsWithPermissionForCurrentUser(SecuredDomainType.COMPETITOR,
                    SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC, competitorStore.getAllCompetitors(),
                    boat -> boat.getIdentifier().toString(),
                    filteredObject -> result.add(convertToCompetitorDTO(filteredObject)));
        } else if (filterCompetitorsWithBoat == true && filterCompetitorsWithoutBoat == false) {
            getSecurityService().filterObjectsWithPermissionForCurrentUser(SecuredDomainType.COMPETITOR,
                    SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC, competitorStore.getCompetitorsWithoutBoat(),
                    boat -> boat.getIdentifier().toString(),
                    filteredObject -> result.add(convertToCompetitorDTO(filteredObject)));
        } else if (filterCompetitorsWithBoat == false && filterCompetitorsWithoutBoat == true) {
            getSecurityService().filterObjectsWithPermissionForCurrentUser(SecuredDomainType.COMPETITOR,
                    SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC, competitorStore.getCompetitorsWithBoat(),
                    boat -> boat.getIdentifier().toString(),
                    filteredObject -> result.add(convertToCompetitorDTO(filteredObject)));
        }
        return result;
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        return convertToCompetitorDTOs(leaderboard.getAllCompetitors());
    }

    @Override
    public Map<? extends CompetitorDTO, BoatDTO> getCompetitorsAndBoatsOfRace(String leaderboardName, String raceColumnName, String fleetName) {
        Map<Competitor, Boat> competitorsAndBoats = getService().getCompetitorToBoatMappingsForRace(leaderboardName, raceColumnName, fleetName);
        return baseDomainFactory.convertToCompetitorAndBoatDTOs(competitorsAndBoats);
    }
    
    @Override
    public List<CompetitorDTO> addOrUpdateCompetitors(List<CompetitorDTO> competitors) throws URISyntaxException {
        final List<CompetitorDTO> result = new ArrayList<>();
        for (final CompetitorDTO competitor : competitors) {
            if (competitor.hasBoat()) {
                result.add(addOrUpdateCompetitorWithBoat((CompetitorWithBoatDTO) competitor));
            } else {
                result.add(addOrUpdateCompetitorWithoutBoat(competitor));
            }
        }
        return result;
    }

    private CompetitorWithBoat addOrUpdateCompetitorWithBoatInternal(CompetitorWithBoatDTO competitor) throws URISyntaxException {
        final CompetitorWithBoat result;
        CompetitorWithBoat existingCompetitor = getService().getCompetitorAndBoatStore().getExistingCompetitorWithBoatByIdAsString(competitor.getIdAsString());
        Nationality nationality = (competitor.getThreeLetterIocCountryCode() == null || competitor.getThreeLetterIocCountryCode().isEmpty()) ? null :
            getBaseDomainFactory().getOrCreateNationality(competitor.getThreeLetterIocCountryCode());
        if (competitor.getIdAsString() == null || competitor.getIdAsString().isEmpty() || existingCompetitor == null) {
            // new competitor
            UUID competitorUUID = UUID.randomUUID();
            DynamicPerson sailor = new PersonImpl(competitor.getName(), nationality, null, null);
            DynamicTeam team = new TeamImpl(competitor.getName() + " team", Collections.singleton(sailor), null);
            // new boat
            DynamicBoat boat = (DynamicBoat) addOrUpdateBoatInternal(competitor.getBoat());
            final UUID competitorUuid = UUID.randomUUID();
            result = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.COMPETITOR, competitorUuid.toString(), competitor.getName(),
                    new ActionWithResult<CompetitorWithBoat>() {
                        @Override
                        public CompetitorWithBoat run() throws Exception {
                            return getBaseDomainFactory().getCompetitorAndBoatStore().getOrCreateCompetitorWithBoat(
                                    competitorUUID, competitor.getName(), competitor.getShortName(),
                                    competitor.getColor(), competitor.getEmail(),
                                    competitor.getFlagImageURL() == null ? null : new URI(competitor.getFlagImageURL()),
                                    team, competitor.getTimeOnTimeFactor(),
                                    competitor.getTimeOnDistanceAllowancePerNauticalMile(), competitor.getSearchTag(),
                                    boat);
                        }
                    });
        } else {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.COMPETITOR.getStringPermissionForObjects(DefaultActions.UPDATE, competitor.getIdAsString()));
            Competitor updatedCompetitor  = getService().apply(
                            new UpdateCompetitor(competitor.getIdAsString(), competitor.getName(), competitor.getShortName(),
                                    competitor.getColor(), competitor.getEmail(), nationality,
                                    competitor.getImageURL() == null ? null : new URI(competitor.getImageURL()),
                                    competitor.getFlagImageURL() == null ? null : new URI(competitor.getFlagImageURL()),
                                    competitor.getTimeOnTimeFactor(),
                                    competitor.getTimeOnDistanceAllowancePerNauticalMile(), 
                                    competitor.getSearchTag()));
            DynamicBoat updatedBoat = (DynamicBoat) addOrUpdateBoatInternal(competitor.getBoat());
            result = new CompetitorWithBoatImpl(updatedCompetitor, updatedBoat);
        }
        return result;
    }

    
    private Competitor addOrUpdateCompetitorWithoutBoatInternal(CompetitorDTO competitor) throws URISyntaxException {
        final Competitor result;
        Competitor existingCompetitor = getService().getCompetitorAndBoatStore()
                .getExistingCompetitorByIdAsString(competitor.getIdAsString());
        Nationality nationality = (competitor.getThreeLetterIocCountryCode() == null
                || competitor.getThreeLetterIocCountryCode().isEmpty()) ? null
                        : getBaseDomainFactory().getOrCreateNationality(competitor.getThreeLetterIocCountryCode());
        if (competitor.getIdAsString() == null || competitor.getIdAsString().isEmpty() || existingCompetitor == null) {
            // new competitor
            UUID competitorUUID = UUID.randomUUID();
            DynamicPerson sailor = new PersonImpl(competitor.getName(), nationality, null, null);
            DynamicTeam team = new TeamImpl(competitor.getName() + " team", Collections.singleton(sailor), null);
            result = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.COMPETITOR, competitorUUID.toString(), competitor.getName(),
                    new ActionWithResult<Competitor>() {

                        @Override
                        public Competitor run() throws Exception {
                            return getBaseDomainFactory().getOrCreateCompetitor(competitorUUID, competitor.getName(),
                                    competitor.getShortName(), competitor.getColor(), competitor.getEmail(),
                                    competitor.getFlagImageURL() == null ? null : new URI(competitor.getFlagImageURL()),
                                    team, competitor.getTimeOnTimeFactor(),
                                    competitor.getTimeOnDistanceAllowancePerNauticalMile(), competitor.getSearchTag());
                        }
                    });
        } else {
            SecurityUtils.getSubject().checkPermission(SecuredDomainType.COMPETITOR
                    .getStringPermissionForObjects(DefaultActions.UPDATE, competitor.getIdAsString()));
            result = getService().apply(new UpdateCompetitor(competitor.getIdAsString(), competitor.getName(),
                    competitor.getShortName(), competitor.getColor(), competitor.getEmail(), nationality,
                    competitor.getImageURL() == null ? null : new URI(competitor.getImageURL()),
                    competitor.getFlagImageURL() == null ? null : new URI(competitor.getFlagImageURL()),
                    competitor.getTimeOnTimeFactor(), competitor.getTimeOnDistanceAllowancePerNauticalMile(),
                    competitor.getSearchTag()));
        }
        return result;
    }

    @Override
    public CompetitorDTO addOrUpdateCompetitorWithoutBoat(CompetitorDTO competitorDTO) throws URISyntaxException {
        Competitor competitor = addOrUpdateCompetitorWithoutBoatInternal(competitorDTO);
        return getBaseDomainFactory().convertToCompetitorDTO(competitor);
    }
    
    @Override
    public CompetitorWithBoatDTO addOrUpdateCompetitorWithBoat(CompetitorWithBoatDTO competitorDTO) throws URISyntaxException {
        CompetitorWithBoat competitor = addOrUpdateCompetitorWithBoatInternal(competitorDTO);
        return getBaseDomainFactory().convertToCompetitorWithBoatDTO(competitor);
    }

    @Override
    public List<CompetitorWithBoatDTO> addCompetitors(List<CompetitorDescriptor> competitorDescriptors, String searchTag) throws URISyntaxException {
        List<DynamicCompetitorWithBoat> competitorsForSaving = new ArrayList<>();
        for (final CompetitorDescriptor competitorDescriptor : competitorDescriptors) {
            competitorsForSaving.add(getService().convertCompetitorDescriptorToCompetitorWithBoat(competitorDescriptor, searchTag));
        }
        getBaseDomainFactory().getCompetitorAndBoatStore().addNewCompetitorsWithBoat(competitorsForSaving);
        return convertToCompetitorWithBoatDTOs(competitorsForSaving);
    }

    @Override
    public void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors) {
        List<String> competitorIdsAsStrings = new ArrayList<String>();
        for (CompetitorDTO competitor : competitors) {
            competitorIdsAsStrings.add(competitor.getIdAsString());
        }
        getService().apply(new AllowCompetitorResetToDefaults(competitorIdsAsStrings));
    }

    @Override
    public Iterable<BoatDTO> getAllBoats() {
        List<BoatDTO> result = new ArrayList<>();
        getSecurityService().filterObjectsWithPermissionForCurrentUser(SecuredDomainType.BOAT,
                SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC,
                getService().getBaseDomainFactory().getCompetitorAndBoatStore().getBoats(),
                boat -> boat.getIdentifier().toString(),
                filteredObject -> result.add(convertToBoatDTO(filteredObject)));
        return result;
    }

    @Override
    public Iterable<BoatDTO> getStandaloneBoats() {
        List<BoatDTO> result = new ArrayList<>();
        getSecurityService().filterObjectsWithPermissionForCurrentUser(SecuredDomainType.BOAT,
                SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC,
                getService().getBaseDomainFactory().getCompetitorAndBoatStore().getStandaloneBoats(),
                boat -> boat.getIdentifier().toString(),
                filteredObject -> result.add(convertToBoatDTO(filteredObject)));
        return result;
    }

    @Override
    public BoatDTO addOrUpdateBoat(BoatDTO boat) {
        return getBaseDomainFactory().convertToBoatDTO(addOrUpdateBoatInternal(boat));
    }

    private Boat addOrUpdateBoatInternal(BoatDTO boat) {
        Boat existingBoat = getService().getCompetitorAndBoatStore().getExistingBoatByIdAsString(boat.getIdAsString());
        final Boat result;
        if (boat.getIdAsString() == null || boat.getIdAsString().isEmpty() || existingBoat == null) {
            // new boat
            UUID boatUUID = UUID.randomUUID();
            BoatClass boatClass = getBaseDomainFactory().getOrCreateBoatClass(boat.getBoatClass().getName());
            result = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.BOAT, boatUUID.toString(), boat.getName(), new ActionWithResult<Boat>() {

                        @Override
                        public Boat run() throws Exception {
                            return getBaseDomainFactory().getOrCreateBoat(boatUUID, boat.getName(), boatClass,
                                    boat.getSailId(), boat.getColor());
                        }
                    });
        } else {
            SecurityUtils.getSubject().checkPermission(
                    SecuredDomainType.BOAT.getStringPermissionForObjects(DefaultActions.UPDATE, boat.getIdAsString()));
            result = getService()
                    .apply(new UpdateBoat(boat.getIdAsString(), boat.getName(), boat.getColor(), boat.getSailId()));
        }
        return result;
    }

    @Override
    public void allowBoatResetToDefaults(Iterable<BoatDTO> boats) {
        List<String> boatIdsAsStrings = new ArrayList<String>();
        for (BoatDTO boat : boats) {
            boatIdsAsStrings.add(boat.getIdAsString());
        }
        getService().apply(new AllowBoatResetToDefaults(boatIdsAsStrings));
    }

    @Override
    public boolean linkBoatToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString, String boatIdAsString) {
        boolean result = false;
        Boat existingBoat = getService().getCompetitorAndBoatStore().getExistingBoatByIdAsString(boatIdAsString);
        Competitor existingCompetitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(competitorIdAsString);
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null && existingCompetitor != null && existingBoat != null) {
            raceLog.add(new RaceLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), 
                    getService().getServerAuthor(), raceLog.getCurrentPassId(), existingCompetitor, existingBoat));
            result = true;
        }        
        return result;
    }

    @Override
    public boolean unlinkBoatFromCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString) {
        boolean result = false;
        Competitor existingCompetitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(competitorIdAsString);
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null && existingCompetitor != null) {
            List<RaceLogRegisterCompetitorEvent> linkEventsToRevoke = new ArrayList<>();
            for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
                if (event instanceof RaceLogRegisterCompetitorEvent) {
                    linkEventsToRevoke.add((RaceLogRegisterCompetitorEvent) event);
                }
            }
            try {
                for (RaceLogRegisterCompetitorEvent eventToRevoke : linkEventsToRevoke) {
                    raceLog.revokeEvent(getService().getServerAuthor(), eventToRevoke, "unlink competitor from boat");
                }
            } catch (NotRevokableException e) {
                logger.log(Level.WARNING, "Could not unlink competitor from boat by adding RevokeEvent", e);
            }

            result = true;
        }        
        return result;
    }

    @Override
    public BoatDTO getBoatLinkedToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString) {
        BoatDTO result = null;
        Competitor existingCompetitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(competitorIdAsString);
        Map<Competitor, Boat> competitorToBoatMappingsForRace = getService().getCompetitorToBoatMappingsForRace(leaderboardName, raceColumnName, fleetName);
        if (existingCompetitor != null) {
            Boat boatOfCompetitor = competitorToBoatMappingsForRace.get(existingCompetitor);
            if (boatOfCompetitor != null) {
                result = baseDomainFactory.convertToBoatDTO(boatOfCompetitor);
            }
        }
        return result;
    }

    @Override
    public List<DeviceConfigurationMatcherDTO> getDeviceConfigurationMatchers() {
        List<DeviceConfigurationMatcherDTO> configs = new ArrayList<DeviceConfigurationMatcherDTO>();
        for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : 
            getService().getAllDeviceConfigurations().entrySet()) {
            DeviceConfigurationMatcher matcher = entry.getKey();
            configs.add(convertToDeviceConfigurationMatcherDTO(matcher));
        }
        return configs;
    }

    @Override
    public DeviceConfigurationDTO getDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDto) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDto.clients);
        DeviceConfiguration configuration = getService().getAllDeviceConfigurations().get(matcher);
        if (configuration == null) {
            return null;
        } else {
            return convertToDeviceConfigurationDTO(configuration);
        }
    }

    @Override
    public DeviceConfigurationMatcherDTO createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDTO.clients);
        DeviceConfiguration configuration = convertToDeviceConfiguration(configurationDTO);
        getService().createOrUpdateDeviceConfiguration(matcher, configuration);
        return convertToDeviceConfigurationMatcherDTO(matcher);
    }

    @Override
    public boolean removeDeviceConfiguration(List<String> clientIds) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(clientIds);
        getService().removeDeviceConfiguration(matcher);
        return true;
    }

    private DeviceConfigurationMatcherDTO convertToDeviceConfigurationMatcherDTO(DeviceConfigurationMatcher matcher) {
        List<String> clients = new ArrayList<String>();
        if (matcher instanceof DeviceConfigurationMatcherSingle) {
            clients.add(((DeviceConfigurationMatcherSingle)matcher).getClientIdentifier());
        }
        DeviceConfigurationMatcherDTO dto = new DeviceConfigurationMatcherDTO(
                clients);
        return dto;
    }

    private DeviceConfigurationMatcher convertToDeviceConfigurationMatcher(List<String> clientIds) {
        return baseDomainFactory.getOrCreateDeviceConfigurationMatcher(clientIds);
    }

    private DeviceConfigurationDTO convertToDeviceConfigurationDTO(DeviceConfiguration configuration) {
        DeviceConfigurationDTO dto = new DeviceConfigurationDTO();
        dto.allowedCourseAreaNames = configuration.getAllowedCourseAreaNames();
        dto.resultsMailRecipient = configuration.getResultsMailRecipient();
        dto.byNameDesignerCourseNames = configuration.getByNameCourseDesignerCourseNames();
        if (configuration.getRegattaConfiguration() != null) {
            dto.regattaConfiguration = convertToRegattaConfigurationDTO(configuration.getRegattaConfiguration());
        }
        return dto;
    }

    private DeviceConfigurationDTO.RegattaConfigurationDTO convertToRegattaConfigurationDTO(
            RegattaConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        DeviceConfigurationDTO.RegattaConfigurationDTO dto = new DeviceConfigurationDTO.RegattaConfigurationDTO();
        
        dto.defaultRacingProcedureType = configuration.getDefaultRacingProcedureType();
        dto.defaultCourseDesignerMode = configuration.getDefaultCourseDesignerMode();
        dto.defaultProtestTimeDuration = configuration.getDefaultProtestTimeDuration();
        
        if (configuration.getRRS26Configuration() != null) {
            dto.rrs26Configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RRS26ConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getRRS26Configuration(), dto.rrs26Configuration);
            copyRacingProcedureWithConfigurableStartModeFlagProperties(configuration.getRRS26Configuration(), dto.rrs26Configuration);
        }
        if (configuration.getSWCStartConfiguration() != null) {
            dto.swcStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.SWCStartConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getSWCStartConfiguration(), dto.swcStartConfiguration);
            copyRacingProcedureWithConfigurableStartModeFlagProperties(configuration.getSWCStartConfiguration(), dto.swcStartConfiguration);
        }
        if (configuration.getGateStartConfiguration() != null) {
            dto.gateStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.GateStartConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getGateStartConfiguration(), dto.gateStartConfiguration);
            dto.gateStartConfiguration.hasPathfinder = configuration.getGateStartConfiguration().hasPathfinder();
            dto.gateStartConfiguration.hasAdditionalGolfDownTime = configuration.getGateStartConfiguration().hasAdditionalGolfDownTime();
        }
        if (configuration.getESSConfiguration() != null) {
            dto.essConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.ESSConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getESSConfiguration(), dto.essConfiguration);
        }
        if (configuration.getBasicConfiguration() != null) {
            dto.basicConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getBasicConfiguration(), dto.basicConfiguration);
        }
        if (configuration.getLeagueConfiguration() != null) {
            dto.leagueConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.LeagueConfigurationDTO();
            copyBasicRacingProcedureProperties(configuration.getLeagueConfiguration(), dto.leagueConfiguration);
        }
        return dto;
    }
    
    private void copyBasicRacingProcedureProperties(RacingProcedureConfiguration configuration, final RacingProcedureConfigurationDTO racingProcedureConfigurationDTO) {
        racingProcedureConfigurationDTO.classFlag = configuration.getClassFlag();
        racingProcedureConfigurationDTO.hasIndividualRecall = configuration.hasIndividualRecall();
        racingProcedureConfigurationDTO.isResultEntryEnabled = configuration.isResultEntryEnabled();
    }

    private void copyRacingProcedureWithConfigurableStartModeFlagProperties(ConfigurableStartModeFlagRacingProcedureConfiguration configuration, final RacingProcedureWithConfigurableStartModeFlagConfigurationDTO racingProcedureConfigurationDTO) {
        racingProcedureConfigurationDTO.startModeFlags = configuration.getStartModeFlags();
    }

    private DeviceConfigurationImpl convertToDeviceConfiguration(DeviceConfigurationDTO dto) {
        DeviceConfigurationImpl configuration = new DeviceConfigurationImpl(convertToRegattaConfiguration(dto.regattaConfiguration));
        configuration.setAllowedCourseAreaNames(dto.allowedCourseAreaNames);
        configuration.setResultsMailRecipient(dto.resultsMailRecipient);
        configuration.setByNameDesignerCourseNames(dto.byNameDesignerCourseNames);
        return configuration;
    }

    private RegattaConfiguration convertToRegattaConfiguration(RegattaConfigurationDTO dto) {
        if (dto == null) {
            return null;
        }
        RegattaConfigurationImpl configuration = new RegattaConfigurationImpl();
        configuration.setDefaultRacingProcedureType(dto.defaultRacingProcedureType);
        configuration.setDefaultCourseDesignerMode(dto.defaultCourseDesignerMode);
        configuration.setDefaultProtestTimeDuration(dto.defaultProtestTimeDuration);
        if (dto.rrs26Configuration != null) {
            RRS26ConfigurationImpl config = new RRS26ConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.rrs26Configuration, config);
            applyRacingProcedureWithConfigurableStartModeFlagConfigProperties(dto.rrs26Configuration, config);
            configuration.setRRS26Configuration(config);
        }
        if (dto.swcStartConfiguration != null) {
            SWCStartConfigurationImpl config = new SWCStartConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.swcStartConfiguration, config);
            applyRacingProcedureWithConfigurableStartModeFlagConfigProperties(dto.swcStartConfiguration, config);
            configuration.setSWCStartConfiguration(config);
        }
        if (dto.gateStartConfiguration != null) {
            GateStartConfigurationImpl config = new GateStartConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.gateStartConfiguration, config);
            config.setHasPathfinder(dto.gateStartConfiguration.hasPathfinder);
            config.setHasAdditionalGolfDownTime(dto.gateStartConfiguration.hasAdditionalGolfDownTime);
            configuration.setGateStartConfiguration(config);
        }
        if (dto.essConfiguration != null) {
            ESSConfigurationImpl config = new ESSConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.essConfiguration, config);
            configuration.setESSConfiguration(config);
        }
        if (dto.basicConfiguration != null) {
            RacingProcedureConfigurationImpl config = new RacingProcedureConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.basicConfiguration, config);
            configuration.setBasicConfiguration(config);
        }
        if (dto.leagueConfiguration != null) {
            LeagueConfigurationImpl config = new LeagueConfigurationImpl();
            applyGeneralRacingProcedureConfigProperties(dto.leagueConfiguration, config);
            configuration.setLeagueConfiguration(config);
        }
        return configuration;
    }

    private void applyGeneralRacingProcedureConfigProperties(RacingProcedureConfigurationDTO racingProcedureConfigurationDTO,
            RacingProcedureConfigurationImpl config) {
        config.setClassFlag(racingProcedureConfigurationDTO.classFlag);
        config.setHasIndividualRecall(racingProcedureConfigurationDTO.hasIndividualRecall);
        config.setResultEntryEnabled(racingProcedureConfigurationDTO.isResultEntryEnabled);
    }

    private void applyRacingProcedureWithConfigurableStartModeFlagConfigProperties(
            RacingProcedureWithConfigurableStartModeFlagConfigurationDTO racingProcedureConfigurationDTO,
            RacingProcedureWithConfigurableStartModeFlagConfigurationImpl config) {
        config.setStartModeFlags(racingProcedureConfigurationDTO.startModeFlags);
    }
    
    @Override
    public boolean setStartTimeAndProcedure(RaceLogSetStartTimeAndProcedureDTO dto) {
        TimePoint newStartTime = getService().setStartTimeAndProcedure(dto.leaderboardName, dto.raceColumnName, 
                dto.fleetName, dto.authorName, dto.authorPriority,
                dto.passId, new MillisecondsTimePoint(dto.logicalTimePoint), new MillisecondsTimePoint(dto.startTime),
                dto.racingProcedure);
        return new MillisecondsTimePoint(dto.startTime).equals(newStartTime);
    }
    
    @Override
    public Pair<Boolean, Boolean> setFinishingAndEndTime(RaceLogSetFinishingAndFinishTimeDTO dto) {
        TimePoint newFinsihingTime = getService().setFinishingTime(dto.leaderboardName, dto.raceColumnName, 
                dto.fleetName, dto.authorName, dto.authorPriority,
                dto.passId, new MillisecondsTimePoint(dto.finishingTime));
        
        TimePoint newEndTime = getService().setEndTime(dto.leaderboardName, dto.raceColumnName, 
                dto.fleetName, dto.authorName, dto.authorPriority,
                dto.passId, new MillisecondsTimePoint(dto.finishTime));
        
        return new Pair<Boolean, Boolean>(new MillisecondsTimePoint(dto.finishingTime).equals(newFinsihingTime),
                new MillisecondsTimePoint(dto.finishTime).equals(newEndTime));
    }

    @Override
    public void setTrackingTimes(RaceLogSetTrackingTimesDTO dto) throws NotFoundException {
        RaceLog raceLog = getRaceLog(dto.leaderboardName, dto.raceColumnName, dto.fleetName);
        // the tracking start/end time events are not revoked; updates with null as TimePoint may be added instead
        final LogEventAuthorImpl author = new LogEventAuthorImpl(dto.authorName, dto.authorPriority);
        if (!Util.equalsWithNull(dto.newStartOfTracking, dto.currentStartOfTracking)) {
            if (dto.newStartOfTracking != null) {
                raceLog.add(new RaceLogStartOfTrackingEventImpl(dto.newStartOfTracking.getTimePoint(),
                        author, raceLog.getCurrentPassId()));
            } else {
                final Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent> trackingTimesEvents = new TrackingTimesEventFinder(raceLog).analyze();
                // we assume to find a valid "set start of tracking time" event that matches the old start of tracking time;
                // if the time doesn't match dto.currentStartOfTracking, the revocation is rejected with an exception;
                // the result of trying to revoke is returned otherwise, and it may not be the result intended in case
                // the author's priority was lower than that of the author of the event that is to be revoked.
                if (trackingTimesEvents == null || trackingTimesEvents.getA() == null ||
                        !Util.equalsWithNull(trackingTimesEvents.getA().getLogicalTimePoint(), dto.currentStartOfTracking==null?null:dto.currentStartOfTracking.getTimePoint())) {
                    throw new NotFoundException("Old start of tracking time in the race log ("+
                        (trackingTimesEvents==null||trackingTimesEvents.getA()==null?"unset":trackingTimesEvents.getA().getLogicalTimePoint())+
                        ") does not match start of tracking time at transaction start ("+dto.currentStartOfTracking==null?null:dto.currentStartOfTracking.getTimePoint()+")");
                } else {
                    try {
                        raceLog.revokeEvent(author, trackingTimesEvents.getA(), "resetting tracking start time");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "Internal error: event "+trackingTimesEvents.getA()+" was expected to be revokable", e);
                    }
                }
            }
        }
        if (!Util.equalsWithNull(dto.newEndOfTracking, dto.currentEndOfTracking)) {
            if (dto.newEndOfTracking != null) {
                raceLog.add(new RaceLogEndOfTrackingEventImpl(
                        dto.newEndOfTracking.getTimePoint(), author,
                        raceLog.getCurrentPassId()));
            } else {
                final Pair<RaceLogStartOfTrackingEvent, RaceLogEndOfTrackingEvent> trackingTimesEvents = new TrackingTimesEventFinder(raceLog).analyze();
                // we assume to find a valid "set start of tracking time" event that matches the old start of tracking time;
                // if the time doesn't match dto.currentStartOfTracking, the revocation is rejected with an exception;
                // the result of trying to revoke is returned otherwise, and it may not be the result intended in case
                // the author's priority was lower than that of the author of the event that is to be revoked.
                if (trackingTimesEvents == null || trackingTimesEvents.getB() == null ||
                        !Util.equalsWithNull(trackingTimesEvents.getB().getLogicalTimePoint(), dto.currentEndOfTracking==null?null:dto.currentEndOfTracking.getTimePoint())) {
                    throw new NotFoundException("Old end of tracking time in the race log ("+
                        (trackingTimesEvents==null||trackingTimesEvents.getB()==null?"unset":trackingTimesEvents.getB().getLogicalTimePoint())+
                        ") does not match end of tracking time at transaction start ("+dto.currentEndOfTracking==null?null:dto.currentEndOfTracking.getTimePoint()+")");
                } else {
                    try {
                        raceLog.revokeEvent(author, trackingTimesEvents.getB(), "resetting tracking end time");
                    } catch (NotRevokableException e) {
                        logger.log(Level.WARNING, "Internal error: event "+trackingTimesEvents.getB()+" was expected to be revokable", e);
                    }
                }
            }
        }
    }

    @Override
    public Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> getTrackingTimes(String leaderboardName, String raceColumnName, String fleetName) throws NotFoundException {
        final Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> times;
        final RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        if (raceLog != null) {
            times = new TrackingTimesFinder(raceLog).analyze();
        } else {
            times = null;
        }
        return times;
    }

    @Override
    public com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName) {
        com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType> result = getService().getStartTimeAndProcedure(leaderboardName, raceColumnName, fleetName);
        if (result == null || result.getA() == null) {
            return null;
        }
        return new com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType>(result.getA() == null ? null : result.getA().asDate(), result.getB(), result.getC());
    }
    

    @Override
    public com.sap.sse.common.Util.Triple<Date, Date, Integer> getFinishingAndFinishTime(String leaderboardName, String raceColumnName, String fleetName) {
        com.sap.sse.common.Util.Triple<TimePoint, TimePoint, Integer> result = getService().getFinishingAndFinishTime(leaderboardName, raceColumnName, fleetName);
        if (result == null) {
            return null;
        }
        return new com.sap.sse.common.Util.Triple<>(result.getA() == null ? null : result.getA().asDate(),
                result.getB() == null ? null : result.getB().asDate(), result.getC());
    }

    @Override
    public Iterable<String> getAllIgtimiAccountEmailAddresses() {
        List<String> result = new ArrayList<String>();
        for (Account account : getIgtimiConnectionFactory().getAllAccounts()) {
            final String email = account.getUser().getEmail();
            if (SecurityUtils.getSubject().isPermitted(SecuredDomainType.IGTIMI_ACCOUNT.getStringPermissionForObjects(DefaultActions.READ, email))) {
                result.add(email);
            }
        }
        return result;
    }

    private IgtimiConnectionFactory getIgtimiConnectionFactory() {
        return igtimiAdapterTracker.getService();
    }
    
    protected RaceLogTrackingAdapterFactory getRaceLogTrackingAdapterFactory() {
        return raceLogTrackingAdapterTracker.getService();
    }

    protected RaceLogTrackingAdapter getRaceLogTrackingAdapter() {
        return getRaceLogTrackingAdapterFactory().getAdapter(getBaseDomainFactory());
    }

    @Override
    public String getIgtimiAuthorizationUrl(String redirectProtocol, String redirectHostname, String redirectPort) throws MalformedURLException, UnsupportedEncodingException {
        return getIgtimiConnectionFactory().getAuthorizationUrl(redirectProtocol, redirectHostname, redirectPort);
    }

    @Override
    public boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws Exception {
        final Account existingAccount = getIgtimiConnectionFactory().getExistingAccountByEmail(eMailAddress);
        final Account account;
        if (existingAccount == null) {
            account = getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                    SecuredDomainType.IGTIMI_ACCOUNT, eMailAddress,
                    eMailAddress,
                    () -> getIgtimiConnectionFactory()
                            .createAccountToAccessUserData(eMailAddress, password));
        } else {
            logger.warning("Igtimi account "+eMailAddress+" already exists.");
            account = null; // account with that e-mail already exists
        }
        return account != null;
    }

    @Override
    public void removeIgtimiAccount(String eMailOfAccountToRemove) {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.IGTIMI_ACCOUNT.getStringPermissionForObjects(
                DefaultActions.DELETE, eMailOfAccountToRemove));
        getIgtimiConnectionFactory().removeAccount(eMailOfAccountToRemove);
    }

    @Override
    public Map<RegattaAndRaceIdentifier, Integer> importWindFromIgtimi(List<RaceDTO> selectedRaces, boolean correctByDeclination) throws IllegalStateException,
            ClientProtocolException, IOException, org.json.simple.parser.ParseException {
        final IgtimiConnectionFactory igtimiConnectionFactory = getIgtimiConnectionFactory();
        final Iterable<DynamicTrackedRace> trackedRaces;
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            List<DynamicTrackedRace> myTrackedRaces = new ArrayList<DynamicTrackedRace>();
            trackedRaces = myTrackedRaces;
            for (RaceDTO raceDTO : selectedRaces) {
                DynamicTrackedRace trackedRace = getTrackedRace(raceDTO.getRaceIdentifier());
                myTrackedRaces.add(trackedRace);
            }
        } else {
            trackedRaces = getAllTrackedRaces();
        }
        Map<RegattaAndRaceIdentifier, Integer> numberOfWindFixesImportedPerRace = new HashMap<RegattaAndRaceIdentifier, Integer>();
        for (Account account : igtimiConnectionFactory.getAllAccounts()) {
            // filter account based on used permissions to read account:
            if (SecurityUtils.getSubject().isPermitted(SecuredDomainType.IGTIMI_ACCOUNT.getStringPermissionForObjects(
                    DefaultActions.READ, account.getUser().getEmail()))) {
                IgtimiConnection conn = igtimiConnectionFactory.connect(account);
                Map<TrackedRace, Integer> resultsForAccounts = conn.importWindIntoRace(trackedRaces, correctByDeclination);
                for (Entry<TrackedRace, Integer> resultForAccount : resultsForAccounts.entrySet()) {
                    RegattaAndRaceIdentifier key = resultForAccount.getKey().getRaceIdentifier();
                    Integer i = numberOfWindFixesImportedPerRace.get(key);
                    if (i == null) {
                        i = 0;
                    }
                    numberOfWindFixesImportedPerRace.put(key, i+resultForAccount.getValue());
                }
            }
        }
        for (final TrackedRace trackedRace : trackedRaces) {
        	// update polar sheets:
        	getService().getPolarDataService().insertExistingFixes(trackedRace);
        }
        return numberOfWindFixesImportedPerRace;
    }

    private Set<DynamicTrackedRace> getAllTrackedRaces() {
        Set<DynamicTrackedRace> result = new HashSet<DynamicTrackedRace>();
        Iterable<Regatta> allRegattas = getService().getAllRegattas();
        for (Regatta regatta : allRegattas) {
            DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                trackedRegatta.lockTrackedRacesForRead();
                try {
                    Iterable<DynamicTrackedRace> trackedRaces = trackedRegatta.getTrackedRaces();
                    for (TrackedRace trackedRace : trackedRaces) {
                        result.add((DynamicTrackedRace) trackedRace);
                    }
                } finally {
                    trackedRegatta.unlockTrackedRacesAfterRead();
                }
            }
        }
        return result;
    }

    private class TimeoutExtendingInputStream extends FilterInputStream {

    	// default timeout is high to ensure that long running client operations
    	// such as compressing data will not have the server run into a timeout.
    	// this especially applies to foiling data where compression on slower machines
    	// can take up to two hours.
    	private static final int DEFAULT_TIMEOUT_IN_SECONDS = 60*60*2;

        private final URLConnection connection;

        protected TimeoutExtendingInputStream(InputStream in, URLConnection connection) {
            super(in);
            this.connection = connection;
        }

        @Override
        public int read() throws IOException {
            connection.setReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS*1000);
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            connection.setReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS*1000);
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            connection.setReadTimeout(DEFAULT_TIMEOUT_IN_SECONDS*1000);
            return super.read(b, off, len);
        }

    }

    @Override
    public Boolean denoteForRaceLogTracking(String leaderboardName,
    		String raceColumnName, String fleetName) throws NotFoundException, NotDenotableForRaceLogTrackingException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
    	RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
    	Fleet fleet = getFleetByName(raceColumn, fleetName);
    	return getRaceLogTrackingAdapter().denoteRaceForRaceLogTracking(getService(), leaderboard, raceColumn, fleet, null);
    }
    
    @Override
    public void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName) throws NotFoundException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        getRaceLogTrackingAdapter().removeDenotationForRaceLogTracking(getService(), raceLog);
    }
    
    @Override
    public void denoteForRaceLogTracking(String leaderboardName) throws Exception {
        denoteForRaceLogTracking(leaderboardName, /* race name prefix */ null);
    }
    
    @Override
    public void denoteForRaceLogTracking(String leaderboardName, String prefix) throws Exception {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);        
        getRaceLogTrackingAdapter().denoteAllRacesForRaceLogTracking(getService(), leaderboard, prefix);
    }
    
    /**
     * @param triple
     *            leaderboard and racecolumn and fleet names
     */
    private RaceLog getRaceLog(com.sap.sse.common.Util.Triple<String, String, String> triple) throws NotFoundException {
        return getRaceLog(triple.getA(), triple.getB(), triple.getC());
    }
    
    private RegattaLog getRegattaLogInternal(String leaderboardName) throws DoesNotHaveRegattaLogException {
        Leaderboard l = getService().getLeaderboardByName(leaderboardName);
        if (! (l instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        return ((HasRegattaLike) l).getRegattaLike().getRegattaLog();
    }
    
    private RaceLog getRaceLog(String leaderboardName, String raceColumnName, String fleetName) throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        return raceColumn.getRaceLog(fleet);
    }

    private Competitor getCompetitor(CompetitorDTO dto) {
        return getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(dto.getIdAsString());
    }
    
    private CompetitorWithBoat getCompetitor(CompetitorWithBoatDTO dto) {
        return getService().getCompetitorAndBoatStore().getExistingCompetitorWithBoatByIdAsString(dto.getIdAsString());
    }

    private Boat getBoat(BoatDTO dto) {
        return getService().getCompetitorAndBoatStore().getExistingBoatByIdAsString(dto.getIdAsString());
    }

    @Override
    public void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Map<? extends CompetitorDTO, BoatDTO> competitorAndBoatDTOs)
            throws CompetitorRegistrationOnRaceLogDisabledException, NotFoundException {
        Map<Competitor, Boat> competitorToBoatMappingsToRegister = new HashMap<>();
        for (Entry<? extends CompetitorDTO, BoatDTO> competitorAndBoatEntry : competitorAndBoatDTOs.entrySet()) {
            competitorToBoatMappingsToRegister.put(getCompetitor(competitorAndBoatEntry.getKey()), getBoat(competitorAndBoatEntry.getValue()));
        }
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        final Iterable<Competitor> competitorRegistrationsToRemove = filterCompetitorDuplicates(competitorToBoatMappingsToRegister, raceColumn.getCompetitorsRegisteredInRacelog(fleet));
        raceColumn.deregisterCompetitors(competitorRegistrationsToRemove, fleet);
        // we assume that the competitors id of type Competitor here, so we need to find the corresponding boat
        for (final Entry<Competitor, Boat> competitorAndBoatToRegister : competitorToBoatMappingsToRegister.entrySet()) {
            raceColumn.registerCompetitor(competitorAndBoatToRegister.getKey(), competitorAndBoatToRegister.getValue(), fleet);  
        }
    }
    
    @Override
    public void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorWithBoatDTO> competitorDTOs) throws CompetitorRegistrationOnRaceLogDisabledException, NotFoundException {
        Map<CompetitorWithBoat, Boat> competitorsToRegister = new HashMap<>();
        for (CompetitorWithBoatDTO dto : competitorDTOs) {
            competitorsToRegister.put(getCompetitor(dto), getBoat(dto.getBoat()));
        }
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        Map<CompetitorWithBoat, Boat> competitorsRegisteredInRaceLog = new HashMap<>();
        for (final Entry<Competitor, Boat> e : raceColumn.getCompetitorsRegisteredInRacelog(fleet).entrySet()) {
            competitorsRegisteredInRaceLog.put((CompetitorWithBoat) e.getKey(), e.getValue());
        }
        final Iterable<CompetitorWithBoat> competitorSetToRemove = filterCompetitorDuplicates(competitorsToRegister, competitorsRegisteredInRaceLog);
        raceColumn.deregisterCompetitors(competitorSetToRemove, fleet);
        // we assume that the competitors id of type Competitor here, so we need to find the corresponding boat
        for (CompetitorWithBoat competitorToRegister : competitorsToRegister.keySet()) {
            if (competitorToRegister.hasBoat()) {
                raceColumn.registerCompetitor(competitorToRegister, fleet);  
            } else {
                logger.warning("The competitor "+competitorToRegister.getName()+" does not have a boat associated but should have; "+
                        "competitor is not registered for race log of race "+raceColumnName+" in leaderboard "+leaderboardName+" for fleet "+fleetName);
            }
        }
    }
    
    @Override
    public void setCompetitorRegistrationsInRegattaLog(String leaderboardName, Set<? extends CompetitorDTO> competitorDTOs)
            throws DoesNotHaveRegattaLogException, NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (!(leaderboard instanceof HasRegattaLike)){
            throw new DoesNotHaveRegattaLogException();
        }
        Map<Competitor, Boat> competitorsToRegister = new HashMap<>();
        for (CompetitorDTO dto : competitorDTOs) {
            competitorsToRegister.put(getCompetitor(dto), /* boat doesn't matter here */ null);
        }
        HasRegattaLike hasRegattaLike = (HasRegattaLike) leaderboard;
        Map<Competitor, Boat> competitorsAlreadyRegistered = new HashMap<>();
        for (final Competitor c : leaderboard.getAllCompetitors()) {
            competitorsAlreadyRegistered.put(c,  /* boat doesn't matter here */ null);
        }
        final Iterable<Competitor> competitorSetToRemove = filterCompetitorDuplicates(competitorsToRegister, competitorsAlreadyRegistered);
        hasRegattaLike.deregisterCompetitors(competitorSetToRemove);
        hasRegattaLike.registerCompetitors(competitorsToRegister.keySet());
    }

    /**
     * Removes competitors already registered to the same boats (those in {@code competitorsRegistered}) from {@code competitorsToRegister} to avoid
     * registering them again and then returns those competitors that need to be de-registered. Those to de-register includes those registered but
     * to a different boat, and those will be left in the {@code competitorToBoatMappingsToRegister} map.
     * 
     * @param competitorToBoatMappingsToRegister will be modified by removing all competitors in {@code competitorsRegistered}
     * @param competitorToBoatMappingsRegistered the competitors already registered; those will be removed from {@code competitorsToRegister}
     * @return the competitors to de-register because they were in {@code competitorsRegistered} but are not in {@code competitorsToRegister}
     */
    private <CompetitorType extends Competitor> Iterable<CompetitorType> filterCompetitorDuplicates(
            Map<CompetitorType, Boat> competitorToBoatMappingsToRegister,
            Map<CompetitorType, Boat> competitorToBoatMappingsRegistered) {
        final Set<CompetitorType> competitorsToUnregister = new HashSet<>();
        Util.addAll(competitorToBoatMappingsRegistered.keySet(), competitorsToUnregister);
        for (final Entry<CompetitorType, Boat> e : competitorToBoatMappingsRegistered.entrySet()) {
            CompetitorType competitor = e.getKey();
            if (competitorToBoatMappingsToRegister.containsKey(competitor)) { // is competitor to be registered?
                final Boat boatOfCompetitorToRegister = competitorToBoatMappingsToRegister.get(competitor);
                final Boat boatOfRegisteredCompetitor = e.getValue();
                if (boatOfCompetitorToRegister == boatOfRegisteredCompetitor) {
                    // User wants to map competitor to boat, and that mapping already exists; neither add nor remove this registration but leave as is:
                    competitorToBoatMappingsToRegister.remove(competitor);
                    competitorsToUnregister.remove(competitor);
                }
            }
        }
        return competitorsToUnregister;
    }

    private HashSet<Boat> filterBoatDuplicates(Set<Boat> boatsToRegister, HashSet<Boat> boatSetToRemove) {
        for (Iterator<Boat> iterator = boatSetToRemove.iterator(); iterator.hasNext();) {
            Boat boat = iterator.next();
            if (boatsToRegister.remove(boat)) {
                iterator.remove();
            }
        }
        return boatSetToRemove;
    }

    private Mark convertToMark(MarkDTO dto, boolean resolve) {
        Mark result = null;
        if (resolve) {
            Mark existing = baseDomainFactory.getExistingMarkByIdAsString(dto.getIdAsString());
            if (existing != null) {
                result = existing;
            }
        }
        if (result == null) {
            Serializable id = UUID.randomUUID();
            result = baseDomainFactory.getOrCreateMark(id, dto.getName(), dto.type, dto.color, dto.shape, dto.pattern);
        }
        return result;
    }
    
    /**
     * Also finds the last position of the marks if set by pinging them as long as currently a {@link TrackedRace} exists
     * whose tracking interval spans the current time. If at least a non-spanning {@link TrackedRace} can be found in the
     * scope of the <code>leaderboard</code>, the time-wise closest position fix for the mark will be used as its position.
     * If the mark has been pinged through the {@link RegattaLog} but no {@link TrackedRace} exists that has loaded that ping,
     * the ping won't be visible to this API.
     */
    private MarkDTO convertToMarkDTO(LeaderboardThatHasRegattaLike leaderboard, Mark mark) {
        final TimePoint now = MillisecondsTimePoint.now();
        final Position lastPos = getService().getMarkPosition(mark, leaderboard, now);
        return convertToMarkDTO(mark, lastPos);
    }
    
    @Override
    public void addMarkToRegattaLog(String leaderboardName, MarkDTO markDTO) throws DoesNotHaveRegattaLogException {
        Mark mark = convertToMark(markDTO, false);

        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        RegattaLogDefineMarkEventImpl event = new RegattaLogDefineMarkEventImpl(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), MillisecondsTimePoint.now(), UUID.randomUUID(), mark);
        regattaLog.add(event);
    }
    
    @Override
    public void revokeMarkDefinitionEventInRegattaLog(String leaderboardName, MarkDTO markDTO) throws DoesNotHaveRegattaLogException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        final List<RegattaLogEvent> regattaLogDefineMarkEvents = new AllEventsOfTypeFinder<>(regattaLog, /* only unrevoked */ true, RegattaLogDefineMarkEvent.class).analyze();
        RegattaLogDefineMarkEvent eventToRevoke = null;
        for (RegattaLogEvent event : regattaLogDefineMarkEvents) {
            RegattaLogDefineMarkEvent defineMarkEvent = (RegattaLogDefineMarkEvent) event;
            if (defineMarkEvent.getMark().getId().toString().equals(markDTO.getIdAsString())){
                eventToRevoke = defineMarkEvent;
                break;
            }
        }
        regattaLog.revokeDefineMarkEventAndRelatedDeviceMappings(eventToRevoke, getService().getServerAuthor(), logger);
    }

    @Override
    public void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> courseDTO) throws NotFoundException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        String courseName = "Course of " + raceColumnName;
        if (!LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleetName)) {
            courseName += " - " + fleetName; 
        }
        CourseBase lastPublishedCourse = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ false).analyze();
        if (lastPublishedCourse == null) {
            lastPublishedCourse = new CourseDataImpl(courseName);
        }
        
        List<Pair<ControlPoint, PassingInstruction>> controlPoints = new ArrayList<>();
        for (Pair<ControlPointDTO, PassingInstruction> waypointDTO : courseDTO) {
            controlPoints.add(new Pair<>(getOrCreateControlPoint(waypointDTO.getA()), waypointDTO.getB()));
        }
        Course course = new CourseImpl(courseName, lastPublishedCourse.getWaypoints());
        
        try {
            course.update(controlPoints, baseDomainFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RaceLogEvent event = new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), raceLog.getCurrentPassId(), course, CourseDesignerMode.ADMIN_CONSOLE);
        raceLog.add(event);
    }
    
    /**
     * @param trackedRace
     *            if <code>null</code>, no position data will be attached to the {@link MarkDTO}s
     * @param timePoint
     *            if <code>trackedRace</code> is not <code>null</code>, specifies the time point for which to determine
     *            the mark positions and attach to the {@link MarkDTO}s. If <code>null<c/code>, the current time point
     *            will be used as default.
     */
    private WaypointDTO convertToWaypointDTO(Waypoint waypoint, Map<Serializable, ControlPointDTO> controlPointCache, MarkPositionFinder positionFinder, TimePoint timePoint) {
        ControlPointDTO cp = controlPointCache.get(waypoint.getControlPoint().getId());
        if (cp == null) {
            cp = convertToControlPointDTO(waypoint.getControlPoint(), positionFinder, timePoint);
            controlPointCache.put(waypoint.getControlPoint().getId(), cp);
        }
        return new WaypointDTO(waypoint.getName(), cp, waypoint.getPassingInstructions());
    }
    
    /**
     * @param course
     *            the course to convert
     * @param trackedRace
     *            if <code>null</code>, no position data will be attached to the {@link MarkDTO}s
     * @param timePoint
     *            if <code>trackedRace</code> is not <code>null</code>, specifies the time point for which to determine
     *            the mark positions and attach to the {@link MarkDTO}s. If <code>null<c/code>, the current time point
     *            will be used as default.
     */
    private RaceCourseDTO convertToRaceCourseDTO(CourseBase course, MarkPositionFinder positionFinder, TimePoint timePoint) {
        final RaceCourseDTO result;
        if (course != null) {
            List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
            Map<Serializable, ControlPointDTO> controlPointCache = new HashMap<>();
            for (Waypoint waypoint : course.getWaypoints()) {
                waypointDTOs.add(convertToWaypointDTO(waypoint, controlPointCache, positionFinder, timePoint));
            }
            result = new RaceCourseDTO(waypointDTOs);
        } else {
            result = new RaceCourseDTO(Collections.<WaypointDTO> emptyList());
        }
        return result;
    }
    
    @Override
    public RaceCourseDTO getLastCourseDefinitionInRaceLog(final String leaderboardName, String raceColumnName, String fleetName) throws NotFoundException {
        final RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        // only look for course definitions that really define waypoints; ignore by-name course updates
        CourseBase lastPublishedCourse = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ true).analyze();
        if (lastPublishedCourse == null) {
            lastPublishedCourse = new CourseDataImpl("");
        }
        return convertToRaceCourseDTO(lastPublishedCourse, new MarkPositionFinder() {
            @Override
            public Position find(Mark mark, TimePoint at) {
                return getService().getMarkPosition(mark, (LeaderboardThatHasRegattaLike) getService().getLeaderboardByName(leaderboardName), at);
            }
        }, /* timePoint */ MillisecondsTimePoint.now());
    }
    
    @Override
    public void pingMark(String leaderboardName,
            MarkDTO markDTO, TimePoint timePoint, Position positionDTO) throws DoesNotHaveRegattaLogException {
        final RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        final Mark mark = convertToMark(markDTO, true);
        final TimePoint time = timePoint == null ? MillisecondsTimePoint.now() : timePoint;
        final Position position = positionDTO;
        final GPSFix fix = new GPSFixImpl(position, time);
        getRaceLogTrackingAdapter().pingMark(regattaLog, mark, fix, getService());
    }
    
    @Override
    public void copyCourseToOtherRaceLogs(com.sap.sse.common.Util.Triple<String, String, String> fromTriple,
            Set<com.sap.sse.common.Util.Triple<String, String, String>> toTriples) throws NotFoundException {
        RaceLog fromRaceLog = getRaceLog(fromTriple);
        Set<RaceLog> toRaceLogs = new HashSet<>();
        for (com.sap.sse.common.Util.Triple<String, String, String> toTriple : toTriples) {
            toRaceLogs.add(getRaceLog(toTriple));
        }
        getRaceLogTrackingAdapter().copyCourse(fromRaceLog, toRaceLogs, baseDomainFactory, getService());
    }
    
    @Override
    public void copyCompetitorsToOtherRaceLogs(com.sap.sse.common.Util.Triple<String, String, String> fromTriple,
            Set<com.sap.sse.common.Util.Triple<String, String, String>> toTriples) throws NotFoundException {
        final RaceColumn raceColumn = getRaceColumn(fromTriple.getA(), fromTriple.getB());
        final Set<Pair<RaceColumn, Fleet>> toRaces = new HashSet<>();
        for (com.sap.sse.common.Util.Triple<String, String, String> toTriple : toTriples) {
            final RaceColumn toRaceColumn = getRaceColumn(toTriple.getA(), toTriple.getB());
            final Fleet toFleet = getFleetByName(toRaceColumn, toTriple.getC());
            toRaces.add(new Pair<>(toRaceColumn, toFleet));
        }
        getRaceLogTrackingAdapter().copyCompetitors(raceColumn, getFleetByName(raceColumn, fromTriple.getC()), toRaces);
    }
    
    private TypeBasedServiceFinder<DeviceIdentifierStringSerializationHandler> getDeviceIdentifierStringSerializerHandlerFinder(
            boolean withFallback) {
        TypeBasedServiceFinderFactory factory = getService().getTypeBasedServiceFinderFactory();
        TypeBasedServiceFinder<DeviceIdentifierStringSerializationHandler> finder = factory.createServiceFinder(DeviceIdentifierStringSerializationHandler.class);
        if (withFallback) {
            finder.setFallbackService(new PlaceHolderDeviceIdentifierStringSerializationHandler());
        }
        return finder;
    }
    
    private DeviceIdentifier deserializeDeviceIdentifier(String type, String deviceId) throws NoCorrespondingServiceRegisteredException,
    TransformationException {
        DeviceIdentifierStringSerializationHandler handler =
                getDeviceIdentifierStringSerializerHandlerFinder(false).findService(type);
        return handler.deserialize(deviceId, type, deviceId);
    }
    
    private String serializeDeviceIdentifier(DeviceIdentifier deviceId) throws TransformationException {
        return getDeviceIdentifierStringSerializerHandlerFinder(true).findService(
                deviceId.getIdentifierType()).serialize(deviceId).getB();
    }
    
    private DeviceMappingDTO convertToDeviceMappingDTO(DeviceMapping<?> mapping) throws TransformationException {
        final Map<DeviceIdentifier, Timed> lastFixes = getService().getSensorFixStore().getLastFix(Collections.singleton(mapping.getDevice()));
        final Timed lastFix;
        if (lastFixes != null && lastFixes.containsKey(mapping.getDevice())) {
            lastFix = lastFixes.get(mapping.getDevice());
        } else {
            lastFix = null;
        }
        String deviceId = serializeDeviceIdentifier(mapping.getDevice());
        Date from = mapping.getTimeRange().from() == null || mapping.getTimeRange().from().equals(TimePoint.BeginningOfTime) ? 
                null : mapping.getTimeRange().from().asDate();
        Date to = mapping.getTimeRange().to() == null || mapping.getTimeRange().to().equals(TimePoint.EndOfTime) ?
                null : mapping.getTimeRange().to().asDate();
        MappableToDevice item = null;
        final WithID mappedTo = mapping.getMappedTo();
        if (mappedTo == null) {
            throw new RuntimeException("Device mapping not mapped to any object");
        } else if (mappedTo instanceof Competitor) {
            item = baseDomainFactory.convertToCompetitorDTO((Competitor) mapping.getMappedTo());
        } else if (mappedTo instanceof Mark) {
            item = convertToMarkDTO((Mark) mapping.getMappedTo(), null);
        } else if (mappedTo instanceof Boat) {
            item = baseDomainFactory.convertToBoatDTO((Boat) mappedTo);
        } else {
            throw new RuntimeException("Can only handle Competitor, Boat or Mark as mapped item type, but not "
                    + mappedTo.getClass().getName());
        }
        //Only deal with UUIDs - otherwise we would have to pass Serializable to browser context - which
        //has a large performance implact for GWT.
        //As any Serializable subclass is converted to String by the BaseRaceLogEventSerializer, and only UUIDs are
        //recovered by the BaseRaceLogEventDeserializer, only UUIDs are safe to use anyway.
        List<UUID> originalRaceLogEventUUIDs = new ArrayList<UUID>();
        for (Serializable id : mapping.getOriginalRaceLogEventIds()) {
            if (! (id instanceof UUID)) {
                logger.log(Level.WARNING, "Got RaceLogEvent with id that was not UUID, but " + id.getClass().getName());
                throw new TransformationException("Could not send device mapping to browser: can only deal with UUIDs");
            }
            originalRaceLogEventUUIDs.add((UUID) id);
        }
        return new DeviceMappingDTO(new DeviceIdentifierDTO(mapping.getDevice().getIdentifierType(),
                deviceId), from, to, item, originalRaceLogEventUUIDs, lastFix==null?null:lastFix.getTimePoint());
    }
    
    private List<AbstractLog<?, ?>> getLogHierarchy(String leaderboardName, String raceColumnName,
            String fleetName) throws NotFoundException {
        List<AbstractLog<?, ?>> result = new ArrayList<>();
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        
        if (raceLog != null){
            result.add(raceLog);
        }
        
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (leaderboard instanceof HasRegattaLike) {
            result.add(((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog());
        }
        return result;
    }
    
    private List<DeviceMappingDTO> getDeviceMappings(RegattaLog regattaLog)
            throws TransformationException {
        List<DeviceMappingDTO> result = new ArrayList<DeviceMappingDTO>();
        for (List<? extends DeviceMapping<WithID>> list : new RegattaLogDeviceMappingFinder<>(
                regattaLog).analyze().values()) {
            for (DeviceMapping<WithID> mapping : list) {
                result.add(convertToDeviceMappingDTO(mapping));
            }
        }
        return result;
    }
    
    @Override
    public void addDeviceMappingToRegattaLog(String leaderboardName,
            DeviceMappingDTO dto) throws NoCorrespondingServiceRegisteredException, TransformationException, DoesNotHaveRegattaLogException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        DeviceMapping<?> mapping = convertToDeviceMapping(dto);
        TimePoint now = MillisecondsTimePoint.now();
        RegattaLogEvent event = null;
        TimePoint from = mapping.getTimeRange().hasOpenBeginning() ? null : mapping.getTimeRange().from();
        TimePoint to = mapping.getTimeRange().hasOpenEnd() ? null : mapping.getTimeRange().to();
        if (mapping.getMappedTo() instanceof Mark) {
            Mark mark = (Mark) mapping.getMappedTo();
            event = new RegattaLogDeviceMarkMappingEventImpl(now, now, getService().getServerAuthor(), UUID.randomUUID(), 
                    mark, mapping.getDevice(), from, to);
        } else if (mapping.getMappedTo() instanceof Competitor) {
            Competitor competitor = (Competitor) mapping.getMappedTo();
            if (mapping.getDevice().getIdentifierType().equals(ExpeditionSensorDeviceIdentifier.TYPE)) {
                event = new RegattaLogDeviceCompetitorExpeditionExtendedMappingEventImpl(
                        now, now, getService().getServerAuthor(), UUID.randomUUID(), 
                        competitor, mapping.getDevice(), from, to);
            } else {
                event = new RegattaLogDeviceCompetitorMappingEventImpl(now, now, getService().getServerAuthor(), UUID.randomUUID(), 
                        competitor, mapping.getDevice(), from, to);
            }
        } else if (mapping.getMappedTo() instanceof Boat) {
            final Boat boat = (Boat) mapping.getMappedTo();
            event = new RegattaLogDeviceBoatMappingEventImpl(now, now, getService().getServerAuthor(), UUID.randomUUID(), 
                    boat, mapping.getDevice(), from, to);
        } else {
            throw new RuntimeException("Can only map devices to competitors, boats or marks");
        }
        regattaLog.add(event);
    }
    
    @Override
    public void addTypedDeviceMappingToRegattaLog(String leaderboardName, TypedDeviceMappingDTO dto)
            throws NoCorrespondingServiceRegisteredException, TransformationException, DoesNotHaveRegattaLogException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        DeviceMapping<?> mapping = convertToDeviceMapping(dto);
        TimePoint now = MillisecondsTimePoint.now();
        RegattaLogEvent event = null;
        TimePoint from = mapping.getTimeRange().hasOpenBeginning() ? null : mapping.getTimeRange().from();
        TimePoint to = mapping.getTimeRange().hasOpenEnd() ? null : mapping.getTimeRange().to();
        if (dto.mappedTo instanceof CompetitorWithBoatDTO) {
            DoubleVectorFixImporter importer = getRegisteredImporter(DoubleVectorFixImporter.class, dto.dataType);
            event = importer.createEvent(now, now, getService().getServerAuthor(), UUID.randomUUID(),
                    getCompetitor((CompetitorWithBoatDTO) dto.mappedTo), mapping.getDevice(), from, to);
        } else if (dto.mappedTo instanceof BoatDTO) {
            DoubleVectorFixImporter importer = getRegisteredImporter(DoubleVectorFixImporter.class, dto.dataType);
            event = importer.createEvent(now, now, getService().getServerAuthor(), UUID.randomUUID(),
                    getBoat((BoatDTO) dto.mappedTo), mapping.getDevice(), from, to);
        } else {
            throw new RuntimeException("Can only map devices to a competitor or boat");
        }
        regattaLog.add(event);
    }
    
    @Override
    public List<String> getDeserializableDeviceIdentifierTypes() {
        List<String> result = new ArrayList<String>();
        for (ServiceReference<DeviceIdentifierStringSerializationHandler> reference :
            deviceIdentifierStringSerializationHandlerTracker.getServiceReferences()) {
            result.add((String) reference.getProperty(TypeBasedServiceFinder.TYPE));
        }
        return result;
    }
    
    private DeviceMapping<?> convertToDeviceMapping(DeviceMappingDTO dto)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        DeviceIdentifier device = deserializeDeviceIdentifier(dto.deviceIdentifier.deviceType, dto.deviceIdentifier.deviceId);
        TimePoint from = dto.from == null ? null : new MillisecondsTimePoint(dto.from);
        TimePoint to = dto.to == null ? null : new MillisecondsTimePoint(dto.to);
        TimeRange timeRange = new TimeRangeImpl(from, to);
        if (dto.mappedTo instanceof MarkDTO) {
            Mark mark = convertToMark(((MarkDTO) dto.mappedTo), true);
            //expect UUIDs
            return new DeviceMappingImpl<Mark>(mark, device, timeRange, dto.originalRaceLogEventIds, RegattaLogDeviceMarkMappingEventImpl.class);
        } else if (dto.mappedTo instanceof CompetitorDTO) {
            Competitor competitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(
                    ((CompetitorDTO) dto.mappedTo).getIdAsString());
            return new DeviceMappingImpl<Competitor>(competitor, device, timeRange, dto.originalRaceLogEventIds, RegattaLogDeviceCompetitorMappingEventImpl.class);
        } else if (dto.mappedTo instanceof BoatDTO) {
            final Boat boat = getService().getCompetitorAndBoatStore()
                    .getExistingBoatByIdAsString(dto.mappedTo.getIdAsString());
            return new DeviceMappingImpl<WithID>(boat, device, timeRange, dto.originalRaceLogEventIds,
                    RegattaLogDeviceBoatMappingEventImpl.class);
        } else {
            throw new RuntimeException("Can only map devices to competitors, boats or marks");
        }
    }

    private void closeOpenEndedDeviceMapping(RegattaLog regattaLog, DeviceMappingDTO mappingDTO, Date closingTimePoint) throws TransformationException, UnableToCloseDeviceMappingException {
        boolean successfullyClosed = false;
        List<RegattaLogCloseOpenEndedDeviceMappingEvent> closingEvents = null;
        regattaLog.lockForRead();
        try {
            RegattaLogEvent event = regattaLog.getEventById(mappingDTO.originalRaceLogEventIds.get(0));
            if (event != null) {
                successfullyClosed = true;
                DeviceMapping<?> mapping = convertToDeviceMapping(mappingDTO);
                closingEvents = new RegattaLogOpenEndedDeviceMappingCloser(regattaLog, mapping, getService().getServerAuthor(),
                                new MillisecondsTimePoint(closingTimePoint)).analyze();
            }
        } finally {
            regattaLog.unlockAfterRead();
        }
        // important: read lock must be release before write lock is obtained in add(...); see bug 3774
        if (successfullyClosed) {
            for (RegattaLogEvent closingEvent : closingEvents) {
                regattaLog.add(closingEvent);
            }
        } else {
            throw new UnableToCloseDeviceMappingException();
        }
    }
    
    @Override
    public void revokeRaceAndRegattaLogEvents(String leaderboardName, String raceColumnName, String fleetName,
            List<UUID> eventIds) throws NotRevokableException, NotFoundException {
        List<AbstractLog<?, ?>> logs = getLogHierarchy(leaderboardName, raceColumnName, fleetName);
        revokeEventsFromLogs(logs, eventIds);
    }
    
    @Override
    public void revokeRaceAndRegattaLogEvents(String leaderboardName,
        List<UUID> eventIds) throws NotRevokableException, DoesNotHaveRegattaLogException {
        List<AbstractLog<?, ?>> logs = getLogHierarchy(leaderboardName);
        revokeEventsFromLogs(logs, eventIds);
    }
    
    private void revokeEventsFromLogs(List<AbstractLog<?, ?>> logs, List<UUID> eventIds) throws NotRevokableException{
        boolean eventRevoked = false;
        for (Serializable idToRevoke : eventIds) {
            eventRevoked = false;
            for (AbstractLog<?, ?> abstractLog : logs) {
                eventRevoked = revokeEvent(eventRevoked, idToRevoke, abstractLog);
            }
            if (!eventRevoked){
                logger.warning("Could not revoke event with id "+idToRevoke);
            }
        }
    }

    private <EventT extends AbstractLogEvent<VisitorT>, VisitorT> boolean revokeEvent(boolean eventRevoked, Serializable idToRevoke, AbstractLog<EventT, VisitorT> abstractLog)
            throws NotRevokableException {
        final EventT event; 
        abstractLog.lockForRead();
        try {
            event = abstractLog.getEventById(idToRevoke);
        } finally {
            abstractLog.unlockAfterRead();
        }
        if (event != null) {
            abstractLog.revokeEvent(getService().getServerAuthor(), event, "revoke triggered by GWT user action"); 
            eventRevoked = true;
        }
        return eventRevoked;
    }
    
    @Override
    public void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, final boolean trackWind, final boolean correctWindByDeclination)
            throws NotDenotedForRaceLogTrackingException, Exception {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        getRaceLogTrackingAdapter().startTracking(getService(), leaderboard, raceColumn, fleet, trackWind, correctWindByDeclination);
    }
    
    @Override
    public void startRaceLogTracking(List<Triple<String, String, String>> leaderboardRaceColumnFleetNames,
            final boolean trackWind, final boolean correctWindByDeclination)
            throws NotDenotedForRaceLogTrackingException, Exception {
        for (final Triple<String, String, String> leaderboardRaceColumnFleetName : leaderboardRaceColumnFleetNames) {
            startRaceLogTracking(leaderboardRaceColumnFleetName.getA(), leaderboardRaceColumnFleetName.getB(),
                    leaderboardRaceColumnFleetName.getC(), trackWind, correctWindByDeclination);
        }
    }
    
    @Override
    public Collection<String> getGPSFixImporterTypes() {
        return getRegisteredImporterTypes(GPSFixImporter.class);
    }
    
    @Override
    public Collection<String> getSensorDataImporterTypes() {
        return getRegisteredImporterTypes(DoubleVectorFixImporter.class);
    }
    
    private <S> Collection<String> getRegisteredImporterTypes(Class<S> referenceClass) {
        Set<String> result = new HashSet<>();
        for (ServiceReference<S> reference : getRegisteredServiceReferences(referenceClass)) {
            result.add((String) reference.getProperty(TypeBasedServiceFinder.TYPE));
        }
        return result;
    }
    
    private <S extends DoubleVectorFixImporter> S getRegisteredImporter(Class<S> referenceClass, String type)
            throws NoCorrespondingServiceRegisteredException {
        for (ServiceReference<S> reference : getRegisteredServiceReferences(referenceClass)) {
            S importer = Activator.getDefault().getService(reference);
            if (importer != null && importer.getType().equals(type)) {
                return importer;
            }
        }
        throw new NoCorrespondingServiceRegisteredException("No importer service found!", type, referenceClass.getName());
    }
    
    private <S> Collection<ServiceReference<S>> getRegisteredServiceReferences(Class<S> referenceClass) {
        try {
            return Activator.getDefault().getServiceReferences(referenceClass, null);
        } catch (InvalidSyntaxException e) {
            // shouldn't happen, as we are passing null for the filter
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<TrackFileImportDeviceIdentifierDTO> getTrackFileImportDeviceIds(List<String> uuids)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        try {
            final List<TrackFileImportDeviceIdentifierDTO> result = new ArrayList<>();
            for (String uuidAsString : uuids) {
                UUID uuid = UUID.fromString(uuidAsString);
                TrackFileImportDeviceIdentifier device = TrackFileImportDeviceIdentifierImpl.getOrCreate(uuid);
                long numFixes = getService().getSensorFixStore().getNumberOfFixes(device);
                TimeRange timeRange = getService().getSensorFixStore().getTimeRangeCoveredByFixes(device);
                Date from = timeRange == null ? null : timeRange.from().asDate();
                Date to = timeRange == null ? null : timeRange.to().asDate();
                result.add(new TrackFileImportDeviceIdentifierDTO(uuidAsString, device.getFileName(), device.getTrackName(),
                        numFixes, from, to));
            }
            return result;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception trying to obtain track file import device IDs", e);
            throw e;
        }
    }

    @Override
    public RaceDTO setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived) {
        RegattaNameAndRaceName regattaAndRaceIdentifier = new RegattaNameAndRaceName(raceIdentifier.getRegattaName(),
                raceIdentifier.getRaceName());
        DynamicTrackedRace trackedRace = getService().getTrackedRace(regattaAndRaceIdentifier);
        trackedRace.setStartTimeReceived(
                newStartTimeReceived == null ? null : new MillisecondsTimePoint(newStartTimeReceived));
        return baseDomainFactory.createRaceDTO(getService(), false, regattaAndRaceIdentifier, trackedRace);
    }
    
    @Override
    public ArrayList<EventDTO> getEventsForLeaderboard(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        HashMap<UUID, EventDTO> events = new HashMap<>();
        for (Event e : getService().getAllEvents()) {
            for (LeaderboardGroup g : e.getLeaderboardGroups()) {
                for (Leaderboard l : g.getLeaderboards()) {
                    if (leaderboard.equals(l)) {
                        events.put(e.getId(), convertToEventDTO(e, false));
                    }
                }
            }
        }
        return new ArrayList<>(events.values());
    }

    @Override
    public Map<Integer, Date> getCompetitorRaceLogMarkPassingData(String leaderboardName, String raceColumnName, String fleetName, CompetitorDTO competitor) {
        Map<Integer, Date> result = new HashMap<>();
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        for (Triple<Competitor, Integer, TimePoint> fixedEvent : new MarkPassingDataFinder(raceLog).analyze()) {
            if (fixedEvent.getA().getName().equals(competitor.getName())) {
                final Date date;
                if (fixedEvent.getC() != null) {
                    date = new Date(fixedEvent.getC().asMillis());
                } else {
                    date = null;
                }
                result.put(fixedEvent.getB(), date);
            }
        }
        return result;
    }

    @Override
    public void updateFixedMarkPassing(String leaderboardName, String raceColumnName, String fleetName, Integer indexOfWaypoint,
            Date dateOfMarkPassing, CompetitorDTO competitorDTO) {
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        Competitor competitor = getCompetitor(competitorDTO);
        RaceLogFixedMarkPassingEvent oldFixedMarkPassingEvent = null;
        for (RaceLogEvent event : raceLog.getUnrevokedEvents()) {
            if (event instanceof RaceLogFixedMarkPassingEventImpl && event.getInvolvedCompetitors().contains(competitor)) {
                RaceLogFixedMarkPassingEvent fixedEvent = (RaceLogFixedMarkPassingEvent) event;
                if (Util.equalsWithNull(fixedEvent.getZeroBasedIndexOfPassedWaypoint(), indexOfWaypoint)) {
                    oldFixedMarkPassingEvent = fixedEvent;
                }
            }
        }
        if (oldFixedMarkPassingEvent != null) {
            try {
                raceLog.revokeEvent(getService().getServerAuthor(), oldFixedMarkPassingEvent);
            } catch (NotRevokableException e) {
                e.printStackTrace();
            }
        }
        if (dateOfMarkPassing != null) {
            raceLog.add(new RaceLogFixedMarkPassingEventImpl(MillisecondsTimePoint.now(), getService()
                    .getServerAuthor(), competitor, raceLog.getCurrentPassId(),
                    new MillisecondsTimePoint(dateOfMarkPassing), indexOfWaypoint));
        }
    }
    
    @Override
    public void updateSuppressedMarkPassings(String leaderboardName, String raceColumnName, String fleetName,
            Integer newZeroBasedIndexOfSuppressedMarkPassing, CompetitorDTO competitorDTO) {
        RaceLogSuppressedMarkPassingsEvent oldSuppressedMarkPassingEvent = null;
        RaceLog raceLog = getService().getRaceLog(leaderboardName, raceColumnName, fleetName);
        Competitor competitor = getCompetitor(competitorDTO);
        NavigableSet<RaceLogEvent> unrevokedEvents = raceLog.getUnrevokedEvents();
        for (RaceLogEvent event : unrevokedEvents) {
            if (event instanceof RaceLogSuppressedMarkPassingsEvent && event.getInvolvedCompetitors().contains(competitor)) {
                oldSuppressedMarkPassingEvent = (RaceLogSuppressedMarkPassingsEvent) event;
                break;
            }
        }
        
        final boolean create;
        final boolean revoke;
        if (oldSuppressedMarkPassingEvent == null) {
            if (newZeroBasedIndexOfSuppressedMarkPassing == null) {
                create = false;
                revoke = false;
            } else {
                create = true;
                revoke = false;
            }
        } else {
            if (newZeroBasedIndexOfSuppressedMarkPassing == null) {
                revoke = true;
                create = false;
            } else {
                boolean equal = Util.equalsWithNull(newZeroBasedIndexOfSuppressedMarkPassing,
                		oldSuppressedMarkPassingEvent.getZeroBasedIndexOfFirstSuppressedWaypoint());
                create = !equal;
                revoke = !equal;
            }
        }
        if (revoke) {
            try {
                raceLog.revokeEvent(getService().getServerAuthor(), oldSuppressedMarkPassingEvent);
            } catch (NotRevokableException e) {
            }
        }
        if (create) {
            raceLog.add(new RaceLogSuppressedMarkPassingsEventImpl(MillisecondsTimePoint.now(), getService()
                    .getServerAuthor(), competitor, raceLog.getCurrentPassId(),
                    newZeroBasedIndexOfSuppressedMarkPassing));
        }
    }

    @Override
    public Map<Integer, Date> getCompetitorMarkPassings(RegattaAndRaceIdentifier race, CompetitorDTO competitorDTO, boolean waitForCalculations) {
        Map<Integer, Date> result = new HashMap<>();
        final TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            Competitor competitor = getCompetitorByIdAsString(trackedRace.getRace().getCompetitors(), competitorDTO.getIdAsString());
            Set<MarkPassing> competitorMarkPassings;
            competitorMarkPassings = trackedRace.getMarkPassings(competitor, waitForCalculations);
            Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
            if (competitorMarkPassings != null) {
                for (MarkPassing markPassing : competitorMarkPassings) {
                    result.put(Util.indexOf(waypoints, markPassing.getWaypoint()), markPassing.getTimePoint().asDate());
                }
            }
        }
        return result;
    }

    @Override
    public PolarSheetsXYDiagramData createXYDiagramForBoatClass(String boatClassName) {
        BoatClass boatClass = getService().getBaseDomainFactory().getOrCreateBoatClass(boatClassName);
        Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> regressionSpeedDataLists = new HashMap<>();
        for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
            for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                regressionSpeedDataLists.put(new Pair<LegType, Tack>(legType, tack),
                        new ArrayList<Pair<Double, Double>>());
            }
        }
        for (double windInKnots = 0.1; windInKnots < 30; windInKnots = windInKnots + 0.1) {
            for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
                for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {     
                    try {
                        SpeedWithBearingWithConfidence<Void> averageUpwindStarboardRegression = getService()
                                .getPolarDataService().getAverageSpeedWithBearing(boatClass,
                                        new KnotSpeedImpl(windInKnots), legType, tack);

                        regressionSpeedDataLists.get(new Pair<LegType, Tack>(legType, tack)).add(
                                new Pair<Double, Double>(windInKnots, averageUpwindStarboardRegression.getObject()
                                        .getKnots()));
                    } catch (NotEnoughDataHasBeenAddedException e) {
                        // Do not add a point to the result
                    }
                }
            }
        }

        PolarSheetsXYDiagramData data = new PolarSheetsXYDiagramDataImpl(regressionSpeedDataLists);

        return data;
    }
    
    private FileStorageService getFileStorageService(String name) {
        if (name == null || name.equals("")) {
            return null;
        }
        return getService().getFileStorageManagementService().getFileStorageService(name);
    }
    
    private Locale getLocale(String localeInfoName) {
        return ResourceBundleStringMessages.Util.getLocaleFor(localeInfoName);
    }

    @Override
    public FileStorageServiceDTO[] getAvailableFileStorageServices(String localeInfoName) {
        List<FileStorageServiceDTO> serviceDtos = new ArrayList<>();
        final FileStorageManagementService fileStorageManagementService = getService().getFileStorageManagementService();
        if (fileStorageManagementService != null) {
            for (FileStorageService s : fileStorageManagementService.getAvailableFileStorageServices()) {
                serviceDtos.add(FileStorageServiceDTOUtils.convert(s, getLocale(localeInfoName)));
            }
        }
        return serviceDtos.toArray(new FileStorageServiceDTO[0]);
    }

    @Override
    public void setFileStorageServiceProperties(String serviceName, Map<String, String> properties) {
        for (Entry<String, String> p : properties.entrySet()) {
            try {
                getService().getFileStorageManagementService()
                    .setFileStorageServiceProperty(getFileStorageService(serviceName), p.getKey(), p.getValue());
            } catch (NoCorrespondingServiceRegisteredException | IllegalArgumentException e) {
                //ignore, doing refresh afterwards anyways
            }
        }
    }

    @Override
    public FileStorageServicePropertyErrorsDTO testFileStorageServiceProperties(String serviceName, String localeInfoName) throws IOException {
        try {
            if (serviceName == null) {
                serviceName = getActiveFileStorageServiceName();
            }
            FileStorageService service = getFileStorageService(serviceName);
            if (service != null) {
                service.testProperties();
            }
        } catch (InvalidPropertiesException e) {
            return FileStorageServiceDTOUtils.convert(e, getLocale(localeInfoName));
        }
        return null;
    }

    @Override
    public void setActiveFileStorageService(String serviceName, String localeInfoName) {
        getService().getFileStorageManagementService().setActiveFileStorageService(getFileStorageService(serviceName));
    }

    @Override
    public String getActiveFileStorageServiceName() {
        try {
            final FileStorageService activeFileStorageService = getService().getFileStorageManagementService().getActiveFileStorageService();
            return activeFileStorageService == null ? null : activeFileStorageService.getName();
        } catch (NoCorrespondingServiceRegisteredException e) {
            return null;
        }
    }

    @Override
    public void inviteCompetitorsForTrackingViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto,
            String leaderboardName, Collection<CompetitorDTO> competitorDtos, String iOSAppUrl, String androidAppUrl,
            String localeInfoName) throws MailException {
        Event event = getService().getEvent(eventDto.id);
        Set<Competitor> competitors = new HashSet<>();
        for (CompetitorDTO c : competitorDtos) {
            competitors.add(getCompetitor(c));
        }
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        getRaceLogTrackingAdapter().inviteCompetitorsForTrackingViaEmail(event, leaderboard, serverUrlWithoutTrailingSlash,
                competitors, iOSAppUrl, androidAppUrl, getLocale(localeInfoName));
    }
    
    @Override
    public void inviteBuoyTenderViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto,
            String leaderboardName, String emails, String iOSAppUrl, String androidAppUrl, String localeInfoName)
            throws MailException {
        Event event = getService().getEvent(eventDto.id);
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        getRaceLogTrackingAdapter().inviteBuoyTenderViaEmail(event, leaderboard, serverUrlWithoutTrailingSlash,
                emails, iOSAppUrl, androidAppUrl, getLocale(localeInfoName));
    }

    @Override
    public ArrayList<LeaderboardGroupDTO> getLeaderboardGroupsByEventId(UUID id) {
        Event event = getService().getEvent(id);
        if (event == null) {
            throw new RuntimeException("Event not found");
        }
        
        ArrayList<LeaderboardGroupDTO> result = new ArrayList<>();
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            if (getSecurityService().hasCurrentUserReadPermission(lg)) {
                result.add(convertToLeaderboardGroupDTO(lg, /* withGeoLocationData */false, true));
            }
        }
        return result;
    }

    /**
     * Gets all Marks defined in the RegattaLog and the trackedRace
     * @throws DoesNotHaveRegattaLogException 
     */
    @Override
    public Iterable<MarkDTO> getMarksInRegattaLog(String leaderboardName) throws DoesNotHaveRegattaLogException {
        final Leaderboard l = getService().getLeaderboardByName(leaderboardName);
        if (! (l instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        final LeaderboardThatHasRegattaLike leaderboard = (LeaderboardThatHasRegattaLike) l;
        final RegattaLog regattaLog = leaderboard.getRegattaLike().getRegattaLog();
        final Set<MarkDTO> markDTOs = new HashSet<>();
        final List<RegattaLogEvent> markEvents = new AllEventsOfTypeFinder<>(regattaLog, /* only unrevoked */ true, RegattaLogDefineMarkEvent.class).analyze();
        for (RegattaLogEvent regattaLogEvent : markEvents) {
            final RegattaLogDefineMarkEvent defineMarkEvent = (RegattaLogDefineMarkEvent) regattaLogEvent;
            markDTOs.add(convertToMarkDTO(leaderboard, defineMarkEvent.getMark()));
        }
        return markDTOs;
    }

    @Override
    public void closeOpenEndedDeviceMapping(String leaderboardName, DeviceMappingDTO mappingDTO, Date closingTimePoint) throws TransformationException, DoesNotHaveRegattaLogException, UnableToCloseDeviceMappingException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        closeOpenEndedDeviceMapping(regattaLog, mappingDTO, closingTimePoint);
    }

    /**
     * Gets all the logs corresponding to a leaderboard. This includes all the RaceLogs of the leaderBoard's raceColumns
     * @param leaderboardName
     * @return
     */
    private List<AbstractLog<?, ?>> getLogHierarchy(String leaderboardName) {
        final List<AbstractLog<?, ?>> result;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            result = null;
        } else {
            result = new ArrayList<>();
            if (leaderboard instanceof HasRegattaLike) {
                result.add(((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog());
            }
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    RaceLog raceLog = raceColumn.getRaceLog(fleet);
                    result.add(raceLog);
                }
            }
        }
        return result;
    }   

    public boolean doesRegattaLogContainCompetitors(String leaderboardName) throws DoesNotHaveRegattaLogException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        List<RegattaLogEvent> comeptitorRegistrationEvents = new AllEventsOfTypeFinder<>(regattaLog,
                /* only unrevoked */ true, RegattaLogRegisterCompetitorEvent.class).analyze();
        return !comeptitorRegistrationEvents.isEmpty();
    }

    @Override
    public RegattaAndRaceIdentifier getRaceIdentifier(String regattaLikeName, String raceColumnName, String fleetName) {
        RegattaAndRaceIdentifier result = null;
        final Leaderboard leaderboard = getService().getLeaderboardByName(regattaLikeName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                    if (trackedRace != null) {
                        result = trackedRace.getRaceIdentifier();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Collection<CompetitorAndBoatDTO> getCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName,
            String fleetName) throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        return convertToCompetitorAndBoatDTOs(raceColumn.getAllCompetitorsAndTheirBoats(fleet));
    }
    
    @Override
    public Collection<CompetitorDTO> getCompetitorRegistrationsForLeaderboard(String leaderboardName) throws NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        return convertToCompetitorDTOs(leaderboard.getAllCompetitors());
    }

    @Override
    public Collection<CompetitorDTO> getCompetitorRegistrationsInRegattaLog(String leaderboardName) throws DoesNotHaveRegattaLogException, NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (! (leaderboard instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        HasRegattaLike regattaLikeLeaderboard = ((HasRegattaLike) leaderboard);
        return convertToCompetitorDTOs(regattaLikeLeaderboard.getCompetitorsRegisteredInRegattaLog());
    }
    
    @Override
    public List<CompetitorAndBoatDTO> getCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName,
            String fleetName) throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        return convertToCompetitorAndBoatDTOs(raceColumn.getCompetitorsRegisteredInRacelog(fleet));
    }
    
    @Override
    public Map<CompetitorDTO, BoatDTO> getCompetitorAndBoatRegistrationsInRaceLog(String leaderboardName, String raceColumnName,
            String fleetName) throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        Map<Competitor, Boat> competitorsAndBoatsRegisteredInRacelog = raceColumn.getCompetitorsRegisteredInRacelog(fleet);
        return baseDomainFactory.convertToCompetitorAndBoatDTOs(competitorsAndBoatsRegisteredInRacelog);
    }

    @Override
    public Collection<BoatDTO> getBoatRegistrationsInRegattaLog(String leaderboardName) throws DoesNotHaveRegattaLogException, NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (!(leaderboard instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        HasRegattaLike regattaLikeLeaderboard = ((HasRegattaLike) leaderboard);
        return convertToBoatDTOs(regattaLikeLeaderboard.getBoatsRegisteredInRegattaLog());
    }
    
    @Override
    public void setBoatRegistrationsInRegattaLog(String leaderboardName, Set<BoatDTO> boatDTOs)
            throws DoesNotHaveRegattaLogException, NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        if (!(leaderboard instanceof HasRegattaLike)){
            throw new DoesNotHaveRegattaLogException();
        }
        
        Set<Boat> boatsToRegister = new HashSet<Boat>();
        for (BoatDTO dto : boatDTOs) {
            boatsToRegister.add(getBoat(dto));
        }
        
        HasRegattaLike hasRegattaLike = (HasRegattaLike) leaderboard;
        Iterable<Boat> boatsToRemove = leaderboard.getAllBoats();
        HashSet<Boat> boatSetToRemove = new HashSet<>();
        Util.addAll(boatsToRemove, boatSetToRemove);
        filterBoatDuplicates(boatsToRegister, boatSetToRemove);
        
        hasRegattaLike.deregisterBoats(boatSetToRemove);
        hasRegattaLike.registerBoats(boatsToRegister);        
    }

    @Override
    public Collection<BoatDTO> getBoatRegistrationsForLeaderboard(String leaderboardName) throws NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        return convertToBoatDTOs(leaderboard.getAllBoats());
    }

    @Override
    public Boolean areCompetitorRegistrationsEnabledForRace(String leaderboardName, String raceColumnName,
            String fleetName) throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        return raceColumn.isCompetitorRegistrationInRacelogEnabled(fleet);
    }
    
    private Fleet getFleetByName(RaceColumn raceColumn, String fleetName) throws NotFoundException{
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null){
            throw new NotFoundException("fleet with name "+fleetName+" not found");
        }
        return fleet;
    }
    
    private Leaderboard getLeaderboardByName(String leaderboardName) throws NotFoundException{
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null){
            throw new NotFoundException("Leaderboard with name "+leaderboardName+" not found");
        }
        return leaderboard;
    }
    
    private RaceColumn getRaceColumn(String leaderboardName, String raceColumnName) throws NotFoundException{
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null){
            throw new NotFoundException("leaderboard with name "+leaderboardName+" not found");
        } 
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null){
            throw new NotFoundException("raceColumn with name "+raceColumnName+" not found");
        }
        return raceColumn;
    }

    @Override
    public void disableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName) throws NotRevokableException, NotFoundException {
        if (areCompetitorRegistrationsEnabledForRace(leaderboardName, raceColumnName, fleetName)){
            RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
            raceColumn.disableCompetitorRegistrationOnRaceLog(getFleetByName(raceColumn, fleetName));
        }
    }

    @Override
    public void enableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName) throws IllegalArgumentException, NotFoundException {
        if (!areCompetitorRegistrationsEnabledForRace(leaderboardName, raceColumnName, fleetName)){
            RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
            raceColumn.enableCompetitorRegistrationOnRaceLog(getFleetByName(raceColumn, fleetName));
        }
    }

    @Override
    public Pair<Boolean, String> checkIfMarksAreUsedInOtherRaceLogs(String leaderboardName, String raceColumnName,
            String fleetName, Set<MarkDTO> marksToRemove) throws NotFoundException {
        Set<String> markIds = new HashSet<String>();
        for (MarkDTO markDTO : marksToRemove) {
            markIds.add(markDTO.getIdAsString());
        }
        RaceLog raceLogToIgnore = getRaceLog(leaderboardName, raceColumnName, fleetName);
        HashSet<String> racesContainingMarksToDeleteInCourse = new HashSet<String>();
        boolean marksAreUsedInOtherRaceLogs = false;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                if (raceLog != raceLogToIgnore) {
                    LastPublishedCourseDesignFinder finder = new LastPublishedCourseDesignFinder(raceLog, /* onlyCoursesWithValidWaypointList */ true);
                    CourseBase course = finder.analyze();
                    if (course != null) {
                        for (Waypoint waypoint : course.getWaypoints()) {
                            for (Mark mark : waypoint.getMarks()) {
                                if (markIds.contains(mark.getId().toString())) {
                                    racesContainingMarksToDeleteInCourse.add(raceColumn.getName() + "/"
                                            + fleet.getName());
                                    marksAreUsedInOtherRaceLogs = true;
                                }
                            }
                        }
                    }
                }
            }
        }         
        StringBuilder racesInCollision = new StringBuilder();
        for (String raceName : racesContainingMarksToDeleteInCourse) {
            racesInCollision.append(raceName+", ");
        }
        return new Pair<Boolean, String>(marksAreUsedInOtherRaceLogs, 
                racesInCollision.substring(0, Math.max(0, racesInCollision.length()-2)));
    }

    @Override
    public List<DeviceMappingDTO> getDeviceMappings(String leaderboardName)
            throws DoesNotHaveRegattaLogException, TransformationException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        return getDeviceMappings(regattaLog);
    }
    
    @Override
    public Iterable<MarkDTO> getMarksInTrackedRace(String leaderboardName, String raceColumnName, String fleetName) {
        final List<MarkDTO> marks = new ArrayList<>();
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        marks.add(new MarkDTO(mark.getId().toString(), mark.getName()));
                    }
                }
            }
        }
        return marks;
    }

    @Override
    public MarkTracksDTO getMarkTracks(String leaderboardName, String raceColumnName, String fleetName) {
        final List<MarkTrackDTO> markTracks = new ArrayList<>();
        for (final MarkDTO mark : getMarksInTrackedRace(leaderboardName, raceColumnName, fleetName)) {
            MarkTrackDTO markTrackDTO = getMarkTrack(leaderboardName, raceColumnName, fleetName, mark.getIdAsString());
            markTracks.add(markTrackDTO);
        }
        return new MarkTracksDTO(markTracks);
    }
    
    private MarkTrackDTO getMarkTrack(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet,
            String markIdAsString) {
        MarkDTO markDTO = null;
        Mark mark = null;
        for (final Mark currMark : raceColumn.getAvailableMarks(fleet)) {
            if (currMark.getId().toString().equals(markIdAsString)) {
                mark = currMark;
                markDTO = convertToMarkDTO(currMark, /* position */ null);
                break;
            }
        }

        if (markDTO != null) {
            final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
            final GPSFixTrack<Mark, ? extends GPSFix> markTrack;
            if (trackedRace != null) {
                markTrack = trackedRace.getOrCreateTrack(mark);
            } else {
                DynamicGPSFixTrackImpl<Mark> writeableMarkTrack = new DynamicGPSFixTrackImpl<Mark>(mark,
                        BoatClass.APPROXIMATE_AVERAGE_MANEUVER_DURATION.asMillis());
                markTrack = writeableMarkTrack;
                final RaceLog raceLog = raceColumn.getRaceLog(fleet);
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final TrackingTimesFinder trackingTimesFinder = new TrackingTimesFinder(raceLog);
                final Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> trackingTimes = trackingTimesFinder
                        .analyze();
                try {
                    SensorFixStore sensorFixStore = getService().getSensorFixStore();
                    List<DeviceMappingWithRegattaLogEvent<Mark>> mappings = new RegattaLogDeviceMarkMappingFinder(
                            regattaLog).analyze().get(mark);
                    if (mappings != null) {
                        for (DeviceMapping<Mark> mapping : mappings) {
                            final TimePoint from = Util.getLatestOfTimePoints(trackingTimes.getA().getTimePoint(),
                                    mapping.getTimeRange().from());
                            final TimePoint to = Util.getEarliestOfTimePoints(trackingTimes.getB().getTimePoint(),
                                    mapping.getTimeRange().to());
                            sensorFixStore.<GPSFix> loadFixes(loadedFix -> writeableMarkTrack.add(loadedFix, true),
                                    mapping.getDevice(), from, to, false, () -> false, progressIgnoringConsumer -> {
                                    });
                        }
                    }
                } catch (TransformationException | NoCorrespondingServiceRegisteredException e) {
                    logger.info("Error trying to load mark track for mark " + mark + " from " + trackingTimes.getA()
                            + " to " + trackingTimes.getB());
                }
            }
            Iterable<GPSFixDTO> gpsFixDTOTrack = convertToGPSFixDTOTrack(markTrack);
            return new MarkTrackDTO(markDTO, gpsFixDTOTrack, /* thinned out */ false);
        }
        return null;
    }
    
    @Override
    public MarkTrackDTO getMarkTrack(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString) {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    return getMarkTrack(leaderboard, raceColumn, fleet, markIdAsString);
                }
            }
        }
        return null;
    }
    
    /**
     * Uses all fixed from {@code track} with the outliers removed, {@link #convertToGPSFixDTO(GPSFix) converts} each
     * of them to a {@link GPSFixDTO} and adds them to the resulting list.
     */
    private Iterable<GPSFixDTO> convertToGPSFixDTOTrack(Track<? extends GPSFix> track) {
        final List<GPSFixDTO> result = new ArrayList<>();
        track.lockForRead();
        try {
            for (final GPSFix fix : track.getRawFixes()) {
                result.add(convertToGPSFixDTO(fix));
            }
        } finally {
            track.unlockAfterRead();
        }
        return result;
    }
    
    private GPSFixDTO convertToGPSFixDTO(GPSFix fix) {
        final GPSFixDTO result;
        if (fix == null) {
            result = null;
        } else {
            result = new GPSFixDTO(fix.getTimePoint().asDate(), fix.getPosition());
        }
        return result;
    }

    @Override
    public boolean canRemoveMarkFix(String leaderboardName, String raceColumnName, String fleetName,
            String markIdAsString, GPSFixDTO fix) {
        boolean result = false;
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final TimePoint fixTimePoint = new MillisecondsTimePoint(fix.timepoint);
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final BaseRegattaLogDeviceMappingFinder<Mark> mappingFinder = new RegattaLogDeviceMarkMappingFinder(regattaLog);
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        if (mark.getId().toString().equals(markIdAsString)) {
                            result = mappingFinder.hasMappingFor(mark, fixTimePoint);
                            if (result) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void removeMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString, GPSFixDTO fix) throws NotRevokableException {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final TimePoint fixTimePoint = new MillisecondsTimePoint(fix.timepoint);
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final BaseRegattaLogDeviceMappingFinder<Mark> mappingFinder = new RegattaLogDeviceMarkMappingFinder(regattaLog);
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        if (mark.getId().toString().equals(markIdAsString)) {
                            mappingFinder.removeTimePointFromMapping(mark, fixTimePoint);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString, GPSFixDTO newFix) {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        if (mark.getId().toString().equals(markIdAsString)) {
                            getRaceLogTrackingAdapter().pingMark(regattaLog, mark,
                                    new GPSFixImpl(newFix.position, new MillisecondsTimePoint(newFix.timepoint)),
                                    getService());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void editMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO oldFix, Position newPosition) throws NotRevokableException {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final BaseRegattaLogDeviceMappingFinder<Mark> mappingFinder = new RegattaLogDeviceMarkMappingFinder(
                        regattaLog);
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    final TimePoint fixTimePoint = new MillisecondsTimePoint(oldFix.timepoint);
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        if (mark.getId().toString().equals(markIdAsString)) {
                            mappingFinder.removeTimePointFromMapping(mark, fixTimePoint);
                            getRaceLogTrackingAdapter().pingMark(regattaLog, mark,
                                    new GPSFixImpl(newPosition, fixTimePoint), getService());
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> getTrackingTimes(
            Collection<Triple<String, String, String>> leaderboardRaceColumnFleetNames) {
        Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> trackingTimes = new HashMap<>(); 
        for (Triple<String, String, String> leaderboardRaceColumnFleetName : leaderboardRaceColumnFleetNames) {
            try {
                trackingTimes.put(leaderboardRaceColumnFleetName, getTrackingTimes(leaderboardRaceColumnFleetName.getA(), 
                        leaderboardRaceColumnFleetName.getB(), leaderboardRaceColumnFleetName.getC()));
            } catch (Exception e) {
                trackingTimes.put(leaderboardRaceColumnFleetName, null);
            }
        }
        return trackingTimes;
    }

    @Override
    public Collection<CompetitorDTO> getEliminatedCompetitors(String leaderboardName) {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null || !(leaderboard instanceof RegattaLeaderboardWithEliminations)) {
            throw new IllegalArgumentException(leaderboardName+" does not match a regatta leaderboard with eliminations");
        }
        final RegattaLeaderboardWithEliminations rlwe = (RegattaLeaderboardWithEliminations) leaderboard;
        return convertToCompetitorDTOs(rlwe.getEliminatedCompetitors());
    }
    
    @Override
    public void setEliminatedCompetitors(String leaderboardName, Set<CompetitorDTO> newEliminatedCompetitorDTOs) {
        Set<Competitor> newEliminatedCompetitors = new HashSet<>();
        for (final CompetitorDTO cDTO : newEliminatedCompetitorDTOs) {
            newEliminatedCompetitors.add(getCompetitor(cDTO));
        }
        getService().apply(new UpdateEliminatedCompetitorsInLeaderboard(leaderboardName, newEliminatedCompetitors));
    }

    @Override
    public Iterable<DetailType> determineDetailTypesForCompetitorChart(String leaderboardGroupName, RegattaAndRaceIdentifier identifier) {
        final List<DetailType> availableDetailsTypes = new ArrayList<>();
        availableDetailsTypes.addAll(DetailType.getAutoplayDetailTypesForChart());
        availableDetailsTypes.removeAll(DetailType.getRaceBravoDetailTypes());
        final DynamicTrackedRace trackedRace = getService().getTrackedRace(identifier);
        if (trackedRace != null) {
            boolean hasBravoTrack = false;
            boolean hasExtendedBravoFixes = false;
            for (BravoFixTrack<Competitor> track : trackedRace.<BravoFix, BravoFixTrack<Competitor>>getSensorTracks(BravoFixTrack.TRACK_NAME)) {
                hasBravoTrack = true;
                if (track.hasExtendedFixes()) {
                    hasExtendedBravoFixes = true;
                    break;
                }
            }
            if (hasBravoTrack) {
                availableDetailsTypes.addAll(DetailType.getRaceBravoDetailTypes());
            }
            if (hasExtendedBravoFixes) {
                availableDetailsTypes.addAll(DetailType.getRaceExtendedBravoDetailTypes());
                availableDetailsTypes.addAll(DetailType.getRaceExpeditionDetailTypes());
            }
        }
        if (leaderboardGroupName != null) {
            final LeaderboardGroupDTO group = getLeaderboardGroupByName(leaderboardGroupName, false);
            if (group != null ? group.hasOverallLeaderboard() : false) {
                availableDetailsTypes.add(DetailType.OVERALL_RANK);
            }
        }
        return availableDetailsTypes;
    }

    @Override
    public List<ExpeditionDeviceConfiguration> getExpeditionDeviceConfigurations() {
        final List<ExpeditionDeviceConfiguration> result = new ArrayList<>();
        final ExpeditionTrackerFactory expeditionConnector = expeditionConnectorTracker.getService();
        final Subject subject = SecurityUtils.getSubject();
        if (expeditionConnector != null) {
            for (final ExpeditionDeviceConfiguration config : expeditionConnector.getDeviceConfigurations()) {
                if (subject.isPermitted(SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getStringPermissionForObjects(DefaultActions.READ,
                        WildcardPermissionEncoder.encode(getServerInfo().getServerName(), config.getName())))) {
                    result.add(config);
                }
            }
        }
        return result;
    }

    @Override
    public void addOrReplaceExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration deviceConfiguration) {
        final Subject subject = SecurityUtils.getSubject();
        subject.checkPermission(
                SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getStringPermissionForObjects(DefaultActions.CREATE,
                        WildcardPermissionEncoder.encode(getServerInfo().getServerName(),
                                deviceConfiguration.getName())));

        // TODO consider replication
        final ExpeditionTrackerFactory expeditionConnector = expeditionConnectorTracker.getService();
        if (expeditionConnector != null) {
            expeditionConnector.addOrReplaceDeviceConfiguration(deviceConfiguration);
        }
    }

    @Override
    public void removeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration deviceConfiguration) {
        final Subject subject = SecurityUtils.getSubject();
        subject.checkPermission(SecuredDomainType.EXPEDITION_DEVICE_CONFIGURATION.getStringPermissionForObjects(
                DefaultActions.DELETE,
                WildcardPermissionEncoder.encode(getServerInfo().getServerName(), deviceConfiguration.getName())));

        // TODO consider replication
        final ExpeditionTrackerFactory expeditionConnector = expeditionConnectorTracker.getService();
        if (expeditionConnector != null) {
            expeditionConnector.removeDeviceConfiguration(deviceConfiguration);
        }
    }
    
    @Override
    public PairingListTemplateDTO calculatePairingListTemplate(final int flightCount, final int groupCount,
            final int competitorCount, final int flightMultiplier) {
        PairingListTemplate template = getService().createPairingListTemplate(flightCount, groupCount, competitorCount, 
                flightMultiplier);
        return new PairingListTemplateDTO(flightCount, groupCount, competitorCount, flightMultiplier, 
                template.getPairingListTemplate(), template.getQuality());
    }
    
    @Override
    public PairingListDTO getPairingListFromTemplate(final String leaderboardName, final int flightMultiplier,
            final Iterable<String> selectedRaceColumnNames, PairingListTemplateDTO templateDTO) 
            throws NotFoundException, PairingListCreationException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        List<RaceColumn> selectedRaces = new ArrayList<RaceColumn>();
        for (String raceColumnName : selectedRaceColumnNames) {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                if (raceColumnName.equalsIgnoreCase(raceColumn.getName())) {
                    selectedRaces.add(raceColumn);
                }
            }
        }
        PairingListTemplate pairingListTemplate = new PairingListTemplateImpl(templateDTO.getPairingListTemplate(),
                templateDTO.getCompetitorCount(), templateDTO.getFlightMultiplier());
        PairingList<RaceColumn, Fleet, Competitor, Boat> pairingList = getService()
                .getPairingListFromTemplate(pairingListTemplate, leaderboardName, selectedRaces);
        List<List<List<Pair<CompetitorDTO, BoatDTO>>>> result = new ArrayList<>();
        for (RaceColumn raceColumn : selectedRaces) {
            List<List<Pair<CompetitorDTO, BoatDTO>>> raceColumnList = new ArrayList<>();
            for (Fleet fleet : raceColumn.getFleets()) {
                List<Pair<CompetitorDTO, BoatDTO>> fleetList = new ArrayList<>();
                for (Pair<Competitor, Boat> competitorAndBoatPair : pairingList.getCompetitors(raceColumn, fleet)) {
                    final Boat boat = competitorAndBoatPair.getB();
                    final CompetitorDTO competitorDTO;
                    if (competitorAndBoatPair.getA() != null) {
                        competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitorAndBoatPair.getA());
                    } else {
                        competitorDTO = null;
                    }
                    final BoatDTO boatDTO;
                    if (boat != null) {
                        boatDTO = new BoatDTO(boat.getId().toString(), boat.getName(), convertToBoatClassDTO(boat.getBoatClass()),
                                boat.getSailID(), boat.getColor());
                    } else {
                        boatDTO = null;
                    }
                    fleetList.add(new Pair<>(competitorDTO, boatDTO));
                }
                raceColumnList.add(fleetList);
            }
            result.add(raceColumnList);
        }
        return new PairingListDTO(result, Util.asList(selectedRaceColumnNames));
    }
    
    @Override
    public PairingListDTO getPairingListFromRaceLogs(final String leaderboardName) throws NotFoundException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        List<List<List<Pair<CompetitorDTO, BoatDTO>>>> result = new ArrayList<>();
        List<String> raceColumnNames = new ArrayList<>();
        PairingListLeaderboardAdapter adapter = new PairingListLeaderboardAdapter();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (!raceColumn.isMedalRace()) {
                List<List<Pair<CompetitorDTO, BoatDTO>>> raceColumnList = new ArrayList<>();
                for (Fleet fleet : raceColumn.getFleets()) {
                    List<Pair<CompetitorDTO, BoatDTO>> fleetList = new ArrayList<>();
                    for (Pair<Competitor, Boat> competitorAndBoatPair : adapter.getCompetitors(raceColumn, fleet)) {
                        final Boat boat = competitorAndBoatPair.getB();
                        fleetList.add(new Pair<CompetitorDTO, BoatDTO>(baseDomainFactory.convertToCompetitorDTO(competitorAndBoatPair.getA()),
                                new BoatDTO(boat.getId().toString(), boat.getName(),
                                        convertToBoatClassDTO(boat.getBoatClass()), boat.getSailID(), 
                                        boat.getColor())));
                    }
                    if (fleetList.size() > 0) {
                        raceColumnList.add(fleetList);
                    }
                }
                if (raceColumnList.size() > 0) {
                    result.add(raceColumnList);
                    if (!raceColumnNames.contains(raceColumn.getName())) {
                        raceColumnNames.add(raceColumn.getName());
                    }
                }
            }
        }
        return new PairingListDTO(result, raceColumnNames);
    }
    
    @Override
    public void fillRaceLogsFromPairingListTemplate(final String leaderboardName, final int flightMultiplier,
            final Iterable<String> selectedFlightNames, final PairingListDTO pairingListDTO)
            throws NotFoundException, CompetitorRegistrationOnRaceLogDisabledException {
        Leaderboard leaderboard = getLeaderboardByName(leaderboardName);
        int flightCount = 0;
        int groupCount = 0;
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (Util.contains(selectedFlightNames, raceColumn.getName())) {
                groupCount = 0;
                for (Fleet fleet : raceColumn.getFleets()) {
                    raceColumn.enableCompetitorRegistrationOnRaceLog(fleet);
                    Map<CompetitorDTO, BoatDTO> competitors = new HashMap<>();
                    List<Pair<CompetitorDTO, BoatDTO>> competitorsFromPairingList = pairingListDTO.getPairingList()
                            .get(flightCount).get(groupCount);
                    for (Pair<CompetitorDTO, BoatDTO> competitorAndBoatPair : competitorsFromPairingList) {
                        if (competitorAndBoatPair.getA() != null && competitorAndBoatPair.getA().getName() != null) {
                            competitors.put(competitorAndBoatPair.getA(), competitorAndBoatPair.getB());
                        }
                    }
                    this.setCompetitorRegistrationsInRaceLog(leaderboard.getName(), raceColumn.getName(),
                            fleet.getName(), competitors);
                    groupCount++;
                }
                flightCount++;
            } else {
                for (Fleet fleet : raceColumn.getFleets()) {
                    this.setCompetitorRegistrationsInRaceLog(leaderboard.getName(), raceColumn.getName(),
                            fleet.getName(), new HashSet<CompetitorWithBoatDTO>());
                }
            }
        }
        if (leaderboard instanceof LeaderboardThatHasRegattaLike && flightMultiplier > 1) {
            final IsRegattaLike regattaLike = ((LeaderboardThatHasRegattaLike) leaderboard).getRegattaLike();
            logger.info("Updating regatta "+regattaLike.getRegattaLikeIdentifier().getName()+
                    ", setting flag that fleets can run in parallel because a pairing list with flight multiplier "+
                    flightMultiplier+" has been used.");
            regattaLike.setFleetsCanRunInParallelToTrue();
        }
    }
    
    public List<String> getRaceDisplayNamesFromLeaderboard(String leaderboardName,List<String> raceColumnNames) throws NotFoundException {
        Leaderboard leaderboard = this.getLeaderboardByName(leaderboardName);
        List<String> result = new ArrayList<>();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (raceColumn.hasTrackedRaces()) {
                if (raceColumnNames.contains(raceColumn.getName())) {
                    for (Fleet fleet : raceColumn.getFleets()) {
                        if(raceColumn.getTrackedRace(fleet) != null && raceColumn.getTrackedRace(fleet).getRaceIdentifier()!=null) {
                            result.add(raceColumn.getTrackedRace(fleet).getRaceIdentifier().getRaceName());
                        } else {
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }
        if (result.size()==raceColumnNames.size()*Util.size(leaderboard.getRaceColumnByName(raceColumnNames.get(0)).getFleets())) {
            return result;
        }
        result.clear();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet: raceColumn.getFleets()) {
                NavigableSet<RaceLogEvent> set=raceColumn.getRaceLog(fleet).getUnrevokedEvents();
                for (RaceLogEvent raceLogEvent : set) {
                    if (raceLogEvent instanceof RaceLogDenoteForTrackingEvent) {
                        RaceLogDenoteForTrackingEvent denoteEvent = (RaceLogDenoteForTrackingEvent) raceLogEvent;
                        result.add(denoteEvent.getRaceName());
                        break;
                    }
                }
            }
        }
        if (result.size()==raceColumnNames.size()*Util.size(leaderboard.getRaceColumnByName(raceColumnNames.get(0)).getFleets())) {
            return result;
        }
        result.clear();
        for (int count=1;count<=raceColumnNames.size()*Util.size(leaderboard.getRaceColumnByName(raceColumnNames.get(0)).getFleets());count++) {
            result.add("Race "+count);
        }
        return result;
    }

    @Override
    public Iterable<DetailType> getAvailableDetailTypesForLeaderboard(String leaderboardName, RegattaAndRaceIdentifier raceIdentifierOrNull) {
        final Set<DetailType> allowed = new HashSet<>();
        allowed.addAll(DetailType.getAllNonRestrictedDetailTypes());
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            boolean hasBravoTrack = false;
            boolean hasExtendedBravoFixes = false;
            abort: for (RaceColumn race : leaderboard.getRaceColumns()) {
                for (Fleet fleet : race.getFleets()) {
                    if (raceIdentifierOrNull != null && !raceIdentifierOrNull.equals(race.getRaceIdentifier(fleet))) {
                        continue;
                    }
                    
                    final TrackedRace trace = race.getTrackedRace(fleet);
                    if (trace != null) {
                        final DynamicTrackedRace trackedRace = getService().getTrackedRace(trace.getRaceIdentifier());
                        if (trackedRace != null) {
                            for (BravoFixTrack<Competitor> track : trackedRace
                                    .<BravoFix, BravoFixTrack<Competitor>>getSensorTracks(BravoFixTrack.TRACK_NAME)) {
                                hasBravoTrack = true;
                                if (track.hasExtendedFixes()) {
                                    hasExtendedBravoFixes = true;
                                    break abort;
                                }
                            }
                        }
                    }
                }
            }
            if (hasBravoTrack) {
                allowed.addAll(DetailType.getRaceBravoDetailTypes());
                allowed.addAll(DetailType.getLegBravoDetailTypes());
                allowed.addAll(DetailType.getOverallBravoDetailTypes());
            }
            if (hasExtendedBravoFixes) {
                allowed.addAll(DetailType.getRaceExpeditionDetailTypes());
                allowed.addAll(DetailType.getLegExpeditionDetailColumnTypes());
            }
        }
        allowed.removeAll(DetailType.getDisabledDetailColumTypes());
        return allowed;
    }

    public SpotDTO getWindFinderSpot(String spotId) throws MalformedURLException, IOException, org.json.simple.parser.ParseException, InterruptedException, ExecutionException {
        final SpotDTO result;
        final WindFinderTrackerFactory windFinderTrackerFactory = windFinderTrackerFactoryServiceTracker.getService();
        if (windFinderTrackerFactory != null) {
            final Spot spot = windFinderTrackerFactory.getSpotById(spotId, /* cached */ false);
            if (spot != null) {
                result = new SpotDTO(spot);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }
    
    @Override
    public boolean canSliceRace(RegattaAndRaceIdentifier raceIdentifier) {
        final Regatta regatta = getService().getRegattaByName(raceIdentifier.getRegattaName());
        final Leaderboard regattaLeaderboard = getService().getLeaderboardByName(raceIdentifier.getRegattaName());
        final DynamicTrackedRace trackedRace = getService().getTrackedRace(raceIdentifier);
        final boolean result;
        if (regatta == null || !(regattaLeaderboard instanceof RegattaLeaderboard) || trackedRace == null
                || trackedRace.getStartOfTracking() == null || !isSmartphoneTrackingEnabled(trackedRace)) {
            result = false;
        } else {
            final Pair<RaceColumn, Fleet> raceColumnAndFleetOfRaceToSlice = regattaLeaderboard.getRaceColumnAndFleet(trackedRace);
            result = (raceColumnAndFleetOfRaceToSlice != null); // is the TrackedRace associated to the given RegattaLeaderboard?
        }
        return result;
    }
    
    private boolean isSmartphoneTrackingEnabled(DynamicTrackedRace trackedRace) {
        boolean result = false;
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            RaceLogTrackingState raceLogTrackingState = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
            if (raceLogTrackingState.isTracking()) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    @Override
    public SliceRacePreperationDTO prepareForSlicingOfRace(final RegattaAndRaceIdentifier raceIdentifier) {
        final Leaderboard regattaLeaderboard = getService().getLeaderboardByName(raceIdentifier.getRegattaName());
        String prefix = null;
        int currentCount = 0;
        final Pattern pattern = Pattern.compile("^([a-zA-Z_ -]+)([0-9]+)$");
        final HashSet<String> alreadyUsedRaceNames = new HashSet<>();
        for (RaceColumn column : regattaLeaderboard.getRaceColumns()) {
            alreadyUsedRaceNames.add(column.getName());
            final Matcher matcher = pattern.matcher(column.getName());
            if (matcher.matches()) {
                prefix = matcher.group(1);
                currentCount = Integer.parseInt(matcher.group(2));
            }
        }
        if (prefix == null) {
            prefix = "R";
        }
        currentCount++;
        return new SliceRacePreperationDTO(prefix + currentCount, alreadyUsedRaceNames);
    }
    
    @Override
    public RegattaAndRaceIdentifier sliceRace(RegattaAndRaceIdentifier raceIdentifier, String newRaceColumnName,
            TimePoint sliceFrom, TimePoint sliceTo) throws ServiceException {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, raceIdentifier.getRegattaName()));
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, raceIdentifier.getRegattaName()));
        final Locale locale = getClientLocale();
        if (!canSliceRace(raceIdentifier)) {
            throw new ServiceException(serverStringMessages.get(locale, "slicingCannotSliceRace"));
        }
        final String trackedRaceName = newRaceColumnName;
        final RegattaIdentifier regattaIdentifier = new RegattaName(raceIdentifier.getRegattaName());
        final Regatta regatta = getService().getRegatta(regattaIdentifier);
        final Leaderboard regattaLeaderboard = getService().getLeaderboardByName(regatta.getName());
        if (regattaLeaderboard == null) {
            throw new IllegalArgumentException("Cannot slice a race for which no regatta leaderboard exists");
        }
        if (regatta.getRaceColumnByName(newRaceColumnName) != null) {
            throw new ServiceException(serverStringMessages.get(locale, "slicingRaceColumnAlreadyUsedThe"));
        }
        final DynamicTrackedRace trackedRaceToSlice = getService().getTrackedRace(raceIdentifier);
        final TimePoint startOfTrackingOfRaceToSlice = trackedRaceToSlice.getStartOfTracking();
        final TimePoint endOfTrackingOfRaceToSlice = trackedRaceToSlice.getEndOfTracking();
        if (sliceFrom == null || sliceTo == null || startOfTrackingOfRaceToSlice.after(sliceFrom)
                || (endOfTrackingOfRaceToSlice != null && endOfTrackingOfRaceToSlice.before(sliceTo))) {
            throw new ServiceException(serverStringMessages.get(locale, "slicingTimeRangeOutOfBounds"));
        }
        final Pair<RaceColumn, Fleet> raceColumnAndFleetOfRaceToSlice = regatta.getRaceColumnAndFleet(trackedRaceToSlice);
        // RaceColumns in a RegattaLeaderboard are always RaceColumnInSeries instances
        final RaceColumnInSeries raceColumnOfRaceToSlice = (RaceColumnInSeries) raceColumnAndFleetOfRaceToSlice.getA();
        final Fleet fleet = raceColumnAndFleetOfRaceToSlice.getB();
        final Series series = raceColumnOfRaceToSlice.getSeries();
        final RaceLog raceLogOfRaceToSlice = raceColumnOfRaceToSlice.getRaceLog(fleet);
        final RaceColumn raceColumn = getService().apply(new AddColumnToSeries(regattaIdentifier, series.getName(), newRaceColumnName));
        final RaceLog raceLog = raceColumn.getRaceLog(fleet);
        final AbstractLogEventAuthor author = getService().getServerAuthor();
        final TimePoint startOfTracking = sliceFrom;
        final TimePoint endOfTracking = sliceTo;
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
        final TimeRange timeRange = new TimeRangeImpl(sliceFrom, sliceTo);
        final StartTimeFinderResult startTimeFinderResult = new StartTimeFinder(getService(), raceLogOfRaceToSlice).analyze();
        final TimePoint startTime = startTimeFinderResult.getStartTime();
        final boolean hasStartTime = startTime != null && timeRange.includes(startTime);
        final boolean dependentStartTime = startTimeFinderResult.isDependentStartTime();
        final boolean hasFinishingTime;
        final boolean hasFinishedTime;
        if (hasStartTime) {
            final TimePoint finishingTime = new FinishingTimeFinder(raceLog).analyze();
            hasFinishingTime = finishingTime != null && timeRange.includes(finishingTime);
            if (hasFinishingTime) {
                final TimePoint finishedTime = new FinishedTimeFinder(raceLog).analyze();
                hasFinishedTime = finishedTime != null && timeRange.includes(finishedTime);
            } else {
                hasFinishedTime = false;
            }
        } else {
            hasFinishingTime = false;
            hasFinishedTime = false;
        }
        
        // Only wind fixes in the new tracking interval as well as the best fallback fixes are added to the new RaceLog
        final LogEventTimeRangeWithFallbackFilter<RaceLogWindFixEvent> windFixEvents = new LogEventTimeRangeWithFallbackFilter<>(
                timeRange);
        raceLogOfRaceToSlice.lockForRead();
        try {
            for (RaceLogEvent raceLogEvent : raceLogOfRaceToSlice.getUnrevokedEvents()) {
                raceLogEvent.accept(new BaseRaceLogEventVisitor() {
                    @Override
                    public void visit(RaceLogDependentStartTimeEvent event) {
                        if (dependentStartTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogDependentStartTimeEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getDependentOnRaceIdentifier(),
                                    event.getStartTimeDifference(), event.getNextStatus()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogStartTimeEvent event) {
                        if (!dependentStartTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogStartTimeEventImpl(event.getCreatedAt(), event.getLogicalTimePoint(),
                                    event.getAuthor(), UUID.randomUUID(), raceLog.getCurrentPassId(), event.getStartTime(),
                                    event.getNextStatus()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogRegisterCompetitorEvent event) {
                        raceLog.add(new RaceLogRegisterCompetitorEventImpl(event.getCreatedAt(),
                                event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                raceLog.getCurrentPassId(), event.getCompetitor(), event.getBoat()));
                    }
    
                    @Override
                    public void visit(RaceLogWindFixEvent event) {
                        windFixEvents.addEvent(event);
                    }
                    
                    @Override
                    public void visit(RaceLogUseCompetitorsFromRaceLogEvent event) {
                        raceLog.add(new RaceLogUseCompetitorsFromRaceLogEventImpl(event.getCreatedAt(), event.getAuthor(),
                                event.getLogicalTimePoint(), UUID.randomUUID(), raceLog.getCurrentPassId()));
                    }
                    
                    @Override
                    public void visit(RaceLogCourseDesignChangedEvent event) {
                        raceLog.add(new RaceLogCourseDesignChangedEventImpl(event.getCreatedAt(),
                                event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                raceLog.getCurrentPassId(), event.getCourseDesign(), event.getCourseDesignerMode()));
                    }
                    
                    @Override
                    public void visit(RaceLogFlagEvent event) {
                        if (hasStartTime && isLatestPass(event) && !event.getLogicalTimePoint().after(sliceTo)) {
                            raceLog.add(new RaceLogFlagEventImpl(event.getCreatedAt(), event.getLogicalTimePoint(),
                                    event.getAuthor(), UUID.randomUUID(), raceLog.getCurrentPassId(), event.getUpperFlag(),
                                    event.getLowerFlag(), event.isDisplayed()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogStartProcedureChangedEvent event) {
                        if (hasStartTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogStartProcedureChangedEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getStartProcedureType()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogFinishPositioningConfirmedEvent event) {
                        if (hasFinishedTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogFinishPositioningConfirmedEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getPositionedCompetitorsIDsNamesMaxPointsReasons()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogFinishPositioningListChangedEvent event) {
                        if (hasFinishedTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogFinishPositioningListChangedEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getPositionedCompetitorsIDsNamesMaxPointsReasons()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogFixedMarkPassingEvent event) {
                        if (hasStartTime && isLatestPass(event) && timeRange.includes(event.getTimePointOfFixedPassing())) {
                            raceLog.add(new RaceLogFixedMarkPassingEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    event.getInvolvedCompetitors(), raceLog.getCurrentPassId(),
                                    event.getTimePointOfFixedPassing(), event.getZeroBasedIndexOfPassedWaypoint()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogSuppressedMarkPassingsEvent event) {
                        if (hasStartTime && isLatestPass(event) && timeRange.includes(event.getLogicalTimePoint())) {
                            raceLog.add(new RaceLogSuppressedMarkPassingsEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    event.getInvolvedCompetitors(), raceLog.getCurrentPassId(),
                                    event.getZeroBasedIndexOfFirstSuppressedWaypoint()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogProtestStartTimeEvent event) {
                        if (hasFinishedTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogProtestStartTimeEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getProtestTime()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogAdditionalScoringInformationEvent event) {
                        if (hasFinishedTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogAdditionalScoringInformationEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getType()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogPathfinderEvent event) {
                        if (hasStartTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogPathfinderEventImpl(event.getCreatedAt(), event.getLogicalTimePoint(),
                                    event.getAuthor(), UUID.randomUUID(), raceLog.getCurrentPassId(),
                                    event.getPathfinderId()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogGateLineOpeningTimeEvent event) {
                        if (hasStartTime && isLatestPass(event)) {
                            raceLog.add(new RaceLogGateLineOpeningTimeEventImpl(event.getCreatedAt(),
                                    event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(),
                                    raceLog.getCurrentPassId(), event.getGateLineOpeningTimes().getGateLaunchStopTime(),
                                    event.getGateLineOpeningTimes().getGolfDownTime()));
                        }
                    }
                    
                    @Override
                    public void visit(RaceLogRaceStatusEvent event) {
                        if (isLatestPass(event) && !(event instanceof RaceLogDependentStartTimeEvent)
                                && !(event instanceof RaceLogStartTimeEvent)) {
                            if ((hasStartTime
                                    && event.getNextStatus().getOrderNumber() <= RaceLogRaceStatus.RUNNING.getOrderNumber())
                                    || (hasFinishingTime && event.getNextStatus() == RaceLogRaceStatus.FINISHING)
                                    || (hasFinishedTime && event.getNextStatus() == RaceLogRaceStatus.FINISHED)) {
                                raceLog.add(new RaceLogRaceStatusEventImpl(event.getCreatedAt(), event.getLogicalTimePoint(),
                                        event.getAuthor(), UUID.randomUUID(), raceLog.getCurrentPassId(),
                                        event.getNextStatus()));
                            }
                        }
                    }
                    
                    private boolean isLatestPass(RaceLogEvent event) {
                        return event.getPassId() == raceLogOfRaceToSlice.getCurrentPassId();
                    }
                });
            }
        } finally {
            raceLogOfRaceToSlice.unlockAfterRead();
        }
        windFixEvents.getFilteredEvents()
                .forEach(event -> raceLog.add(new RaceLogWindFixEventImpl(event.getCreatedAt(),
                        event.getLogicalTimePoint(), event.getAuthor(), UUID.randomUUID(), raceLog.getCurrentPassId(),
                        event.getWindFix(), event.isMagnetic())));
        final TimePoint startTrackingTimePoint = MillisecondsTimePoint.now();
        // this ensures that the events consistently have different timepoints to ensure a consistent result of the state analysis
        // that's why we can't just call adapter.denoteRaceForRaceLogTracking
        final TimePoint denotationTimePoint = startTrackingTimePoint.minus(1);
        raceLog.add(new RaceLogDenoteForTrackingEventImpl(denotationTimePoint,
                author, raceLog.getCurrentPassId(), trackedRaceName, regatta.getBoatClass(), UUID.randomUUID()));
        raceLog.add(new RaceLogStartTrackingEventImpl(startTrackingTimePoint, author, raceLog.getCurrentPassId()));
        try {
            final RaceHandle raceHandle = getRaceLogTrackingAdapter().startTracking(getService(), regattaLeaderboard,
                    raceColumn, fleet, /* trackWind */ true, /* correctWindDirectionByMagneticDeclination */ true);
            
            // wait for the RaceDefinition to be created
            raceHandle.getRace();

            final DynamicTrackedRace trackedRace = WaitForTrackedRaceUtil.waitForTrackedRace(raceColumn, fleet, 10);
            if (trackedRace == null) {
                throw new ServiceException(serverStringMessages.get(locale, "slicingCouldNotObtainRace"));
            }
            for (WindSource windSourceToCopy : trackedRaceToSlice.getWindSources()) {
                if (windSourceToCopy.canBeStored()) {
                    final WindTrack windTrackToCopyFrom = trackedRaceToSlice.getOrCreateWindTrack(windSourceToCopy);
                    windTrackToCopyFrom.lockForRead();
                    try {
                        for (Wind windToCopy : windTrackToCopyFrom.getFixes(startOfTracking, true, endOfTracking, true)) {
                            trackedRace.recordWind(windToCopy, windSourceToCopy);
                        }
                    } finally {
                        windTrackToCopyFrom.unlockAfterRead();
                    }
                }
            }
            
            final Iterable<MediaTrack> mediaTracksForOriginalRace = getService().getMediaTracksForRace(raceIdentifier);
            for (MediaTrack mediaTrack : mediaTracksForOriginalRace) {
                if (mediaTrack.overlapsWith(sliceFrom, sliceTo)) {
                    final Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<>(mediaTrack.assignedRaces);
                    assignedRaces.add(trackedRace.getRaceIdentifier());
                    // we can't just use the original instance and add the Race due to the fact that this leads to
                    // assignedRaces being empty afterwards.
                    final MediaTrack mediaTrackToSave = new MediaTrack(mediaTrack.dbId, mediaTrack.title,
                            mediaTrack.url, mediaTrack.startTime, mediaTrack.duration, mediaTrack.mimeType,
                            assignedRaces);
                    getService().mediaTrackAssignedRacesChanged(mediaTrackToSave);
                }
            }
            return trackedRace.getRaceIdentifier();
        } catch (Exception e) {
            throw new ServiceException(serverStringMessages.get(locale, "slicingError"));
        }
    }

    @Override
    public Boolean checkIfRaceIsTracking(RegattaAndRaceIdentifier race) {
        boolean result = false;
        DynamicTrackedRace trace = getService().getTrackedRace(race);
        if (trace != null) {
            final TrackedRaceStatusEnum status = trace.getStatus().getStatus();
            if (status == TrackedRaceStatusEnum.LOADING || status == TrackedRaceStatusEnum.TRACKING) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        ShardingType identifiedShardingType = null;
        try {
            if (req instanceof HttpServletRequest) {
                identifiedShardingType = ShardingContext.identifyAndSetShardingConstraint(((HttpServletRequest) req).getPathInfo());
            }
            super.service(req, res);
        } finally {
            if (identifiedShardingType != null) {
                ShardingContext.clearShardingConstraint(identifiedShardingType);
            }
        }
    }

    @Override
    public Set<ImageDTO> resizeImage(final ImageResizingTaskDTO resizingTask) throws Exception {
        if (resizingTask.getResizingTask() == null || resizingTask.getResizingTask().size() == 0) {
            throw new InvalidAttributeValueException("Resizing Task can not be null or empty");
        }
        final ImageConverter converter = new ImageConverter();
        // calculating the fileType of the image by its uri
        final String sourceRef = resizingTask.getImage().getSourceRef();
        final String fileType = sourceRef.substring(sourceRef.lastIndexOf(".") + 1);
        final ImageWithMetadata imageAndMetadata = converter
                .loadImage(HttpUrlConnectionHelper.redirectConnection(new URL(sourceRef)).getInputStream(), fileType);
        final List<BufferedImage> resizedImages = converter.convertImage(imageAndMetadata.getImage(),
                resizingTask.getResizingTask());
        final List<String> sourceRefs = storeImages(resizedImages, fileType, imageAndMetadata.getMetadata());
        // if an error occures while storing the files, all already stored files are removed before throwing an
        // exception
        if (sourceRefs == null || sourceRefs.size() < resizingTask.getResizingTask().size()) {
            for (String alreadyStoredFileRef : sourceRefs) {
                try {
                    getService().getFileStorageManagementService().getActiveFileStorageService()
                            .removeFile(new URI(alreadyStoredFileRef));
                } catch (Exception e) {
                }
                // Exception occured while trying to revert changes after exception
                // This only keeps some trash on the FileStorage
            }
            throw new Exception("Error occured while storing images on the FileStorage");
        }
        final Set<ImageDTO> resizedImagesAsDTOs = createImageDTOsFromURLsAndResizingTask(sourceRefs, resizingTask,
                resizedImages);
        for (String tag : resizingTask.getImage().getTags()) {
            final MediaTagConstants predefinedTag = MediaTagConstants.fromName(tag);
            if (predefinedTag != null && !resizingTask.getResizingTask().contains(predefinedTag)) {
                final ImageDTO image = resizingTask.getImage();
                for (MediaTagConstants tagConstant : resizingTask.getResizingTask()) {
                    image.getTags().remove(tagConstant.getName());
                }
                resizedImagesAsDTOs.add(image);
            }
        }
        return resizedImagesAsDTOs;
    }

    /**
     * Takes a list of source URLs, the resizing task and the sizes of the resized images to create a ImageDTO for every
     * resized image
     * 
     * @author Robin Fleige (D067799)
     * 
     * @param sourceRefs
     *            list of source URLs
     * @param resizingTask
     *            the resizing task, with information about resizes and the original ImageDTO
     * @param images
     *            the BufferedImages, used to get their width and height
     * @returns a List of ImageDTOs that contains an ImageDTO per resized image
     */
    private Set<ImageDTO> createImageDTOsFromURLsAndResizingTask(final List<String> sourceRefs,
            final ImageResizingTaskDTO resizingTask, final List<BufferedImage> images) {
        final Set<ImageDTO> imageDTOs = new HashSet<ImageDTO>();
        for (int i = 0; i < sourceRefs.size(); i++) {
            final ImageDTO imageDTO = resizingTask.cloneImageDTO();
            for (MediaTagConstants tag : MediaTagConstants.values()) {
                imageDTO.getTags().remove(tag.getName());
            }
            imageDTO.getTags().add(resizingTask.getResizingTask().get(i).getName());
            imageDTO.setSourceRef(sourceRefs.get(i));
            imageDTO.setSizeInPx(images.get(i).getWidth(), images.get(i).getHeight());
            imageDTOs.add(imageDTO);
        }
        return imageDTOs;
    }

    /**
     * Stores a list of BufferedImages and returns a list of URLs as Strings under which the BufferedImages are stored
     * 
     * @author Robin Fleige (D067799)
     * 
     * @param resizedImages
     *            the BufferedImages that will be stored
     * @param fileType
     *            the format of the image, for example "png", "jpeg" or "jpg"
     * @param metadata
     *            the metadata of the original image
     * @returns a list of URLs as Strings under which the BufferedImages are stored
     */
    private List<String> storeImages(final List<BufferedImage> resizedImages, final String fileType,
            final IIOMetadata metadata) {
        final List<String> sourceRefs = new ArrayList<>();

        try {
            for (final BufferedImage resizedImage : resizedImages) {
                final InputStream fileStorageStream = new ImageConverter().imageWithMetadataToInputStream(resizedImage,
                        metadata, fileType);
                sourceRefs.add(getService().getFileStorageManagementService().getActiveFileStorageService()
                        .storeFile(fileStorageStream, "." + fileType, new Long(fileStorageStream.available()))
                        .toString());
            }
        } catch (NoCorrespondingServiceRegisteredException | IOException | OperationFailedException
                | InvalidPropertiesException e) {
            logger.log(Level.SEVERE, "Could not store file. Cause: " + e.getMessage());
        }
        return sourceRefs;
    }
    
    @Override
    public SuccessInfo addTag(String leaderboardName, String raceColumnName, String fleetName, String tag,
            String comment, String imageURL, String resizedImageURL, boolean visibleForPublic,
            TimePoint raceTimepoint) {
        SuccessInfo successInfo = new SuccessInfo(true, null, null, null);
        try {
            getService().getTaggingService().addTag(leaderboardName, raceColumnName, fleetName, tag, comment, imageURL,
                    resizedImageURL, visibleForPublic, raceTimepoint);
        } catch (AuthorizationException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "missingAuthorization"),
                    null, null);
        } catch (IllegalArgumentException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "invalidParameters"), null,
                    null);
        } catch (RaceLogNotFoundException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "raceLogNotFound"), null,
                    null);
        } catch (ServiceNotFoundException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "securityServiceNotFound"),
                    null, null);
        } catch (TagAlreadyExistsException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "tagAlreadyExists"), null,
                    null);
        } catch (Exception e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "unknownError"), null,
                    null);
        }
        return successInfo;
    }

    @Override
    public SuccessInfo removeTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag) {
        SuccessInfo successInfo = new SuccessInfo(true, null, null, null);
        try {
            getService().getTaggingService().removeTag(leaderboardName, raceColumnName, fleetName, tag);
        } catch (AuthorizationException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "missingAuthorization"),
                    null, null);
        } catch (IllegalArgumentException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "invalidParameters"), null,
                    null);
        } catch (NotRevokableException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "tagNotRevokable"), null,
                    null);
        } catch (RaceLogNotFoundException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "raceLogNotFound"), null,
                    null);
        } catch (Exception e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "unknownError"), null,
                    null);
        }
        return successInfo;
    }

    @Override
    public SuccessInfo updateTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tagToUpdate,
            String tag, String comment, String imageURL, String resizedImageURL, boolean visibleForPublic) {
        SuccessInfo successInfo = new SuccessInfo(true, null, null, null);
        try {
            getService().getTaggingService().updateTag(leaderboardName, raceColumnName, fleetName, tagToUpdate, tag,
                    comment, imageURL, resizedImageURL, visibleForPublic);
        } catch (AuthorizationException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "missingAuthorization"),
                    null, null);
        } catch (IllegalArgumentException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "invalidParameters"), null,
                    null);
        } catch (NotRevokableException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "tagNotRevokable"), null,
                    null);
        } catch (RaceLogNotFoundException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "raceLogNotFound"), null,
                    null);
        } catch (TagAlreadyExistsException e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "tagAlreadyExists"), null,
                    null);
        } catch (Exception e) {
            successInfo = new SuccessInfo(false, serverStringMessages.get(getClientLocale(), "unknownError"), null,
                    null);
        }
        return successInfo;
    }

    @Override
    public List<TagDTO> getAllTags(String leaderboardName, String raceColumnName, String fleetName) {
        List<TagDTO> result = new ArrayList<TagDTO>();
        result.addAll(getPublicTags(leaderboardName, raceColumnName, fleetName));
        result.addAll(getPrivateTags(leaderboardName, raceColumnName, fleetName));
        return result;
    }

    @Override
    public List<TagDTO> getPublicTags(String leaderboardName, String raceColumnName, String fleetName) {
        List<TagDTO> result = new ArrayList<TagDTO>();
        try {
            result.addAll(getService().getTaggingService().getPublicTags(leaderboardName, raceColumnName, fleetName, null, false));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem obtaining public tags for leaderboard "+leaderboardName+", race column "+raceColumnName+
                    ", fleet "+fleetName, e);
            // do nothing as method will always return at least an empty list
        }
        return result;
    }

    @Override
    public List<TagDTO> getPrivateTags(String leaderboardName, String raceColumnName, String fleetName) {
        List<TagDTO> result = new ArrayList<TagDTO>();
        try {
            result.addAll(getService().getTaggingService().getPrivateTags(leaderboardName, raceColumnName, fleetName));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Problem obtaining private tags for leaderboard "+leaderboardName+", race column "+raceColumnName+
                    ", fleet "+fleetName, e);
            // do nothing as method will always return at least an empty list
        }
        return result;
    }

    @Override
    public RaceTimesInfoDTO getRaceTimesInfoIncludingTags(RegattaAndRaceIdentifier raceIdentifier,
            TimePoint searchSince) {
        RaceTimesInfoDTO raceTimesInfo = getRaceTimesInfo(raceIdentifier);
        raceTimesInfo.setTags(getService().getTaggingService().getPublicTags(raceIdentifier, searchSince));
        return raceTimesInfo;
    }

    @Override
    public List<RaceTimesInfoDTO> getRaceTimesInfosIncludingTags(Collection<RegattaAndRaceIdentifier> raceIdentifiers,
            Map<RegattaAndRaceIdentifier, TimePoint> searchSinceMap) {
        List<RaceTimesInfoDTO> raceTimesInfos = new ArrayList<RaceTimesInfoDTO>();
        for (RegattaAndRaceIdentifier raceIdentifier : raceIdentifiers) {
            RaceTimesInfoDTO raceTimesInfo = getRaceTimesInfoIncludingTags(raceIdentifier,
                    searchSinceMap.get(raceIdentifier));
            if (raceTimesInfo != null) {
                raceTimesInfos.add(raceTimesInfo);
            }
        }
        return raceTimesInfos;
    }

    @Override
    public String openRegattaRegistrationQrCode(String url) {
        String result = "";
        try (DataInputStream imageIs = new DataInputStream(QRCodeGenerationUtil.create(url, 600, "H"))) {
            byte[] targetArray = new byte[imageIs.available()];
            imageIs.readFully(targetArray);
            result = Base64Utils.toBase64(targetArray);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while generating QR code for open regatta", e);
        }
        return result;
    }

    @Override
    public void setDefaultTenantForCurrentServer(String tennant) {
        User user = getSecurityService().getCurrentUser();
        if (user != null) {
            if (tennant == null) {
                user.getDefaultTenantMap().remove(ServerInfo.getName());
            } else {
                UserGroup userGroupCandidate = getSecurityService().getUserGroupByName(tennant);
                // FIXME allow admins to pose as any tenant?
                if (Util.contains(user.getUserGroups(), userGroupCandidate)) {
                    user.getDefaultTenantMap().put(ServerInfo.getName(), userGroupCandidate);
                }
            }
        }
    }

    @Override
    public List<String> getPossibleTennants() {
        // FIXME this is most likely greatly oversimplified and needs to be improved
        ArrayList<String> tenants = new ArrayList<>();
        for (UserGroup group : getSecurityService().getCurrentUser().getUserGroups()) {
            tenants.add(group.getName());
        }
        return tenants;
    }

    private Boolean isSelfServiceServer() {
        final Boolean result;
        final User allUser = getSecurityService().getAllUser();
        if (allUser != null) {
            result = Util.contains(allUser.getPermissions(), SecuredSecurityTypes.SERVER
                    .getPermissionForObjects(ServerActions.CREATE_OBJECT, ServerInfo.getName()));
        } else {
            result = null;
        }
        return result;
    }

    private Boolean isPublicServer() {
        final Boolean result;
        final User allUser = getSecurityService().getAllUser();
        final RoleDefinition viewerRole = getSecurityService()
                .getRoleDefinition(SailingViewerRole.getInstance().getId());
        final UserGroup defaultServerTenant = getSecurityService().getDefaultTenant();
        if (allUser != null && viewerRole != null && defaultServerTenant != null) {
            result = allUser.hasRole(new RoleImpl(viewerRole, defaultServerTenant, null));
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public void updateGroupOwnerForEventHierarchy(UUID eventId,
            MigrateGroupOwnerForHierarchyDTO migrateGroupOwnerForHierarchyDTO) {
        Event event = getService().getEvent(eventId);
        if (event != null) {
            createOwnershipUpdater(migrateGroupOwnerForHierarchyDTO).updateGroupOwnershipForEventHierarchy(event);
        }
    }

    @Override
    public void updateGroupOwnerForLeaderboardGroupHierarchy(UUID leaderboardGroupId,
            MigrateGroupOwnerForHierarchyDTO migrateGroupOwnerForHierarchyDTO) {
        LeaderboardGroup leaderboardGroup = getService().getLeaderboardGroupByID(leaderboardGroupId);
        if (leaderboardGroup != null) {
            createOwnershipUpdater(migrateGroupOwnerForHierarchyDTO)
                    .updateGroupOwnershipForLeaderboardGroupHierarchy(leaderboardGroup);
        }
    }

    private SailingHierarchyOwnershipUpdater createOwnershipUpdater(
            MigrateGroupOwnerForHierarchyDTO migrateGroupOwnerForHierarchyDTO) {
        final GroupOwnerUpdateStrategy updateStrategy;
        if (!migrateGroupOwnerForHierarchyDTO.isCreateNewGroup()) {
            final UserGroup groupOwnerToSet = getSecurityService()
                    .getUserGroup(migrateGroupOwnerForHierarchyDTO.getExistingUserGroup().getId());
            if (groupOwnerToSet == null) {
                throw new RuntimeException("User group does not exist");
            }
            updateStrategy = new GroupOwnerUpdateStrategy() {
                @Override
                public boolean needsUpdate(QualifiedObjectIdentifier identifier, OwnershipAnnotation currentOwnership) {
                    return currentOwnership == null
                            || !groupOwnerToSet.equals(currentOwnership.getAnnotation().getTenantOwner());
                }

                @Override
                public UserGroup getNewGroupOwner() {
                    return groupOwnerToSet;
                }
            };
        } else if (migrateGroupOwnerForHierarchyDTO.getGroupName() != null
                && !migrateGroupOwnerForHierarchyDTO.getGroupName().isEmpty()) {
            updateStrategy = new GroupOwnerUpdateStrategy() {
                private UserGroup groupOwnerToSet;

                @Override
                public boolean needsUpdate(QualifiedObjectIdentifier identifier, OwnershipAnnotation currentOwnership) {
                    return true;
                }

                @Override
                public UserGroup getNewGroupOwner() {
                    if (groupOwnerToSet == null) {
                        try {
                            final UserGroup existingUserGroup = migrateGroupOwnerForHierarchyDTO.getExistingUserGroup();
                            if (existingUserGroup != null) {
                                // When migrating from an existing user group -> copy as much as possible from the
                                // existing group to make the migrated objects to be visible for most people as before
                                groupOwnerToSet = copyUserGroup(existingUserGroup,
                                        migrateGroupOwnerForHierarchyDTO.getGroupName());
                            } else {
                                // The migration may start at an object that currently has no group owner (e.g. in case
                                // this owner was just deleted) -> in this case we just create a new group
                                groupOwnerToSet = getSecurityService().createUserGroup(UUID.randomUUID(),
                                        migrateGroupOwnerForHierarchyDTO.getGroupName());
                            }
                        } catch (UserGroupManagementException e) {
                            throw new RuntimeException("Could not create user group");
                        }
                    }
                    return groupOwnerToSet;
                }
            };
        } else {
            throw new RuntimeException("No user group given");
        }
        return new SailingHierarchyOwnershipUpdater(getService(), getSecurityService(), updateStrategy,
                migrateGroupOwnerForHierarchyDTO.isUpdateCompetitors(),
                migrateGroupOwnerForHierarchyDTO.isUpdateBoats());
    }

    private UserGroup copyUserGroup(UserGroup userGroup, String name) throws UserGroupManagementException {
        // explicitly loading the current version of the group in case the given instance e.g. originates from the UI
        // and is possible out of date.
        final UserGroup userGroupToCopy = getSecurityService().getUserGroup(userGroup.getId());
        if (userGroupToCopy == null) {
            throw new IllegalArgumentException("User group does not exist");
        }
        final UUID newGroupId = UUID.randomUUID();
        return getSecurityService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(
                SecuredSecurityTypes.USER_GROUP, newGroupId.toString(), name, () -> {
                    final UserGroup createdUserGroup = getSecurityService().createUserGroup(newGroupId, name);
                    getSecurityService().copyUsersAndRoleAssociations(userGroupToCopy, createdUserGroup);
                    return createdUserGroup;
                });
    }
}
