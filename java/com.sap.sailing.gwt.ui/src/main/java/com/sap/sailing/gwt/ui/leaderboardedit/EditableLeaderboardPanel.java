package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;

/**
 * An editable version of the {@link LeaderboardPanel} which allows a user to enter carried / accumulated
 * points and fix individual race scores.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EditableLeaderboardPanel extends LeaderboardPanel {
    private static EditableLeaderboardResources resources = GWT.create(EditableLeaderboardResources.class);

    private class EditableCarryColumn extends CarryColumn {
        public EditableCarryColumn() {
            super(new EditTextCell());
            setFieldUpdater(new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                    getSailingService().updateLeaderboardCarryValue(getLeaderboardName(), row.competitor.id,
                            value == null || value.length() == 0 ? null : Integer.valueOf(value.trim()),
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    EditableLeaderboardPanel.this.getErrorReporter().reportError("Error trying to update carry value for competitor "+
                                            row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                            "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Void v) {
                                    row.carriedPoints = value==null||value.length()==0 ? null : Integer.valueOf(value.trim());
                                    EditableLeaderboardPanel.this.getData().getList().set(rowIndex, row);
                                }
                            });
                }
            });
        }
    }
    
    private class EditableCompetitorColumn extends CompetitorColumn {
        
        @Override
        public EditTextCell getCell() {
            return (EditTextCell) super.getCell();
        }
        
        @Override
        public void render(Context context, LeaderboardRowDTO row, SafeHtmlBuilder sb) {
            final boolean isDisplayNameSet = getLeaderboard().isDisplayNameSet(row.competitor);
            if (isDisplayNameSet) {
                sb.appendHtmlConstant("<b>");
            }
            super.render(context, row, sb);
            if (isDisplayNameSet) {
                sb.appendHtmlConstant("</b>");
            }
        }

        public EditableCompetitorColumn() {
            super(new EditTextCell());
            setFieldUpdater(new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                    getSailingService().updateCompetitorDisplayNameInLeaderboard(getLeaderboardName(), row.competitor.id,
                            value == null || value.length() == 0 ? null : value.trim(),
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    EditableLeaderboardPanel.this.getErrorReporter().reportError("Error trying to update display name for competitor "+
                                            row.competitor.name+" in leaderboard "+getLeaderboardName()+": "+t.getMessage()+
                                            "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Void v) {
                                    if (getLeaderboard().competitorDisplayNames == null) {
                                        getLeaderboard().competitorDisplayNames = new HashMap<CompetitorDTO, String>();
                                    }
                                    getLeaderboard().competitorDisplayNames.put(row.competitor, value == null || value.trim().length() == 0 ? null : value.trim());
                                    getCell().setViewData(row, null); // ensure that getValue() is called again
                                    EditableLeaderboardPanel.this.getData().getList().set(rowIndex, row);
                                }
                            });
                }
            });
        }
    }
    
    private class CompositeCellRememberingRenderingContextAndObject extends CompositeCell<LeaderboardRowDTO> {
        private final List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> cells;
        
        public CompositeCellRememberingRenderingContextAndObject(List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> hasCells) {
            super(new ArrayList<HasCell<LeaderboardRowDTO, ?>>(hasCells));
            cells = hasCells;
        }

        @Override
        public void render(Context context, LeaderboardRowDTO value, SafeHtmlBuilder sb) {
            tellCellsWhatIsBeingRendered(value);
            super.render(context, value, sb);
        }

        private void tellCellsWhatIsBeingRendered(LeaderboardRowDTO value) {
            for (RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?> cell : cells) {
                cell.setCurrentlyRendering(value);
            }
        }

        @Override
        protected <X> void render(Context context, LeaderboardRowDTO value, SafeHtmlBuilder sb,
                HasCell<LeaderboardRowDTO, X> hasCell) {
            tellCellsWhatIsBeingRendered(value);
            super.render(context, value, sb, hasCell);
        }
    }

    private class EditableRaceColumn extends RaceColumn<LeaderboardRowDTO> implements RowUpdateWhiteboardOwner<LeaderboardRowDTO> {
        private RowUpdateWhiteboard<LeaderboardRowDTO> currentRowUpdateWhiteboard;
        
        public EditableRaceColumn(RaceColumnDTO race, List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> cellList) {
            super(race,
                    /* expandable */ false, // we don't want leg expansion when editing scores
                    new CompositeCellRememberingRenderingContextAndObject(cellList),
                    RACE_COLUMN_HEADER_STYLE, RACE_COLUMN_STYLE);
            for (RowUpdateWhiteboardProducer<LeaderboardRowDTO> rowUpdateWhiteboardProducer : cellList) {
                rowUpdateWhiteboardProducer.setWhiteboardOwner(this);
            }
            // the field updater for the composite is invoked after any component has updated its field
            setFieldUpdater(new FieldUpdater<LeaderboardRowDTO, LeaderboardRowDTO>() {
                @Override
                public void update(int rowIndex, LeaderboardRowDTO row, LeaderboardRowDTO value) {
                    currentRowUpdateWhiteboard.setIndexOfRowToUpdate(rowIndex);
                    currentRowUpdateWhiteboard = null; // show that it has been consumed and updated
                }
            });
        }

        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder html) {
            defaultRender(context, object, html);
        }

        @Override
        public LeaderboardRowDTO getValue(LeaderboardRowDTO object) {
            return object;
        }

        @Override
        public void whiteboardProduced(RowUpdateWhiteboard<LeaderboardRowDTO> whiteboard) {
            currentRowUpdateWhiteboard = whiteboard;
        }
    }
    
    private class MaxPointsDropDownCellProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDTO, String> {
        private final SelectionCell dropDownCell;
        private final String raceColumnName;
        
        public MaxPointsDropDownCellProvider(String raceColumnName) {
            this.raceColumnName = raceColumnName;
            List<String> selectionCellContents = new ArrayList<String>();
            selectionCellContents.add(""); // represents "no" max points reason
            for (MaxPointsReason maxPointReason : MaxPointsReason.values()) {
                selectionCellContents.add(maxPointReason.name());
            }
            dropDownCell = new SelectionCell(selectionCellContents);
        }
        
        @Override
        public SelectionCell getCell() {
            return dropDownCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDTO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDTO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    getBusyIndicator().setBusy(true);
                    getSailingService().updateLeaderboardMaxPointsReason(getLeaderboardName(), row.competitor.id,
                            raceColumnName, value == null || value.trim().length() == 0 ? null : MaxPointsReason.valueOf(value.trim()),
                            getLeaderboardDisplayDate(), new AsyncCallback<Triple<Integer, Integer, Boolean>>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    getBusyIndicator().setBusy(false);
                                    getErrorReporter().reportError(
                                            "Error trying to update max points reason for competitor "
                                                    + row.competitor.name + " in leaderboard " + getLeaderboardName()
                                                    + ": " + t.getMessage() + "\nYou may have to refresh your view.");
                                }

                                @Override
                                public void onSuccess(Triple<Integer, Integer, Boolean> newNetAndTotalPointsAndIsCorrected) {
                                    getBusyIndicator().setBusy(false);
                                    row.fieldsByRaceColumnName.get(raceColumnName).reasonForMaxPoints = value == null
                                            || value.length() == 0 ? null : MaxPointsReason.valueOf(value.trim());
                                    row.fieldsByRaceColumnName.get(raceColumnName).netPoints = newNetAndTotalPointsAndIsCorrected.getA();
                                    row.fieldsByRaceColumnName.get(raceColumnName).totalPoints = newNetAndTotalPointsAndIsCorrected.getB();
                                    row.fieldsByRaceColumnName.get(raceColumnName).netPointsCorrected = newNetAndTotalPointsAndIsCorrected.getC();
                                    getCell().setViewData(row, null); // ensure that getValue() is called again
                                    whiteboard.setObjectWithWhichToUpdateRow(row);
                                }
                            });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            LeaderboardEntryDTO leaderboardEntryDTO = object.fieldsByRaceColumnName.get(raceColumnName);
            MaxPointsReason reasonForMaxPoints = null;
            if (leaderboardEntryDTO != null) {
                reasonForMaxPoints = leaderboardEntryDTO.reasonForMaxPoints;
            }
            return reasonForMaxPoints == null ? "" : reasonForMaxPoints.name();
        }
    }
    
    private class NetPointsEditCellProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDTO, String> {
        private final EditTextCell netPointsEditCell;
        private final String raceName;
        
        protected NetPointsEditCellProvider(String raceName) {
            this.raceName = raceName;
            netPointsEditCell = new EditTextCell(new SafeHtmlRenderer<String>() {
                @Override
                public void render(String netPointsAsString, SafeHtmlBuilder html) {
                    if (netPointsAsString != null) {
                        final boolean netPointsCorrected = isNetPointsCorrected();
                        if (netPointsCorrected) {
                            html.appendHtmlConstant("<b>");
                        }
                        html.appendEscaped(netPointsAsString);
                        if (netPointsCorrected) {
                            html.appendHtmlConstant("</b>");
                        }
                    }
                }
                
                @Override
                public SafeHtml render(String netPointsAsString) {
                    final boolean netPointsCorrected = isNetPointsCorrected();
                    String prefix;
                    String suffix;
                    if (netPointsCorrected) {
                        prefix = "<b>";
                        suffix = "</b>";
                    } else {
                        prefix = "";
                        suffix = "";
                    }
                    return SafeHtmlUtils.fromSafeConstant(prefix+SafeHtmlUtils.fromString(netPointsAsString).asString()+suffix);
                }
            });
        }

        private boolean isNetPointsCorrected() {
            LeaderboardEntryDTO leaderboardEntryDTO = getCurrentlyRendering().fieldsByRaceColumnName.get(raceName);
            return leaderboardEntryDTO.netPointsCorrected;
        }

        @Override
        public EditTextCell getCell() {
            return netPointsEditCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDTO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDTO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    getBusyIndicator().setBusy(true);
                    getSailingService().updateLeaderboardScoreCorrection(getLeaderboardName(), row.competitor.id, raceName,
                            value == null || value.trim().length() == 0 ? null : Integer.valueOf(value.trim()), getLeaderboardDisplayDate(),
                    new AsyncCallback<Triple<Integer, Integer, Boolean>>() {
                        @Override
                        public void onFailure(Throwable t) {
                            getBusyIndicator().setBusy(false);
                            getErrorReporter().reportError("Error trying to update score correction for competitor "+
                                    row.competitor.name+" in leaderboard "+getLeaderboardName()+
                                    " for race "+raceName+": "+t.getMessage()+
                                    "\nYou may have to refresh your view.");
                        }

                        @Override
                        public void onSuccess(Triple<Integer, Integer, Boolean> newNetAndTotalPointsAndIsCorrected) {
                            getBusyIndicator().setBusy(false);
                            final LeaderboardEntryDTO leaderboardEntryDTO = row.fieldsByRaceColumnName.get(raceName);
                                    leaderboardEntryDTO.netPoints = value == null || value.length() == 0 ? newNetAndTotalPointsAndIsCorrected
                                            .getA() : Integer.valueOf(value.trim());
                                    leaderboardEntryDTO.totalPoints = newNetAndTotalPointsAndIsCorrected.getB();
                            leaderboardEntryDTO.netPointsCorrected = newNetAndTotalPointsAndIsCorrected.getC();
                            getCell().setViewData(row, null); // ensure that getValue() is called again
                            whiteboard.setObjectWithWhichToUpdateRow(row);
                        }
                    });
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            LeaderboardEntryDTO leaderboardEntryDTO = object.fieldsByRaceColumnName.get(raceName);
            String result = "";
            if (leaderboardEntryDTO != null) {
                result = result+leaderboardEntryDTO.netPoints;
            }
            return result;
        }
    }

    public EditableLeaderboardPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            String leaderboardName, String leaderboardGroupName, ErrorReporter errorReporter,
            final StringMessages stringMessages, UserAgentDetails userAgent) {
        super(sailingService, asyncActionsExecutor, LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
                /* racesToShow */ null, /* namesOfRacesToShow */ null, null, /* autoExpandFirstRace */false),
                new CompetitorSelectionModel(/* hasMultiSelection */true),
                leaderboardName, leaderboardGroupName, errorReporter, stringMessages, userAgent, /* showRaceDetails */ true);
        ImageResource importIcon = resources.importIcon();
        Anchor importAnchor = new Anchor(AbstractImagePrototype.create(importIcon).getSafeHtml());
        getRefreshAndSettingsPanel().insert(importAnchor, 0);
        importAnchor.setTitle(stringMessages.importOfficialResults());
        importAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performImport(stringMessages);
            }
        });
    }

    private void performImport(final StringMessages stringMessages) {
        getBusyIndicator().setBusy(true);
        getSailingService().getScoreCorrectionProviderDTOs(new AsyncCallback<Iterable<ScoreCorrectionProviderDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                EditableLeaderboardPanel.this.getBusyIndicator().setBusy(false);
                getErrorReporter().reportError(stringMessages.errorLoadingScoreCorrectionProviders(caught.getMessage()));
            }

            @Override
            public void onSuccess(Iterable<ScoreCorrectionProviderDTO> result) {
                EditableLeaderboardPanel.this.getBusyIndicator().setBusy(false);
                showScoreCorrectionSelectionDialog(result);
            }
        });
    }

    private void showScoreCorrectionSelectionDialog(Iterable<ScoreCorrectionProviderDTO> result) {
        List<Triple<String, String, Pair<String, Date>>> providerNameAndEventNameBoatClassNameCapturedWhen =
                new ArrayList<Triple<String,String, Pair<String,Date>>>();
        for (ScoreCorrectionProviderDTO scp : result) {
            for (Entry<String, Set<Pair<String, Date>>> e : scp.getHasResultsForBoatClassFromDateByEventName().entrySet()) {
                for (Pair<String, Date> se : e.getValue()) {
                    providerNameAndEventNameBoatClassNameCapturedWhen.add(new Triple<String, String, Pair<String, Date>>(
                            scp.name, e.getKey(), se));
                }
            }
        }
        sortOfficialResultsByRelevance(providerNameAndEventNameBoatClassNameCapturedWhen);
        new ResultSelectionAndApplyDialog(this, getSailingService(), getStringMessages(),
                providerNameAndEventNameBoatClassNameCapturedWhen, getErrorReporter()).show();
    }

    private void sortOfficialResultsByRelevance(
            List<Triple<String, String, Pair<String, Date>>> providerNameAndEventNameBoatClassNameCapturedWhen) {
        final Set<BoatClassDTO> boatClasses = getLeaderboard().getBoatClasses();
        final Set<String> lowercaseBoatClassNames = new HashSet<String>();
        for (BoatClassDTO boatClass : boatClasses) {
            lowercaseBoatClassNames.add(boatClass.name.toLowerCase());
        }
        Collections.sort(providerNameAndEventNameBoatClassNameCapturedWhen, new Comparator<Triple<String, String, Pair<String, Date>>>() {
            @Override
            public int compare(Triple<String, String, Pair<String, Date>> o1,
                    Triple<String, String, Pair<String, Date>> o2) {
                int result;
                // TODO consider looking for longest common substring to handle things like "470 M" vs. "470 Men"
                if (lowercaseBoatClassNames.contains(o1.getC().getA().toLowerCase())) {
                    if (lowercaseBoatClassNames.contains(o2.getC().getA().toLowerCase())) {
                        // both don't seem to have the right boat class; compare by time stamp; newest first
                        result = o2.getC().getB().compareTo(o1.getC().getB());
                    } else {
                        result = -1; // o1 scores "better", comes first, because it has the right boat class name
                    }
                } else if (o2.getC().getA() != null && lowercaseBoatClassNames.contains(o2.getC().getA().toLowerCase())) {
                    result = 1;
                } else {
                    // both don't seem to have the right boat class; compare by time stamp; newest first
                    result = o2.getC().getB().compareTo(o1.getC().getB());
                }
                return result;
            }
        });
    }

    /**
     * Always ensures that there is a carry column displayed because in the editable version
     * of the leaderboard the carried / accumulated values must always be editable and therefore
     * the column must always be shown.
     */
    @Override
    protected void updateCarryColumn(LeaderboardDTO leaderboard) {
        ensureCarryColumn();
    }

    @Override
    protected CarryColumn createCarryColumn() {
        return new EditableCarryColumn();
    }
    
    @Override
    protected CompetitorColumn createCompetitorColumn() {
        return new EditableCompetitorColumn();
    }

    @Override
    protected RaceColumn<?> createRaceColumn(RaceColumnDTO race) {
        return new EditableRaceColumn(race, getCellList(race));
    }

    private List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> getCellList(RaceColumnDTO race) {
        List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> list =
                new ArrayList<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>>();
        list.add(new MaxPointsDropDownCellProvider(race.getRaceColumnName()));
        list.add(new NetPointsEditCellProvider(race.getRaceColumnName()));
        return list;
    }

    
}
