package com.sap.sailing.gwt.ui.server;

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
import java.text.ParseException;
import java.util.ArrayList;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletContext;

import org.apache.http.client.ClientProtocolException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.MultiLogAnalyzer;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.ReadonlyRRS26RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogCloseOpenEndedDeviceMappingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DefinedMarkFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogOpenEndedDeviceMappingCloser;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEvent;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceCompetitorMappingFinder;
import com.sap.sailing.domain.abstractlog.shared.analyzing.DeviceMarkMappingFinder;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherMulti;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationMatcherSingle;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.domain.base.impl.DynamicPerson;
import com.sap.sailing.domain.base.impl.DynamicTeam;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceFetcher;
import com.sap.sailing.domain.common.RaceIdentifier;
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
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.FullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalLeaderboardDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTOFactory;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RaceLogTrackingInfoDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.PolarSheetGenerationResponseImpl;
import com.sap.sailing.domain.common.impl.PolarSheetsXYDiagramDataImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.MappableToDevice;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.igtimiadapter.Account;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnection;
import com.sap.sailing.domain.igtimiadapter.IgtimiConnectionFactory;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.leaderboard.caching.LiveLeaderboardUpdater;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoRaceLogStoreFactory;
import com.sap.sailing.domain.persistence.MongoRegattaLogStoreFactory;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.RaceStateOfSameDayHelper;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.domain.racelogtracking.DeviceMapping;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.DeviceMappingImpl;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
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
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPassingManeuver;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.domain.tractracadapter.TracTracAdapter;
import com.sap.sailing.domain.tractracadapter.TracTracAdapterFactory;
import com.sap.sailing.domain.tractracadapter.TracTracConfiguration;
import com.sap.sailing.domain.tractracadapter.TracTracConnectionConstants;
import com.sap.sailing.gwt.ui.client.SailingService;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.DeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.GateDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupBaseDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;
import com.sap.sailing.gwt.ui.shared.LegInfoDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MarkPassingTimesDTO;
import com.sap.sailing.gwt.ui.shared.MarkpassingManeuverDTO;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.GateStartInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.RRS26InfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceInfoDTO.RaceInfoExtensionDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogEventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO.ScoreCorrectionEntryDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sailing.gwt.ui.shared.ReplicaDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationMasterDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.SidelineDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
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
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.manage2sail.EventResultDescriptor;
import com.sap.sailing.manage2sail.Manage2SailEventResultsParserImpl;
import com.sap.sailing.manage2sail.RaceResultDescriptor;
import com.sap.sailing.manage2sail.RegattaResultDescriptor;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.polars.regression.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.resultimport.ResultUrlProvider;
import com.sap.sailing.resultimport.ResultUrlRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.masterdata.MasterDataImporter;
import com.sap.sailing.server.operationaltransformation.AddColumnToLeaderboard;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddCourseArea;
import com.sap.sailing.server.operationaltransformation.AddRemoteSailingServerReference;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.AllowCompetitorResetToDefaults;
import com.sap.sailing.server.operationaltransformation.ConnectTrackedRaceToLeaderboardColumn;
import com.sap.sailing.server.operationaltransformation.CreateEvent;
import com.sap.sailing.server.operationaltransformation.CreateFlexibleLeaderboard;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.DisconnectLeaderboardColumnFromTrackedRace;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesDown;
import com.sap.sailing.server.operationaltransformation.MoveColumnInSeriesUp;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnDown;
import com.sap.sailing.server.operationaltransformation.MoveLeaderboardColumnUp;
import com.sap.sailing.server.operationaltransformation.RemoveAndUntrackRace;
import com.sap.sailing.server.operationaltransformation.RemoveColumnFromSeries;
import com.sap.sailing.server.operationaltransformation.RemoveCourseArea;
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
import com.sap.sailing.server.operationaltransformation.StopTrackingRegatta;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitor;
import com.sap.sailing.server.operationaltransformation.UpdateCompetitorDisplayNameInLeaderboard;
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
import com.sap.sailing.server.operationaltransformation.UpdateSpecificRegatta;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.PolarDiagram;
import com.sap.sailing.simulator.SailingSimulator;
import com.sap.sailing.simulator.SimulationParameters;
import com.sap.sailing.simulator.TimedPositionWithSpeed;
import com.sap.sailing.simulator.impl.PolarDiagramGPS;
import com.sap.sailing.simulator.impl.SailingSimulatorImpl;
import com.sap.sailing.simulator.impl.SimulationParametersImpl;
import com.sap.sailing.simulator.util.SailingSimulatorConstants;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;
import com.sap.sailing.simulator.windfield.impl.WindFieldTrackedRaceImpl;
import com.sap.sailing.xrr.schema.RegattaResults;
import com.sap.sailing.xrr.structureimport.SeriesParameters;
import com.sap.sailing.xrr.structureimport.StructureImporter;
import com.sap.sailing.xrr.structureimport.buildstructure.SetRacenumberFromSeries;
import com.sap.sse.BuildVersion;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.TimeRangeImpl;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.search.KeywordQuery;
import com.sap.sse.common.search.Result;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.InvalidPropertiesException;
import com.sap.sse.gwt.server.filestorage.FileStorageServiceDTOUtils;
import com.sap.sse.gwt.shared.filestorage.FileStorageServiceDTO;
import com.sap.sse.gwt.shared.filestorage.FileStorageServicePropertyErrorsDTO;
import com.sap.sse.i18n.ResourceBundleStringMessages;
import com.sap.sse.replication.OperationWithResult;
import com.sap.sse.replication.ReplicationFactory;
import com.sap.sse.replication.ReplicationMasterDescriptor;
import com.sap.sse.replication.ReplicationService;
import com.sap.sse.replication.impl.ReplicaDescriptor;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sapsailing.xrr.structureimport.eventimport.RegattaJSON;


/**
 * The server side implementation of the RPC service.
 */
public class SailingServiceImpl extends ProxiedRemoteServiceServlet implements SailingService, RaceFetcher, RegattaFetcher {
    private static final Logger logger = Logger.getLogger(SailingServiceImpl.class.getName());

    private static final long serialVersionUID = 9031688830194537489L;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    private final ServiceTracker<ReplicationService, ReplicationService> replicationServiceTracker;

    private final ServiceTracker<ResultUrlRegistry, ResultUrlRegistry> resultUrlRegistryServiceTracker;

    private final ServiceTracker<ScoreCorrectionProvider, ScoreCorrectionProvider> scoreCorrectionProviderServiceTracker;

    private final MongoObjectFactory mongoObjectFactory;

    private final SwissTimingAdapterPersistence swissTimingAdapterPersistence;
    
    private final ServiceTracker<SwissTimingAdapterFactory, SwissTimingAdapterFactory> swissTimingAdapterTracker;

    private final ServiceTracker<TracTracAdapterFactory, TracTracAdapterFactory> tractracAdapterTracker;

    private final ServiceTracker<IgtimiConnectionFactory, IgtimiConnectionFactory> igtimiAdapterTracker;

    private final ServiceTracker<RaceLogTrackingAdapterFactory, RaceLogTrackingAdapterFactory> raceLogTrackingAdapterTracker;
    
    private final ServiceTracker<DeviceIdentifierStringSerializationHandler, DeviceIdentifierStringSerializationHandler>
    deviceIdentifierStringSerializationHandlerTracker;
    
    private final com.sap.sailing.domain.tractracadapter.persistence.MongoObjectFactory tractracMongoObjectFactory;

    private final DomainObjectFactory domainObjectFactory;

    private final SwissTimingFactory swissTimingFactory;

    private final com.sap.sailing.domain.tractracadapter.persistence.DomainObjectFactory tractracDomainObjectFactory;

    private final com.sap.sse.common.CountryCodeFactory countryCodeFactory;

    private final Executor executor;
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private static final int LEADERBOARD_BY_NAME_RESULTS_CACHE_BY_ID_SIZE = 100;
    
    private static final int LEADERBOARD_DIFFERENCE_CACHE_SIZE = 50;

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
        if (context != null) {
            activator.setSailingService(this); // register so this service is informed when the bundle shuts down
        }
        quickRanksLiveCache = new QuickRanksLiveCache(this);
        racingEventServiceTracker = ServiceTrackerFactory.createAndOpen(context, RacingEventService.class);
        replicationServiceTracker = ServiceTrackerFactory.createAndOpen(context, ReplicationService.class);
        resultUrlRegistryServiceTracker = ServiceTrackerFactory.createAndOpen(context, ResultUrlRegistry.class);
        swissTimingAdapterTracker = ServiceTrackerFactory.createAndOpen(context, SwissTimingAdapterFactory.class);
        tractracAdapterTracker = ServiceTrackerFactory.createAndOpen(context, TracTracAdapterFactory.class);
        raceLogTrackingAdapterTracker = ServiceTrackerFactory.createAndOpen(context,
                RaceLogTrackingAdapterFactory.class);
        deviceIdentifierStringSerializationHandlerTracker = ServiceTrackerFactory.createAndOpen(context,
                DeviceIdentifierStringSerializationHandler.class);
        igtimiAdapterTracker = ServiceTrackerFactory.createAndOpen(context, IgtimiConnectionFactory.class);
        baseDomainFactory = getService().getBaseDomainFactory();
        mongoObjectFactory = getService().getMongoObjectFactory();
        domainObjectFactory = getService().getDomainObjectFactory();
        // TODO what about passing on the mongo/domain object factory to obtain an according SwissTimingAdapterPersistence instance similar to how the tractracDomainObjectFactory etc. are created below?
        swissTimingAdapterPersistence = SwissTimingAdapterPersistence.INSTANCE;
        swissTimingReplayService = ServiceTrackerFactory.createAndOpen(context, SwissTimingReplayServiceFactory.class)
                .getService().createSwissTimingReplayService(getSwissTimingAdapter().getSwissTimingDomainFactory());
        scoreCorrectionProviderServiceTracker = ServiceTrackerFactory.createAndOpen(context,
                ScoreCorrectionProvider.class);
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
        final int THREAD_POOL_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 3);
        executor = new ThreadPoolExecutor(/* corePoolSize */ THREAD_POOL_SIZE,
                /* maximumPoolSize */ THREAD_POOL_SIZE,
                /* keepAliveTime */ 60, TimeUnit.SECONDS,
                /* workQueue */ new LinkedBlockingQueue<Runnable>());
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
            if(scoreCorrectionProvider.getName().equals(providerName)) {
                result = convertScoreCorrectionProviderDTO(scoreCorrectionProvider);
                break;
            }
        }
        return result;
    }

    private Iterable<ScoreCorrectionProvider> getAllScoreCorrectionProviders() {
        final Object[] services = scoreCorrectionProviderServiceTracker.getServices();
        List<ScoreCorrectionProvider> result = new ArrayList<ScoreCorrectionProvider>();
        if (services != null) {
            for (Object service : services) {
                result.add((ScoreCorrectionProvider) service);
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
            String previousLeaderboardId, boolean fillNetPointsUncorrected) throws NoWindException, InterruptedException, ExecutionException,
            IllegalArgumentException {
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
                        namesOfRaceColumnsForWhichToLoadLegDetails, addOverallDetails, getService(), baseDomainFactory, fillNetPointsUncorrected);
                LeaderboardDTO previousLeaderboardDTO = null;
                synchronized (leaderboardByNameResultsCacheById) {
                    leaderboardByNameResultsCacheById.put(leaderboardDTO.getId(), leaderboardDTO);
                    if (previousLeaderboardId != null) {
                        previousLeaderboardDTO = leaderboardByNameResultsCacheById.get(previousLeaderboardId);
                    }
                }
                // Un-comment the following lines if you need to update the file used by LeaderboardDTODiffingTest, set a breakpoint
                // and toggle the storeLeaderboardForTesting flag if you found a good version. See also bug 1417.
//                boolean storeLeaderboardForTesting = false;
//                if (storeLeaderboardForTesting) {
//                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("c:/data/SAP/sailing/workspace/java/com.sap.sailing.domain.test/resources/IncrementalLeaderboardDTO.ser")));
//                    oos.writeObject(leaderboardDTO);
//                    oos.close();
//                }
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
        List<RegattaDTO> result = new ArrayList<RegattaDTO>();
        for (Regatta regatta : getService().getAllRegattas()) {
            result.add(convertToRegattaDTO(regatta));
        }
        return result;
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
        if(position != null) {
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
            regattaDTO.boatClass = new BoatClassDTO(boatClass.getName(), boatClass.getDisplayName(), boatClass.getHullLength().getMeters());
        }
        if (regatta.getDefaultCourseArea() != null) {
            regattaDTO.defaultCourseAreaUuid = regatta.getDefaultCourseArea().getId();
            regattaDTO.defaultCourseAreaName = regatta.getDefaultCourseArea().getName();
        }
        regattaDTO.useStartTimeInference = regatta.useStartTimeInference();
        regattaDTO.configuration = convertToRegattaConfigurationDTO(regatta.getRegattaConfiguration());
        return regattaDTO;
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
        SeriesDTO result = new SeriesDTO(series.getName(), fleets, raceColumns, series.isMedal(),
                series.getResultDiscardingRule() == null ? null : series.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(),
                        series.isStartsWithZeroScore(), series.isFirstColumnIsNonDiscardableCarryForward(), series.hasSplitFleetContiguousScoring());
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
                    raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getSeries().getName() : null);
            raceColumnDTOs.add(raceColumnDTO);
        }
        return raceColumnDTOs;
    }
    
    private RaceInfoDTO createRaceInfoDTO(String seriesName, RaceColumn raceColumn, Fleet fleet) {
        RaceInfoDTO raceInfoDTO = new RaceInfoDTO();
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null) {
            
            raceInfoDTO.isTracked = raceColumn.getTrackedRace(fleet) != null ? true : false;
            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
            
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
                    TimePoint endOfRace = raceColumn.getTrackedRace(fleet).getEndOfRace();
                    raceInfoDTO.finishedTime = endOfRace != null ? endOfRace.asDate() : null;
                }
            }

            if (startTime != null) {
                FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
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
                
                if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                    raceInfoDTO.lastUpperFlag = abortingFlagEvent.getUpperFlag();
                    raceInfoDTO.lastLowerFlag = abortingFlagEvent.getLowerFlag();
                    raceInfoDTO.lastFlagsAreDisplayed = abortingFlagEvent.isDisplayed();
                    raceInfoDTO.lastFlagsDisplayedStateChanged = true;
                }
            }
            
            CourseBase lastCourse = state.getCourseDesign();
            if (lastCourse != null) {
                raceInfoDTO.lastCourseDesign = convertCourseDesignToRaceCourseDTO(lastCourse);
                raceInfoDTO.lastCourseName = lastCourse.getName();
            }
            
            if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
                TimePoint protestStartTime = state.getProtestTime();
                if (protestStartTime != null) {
                    long protestDuration = 90 * 60 * 1000; // 90 min protest duration
                    raceInfoDTO.protestFinishTime = protestStartTime.plus(protestDuration).asDate();
                    raceInfoDTO.lastUpperFlag = Flags.BRAVO;
                    raceInfoDTO.lastLowerFlag = Flags.NONE;
                    raceInfoDTO.lastFlagsAreDisplayed = true;
                    raceInfoDTO.lastFlagsDisplayedStateChanged = true;
                }
            }
            
            Wind wind = state.getWindFix();
            if (wind != null) {
                raceInfoDTO.lastWind = createWindDTOFromAlreadyAveraged(wind, MillisecondsTimePoint.now());
            }

            fillStartProcedureSpecifics(raceInfoDTO, state);
        }
        raceInfoDTO.seriesName = seriesName;
        raceInfoDTO.raceName = raceColumn.getName();
        raceInfoDTO.fleetName = fleet.getName();
        raceInfoDTO.fleetOrdering = fleet.getOrdering();
        raceInfoDTO.raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        raceInfoDTO.isTracked = raceColumn.getTrackedRace(fleet) != null ? true : false;
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
            ReadonlyRRS26RacingProcedure rrs26 = state.getTypedReadonlyRacingProcedure();
            info = new RRS26InfoDTO(rrs26.getStartModeFlag());
        case UNKNOWN:
        default:
            break;
        }
        raceInfoDTO.startProcedureDTO = info;
    }

    private RaceCourseDTO convertCourseDesignToRaceCourseDTO(CourseBase lastCourseDesign) {
        RaceCourseDTO result = new RaceCourseDTO(Collections.<WaypointDTO> emptyList());
        if (lastCourseDesign != null) {
            List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
            for (Waypoint waypoint : lastCourseDesign.getWaypoints()) {
                ControlPointDTO controlPointDTO = convertToControlPointDTO(waypoint.getControlPoint());
                WaypointDTO waypointDTO = new WaypointDTO(waypoint.getName(), controlPointDTO, waypoint.getPassingInstructions());
                waypointDTOs.add(waypointDTO);
            }
            result = new RaceCourseDTO(waypointDTOs);
        }
        return result;
    }

    private List<RaceWithCompetitorsDTO> convertToRaceDTOs(Regatta regatta) {
        List<RaceWithCompetitorsDTO> result = new ArrayList<RaceWithCompetitorsDTO>();
        for (RaceDefinition r : regatta.getAllRaces()) {
            RegattaAndRaceIdentifier raceIdentifier = new RegattaNameAndRaceName(regatta.getName(), r.getName());
            TrackedRace trackedRace = getService().getExistingTrackedRace(raceIdentifier);
            TrackedRaceDTO trackedRaceDTO = null; 
            if (trackedRace != null) {
                trackedRaceDTO = getBaseDomainFactory().createTrackedRaceDTO(trackedRace);
            }
            RaceWithCompetitorsDTO raceDTO = new RaceWithCompetitorsDTO(raceIdentifier, convertToCompetitorDTOs(r.getCompetitors()),
                    trackedRaceDTO, getService().isRaceBeingTracked(regatta, r));
            if (trackedRace != null) {
                getBaseDomainFactory().updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
            }
            raceDTO.boatClass = regatta.getBoatClass() == null ? null : regatta.getBoatClass().getName(); 
            result.add(raceDTO);
        }
        return result;
    }

    private List<CompetitorDTO> convertToCompetitorDTOs(Iterable<? extends Competitor> iterable) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor c : iterable) {
            CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(c);
            result.add(competitorDTO);
        }
        return result;
    }

    @Override
    public com.sap.sse.common.Util.Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws MalformedURLException, IOException, ParseException, org.json.simple.parser.ParseException, URISyntaxException {
        com.sap.sse.common.Util.Pair<String,List<RaceRecord>> raceRecords;
        raceRecords = getTracTracAdapter().getTracTracRaceRecords(new URL(eventJsonURL), /*loadClientParam*/ false);
        List<TracTracRaceRecordDTO> result = new ArrayList<TracTracRaceRecordDTO>();
        for (RaceRecord raceRecord : raceRecords.getB()) {
            if (listHiddenRaces == false && raceRecord.getRaceStatus().equals(TracTracConnectionConstants.HIDDEN_STATUS)) {
                continue;
            }
            
            result.add(new TracTracRaceRecordDTO(raceRecord.getID(), raceRecord.getEventName(), raceRecord.getName(),
                    raceRecord.getTrackingStartTime().asDate(), 
                    raceRecord
                    .getTrackingEndTime().asDate(), raceRecord.getRaceStartTime() == null ? null : raceRecord.getRaceStartTime().asDate(),
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
            String courseDesignUpdateURI, boolean trackWind, final boolean correctWindByDeclination, final boolean simulateWithStartTimeNow, 
            String tracTracUsername, String tracTracPassword) throws Exception {
        logger.info("tracWithTracTrac for regatta "+regattaToAddTo+" for race records "+rrs+" with liveURI "+liveURI+" and storedURI "+storedURI);
        for (TracTracRaceRecordDTO rr : rrs) {
            // reload JSON and load clientparams.php
            RaceRecord record = getTracTracAdapter().getSingleTracTracRaceRecord(new URL(rr.jsonURL), rr.id, /*loadClientParams*/true);
            logger.info("Loaded race " + record.getName() + " in " + record.getEventName() + " start:" + record.getRaceStartTime() + " trackingStart:" + record.getTrackingStartTime() + " trackingEnd:" + record.getTrackingEndTime());
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
            final RaceHandle raceHandle = getTracTracAdapter().addTracTracRace(getService(), regattaToAddTo,
                    record.getParamURL(), effectiveLiveURI == null ? null : new URI(effectiveLiveURI),
                    new URI(effectiveStoredURI), new URI(courseDesignUpdateURI),
                    new MillisecondsTimePoint(record.getTrackingStartTime().asMillis()),
                    new MillisecondsTimePoint(record.getTrackingEndTime().asMillis()),
                    getRaceLogStore(), getRegattaLogStore(),
                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS, simulateWithStartTimeNow, 
                    tracTracUsername, tracTracPassword,
                    record.getRaceStatus(), record.getRaceVisibility());
            if (trackWind) {
                new Thread("Wind tracking starter for race " + record.getEventName() + "/" + record.getName()) {
                    public void run() {
                        try {
                            startTrackingWind(raceHandle, correctWindByDeclination,
                                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception {
        Iterable<TracTracConfiguration> configs = tractracDomainObjectFactory.getTracTracConfigurations();
        List<TracTracConfigurationDTO> result = new ArrayList<TracTracConfigurationDTO>();
        for (TracTracConfiguration ttConfig : configs) {
            result.add(new TracTracConfigurationDTO(ttConfig.getName(), ttConfig.getJSONURL().toString(),
                    ttConfig.getLiveDataURI().toString(), ttConfig.getStoredDataURI().toString(), ttConfig.getCourseDesignUpdateURI().toString(),
                    ttConfig.getTracTracUsername().toString(), ttConfig.getTracTracPassword().toString()));
        }
        return result;
    }

    @Override
    public void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) throws Exception {
        tractracMongoObjectFactory.storeTracTracConfiguration(getTracTracAdapter().createTracTracConfiguration(name, jsonURL, liveDataURI, storedDataURI, 
                courseDesignUpdateURI, tracTracUsername, tracTracPassword));
    }

    @Override
    public void stopTrackingEvent(RegattaIdentifier regattaIdentifier) throws Exception {
        getService().apply(new StopTrackingRegatta(regattaIdentifier));
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

    /**
     * @param timeoutInMilliseconds eventually passed to {@link RaceHandle#getRace(long)}. If the race definition
     * can be obtained within this timeout, wind for the race will be tracked; otherwise, the method returns without
     * taking any effect.
     */
    private void startTrackingWind(RaceHandle raceHandle, boolean correctByDeclination, long timeoutInMilliseconds) throws Exception {
        Regatta regatta = raceHandle.getRegatta();
        if (regatta != null) {
            RaceDefinition race = raceHandle.getRace(timeoutInMilliseconds);
            if (race != null) {
                getService().startTrackingWind(regatta, race, correctByDeclination);
            } else {
                log("RaceDefinition wasn't received within " + timeoutInMilliseconds + "ms for a race in regatta "
                        + regatta.getName() + ". Aborting wait; no wind tracking for this race.");
            }
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
                if (windSource.getType() == WindSourceType.WEB) {
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
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition()
                    .getLngDeg());
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
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition()
                    .getLngDeg());
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
                windSourceTypeNames, trackedRace, /* onlyUpToNewestEvent */ true, includeCombinedWindForAllLegMiddles);
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
            List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
            Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
            final WindSource combinedWindSource = new WindSourceImpl(WindSourceType.COMBINED);
            windSourcesToDeliver.add(combinedWindSource);
            for (final WindSource windSource : windSourcesToDeliver) {
                // TODO consider parallelizing
                if (windSourceTypeNames == null || windSourceTypeNames.contains(windSource.getType().name())) {
                    WindTrackInfoDTO windTrackInfoDTO = createWindTrackInfoDTO(from, millisecondsStepWidth,
                            numberOfFixes, trackedRace, onlyUpToNewestEvent, newestEvent, windSource,
                            new PositionAtTimeProvider() { @Override public Position getPosition(TimePoint at) {
                                return windSource == combinedWindSource ? trackedRace.getCenterOfCourse(at) : null;
                            }});
                    windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
                }
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
                int numberOfFixes = (int) ((toTimePoint.asMillis() - fromTimePoint.asMillis())/resolutionInMilliseconds);
                result = getAveragedWindInfo(fromTimePoint, resolutionInMilliseconds, numberOfFixes,
                        windSourceTypeNames, trackedRace, onlyUpToNewestEvent, /* includeCombinedWindForAllLegMiddles */ false);
            }
        }
        return result;
    }

    @Override
    public boolean getPolarResults(RegattaAndRaceIdentifier raceIdentifier) {
        final boolean result;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace == null) {
            result = false;
        } else {
            BoatClass boatClass = trackedRace.getRace().getBoatClass();
            PolarDataService polarData = getService().getPolarDataService();
            PolarDiagram polarDiagram = new PolarDiagramGPS(boatClass, polarData);
            result = polarDiagram != null;
        }
        return result;
    }

    @Override
    public SimulatorResultsDTO getSimulatorResults(RegattaAndRaceIdentifier raceIdentifier, Date from, Date prevStartTime) {
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        SimulatorResultsDTO result = null;
        
        if (trackedRace != null) {
            TimePoint fromTimePoint = from == null ? new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis() + 10000) : new MillisecondsTimePoint(from);
            
            // get previous mark or start line as start-position
            TrackedLeg trackedLeg = trackedRace.getCurrentLeg(fromTimePoint);
            if (trackedLeg == null) {
                return result;
            }
            Waypoint fromWaypoint = trackedLeg.getLeg().getFrom();            
            
            // get next mark as end-position
            Waypoint toWaypoint = trackedLeg.getLeg().getTo();

            TimePoint startTimePoint = null;
            TimePoint endTimePoint = null;
            Iterable<Util.Pair<Waypoint, Util.Pair<TimePoint, TimePoint>>> markPassings = trackedRace.getMarkPassingsTimes();
            long legDuration = 0;
            synchronized(markPassings) {
                for(Pair<Waypoint, Pair<TimePoint, TimePoint>> markPassing : markPassings) {
                    if (markPassing.getA().equals(fromWaypoint)) {
                        startTimePoint = markPassing.getB().getA();
                    }
                    if (markPassing.getA().equals(toWaypoint)) {
                        endTimePoint = markPassing.getB().getA();
                    }
                }
            }
            legDuration = endTimePoint.asMillis() - startTimePoint.asMillis();
            Position startPosition = trackedRace.getApproximatePosition(fromWaypoint, startTimePoint);
            Position endPosition = trackedRace.getApproximatePosition(toWaypoint, endTimePoint);

            if (startTimePoint.asDate().equals(prevStartTime)) {
                return new SimulatorResultsDTO(startTimePoint.asDate(), 0, 0, null, null, null, null);
            }
            
            // determine legtype upwind/downwind/reaching
            LegType legType = null;
            try {
                legType = trackedLeg.getLegType(fromTimePoint);
            } catch (NoWindException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // get windfield
            WindFieldGenerator windField = new WindFieldTrackedRaceImpl(trackedRace);
            Duration timeStep = new MillisecondsDurationImpl(15 * 1000);
            windField.generate(startTimePoint, null, timeStep);
            
            // prepare simulation-parameters
            List<Position> course = new ArrayList<Position>();
            course.add(startPosition);
            course.add(endPosition);
            BoatClass boatClass = trackedRace.getRace().getBoatClass();
            PolarDataService polarData = getService().getPolarDataService();
            PolarDiagram polarDiagram = new PolarDiagramGPS(boatClass, polarData);
            SimulationParameters simulationPars = new SimulationParametersImpl(course, polarDiagram, windField, SailingSimulatorConstants.ModeEvent, true, true);
            
            // for upwind/downwind, run simulation with start-time, start-position, end-position and windfield
            SailingSimulator simulator = new SailingSimulatorImpl(simulationPars);
            Map<String, Path> pathsAndNames = null;
            if (legType != LegType.REACHING) {
                pathsAndNames = simulator.getAllPathsEvenTimed(timeStep.asMillis(), null);
            }

            // prepare simulator-results-dto
            if (pathsAndNames != null) {
                int noOfPaths = pathsAndNames.size();
                PathDTO[] pathDTOs = new PathDTO[noOfPaths];
                int index = noOfPaths - 1;
                for (Entry<String, Path> entry : pathsAndNames.entrySet()) {
                    pathDTOs[index] = new PathDTO(entry.getKey());
                    // fill pathDTO with path points where speed is true wind speed
                    List<SimulatorWindDTO> wList = new ArrayList<SimulatorWindDTO>();
                    for (TimedPositionWithSpeed p : entry.getValue().getPathPoints()) {
                        wList.add(createSimulatorWindDTO(p));
                    }
                    pathDTOs[index].setPoints(wList);
                    index--;
                }
                RaceMapDataDTO rcDTO;
                rcDTO = new RaceMapDataDTO();
                rcDTO.coursePositions = new CoursePositionsDTO();
                rcDTO.coursePositions.waypointPositions = new ArrayList<PositionDTO>();
                PositionDTO posDTO;
                posDTO = SimulatorServiceUtils.toPositionDTO(startPosition);
                rcDTO.coursePositions.waypointPositions.add(posDTO);
                posDTO = SimulatorServiceUtils.toPositionDTO(endPosition);
                rcDTO.coursePositions.waypointPositions.add(posDTO);
                result = new SimulatorResultsDTO(startTimePoint.asDate(), timeStep.asMillis(), legDuration, rcDTO, pathDTOs, null, null);
            }
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
            result.position = SimulatorServiceUtils.toPositionDTO(position);
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
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
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
            if(Util.size(webWindSources) == 0) {
                // create a new WEB wind source if not available
                trackedRace.recordWind(wind, new WindSourceImpl(WindSourceType.WEB));
            } else {
                trackedRace.recordWind(wind, webWindSources.iterator().next());
            }
        }
    }

    @Override
    public CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        final Map<CompetitorDTO, List<GPSFixDTO>> boatPositions = getBoatPositions(raceIdentifier,
                fromPerCompetitorIdAsString, toPerCompetitorIdAsString, extrapolate);
        final CoursePositionsDTO coursePositions = getCoursePositions(raceIdentifier, date);
        final List<SidelineDTO> courseSidelines = getCourseSidelines(raceIdentifier, date);
        final List<QuickRankDTO> quickRanks = getQuickRanks(raceIdentifier, date);
        return new CompactRaceMapDataDTO(boatPositions, coursePositions, courseSidelines, quickRanks);
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
     *            keys in the <code>from</code> parameter, requests the GPS fixes up to but excluding the date provided
     *            as value
     * @param extrapolate
     *            if <code>true</code> and no (exact or interpolated) position is known for <code>date</code>, the last
     *            entry returned in the list of GPS fixes will be obtained by extrapolating from the competitors last
     *            known position before <code>date</code> and the estimated speed.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    private Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate)
            throws NoWindException {
        Map<Pair<Leg, TimePoint>, LegType> legTypeCache = new HashMap<>();
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                if (fromPerCompetitorIdAsString.containsKey(competitor.getId().toString())) {
                    CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                    List<GPSFixDTO> fixesForCompetitor = new ArrayList<GPSFixDTO>();
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
                            if (fix.getTimePoint().before(toTimePointExcluding)) {
                                if (logger.isLoggable(Level.FINEST)) {
                                    logger.finest(""+competitor.getName()+": " + fix);
                                }
                                fixes.add(fix);
                            } else {
                                break;
                            }
                        }
                    } finally {
                        track.unlockAfterRead();
                    }
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
                        }
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
                            GPSFixDTO fixDTO = createGPSFixDTO(fix, estimatedSpeed, windDTO, tack, legType, /* extrapolate */
                                    false);
                            fixesForCompetitor.add(fixDTO);
                            if (fixIter.hasNext()) {
                                fix = fixIter.next();
                            } else {
                                // check if fix was at date and if extrapolation is requested
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
                                    GPSFixDTO extrapolated = new GPSFixDTO(
                                            toPerCompetitorIdAsString.get(competitorDTO.getIdAsString()),
                                            position==null?null:new PositionDTO(position.getLatDeg(), position.getLngDeg()),
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

    private GPSFixDTO createGPSFixDTO(GPSFix fix, SpeedWithBearing speedWithBearing, WindDTO windDTO, Tack tack, LegType legType, boolean extrapolated) {
        return new GPSFixDTO(fix.getTimePoint().asDate(), fix.getPosition()==null?null:new PositionDTO(fix
                .getPosition().getLatDeg(), fix.getPosition().getLngDeg()),
                speedWithBearing==null?null:createSpeedWithBearingDTO(speedWithBearing), windDTO, tack, legType, extrapolated);
    }

    @Override
    public RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier) {
        RaceTimesInfoDTO raceTimesInfo = null;
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);

        if (trackedRace != null) {
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
            RaceTimesInfoDTO raceTimesInfo = getRaceTimesInfo(raceIdentifier);
            if (raceTimesInfo != null) {
                raceTimesInfos.add(raceTimesInfo);
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
            final TimePoint dateAsTimePoint;
            if (date == null) {
                dateAsTimePoint = MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
            } else {
                dateAsTimePoint = new MillisecondsTimePoint(date);
            }
            result.marks = new HashSet<MarkDTO>();
            result.waypointPositions = new ArrayList<PositionDTO>();
            Set<Mark> marks = new HashSet<Mark>();
            Course course = trackedRace.getRace().getCourse();
            for (Waypoint waypoint : course.getWaypoints()) {
                Position waypointPosition = trackedRace.getApproximatePosition(waypoint, dateAsTimePoint);
                if (waypointPosition != null) {
                    result.waypointPositions.add(new PositionDTO(waypointPosition.getLatDeg(), waypointPosition
                            .getLngDeg()));
                }
                for (Mark b : waypoint.getMarks()) {
                    marks.add(b);
                }
            }
            for (Mark mark : marks) {
                GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                Position positionAtDate = track.getEstimatedPosition(dateAsTimePoint, /* extrapolate */false);
                if (positionAtDate != null) {
                    result.marks.add(convertToMarkDTO(mark, positionAtDate));
                }
            }

            // set the positions of start and finish
            Waypoint firstWaypoint = course.getFirstWaypoint();
            if (firstWaypoint != null && Util.size(firstWaypoint.getMarks()) == 2) {
                final LineDetails markPositionDTOsAndLineAdvantage = trackedRace.getStartLine(dateAsTimePoint);
                if (markPositionDTOsAndLineAdvantage != null) {
                    final List<PositionDTO> startMarkPositionDTOs = getMarkPositionDTOs(dateAsTimePoint, trackedRace,
                            firstWaypoint);
                    result.startMarkPositions = startMarkPositionDTOs;
                    result.startLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                    Bearing absoluteAngleDifferenceToTrueWind = markPositionDTOsAndLineAdvantage
                            .getAbsoluteAngleDifferenceToTrueWind();
                    result.startLineAngleToCombinedWind = absoluteAngleDifferenceToTrueWind == null ? null
                            : absoluteAngleDifferenceToTrueWind.getDegrees();
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
                    final List<PositionDTO> finishMarkPositionDTOs = getMarkPositionDTOs(dateAsTimePoint, trackedRace,
                            lastWaypoint);
                    result.finishMarkPositions = finishMarkPositionDTOs;
                    result.finishLineLengthInMeters = markPositionDTOsAndLineAdvantage.getLength().getMeters();
                    Bearing absoluteAngleDifferenceToTrueWind = markPositionDTOsAndLineAdvantage
                            .getAbsoluteAngleDifferenceToTrueWind();
                    result.finishLineAngleToCombinedWind = absoluteAngleDifferenceToTrueWind == null ? null
                            : absoluteAngleDifferenceToTrueWind.getDegrees();
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
            for (Mark mark : trackedRace.getMarks()) {
                Position pos = trackedRace.getOrCreateTrack(mark).getEstimatedPosition(dateAsTimePoint, false);
                allMarks.add(convertToMarkDTO(mark, pos));
            }
            Course course = trackedRace.getRace().getCourse();
            for (Waypoint waypoint : course.getWaypoints()) {
                ControlPointDTO controlPointDTO = controlPointCache.get(waypoint.getControlPoint().getId());
                if (controlPointDTO == null) {
                    controlPointDTO = convertToControlPointDTO(waypoint.getControlPoint(), trackedRace, dateAsTimePoint);
                    controlPointCache.put(waypoint.getControlPoint().getId(), controlPointDTO);
                }
                WaypointDTO waypointDTO = new WaypointDTO(waypoint.getName(), controlPointDTO,
                        waypoint.getPassingInstructions());
                waypointDTOs.add(waypointDTO);
            }
        }
        return new RaceCourseDTO(waypointDTOs, allMarks);
    }
    
    private ControlPointDTO convertToControlPointDTO(ControlPoint controlPoint, TrackedRace trackedRace, TimePoint timePoint) {
        ControlPointDTO result;
        if (controlPoint instanceof ControlPointWithTwoMarks) {
            final Mark left = ((ControlPointWithTwoMarks) controlPoint).getLeft();
            final Position leftPos = trackedRace.getOrCreateTrack(left).getEstimatedPosition(timePoint, /* extrapolate */ false);
            final Mark right = ((ControlPointWithTwoMarks) controlPoint).getRight();
            final Position rightPos = trackedRace.getOrCreateTrack(right).getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new GateDTO(controlPoint.getId().toString(), controlPoint.getName(), convertToMarkDTO(left, leftPos), convertToMarkDTO(right, rightPos)); 
        } else {
            final Position posOfFirst = trackedRace.getOrCreateTrack(controlPoint.getMarks().iterator().next()).
                    getEstimatedPosition(timePoint, /* extrapolate */ false);
            result = new MarkDTO(controlPoint.getId().toString(), controlPoint.getName(), posOfFirst == null ? -1 : posOfFirst.getLatDeg(), posOfFirst == null ? -1 : posOfFirst.getLngDeg());
        }
        return result;
    }
    
    private ControlPointDTO convertToControlPointDTO(ControlPoint controlPoint) {
        ControlPointDTO result;
        if (controlPoint instanceof ControlPointWithTwoMarks) {
            final Mark left = ((ControlPointWithTwoMarks) controlPoint).getLeft();
            final Mark right = ((ControlPointWithTwoMarks) controlPoint).getRight();
            result = new GateDTO(controlPoint.getId().toString(), controlPoint.getName(), convertToMarkDTO(left, null), convertToMarkDTO(right, null)); 
        } else {
            result = new MarkDTO(controlPoint.getId().toString(), controlPoint.getName());
        }
        return result;
    }
    
    private ControlPoint getOrCreateControlPoint(ControlPointDTO dto) {
        String id = dto.getIdAsString();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (dto instanceof GateDTO) {
            GateDTO gateDTO = (GateDTO) dto;
            Mark left = (Mark) getOrCreateControlPoint(gateDTO.getLeft());
            Mark right = (Mark) getOrCreateControlPoint(gateDTO.getRight());
            return baseDomainFactory.getOrCreateControlPointWithTwoMarks(id, gateDTO.getName(), left, right);
        } else {
            MarkDTO markDTO = (MarkDTO) dto;
            return baseDomainFactory.getOrCreateMark(id, dto.getName(), markDTO.type, markDTO.color, markDTO.shape, markDTO.pattern);
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

    private List<PositionDTO> getMarkPositionDTOs(
            TimePoint timePoint, TrackedRace trackedRace, Waypoint waypoint) {
        List<PositionDTO> markPositionDTOs = new ArrayList<PositionDTO>();
        for (Mark startMark : waypoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark)
                    .getEstimatedPosition(timePoint, /* extrapolate */false);
            if (estimatedMarkPosition != null) {
                markPositionDTOs.add(new PositionDTO(estimatedMarkPosition.getLatDeg(), estimatedMarkPosition.getLngDeg()));
            }
        }
        return markPositionDTOs;
    }

    /**
     * @param timePoint
     *            <code>null</code> means "live" and is then replaced by "now" minus the tracked race's
     *            {@link TrackedRace#getDelayToLiveInMillis() delay}.
     */
    public List<QuickRankDTO> computeQuickRanks(RegattaAndRaceIdentifier raceIdentifier, TimePoint timePoint)
            throws NoWindException {
        List<QuickRankDTO> result = new ArrayList<QuickRankDTO>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            final TimePoint actualTimePoint;
            if (timePoint == null) {
                actualTimePoint = MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
            } else {
                actualTimePoint = timePoint;
            }
            RaceDefinition race = trackedRace.getRace();
            int rank = 1;
            for (Competitor competitor : trackedRace.getCompetitorsFromBestToWorst(actualTimePoint)) {
                TrackedLegOfCompetitor trackedLeg = trackedRace.getTrackedLeg(competitor, actualTimePoint);
                if (trackedLeg != null) {
                    int legNumberOneBased = race.getCourse().getLegs().indexOf(trackedLeg.getLeg()) + 1;
                    QuickRankDTO quickRankDTO = new QuickRankDTO(baseDomainFactory.convertToCompetitorDTO(competitor),
                            rank, legNumberOneBased);
                    result.add(quickRankDTO);
                }
                rank++;
            }
        }
        return result;
    }

    private List<QuickRankDTO> getQuickRanks(RegattaAndRaceIdentifier raceIdentifier, Date date) throws NoWindException {
        final List<QuickRankDTO> result;
        if (date == null) {
            result = quickRanksLiveCache.get(raceIdentifier);
        } else {
            result = computeQuickRanks(raceIdentifier, date == null ? null : new MillisecondsTimePoint(date));
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

            for(WindSource windSource: trackedRace.getWindSources()) {
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
                p = new DegreePosition(windDTO.position.latDeg, windDTO.position.lngDeg);
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
    
    @Override
    public List<String> getLeaderboardNames() {
        return new ArrayList<String>(getService().getLeaderboards().keySet());
    }

    @Override
    public com.sap.sse.common.Util.Pair<String, LeaderboardType> checkLeaderboardName(String leaderboardName) {
        com.sap.sse.common.Util.Pair<String, LeaderboardType> result = null;

        if(getService().getLeaderboards().containsKey(leaderboardName)) {
            Leaderboard leaderboard = getService().getLeaderboards().get(leaderboardName);
            boolean isMetaLeaderboard = leaderboard instanceof MetaLeaderboard ? true : false;
            boolean isRegattaLeaderboard = leaderboard instanceof RegattaLeaderboard ? true : false;
            LeaderboardType type;
            if(isMetaLeaderboard) {
                type = isRegattaLeaderboard ? LeaderboardType.RegattaMetaLeaderboard : LeaderboardType.FlexibleMetaLeaderboard;
            } else {
                type = isRegattaLeaderboard ? LeaderboardType.RegattaLeaderboard : LeaderboardType.FlexibleLeaderboard;
            }
            result = new com.sap.sse.common.Util.Pair<String, LeaderboardType>(leaderboard.getName(), type);
        }
        
        return result;
    }

    @Override
    public StrippedLeaderboardDTO createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringSchemeType scoringSchemeType,
            UUID courseAreaId) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateFlexibleLeaderboard(leaderboardName, leaderboardDisplayName, discardThresholds,
                baseDomainFactory.createScoringScheme(scoringSchemeType), courseAreaId)), false, false);
    }

    public StrippedLeaderboardDTO createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds) {
        return createStrippedLeaderboardDTO(getService().apply(new CreateRegattaLeaderboard(regattaIdentifier, leaderboardDisplayName, discardThresholds)), false, false);
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboards() {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        for(Leaderboard leaderboard: leaderboards.values()) {
            StrippedLeaderboardDTO dao = createStrippedLeaderboardDTO(leaderboard, false, false);
            results.add(dao);
        }
        return results;
    }

    @Override
    public StrippedLeaderboardDTO getLeaderboard(String leaderboardName) {
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        StrippedLeaderboardDTO result = null;
        Leaderboard leaderboard = leaderboards.get(leaderboardName);
        if (leaderboard != null) {
            result = createStrippedLeaderboardDTO(leaderboard, false, false);
        }
        return result;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByEvent(EventDTO event) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        if (event != null) {
            for (RegattaDTO regatta : event.regattas) {
                results.addAll(getLeaderboardsByRegatta(regatta));
            }
            HashSet<StrippedLeaderboardDTO> set = new HashSet<StrippedLeaderboardDTO>(results);
            results.clear();
            results.addAll(set);
        }
        return results;
    }

    private List<StrippedLeaderboardDTO> getLeaderboardsByRegatta(RegattaDTO regatta) {
        List<StrippedLeaderboardDTO> results = new ArrayList<StrippedLeaderboardDTO>();
        if (regatta != null && regatta.races != null) {
            for (RaceDTO race : regatta.races) {
                List<StrippedLeaderboardDTO> leaderboard = getLeaderboardsByRaceAndRegatta(race, regatta.getRegattaIdentifier());
                if (leaderboard != null && !leaderboard.isEmpty()) {
                    results.addAll(leaderboard);
                }
            }
        }
        // Removing duplicates
        HashSet<StrippedLeaderboardDTO> set = new HashSet<StrippedLeaderboardDTO>(results);
        results.clear();
        results.addAll(set);
        return results;
    }

    @Override
    public List<StrippedLeaderboardDTO> getLeaderboardsByRaceAndRegatta(RaceDTO race, RegattaIdentifier regattaIdentifier) {
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
                            if (trackedRaceDef.getName().equals(race.getName())) {
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
        StrippedLeaderboardDTO leaderboardDTO = new StrippedLeaderboardDTO();
        TimePoint startOfLatestRace = null;
        Long delayToLiveInMillisForLatestRace = null;
        leaderboardDTO.name = leaderboard.getName();
        leaderboardDTO.displayName = leaderboard.getDisplayName();
        leaderboardDTO.competitorDisplayNames = new HashMap<CompetitorDTO, String>();
        leaderboardDTO.competitorsCount = Util.size(leaderboard.getCompetitors());
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            Regatta regatta = regattaLeaderboard.getRegatta();
            leaderboardDTO.regattaName = regatta.getName(); 
            leaderboardDTO.type = leaderboard instanceof MetaLeaderboard ? LeaderboardType.RegattaMetaLeaderboard : LeaderboardType.RegattaLeaderboard;
            leaderboardDTO.scoringScheme = regatta.getScoringScheme().getType();
        } else {
            leaderboardDTO.type = leaderboard instanceof MetaLeaderboard ? LeaderboardType.FlexibleMetaLeaderboard : LeaderboardType.FlexibleLeaderboard;
            leaderboardDTO.scoringScheme = leaderboard.getScoringScheme().getType();
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
                        Collection<MediaTrack> mediaTracksForRace = getService().getMediaTracksForRace(raceIdentifier);
                        raceDTO.trackedRaceStatistics = baseDomainFactory.createTrackedRaceStatisticsDTO(trackedRace, leaderboard, raceColumn, fleet, mediaTracksForRace); 
                    }
                }    
                final FleetDTO fleetDTO = baseDomainFactory.convertToFleetDTO(fleet);
                RaceColumnDTO raceColumnDTO = leaderboardDTO.addRace(raceColumn.getName(), raceColumn.getExplicitFactor(), raceColumn.getFactor(),
                        raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getRegatta().getName() : null,
                        raceColumn instanceof RaceColumnInSeries ? ((RaceColumnInSeries) raceColumn).getSeries().getName() : null,
                        fleetDTO, raceColumn.isMedalRace(), raceIdentifier, raceDTO);
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                RaceLogTrackingState raceLogTrackingState = raceLog == null ? RaceLogTrackingState.NOT_A_RACELOG_TRACKED_RACE :
                    new RaceLogTrackingStateAnalyzer(raceLog).analyze();
                boolean raceLogTrackerExists = raceLog == null ? false : getService().getRaceTrackerById(raceLog.getId()) != null;
                boolean competitorRegistrationsExist = raceLog == null ? false : !new RegisteredCompetitorsAnalyzer<>(raceLog).analyze().isEmpty();
                RaceLogTrackingInfoDTO raceLogTrackingInfo = new RaceLogTrackingInfoDTO(raceLogTrackerExists,
                        competitorRegistrationsExist, raceLogTrackingState);
                raceColumnDTO.setRaceLogTrackingInfo(fleetDTO, raceLogTrackingInfo);
            }
        }
        return leaderboardDTO;
    }

    @Override
    public StrippedLeaderboardDTO updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds, UUID newCourseAreaId) {
        Leaderboard updatedLeaderboard = getService().apply(new UpdateLeaderboard(leaderboardName, newLeaderboardName, newLeaderboardDisplayName, newDiscardingThresholds, newCourseAreaId));
        return createStrippedLeaderboardDTO(updatedLeaderboard, false, false);
    }
    
    @Override
    public void removeLeaderboards(Collection<String> leaderboardNames) {
        for (String leaderoardName : leaderboardNames) {
            removeLeaderboard(leaderoardName);
        }
    }

    @Override
    public void removeLeaderboard(String leaderboardName) {
        getService().apply(new RemoveLeaderboard(leaderboardName));
    }

    @Override
    public void renameLeaderboard(String leaderboardName, String newLeaderboardName) {
        getService().apply(new RenameLeaderboard(leaderboardName, newLeaderboardName));
    }

    @Override
    public void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        getService().apply(new AddColumnToLeaderboard(columnName, leaderboardName, medalRace));
    }

    @Override
    public void addColumnsToLeaderboard(String leaderboardName, List<com.sap.sse.common.Util.Pair<String, Boolean>> columnsToAdd) {
        for(com.sap.sse.common.Util.Pair<String, Boolean> columnToAdd: columnsToAdd) {
            getService().apply(new AddColumnToLeaderboard(columnToAdd.getA(), leaderboardName, columnToAdd.getB()));
        }
    }

    @Override
    public void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove) {
        for (String columnToRemove : columnsToRemove) {
            getService().apply(new RemoveLeaderboardColumn(columnToRemove, leaderboardName));
        }
    }

    @Override
    public void removeLeaderboardColumn(String leaderboardName, String columnName) {
        getService().apply(new RemoveLeaderboardColumn(columnName, leaderboardName));
    }

    @Override
    public void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        getService().apply(new RenameLeaderboardColumn(leaderboardName, oldColumnName, newColumnName));
    }

    @Override
    public void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor) {
        getService().apply(new UpdateLeaderboardColumnFactor(leaderboardName, columnName, newFactor));
    }

    @Override
    public void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed) {
        getService().apply(new SetSuppressedFlagForCompetitorInLeaderboard(leaderboardName, competitorIdAsString, suppressed));
    }

    @Override
    public boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            String fleetName, RegattaAndRaceIdentifier raceIdentifier) {
        return getService().apply(new ConnectTrackedRaceToLeaderboardColumn(leaderboardName, raceColumnName, fleetName, raceIdentifier));
    }

    @Override
    public Map<String, RegattaAndRaceIdentifier> getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName) {
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
        getService().apply(new DisconnectLeaderboardColumnFromTrackedRace(leaderboardName, raceColumnName, fleetName));
    }

    @Override
    public void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints) {
        getService().apply(new UpdateLeaderboardCarryValue(leaderboardName, competitorIdAsString, carriedPoints));
    }

    @Override
    public com.sap.sse.common.Util.Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString, String raceColumnName,
            MaxPointsReason maxPointsReason, Date date) throws NoWindException {
        return getService().apply(
                new UpdateLeaderboardMaxPointsReason(leaderboardName, raceColumnName, competitorIdAsString,
                        maxPointsReason, new MillisecondsTimePoint(date)));
    }

    @Override
    public com.sap.sse.common.Util.Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName,
            String competitorIdAsString, String columnName, Double correctedScore, Date date) throws NoWindException {
        return getService().apply(
                new UpdateLeaderboardScoreCorrection(leaderboardName, columnName, competitorIdAsString, correctedScore,
                        new MillisecondsTimePoint(date)));
    }

    @Override
    public void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity, String comment) {
        getService().apply(
                new UpdateLeaderboardScoreCorrectionMetadata(leaderboardName,
                        timePointOfLastCorrectionValidity == null ? null : new MillisecondsTimePoint(timePointOfLastCorrectionValidity),
                                comment));
    }

    @Override
    public void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates) throws NoWindException {
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
        getService().apply(new UpdateCompetitorDisplayNameInLeaderboard(leaderboardName, competitorIdAsString, displayName));
    }

    @Override
    public void moveLeaderboardColumnUp(String leaderboardName, String columnName) {
        getService().apply(new MoveLeaderboardColumnUp(leaderboardName, columnName));
    }

    @Override
    public void moveLeaderboardColumnDown(String leaderboardName, String columnName) {
        getService().apply(new MoveLeaderboardColumnDown(leaderboardName, columnName));
    }

    @Override
    public void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) {
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
        List<SwissTimingConfigurationDTO> result = new ArrayList<SwissTimingConfigurationDTO>();
        for (SwissTimingConfiguration stConfig : configs) {
            result.add(new SwissTimingConfigurationDTO(stConfig.getName(), stConfig.getJsonURL(), stConfig.getHostname(), stConfig.getPort()));
        }
        return result;
    }

    @Override
    public SwissTimingEventRecordDTO getRacesOfSwissTimingEvent(String eventJsonURL) 
            throws UnknownHostException, IOException, InterruptedException, ParseException {
        SwissTimingEventRecordDTO result = null; 
        List<SwissTimingRaceRecordDTO> swissTimingRaces = new ArrayList<SwissTimingRaceRecordDTO>();
        
        // TODO: delete getSwissTimingAdapter().getSwissTimingRaceRecords() method
        // TODO: delete SwissTimingDomainFactory.getRaceTypeFromRaceID(String raceID)
        URL url = new URL(eventJsonURL);
        URLConnection eventResultConn = url.openConnection();
        Manage2SailEventResultsParserImpl parser = new Manage2SailEventResultsParserImpl();
        EventResultDescriptor eventResult = parser.getEventResult((InputStream) eventResultConn.getContent());
        if (eventResult != null) {
            for (RegattaResultDescriptor regattaResult : eventResult.getRegattaResults()) {
                for(RaceResultDescriptor race: regattaResult.getRaceResults()) {
                	// add only the  tracked races
                	if(race.isTracked() != null && race.isTracked() == true) {
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
    public void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, int port) {
        if(!jsonURL.equalsIgnoreCase("test")) {
            swissTimingAdapterPersistence.storeSwissTimingConfiguration(swissTimingFactory.createSwissTimingConfiguration(configName, jsonURL, hostname, port));
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
            boolean trackWind, final boolean correctWindByDeclination) throws Exception {
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
            if(regattaResults == null) {
            	regattaResults = getSwissTimingAdapter().readRegattaEntryListFromXrrUrl(rr.xrrEntriesUrl);
                if(regattaResults != null) {
                	cachedRegattaEntriesLists.put(rr.xrrEntriesUrl, regattaResults);
                }
            }
            StartList startList = null;
            if(regattaResults != null) {
            	startList = getSwissTimingAdapter().readStartListForRace(rr.raceId, regattaResults);
            }
            // now read the entry list for the race from the result
            final RaceHandle raceHandle = getSwissTimingAdapter().addSwissTimingRace(getService(), regattaToAddTo,
                    rr.raceId, rr.getName(), raceDescription, boatClass, hostname, port, startList,
                    getRaceLogStore(), getRegattaLogStore(),
                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
            if (trackWind) {
                new Thread("Wind tracking starter for race " + rr.raceId + "/" + rr.getName()) {
                    public void run() {
                        try {
                            startTrackingWind(raceHandle, correctWindByDeclination,
                                    RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }.start();
            }
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
            result.add(new SwissTimingReplayRaceDTO(replayRace.getFlightNumber(), replayRace.getRaceId(), replayRace.getRsc(), replayRace.getName(), replayRace.getBoatClass(), replayRace.getStartTime(), replayRace.getLink(), hasRememberedRegatta(replayRace.getRaceId())));
        }
        return result;
    }

    @Override
    public void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaceDTOs,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow) {
        logger.info("replaySwissTimingRace for regatta "+regattaIdentifier+" for races "+replayRaceDTOs);
        Regatta regatta;
        for (SwissTimingReplayRaceDTO replayRaceDTO : replayRaceDTOs) {
            try {
                if (regattaIdentifier == null) {
                    String boatClass = replayRaceDTO.boat_class;
                    for (String genderIndicator : new String[] { "Man", "Woman", "Men", "Women", "M", "W" }) {
                        Pattern p = Pattern.compile("(( - )|-| )" + genderIndicator + "$");
                        Matcher m = p.matcher(boatClass.trim());
                        if (m.find()) {
                            boatClass = boatClass.trim().substring(0, m.start(1));
                            break;
                        }
                    }
                    regatta = getService().createRegatta(
                            RegattaImpl.getDefaultName(replayRaceDTO.rsc, boatClass.trim()),
                            boatClass.trim(),
                            /* startDate*/ null, /*endDate*/ null,
                            RegattaImpl.getDefaultName(replayRaceDTO.rsc, replayRaceDTO.boat_class),
                            Collections.singletonList(new SeriesImpl(LeaderboardNameConstants.DEFAULT_SERIES_NAME,
                            /* isMedal */false, Collections.singletonList(new FleetImpl(
                                    LeaderboardNameConstants.DEFAULT_FLEET_NAME)),
                            /* race column names */new ArrayList<String>(), getService())), false,
                            baseDomainFactory.createScoringScheme(ScoringSchemeType.LOW_POINT), null, /* useStartTimeInference */ true);
                    // TODO: is course area relevant for swiss timing replay?
                } else {
                    regatta = getService().getRegatta(regattaIdentifier);
                }
                getSwissTimingReplayService().loadRaceData(replayRaceDTO.link, regatta, getService());
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
            case COURSE_OVER_GROUND_TRUE_DEGREES:
                final GPSFixTrack<Competitor, GPSFixMoving> cogTrack = trackedRace.getTrack(competitor);
                if (cogTrack != null) {
                    SpeedWithBearing speedOverGround = cogTrack.getEstimatedSpeed(timePoint);
                    result = (speedOverGround == null) ? null : speedOverGround.getBearing().getDegrees();
                }
                break;
            case VELOCITY_MADE_GOOD_IN_KNOTS:
                if (trackedLeg != null) {
                    Speed velocityMadeGood = trackedLeg.getVelocityMadeGood(timePoint, WindPositionMode.EXACT, cache);
                    result = (velocityMadeGood == null) ? null : velocityMadeGood.getKnots();
                }
                break;
            case DISTANCE_TRAVELED:
                if (trackedLeg != null) {
                    Distance distanceTraveled = trackedRace.getDistanceTraveled(competitor, timePoint);
                    result = distanceTraveled == null ? null : distanceTraveled.getMeters();
                }
                break;
            case DISTANCE_TRAVELED_INCLUDING_GATE_START:
                if (trackedLeg != null) {
                    Distance distanceTraveledConsideringGateStart = trackedRace.getDistanceTraveledIncludingGateStart(competitor, timePoint);
                    result = distanceTraveledConsideringGateStart == null ? null : distanceTraveledConsideringGateStart.getMeters();
                }
                break;
            case GAP_TO_LEADER_IN_SECONDS:
                if (trackedLeg != null) {
                    result = trackedLeg.getGapToLeaderInSeconds(timePoint, WindPositionMode.LEG_MIDDLE, cache);
                }
                break;
            case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                if (trackedLeg != null) {
                    Distance distanceToLeader = trackedLeg.getWindwardDistanceToOverallLeader(timePoint, WindPositionMode.LEG_MIDDLE);
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
            case DISTANCE_TO_START_LINE:
                TimePoint startOfRace = trackedRace.getStartOfRace();
                if (startOfRace == null || timePoint.before(startOfRace) || timePoint.equals(startOfRace)) {
                    Distance distanceToStartLine = trackedRace.getDistanceToStartLine(competitor, timePoint);
                    result = distanceToStartLine == null ? null : distanceToStartLine.getMeters();
                }
                break;
            case BEAT_ANGLE:
                if (trackedLeg != null) {
                    Bearing beatAngle = trackedLeg.getBeatAngle(timePoint, cache);
                    result = beatAngle == null ? null : Math.abs(beatAngle.getDegrees());
                }
                break;
            default:
                throw new UnsupportedOperationException("Theres currently no support for the enum value '" + dataType
                        + "' in this method.");
            }
            return result;
        } finally {
            course.unlockAfterRead();
        }
    }

    @Override
    public CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            final long stepSizeInMs, final DetailType detailType, final String leaderboardGroupName, final String leaderboardName) throws NoWindException {
        CompetitorsRaceDataDTO result = null;
        final TrackedRace trackedRace = getExistingTrackedRace(race);
        if (trackedRace != null) {
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            final TimePoint startTime = from == null ? trackedRace.getStartOfTracking() : new MillisecondsTimePoint(from);
            final TimePoint endTime = (to == null || to.after(newestEvent.asDate())) ? newestEvent : new MillisecondsTimePoint(to);
            result = new CompetitorsRaceDataDTO(detailType, startTime==null?null:startTime.asDate(), endTime==null?null:endTime.asDate());
            final ConcurrentHashMap<TimePoint, WindLegTypeAndLegBearingCache> cachesByTimePoint = new ConcurrentHashMap<>();
            Map<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>> resultFutures = new HashMap<CompetitorDTO, FutureTask<CompetitorRaceDataDTO>>();
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
                                    for (long i = startTime.asMillis(); i <= endTime.asMillis(); i += stepSizeInMs) {
                                        MillisecondsTimePoint time = new MillisecondsTimePoint(i);
                                        WindLegTypeAndLegBearingCache cache = cachesByTimePoint.get(time);
                                        if (cache == null) {
                                            cache = new LeaderboardDTOCalculationReuseCache(time);
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
    public List<com.sap.sse.common.Util.Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, 
            Date date, DetailType detailType) throws Exception {
        List<com.sap.sse.common.Util.Triple<String, List<CompetitorDTO>, List<Double>>> result = new ArrayList<com.sap.sse.common.Util.Triple<String,List<CompetitorDTO>,List<Double>>>();
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
                case REGATTA_TOTAL_POINTS_SUM:
                    for (Entry<RaceColumn, Map<Competitor, Double>> e : leaderboard.getTotalPointsSumAfterRaceColumn(effectiveTimePoint).entrySet()) {
                        List<CompetitorDTO> competitorDTOs = new ArrayList<>();
                        List<Double> pointSums = new ArrayList<>();
                        for (Entry<Competitor, Double> e2 : e.getValue().entrySet()) {
                            competitorDTOs.add(baseDomainFactory.convertToCompetitorDTO(e2.getKey()));
                            pointSums.add(e2.getValue());
                        }
                        result.add(new Triple<String, List<CompetitorDTO>, List<Double>>(e.getKey().getName(), competitorDTOs, pointSums)); 
                    }
                    break;
                case REGATTA_RANK:
                case OVERALL_RANK:
                    Map<RaceColumn, List<Competitor>> competitorsFromBestToWorst = leaderboard
                            .getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(effectiveTimePoint);
                    for (Entry<RaceColumn, List<Competitor>> e : competitorsFromBestToWorst.entrySet()) {
                        int rank = 1;
                        List<Double> values = new ArrayList<Double>();
                        List<CompetitorDTO> competitorDTOs = new ArrayList<CompetitorDTO>();
                        for (Competitor competitor : e.getValue()) {
                            values.add(new Double(rank));
                            competitorDTOs.add(baseDomainFactory.convertToCompetitorDTO(competitor));
                            rank++;
                        }
                        result.add(new Triple<String, List<CompetitorDTO>, List<Double>>(e.getKey().getName(), competitorDTOs, values));
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
    public List<com.sap.sse.common.Util.Pair<String, String>> getLeaderboardsNamesOfMetaLeaderboard(String metaLeaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(metaLeaderboardName);
        if (leaderboard == null) {
            throw new IllegalArgumentException("Couldn't find leaderboard named "+metaLeaderboardName);
        }
        if (!(leaderboard instanceof MetaLeaderboard)) {
            throw new IllegalArgumentException("The leaderboard "+metaLeaderboardName + " is not a metaleaderboard");
        }
        List<com.sap.sse.common.Util.Pair<String, String>> result = new ArrayList<com.sap.sse.common.Util.Pair<String, String>>();
        MetaLeaderboard metaLeaderboard = (MetaLeaderboard) leaderboard;
        for (Leaderboard containedLeaderboard: metaLeaderboard.getLeaderboards()) {
            result.add(new com.sap.sse.common.Util.Pair<String, String>(containedLeaderboard.getName(),
                    containedLeaderboard.getDisplayName() != null ? containedLeaderboard.getDisplayName() : containedLeaderboard.getName()));
        }
        return result;
    }

    @Override
    public Map<CompetitorDTO, List<GPSFixDTO>> getDouglasPoints(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            double meters) throws NoWindException {
        Map<CompetitorDTO, List<GPSFixDTO>> result = new HashMap<CompetitorDTO, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            MeterDistance maxDistance = new MeterDistance(meters);
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    // get Track of competitor
                    GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack = trackedRace.getTrack(competitor);
                    // Distance for DouglasPeucker
                    TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDTO));
                    TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDTO));
                    Iterable<GPSFixMoving> gpsFixApproximation = trackedRace.approximate(competitor, maxDistance,
                            timePointFrom, timePointTo);
                    List<GPSFixDTO> gpsFixDouglasList = new ArrayList<GPSFixDTO>();
                    GPSFix fix = null;
                    for (GPSFix next : gpsFixApproximation) {
                        if (fix != null) {
                            Bearing bearing = fix.getPosition().getBearingGreatCircle(next.getPosition());
                            Speed speed = fix.getPosition().getDistance(next.getPosition())
                                    .inTime(next.getTimePoint().asMillis() - fix.getTimePoint().asMillis());
                            final SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(speed.getKnots(), bearing);
                            GPSFixDTO fixDTO = createDouglasPeuckerGPSFixDTO(trackedRace, competitor, fix, speedWithBearing);
                            gpsFixDouglasList.add(fixDTO);
                        }
                        fix = next;
                    }
                    if (fix != null) {
                        // add one last GPSFixDTO with no successor to calculate speed/bearing to:
                        final SpeedWithBearing speedWithBearing = gpsFixTrack.getEstimatedSpeed(fix.getTimePoint());
                        GPSFixDTO fixDTO = createDouglasPeuckerGPSFixDTO(trackedRace, competitor, fix, speedWithBearing);
                        gpsFixDouglasList.add(fixDTO);
                    }
                    result.put(competitorDTO, gpsFixDouglasList);
                }
            }
        }
        return result;
    }

    private GPSFixDTO createDouglasPeuckerGPSFixDTO(TrackedRace trackedRace, Competitor competitor, GPSFix fix,
            SpeedWithBearing speedWithBearing) throws NoWindException {
        Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
        TrackedLegOfCompetitor trackedLegOfCompetitor = trackedRace.getTrackedLeg(competitor,
                fix.getTimePoint());
        LegType legType = trackedLegOfCompetitor == null ? null : trackedRace.getTrackedLeg(
                trackedLegOfCompetitor.getLeg()).getLegType(fix.getTimePoint());
        Wind wind = trackedRace.getWind(fix.getPosition(), fix.getTimePoint());
        WindDTO windDTO = createWindDTOFromAlreadyAveraged(wind, fix.getTimePoint());
        GPSFixDTO fixDTO = createGPSFixDTO(fix, speedWithBearing, windDTO, tack, legType, /* extrapolated */
                false);
        return fixDTO;
    }

    @Override
    public Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to) throws NoWindException {
        Map<CompetitorDTO, List<ManeuverDTO>> result = new HashMap<CompetitorDTO, List<ManeuverDTO>>();
        final TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            Map<CompetitorDTO, Future<List<ManeuverDTO>>> futures = new HashMap<CompetitorDTO, Future<List<ManeuverDTO>>>();
            for (final Competitor competitor : trackedRace.getRace().getCompetitors()) {
                CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                if (from.containsKey(competitorDTO)) {
                    final TimePoint timePointFrom = new MillisecondsTimePoint(from.get(competitorDTO));
                    final TimePoint timePointTo = new MillisecondsTimePoint(to.get(competitorDTO));
                    RunnableFuture<List<ManeuverDTO>> future = new FutureTask<List<ManeuverDTO>>(
                            new Callable<List<ManeuverDTO>>() {
                                @Override
                                public List<ManeuverDTO> call() {
                                    List<Maneuver> maneuversForCompetitor;
                                    maneuversForCompetitor = trackedRace.getManeuvers(competitor, timePointFrom,
                                            timePointTo, /* waitForLatest */ true);
                                    return createManeuverDTOsForCompetitor(maneuversForCompetitor, trackedRace, competitor);
                                }
                            });
                    executor.execute(future);
                    futures.put(competitorDTO, future);
                }
            }
            for (Map.Entry<CompetitorDTO, Future<List<ManeuverDTO>>> competitorAndFuture : futures.entrySet()) {
                try {
                    result.put(competitorAndFuture.getKey(), competitorAndFuture.getValue().get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    private List<ManeuverDTO> createManeuverDTOsForCompetitor(List<Maneuver> maneuvers, TrackedRace trackedRace, Competitor competitor) {
        List<ManeuverDTO> result = new ArrayList<ManeuverDTO>();
        for (Maneuver maneuver : maneuvers) {
            final ManeuverDTO maneuverDTO;
            if (maneuver.getType() == ManeuverType.MARK_PASSING) {
                maneuverDTO = new MarkpassingManeuverDTO(maneuver.getType(), maneuver.getNewTack(),
                        new PositionDTO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                        maneuver.getTimePoint().asDate(),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore()),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter()),
                        maneuver.getDirectionChangeInDegrees(), maneuver.getManeuverLoss()==null?null:maneuver.getManeuverLoss().getMeters(),
                                ((MarkPassingManeuver) maneuver).getSide());
            } else  {
                maneuverDTO = new ManeuverDTO(maneuver.getType(), maneuver.getNewTack(),
                        new PositionDTO(maneuver.getPosition().getLatDeg(), maneuver.getPosition().getLngDeg()), 
                        maneuver.getTimePoint().asDate(),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingBefore()),
                        createSpeedWithBearingDTO(maneuver.getSpeedWithBearingAfter()),
                        maneuver.getDirectionChangeInDegrees(), maneuver.getManeuverLoss()==null?null:maneuver.getManeuverLoss().getMeters());
            }
            result.add(maneuverDTO);
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
        return trackedRace;
    }

    @Override
    public TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        return getService().getExistingTrackedRace(regattaNameAndRaceName);
    }

    @Override
    public Regatta getRegatta(RegattaName regattaIdentifier) {
        return getService().getRegattaByName(regattaIdentifier.getRegattaName());
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
        ArrayList<LeaderboardGroupDTO> leaderboardGroupDTOs = new ArrayList<LeaderboardGroupDTO>();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();

        for (LeaderboardGroup leaderboardGroup : leaderboardGroups.values()) {
            leaderboardGroupDTOs.add(convertToLeaderboardGroupDTO(leaderboardGroup, withGeoLocationData, false));
        }

        return leaderboardGroupDTOs;
    }

    @Override
    public LeaderboardGroupDTO getLeaderboardGroupByName(String groupName, boolean withGeoLocationData) {
        return convertToLeaderboardGroupDTO(getService().getLeaderboardGroupByName(groupName), withGeoLocationData, false);
    }

    private LeaderboardGroupDTO convertToLeaderboardGroupDTO(LeaderboardGroup leaderboardGroup, boolean withGeoLocationData, boolean withStatisticalData) {
        LeaderboardGroupDTO groupDTO = new LeaderboardGroupDTO(leaderboardGroup.getId(), leaderboardGroup.getName(),
                leaderboardGroup.getDisplayName(), leaderboardGroup.getDescription());
        groupDTO.displayLeaderboardsInReverseOrder = leaderboardGroup.isDisplayGroupsInReverseOrder();
        for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
            groupDTO.leaderboards.add(createStrippedLeaderboardDTO(leaderboard, withGeoLocationData, withStatisticalData));
        }
        Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
        if (overallLeaderboard != null) {
            if (overallLeaderboard.getResultDiscardingRule() instanceof ThresholdBasedResultDiscardingRule) {
                groupDTO.setOverallLeaderboardDiscardThresholds(((ThresholdBasedResultDiscardingRule) overallLeaderboard
                        .getResultDiscardingRule()).getDiscardIndexResultsStartingWithHowManyRaces());
            }
            groupDTO.setOverallLeaderboardScoringSchemeType(overallLeaderboard.getScoringScheme().getType());
        }
        return groupDTO;
    }


    @Override
    public void renameLeaderboardGroup(String oldName, String newName) {
        getService().apply(new RenameLeaderboardGroup(oldName, newName));
    }

    @Override
    public void removeLeaderboardGroups(Set<String> groupNames) {
        for (String groupName : groupNames) {
            removeLeaderboardGroup(groupName);
        }
    }

    private void removeLeaderboardGroup(String groupName) {
        getService().apply(new RemoveLeaderboardGroup(groupName));
    }

    @Override
    public LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description, String displayName,
            boolean displayGroupsInReverseOrder,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        CreateLeaderboardGroup createLeaderboardGroupOp = new CreateLeaderboardGroup(groupName, description, displayName,
                displayGroupsInReverseOrder, new ArrayList<String>(), overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        return convertToLeaderboardGroupDTO(getService().apply(createLeaderboardGroupOp), false, false);
    }

    @Override
    public void updateLeaderboardGroup(String oldName, String newName, String newDescription, String newDisplayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
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
            replicaDTOs.add(new ReplicaDTO(replicaDescriptor.getIpAddress().getHostName(), replicaDescriptor
                    .getRegistrationTime().asDate(), replicaDescriptor.getUuid().toString(),
                    replicationCountByOperationClassName, service.getAverageNumberOfOperationsPerMessage(replicaDescriptor),
                    service.getNumberOfMessagesSent(replicaDescriptor), service.getNumberOfBytesSent(replicaDescriptor), service.getAverageNumberOfBytesPerMessage(replicaDescriptor)));
        }
        ReplicationMasterDTO master;
        ReplicationMasterDescriptor replicatingFromMaster = service.getReplicatingFromMaster();
        if (replicatingFromMaster == null) {
            master = null;
        } else {
            master = new ReplicationMasterDTO(replicatingFromMaster.getHostname(), replicatingFromMaster.getMessagingPort(),
                    replicatingFromMaster.getServletPort());
        }
        return new ReplicationStateDTO(master, replicaDTOs, service.getServerIdentifier().toString());
    }

    @Override
    public void startReplicatingFromMaster(String messagingHost, String masterHost, String exchangeName, int servletPort, int messagingPort) throws IOException, ClassNotFoundException, InterruptedException {
        // the queue name must be always the same for this server. in order to achieve
        // this we're using the unique server identifier
        getReplicationService().startToReplicateFrom(
                ReplicationFactory.INSTANCE.createReplicationMasterDescriptor(messagingHost, masterHost, exchangeName, servletPort, messagingPort, 
                        getReplicationService().getServerIdentifier().toString()));
    }

    @Override
    public List<EventDTO> getEvents() throws MalformedURLException {
        String requestBaseURL = getRequestBaseURL().toString();
        List<EventDTO> result = new ArrayList<EventDTO>();
        for (Event event : getService().getAllEvents()) {
            EventDTO eventDTO = convertToEventDTO(event, false);
            eventDTO.setBaseURL(requestBaseURL);
            eventDTO.setIsOnRemoteServer(false);
            result.add(eventDTO);
        }
        return result;
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
            String baseURL = getBaseURL(serverRef.getURL()).toString();
            if (remoteEvents != null) {
                for (EventBase remoteEvent : remoteEvents) {
                    EventBaseDTO remoteEventDTO = convertToEventDTO(remoteEvent);
                    remoteEventDTO.setBaseURL(baseURL);
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
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURLString,
            String logoImageURLString, Iterable<String> imageURLStrings, Iterable<String> videoURLStrings,
            Iterable<String> sponsorImageURLStrings) throws MalformedURLException {
        TimePoint startTimePoint = startDate != null ? new MillisecondsTimePoint(startDate) : null;
        TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
        URL officialWebsiteURL = officialWebsiteURLString != null ? new URL(officialWebsiteURLString) : null;
        URL logoImageURL = logoImageURLString != null ? new URL(logoImageURLString) : null;
        List<URL> imageURLs = createURLsFromStrings(imageURLStrings);
        List<URL> videoURLs = createURLsFromStrings(videoURLStrings);
        List<URL> sponsorimagieURLs = createURLsFromStrings(sponsorImageURLStrings);
        getService().apply(
                new UpdateEvent(eventId, eventName, eventDescription, startTimePoint, endTimePoint, venue.getName(),
                        isPublic, leaderboardGroupIds, logoImageURL, officialWebsiteURL, imageURLs, videoURLs,
                        sponsorimagieURLs));
        return getEventById(eventId, false);
    }

    /**
     * @param urlStrings
     * @return
     * @throws MalformedURLException
     */
    private List<URL> createURLsFromStrings(Iterable<String> urlStrings) throws MalformedURLException {
        List<URL> imageURLs = new ArrayList<>();
        for (String imageURLString : urlStrings) {
            imageURLs.add(new URL(imageURLString));
        }
        return imageURLs;
    }

    @Override
    public EventDTO createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue,
            boolean isPublic, List<String> courseAreaNames, Iterable<String> imageURLs,
            Iterable<String> videoURLs, Iterable<String> sponsorImageURLs, String logoImageURLAsString, String officialWebsiteURLAsString)
            throws MalformedURLException {
        UUID eventUuid = UUID.randomUUID();
        TimePoint startTimePoint = startDate != null ?  new MillisecondsTimePoint(startDate) : null;
        TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
        getService().apply(
                new CreateEvent(eventName, eventDescription, startTimePoint, endTimePoint, venue, isPublic, eventUuid,
                        createURLsFromStrings(imageURLs), createURLsFromStrings(videoURLs),
                        createURLsFromStrings(sponsorImageURLs),
                        logoImageURLAsString == null ? null : new URL(logoImageURLAsString), officialWebsiteURLAsString == null ? null : new URL(officialWebsiteURLAsString)));
        for (String courseAreaName : courseAreaNames) {
            createCourseArea(eventUuid, courseAreaName);
        }
        return getEventById(eventUuid, false);
    }

    @Override
    public void createCourseArea(UUID eventId, String courseAreaName) {
        getService().apply(new AddCourseArea(eventId, courseAreaName, UUID.randomUUID()));
    }

    @Override
    public void removeCourseArea(UUID eventId, UUID courseAreaId) {
        getService().apply(new RemoveCourseArea(eventId, courseAreaId));
    }

    @Override
    public void removeEvents(Collection<UUID> eventIds) {
        for (UUID eventId : eventIds) {
            removeEvent(eventId);
        }
    }

    @Override
    public void removeEvent(UUID eventId) {
        getService().apply(new RemoveEvent(eventId));
    }

    @Override
    public void renameEvent(UUID eventId, String newName) {
        getService().apply(new RenameEvent(eventId, newName));
    }

    @Override
    public EventDTO getEventById(UUID id, boolean withStatisticalData) throws MalformedURLException {
        EventDTO result = null;
        String requestBaseURL = getRequestBaseURL().toString();
        Event event = getService().getEvent(id);
        if (event != null) {
            result = convertToEventDTO(event, withStatisticalData);
            result.setBaseURL(requestBaseURL);
            result.setIsOnRemoteServer(false);
        }
        return result;
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
        if (event.getLogoImageURL() == null) {
            eventDTO.setLogoImageURL(null);
        } else {
            eventDTO.setLogoImageURL(event.getLogoImageURL().toString());
            setImageSize(event, eventDTO, event.getLogoImageURL());
        }
        for(URL url: event.getSponsorImageURLs()) {
            eventDTO.addSponsorImageURL(url.toString());
            setImageSize(event, eventDTO, url);
        }
        for(URL url: event.getImageURLs()) {
            eventDTO.addImageURL(url.toString());
            setImageSize(event, eventDTO, url);
        }
        for(URL url: event.getVideoURLs()) {
            eventDTO.addVideoURL(url.toString());
        }
    }

    private void setImageSize(EventBase event, EventBaseDTO eventDTO, URL imageURL) {
        try {
            eventDTO.setImageSize(imageURL.toString(), event.getImageSize(imageURL));
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.FINE, "Was unable to obtain image size for "+imageURL+" earlier.", e);
        }
    }
    
    private EventDTO convertToEventDTO(Event event, boolean withStatisticalData) {
        EventDTO eventDTO = new EventDTO(event.getName());
        copyEventBaseFieldsToDTO(event, eventDTO);
        eventDTO.regattas = new ArrayList<RegattaDTO>();
        for (Regatta regatta: event.getRegattas()) {
            RegattaDTO regattaDTO = new RegattaDTO();
            regattaDTO.setName(regatta.getName());
            regattaDTO.races = convertToRaceDTOs(regatta);
            eventDTO.regattas.add(regattaDTO);
        }
        eventDTO.venue.setCourseAreas(new ArrayList<CourseAreaDTO>());
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            CourseAreaDTO courseAreaDTO = convertToCourseAreaDTO(courseArea);
            eventDTO.venue.getCourseAreas().add(courseAreaDTO);
        }
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            eventDTO.addLeaderboardGroup(convertToLeaderboardGroupDTO(lg, /* withGeoLocationData */false, withStatisticalData));
        }
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

    /** the replacement service for getRegattaStructureForEvent() */
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
                        for(Competitor c: leaderboard.getCompetitors()) {
                            if(c.getBoat() != null && c.getBoat().getBoatClass() != null) {
                                raceGroup.boatClass = c.getBoat().getBoatClass().getDisplayName();
                            }
                        }
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
        getService().apply(new RemoveRegatta(regattaIdentifier));
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
            RegattaConfigurationDTO configurationDTO, boolean useStartTimeInference) {
        TimePoint startTimePoint = startDate != null ?  new MillisecondsTimePoint(startDate) : null;
        TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
        getService().apply(new UpdateSpecificRegatta(regattaName, startTimePoint, endTimePoint,
                defaultCourseAreaUuid, convertToRegattaConfiguration(configurationDTO), useStartTimeInference));
    }

    @Override
    public List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName,
            List<String> columnNames) {
        List<RaceColumnInSeriesDTO> result = new ArrayList<RaceColumnInSeriesDTO>();
        for (String columnName : columnNames) {
            RaceColumnInSeries raceColumnInSeries = getService().apply(
                    new AddColumnToSeries(regattaIdentifier, seriesName, columnName));
            if (raceColumnInSeries != null) {
                result.add(convertToRaceColumnInSeriesDTO(raceColumnInSeries));
            }
        }
        return result;
    }
    
    @Override
    public void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            List<FleetDTO> fleets) {
        getService().apply(
                new UpdateSeries(regattaIdentifier, seriesName, newSeriesName, isMedal, resultDiscardingThresholds,
                        startsWithZeroScore, firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring,
                        fleets));
    }

    @Override
    public RaceColumnInSeriesDTO addRaceColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        RaceColumnInSeriesDTO result = null;
        RaceColumnInSeries raceColumnInSeries = getService().apply(new AddColumnToSeries(regattaIdentifier, seriesName, columnName));
        if(raceColumnInSeries != null) {
            result = convertToRaceColumnInSeriesDTO(raceColumnInSeries);
        }
        return result;
    }

    @Override
    public void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames) {
        for(String columnName: columnNames) {
            getService().apply(new RemoveColumnFromSeries(regattaIdentifier, seriesName, columnName));
        }
    }

    @Override
    public void removeRaceColumnFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new RemoveColumnFromSeries(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new MoveColumnInSeriesUp(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName) {
        getService().apply(new MoveColumnInSeriesDown(regattaIdentifier, seriesName, columnName));
    }

    @Override
    public RegattaDTO createRegatta(String regattaName, String boatClassName, Date startDate, Date endDate, 
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, boolean useStartTimeInference) {
        TimePoint startTimePoint = startDate != null ?  new MillisecondsTimePoint(startDate) : null;
        TimePoint endTimePoint = endDate != null ?  new MillisecondsTimePoint(endDate) : null;
        Regatta regatta = getService().apply(
                new AddSpecificRegatta(
                        regattaName, boatClassName, startTimePoint, endTimePoint, UUID.randomUUID(),
                        seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
                        persistent, baseDomainFactory.createScoringScheme(scoringSchemeType), defaultCourseAreaId, useStartTimeInference));
        return convertToRegattaDTO(regatta);
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
    public List<String> getUrlResultProviderNames() {
        List<String> result = new ArrayList<String>();
        for (ScoreCorrectionProvider scp : getAllScoreCorrectionProviders()) {
            if (scp instanceof ResultUrlProvider) {
                result.add(scp.getName());
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
            for (URL url : allUrls) {
                result.add(url.toString());
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
        Iterable<SwissTimingArchiveConfiguration> configs = swissTimingAdapterPersistence.getSwissTimingArchiveConfigurations();
        List<SwissTimingArchiveConfigurationDTO> result = new ArrayList<SwissTimingArchiveConfigurationDTO>();
        for (SwissTimingArchiveConfiguration stArchiveConfig : configs) {
            result.add(new SwissTimingArchiveConfigurationDTO(stArchiveConfig.getJsonUrl()));
        }
        return result;
    }

    @Override
    public void storeSwissTimingArchiveConfiguration(String swissTimingJsonUrl) {
        swissTimingAdapterPersistence.storeSwissTimingArchiveConfiguration(swissTimingFactory.createSwissTimingArchiveConfiguration(
                swissTimingJsonUrl));
    }

    @Override
    public PolarSheetGenerationResponse generatePolarSheetForRaces(List<RegattaAndRaceIdentifier> selectedRaces,
            PolarSheetGenerationSettings settings, String name) throws Exception {
        String id = UUID.randomUUID().toString();
        RacingEventService service = getService();
        Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
        for (RegattaAndRaceIdentifier race : selectedRaces) {
            trackedRaces.add(service.getTrackedRace(race));
        }
        if (name == null || name.isEmpty()) {
            name = getCommonBoatClass(trackedRaces);
        }
        PolarDataService polarDataService = service.getPolarDataService();
        PolarSheetsData result = polarDataService.generatePolarSheet(trackedRaces, settings, executor);
        return new PolarSheetGenerationResponseImpl(id, name, result);
    }

    @Override
    public List<String> getBoatClassNamesWithPolarSheetsAvailable() {
        Set<BoatClass> boatClasses = getService().getPolarDataService().getAllBoatClassesWithPolarSheetsAvailable();
        List<String> names = new ArrayList<String>();
        for (BoatClass boatClass : boatClasses) {
            names.add(boatClass.getDisplayName());
        }
        return names;
    }

    @Override
    public PolarSheetGenerationResponse showCachedPolarSheetForBoatClass(String boatClassName) {
        BoatClass boatClass = getService().getBaseDomainFactory().getOrCreateBoatClass(boatClassName);
        PolarSheetsData data = getService().getPolarDataService().getPolarSheetForBoatClass(boatClass);
        String name = boatClassName + "_OVERALL";
        String id = name;
        return new PolarSheetGenerationResponseImpl(id, name, data);
    }

    private String getCommonBoatClass(Set<TrackedRace> trackedRaces) {
        BoatClass boatClass = null;
        for (TrackedRace race : trackedRaces) {
            if (boatClass == null) {
                boatClass = race.getRace().getBoatClass();
            }
            if (!boatClass.getName().toLowerCase().matches(race.getRace().getBoatClass().getName().toLowerCase())) {
                return "Mixed";
            }
        }

        return boatClass.getName();
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(String leaderboardName,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, final List<String> visibleRegattas)
            throws NoWindException, InterruptedException, ExecutionException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        return getRaceStateEntriesForLeaderboard(leaderboard, showOnlyCurrentlyRunningRaces, showOnlyRacesOfSameDay, visibleRegattas);
    }

    private List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(Leaderboard leaderboard,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, final List<String> visibleRegattas)
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
                for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
                    Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                    for (RaceColumn raceColumn : series.getRaceColumns()) {
                        getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck,
                                usedCourseArea, leaderboard, regattaName, series.getName(), raceColumn, entriesPerFleet);
                    }
                    result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
                }

            } else {
                Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck, usedCourseArea,
                            leaderboard, regattaName, LeaderboardNameConstants.DEFAULT_SERIES_NAME, raceColumn, entriesPerFleet);
                }
                result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
            }
        }
        return result;
    }
    
    private void createRegattaFromRegattaDTO(RegattaDTO regatta) {
        this.createRegatta(regatta.getName(), regatta.boatClass.getName(), regatta.startDate, regatta.endDate,
                        new RegattaCreationParametersDTO(getSeriesCreationParameters(regatta)), 
                        true, regatta.scoringScheme, regatta.defaultCourseAreaUuid, regatta.useStartTimeInference);
    }
    
    private SeriesParameters getSeriesParameters(SeriesDTO seriesDTO) {
        SeriesParameters series = new SeriesParameters(false, false, false, null);
            series
                    .setFirstColumnIsNonDiscardableCarryForward(seriesDTO.isFirstColumnIsNonDiscardableCarryForward());
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
                false, seriesParameters.isStartswithZeroScore(), seriesParameters.isFirstColumnIsNonDiscardableCarryForward(),
                        seriesParameters.getDiscardingThresholds(), seriesParameters.isHasSplitFleetContiguousScoring()));
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
    private void addRaceColumnsToRegattaSeries(RegattaDTO regatta, String eventName) {
        for (SeriesDTO series : regatta.series) {
            List<String> raceNames = new ArrayList<String>();
            for (RaceColumnDTO raceColumnInSeries : series.getRaceColumns()) {
                raceNames.add(raceColumnInSeries.getName());
            }
            addRaceColumnsToSeries(regatta.getRegattaIdentifier(), series.getName(), raceNames);
        }
    }

    @Override
    public void createRegattaStructure(final Iterable<RegattaDTO> regattas, final EventDTO newEvent) throws MalformedURLException {
        final List<String> leaderboardNames = new ArrayList<String>();
        for (RegattaDTO regatta : regattas) {
            createRegattaFromRegattaDTO(regatta);
            addRaceColumnsToRegattaSeries(regatta, newEvent.getName());
            if (getLeaderboard(regatta.getName()) == null) {
                leaderboardNames.add(regatta.getName());
                createRegattaLeaderboard(regatta.getRegattaIdentifier(), regatta.boatClass.toString(), new int[0]);
            }
        }
        createAndAddLeaderboardGroup(newEvent, leaderboardNames);
        // TODO find a way to import the competitors for the selected regattas. You'll need the regattas as Iterable<RegattaResults>
        // structureImporter.setCompetitors(regattas, "");
    }

    private void createAndAddLeaderboardGroup(final EventDTO newEvent, List<String> leaderboardNames) throws MalformedURLException {
        LeaderboardGroupDTO leaderboardGroupDTO = null;
        String description = "";
        if (newEvent.getDescription() != null) {
            description = newEvent.getDescription();
        }
        String eventName = newEvent.getName();
        List<UUID> eventLeaderboardGroupUUIDs = new ArrayList<>();

        // create Leaderboard Group
        if (getService().getLeaderboardGroupByName(eventName) == null) {
            CreateLeaderboardGroup createLeaderboardGroupOp = new CreateLeaderboardGroup(eventName, description,
                    eventName, false, leaderboardNames, null, null);
            leaderboardGroupDTO = convertToLeaderboardGroupDTO(getService().apply(createLeaderboardGroupOp), false,
                    false);
            eventLeaderboardGroupUUIDs.add(leaderboardGroupDTO.getId());
        } else {
            leaderboardNames.addAll(getLeaderboardNames());
            updateLeaderboardGroup(eventName, eventName, newEvent.getDescription(), eventName, leaderboardNames, null, null);
            leaderboardGroupDTO = getLeaderboardGroupByName(eventName, false);
        }
        for (LeaderboardGroupDTO lg : newEvent.getLeaderboardGroups()) {
            eventLeaderboardGroupUUIDs.add(lg.getId());
        }
        updateEvent(newEvent.id, newEvent.getName(), description, newEvent.startDate, newEvent.endDate, newEvent.venue,
                newEvent.isPublic, eventLeaderboardGroupUUIDs, newEvent.getLogoImageURL(),
                newEvent.getOfficialWebsiteURL(), newEvent.getImageURLs(), newEvent.getVideoURLs(),
                newEvent.getSponsorImageURLs());
    }
    
    @Override
    public List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreaIds,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay)
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
                                    showOnlyCurrentlyRunningRaces, showOnlyRacesOfSameDay, visibleRegattas));
                        }
                    }
                }
            }
        }
        return result;
    }

    private void getRegattaOverviewEntries(boolean showOnlyRacesOfSameDay, Calendar dayToCheck,
            CourseArea courseArea, Leaderboard leaderboard, String regattaName, String seriesName, RaceColumn raceColumn,
            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {

        for (Fleet fleet : raceColumn.getFleets()) {
            RegattaOverviewEntryDTO entry = createRegattaOverviewEntryDTO(courseArea,
                    leaderboard, regattaName, seriesName, raceColumn, fleet, 
                    showOnlyRacesOfSameDay, dayToCheck);
            if (entry != null) {
                addRegattaOverviewEntryToEntriesPerFleet(entriesPerFleet, fleet, entry);
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
    
    private RegattaOverviewEntryDTO createRegattaOverviewEntryDTO(CourseArea courseArea, Leaderboard leaderboard,
            String regattaName, String seriesName, RaceColumn raceColumn, Fleet fleet, boolean showOnlyRacesOfSameDay, Calendar dayToCheck) {
        RegattaOverviewEntryDTO entry = new RegattaOverviewEntryDTO();
        if (courseArea != null) {
            entry.courseAreaName = courseArea.getName();
            entry.courseAreaIdAsString = courseArea.getId().toString();
        } else {
            entry.courseAreaName = "Default";
            entry.courseAreaIdAsString = "Default";
        }
        entry.regattaDisplayName = regattaName;
        entry.regattaName = leaderboard.getName();
        entry.raceInfo = createRaceInfoDTO(seriesName, raceColumn, fleet);
        entry.currentServerTime = new Date();
        
        if (showOnlyRacesOfSameDay) {
            if (!RaceStateOfSameDayHelper.isRaceStateOfSameDay(entry.raceInfo.startTime, entry.raceInfo.finishedTime, entry.raceInfo.abortingTimeInPassBefore, dayToCheck)) {
                entry = null;
            }
        }
        return entry;
    }
    
    @Override
    public String getBuildVersion() {
        return BuildVersion.getBuildVersion();
    }

    @Override
    public void stopReplicatingFromMaster() {
        try {
            getReplicationService().stopToReplicateFromMaster();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopAllReplicas() {
        try {
            getReplicationService().stopAllReplica();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopSingleReplicaInstance(String identifier) {
        UUID uuid = UUID.fromString(identifier);
        ReplicaDescriptor replicaDescriptor = new ReplicaDescriptor(null, uuid, "");
        try {
            getReplicationService().unregisterReplica(replicaDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
        getService().reloadRaceLog(leaderboardName, raceColumnDTO.getName(), fleet.getName());
    }

    @Override
    public RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet) {
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

    @Override
    public List<String> getLeaderboardGroupNamesFromRemoteServer(String url) {
        com.sap.sse.common.Util.Pair<String, Integer> hostnameAndPort = parseHostAndPort(url);
        String hostname = hostnameAndPort.getA();
        int port = hostnameAndPort.getB();
        final String path = "/sailingserver/api/v1/leaderboardgroups";
        final String query = null;

        HttpURLConnection connection = null;

        URL serverAddress = null;
        InputStream inputStream = null;
        try {
            serverAddress = createUrl(hostname, port, path, query);
            // set up out communications stuff
            connection = null;
            // Set up the initial connection
            connection = (HttpURLConnection) serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            // Initial timeout needs to be big enough to allow the first parts of the response to reach this server
            connection.setReadTimeout(10000);
            connection.connect();

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
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }

    }

    private com.sap.sse.common.Util.Pair<String, Integer> parseHostAndPort(String urlAsString) {
        String hostname;
        Integer port = 80;
        try {
            URL url = new URL(urlAsString);
            hostname = url.getHost();
            int portFromUrl = url.getPort();
            if (portFromUrl > 0) {
                port = portFromUrl;
            }
        } catch (MalformedURLException e1) {
            hostname = urlAsString;
            if (urlAsString.contains("://")) {
                hostname = hostname.split("://")[1];
            }
            if (hostname.contains("/")) {
                hostname = hostname.split("/")[0]; // also eliminate a trailing slash
            }
            if (hostname.contains(":")) {
                String[] split = hostname.split(":");
                hostname = split[0];
                if (port > 0) {
                    port = Integer.parseInt(split[1]);
                }
            }
        }
        return new com.sap.sse.common.Util.Pair<String, Integer>(hostname, port);
    }

    @Override
    public UUID importMasterData(final String urlAsString, final String[] groupNames, final boolean override,
            final boolean compress, final boolean exportWind) {
        final UUID importOperationId = UUID.randomUUID();
        getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.0, "Initializing", 0.0);
        // Create a progress indicator for as long as the server gets data from the other server.
        // As soon as the server starts the import operation, a progress object will be built on every server
        Runnable masterDataImportTask = new Runnable() {

            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.01,
                        "Setting up connection", 0.5);
                com.sap.sse.common.Util.Pair<String, Integer> hostnameAndPort = parseHostAndPort(urlAsString);
                String hostname = hostnameAndPort.getA();
                int port = hostnameAndPort.getB();
                String query;
                try {
                    query = createLeaderboardQuery(groupNames, compress, exportWind);
                } catch (UnsupportedEncodingException e1) {
                    throw new RuntimeException(e1);
                }
                HttpURLConnection connection = null;

                URL serverAddress = null;
                InputStream inputStream = null;
                try {
                    String path = "/sailingserver/spi/v1/masterdata/leaderboardgroups";
                    serverAddress = createUrl(hostname, port, path, query);
                    // set up out communications stuff
                    connection = null;
                    // Set up the initial connection
                    connection = (HttpURLConnection) serverAddress.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
                    // Initial timeout needs to be big enough to allow the first parts of the response to reach this
                    // server
                    connection.setReadTimeout(60000);
                    connection.connect();
                    getService().createOrUpdateDataImportProgressWithReplication(importOperationId, 0.02, "Connecting",
                            0.5);

                    if (compress) {
                        InputStream timeoutExtendingInputStream = new TimeoutExtendingInputStream(
                                connection.getInputStream(), connection);
                        inputStream = new GZIPInputStream(timeoutExtendingInputStream);
                    } else {
                        inputStream = new TimeoutExtendingInputStream(connection.getInputStream(), connection);
                    }

                    final MasterDataImporter importer = new MasterDataImporter(baseDomainFactory, getService());
                    importer.importFromStream(inputStream, importOperationId, override);
                } catch (Exception e) {
                    getService()
                            .setDataImportFailedWithReplication(
                                    importOperationId,
                                    e.getMessage()
                                            + "\n\nHave you checked if the"
                                            + " versions (commit-wise) of the importing and exporting servers are compatible with each other? "
                                            + "If the error still occurs, when both servers are running the same version, please report the problem.");
                    throw new RuntimeException(e);
                } finally {
                    // close the connection, set all objects to null
                    getService().setDataImportDeleteProgressFromMapTimerWithReplication(importOperationId);
                    connection.disconnect();
                    connection = null;
                    long timeToImport = System.currentTimeMillis() - startTime;
                    logger.info(String.format("Took %s ms overall to import master data.", timeToImport));
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        };
        executor.execute(masterDataImportTask);

        return importOperationId;
    }
    
    private URL createUrl(String host, Integer port, String path, String query) throws Exception {
        URL url;
        if (query != null) {
            url = new URL("http://" + host + ":" + port + path + "?" + query);
        } else {
            url = new URL("http://" + host + ":" + port + path);
        }
        return url;
    }

    public DataImportProgress getImportOperationProgress(UUID id) {
        return getService().getDataImportLock().getProgress(id);
    }

    @Override
    public Integer getStructureImportOperationProgress() {
//        int parsedDocuments = 0;
//        if (structureImporter != null) {
//            parsedDocuments = structureImporter.getProgress();
//            if (structureImporter.isFinished()) {
//                parsedDocuments++;
//            }
//        }
        return 0;
    }

    private String createLeaderboardQuery(String[] groupNames, boolean compress, boolean exportWind)
            throws UnsupportedEncodingException {
        StringBuffer queryStringBuffer = new StringBuffer("");
        for (int i = 0; i < groupNames.length; i++) {
            String encodedGroupName = URLEncoder.encode(groupNames[i], "UTF-8");
            queryStringBuffer.append("names[]=" + encodedGroupName + "&");
        }
        queryStringBuffer.append(String.format("compress=%s&exportWind=%s", compress, exportWind));
        return queryStringBuffer.toString();
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitors() {
        return convertToCompetitorDTOs(getService().getBaseDomainFactory().getCompetitorStore().getCompetitors());
    }

    @Override
    public Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName, boolean lookInRaceLogs) {
        if (lookInRaceLogs) {
            Set<Competitor> result = new HashSet<Competitor>();
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    RaceLog raceLog = raceColumn.getRaceLog(fleet);
                    result.addAll(new RegisteredCompetitorsAnalyzer<>(raceLog).analyze());
                }
            }
            return convertToCompetitorDTOs(result);
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            return convertToCompetitorDTOs(leaderboard.getAllCompetitors());
        }
    }

    @Override
    public CompetitorDTO addOrUpdateCompetitor(CompetitorDTO competitor) throws URISyntaxException {
        Competitor existingCompetitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitor.getIdAsString());
    	Nationality nationality = (competitor.getThreeLetterIocCountryCode() == null || competitor.getThreeLetterIocCountryCode().isEmpty()) ? null :
            getBaseDomainFactory().getOrCreateNationality(competitor.getThreeLetterIocCountryCode());
    	final CompetitorDTO result;
    	// new competitor
    	if (competitor.getIdAsString() == null || competitor.getIdAsString().isEmpty() || existingCompetitor == null) {
    	    BoatClass boatClass = getBaseDomainFactory().getOrCreateBoatClass(competitor.getBoatClass().getName());
    	    DynamicPerson sailor = new PersonImpl(competitor.getName(), nationality, null, null);
    	    DynamicTeam team = new TeamImpl(competitor.getName() + " team", Collections.singleton(sailor), null);
    	    DynamicBoat boat = new BoatImpl(competitor.getName() + " boat", boatClass, competitor.getSailID());
            result = getBaseDomainFactory().convertToCompetitorDTO(
                    getBaseDomainFactory().getOrCreateCompetitor(UUID.randomUUID(), competitor.getName(),
                            competitor.getColor(), competitor.getEmail(), team, boat));
        } else {
            result = getBaseDomainFactory().convertToCompetitorDTO(
                    getService().apply(
                            new UpdateCompetitor(competitor.getIdAsString(), competitor.getName(), competitor
                                    .getColor(), competitor.getEmail(), competitor.getSailID(), nationality,
                                    competitor.getImageURL()==null?null:new URI(competitor.getImageURL()))));
        }
        return result;
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
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDto.type, matcherDto.clients);
        DeviceConfiguration configuration = getService().getAllDeviceConfigurations().get(matcher);
        if (configuration == null) {
            return null;
        } else {
            return convertToDeviceConfigurationDTO(configuration);
        }
    }

    @Override
    public DeviceConfigurationMatcherDTO createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(matcherDTO.type, matcherDTO.clients);
        DeviceConfiguration configuration = convertToDeviceConfiguration(configurationDTO);
        getService().createOrUpdateDeviceConfiguration(matcher, configuration);
        return convertToDeviceConfigurationMatcherDTO(matcher);
    }

    @Override
    public boolean removeDeviceConfiguration(DeviceConfigurationMatcherType type, List<String> clientIds) {
        DeviceConfigurationMatcher matcher = convertToDeviceConfigurationMatcher(type, clientIds);
        getService().removeDeviceConfiguration(matcher);
        return true;
    }

    private DeviceConfigurationMatcherDTO convertToDeviceConfigurationMatcherDTO(DeviceConfigurationMatcher matcher) {
        List<String> clients = new ArrayList<String>();
        
        if (matcher instanceof DeviceConfigurationMatcherSingle) {
            clients.add(((DeviceConfigurationMatcherSingle)matcher).getClientIdentifier());
        } else if (matcher instanceof DeviceConfigurationMatcherMulti) {
            Util.addAll(((DeviceConfigurationMatcherMulti)matcher).getClientIdentifiers(), clients);
        }
        
        DeviceConfigurationMatcherDTO dto = new DeviceConfigurationMatcherDTO(
                matcher.getMatcherType(),
                clients,  
                matcher.getMatchingRank());
        return dto;
    }

    private DeviceConfigurationMatcher convertToDeviceConfigurationMatcher(DeviceConfigurationMatcherType type, List<String> clientIds) {
        return baseDomainFactory.getOrCreateDeviceConfigurationMatcher(type, clientIds);
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
        
        if (configuration.getRRS26Configuration() != null) {
            dto.rrs26Configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RRS26ConfigurationDTO();
            dto.rrs26Configuration.classFlag = configuration.getRRS26Configuration().getClassFlag();
            dto.rrs26Configuration.hasInidividualRecall = configuration.getRRS26Configuration().hasInidividualRecall();
            dto.rrs26Configuration.startModeFlags = configuration.getRRS26Configuration().getStartModeFlags();
        }
        if (configuration.getGateStartConfiguration() != null) {
            dto.gateStartConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.GateStartConfigurationDTO();
            dto.gateStartConfiguration.classFlag = configuration.getGateStartConfiguration().getClassFlag();
            dto.gateStartConfiguration.hasInidividualRecall = configuration.getGateStartConfiguration().hasInidividualRecall();
            dto.gateStartConfiguration.hasPathfinder = configuration.getGateStartConfiguration().hasPathfinder();
            dto.gateStartConfiguration.hasAdditionalGolfDownTime = configuration.getGateStartConfiguration().hasAdditionalGolfDownTime();
        }
        if (configuration.getESSConfiguration() != null) {
            dto.essConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.ESSConfigurationDTO();
            dto.essConfiguration.classFlag = configuration.getESSConfiguration().getClassFlag();
            dto.essConfiguration.hasInidividualRecall = configuration.getESSConfiguration().hasInidividualRecall();
        }
        if (configuration.getBasicConfiguration() != null) {
            dto.basicConfiguration = new DeviceConfigurationDTO.RegattaConfigurationDTO.RacingProcedureConfigurationDTO();
            dto.basicConfiguration.classFlag = configuration.getBasicConfiguration().getClassFlag();
            dto.basicConfiguration.hasInidividualRecall = configuration.getBasicConfiguration().hasInidividualRecall();
        }
        return dto;
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
        if (dto.rrs26Configuration != null) {
            RRS26ConfigurationImpl config = new RRS26ConfigurationImpl();
            config.setClassFlag(dto.rrs26Configuration.classFlag);
            config.setHasInidividualRecall(dto.rrs26Configuration.hasInidividualRecall);
            config.setStartModeFlags(dto.rrs26Configuration.startModeFlags);
            configuration.setRRS26Configuration(config);
        }
        if (dto.gateStartConfiguration != null) {
            GateStartConfigurationImpl config = new GateStartConfigurationImpl();
            config.setClassFlag(dto.gateStartConfiguration.classFlag);
            config.setHasInidividualRecall(dto.gateStartConfiguration.hasInidividualRecall);
            config.setHasPathfinder(dto.gateStartConfiguration.hasPathfinder);
            config.setHasAdditionalGolfDownTime(dto.gateStartConfiguration.hasAdditionalGolfDownTime);
            configuration.setGateStartConfiguration(config);
        }
        if (dto.essConfiguration != null) {
            ESSConfigurationImpl config = new ESSConfigurationImpl();
            config.setClassFlag(dto.essConfiguration.classFlag);
            config.setHasInidividualRecall(dto.essConfiguration.hasInidividualRecall);
            configuration.setESSConfiguration(config);
        }
        if (dto.basicConfiguration != null) {
            RacingProcedureConfigurationImpl config = new RacingProcedureConfigurationImpl();
            config.setClassFlag(dto.basicConfiguration.classFlag);
            config.setHasInidividualRecall(dto.basicConfiguration.hasInidividualRecall);
            configuration.setBasicConfiguration(config);
        }
        return configuration;
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
    public com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName) {
        com.sap.sse.common.Util.Triple<TimePoint, Integer, RacingProcedureType> result = getService().getStartTimeAndProcedure(leaderboardName, raceColumnName, fleetName);
        if (result == null || result.getA() == null) {
            return null;
        }
        return new com.sap.sse.common.Util.Triple<Date, Integer, RacingProcedureType>(result.getA() == null ? null : result.getA().asDate(), result.getB(), result.getC());
    }

    @Override
    public Iterable<String> getAllIgtimiAccountEmailAddresses() {
        List<String> result = new ArrayList<String>();
        for (Account account : getIgtimiConnectionFactory().getAllAccounts()) {
            result.add(account.getUser().getEmail());
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
    public String getIgtimiAuthorizationUrl() {
        return getIgtimiConnectionFactory().getAuthorizationUrl();
    }

    @Override
    public boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws Exception {
        Account account = getIgtimiConnectionFactory().createAccountToAccessUserData(eMailAddress, password);
        return account != null;
    }

    @Override
    public void removeIgtimiAccount(String eMailOfAccountToRemove) {
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
        return numberOfWindFixesImportedPerRace;
    }

    private Set<DynamicTrackedRace> getAllTrackedRaces() {
        Set<DynamicTrackedRace> result = new HashSet<DynamicTrackedRace>();
        Iterable<Regatta> allRegattas = getService().getAllRegattas();
        for (Regatta regatta : allRegattas) {
            DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
            if (trackedRegatta != null) {
                Iterable<DynamicTrackedRace> trackedRaces = trackedRegatta.getTrackedRaces();
                for (TrackedRace trackedRace : trackedRaces) {
                    result.add((DynamicTrackedRace) trackedRace);
                }
            }
        }
        return result;
    }

    private class TimeoutExtendingInputStream extends FilterInputStream {

        private final HttpURLConnection connection;

        protected TimeoutExtendingInputStream(InputStream in, HttpURLConnection connection) {
            super(in);
            this.connection = connection;
        }

        @Override
        public int read() throws IOException {
            connection.setReadTimeout(10000);
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            connection.setReadTimeout(10000);
            return super.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            connection.setReadTimeout(10000);
            return super.read(b, off, len);
        }

    }

    @Override
    public void denoteForRaceLogTracking(String leaderboardName,
    		String raceColumnName, String fleetName) throws Exception {
    	Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
    	RaceColumn raceColumn = getService().getLeaderboardByName(leaderboardName).getRaceColumnByName(raceColumnName);
    	Fleet fleet = raceColumn.getFleetByName(fleetName);
    	
    	getRaceLogTrackingAdapter().denoteRaceForRaceLogTracking(getService(), leaderboard, raceColumn, fleet, null);
    }
    
    @Override
    public void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        getRaceLogTrackingAdapter().removeDenotationForRaceLogTracking(getService(), raceLog);
    }
    
    @Override
    public void denoteForRaceLogTracking(String leaderboardName) throws Exception {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);        
        getRaceLogTrackingAdapter().denoteAllRacesForRaceLogTracking(getService(), leaderboard);
    }
    
    /**
     * @param triple leaderboard and racecolumn and fleet names
     * @return
     */
    private RaceLog getRaceLog(com.sap.sse.common.Util.Triple<String, String, String> triple) {
        return getRaceLog(triple.getA(), triple.getB(), triple.getC());
    }
    
    private RegattaLog getRegattaLogInternal(String leaderboardName) throws DoesNotHaveRegattaLogException {
        Leaderboard l = getService().getLeaderboardByName(leaderboardName);
        if (! (l instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        return ((HasRegattaLike) l).getRegattaLike().getRegattaLog();
    }
    
    private RaceLog getRaceLog(String leaderboardName, String raceColumnName, String fleetName) {
        RaceColumn raceColumn = getService().getLeaderboardByName(leaderboardName).getRaceColumnByName(raceColumnName);
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        return raceColumn.getRaceLog(fleet);
    }
    
    @Override
    public Collection<CompetitorDTO> getCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName) {
        return convertToCompetitorDTOs(
                new RegisteredCompetitorsAnalyzer<>(
                        getRaceLog(leaderboardName, raceColumnName, fleetName)).analyze());
    }
    
    @Override
    public Collection<CompetitorDTO> getCompetitorRegistrations(String leaderboardName)
            throws DoesNotHaveRegattaLogException {
        return convertToCompetitorDTOs(
                new RegisteredCompetitorsAnalyzer<>(
                        getRegattaLogInternal(leaderboardName)).analyze());
    }
    
    private Competitor getCompetitor(CompetitorDTO dto) {
        return getService().getCompetitorStore().getExistingCompetitorByIdAsString(dto.getIdAsString());
    }
    
    @Override
    public void setCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorDTO> competitorDTOs) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Set<Competitor> competitors = new HashSet<Competitor>();
        for (CompetitorDTO dto : competitorDTOs) {
            competitors.add(getCompetitor(dto));
        }
        
        getRaceLogTrackingAdapter().registerCompetitors(getService(), raceLog, competitors);
    }
    
    @Override
    public void setCompetitorRegistrations(String leaderboardName, Set<CompetitorDTO> competitorDTOs)
            throws DoesNotHaveRegattaLogException {
        RegattaLog regattaLog = getRegattaLogInternal(leaderboardName);
        Set<Competitor> competitors = new HashSet<Competitor>();
        for (CompetitorDTO dto : competitorDTOs) {
            competitors.add(getCompetitor(dto));
        }
        
        getRaceLogTrackingAdapter().registerCompetitors(getService(), regattaLog, competitors);
    }
    
    private Mark convertToMark(MarkDTO dto, boolean resolve) {
        if (resolve) {
            Mark existing = baseDomainFactory.getExistingMarkByIdAsString(dto.getIdAsString());
            if (existing != null) {
                return existing;
            }
        }
        Serializable id = UUID.randomUUID();
        return baseDomainFactory.getOrCreateMark(id, dto.getName(), dto.type, dto.color, dto.shape, dto.pattern);
    }
    
    /**
     * Also finds the last position of the marks, if set by pinging them
     */
    private Collection<MarkDTO> convertToMarkDTOs(LeaderboardThatHasRegattaLike leaderboard, RaceLog raceLog, Iterable<Mark> marks) {
        List<MarkDTO> dtos = new ArrayList<MarkDTO>();
        final TimePoint now = MillisecondsTimePoint.now();
        for (Mark mark : marks) {
            final Position lastPos = getService().getMarkPosition(mark, leaderboard, now, raceLog);
            dtos.add(convertToMarkDTO(mark, lastPos));
        }
        return dtos;
    }
    
    @Override
    public void addMarkToRaceLog(String leaderboardName, String raceColumnName, String fleetName, MarkDTO markDTO) {
        Mark mark = convertToMark(markDTO, false);
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createDefineMarkEvent(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), raceLog.getCurrentPassId(), mark);
        raceLog.add(event);
    }
    
    @Override
    public Iterable<MarkDTO> getMarksInRaceLog(String leaderboardName, String raceColumnName, String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Iterable<Mark> marks = new DefinedMarkFinder(raceLog).analyze();
        return convertToMarkDTOs((LeaderboardThatHasRegattaLike) getService().getLeaderboardByName(leaderboardName), raceLog, marks);
    }
    
    @Override
    public Iterable<MarkDTO> getMarksInRaceLogsAndTrackedRaces(String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            // TODO: implement proper Exception Handling
            return null;
        }
        Set<MarkDTO> markDTOs = new HashSet<>();
        Map<Serializable, Mark> marksById = new HashMap<>();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (Fleet fleet : raceColumn.getFleets()) {
                RaceLog raceLog = raceColumn.getRaceLog(fleet);
                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet); //might not yet be attached
                for (Mark markDefinitionFromRaceLog : new DefinedMarkFinder(raceLog).analyze()) {
                    marksById.put(markDefinitionFromRaceLog.getId(), markDefinitionFromRaceLog);
                }
                if (trackedRace != null) {
                    for (Mark markFromTrackedRace : trackedRace.getMarks()) {
                        marksById.put(markFromTrackedRace.getId(), markFromTrackedRace);
                    }
                }
                Util.addAll(convertToMarkDTOs(
                                (LeaderboardThatHasRegattaLike) getService().getLeaderboardByName(leaderboardName),
                                raceLog, marksById.values()), markDTOs);
            }
        }
        return markDTOs;
    }

    @Override
    public void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<com.sap.sse.common.Util.Pair<ControlPointDTO, PassingInstruction>> courseDTO) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        String name = String.format("Course for %s - %s - %s", leaderboardName, raceColumnName, fleetName);
        
        CourseBase lastPublishedCourse = new LastPublishedCourseDesignFinder(raceLog).analyze();
        if (lastPublishedCourse == null) {
            lastPublishedCourse = new CourseDataImpl(name);
        }
        
        List<Pair<ControlPoint, PassingInstruction>> controlPoints = new ArrayList<>();
        for (Pair<ControlPointDTO, PassingInstruction> waypointDTO : courseDTO) {
            controlPoints.add(new Pair<>(getOrCreateControlPoint(waypointDTO.getA()), waypointDTO.getB()));
        }
        Course course = new CourseImpl(name, lastPublishedCourse.getWaypoints());
        
        try {
            course.update(controlPoints, baseDomainFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), raceLog.getCurrentPassId(), course);
        raceLog.add(event);
    }
    
    private WaypointDTO convertToWaypointDTO(Waypoint waypoint, Map<Serializable, ControlPointDTO> controlPointCache) {
        ControlPointDTO cp = controlPointCache.get(waypoint.getControlPoint().getId());
        if (cp == null) {
            cp = convertToControlPointDTO(waypoint.getControlPoint());
            controlPointCache.put(waypoint.getControlPoint().getId(), cp);
        }
        return new WaypointDTO(waypoint.getName(), cp, waypoint.getPassingInstructions());
    }
    
    private RaceCourseDTO convertToRaceCourseDTO(CourseBase course) {
        List<WaypointDTO> waypointDTOs = new ArrayList<WaypointDTO>();
        Map<Serializable, ControlPointDTO> controlPointCache = new HashMap<>();
        RaceCourseDTO result = new RaceCourseDTO(waypointDTOs);
        for (Waypoint waypoint : course.getWaypoints()) {
            waypointDTOs.add(convertToWaypointDTO(waypoint, controlPointCache));
        }
        return result;
    }
    
    @Override
    public RaceCourseDTO getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName,
            String fleetName) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        
        CourseBase lastPublishedCourse = new LastPublishedCourseDesignFinder(raceLog).analyze();
        if (lastPublishedCourse == null) {
            lastPublishedCourse = new CourseDataImpl("");
        }
        
        return convertToRaceCourseDTO(lastPublishedCourse);
    }
    
    private Position convertToPosition(PositionDTO dto) {
        return new DegreePosition(dto.latDeg, dto.lngDeg);
    }
    
    @Override
    public void pingMarkViaRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName,
            MarkDTO markDTO, PositionDTO positionDTO) {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        Mark mark = convertToMark(markDTO, true);
        TimePoint time = MillisecondsTimePoint.now();
        Position position = convertToPosition(positionDTO);
        GPSFix fix = new GPSFixImpl(position, time);
        
        getRaceLogTrackingAdapter().pingMark(raceLog, mark, fix, getService());
    }
    
    @Override
    public void copyCourseAndCompetitorsToOtherRaceLogs(com.sap.sse.common.Util.Triple<String, String, String> fromTriple,
            Set<com.sap.sse.common.Util.Triple<String, String, String>> toTriples) {
        RaceLog fromRaceLog = getRaceLog(fromTriple);
        Set<RaceLog> toRaceLogs = new HashSet<>();
        for (com.sap.sse.common.Util.Triple<String, String, String> toTriple : toTriples) {
            toRaceLogs.add(getRaceLog(toTriple));
        }
        getRaceLogTrackingAdapter().copyCourseAndCompetitors(fromRaceLog, toRaceLogs, baseDomainFactory, getService());
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
        String deviceId = serializeDeviceIdentifier(mapping.getDevice());
        Date from = mapping.getTimeRange().from() == null || mapping.getTimeRange().from().asMillis() == Long.MIN_VALUE ? 
                null : mapping.getTimeRange().from().asDate();
        Date to = mapping.getTimeRange().to() == null || mapping.getTimeRange().to().asMillis() == Long.MAX_VALUE ?
                null : mapping.getTimeRange().to().asDate();
        MappableToDevice item = null;
        if (mapping.getMappedTo() instanceof Competitor) {
            item = baseDomainFactory.convertToCompetitorDTO((Competitor) mapping.getMappedTo());
        } else if (mapping.getMappedTo() instanceof Mark) {
            item = convertToMarkDTO((Mark) mapping.getMappedTo(), null);
        } else {
            throw new RuntimeException("Can only handle Competitor or Mark as mapped item type");
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
                deviceId), from, to, item, originalRaceLogEventUUIDs);
    }
    
    private List<AbstractLog<?, ?>> getLogHierarchy(String leaderboardName, String raceColumnName,
            String fleetName) {
        List<AbstractLog<?, ?>> result = new ArrayList<>();
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        result.add(raceLog);
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard instanceof HasRegattaLike) {
            result.add(((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog());
        }
        return result;
    }
    
    private List<DeviceMappingDTO> getDeviceMappingsFromLogs(List<AbstractLog<?, ?>> logs)
            throws TransformationException {
        List<DeviceMappingDTO> result = new ArrayList<DeviceMappingDTO>();
        for (List<? extends DeviceMapping<Competitor>> list : new MultiLogAnalyzer<>(
                new DeviceCompetitorMappingFinder.Factory(),
                new MultiLogAnalyzer.MapWithValueCollectionReducer<Competitor, DeviceMapping<Competitor>, List<DeviceMapping<Competitor>>>(),
                logs).analyze().values()) {
            for (DeviceMapping<Competitor> mapping : list) {
                result.add(convertToDeviceMappingDTO(mapping));
            }
        }
        for (List<? extends DeviceMapping<Mark>> list : new MultiLogAnalyzer<>(
                new DeviceMarkMappingFinder.Factory(),
                new MultiLogAnalyzer.MapWithValueCollectionReducer<Mark, DeviceMapping<Mark>, List<DeviceMapping<Mark>>>(),
                logs).analyze().values()) {
            for (DeviceMapping<Mark> mapping : list) {
                result.add(convertToDeviceMappingDTO(mapping));
            }
        }
        return result;
    }
    
    @Override
    public List<DeviceMappingDTO> getDeviceMappingsFromLogHierarchy(String leaderboardName, String raceColumnName,
            String fleetName) throws TransformationException {
        return getDeviceMappingsFromLogs(getLogHierarchy(leaderboardName, raceColumnName, fleetName));
    }
    
    @Override
    public List<DeviceMappingDTO> getDeviceMappingsFromRaceLog(String leaderboardName, String raceColumnName,
            String fleetName) throws TransformationException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        return getDeviceMappingsFromLogs(Collections.<AbstractLog<?, ?>>singletonList(raceLog));
    }
    
    @Override
    public void addDeviceMappingToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO dto) throws NoCorrespondingServiceRegisteredException, TransformationException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        DeviceMapping<?> mapping = convertToDeviceMapping(dto);
        TimePoint now = MillisecondsTimePoint.now();
        RaceLogEvent event = null;
        TimePoint from = mapping.getTimeRange().hasOpenBeginning() ? null : mapping.getTimeRange().from();
        TimePoint to = mapping.getTimeRange().hasOpenEnd() ? null : mapping.getTimeRange().to();
        if (dto.mappedTo instanceof MarkDTO) {
            Mark mark = convertToMark(((MarkDTO) dto.mappedTo), true); 
            event = RaceLogEventFactory.INSTANCE.createDeviceMarkMappingEvent(now, getService().getServerAuthor(),
                    mapping.getDevice(), mark, raceLog.getCurrentPassId(), from, to);
        } else if (dto.mappedTo instanceof CompetitorDTO) {
            Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(
                    ((CompetitorDTO) dto.mappedTo).getIdAsString());
            event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(now, getService().getServerAuthor(),
                    mapping.getDevice(), competitor, raceLog.getCurrentPassId(), from, to);
        } else {
            throw new RuntimeException("Can only map devices to competitors or marks");
        }
        raceLog.add(event);
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
    
    DeviceMapping<?> convertToDeviceMapping(DeviceMappingDTO dto) throws NoCorrespondingServiceRegisteredException, TransformationException {
        DeviceIdentifier device = deserializeDeviceIdentifier(dto.deviceIdentifier.deviceType, dto.deviceIdentifier.deviceId);
        TimePoint from = dto.from == null ? null : new MillisecondsTimePoint(dto.from);
        TimePoint to = dto.to == null ? null : new MillisecondsTimePoint(dto.to);
        TimeRange timeRange = new TimeRangeImpl(from, to);
        if (dto.mappedTo instanceof MarkDTO) {
            Mark mark = convertToMark(((MarkDTO) dto.mappedTo), true);
            //expect UUIDs
            return new DeviceMappingImpl<Mark>(mark, device, timeRange, dto.originalRaceLogEventIds);
        } else if (dto.mappedTo instanceof CompetitorDTO) {
            Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(
                    ((CompetitorDTO) dto.mappedTo).getIdAsString());
            return new DeviceMappingImpl<Competitor>(competitor, device, timeRange, dto.originalRaceLogEventIds);
        } else {
            throw new RuntimeException("Can only map devices to competitors or marks");
        }
    }
    
    @Override
    public void closeOpenEndedDeviceMapping(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO mappingDTO, Date closingTimePoint) throws NoCorrespondingServiceRegisteredException, TransformationException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        DeviceMapping<?> mapping = convertToDeviceMapping(mappingDTO);
        List<RaceLogCloseOpenEndedDeviceMappingEvent> closingEvents =
                new RaceLogOpenEndedDeviceMappingCloser(raceLog, mapping, getService().getServerAuthor(),
                new MillisecondsTimePoint(closingTimePoint)).analyze();
        
        for (RaceLogEvent event : closingEvents) {
            raceLog.add(event);            
        }
    }
    
    @Override
    public void revokeRaceLogEvents(String leaderboardName, String raceColumnName, String fleetName,
            List<UUID> eventIds) throws NotRevokableException {
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        for (Serializable idToRevoke : eventIds) {
            raceLog.lockForRead();
            RaceLogEvent event = raceLog.getEventById(idToRevoke);
            raceLog.unlockAfterRead();
            if (event instanceof Revokable) {
                raceLog.revokeEvent(getService().getServerAuthor(), event, "revoke triggered by GWT user action");
            }
        }
    }
    
    @Override
    public void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, final boolean trackWind, final boolean correctWindByDeclination)
            throws NotDenotedForRaceLogTrackingException, Exception {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        final RaceHandle raceHandle = getRaceLogTrackingAdapter().startTracking(getService(), leaderboard, raceColumn, fleet);
        if (raceHandle != null && trackWind) {
            new Thread("Wind tracking starter for race " + leaderboardName + "/" + raceColumnName + "/" + fleetName) {
                public void run() {
                    try {
                        startTrackingWind(raceHandle, correctWindByDeclination,
                                RaceTracker.TIMEOUT_FOR_RECEIVING_RACE_DEFINITION_IN_MILLISECONDS);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();
        }
    }
    
    @Override
    public Collection<String> getGPSFixImporterTypes() {
        Set<String> result = new HashSet<>();
        Collection<ServiceReference<GPSFixImporter>> refs = Collections.emptyList();
        try {
            refs = Activator.getDefault(). getServiceReferences(GPSFixImporter.class, null);
        } catch (InvalidSyntaxException e) {
            //shouldn't happen, as we are passing null for the filter
        }
        for (ServiceReference<GPSFixImporter> ref : refs) {
            result.add((String) ref.getProperty(TypeBasedServiceFinder.TYPE));
        }
        return result;
    }
    
    @Override
    public List<TrackFileImportDeviceIdentifierDTO> getTrackFileImportDeviceIds(List<String> uuids)
            throws NoCorrespondingServiceRegisteredException, TransformationException {
        List<TrackFileImportDeviceIdentifierDTO> result = new ArrayList<>();
        for (String uuidAsString : uuids) {
            UUID uuid = UUID.fromString(uuidAsString);
            TrackFileImportDeviceIdentifier device = TrackFileImportDeviceIdentifierImpl.getOrCreate(uuid);
            long numFixes = getService().getGPSFixStore().getNumberOfFixes(device);
            TimeRange timeRange = getService().getGPSFixStore().getTimeRangeCoveredByFixes(device);
            Date from = timeRange == null ? null : timeRange.from().asDate();
            Date to = timeRange == null ? null : timeRange.to().asDate();
            result.add(new TrackFileImportDeviceIdentifierDTO(uuidAsString, device.getFileName(), device.getTrackName(),
                    numFixes, from, to));
        }
        return result;
    }

    @Override
    public Iterable<String> getSearchServerNames() {
        List<String> result = new ArrayList<>();
        for (RemoteSailingServerReference remoteServerRef : getService().getLiveRemoteServerReferences()) {
            result.add(remoteServerRef.getName());
        }
        return result;
    }

    @Override
    public Iterable<LeaderboardSearchResultDTO> search(String serverNameOrNullForMain, KeywordQuery query) throws MalformedURLException {
        final List<LeaderboardSearchResultDTO> result = new ArrayList<>();
        if (serverNameOrNullForMain == null) {
            Result<LeaderboardSearchResult> searchResult = getService().search(query);
            for (LeaderboardSearchResult hit : searchResult.getHits()) {
                result.add(createLeaderboardSearchResultDTO(hit, getRequestBaseURL(), false));
            }
        } else {
            RemoteSailingServerReference remoteRef = getService().getRemoteServerReferenceByName(serverNameOrNullForMain);
            for (LeaderboardSearchResultBase hit : getService().searchRemotely(serverNameOrNullForMain, query).getHits()) {
                result.add(createLeaderboardSearchResultDTO(hit, remoteRef.getURL(), true));
            }
        }
        return result;
    }

    private LeaderboardSearchResultDTO createLeaderboardSearchResultDTO(LeaderboardSearchResultBase leaderboardSearchResult, URL baseURL,
            boolean isOnRemoteServer) {
        ArrayList<LeaderboardGroupBaseDTO> leaderboardGroups = new ArrayList<>();
        for (LeaderboardGroupBase lgb : leaderboardSearchResult.getLeaderboardGroups()) {
            LeaderboardGroupBaseDTO leaderboardGroupDTO = convertToLeaderboardGroupBaseDTO(lgb);
            leaderboardGroups.add(leaderboardGroupDTO);
        }
        return new LeaderboardSearchResultDTO(baseURL.toString(), isOnRemoteServer, leaderboardSearchResult.getLeaderboard().getName(),
                leaderboardSearchResult.getLeaderboard().getDisplayName(), leaderboardSearchResult.getRegattaName(),
                leaderboardSearchResult.getBoatClassName(), convertToEventDTO(leaderboardSearchResult.getEvent()),
                leaderboardGroups);
    }

    @Override
    public RaceDTO setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived) {
        if (newStartTimeReceived != null) {
            RegattaNameAndRaceName regattaAndRaceIdentifier = new RegattaNameAndRaceName(
                    raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
            DynamicTrackedRace trackedRace = getService().getTrackedRace(regattaAndRaceIdentifier);
            trackedRace.setStartTimeReceived(new MillisecondsTimePoint(newStartTimeReceived));
            
            return baseDomainFactory.createRaceDTO(getService(), false, regattaAndRaceIdentifier, trackedRace);
        }
        return null;
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
    public PolarSheetsXYDiagramData createXYDiagramForBoatClass(String boatClassName) {
        BoatClass boatClass = getService().getBaseDomainFactory().getOrCreateBoatClass(boatClassName);
        Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> movingAverageSpeedDataLists = new HashMap<>();
        Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> regressionSpeedDataLists = new HashMap<>();
        Map<Pair<LegType, Tack>, List<Pair<Double, Double>>> averageConfidenceDataLists = new HashMap<>();
        for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
            for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {
                movingAverageSpeedDataLists.put(new Pair<LegType, Tack>(legType, tack),
                        new ArrayList<Pair<Double, Double>>());
                regressionSpeedDataLists.put(new Pair<LegType, Tack>(legType, tack),
                        new ArrayList<Pair<Double, Double>>());
                averageConfidenceDataLists.put(new Pair<LegType, Tack>(legType, tack),
                        new ArrayList<Pair<Double, Double>>());
            }
        }
        for (double windInKnots = 0.1; windInKnots < 30; windInKnots = windInKnots + 0.1) {
            for (LegType legType : new LegType[] { LegType.UPWIND, LegType.DOWNWIND }) {
                for (Tack tack : new Tack[] { Tack.PORT, Tack.STARBOARD }) {

                    try {
                        SpeedWithBearingWithConfidence<Void> averageUpwindStarboardMovingAverage = getService()
                                .getPolarDataService().getAverageSpeedWithBearing(boatClass,
                                        new KnotSpeedImpl(windInKnots), legType, tack, false);

                        movingAverageSpeedDataLists.get(new Pair<LegType, Tack>(legType, tack)).add(
                                new Pair<Double, Double>(windInKnots, averageUpwindStarboardMovingAverage.getObject()
                                        .getKnots()));
                        

                        averageConfidenceDataLists.get(new Pair<LegType, Tack>(legType, tack)).add(
                                new Pair<Double, Double>(windInKnots, averageUpwindStarboardMovingAverage
                                        .getConfidence()));
                        
                    } catch (NotEnoughDataHasBeenAddedException e) {
                        // Do not add a point to the result
                    }
                    
                    try {
                        SpeedWithBearingWithConfidence<Void> averageUpwindStarboardRegression = getService()
                                .getPolarDataService().getAverageSpeedWithBearing(boatClass,
                                        new KnotSpeedImpl(windInKnots), legType, tack, true);

                        regressionSpeedDataLists.get(new Pair<LegType, Tack>(legType, tack)).add(
                                new Pair<Double, Double>(windInKnots, averageUpwindStarboardRegression.getObject()
                                        .getKnots()));
                    } catch (NotEnoughDataHasBeenAddedException e) {
                        // Do not add a point to the result
                    }
                }
            }
        }

        PolarSheetsXYDiagramData data = new PolarSheetsXYDiagramDataImpl(movingAverageSpeedDataLists,
                regressionSpeedDataLists, averageConfidenceDataLists);

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
        for (FileStorageService s : getService().getFileStorageManagementService().getAvailableFileStorageServices()) {
            serviceDtos.add(FileStorageServiceDTOUtils.convert(s, getLocale(localeInfoName)));
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
            String leaderboardName, Set<CompetitorDTO> competitorDtos, String localeInfoName) throws MailException {
        Event event = getService().getEvent(eventDto.id);
        Set<Competitor> competitors = new HashSet<>();
        for (CompetitorDTO c : competitorDtos) {
            competitors.add(getCompetitor(c));
        }
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        getRaceLogTrackingAdapter().inviteCompetitorsForTrackingViaEmail(event, leaderboard, serverUrlWithoutTrailingSlash,
                competitors, getLocale(localeInfoName));
    }
    
    @Override
    public void inviteBuoyTenderViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto,
            String leaderboardName, String emails, String localeInfoName) throws MailException {
        Event event = getService().getEvent(eventDto.id);
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        getRaceLogTrackingAdapter().inviteBuoyTenderViaEmail(event, leaderboard, serverUrlWithoutTrailingSlash,
                emails, getLocale(localeInfoName));
    }
}
