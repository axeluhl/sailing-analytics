package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RaceLogDialog extends DataEntryDialog<RaceLogDTO> {
    private final StringMessages stringMessages;
    private final RaceLogDTO raceLogDTO;

    private final SelectionModel<RaceLogEventDTO> selectionModel;
    private final CellTable<RaceLogEventDTO> raceLogEventsTable;
    private ListDataProvider<RaceLogEventDTO> raceLogEventsList;

    protected static class RaceLogValidator implements Validator<RaceLogDTO> {
        public RaceLogValidator() {
            super();
        }

        @Override
        public String getErrorMessage(RaceLogDTO valueToValidate) {
            return null;
        }
    }
        
    public RaceLogDialog(final RaceLogDTO raceLogDTO, final StringMessages stringMessages, DialogCallback<RaceLogDTO> callback) {
        super(stringMessages.raceLog(), null, stringMessages.ok(), stringMessages.cancel(), new RaceLogValidator(), callback);
        this.stringMessages = stringMessages;
        this.raceLogDTO = raceLogDTO;

        raceLogEventsList = new ListDataProvider<RaceLogEventDTO>();
        selectionModel = new SingleSelectionModel<RaceLogEventDTO>();

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceLogEventsTable = new CellTable<RaceLogEventDTO>(/* pageSize */10000, tableRes);
        raceLogEventsTable.ensureDebugId("TrackedRacesTable");
        raceLogEventsTable.setSelectionModel(selectionModel);

        raceLogEventsList.setList(raceLogDTO.getEntries());
        ListHandler<RaceLogEventDTO> columnSortHandler = new ListHandler<RaceLogEventDTO>(raceLogEventsList.getList());
        raceLogEventsTable.addColumnSortHandler(columnSortHandler);

        TextColumn<RaceLogEventDTO> raceLogEventPassIdColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return String.valueOf(raceLogEventDTO.getPassId());
            }
        };
        raceLogEventPassIdColumn.setSortable(true);
        columnSortHandler.setComparator(raceLogEventPassIdColumn, new Comparator<RaceLogEventDTO>() {
            @Override
            public int compare(RaceLogEventDTO r1, RaceLogEventDTO r2) {
                return (r1.getPassId()<r2.getPassId() ? -1 : (r1.getPassId()==r2.getPassId() ? 0 : 1));
            }
        });

        TextColumn<RaceLogEventDTO> raceLogEventAuthorColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return String.valueOf(raceLogEventDTO.getAuthorName());
            }
        };

        TextColumn<RaceLogEventDTO> raceLogEventAuthorPriorityColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return String.valueOf(raceLogEventDTO.getAuthorPriority());
            }
        };

        TextColumn<RaceLogEventDTO> raceLogEventCreatedColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                Date createdAt = raceLogEventDTO.getCreatedAt();
                if(createdAt != null) {
                    return DateAndTimeFormatterUtil.defaultDateFormatter.render(createdAt) + " " + 
                            DateAndTimeFormatterUtil.defaultTimeFormatter.render(createdAt);
                }
                return "";
            }
        };
        raceLogEventCreatedColumn.setSortable(true);
        columnSortHandler.setComparator(raceLogEventCreatedColumn, new Comparator<RaceLogEventDTO>() {
            @Override
            public int compare(RaceLogEventDTO r1, RaceLogEventDTO r2) {
                if (r1.getCreatedAt() != null && r2.getCreatedAt() != null) {
                    return r1.getCreatedAt().compareTo(r2.getCreatedAt());
                }

                return r1.getCreatedAt() == null ? (r2.getCreatedAt() == null ? 0 : -1) : 1;
            }
        });

        TextColumn<RaceLogEventDTO> raceLogEventLogicalTimeColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                Date logicalTimePoint = raceLogEventDTO.getLogicalTimePoint();
                if(logicalTimePoint != null) {
                    return DateAndTimeFormatterUtil.defaultDateFormatter.render(logicalTimePoint) + " " + 
                            DateAndTimeFormatterUtil.defaultTimeFormatter.render(logicalTimePoint);
                }
                return "";
            }
        };
        raceLogEventLogicalTimeColumn.setSortable(true);
        columnSortHandler.setComparator(raceLogEventCreatedColumn, new Comparator<RaceLogEventDTO>() {
            @Override
            public int compare(RaceLogEventDTO r1, RaceLogEventDTO r2) {
                if (r1.getLogicalTimePoint() != null && r2.getLogicalTimePoint() != null) {
                    return r1.getLogicalTimePoint().compareTo(r2.getLogicalTimePoint());
                }

                return r1.getLogicalTimePoint() == null ? (r2.getLogicalTimePoint() == null ? 0 : -1) : 1;
            }
        });

        TextColumn<RaceLogEventDTO> raceLogEventTypeColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return raceLogEventDTO.getType();
            }
        };
        raceLogEventTypeColumn.setSortable(true);
        columnSortHandler.setComparator(raceLogEventTypeColumn, new Comparator<RaceLogEventDTO>() {
            @Override
            public int compare(RaceLogEventDTO r1, RaceLogEventDTO r2) {
                return new NaturalComparator().compare(r1.getType(), r2.getType());
            }
        });

        TextColumn<RaceLogEventDTO> raceLogEventInfoColumn = new TextColumn<RaceLogEventDTO>() {
            @Override
            public String getValue(RaceLogEventDTO raceLogEventDTO) {
                return raceLogEventDTO.getInfo();
            }
        };
        
        raceLogEventsTable.addColumn(raceLogEventPassIdColumn, "PassId");
        raceLogEventsTable.addColumn(raceLogEventTypeColumn, stringMessages.type());
        raceLogEventsTable.addColumn(raceLogEventCreatedColumn, "Created at");
        raceLogEventsTable.addColumn(raceLogEventLogicalTimeColumn, "Logical timepoint");
        raceLogEventsTable.addColumn(raceLogEventAuthorColumn, "Author");
        raceLogEventsTable.addColumn(raceLogEventAuthorPriorityColumn, "Priority");
        raceLogEventsTable.addColumn(raceLogEventInfoColumn, "Info");

        raceLogEventsList.addDataDisplay(raceLogEventsTable);
        raceLogEventsTable.getColumnSortList().push(raceLogEventLogicalTimeColumn);
    }

    @Override
    protected RaceLogDTO getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel vPanel = new VerticalPanel(); 
     
        Label currentPassLabel = new Label(stringMessages.currentPass() + ":" + raceLogDTO.getCurrentPassId());
        vPanel.add(currentPassLabel);
        
        ScrollPanel scrollPanel = new ScrollPanel(raceLogEventsTable);
        scrollPanel.setSize("960px", "500px");
        
        vPanel.add(scrollPanel);
        
        return vPanel; 
    }
}
