package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collections;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.AbstractRaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sse.common.Util;

public class RaceColumnImpl extends AbstractRaceColumn implements FlexibleRaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    private String name;

    private boolean medalRace;
    private final RaceExecutionOrderProvider raceExecutionOrderProvider;
    private IsRegattaLike regattaLikeHelper;

    public RaceColumnImpl(String name, boolean medalRace, RaceExecutionOrderProvider raceExecutionOrderProvider) {
        super();
        this.name = name;
        this.medalRace = medalRace;
        this.raceExecutionOrderProvider = raceExecutionOrderProvider;
    }

    @Override
    public void setName(String newName) {
        final String oldName = this.name;
        this.name = newName;
        if (!Util.equalsWithNull(oldName, newName)) {
            getRaceColumnListeners().notifyListenersAboutRaceColumnNameChanged(this, oldName, newName);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMedalRace() {
        return medalRace;
    }

    @Override
    public void setIsMedalRace(boolean isMedalRace) {
        this.medalRace = isMedalRace;
        getRaceColumnListeners().notifyListenersAboutIsMedalRaceChanged(this, isMedalRace());
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        return Collections.singleton(FlexibleLeaderboardImpl.defaultFleet);
    }

    @Override
    public RaceExecutionOrderProvider getRaceExecutionOrderProvider() {
        return raceExecutionOrderProvider;
    }

    @Override
    public void setRegattaLikeHelper(IsRegattaLike regattaLikeHelper) {
        this.regattaLikeHelper = regattaLikeHelper;
    }

    @Override
    public RegattaLog getRegattaLog() {
        if (regattaLikeHelper != null){
            return regattaLikeHelper.getRegattaLog();
        } else {
            return null;
        }
    }
}
