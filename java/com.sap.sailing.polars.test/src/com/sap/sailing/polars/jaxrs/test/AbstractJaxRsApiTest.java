package com.sap.sailing.polars.jaxrs.test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.polars.impl.PolarDataServiceImpl;
import com.sap.sailing.polars.jaxrs.AbstractPolarResource;
import com.sap.sailing.polars.mining.CubicRegressionPerCourseProcessor;
import com.sap.sailing.polars.mining.GPSFixMovingWithPolarContext;
import com.sap.sailing.polars.mining.PolarDataMiner;
import com.sap.sailing.polars.mining.SpeedRegressionPerAngleClusterProcessor;
import com.sap.sailing.polars.mining.test.PolarDataMinerTest;
import com.sap.sse.datamining.data.ClusterGroup;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

/**
 * Abstract super class for testing. Extends {@link PolarDataMinerTest} to avoid code duplication as it has few mocking
 * methods
 */
public abstract class AbstractJaxRsApiTest extends PolarDataMinerTest {
    protected PolarDataService polarDataService;
    private GroupKey groupKey = new GenericGroupKey<>("1");

    public void setUp() {
        PolarSheetGenerationSettings settings = createTestPolarSettings();
        ClusterGroup<Bearing> angleClusterGroup = createAngleClusterGroup();
        CubicRegressionPerCourseProcessor processor = new CubicRegressionPerCourseProcessor();

        BoatClass mockedBoatClass = mock(BoatClass.class);
        when(mockedBoatClass.getName()).thenReturn("49ER");

        Competitor competitor = mock(Competitor.class);
        when(competitor.getId()).thenReturn("1C");

        GPSFixMoving fix = createMockedFix(13, 45, 54.873740, 10.193648, 43.2, 10);
        Map<Competitor, Set<GPSFixMoving>> fixesPerCompetitor = new HashMap<Competitor, Set<GPSFixMoving>>();
        fixesPerCompetitor.put(competitor, new HashSet<>(Arrays.asList(fix)));

        TrackedRace trackedRace = createMockedTrackedRace(fixesPerCompetitor, mockedBoatClass);

        processor.processElement(new GroupedDataEntry<>(groupKey,
                new GPSFixMovingWithPolarContext(fix, trackedRace, competitor, angleClusterGroup)));

        PolarDataMiner miner = new PolarDataMiner(settings, processor,
                new SpeedRegressionPerAngleClusterProcessor(angleClusterGroup), angleClusterGroup);

        polarDataService = new PolarDataServiceImpl(miner);
    }

    protected <T extends AbstractPolarResource> T spyResource(T resource) {
        T spyResource = spy(resource);

        doReturn(polarDataService).when(spyResource).getPolarDataServiceImpl();
        return spyResource;
    }

}
