package com.sap.sailing.gwt.ui.leaderboardedit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleTableResources;
import com.sap.sailing.gwt.ui.client.Collator;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.FlagImageRenderer;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorColumnBase;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFetcher;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * An editable version of the {@link ClassicLeaderboardPanel} which allows a user to enter carried / accumulated
 * points and fix individual race scores.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EditableLeaderboardPanel extends MultiRaceLeaderboardPanel {
    private static EditableLeaderboardResources resources = GWT.create(EditableLeaderboardResources.class);

    final DateBox lastScoreCorrectionTimeBox;
    final TextBox lastScoreCorrectionCommentBox;

    final private CellTable<CompetitorDTO> suppressedCompetitorsTable;
    final private ListDataProvider<CompetitorDTO> suppressedCompetitorsShown;

    private CheckBox showUncorrectedTotalPointsCheckbox;
    
    private class SettingsClickHandler implements ClickHandler {
        private final StringMessages stringMessages;

        private SettingsClickHandler(StringMessages stringMessages) {
            this.stringMessages = stringMessages;
        }

        @Override
        public void onClick(ClickEvent event) {
            new SettingsDialog<MultiRaceLeaderboardSettings>(EditableLeaderboardPanel.this, stringMessages).show();
        }
    }

    private class EditableCarryColumn extends CarryColumn {
        public EditableCarryColumn() {
            super(new CompositeCell<LeaderboardRowDTO>(getCellListForEditableCarryColumn()));
        }
    }

    private class EditableCompetitorColumn extends CompetitorColumn {
        public EditableCompetitorColumn(List<HasCell<LeaderboardRowDTO, ?>> cells, CompetitorColumnBase<LeaderboardRowDTO> base) {
            super(new CompositeCell<LeaderboardRowDTO>(cells), base);
        }

        @Override
        public void render(Context context, LeaderboardRowDTO object, SafeHtmlBuilder sb) {
            super.defaultRender(context, object, sb);
        }
    }

    /**
     * Shows the country flag and sail ID, if present
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    private class SuppressedShortInfoColumn extends LeaderboardSortableColumnWithMinMax<CompetitorDTO, String> {
        protected SuppressedShortInfoColumn() {
            super(new TextCell(), SortingOrder.ASCENDING, EditableLeaderboardPanel.this);
        }

        @Override
        public InvertibleComparator<CompetitorDTO> getComparator() {
            return new InvertibleComparatorAdapter<CompetitorDTO>() {
                @Override
                public int compare(CompetitorDTO o1, CompetitorDTO o2) {
                    return Collator.getInstance().compare(o1.getShortInfo(), o2.getShortInfo());
                }
            };
        }

        @Override
        public Header<String> getHeader() {
            return new TextHeader(stringMessages.competitor());
        }

        @Override
        public void render(Context context, CompetitorDTO object, SafeHtmlBuilder sb) {
            final String twoLetterIsoCountryCode = object.getTwoLetterIsoCountryCode();
            final ImageResource flagImageResource;
            if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                flagImageResource = FlagImageResolverImpl.get().getEmptyFlagImageResource();
            } else {
                flagImageResource = FlagImageResolverImpl.get().getFlagImageResource(twoLetterIsoCountryCode);
            }
            if (flagImageResource != null) {
                sb.append(FlagImageRenderer.image(flagImageResource.getSafeUri().asString()));
                sb.appendHtmlConstant("&nbsp;");
            }
            sb.appendEscaped(object.getShortInfo());
        }

        @Override
        public String getValue(CompetitorDTO object) {
            return object.getShortInfo();
        }
    }

    protected class SuppressedCompetitorColumn extends LeaderboardSortableColumnWithMinMax<CompetitorDTO, CompetitorDTO> {
        private final CompetitorColumnBase<CompetitorDTO> base;

        protected SuppressedCompetitorColumn(CompetitorColumnBase<CompetitorDTO> base) {
            super(base.getCell(getLeaderboard()), SortingOrder.ASCENDING, EditableLeaderboardPanel.this);
            this.base = base;
        }

        @Override
        public InvertibleComparator<CompetitorDTO> getComparator() {
            return base.getComparator();
        }

        @Override
        public SafeHtmlHeader getHeader() {
            return base.getHeader();
        }

        @Override
        public CompetitorDTO getValue(CompetitorDTO competitor) {
            return competitor;
        }

        @Override
        public void render(Context context, CompetitorDTO competitor, SafeHtmlBuilder sb) {
            base.render(context, competitor, sb);
        }
    }

    private FieldUpdater<LeaderboardRowDTO, String> createCompetitorColumnFieldUpdater(final EditTextCell cell) {
        return new FieldUpdater<LeaderboardRowDTO, String>() {
            @Override
            public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                getSailingService().updateCompetitorDisplayNameInLeaderboard(getLeaderboardName(), row.competitor.getIdAsString(),
                        value == null || value.length() == 0 ? null : value.trim(),
                                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        EditableLeaderboardPanel.this.getErrorReporter().reportError(
                                stringMessages.errorUpdatingDisplayNames(row.competitor.getName(),
                                        getLeaderboardName(), t.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void v) {
                        if (getLeaderboard().competitorDisplayNames == null) {
                            getLeaderboard().competitorDisplayNames = new HashMap<>();
                        }
                        getLeaderboard().competitorDisplayNames.put(row.competitor, value == null || value.trim().length() == 0 ? null : value.trim());
                        cell.setViewData(row, null); // ensure that getValue() is called again
                        EditableLeaderboardPanel.this.getData().getList().set(
                                EditableLeaderboardPanel.this.getData().getList().indexOf(row), row);
                    }
                });
            }
        };
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
                    new CompositeCellRememberingRenderingContextAndObject(cellList), SortingOrder.NONE,
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
                    addBusyTask();
                    getSailingService().updateLeaderboardMaxPointsReason(getLeaderboardName(), row.competitor.getIdAsString(),
                            raceColumnName, value == null || value.trim().length() == 0 ? null : MaxPointsReason.valueOf(value.trim()),
                                    getLeaderboardDisplayDate(), new AsyncCallback<Util.Triple<Double, Double, Boolean>>() {
                        @Override
                        public void onFailure(Throwable t) {
                            removeBusyTask();
                            getErrorReporter().reportError(stringMessages.errorUpdatingMaxPointsReason(
                                    row.competitor.getName(), getLeaderboardName(), t.getMessage()));
                        }

                        @Override
                        public void onSuccess(Util.Triple<Double, Double, Boolean> newTotalAndNetPointsAndIsCorrected) {
                            removeBusyTask();
                            row.fieldsByRaceColumnName.get(raceColumnName).reasonForMaxPoints = value == null
                                    || value.length() == 0 ? null : MaxPointsReason.valueOf(value.trim());
                            row.fieldsByRaceColumnName.get(raceColumnName).totalPoints = newTotalAndNetPointsAndIsCorrected.getA();
                            row.fieldsByRaceColumnName.get(raceColumnName).netPoints = newTotalAndNetPointsAndIsCorrected.getB();
                            row.fieldsByRaceColumnName.get(raceColumnName).totalPointsCorrected = newTotalAndNetPointsAndIsCorrected.getC();
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
    
    private class UncorrectedTotalPointsViewProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDTO, String> {
        private final String raceColumnName;
        
        protected UncorrectedTotalPointsViewProvider(String raceColumnName) {
            this.raceColumnName = raceColumnName;
        }

        @Override
        public Cell<String> getCell() {
            return new TextCell();
        }

        @Override
        public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            LeaderboardEntryDTO leaderboardEntryDTO = object.fieldsByRaceColumnName.get(raceColumnName);
            String result = "";
            if (leaderboardEntryDTO != null && leaderboardEntryDTO.totalPointsUncorrected != null) {
                result="("+scoreFormat.format(leaderboardEntryDTO.totalPointsUncorrected)+")";
            }
            return result;
        }
    }

    private class TotalPointsEditCellProvider extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDTO, String> {
        private final EditTextCell totalPointsEditCell;
        private final String raceColumnName;

        protected TotalPointsEditCellProvider(String raceColumnName) {
            this.raceColumnName = raceColumnName;
            totalPointsEditCell = new EditTextCell(new SafeHtmlRenderer<String>() {
                @Override
                public void render(String totalPointsAsString, SafeHtmlBuilder html) {
                    if (totalPointsAsString != null) {
                        final boolean totalPointsCorrected = isTotalPointsCorrected();
                        if (totalPointsCorrected) {
                            html.appendHtmlConstant("<b>");
                        }
                        html.appendEscaped(totalPointsAsString);
                        if (totalPointsCorrected) {
                            html.appendHtmlConstant("</b>");
                        }
                    }
                }

                @Override
                public SafeHtml render(String totalPointsAsString) {
                    final boolean totalPointsCorrected = isTotalPointsCorrected();
                    String prefix;
                    String suffix;
                    if (totalPointsCorrected) {
                        prefix = "<b class='bold'>";
                        suffix = "</b>";
                    } else {
                        prefix = "";
                        suffix = "";
                    }
                    return SafeHtmlUtils.fromSafeConstant(prefix+SafeHtmlUtils.fromString(totalPointsAsString).asString()+suffix);
                }
            });
        }

        private boolean isTotalPointsCorrected() {
            LeaderboardEntryDTO leaderboardEntryDTO = getCurrentlyRendering().fieldsByRaceColumnName.get(raceColumnName);
            return leaderboardEntryDTO != null && leaderboardEntryDTO.totalPointsCorrected;
        }

        @Override
        public EditTextCell getCell() {
            return totalPointsEditCell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDTO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDTO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    addBusyTask();
                    getSailingService().updateLeaderboardScoreCorrection(getLeaderboardName(), row.competitor.getIdAsString(), raceColumnName,
                            value == null || value.trim().length() == 0 ? null : value.trim().equals("n/a") ? null
                                    : Double.valueOf(value.trim()), getLeaderboardDisplayDate(),
                            new AsyncCallback<Util.Triple<Double, Double, Boolean>>() {
                        @Override
                        public void onFailure(Throwable t) {
                            removeBusyTask();
                            getErrorReporter().reportError(
                                    stringMessages.errorUpdatingLeaderboardScore(row.competitor.getName(),
                                            getLeaderboardName(), raceColumnName, t.getMessage()));
                        }

                        @Override
                        public void onSuccess(Util.Triple<Double, Double, Boolean> newTotalAndTotalPointsAndIsCorrected) {
                            removeBusyTask();
                            final LeaderboardEntryDTO leaderboardEntryDTO = row.fieldsByRaceColumnName.get(raceColumnName);
                            leaderboardEntryDTO.totalPoints = value == null || value.length() == 0 ? newTotalAndTotalPointsAndIsCorrected
                                    .getA() : Double.valueOf(value.trim());
                            leaderboardEntryDTO.netPoints = newTotalAndTotalPointsAndIsCorrected.getB();
                            leaderboardEntryDTO.totalPointsCorrected = newTotalAndTotalPointsAndIsCorrected.getC();
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
            String result = "n/a";
            if (leaderboardEntryDTO != null && leaderboardEntryDTO.totalPoints != null) {
                result = scoreFormat.format(leaderboardEntryDTO.totalPoints);
            }
            return result;
        }
    }
    
    private class MaxPointsReasonAndTotalPointsEditButtonCell extends AbstractRowUpdateWhiteboardProducerThatHasCell<LeaderboardRowDTO, String> {
        private final ButtonCell cell = new ButtonCell();
        private final String raceColumnName;
        private final StringMessages stringMessages;
        private final MaxPointsDropDownCellProvider maxPointsDropDownCellProvider;
        private final TotalPointsEditCellProvider totalPointsEditCellProvider;
        
        /**
         * @param maxPointsDropDownCellProvider will have its cell's view data reset after a successful update to force call to getValue
         * @param totalPointsEditCellProvider will have its cell's view data reset after a successful update to force call to getValue
         */
        public MaxPointsReasonAndTotalPointsEditButtonCell(StringMessages stringMessages, String raceColumnName,
                MaxPointsDropDownCellProvider maxPointsDropDownCellProvider,
                TotalPointsEditCellProvider totalPointsEditCellProvider) {
            this.stringMessages = stringMessages;
            this.raceColumnName = raceColumnName;
            this.maxPointsDropDownCellProvider = maxPointsDropDownCellProvider;
            this.totalPointsEditCellProvider = totalPointsEditCellProvider;
        }

        @Override
        public ButtonCell getCell() {
            return cell;
        }

        @Override
        public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
            return new FieldUpdater<LeaderboardRowDTO, String>() {
                @Override
                public void update(int index, final LeaderboardRowDTO row, final String value) {
                    final RowUpdateWhiteboard<LeaderboardRowDTO> whiteboard = new RowUpdateWhiteboard<LeaderboardRowDTO>(
                            EditableLeaderboardPanel.this.getData());
                    getWhiteboardOwner().whiteboardProduced(whiteboard);
                    new EditScoreDialog(stringMessages, row.competitor.getName(), raceColumnName,
                            row.fieldsByRaceColumnName.get(raceColumnName).reasonForMaxPoints,
                            row.fieldsByRaceColumnName.get(raceColumnName).totalPoints, new DialogCallback<Util.Pair<MaxPointsReason, Double>>() {
                        @Override
                        public void ok(final Util.Pair<MaxPointsReason, Double> editedObject) {
                            addBusyTask();
                            getSailingService().updateLeaderboardScoreCorrection(getLeaderboardName(), row.competitor.getIdAsString(), raceColumnName,
                                    editedObject.getB(), getLeaderboardDisplayDate(),
                                            new AsyncCallback<Util.Triple<Double, Double, Boolean>>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    removeBusyTask();
                                    getErrorReporter().reportError(stringMessages.errorUpdatingLeaderboardScore(
                                            row.competitor.getName(), getLeaderboardName(),
                                            raceColumnName, t.getMessage()));
                                }

                                @Override
                                public void onSuccess(Util.Triple<Double, Double, Boolean> newTotalAndTotalPointsAndIsCorrected) {
                                    getSailingService().updateLeaderboardMaxPointsReason(getLeaderboardName(), row.competitor.getIdAsString(), raceColumnName,
                                            editedObject.getA(), getLeaderboardDisplayDate(),
                                                    new AsyncCallback<Util.Triple<Double, Double, Boolean>>() {
                                        @Override
                                        public void onFailure(Throwable t) {
                                            removeBusyTask();
                                            getErrorReporter().reportError(stringMessages.errorUpdatingLeaderboardScore(
                                                    row.competitor.getName(), getLeaderboardName(), raceColumnName, t.getMessage()));
                                        }

                                        @Override
                                        public void onSuccess(Util.Triple<Double, Double, Boolean> newTotalAndNetPointsAndIsCorrected) {
                                            removeBusyTask();
                                            final LeaderboardEntryDTO leaderboardEntryDTO = row.fieldsByRaceColumnName.get(raceColumnName);
                                            leaderboardEntryDTO.reasonForMaxPoints = editedObject.getA();
                                            leaderboardEntryDTO.totalPoints = newTotalAndNetPointsAndIsCorrected.getA();
                                            leaderboardEntryDTO.netPoints = newTotalAndNetPointsAndIsCorrected.getB();
                                            leaderboardEntryDTO.totalPointsCorrected = newTotalAndNetPointsAndIsCorrected.getC();
                                            maxPointsDropDownCellProvider.getCell().setViewData(row, null);
                                            totalPointsEditCellProvider.getCell().setViewData(row, null);
                                            whiteboard.setObjectWithWhichToUpdateRow(row);
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void cancel() {}
                    }).show();
                }
            };
        }

        @Override
        public String getValue(LeaderboardRowDTO object) {
            return stringMessages.edit();
        }
    }

    public EditableLeaderboardPanel(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            String leaderboardName, String leaderboardGroupName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, UserAgentDetails userAgent, Iterable<DetailType> availableDetailTypes) {
        super(null, null, sailingService, asyncActionsExecutor, new MultiRaceLeaderboardSettings(),
                new CompetitorSelectionModel(/* hasMultiSelection */true),
                leaderboardName, errorReporter, stringMessages, /* showRaceDetails */ true, new ClassicLeaderboardStyle(),
                FlagImageResolverImpl.get(), availableDetailTypes);
        suppressedCompetitorsShown = new ListDataProvider<>(new ArrayList<>());
        suppressedCompetitorsTable = createSuppressedCompetitorsTable();
        ImageResource importIcon = resources.importIcon();
        Anchor importAnchor = new Anchor(AbstractImagePrototype.create(importIcon).getSafeHtml());
        getRefreshAndSettingsPanel().insert(importAnchor, 0);
        importAnchor.setTitle(stringMessages.importOfficialResults());
        importAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sailingService.getScoreCorrectionProviderNames(new AsyncCallback<Iterable<String>>() {
                    @Override
                    public void onSuccess(Iterable<String> providerNames) {
                        ResultSelectionAndApplyDialog dialog = new ResultSelectionAndApplyDialog(EditableLeaderboardPanel.this, providerNames, getSailingService(), 
                                stringMessages, getErrorReporter());
                        dialog.show();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorGettingScoreCorrectionProvider(caught.getMessage()));
                    }
                });
            }
        });
        
        

        Grid scoreCorrectionInfoGrid = new Grid(3,4);
        scoreCorrectionInfoGrid.setCellPadding(3);
        scoreCorrectionInfoGrid.setWidget(0,  0, new Label(stringMessages.lastScoreCorrectionsTime() + ":"));
        lastScoreCorrectionTimeBox = new DateBox();
        scoreCorrectionInfoGrid.setWidget(0,  1, lastScoreCorrectionTimeBox);

        scoreCorrectionInfoGrid.setWidget(1,  0, new Label(stringMessages.lastScoreCorrectionsComment() + ":"));
        lastScoreCorrectionCommentBox = new TextBox();
        scoreCorrectionInfoGrid.setWidget(1,  1, lastScoreCorrectionCommentBox);

        final Button saveScoreCorrectionInfoBtn = new Button(stringMessages.save());
        scoreCorrectionInfoGrid.setWidget(2,  1, saveScoreCorrectionInfoBtn);

        saveScoreCorrectionInfoBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final String lastScoreCorrectionComment = lastScoreCorrectionCommentBox.getText();
                final Date lastScoreCorrectionTime = lastScoreCorrectionTimeBox.getValue();

                sailingService.updateLeaderboardScoreCorrectionMetadata(getLeaderboardName(), lastScoreCorrectionTime, lastScoreCorrectionComment, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void noarg) {
                        updateScoreCorrectionInformation(lastScoreCorrectionComment, lastScoreCorrectionTime);
                        Notification.notify(stringMessages.successfullyUpdatedScores(), NotificationType.SUCCESS);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorUpdatingLeaderboardScoreCorrection(caught.getMessage()));
                    }
                });
            }
        });

        final Button setScoreCorrectionDefaultTimeBtn = new Button(stringMessages.setTimeToNow());
        setScoreCorrectionDefaultTimeBtn.addStyleName("inlineButton");
        scoreCorrectionInfoGrid.setWidget(0, 2, setScoreCorrectionDefaultTimeBtn);
        showUncorrectedTotalPointsCheckbox = new CheckBox(stringMessages.showUncorrectedTotalPoints());
        showUncorrectedTotalPointsCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                loadCompleteLeaderboard(/* showProgress */ true);
            }
        });
        scoreCorrectionInfoGrid.setWidget(0, 3, showUncorrectedTotalPointsCheckbox);

        setScoreCorrectionDefaultTimeBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                lastScoreCorrectionTimeBox.setValue(new Date());
            }
        });
        getContentPanel().insert(scoreCorrectionInfoGrid, 0);
        getContentPanel().add(new Label(stringMessages.suppressedCompetitors() + ":"));
        getContentPanel().add(suppressedCompetitorsTable);

        // add a dedicated settings button that allows users to remove columns if needed; the settings
        // button has disappeared as the LeaderboardPanel switched to the use of the more general Component
        // framework with its own handling of component and settings visibility
        Anchor settingsAnchor = new Anchor(AbstractImagePrototype.create(getSettingsIcon()).getSafeHtml());
        settingsAnchor.setTitle(stringMessages.settings());
        settingsAnchor.addClickHandler(new SettingsClickHandler(stringMessages));
        getRefreshAndSettingsPanel().add(settingsAnchor);

    }

    private CellTable<CompetitorDTO> createSuppressedCompetitorsTable() {
        final Resources tableResources = GWT.create(AdminConsoleTableResources.class);
        CellTable<CompetitorDTO> result = new BaseCelltable<>(10000, tableResources);
        suppressedCompetitorsShown.addDataDisplay(result);
        final SuppressedShortInfoColumn suppressedSailIDColumn = new SuppressedShortInfoColumn();
        suppressedSailIDColumn.setSortable(true);
        result.addColumn(suppressedSailIDColumn, suppressedSailIDColumn.getHeader());
        final SuppressedCompetitorColumn suppressedCompetitorColumn = new SuppressedCompetitorColumn(
                new CompetitorColumnBase<CompetitorDTO>(this, stringMessages, new CompetitorFetcher<CompetitorDTO>() {
                    @Override
                    public CompetitorDTO getCompetitor(CompetitorDTO t) {
                        return t;
                    }
                }));
        result.addColumn(suppressedCompetitorColumn, suppressedCompetitorColumn.getHeader());
        suppressedCompetitorColumn.setSortable(true);
        final Column<CompetitorDTO, String> unsuppressButtonColumn = new Column<CompetitorDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(CompetitorDTO object) {
                return stringMessages.unsuppress();
            }
        };
        unsuppressButtonColumn.setFieldUpdater(new FieldUpdater<CompetitorDTO, String>() {
            @Override
            public void update(int index, final CompetitorDTO object, String value) {
                getSailingService().suppressCompetitorInLeaderboard(getLeaderboardName(), object.getIdAsString(),
                        /* suppressed */ false, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        getErrorReporter().reportError(stringMessages.errorTryingToUnsupressCompetitor(object.getName()));
                    }
                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify("Successfully unsuppressed competitor "+object.getName(), NotificationType.INFO);
                        // force a reload of the entire editable leaderboard to hide the now suppressed competitor
                        loadCompleteLeaderboard(/* showProgress */ true);
                    }
                });
            }
        });
        result.addColumn(unsuppressButtonColumn);
        final ListHandler<CompetitorDTO> sortHandler = new ListHandler<>(suppressedCompetitorsShown.getList());
        sortHandler.setComparator(suppressedSailIDColumn, suppressedSailIDColumn.getComparator());
        sortHandler.setComparator(suppressedCompetitorColumn, suppressedCompetitorColumn.getComparator());
        result.addColumnSortHandler(sortHandler);
        return result;
    }

    private void updateScoreCorrectionInformation(String lastScoreCorrectionComment, Date lastScoreCorrectionTime) {
        getLeaderboard().setComment(lastScoreCorrectionComment);
        getLeaderboard().setTimePointOfLastCorrectionsValidity(lastScoreCorrectionTime);
        updateLeaderboard(getLeaderboard());
    }

    /**
     * Always ensures that there is a carry column displayed because in the editable version
     * of the leaderboard the carried / accumulated values must always be editable and therefore
     * the column must always be shown.
     */
    @Override
    protected int updateCarryColumn(LeaderboardDTO leaderboard, int zeroBasedIndexOfCarryColumn) {
        ensureCarryColumn(zeroBasedIndexOfCarryColumn);
        return zeroBasedIndexOfCarryColumn+1;
    }

    @Override
    protected CarryColumn createCarryColumn() {
        return new EditableCarryColumn();
    }

    @Override
    protected CompetitorColumn createCompetitorColumn() {
        return new EditableCompetitorColumn(getCellListForEditableCompetitorColumn(),
                new CompetitorColumnBase<LeaderboardRowDTO>(this, stringMessages,
                        new CompetitorFetcher<LeaderboardRowDTO>() {
                    @Override
                    public CompetitorDTO getCompetitor(LeaderboardRowDTO t) {
                        return t.competitor;
                    }
                }));
    }

    private List<HasCell<LeaderboardRowDTO, ?>> getCellListForEditableCompetitorColumn() {
        List<HasCell<LeaderboardRowDTO, ?>> result = new ArrayList<HasCell<LeaderboardRowDTO, ?>>();
        result.add(new HasCell<LeaderboardRowDTO, String>() {
            private final ButtonCell cell = new ButtonCell();
            @Override
            public Cell<String> getCell() {
                return cell;
            }

            @Override
            public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
                return new FieldUpdater<LeaderboardRowDTO, String>() {
                    @Override
                    public void update(int index, final LeaderboardRowDTO row, String value) {
                        getSailingService().suppressCompetitorInLeaderboard(getLeaderboardName(), row.competitor.getIdAsString(),
                                /* suppressed */ true,
                                new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                getErrorReporter().reportError("Error trying to suppress competitor "+row.competitor.getName()+
                                        " in leaderboard "+getLeaderboardName());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                // force a reload of the entire editable leaderboard to hide the now suppressed competitor
                                loadCompleteLeaderboard(/* showProgress */ true);
                            }
                        });
                    }
                };
            }

            @Override
            public String getValue(LeaderboardRowDTO object) {
                return stringMessages.suppress();
            }
        });
        final class OptionalBoldRenderer implements SafeHtmlRenderer<String> {
            private LeaderboardRowDTO currentRow;

            @Override
            public SafeHtml render(String object) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                render(object, builder);
                return builder.toSafeHtml();
            }

            private boolean isDisplayNameSet() {
                return currentRow != null && getLeaderboard().isDisplayNameSet(currentRow.competitor);
            }

            @Override
            public void render(String value, SafeHtmlBuilder builder) {
                if (isDisplayNameSet()) {
                    builder.appendHtmlConstant("<b>");
                }
                builder.appendEscaped(value);
                if (isDisplayNameSet()) {
                    builder.appendHtmlConstant("</b>");
                }
            }

            public void setCurrentRow(LeaderboardRowDTO currentRow) {
                this.currentRow = currentRow;
            }
        }

        final OptionalBoldRenderer renderer = new OptionalBoldRenderer();

        final EditTextCell cellForCompetitorName = new EditTextCell(renderer) {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb) {
                renderer.setCurrentRow((LeaderboardRowDTO) context.getKey());
                super.render(context, value, sb);
            }
        };
        result.add(new HasCell<LeaderboardRowDTO, String>() {
            @Override
            public EditTextCell getCell() {
                return cellForCompetitorName;
            }

            @Override
            public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
                return createCompetitorColumnFieldUpdater(getCell());
            }

            @Override
            public String getValue(LeaderboardRowDTO row) {
                return getLeaderboard().getDisplayName(row.competitor);
            }
        });
        result.add(new HasCell<LeaderboardRowDTO, String>() {
            private final ButtonCell cell = new ButtonCell();
            @Override
            public Cell<String> getCell() {
                return cell;
            }

            @Override
            public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
                return new FieldUpdater<LeaderboardRowDTO, String>() {
                    @Override
                    public void update(int index, final LeaderboardRowDTO row, String valueToUpdate) {
                        new EditCompetitorNameDialog(stringMessages, row.competitor.getName(),
                                new DialogCallback<String>() {
                            @Override
                            public void ok(final String value) {
                                getSailingService().updateCompetitorDisplayNameInLeaderboard(getLeaderboardName(), row.competitor.getIdAsString(),
                                        value == null || value.length() == 0 ? null : value.trim(),
                                                new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable t) {
                                        EditableLeaderboardPanel.this.getErrorReporter().reportError(
                                                stringMessages.errorUpdatingDisplayNames(row.competitor.getName(), getLeaderboardName(), t.getMessage())); 
                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        if (getLeaderboard().competitorDisplayNames == null) {
                                            getLeaderboard().competitorDisplayNames = new HashMap<>();
                                        }
                                        getLeaderboard().competitorDisplayNames.put(row.competitor, value == null || value.trim().length() == 0 ? null : value.trim());
                                        cellForCompetitorName.setViewData(row, null); // ensure that getValue() is called again
                                        EditableLeaderboardPanel.this.getData().getList().set(
                                                EditableLeaderboardPanel.this.getData().getList().indexOf(row), row);
                                    }
                                });
                            }
                            @Override
                            public void cancel() {
                            }
                        }).show();
                    }
                };
            }

            @Override
            public String getValue(LeaderboardRowDTO object) {
                return stringMessages.edit();
            }
        });
        return result;
    }
    
    private List<HasCell<LeaderboardRowDTO, ?>> getCellListForEditableCarryColumn() {
        List<HasCell<LeaderboardRowDTO, ?>> result = new ArrayList<HasCell<LeaderboardRowDTO, ?>>();
        result.add(new HasCell<LeaderboardRowDTO, String>() {
            private final ButtonCell cell = new ButtonCell();
            @Override
            public Cell<String> getCell() {
                return cell;
            }

            @Override
            public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
                return new FieldUpdater<LeaderboardRowDTO, String>() {
                    @Override
                    public void update(int index, final LeaderboardRowDTO row, String value) {
                        new EditCarryValueDialog(stringMessages, row.competitor.getName(), row.carriedPoints,
                                new DialogCallback<Double>() {
                            @Override
                            public void ok(final Double value) {
                                updateCarriedPoints(EditableLeaderboardPanel.this.getData().getList().indexOf(row), row, value);
                            }
                            @Override
                            public void cancel() {
                            }
                        }).show();
                    }
                };
            }

            @Override
            public String getValue(LeaderboardRowDTO object) {
                return stringMessages.edit();
            }
        });
        final EditTextCell carryTextCell = new EditTextCell();
        final FieldUpdater<LeaderboardRowDTO, String> fieldUpdater = new FieldUpdater<LeaderboardRowDTO, String>() {
            @Override
            public void update(final int rowIndex, final LeaderboardRowDTO row, final String value) {
                updateCarriedPoints(rowIndex, row, value==null||value.length()==0 ? null : Double.valueOf(value.trim()));
            }
        };
        result.add(new HasCell<LeaderboardRowDTO, String>() {
            @Override
            public Cell<String> getCell() {
                return carryTextCell;
            }

            @Override
            public FieldUpdater<LeaderboardRowDTO, String> getFieldUpdater() {
                return fieldUpdater;
            }

            @Override
            public String getValue(LeaderboardRowDTO object) {
                return object.carriedPoints == null ? "" : scoreFormat.format(object.carriedPoints);
            }
        });
        return result;
    }
    
    private void updateCarriedPoints(final int rowIndex, final LeaderboardRowDTO row, final Double value) {
        getSailingService().updateLeaderboardCarryValue(getLeaderboardName(), row.competitor.getIdAsString(), value,
                new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                EditableLeaderboardPanel.this.getErrorReporter().reportError(
                        stringMessages.errorUpdatingCarriedPoints(row.competitor.getName(), getLeaderboardName(), t.getMessage()));
            }

            @Override
            public void onSuccess(Void v) {
                row.carriedPoints = value;
                EditableLeaderboardPanel.this.getData().getList().set(rowIndex, row);
            }
        });
    }

    @Override
    protected RaceColumn<?> createRaceColumn(RaceColumnDTO race) {
        return new EditableRaceColumn(race, getRaceColumnCellList(race));
    }

    private List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> getRaceColumnCellList(RaceColumnDTO race) {
        List<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>> list =
                new ArrayList<RowUpdateWhiteboardProducerThatAlsoHasCell<LeaderboardRowDTO, ?>>();
        final MaxPointsDropDownCellProvider maxPointsDropDownCellProvider = new MaxPointsDropDownCellProvider(race.getRaceColumnName());
        list.add(maxPointsDropDownCellProvider);
        final TotalPointsEditCellProvider totalPointsEditCellProvider = new TotalPointsEditCellProvider(race.getRaceColumnName());
        list.add(totalPointsEditCellProvider);
        final UncorrectedTotalPointsViewProvider uncorrectedViewProvider = new UncorrectedTotalPointsViewProvider(race.getRaceColumnName());
        list.add(uncorrectedViewProvider);
        list.add(new MaxPointsReasonAndTotalPointsEditButtonCell(stringMessages, race.getRaceColumnName(),
                maxPointsDropDownCellProvider, totalPointsEditCellProvider));
        return list;
    }
    
    /**
     * Computing the uncorrected scores can be expensive for large leaderboards; disable by default but allow user to enable
     */
    @Override
    protected boolean isFillTotalPointsUncorrected() {
        return showUncorrectedTotalPointsCheckbox==null?false:showUncorrectedTotalPointsCheckbox.getValue();
    }

    @Override
    public void updateLeaderboard(LeaderboardDTO leaderboard) {
        super.updateLeaderboard(leaderboard);
        if (leaderboard != null) {
            String lastScoreCorrectionComment = leaderboard.getComment();
            Date lastScoreCorrectionTime = leaderboard.getTimePointOfLastCorrectionsValidity();
            lastScoreCorrectionCommentBox.setText(lastScoreCorrectionComment != null ? lastScoreCorrectionComment : "");
            lastScoreCorrectionTimeBox.setValue(lastScoreCorrectionTime != null ? lastScoreCorrectionTime : new Date());
            suppressedCompetitorsShown.getList().clear();
            for (CompetitorDTO suppressedCompetitor : leaderboard.getSuppressedCompetitors()) {
                suppressedCompetitorsShown.getList().add(suppressedCompetitor);
            }
        }
    }

    /**
     * When editing, scrolling is not really an issue because usually the editable version is not used in live mode. But when
     * clicking the "Edit" button in a previously de-selected row, we still want the score to get the selection and the focus.
     * So turn off any focus/blur magic for this subclass.
     */
    @Override
    protected void blurFocusedElementAfterSelectionChange() {
    }   

}
