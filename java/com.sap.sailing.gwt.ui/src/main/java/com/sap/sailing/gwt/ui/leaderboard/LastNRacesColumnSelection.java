package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

/**
 * Doesn't respect requests to explicitly turn on or off certain columns. Instead, up to the last <code>n</code> (with
 * <code>n</code> being an integer number to configure through the constructor) race columns will be selected. "Last" is
 * decided based on the race start times which are obtained through a {@link RaceTimesInfoProvider}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LastNRacesColumnSelection extends AbstractRaceColumnSelection implements RaceTimesInfoProviderListener {
    private final int numberOfLastRacesToShow;
    private final RaceTimesInfoProvider raceTimesInfoProvider;
    private LeaderboardDTO lastLeaderboard;
    private Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo;
    
    public LastNRacesColumnSelection(int numberOfLastRacesToShow, RaceTimesInfoProvider raceTimesInfoProvider) {
        this.numberOfLastRacesToShow = numberOfLastRacesToShow;
        this.raceTimesInfoProvider = raceTimesInfoProvider;
        raceTimesInfoProvider.addRaceTimesInfoProviderListener(this);
    }

    @Override
    public void requestRaceColumnSelection(String raceColumnName, RaceColumnDTO column) {
        // no-op
    }

    @Override
    public void requestClear() {
        // no-op
    }

    @Override
    public void autoUpdateRaceColumnSelectionForUpdatedLeaderboard(LeaderboardDTO oldLeaderboard,
            LeaderboardDTO newLeaderboard) {
        this.lastLeaderboard = newLeaderboard;
        // update the race times info provider with the race identifiers of the leaderboard's races:
        updateRaceTimesInfoProvider(newLeaderboard);
    }

    private void updateRaceTimesInfoProvider(LeaderboardDTO newLeaderboard) {
        Set<RegattaAndRaceIdentifier> oldRaceIdentifiers = raceTimesInfoProvider.getRaceIdentifiers();
        Set<RegattaAndRaceIdentifier> newRaceIdentifiers = new HashSet<RegattaAndRaceIdentifier>();
        for (RaceColumnDTO column : newLeaderboard.getRaceList()) {
            for (FleetDTO fleet : column.getFleets()) {
                RegattaAndRaceIdentifier raceIdentifier = column.getRaceIdentifier(fleet);
                if (raceIdentifier != null) {
                    newRaceIdentifiers.add(raceIdentifier);
                }
            }
        }
        for (RegattaAndRaceIdentifier old : oldRaceIdentifiers) {
            if (!newRaceIdentifiers.contains(old)) {
                raceTimesInfoProvider.removeRaceIdentifier(old);
            }
        }
        for (RegattaAndRaceIdentifier neew : newRaceIdentifiers) {
            if (!oldRaceIdentifiers.contains(neew)) {
                raceTimesInfoProvider.addRaceIdentifier(neew, /* forceTimesInfoRequest */ false);
            }
        }
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        this.raceTimesInfo = raceTimesInfo;
    }

    private Iterable<RaceColumnDTO> getRaceColumnsFromNewestToOldest(LeaderboardDTO leaderboard,
            Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        final Map<RaceColumnDTO, Date> latestStarts = new HashMap<RaceColumnDTO, Date>();
        List<RaceColumnDTO> columns = new ArrayList<RaceColumnDTO>(leaderboard.getRaceList());
        for (Iterator<RaceColumnDTO> i=columns.iterator(); i.hasNext(); ) {
            RaceColumnDTO column = i.next();
            Date latestStart = getLatestStart(column);
            if (latestStart == null) {
                i.remove();
            } else {
                latestStarts.put(column, latestStart);
            }
        }
        Comparator<RaceColumnDTO> comparator = new Comparator<RaceColumnDTO>() {
            @Override
            public int compare(RaceColumnDTO o1, RaceColumnDTO o2) {
                int result;
                Date latestStartO1 = latestStarts.get(o1);
                Date latestStartO2 = latestStarts.get(o2);
                result = latestStartO2.compareTo(latestStartO1);
                return result;
            }
        };
        Collections.sort(columns, comparator);
        return columns;
    }

    private Date getLatestStart(RaceColumnDTO column) {
        Date latestStart = null;
        for (FleetDTO fleet : column.getFleets()) {
            RegattaAndRaceIdentifier raceIdentifier = column.getRaceIdentifier(fleet);
            RaceTimesInfoDTO times = raceTimesInfo.get(raceIdentifier);
            if (times != null && times.startOfRace != null && (latestStart == null || times.startOfRace.after(latestStart))) {
                latestStart = times.startOfRace;
            }
        }
        return latestStart;
    }

    @Override
    public Iterable<RaceColumnDTO> getSelectedRaceColumns() {
        Iterable<RaceColumnDTO> result;
        LeaderboardDTO leaderboard = lastLeaderboard;
        if (leaderboard == null || raceTimesInfo == null) {
            result = Collections.emptyList();
        } else {
            Iterable<RaceColumnDTO> raceColumnsFromNewestToOldest = getRaceColumnsFromNewestToOldest(leaderboard, raceTimesInfo);
            List<RaceColumnDTO> resultList = new ArrayList<RaceColumnDTO>(numberOfLastRacesToShow);
            Iterator<RaceColumnDTO> i = raceColumnsFromNewestToOldest.iterator();
            while (i.hasNext() && resultList.size() < numberOfLastRacesToShow) {
                resultList.add(i.next());
            }
            result = resultList;
        }
        return result;
    }

    @Override
    public RaceColumnSelectionStrategies getType() {
        return RaceColumnSelectionStrategies.LAST_N;
    }

    public int getNumberOfLastRaces() {
        return numberOfLastRacesToShow;
    }

}
