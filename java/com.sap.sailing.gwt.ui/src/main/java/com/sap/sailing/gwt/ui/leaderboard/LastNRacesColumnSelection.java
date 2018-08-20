package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

/**
 * Doesn't respect requests to explicitly turn on or off certain columns. Instead, up to the last <code>n</code> (with
 * <code>n</code> being an integer number to configure through the constructor) race columns will be selected. "Last" is
 * decided based on the column ordering in the leaderboard, considering races with a valid start time which are obtained
 * through a {@link RaceTimesInfoProvider} or a score correction.
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
        Date now = new Date();
        raceTimesInfosReceived(raceTimesInfoProvider.getRaceTimesInfos(), /* clientTimeWhenRequestWasSent */ now.getTime(), /* server time is guessed here */ now, now.getTime());
    }

    @Override
    public void requestRaceColumnSelection(RaceColumnDTO column) {
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
        Set<RegattaAndRaceIdentifier> oldRaceIdentifiers = new HashSet<>(raceTimesInfoProvider.getRaceIdentifiers());
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
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived) {
        this.raceTimesInfo = raceTimesInfo;
    }

    private Iterable<RaceColumnDTO> getRaceColumnsFromNewestToOldest(final LeaderboardDTO leaderboard,
            Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        List<RaceColumnDTO> columns = new ArrayList<RaceColumnDTO>(leaderboard.getRaceList());
        for (Iterator<RaceColumnDTO> i=columns.iterator(); i.hasNext(); ) {
            RaceColumnDTO column = i.next();
            if (getLatestStart(column) == null && !hasScoreCorrections(leaderboard, column)) {
                i.remove();
            }
        }
        Comparator<RaceColumnDTO> comparator = new Comparator<RaceColumnDTO>() {
            @Override
            public int compare(RaceColumnDTO o1, RaceColumnDTO o2) {
                return leaderboard.getRaceList().indexOf(o2) - leaderboard.getRaceList().indexOf(o1);
            }
        };
        Collections.sort(columns, comparator);
        return columns;
    }

    private boolean hasScoreCorrections(LeaderboardDTO leaderboard, RaceColumnDTO column) {
        for (Map.Entry<CompetitorDTO, LeaderboardRowDTO> e : leaderboard.rows.entrySet()) {
            LeaderboardEntryDTO entry = e.getValue().fieldsByRaceColumnName.get(column.getName());
            if (entry != null && entry.hasScoreCorrection()) {
                return true;
            }
        }
        return false;
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

    @Override
    public Integer getNumberOfLastRaceColumnsToShow() {
        return numberOfLastRacesToShow;
    }

}
