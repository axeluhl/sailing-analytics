package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsAndBoatsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Allows a user to select a leaderboard from which to copy competitor-to-boat assignments (known as the "pairing list")
 * into another target leaderboard. The target leaderboard must be a regatta leaderboard, and the source leaderboard
 * must be a regatta leaderboard as well. The target leaderboard must either have no boats and no competitors assigned
 * yet, or only those leaderboards qualify as source that have the same competitors/boats as the target leaderboard.
 * <p>
 * 
 * The user must select a sequence of race columns from the source leaderboard. Both underlying regattas must have
 * {@link RegattaDTO#canBoatsOfCompetitorsChangePerRace} set to {@code true}.
 * <p>
 * 
 * If the target leaderboard already has race columns defined, the source race columns' fleet structure must match the
 * fleet structure of the race columns of the target leaderboard. Race columns in source and target are matched by their
 * order in the leaderboard, not by their names. So, the selected source leaderboard race columns could, e.g., be F12,
 * F13, ..., F20, whereas the target leaderboard race columns may be F1, F2, ..., F8. Each matched pair of race columns
 * has to have the same number of fleets, but their names may differ.
 * <p>
 * 
 * The result of confirming this dialog tells the source and target leaderboards as {@link StrippedLeaderboardDTO}
 * objects, furthermore the sequence of {@link RaceColumnDTO}s that were selected from the source leaderboard.
 * <p>
 * 
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CopyPairingListDialog extends DataEntryDialog<com.sap.sailing.gwt.ui.adminconsole.CopyPairingListDialog.Result> {
    public static class Result {
        private final String sourceLeaderboardName;
        private final String fromRaceColumnName;
        private final String toRaceColumnInclusiveName;
        
        public Result(String sourceLeaderboardName, String fromRaceColumnName, String toRaceColumnInclusiveName) {
            this.sourceLeaderboardName = sourceLeaderboardName;
            this.fromRaceColumnName = fromRaceColumnName;
            this.toRaceColumnInclusiveName = toRaceColumnInclusiveName;
        }

        public String getSourceLeaderboardName() {
            return sourceLeaderboardName;
        }

        public String getFromRaceColumnName() {
            return fromRaceColumnName;
        }

        public String getToRaceColumnInclusiveName() {
            return toRaceColumnInclusiveName;
        }
    }
    
    private static class Validator implements DataEntryDialog.Validator<Result> {
        @Override
        public String getErrorMessage(Result valueToValidate) {
            // TODO check that to column is not before from column
            return null;
        }
    }

    private final ListBox otherLeaderboardListBox;
    private final Map<String, RegattaDTO> regattasByName;
    private final Collection<StrippedLeaderboardDTO> otherLeaderboards;
    private final StringMessages stringMessages;
    private final ListBox fromRaceColumnListBox;
    private final ListBox toRaceColumnInclusiveListBox;
    
    public CopyPairingListDialog(Collection<StrippedLeaderboardDTO> availableLeaderboards,
            Collection<RegattaDTO> availableRegattas, StrippedLeaderboardDTO targetLeaderboardDTO,
            StringMessages stringMessages, DialogCallback<Result> dialogCallback) {
        super(stringMessages.copyPairingListFromOtherLeaderboard(), null, stringMessages.ok(), stringMessages.cancel(), new Validator(), dialogCallback);
        this.regattasByName = new HashMap<>();
        availableRegattas.forEach(r->regattasByName.put(r.getName(), r));
        final RegattaDTO targetRegatta = regattasByName.get(targetLeaderboardDTO.regattaName);
        this.otherLeaderboards = Util.asList(Util.filter(availableLeaderboards, l->l != targetLeaderboardDTO && l.canBoatsOfCompetitorsChangePerRace && l.type.isRegattaLeaderboard()
                && regattasByName.containsKey(l.regattaName) && doCompetitorsAndBoatsMatch(regattasByName.get(l.regattaName), targetRegatta)));
        this.otherLeaderboardListBox = AbstractLeaderboardDialog.createSortedRegattaLeaderboardsListBox(otherLeaderboards, /* preSelectedRegattaName */ null, stringMessages, this);
        this.otherLeaderboardListBox.addChangeHandler(this::onOtherLeaderboardSelected);
        this.fromRaceColumnListBox = new ListBox();
        this.toRaceColumnInclusiveListBox = new ListBox();
        this.stringMessages = stringMessages;
    }
    
    /**
     * Updates the race colums drop-downs for first and last race column to copy the competitor-to-boat assignments from,
     * based on the structure of the regatta that belongs to the leaderboard selected in the {@link #otherLeaderboardListBox}.
     */
    private void onOtherLeaderboardSelected(ChangeEvent e) {
        final RegattaDTO sourceRegatta = regattasByName.get(otherLeaderboardListBox.getSelectedValue());
        final List<RaceColumnDTO> raceColumns = getRaceColumns(sourceRegatta);
        fromRaceColumnListBox.clear();
        toRaceColumnInclusiveListBox.clear();
        for (final RaceColumnDTO raceColumn : raceColumns) {
            fromRaceColumnListBox.addItem(raceColumn.getName(), raceColumn.getName());
            toRaceColumnInclusiveListBox.addItem(raceColumn.getName(), raceColumn.getName());
        }
    }

    private List<RaceColumnDTO> getRaceColumns(final RegattaDTO regatta) {
        final List<RaceColumnDTO> raceColumns = new ArrayList<>();
        if (regatta != null) {
            for (final SeriesDTO series : regatta.series) {
                for (final RaceColumnDTO raceColumn : series.getRaceColumns()) {
                    raceColumns.add(raceColumn);
                }
            }
        }
        return raceColumns;
    }

    private boolean doCompetitorsAndBoatsMatch(RegattaDTO regatta, RegattaDTO targetRegatta) {
        return (Util.isEmpty(getBoats(targetRegatta)) || getBoats(regatta).equals(getBoats(targetRegatta)))
            && (Util.isEmpty(getCompetitors(targetRegatta)) || getCompetitors(regatta).equals(getCompetitors(targetRegatta)));
    }

    private Set<CompetitorDTO> getCompetitors(RegattaDTO regatta) {
        final Set<CompetitorDTO> result = new HashSet<>();
        for (final RaceWithCompetitorsAndBoatsDTO r : regatta.races) {
            r.getCompetitors().forEach(result::add);
        }
        return result;
    }

    private Set<BoatDTO> getBoats(RegattaDTO regatta) {
        final Set<BoatDTO> result = new HashSet<>();
        for (final RaceWithCompetitorsAndBoatsDTO r : regatta.races) {
            r.getBoats().forEach(result::add);
        }
        return result;
    }

    @Override
    protected Widget getAdditionalWidget() {
        final Grid result = new Grid(2, 2);
        int row = 0;
        result.setWidget(row, 0, new Label(stringMessages.selectALeaderboard()));
        result.setWidget(row++, 1, otherLeaderboardListBox);
        result.setWidget(row, 0, new Label(stringMessages.selectRaceColumnsWhosePairingsToCopy()));
        final HorizontalPanel hp = new HorizontalPanel();
        result.setWidget(row++, 1, hp);
        hp.add(new Label(stringMessages.from()));
        hp.add(fromRaceColumnListBox);
        hp.add(new Label(stringMessages.to()));
        hp.add(toRaceColumnInclusiveListBox);
        return result;
    }


    @Override
    protected Result getResult() {
        return new Result(otherLeaderboardListBox.getSelectedValue(),
                fromRaceColumnListBox.getSelectedValue(), toRaceColumnInclusiveListBox.getSelectedValue());
    }
}
