package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.ui.client.UserService;

/**
 * 
 * @author Daniel Lisunkin (i505543)
 *
 * @param <S>
 */
public class BoatCertificatesTableWrapper<S extends RefreshableSelectionModel<BoatDTO>> extends TableWrapper<BoatDTO, S> {
    private final LabeledAbstractFilterablePanel<BoatDTO> filterField;
    private final boolean filterCompetitorsWithBoat;
    private final boolean filterCompetitorsWithoutBoat;
    
    public BoatCertificatesTableWrapper(SailingServiceAsync sailingService, UserService userService, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager, boolean filterCompetitorsWithBoat, boolean filterCompetitorsWithoutBoat) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
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
        this.filterCompetitorsWithBoat = filterCompetitorsWithBoat;
        this.filterCompetitorsWithoutBoat = filterCompetitorsWithoutBoat;
        ListHandler<BoatDTO> competitorColumnListHandler = getColumnSortHandler();
        
        // competitors table
        TextColumn<BoatDTO> boatNameColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO boat) {
                return boat.getName();
            }
        };
        boatNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(boatNameColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/*case sensitive*/ false);
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });

        TextColumn<BoatDTO> boatClassColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO boat) {
                return boat.getBoatClass().getName();
            }
        };
        boatClassColumn.setSortable(true);
        competitorColumnListHandler.setComparator(boatClassColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(BoatDTO boat1, BoatDTO boat2) {
                if (boat1 == null && boat2 == null) {
                    return 0;
                } else if (boat1 != null && boat2 == null) { 
                    return 1;
                } else if (boat1 == null && boat2 != null) {
                    return -1;
                }
                return comparator.compare(boat1.getBoatClass().getName(), boat2.getBoatClass().getName());
            }
        });

        TextColumn<BoatDTO> sailIdColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO boat) {
                return boat.getSailId();
            }
        };
        sailIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(sailIdColumn, new Comparator<BoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(BoatDTO boat1, BoatDTO boat2) {
                if (boat1 == null && boat2 == null) {
                    return 0;
                } else if (boat1 != null && boat2 == null) { 
                    return 1;
                } else if (boat1 == null && boat2 != null) {
                    return -1;
                }
                return comparator.compare(boat1.getSailId(), boat2.getSailId());
            }
        });

        TextColumn<BoatDTO> boatIdColumn = new TextColumn<BoatDTO>() {
            @Override
            public String getValue(BoatDTO competitor) {
                return competitor.getIdAsString();
            }
        };
        boatIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(boatIdColumn, new Comparator<BoatDTO>() {
            @Override
            public int compare(BoatDTO o1, BoatDTO o2) {
                return new NaturalComparator(false).compare(o1.getIdAsString(), o2.getIdAsString());
            }
        });
        
        filterField = null;
        
        /*
        
        filterField = new LabeledAbstractFilterablePanel<BoatDTO>(new Label(getStringMessages().filterCompetitors()),
                new ArrayList<BoatDTO>(), dataProvider, stringMessages) {
            @Override
            public Iterable<String> getSearchableStrings(BoatDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                string.add(t.getIdAsString());
                string.add(t.getBoatClass().getName());
                string.add(t.getSailId());
                }
                return string;
            }

            @Override
            public AbstractCellTable<BoatDTO> getCellTable() {
                return table;
            }
        };
        filterField.setCheckboxEnabledFilter(comp -> userService.hasPermission(comp, DefaultActions.UPDATE));
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        
        // CompetitorTableEditFeatures
        final HasPermissions type = SecuredDomainType.COMPETITOR;
        AccessControlledActionsColumn<BoatDTO, CompetitorConfigImagesBarCell> competitorActionColumn = create(
                new CompetitorConfigImagesBarCell(getStringMessages()), userService);
        competitorActionColumn.addAction(CompetitorConfigImagesBarCell.ACTION_UPDATE, HasPermissions.DefaultActions.UPDATE, this::editCompetitor);
        competitorActionColumn.addAction(CompetitorConfigImagesBarCell.ACTION_REFRESH, this::allowUpdate);
        final DialogConfig<BoatDTO> editOwnerShipDialog = EditOwnershipDialog.create(userService.getUserManagementService(), SecuredDomainType.COMPETITOR,
                null, stringMessages);
        competitorActionColumn.addAction(CompetitorConfigImagesBarCell.ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP,
                editOwnerShipDialog::openDialog);
        
        final EditACLDialog.DialogConfig<BoatDTO> configACL = EditACLDialog
                .create(userService.getUserManagementService(), type, null, stringMessages);
        competitorActionColumn.addAction(CompetitorConfigImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                configACL::openDialog);
        
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(competitorColumnListHandler);
        table.addColumn(boatClassColumn, getStringMessages().boatClass());
        table.addColumn(flagImageColumn, stringMessages.flags());
        table.addColumn(sailIdColumn, getStringMessages().sailNumber());
        table.addColumn(boatNameColumn, getStringMessages().name());
        table.addColumn(competitorSearchTagColumn, getStringMessages().searchTag());
        table.addColumn(boatIdColumn, getStringMessages().id());
        SecuredDTOOwnerColumn.configureOwnerColumns(table, getColumnSortHandler(), stringMessages);
        table.addColumn(competitorActionColumn, getStringMessages().actions());
        table.ensureDebugId("BoatCertificatesTable");
    }
    
    public Iterable<BoatDTO> getAllCompetitors() {
        return filterField.getAll();
    }
    
    public LabeledAbstractFilterablePanel<BoatDTO> getFilterField() {
        return filterField;
    }
    
    public void refreshCompetitorList(Iterable<BoatDTO> competitors) {
        getFilteredCompetitors(competitors);
    }
    
    public void refreshCompetitorList(String leaderboardName) {
        refreshCompetitorList(leaderboardName, null);
    }
    
    */
    /**
     * @param leaderboardName If null, all existing competitors are loaded
     */
        /*
    public void refreshCompetitorList(String leaderboardName, final Callback<Iterable<BoatDTO>,
            Throwable> callback) {
        final AsyncCallback<Iterable<BoatDTO>> myCallback = new AsyncCallback<Iterable<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Iterable<BoatDTO> result) {
                getFilteredCompetitors(result);
                refreshCompetitorList(result);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }
        };
        if (leaderboardName != null) {
            sailingService.getCompetitorsOfLeaderboard(leaderboardName, myCallback);
        } else {
            sailingService.getCompetitors(filterCompetitorsWithBoat, filterCompetitorsWithoutBoat, myCallback);
        }
    }

    private void getFilteredCompetitors(Iterable<BoatDTO> result) {
        filterField.updateAll(result);
    }

    private void editCompetitor(final BoatDTO competitor) {
        final String boatClassName;
        if (competitor.hasBoat()) {
            BoatClassDTO boatClass = ((CompetitorWithBoatDTO) competitor).getBoatClass();
            boatClassName = boatClass != null ? boatClass.getName() : null;
        } else {
            boatClassName = null;
        }
        if (boatClassName != null) {
            openEditCompetitorWithBoatDialog((CompetitorWithBoatDTO) competitor, boatClassName);
        } else {
            openEditCompetitorWithoutBoatDialog(competitor);
        }
    }

    void openEditCompetitorWithBoatDialog(final CompetitorWithBoatDTO originalCompetitor, String boatClassName) {
        final CompetitorWithBoatEditDialog dialog = new CompetitorWithBoatEditDialog(getStringMessages(), 
                originalCompetitor, new DialogCallback<CompetitorWithBoatDTO>() {
            @Override
            public void ok(final CompetitorWithBoatDTO competitor) {
                sailingService.addOrUpdateCompetitorWithBoat(competitor, new AsyncCallback<CompetitorWithBoatDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to update competitor with boat: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(CompetitorWithBoatDTO updatedCompetitor) {
                        //only reload selected competitors reloading with refreshCompetitorList(leaderboardName)
                        //would not work in case the list is not based on a leaderboard e.g. AbstractCompetitorRegistrationDialog
                        int editedCompetitorIndex = getFilterField().indexOf(originalCompetitor);
                        getFilterField().remove(originalCompetitor);
                        if (editedCompetitorIndex >= 0){
                            getFilterField().add(editedCompetitorIndex, updatedCompetitor);
                        } else {
                            //in case competitor was not present --> not edit, but create
                            getFilterField().add(updatedCompetitor);
                        }
                        getDataProvider().refresh();
                    }  
                });
            }

            @Override
            public void cancel() {
            }
        },  boatClassName);
        dialog.show();
    }

    void openEditCompetitorWithoutBoatDialog(final BoatDTO originalCompetitor) {
        final CompetitorEditDialog<BoatDTO> dialog = CompetitorEditDialog.create(getStringMessages(), originalCompetitor, new DialogCallback<BoatDTO>() {
            @Override
            public void ok(final BoatDTO competitor) {
                sailingService.addOrUpdateCompetitorWithoutBoat(competitor, new AsyncCallback<BoatDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to update competitor: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(BoatDTO updatedCompetitor) {
                        //only reload selected competitors reloading with refreshCompetitorList(leaderboardName)
                        //would not work in case the list is not based on a leaderboard e.g. AbstractCompetitorRegistrationDialog
                        int editedCompetitorIndex = getFilterField().indexOf(originalCompetitor);
                        getFilterField().remove(originalCompetitor);
                        if (editedCompetitorIndex >= 0){
                            getFilterField().add(editedCompetitorIndex, updatedCompetitor);
                        } else {
                            //in case competitor was not present --> not edit, but create
                            getFilterField().add(updatedCompetitor);
                        }
                        getDataProvider().refresh();
                    }  
                });
            }

            @Override
            public void cancel() {
            }
        });
        dialog.show();
    }

    protected void allowUpdate(final Iterable<BoatDTO> competitors) {
        List<BoatDTO> serializableSingletonList = new ArrayList<>();
        Util.addAll(competitors, serializableSingletonList);
        sailingService.allowCompetitorResetToDefaults(serializableSingletonList, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to allow resetting competitors " + competitors
                        + " to defaults: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                Notification.notify(getStringMessages().successfullyAllowedCompetitorReset(competitors.toString()), NotificationType.SUCCESS);
            }
        });
    }

    private void allowUpdate(final BoatDTO competitor) {
        allowUpdate(Collections.singleton(competitor));
    }

    */

    /**
     * This method makes rows grayed out with a tool tip
     */
        
        /*
    public void grayOutCompetitors(final List<CompetitorWithToolTipDTO> competitors) {
        table.addCellPreviewHandler((CellPreviewEvent<BoatDTO> event) -> {
            for (CompetitorWithToolTipDTO competitor : competitors) {
                if (competitor.getCompetitor().equals(event.getValue())) {
                    table.getRowElement(event.getIndex()).setTitle(competitor.getToolTipMessage());
                }
            }
        });
        table.setRowStyles((BoatDTO row, int rowIndex) -> {
            for (CompetitorWithToolTipDTO competitor : competitors) {
                if (competitor.getCompetitor().equals(row)) {
                    return getTableRes().cellTableStyle().cellTableDisabledRow();
                }
            }
            return "";
        });
    }
    */
} }
