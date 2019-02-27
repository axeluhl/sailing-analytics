package com.sap.sailing.gwt.ui.adminconsole;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYUP;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.CHANGE_OWNERSHIP;
import static com.sap.sse.security.shared.HasPermissions.DefaultActions.DELETE;
import static com.sap.sse.security.ui.client.component.AccessControlledActionsColumn.create;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_CHANGE_OWNERSHIP;
import static com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell.ACTION_DELETE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.domain.common.media.MediaUtil;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.adminconsole.multivideo.MultiURLChangeDialog;
import com.sap.sailing.gwt.ui.adminconsole.multivideo.MultiVideoDialog;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.MediaTracksRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.NewMediaWithRaceSelectionDialog;
import com.sap.sailing.gwt.ui.client.media.TimeFormatUtil;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.controls.BetterCheckboxCell;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledActionsColumn;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

/**
 * Table inspired by http://gwt.google.com/samples/Showcase/Showcase.html#!CwCellTable
 * 
 * @author D047974
 * 
 */
public class MediaPanel extends FlowPanel implements MediaTracksRefresher {
    private static AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
    
    private final SailingServiceAsync sailingService;
    private final LabeledAbstractFilterablePanel<MediaTrackWithSecurityDTO> filterableMediaTracks;
    private List<MediaTrackWithSecurityDTO> allMediaTracks;
    private final RegattaRefresher regattaRefresher;
    private final MediaServiceAsync mediaService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private Set<RegattasDisplayer> regattasDisplayers;
    private CellTable<MediaTrackWithSecurityDTO> mediaTracksTable;
    private ListDataProvider<MediaTrackWithSecurityDTO> mediaTrackListDataProvider = new ListDataProvider<>();
    private Date latestDate;
    private RefreshableMultiSelectionModel<MediaTrackWithSecurityDTO> refreshableSelectionModel;

    public MediaPanel(Set<RegattasDisplayer> regattasDisplayers, SailingServiceAsync sailingService,
            RegattaRefresher regattaRefresher, MediaServiceAsync mediaService, ErrorReporter errorReporter,
            StringMessages stringMessages, final UserService userService) {
        this.regattasDisplayers = regattasDisplayers;
        this.sailingService = sailingService;
        this.regattaRefresher = regattaRefresher;
        this.mediaService = mediaService;  
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        HorizontalPanel buttonAndFilterPanel = new HorizontalPanel();
        allMediaTracks = new ArrayList<>();
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadMediaTracks();
            }

        });
        buttonAndFilterPanel.add(refreshButton);
        Button addUrlButton = new Button(stringMessages.addMediaTrack());
        addUrlButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addUrlMediaTrack();
            }
        });
        buttonAndFilterPanel.add(addUrlButton);
        
        Button multiVideo = new Button(stringMessages.multiVideoLinking());
        multiVideo.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new MultiVideoDialog(sailingService, mediaService, stringMessages, errorReporter, new Runnable() {
                    
                    @Override
                    public void run() {
                        loadMediaTracks();
                    }
                }).center();
            }
        });
        buttonAndFilterPanel.add(multiVideo);
        
        
        Button multiVideoRename = new Button(this.stringMessages.multiUrlChangeMediaTrack());
        multiVideoRename.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Set<MediaTrackWithSecurityDTO> selected = refreshableSelectionModel.getSelectedSet();
                if (selected.isEmpty()) {
                    Notification.notify(stringMessages.noSelection(), NotificationType.ERROR);
                } else {
                    new MultiURLChangeDialog(mediaService, stringMessages, selected, errorReporter,
                            new Runnable() {
                                @Override
                                public void run() {
                                    loadMediaTracks();
                                }
                            }).center();
                }
            }
        });
        buttonAndFilterPanel.add(multiVideoRename);
        
        add(buttonAndFilterPanel);
        
        Label lblFilterRaces = new Label(stringMessages.filterMediaByName() + ":");
        lblFilterRaces.setWordWrap(false);
        buttonAndFilterPanel.setSpacing(5);
        buttonAndFilterPanel.add(lblFilterRaces);
        buttonAndFilterPanel.setCellVerticalAlignment(lblFilterRaces, HasVerticalAlignment.ALIGN_MIDDLE);
        this.filterableMediaTracks = new LabeledAbstractFilterablePanel<MediaTrackWithSecurityDTO>(lblFilterRaces,
                allMediaTracks,
                mediaTrackListDataProvider) {
            @Override
            public List<String> getSearchableStrings(MediaTrackWithSecurityDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.title);
                strings.add(t.url);
                if (t.startTime == null) {
                    GWT.log("startTime of media track "+t.title+" undefined");
                } else {
                    strings.add(t.startTime.toString());
                }
                return strings;
            }

            @Override
            public AbstractCellTable<MediaTrackWithSecurityDTO> getCellTable() {
                return mediaTracksTable;
            }
        };
        createMediaTracksTable(userService);
        filterableMediaTracks.getTextBox().ensureDebugId("MediaTracksFilterTextBox");
        buttonAndFilterPanel.add(filterableMediaTracks);
    }

    @Override
    public void loadMediaTracks() {
        mediaTrackListDataProvider.getList().clear();
        mediaService.getAllMediaTracks(new AsyncCallback<Iterable<MediaTrackWithSecurityDTO>>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError(t.toString());
            }

            @Override
            public void onSuccess(Iterable<MediaTrackWithSecurityDTO> allMediaTracks) {
                mediaTrackListDataProvider.getList().clear();
                Util.addAll(allMediaTracks, mediaTrackListDataProvider.getList());
                filterableMediaTracks.updateAll(mediaTrackListDataProvider.getList());
                mediaTrackListDataProvider.refresh();
            }
        });
    }

    private void createMediaTracksTable(final UserService userService) {
        // Create a CellTable.

        // Set a key provider that provides a unique key for each contact. If key is
        // used to identify contacts when fields (such as the name and address)
        // change.
        mediaTracksTable = new BaseCelltable<>(1000, tableResources);
        mediaTracksTable.setWidth("100%");

        // Attach a column sort handler to the ListDataProvider to sort the list.
        ListHandler<MediaTrackWithSecurityDTO> sortHandler = new ListHandler<>(mediaTrackListDataProvider.getList());
        mediaTracksTable.addColumnSortHandler(sortHandler);

        // Add a selection model so we can select cells.
        refreshableSelectionModel = new RefreshableMultiSelectionModel<>(
                new EntityIdentityComparator<MediaTrackWithSecurityDTO>() {
            @Override
                    public boolean representSameEntity(MediaTrackWithSecurityDTO dto1, MediaTrackWithSecurityDTO dto2) {
                return dto1.dbId.equals(dto2.dbId);
            }
            @Override
                    public int hashCode(MediaTrackWithSecurityDTO t) {
                return t.dbId.hashCode();
            }
        }, filterableMediaTracks.getAllListDataProvider());
        mediaTracksTable.setSelectionModel(refreshableSelectionModel,
                DefaultSelectionEventManager.createCustomManager(
                        new DefaultSelectionEventManager.CheckboxEventTranslator<MediaTrackWithSecurityDTO>() {
                    @Override
                            public boolean clearCurrentSelection(CellPreviewEvent<MediaTrackWithSecurityDTO> event) {
                        return !isCheckboxColumn(event.getColumn());
                    }

                    @Override
                            public SelectAction translateSelectionEvent(
                                    CellPreviewEvent<MediaTrackWithSecurityDTO> event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                            if (nativeEvent.getCtrlKey()) {
                                        MediaTrackWithSecurityDTO value = event.getValue();
                                refreshableSelectionModel.setSelected(value, !refreshableSelectionModel.isSelected(value));
                                return SelectAction.IGNORE;
                            }
                            if (!refreshableSelectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                                return SelectAction.DEFAULT;
                            }
                        }
                        return SelectAction.TOGGLE;
                    }

                    private boolean isCheckboxColumn(int columnIndex) {
                        return columnIndex == 0;
                    }
                }));

        // Initialize the columns.
        initTableColumns(sortHandler, userService);

        mediaTrackListDataProvider.addDataDisplay(mediaTracksTable);
        add(mediaTracksTable);
        allMediaTracks.clear();
        allMediaTracks.addAll(mediaTrackListDataProvider.getList());
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final ListHandler<MediaTrackWithSecurityDTO> sortHandler,
            final UserService userService) {
        Column<MediaTrackWithSecurityDTO, Boolean> checkColumn = new Column<MediaTrackWithSecurityDTO, Boolean>(
                new BetterCheckboxCell(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                        tableResources.cellTableStyle().cellTableCheckboxDeselected())) {
            @Override
            public Boolean getValue(MediaTrackWithSecurityDTO object) {
                // Get the value from the selection model.
                return refreshableSelectionModel.isSelected(object);
            }
        };
        mediaTracksTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        mediaTracksTable.setColumnWidth(checkColumn, 40, Unit.PX);

        // db id
        Column<MediaTrackWithSecurityDTO, String> dbIdColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new TextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return mediaTrack.dbId;
            }
        };
        dbIdColumn.setSortable(true);
        sortHandler.setComparator(dbIdColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return mediaTrack1.dbId.compareTo(mediaTrack2.dbId);
            }
        });
        mediaTracksTable.addColumn(dbIdColumn, stringMessages.id());
        mediaTracksTable.setColumnWidth(dbIdColumn, 10, Unit.PCT);

        // media title
        Column<MediaTrackWithSecurityDTO, String> titleColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new EditTextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return mediaTrack.title;
            }
        };
        titleColumn.setSortable(true);
        sortHandler.setComparator(titleColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return mediaTrack1.title.compareTo(mediaTrack2.title);
            }
        });
        mediaTracksTable.addColumn(titleColumn, stringMessages.title());
        titleColumn.setFieldUpdater(new FieldUpdater<MediaTrackWithSecurityDTO, String>() {
            public void update(int index, MediaTrackWithSecurityDTO mediaTrack, String newTitle) {
                // Called when the user changes the value.
                mediaTrack.title = newTitle;
                mediaService.updateTitle(mediaTrack, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError(t.toString());
                    }

                    @Override
                    public void onSuccess(Void allMediaTracks) {
                        mediaTrackListDataProvider.refresh();
                    }
                });
            }
        });
        mediaTracksTable.setColumnWidth(titleColumn, 20, Unit.PCT);

        // url
        Column<MediaTrackWithSecurityDTO, String> urlColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new EditTextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return mediaTrack.url;
            }
        };
        urlColumn.setSortable(true);
        sortHandler.setComparator(urlColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return mediaTrack1.url.compareTo(mediaTrack2.url);
            }
        });
        mediaTracksTable.addColumn(urlColumn, stringMessages.url());
        urlColumn.setFieldUpdater(new FieldUpdater<MediaTrackWithSecurityDTO, String>() {
            public void update(int index, MediaTrackWithSecurityDTO mediaTrack, String newUrl) {
                // Called when the user changes the value.
                mediaTrack.url = newUrl;
                mediaService.updateUrl(mediaTrack, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError(t.toString());
                    }

                    @Override
                    public void onSuccess(Void allMediaTracks) {
                        mediaTrackListDataProvider.refresh();
                    }
                });
            }
        });
        mediaTracksTable.setColumnWidth(urlColumn, 100, Unit.PCT);

        // assingedRaces

        Column<MediaTrackWithSecurityDTO, String> assignedRacesColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new ClickableTextCell() {
            public void onEnterKeyDown(Context context, Element parent, String value, NativeEvent event,
                    ValueUpdater<String> valueUpdater) {
                String type = event.getType();
                int keyCode = event.getKeyCode();
                boolean enterPressed = KEYUP.equals(type) && keyCode == KeyCodes.KEY_ENTER;
                if (CLICK.equals(type) || enterPressed) {
                    openAssignedRacesDialog(context, parent, valueUpdater);
                }
            }
        }) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                if (mediaTrack.assignedRaces != null) {
                    return listAssignedRaces(mediaTrack);
                } else
                    return "";
            }

        };
        assignedRacesColumn.setSortable(true);
        sortHandler.setComparator(assignedRacesColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return (listAssignedRaces(mediaTrack1)).compareTo(listAssignedRaces(mediaTrack2));
            }
        });
        mediaTracksTable.addColumn(assignedRacesColumn, stringMessages.linkedRaces());
        assignedRacesColumn.setFieldUpdater(new FieldUpdater<MediaTrackWithSecurityDTO, String>() {
            public void update(int index, MediaTrackWithSecurityDTO mediaTrack, String newAssignedRace) {
                // Called when the user changes the value.
                if (newAssignedRace.trim().isEmpty()) {
                    mediaTrack.assignedRaces.clear();
                } else {
                    //no op
                }
                mediaService.updateRace(mediaTrack, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError(t.toString());
                    }

                    @Override
                    public void onSuccess(Void allMediaTracks) {
                        mediaTrackListDataProvider.refresh();
                    }
                });
            }
        });
        mediaTracksTable.setColumnWidth(assignedRacesColumn, 100, Unit.PCT);

        // start time
        Column<MediaTrackWithSecurityDTO, String> startTimeColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new EditTextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return mediaTrack.startTime == null ? "" : TimeFormatUtil.DATETIME_FORMAT.format(mediaTrack.startTime
                        .asDate());
            }
        };
        startTimeColumn.setSortable(true);
        sortHandler.setComparator(startTimeColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return MediaUtil.compareDatesAllowingNull(mediaTrack1.startTime, mediaTrack2.startTime);
            }
        });
        startTimeColumn.setFieldUpdater(new FieldUpdater<MediaTrackWithSecurityDTO, String>() {
            public void update(int index, MediaTrackWithSecurityDTO mediaTrack, String newStartTime) {
                // Called when the user changes the value.
                if (newStartTime == null || newStartTime.trim().isEmpty()) {
                    mediaTrack.startTime = null;
                } else {
                    try {
                        mediaTrack.startTime = new MillisecondsTimePoint(TimeFormatUtil.DATETIME_FORMAT
                                .parse(newStartTime.trim()));
                    } catch (IllegalArgumentException e) {
                        errorReporter.reportError(stringMessages.mediaDateFormatError(TimeFormatUtil.DATETIME_FORMAT
                                .toString()));
                    }
                }
                mediaService.updateStartTime(mediaTrack, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError(t.toString());
                    }

                    @Override
                    public void onSuccess(Void allMediaTracks) {
                        mediaTrackListDataProvider.refresh();
                    }
                });
            }
        });
        mediaTracksTable.addColumn(startTimeColumn, stringMessages.startTime());
        mediaTracksTable.setColumnWidth(startTimeColumn, 100, Unit.PCT);

        // duration
        Column<MediaTrackWithSecurityDTO, String> durationColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new EditTextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return TimeFormatUtil.durationToHrsMinSec(mediaTrack.duration);
            }
        };
        durationColumn.setSortable(true);
        sortHandler.setComparator(durationColumn, new Comparator<MediaTrackWithSecurityDTO>() {
            final Comparator<Duration> durationComparator = new NullSafeComparableComparator<>(true);

            public int compare(MediaTrackWithSecurityDTO mediaTrack1, MediaTrackWithSecurityDTO mediaTrack2) {
                return durationComparator.compare(mediaTrack1.duration, mediaTrack2.duration);
            }
        });
        durationColumn.setFieldUpdater(new FieldUpdater<MediaTrackWithSecurityDTO, String>() {
            public void update(int index, MediaTrackWithSecurityDTO mediaTrack, String newDuration) {
                // Called when the user changes the value.
                if (newDuration == null || newDuration.trim().isEmpty()) {
                    mediaTrack.duration = null;
                } else {
                    try {
                        mediaTrack.duration = TimeFormatUtil.hrsMinSecToMilliSeconds(newDuration);
                    } catch (Exception e) {
                        errorReporter.reportError(stringMessages.mediaDateFormatError("Duration hh:mm:ss.xxx"));
                    }
                }
                mediaService.updateDuration(mediaTrack, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError(t.toString());
                    }

                    @Override
                    public void onSuccess(Void allMediaTracks) {
                        mediaTrackListDataProvider.refresh();
                    }
                });
            }
        });
        mediaTracksTable.addColumn(durationColumn, stringMessages.duration());
        mediaTracksTable.setColumnWidth(durationColumn, 100, Unit.PCT);
        // media type
        Column<MediaTrackWithSecurityDTO, String> mimeTypeColumn = new Column<MediaTrackWithSecurityDTO, String>(
                new TextCell()) {
            @Override
            public String getValue(MediaTrackWithSecurityDTO mediaTrack) {
                return mediaTrack.mimeType == null ? "" : mediaTrack.mimeType.toString();
            }
        };
        mimeTypeColumn.setSortable(true);
        mediaTracksTable.addColumn(mimeTypeColumn, stringMessages.mimeType());
        mediaTracksTable.setColumnWidth(mimeTypeColumn, 100, Unit.PCT);
        sortHandler.setComparator(mimeTypeColumn, (mt1, mt2)->
            (mt1.mimeType == null ? "" : mt1.mimeType.toString()).compareTo(
                    mt2.mimeType == null ? "" : mt2.mimeType.toString()));

        final HasPermissions type = SecuredDomainType.MEDIA_TRACK;

        final AccessControlledActionsColumn<MediaTrackWithSecurityDTO, DefaultActionsImagesBarCell> actionsColumn = create(
                new DefaultActionsImagesBarCell(stringMessages), userService);
        actionsColumn.addAction(ACTION_DELETE, DELETE, mediaTrack -> {
            if (Window.confirm(stringMessages.reallyRemoveMediaTrack(mediaTrack.title))) {
                removeMediaTrack(mediaTrack);
            }
        });
        final EditOwnershipDialog.DialogConfig<MediaTrackWithSecurityDTO> configOwnership = EditOwnershipDialog.create(
                userService.getUserManagementService(), type,
                mediaTrack -> {
                    /* no refresh action */},
                stringMessages);

        final EditACLDialog.DialogConfig<MediaTrackWithSecurityDTO> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, mediaTrack -> mediaTrack.getAccessControlList(), stringMessages);
        actionsColumn.addAction(ACTION_CHANGE_OWNERSHIP, CHANGE_OWNERSHIP, configOwnership::openDialog);
        actionsColumn.addAction(DefaultActionsImagesBarCell.ACTION_CHANGE_ACL, DefaultActions.CHANGE_ACL,
                mediaTrack -> configACL.openDialog(mediaTrack));
        mediaTracksTable.addColumn(actionsColumn, stringMessages.actions());

    }

    protected void removeMediaTrack(MediaTrack mediaTrack) {
        mediaService.deleteMediaTrack(mediaTrack, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError(t.toString());
            }

            @Override
            public void onSuccess(Void deleteMediaTrack) {
                loadMediaTracks();
            }
        });
    }

    private void addUrlMediaTrack() {
        NewMediaWithRaceSelectionDialog dialog = new NewMediaWithRaceSelectionDialog(mediaService,
                getDefaultStartTime(), stringMessages, sailingService, errorReporter, regattaRefresher,
                regattasDisplayers, new DialogCallback<MediaTrack>() {

                    @Override
                    public void cancel() {
                        // no op
                    }

                    @Override
                    public void ok(final MediaTrack mediaTrack) {
                        mediaService.addMediaTrack(mediaTrack, new AsyncCallback<String>() {

                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError(t.toString());
                            }

                            @Override
                            public void onSuccess(String dbId) {
                                mediaTrack.dbId = dbId;
                                loadMediaTracks();

                            }
                        });

                    }
                });
        dialog.show();
    }

    private TimePoint getDefaultStartTime() {
        
        if(getLatestDate()!=null){
            return new MillisecondsTimePoint(latestDate); 
        }else{
            return MillisecondsTimePoint.now();
        }

    }

    private Date getLatestDate() {
        sailingService.getRegattas(new MarkedAsyncCallback<List<RegattaDTO>>(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onSuccess(List<RegattaDTO> result) {
               latestDate = getDateFromLatestRegatta(result); 
            }

            private Date getDateFromLatestRegatta(List<RegattaDTO> result) {
                RegattaDTO latestRegatta = null;
                for (RegattaDTO regatta : result) {
                    if(regatta.getStartDate()!=null){
                        if(latestRegatta == null){
                            latestRegatta = regatta;
                        }else if(regatta.getStartDate().after(latestRegatta.getStartDate())) {
                            latestRegatta = regatta;
                        }
                    }
                }
                return latestRegatta == null ? null : latestRegatta.getStartDate();
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        }));
        return latestDate;
    }

    private String listAssignedRaces(MediaTrackWithSecurityDTO mediaTrack) {
        final String result;
        if (mediaTrack.assignedRaces.size() > 1) {
            result = String.valueOf(mediaTrack.assignedRaces.size());
        } else {
            String value = "";
            for (RegattaAndRaceIdentifier assignedRace : mediaTrack.assignedRaces) {
                value += assignedRace.getRegattaName() + " " + assignedRace.getRaceName() + ", ";
            }
            if (value.length() > 1) {
                result = value.substring(0, value.length() - 2);
            } else {
                result = value;
            }
        }
        return result;
    }

    public void onShow() {
        loadMediaTracks();
    }

    public void openAssignedRacesDialog(final Context context, final Element parent,
            final ValueUpdater<String> valueUpdater) {
        final MediaTrackWithSecurityDTO mediaTrack = (MediaTrackWithSecurityDTO) context.getKey();
        final AssignRacesToMediaDialog dialog = new AssignRacesToMediaDialog(sailingService, mediaTrack, errorReporter,
                regattaRefresher, stringMessages, null, new DialogCallback<Set<RegattaAndRaceIdentifier>>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(Set<RegattaAndRaceIdentifier> assignedRaces) {
                        if (assignedRaces.size() >= 0) {
                            String value = "";
                            for (RegattaAndRaceIdentifier assignedRace : assignedRaces) {
                                value = value.concat(assignedRace.getRegattaName() + "    "
                                        + assignedRace.getRaceName() + ",");
                            }
                            mediaTrack.assignedRaces.clear();
                            mediaTrack.assignedRaces.addAll(assignedRaces);
                            valueUpdater.update(value);
                        }

                    }
                });

        regattasDisplayers.add(dialog);
        dialog.ensureDebugId("AssignedRacesDialog");
        dialog.show();
    }

}
