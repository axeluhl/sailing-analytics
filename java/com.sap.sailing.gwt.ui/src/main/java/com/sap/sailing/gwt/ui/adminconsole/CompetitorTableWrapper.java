package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithToolTipDTO;
import com.sap.sailing.gwt.ui.adminconsole.ColorColumn.ColorRetriever;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

/**
 * A filterable competitor table. The data model is managed by the {@link #getFilterField() filter field}. In
 * order to set an initial set of competitors to display by this table, use {@link #refreshCompetitorList(Iterable)}.
 * The selected competitors can be obtained from the {@link #getSelectionModel() selection model}. The competitor
 * set can also be updated to that of a leaderboard by using {@link #refreshCompetitorList(String)}, providing the
 * leaderboard name as parameter. The competitors currently in the table (regardless of the current filter settings)
 * are returned by {@link #getAllCompetitors()}.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <S>
 */
public class CompetitorTableWrapper<S extends RefreshableSelectionModel<CompetitorWithBoatDTO>> extends TableWrapper<CompetitorWithBoatDTO, S> {
    private final LabeledAbstractFilterablePanel<CompetitorWithBoatDTO> filterField;
    private final boolean filterCompetitorsWithBoat;
    private final boolean filterCompetitorsWithoutBoat;
    
    public CompetitorTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter,
            boolean multiSelection, boolean enablePager, boolean filterCompetitorsWithBoat, boolean filterCompetitorsWithoutBoat) {
        super(sailingService, stringMessages, errorReporter, multiSelection, enablePager,
                new EntityIdentityComparator<CompetitorWithBoatDTO>() {
                    @Override
                    public boolean representSameEntity(CompetitorWithBoatDTO dto1, CompetitorWithBoatDTO dto2) {
                        return dto1.getIdAsString().equals(dto2.getIdAsString());
                    }
                    @Override
                    public int hashCode(CompetitorWithBoatDTO t) {
                        return t.getIdAsString().hashCode();
                    }
                });
        this.filterCompetitorsWithBoat = filterCompetitorsWithBoat;
        this.filterCompetitorsWithoutBoat = filterCompetitorsWithoutBoat;
        ListHandler<CompetitorWithBoatDTO> competitorColumnListHandler = getColumnSortHandler();
        
        // competitors table
        TextColumn<CompetitorWithBoatDTO> competitorNameColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getName();
            }
        };
        competitorNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorNameColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        TextColumn<CompetitorWithBoatDTO> competitorShortNameColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getShortName();
            }
        };
        competitorShortNameColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorShortNameColumn, new Comparator<CompetitorWithBoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return comparator.compare(o1.getShortName(), o2.getShortName());
            }
        });

        TextColumn<CompetitorWithBoatDTO> boatClassColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getBoatClass() != null ? competitor.getBoatClass().getName() : "";
            }
        };
        boatClassColumn.setSortable(true);
        competitorColumnListHandler.setComparator(boatClassColumn, new Comparator<CompetitorWithBoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* caseSensitive */ false);
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                BoatDTO boat1 = o1.getBoat();
                BoatDTO boat2 = o2.getBoat();
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

        Column<CompetitorWithBoatDTO, SafeHtml> flagImageColumn = new Column<CompetitorWithBoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorWithBoatDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                final String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
                final String flagImageURL = competitor.getFlagImageURL();
                if (flagImageURL != null && !flagImageURL.isEmpty()) {
                    sb.append(FlagImageResolver.FLAG_RENDERER_TEMPLATE.imageWithTitle(flagImageURL, competitor.getName()));
                    sb.appendHtmlConstant("&nbsp;");
                } else {
                    final ImageResource flagImageResource;
                    if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                        flagImageResource = FlagImageResolverImpl.get().getEmptyFlagImageResource();
                    } else {
                        flagImageResource = FlagImageResolverImpl.get().getFlagImageResource(twoLetterIsoCountryCode);
                    }
                    if (flagImageResource != null) {
                        sb.append(FlagImageResolver.FLAG_RENDERER_TEMPLATE.imageWithTitle(flagImageResource.getSafeUri().asString() ,competitor.getName()));
                        sb.appendHtmlConstant("&nbsp;");
                    }
                }
                return sb.toSafeHtml();
            }
        };

        TextColumn<CompetitorWithBoatDTO> sailIdColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                if (competitor.getBoat() != null) {
                    return competitor.getBoat().getSailId();
                }
                return "";
            }
        };

        sailIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(sailIdColumn, new Comparator<CompetitorWithBoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                BoatDTO boat1 = o1.getBoat();
                BoatDTO boat2 = o2.getBoat();
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

        Column<CompetitorWithBoatDTO, SafeHtml> displayColorColumn = new ColorColumn<>(new ColorRetriever<CompetitorWithBoatDTO>() {
            @Override
            public Color getColor(CompetitorWithBoatDTO t) {
                return t.getColor();
            }
        });
        
        Column<CompetitorWithBoatDTO, SafeHtml> imageColumn = new Column<CompetitorWithBoatDTO, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(CompetitorWithBoatDTO competitor) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (competitor.getImageURL() != null && !competitor.getImageURL().isEmpty()) {
                    sb.appendHtmlConstant("<img src=\"" + competitor.getImageURL() + "\" height=\"40px\" title=\""
                            + competitor.getImageURL() + "\"/>");
                }
                return sb.toSafeHtml();
            }
        };
        imageColumn.setSortable(true);
        competitorColumnListHandler.setComparator(imageColumn, new Comparator<CompetitorWithBoatDTO>() {
            private final NaturalComparator comparator = new NaturalComparator(/* case sensitive */ false);
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return comparator.compare(o1.getImageURL(), o2.getImageURL());
            }
        });

        TextColumn<CompetitorWithBoatDTO> competitorIdColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getIdAsString();
            }
        };
        competitorIdColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorIdColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return new NaturalComparator(false).compare(o1.getIdAsString(), o2.getIdAsString());
            }
        });

        TextColumn<CompetitorWithBoatDTO> competitorEMailColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getEmail();
            }
        };
        competitorEMailColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorEMailColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return new NaturalComparator(false).compare(o1.getEmail(), o2.getEmail());
            }
        });

        TextColumn<CompetitorWithBoatDTO> competitorSearchTagColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getSearchTag();
            }
        };
        competitorSearchTagColumn.setSortable(true);
        competitorColumnListHandler.setComparator(competitorSearchTagColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return new NaturalComparator(false).compare(o1.getSearchTag(), o2.getSearchTag());
            }
        });

        TextColumn<CompetitorWithBoatDTO> timeOnTimeFactorColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getTimeOnTimeFactor()==null?"":(""+competitor.getTimeOnTimeFactor());
            }
        };
        timeOnTimeFactorColumn.setSortable(true);
        competitorColumnListHandler.setComparator(timeOnTimeFactorColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
            public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                return o1.getTimeOnTimeFactor()==null?o2.getTimeOnTimeFactor()==null?0:-1:o2.getTimeOnTimeFactor()==null?1:
                    o1.getTimeOnTimeFactor().compareTo(o2.getTimeOnTimeFactor());
            }
        });
        TextColumn<CompetitorWithBoatDTO> timeOnDistanceAllowancePerNauticalMileColumn = new TextColumn<CompetitorWithBoatDTO>() {
            @Override
            public String getValue(CompetitorWithBoatDTO competitor) {
                return competitor.getTimeOnDistanceAllowancePerNauticalMile()==null?"":(""+competitor.getTimeOnDistanceAllowancePerNauticalMile());
            }
        };
        timeOnTimeFactorColumn.setSortable(true);
        competitorColumnListHandler.setComparator(timeOnDistanceAllowancePerNauticalMileColumn, new Comparator<CompetitorWithBoatDTO>() {
            @Override
                    public int compare(CompetitorWithBoatDTO o1, CompetitorWithBoatDTO o2) {
                        return o1.getTimeOnDistanceAllowancePerNauticalMile() == null ? o2
                                .getTimeOnDistanceAllowancePerNauticalMile() == null ? 0 : -1 : o2
                                .getTimeOnDistanceAllowancePerNauticalMile() == null ? 1 : o1
                                .getTimeOnDistanceAllowancePerNauticalMile().compareTo(
                                        o2.getTimeOnDistanceAllowancePerNauticalMile());
                    }
        });

        
        filterField = new LabeledAbstractFilterablePanel<CompetitorWithBoatDTO>(new Label(stringMessages.filterCompetitors()),
                new ArrayList<CompetitorWithBoatDTO>(), table, dataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(CompetitorWithBoatDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                string.add(t.getShortName());
                string.add(t.getIdAsString());
                string.add(t.getSearchTag());
                if (t.getBoatClass() != null) {
                    string.add(t.getBoatClass().getName());
                }
                if (t.getSailID() != null) {
                    string.add(t.getSailID());
                }
                return string;
            }
        };
        registerSelectionModelOnNewDataProvider(filterField.getAllListDataProvider());
        
        //CompetitorTableEditFeatures
        ImagesBarColumn<CompetitorWithBoatDTO, CompetitorConfigImagesBarCell> competitorActionColumn = new ImagesBarColumn<CompetitorWithBoatDTO, CompetitorConfigImagesBarCell>(
                new CompetitorConfigImagesBarCell(stringMessages));
        competitorActionColumn.setFieldUpdater(new FieldUpdater<CompetitorWithBoatDTO, String>() {
            @Override
            public void update(int index, final CompetitorWithBoatDTO competitor, String value) {
                if (CompetitorConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    if (competitor.getBoat() != null) {
                        String boatClass = null;
                        if (competitor.getBoat() != null && competitor.getBoat().getBoatClass() != null) {
                            boatClass = competitor.getBoat().getBoatClass().getName();
                        }
                        openEditCompetitorWithBoatDialog(competitor, boatClass);
                    } else {
                        openEditCompetitorWithoutBoatDialog(competitor);
                    }
                } else if (CompetitorConfigImagesBarCell.ACTION_REFRESH.equals(value)) {
                    allowUpdate(Collections.singleton(competitor));
                }
            }
        });
        
        mainPanel.insert(filterField, 0);
        table.addColumnSortHandler(competitorColumnListHandler);
        table.addColumn(competitorNameColumn, stringMessages.name());
        table.addColumn(competitorShortNameColumn, stringMessages.shortName());
        table.addColumn(flagImageColumn, stringMessages.flags());
        table.addColumn(timeOnTimeFactorColumn, stringMessages.timeOnTimeFactor());
        table.addColumn(timeOnDistanceAllowancePerNauticalMileColumn, stringMessages.timeOnDistanceAllowanceInSecondsPerNauticalMile());
        table.addColumn(displayColorColumn, stringMessages.color());
        table.addColumn(imageColumn, stringMessages.image());
        table.addColumn(competitorEMailColumn, stringMessages.email());
        table.addColumn(competitorSearchTagColumn, stringMessages.searchTag());
        table.addColumn(competitorIdColumn, stringMessages.id());
        table.addColumn(sailIdColumn, stringMessages.sailNumber());
        table.addColumn(boatClassColumn, stringMessages.boatClass());
        table.addColumn(competitorActionColumn, stringMessages.actions());
        table.ensureDebugId("CompetitorsTable");
    }
    
    public Iterable<CompetitorWithBoatDTO> getAllCompetitors() {
        return filterField.getAll();
    }
    
    public LabeledAbstractFilterablePanel<CompetitorWithBoatDTO> getFilterField() {
        return filterField;
    }
    
    public void refreshCompetitorList(Iterable<CompetitorWithBoatDTO> competitors) {
        getFilteredCompetitors(competitors);
    }
    
    public void refreshCompetitorList(String leaderboardName) {
        refreshCompetitorList(leaderboardName, null);
    }
    
    /**
     * @param leaderboardName If null, all existing competitors are loaded
     */
    public void refreshCompetitorList(String leaderboardName, final Callback<Iterable<CompetitorWithBoatDTO>,
            Throwable> callback) {
        final AsyncCallback<Iterable<CompetitorWithBoatDTO>> myCallback = new AsyncCallback<Iterable<CompetitorWithBoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getCompetitors() - Failure: " + caught.getMessage());
                if (callback != null) {
                    callback.onFailure(caught);
                }
            }

            @Override
            public void onSuccess(Iterable<CompetitorWithBoatDTO> result) {
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

    private void getFilteredCompetitors(Iterable<CompetitorWithBoatDTO> result) {
        filterField.updateAll(result);
    }

    void openEditCompetitorWithBoatDialog(final CompetitorWithBoatDTO originalCompetitor, String boatClass) {
        final CompetitorWithBoatEditDialog dialog = new CompetitorWithBoatEditDialog(stringMessages, 
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
        },  boatClass);
        dialog.show();
    }

    void openEditCompetitorWithoutBoatDialog(final CompetitorWithBoatDTO originalCompetitor) {
        final CompetitorEditDialog dialog = CompetitorEditDialog.create(stringMessages, originalCompetitor, new DialogCallback<CompetitorWithBoatDTO>() {
            @Override
            public void ok(final CompetitorWithBoatDTO competitor) {
                sailingService.addOrUpdateCompetitorWithoutBoat(competitor, new AsyncCallback<CompetitorWithBoatDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to update competitor: " + caught.getMessage());
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
        });
        dialog.show();
    }

    protected void allowUpdate(final Iterable<CompetitorWithBoatDTO> competitors) {
        List<CompetitorWithBoatDTO> serializableSingletonList = new ArrayList<CompetitorWithBoatDTO>();
        Util.addAll(competitors, serializableSingletonList);
        sailingService.allowCompetitorResetToDefaults(serializableSingletonList, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to allow resetting competitors " + competitors
                        + " to defaults: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                Window.alert(stringMessages.successfullyAllowedCompetitorReset(competitors.toString()));
            }
        });
    }

    /**
     * This method makes rows grayed out with a tool tip
     */
    public void grayOutCompetitors(final List<CompetitorWithToolTipDTO> competitors) {
        table.addCellPreviewHandler((CellPreviewEvent<CompetitorWithBoatDTO> event) -> {
            for (CompetitorWithToolTipDTO competitor : competitors) {
                if (competitor.getCompetitor().equals(event.getValue())) {
                    table.getRowElement(event.getIndex()).setTitle(competitor.getToolTipMessage());
                }
            }
        });
        table.setRowStyles((CompetitorWithBoatDTO row, int rowIndex) -> {
            for (CompetitorWithToolTipDTO competitor : competitors) {
                if (competitor.getCompetitor().equals(row)) {
                    return tableRes.cellTableStyle().cellTableDisabledRow();
                }
            }
            return "";
        });
    }
}
