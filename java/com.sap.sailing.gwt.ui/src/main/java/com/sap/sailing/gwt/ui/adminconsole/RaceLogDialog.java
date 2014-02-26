package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogEventDTO;
import com.sap.sse.gwt.ui.DataEntryDialog;

public class RaceLogDialog extends DataEntryDialog<RaceLogDTO> {
	private final SailingServiceAsync sailingService;
	private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private RaceLogDTO raceLogDTO;
    private final TextArea editEventBox;
    private final Button saveButton;
    private Label currentPassLabel;
    private Button deleteEventButton;

    private final SingleSelectionModel<RaceLogEventDTO> selectionModel;
    private final CellTable<RaceLogEventDTO> raceLogEventsTable;
    private ListDataProvider<RaceLogEventDTO> raceLogEventsList;
    
    private final Panel editPanel;

    protected static class RaceLogValidator implements Validator<RaceLogDTO> {
        public RaceLogValidator() {
            super();
        }

        @Override
        public String getErrorMessage(RaceLogDTO valueToValidate) {
            return null;
        }
    }
        
    public RaceLogDialog(SailingServiceAsync sailingService, ErrorReporter errorReporter, final RaceLogDTO raceLogDTO, final StringMessages stringMessages, DialogCallback<RaceLogDTO> callback) {
        super(stringMessages.raceLog(), null, stringMessages.ok(), stringMessages.cancel(), new RaceLogValidator(), callback);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
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
        
        
        editPanel = new VerticalPanel();
        editEventBox = new TextArea();
        editEventBox.setSize("880px", "70px");
        editPanel.add(editEventBox);
        saveButton = new Button(stringMessages.add());
        saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveEditedEvent(selectionModel.getSelectedObject(), editEventBox.getText());
			}
		});
        editPanel.add(saveButton);
        selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				loadEvent(selectionModel.getSelectedObject());
			}
		});
    }
    
    private void updateRaceLog() {
    	RaceColumnDTO raceColumn = new RaceColumnDTO(false);
    	raceColumn.setName(raceLogDTO.getRaceColumnName());
    	FleetDTO fleet = new FleetDTO(raceLogDTO.getFleetName(), 0, null);
    	sailingService.getRaceLog(raceLogDTO.getLeaderboardName(), raceColumn, fleet, new AsyncCallback<RaceLogDTO>() {
					@Override
					public void onFailure(Throwable caught) {
						errorReporter.reportError("Failed to reload RaceLog: " + caught.getMessage());
					}

					@Override
					public void onSuccess(RaceLogDTO result) {
						editEventBox.setText("");
						raceLogDTO = result;
				        raceLogEventsList.setList(raceLogDTO.getEntries());
				        raceLogEventsList.refresh();
				        
				        currentPassLabel.setText(stringMessages.currentPass() + ":" + raceLogDTO.getCurrentPassId());
					}
				});
    }
    
    private void saveEditedEvent(RaceLogEventDTO event, String newText) {
    	try {
    		JSONValue json = JSONParser.parseStrict(newText);
    		
    		sailingService.addOrUpdateRaceLogEvent(raceLogDTO.getLeaderboardName(), raceLogDTO.getRaceColumnName(),
    				raceLogDTO.getFleetName(), json.toString(), new AsyncCallback<Void>() {
						@Override
						public void onSuccess(Void result) {
							updateRaceLog();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							errorReporter.reportError("Could not update event: " + caught.getMessage());
						}
					});
    	} catch (IllegalArgumentException e) {
    		errorReporter.reportError("Invalid JSON format: " + e.getMessage());
    	} catch (NullPointerException e) {
    		errorReporter.reportError("Invalid JSON format: " + e.getMessage());
    	}
    }
    
    private void loadEvent(RaceLogEventDTO event) {
    	if (event != null) {
    		deleteEventButton.setVisible(true);
    		sailingService.getRaceLogEvent(raceLogDTO.getLeaderboardName(), raceLogDTO.getRaceColumnName(),
    				raceLogDTO.getFleetName(), event.getEventId(), new AsyncCallback<String>() {
    			@Override
    			public void onSuccess(String result) {
    				editEventBox.setText(result);
    				saveButton.setText(stringMessages.save());
    			}

    			@Override
    			public void onFailure(Throwable caught) {
    				errorReporter.reportError("Could not load RaceLogEvent: " + caught.getMessage());
    			}
    		});
    	} else {
    		editEventBox.setText("");
    		saveButton.setText(stringMessages.add());
    		deleteEventButton.setVisible(false);
    	}
    }

    @Override
    protected RaceLogDTO getResult() {
        return null;
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel vPanel = new VerticalPanel(); 
     
        HorizontalPanel titlePanel = new HorizontalPanel();
        vPanel.add(titlePanel);
        
        currentPassLabel = new Label(stringMessages.currentPass() + ":" + raceLogDTO.getCurrentPassId());
        titlePanel.add(currentPassLabel);
        
        deleteEventButton = new Button(stringMessages.delete());
        deleteEventButton.setVisible(false);
        titlePanel.add(deleteEventButton);
        deleteEventButton.addClickHandler(new ClickHandler() {
        	@Override
        	public void onClick(ClickEvent clickEvent) {
        		RaceLogEventDTO event = selectionModel.getSelectedObject();
        		if (event != null) {
        			sailingService.deleteRaceLogEvent(raceLogDTO.getLeaderboardName(), raceLogDTO.getRaceColumnName(),
        					raceLogDTO.getFleetName(), event.getEventId(), new AsyncCallback<Void>() {
        				@Override
        				public void onSuccess(Void result) {
        					updateRaceLog();
        				}

        				@Override
        				public void onFailure(Throwable caught) {
        					errorReporter.reportError("Could not delete event: " + caught.getMessage());
        				}
        			});
        		}
			}
		});
        
        ScrollPanel scrollPanel = new ScrollPanel(raceLogEventsTable);
        scrollPanel.setSize("960px", "400px");
        
        vPanel.add(scrollPanel);
        
        vPanel.add(editPanel);
        
        return vPanel; 
    }
}
