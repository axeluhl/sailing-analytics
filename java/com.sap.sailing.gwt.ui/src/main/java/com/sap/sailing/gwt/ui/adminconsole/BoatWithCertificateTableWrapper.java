package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;

/**
 * This Object is a more simplified version of the {@link BoatTableWrapper} to be used when matching {@link ORCCertificate}s to {@link Boat}s.
 * The shown information is stripped down to the sail number, boat name, boat class and some actions including linking/unlinking.
 * 
 * @author Daniel Lisunkin (i505543)
 * 
 */
public class BoatWithCertificateTableWrapper<S extends RefreshableSelectionModel<BoatDTO>> extends TableWrapper<BoatDTO, S> {
    private final LabeledAbstractFilterablePanel<BoatDTO> filterField;
    private final Consumer<BoatDTO> unlinkAction;
    private final Function<BoatDTO, Boolean> isLinkedChecker;
    
    public BoatWithCertificateTableWrapper(SailingServiceAsync sailingService, final UserService userService,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean multiSelection, boolean enablePager,
            int pagingSize, boolean allowActions, Consumer<BoatDTO> unlinkAction,
            SecuredDTO objectToCheckUpdatePermissionFor, Function<BoatDTO, Boolean> isLinkedChecker) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager, pagingSize,
                new EntityIdentityComparator<BoatDTO>() {
                    @Override
                    public boolean representSameEntity(BoatDTO dto1, BoatDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(BoatDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        this.unlinkAction = unlinkAction;
        this.isLinkedChecker = isLinkedChecker;
        ListHandler<BoatDTO> boatColumnListHandler = getColumnSortHandler();
        // boats table
        TextColumn<BoatDTO> boatNameColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO boat) {
                return boat.getName();
            }
        };
        boatNameColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatNameColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });
        TextColumn<BoatDTO> boatClassColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO competitor) {
                return competitor.getBoatClass() != null ? competitor.getBoatClass().getName() : "";
            }
        };
        boatClassColumn.setSortable(true);
        boatColumnListHandler.setComparator(boatClassColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getBoatClass().getName(), o2.getBoatClass().getName());
            }
        });
        Column<BoatDTO, SafeHtml> sailIdColumn = new Column<BoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(BoatDTO boat) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(boat.getSailId());
                return sb.toSafeHtml();
            }
        };
        sailIdColumn.setSortable(true);
        boatColumnListHandler.setComparator(sailIdColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getSailId(), o2.getSailId());
            }
        });
        Column<BoatDTO, SafeHtml> isLinkedColumn = new Column<BoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(BoatDTO boat) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendEscaped(isLinked(boat)?stringMessages.yes():stringMessages.no());
                return sb.toSafeHtml();
            }
        };
        isLinkedColumn.setSortable(true);
        boatColumnListHandler.setComparator(isLinkedColumn, new Comparator<BoatDTO>() {
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return Boolean.compare(isLinked(o1), isLinked(o2));
            }
        });
        filterField = new LabeledAbstractFilterablePanel<BoatDTO>(new Label(stringMessages.filterBoats()),
                new ArrayList<BoatDTO>(), dataProvider, stringMessages) {
            @Override
            public Iterable<String> getSearchableStrings(BoatDTO boat) {
                List<String> string = new ArrayList<String>();
                string.add(boat.getName());
                string.add(boat.getSailId());
                string.add(boat.getBoatClass().getName());
                string.add(boat.getIdAsString());
                return string;
            }

            @Override
            public AbstractCellTable<BoatDTO> getCellTable() {
                return table;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        // BoatTable edit features
        AccessControlledActionsColumn<BoatDTO, BoatConfigImagesBarCell> boatActionColumn = create(
                new BoatConfigImagesBarCell(getStringMessages()), userService, boatDTO->objectToCheckUpdatePermissionFor);
        boatActionColumn.addAction(BoatConfigImagesBarCell.UNLINK, DefaultActions.UPDATE, this::unlink);
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(boatColumnListHandler);
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(boatNameColumn, stringMessages.name());
        table.addColumn(boatClassColumn, stringMessages.boatClass());
        table.addColumn(isLinkedColumn, stringMessages.islinked());
        if (allowActions) {
            table.addColumn(boatActionColumn, stringMessages.actions());
        }
        table.ensureDebugId("BoatsWithVertificateTable");
    }
    
    protected boolean isLinked(BoatDTO boat) {
        return isLinkedChecker.apply(boat);
    }

    public void filterBoats(Iterable<BoatDTO> boats) {
        getFilteredBoats(boats);
    }
    
    private void getFilteredBoats(Iterable<BoatDTO> result) {
        filterField.updateAll(result);
    }
    
    void setBoats(Iterable<BoatDTO> boats) {
        filterField.updateAll(boats);
    }

    private void unlink(final BoatDTO boat) {
        unlinkAction.accept(boat);
    }
    
    public void refresh() {
        filterField.filter();
    }
}
