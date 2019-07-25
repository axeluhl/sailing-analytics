package com.sap.sailing.server.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.LeaderboardMXBean;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class LeaderboardMXBeanImpl implements LeaderboardMXBean {
    private static final Logger logger = Logger.getLogger(LeaderboardMXBeanImpl.class.getName());
    private static final long serialVersionUID = -8420830339429971378L;
    
    private final static String AVERAGE_RANGE_IN_MILLIS = "averageRangeInMillis";
    private final static String AVERAGE_COMPUTE_DURATION_IN_MILLIS = "averageComputeDurationInMillis";
    private final static String NUMBER_OF_COMPUTATIONS = "numberOfComputations";
    private final static CompositeType rowType;

    private final Leaderboard leaderboard;

    static {
        CompositeType myRowType;
        try {
            myRowType = new CompositeType("LeaderboardComputeTimeStat",
                    "Computation times and numbers for different averaging intervals",
                    new String[] { AVERAGE_RANGE_IN_MILLIS, AVERAGE_COMPUTE_DURATION_IN_MILLIS, NUMBER_OF_COMPUTATIONS },
                    new String[] { "Tells the age of the oldest leaderboard computation request, in milliseconds, still considered in this row",
                                   "The average computation duration in milliseconds",
                                   "The number of computations performed in this time range" },
                    new OpenType<?>[] { SimpleType.LONG, SimpleType.LONG, SimpleType.INTEGER });
        } catch (OpenDataException e) {
            logger.log(Level.SEVERE, "Problem trying to expose leaderboard computation times as MBean", e);
            myRowType = null;
        }
        rowType = myRowType;
    }
    
    public LeaderboardMXBeanImpl(Leaderboard leaderboard) {
        super();
        this.leaderboard = leaderboard;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.sap.sailing:type=Leaderboard,name="+escapeIllegalObjectNameCharacters(leaderboard.getName()));
    }
    
    @Override
    public String getName() {
        return leaderboard.getName();
    }

    private String escapeIllegalObjectNameCharacters(String name) {
        return name.replaceAll("[:/,]", "_");
    }

    @Override
    public int getNumberOfCompetitors() {
        return Util.size(leaderboard.getCompetitors());
    }

    @Override
    public int getNumberOfAllCompetitors() {
        return Util.size(leaderboard.getAllCompetitors());
    }

    @Override
    public String getDisplayName() {
        return leaderboard.getDisplayName();
    }

    @Override
    public String getType() {
        return leaderboard.getLeaderboardType().name();
    }
    
    @Override
    public long getDelayToLiveInMillis() {
        return leaderboard.getDelayToLiveInMillis();
    }

    @Override
    public String getBoatClass() {
        return leaderboard.getBoatClass().getName();
    }

    @Override
    public TabularData getComputationTimeAverages() {
        TabularData result;
        try {
            result = new TabularDataSupport(
                    new TabularType(
                            "LeaderboardComputeTimeStats",
                            "Describes average leaderboard calculation times and the number of computations",
                            rowType,
                            /* index */ new String[] { AVERAGE_RANGE_IN_MILLIS }));
            for (final Entry<Duration, Pair<Duration, Integer>> e : getLeaderboard().getComputationTimeStatistics().entrySet()) {
                final Map<String, Object> rowValues = new HashMap<>();
                rowValues.put(AVERAGE_RANGE_IN_MILLIS, e.getKey().asMillis());
                rowValues.put(AVERAGE_COMPUTE_DURATION_IN_MILLIS, e.getValue().getA()==null?-1:e.getValue().getA().asMillis());
                rowValues.put(NUMBER_OF_COMPUTATIONS, e.getValue().getB());
                result.put(new CompositeDataSupport(rowType, rowValues));
            }
        } catch (OpenDataException e) {
            logger.log(Level.SEVERE, "Problem trying to expose leaderboard computation times as MBean", e);
            result = null;
        }
        return result;
    }
}
