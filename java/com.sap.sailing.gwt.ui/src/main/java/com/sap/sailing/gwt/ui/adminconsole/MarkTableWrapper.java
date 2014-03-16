package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkTableWrapper implements IsWidget {
    private final CellTable<MarkDTO> table;
    private final SelectionModel<MarkDTO> selectionModel;
    private final ListDataProvider<MarkDTO> listProvider;
    private List<MarkDTO> allMarks;
    private final VerticalPanel mainPanel;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    
    @Override
    public Widget asWidget() {
        return mainPanel;
    }

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    
    public MarkTableWrapper(SelectionModel<MarkDTO> selectionModel, SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        mainPanel = new VerticalPanel();
        listProvider = new ListDataProvider<MarkDTO>();
        
        this.selectionModel = selectionModel;
        
        table = new CellTable<MarkDTO>(/* pageSize */10000, tableRes);
        mainPanel.add(table);
        listProvider.addDataDisplay(table);
        table.setSelectionModel(selectionModel);
        TextColumn<MarkDTO> markNameColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getName();
            }
        };
        table.addColumn(markNameColumn, stringMessages.mark());

        final SafeHtmlCell markPositionCell = new SafeHtmlCell();
        Column<MarkDTO, SafeHtml> markPositionColumn = new Column<MarkDTO, SafeHtml>(markPositionCell) {
            @Override
            public SafeHtml getValue(MarkDTO mark) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                if(mark.position != null) {
                    NumberFormat fmt = NumberFormat.getFormat("#.###");
                    builder.appendEscaped(fmt.format(mark.position.latDeg)+", "+fmt.format(mark.position.lngDeg));
                }
                return builder.toSafeHtml();
            }
        };
        table.addColumn(markPositionColumn, stringMessages.position());
        
        TextColumn<MarkDTO> markColorColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.color != null ? markDTO.color : "";
            }
        };
        table.addColumn(markColorColumn, stringMessages.color());

        TextColumn<MarkDTO> markShapeColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.shape != null ? markDTO.shape : "";
            }
        };
        table.addColumn(markShapeColumn, stringMessages.shape());

        TextColumn<MarkDTO> markPatternColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.pattern != null ? markDTO.pattern : "";
            }
        };
        table.addColumn(markPatternColumn, stringMessages.pattern());

        TextColumn<MarkDTO> markUUIDColumn = new TextColumn<MarkDTO>() {
            @Override
            public String getValue(MarkDTO markDTO) {
                return markDTO.getIdAsString();
            }
        };
        table.addColumn(markUUIDColumn, "UUID");
    }
    
    public MarkTableWrapper(SailingServiceAsync sailingService, StringMessages stringMessages, ErrorReporter errorReporter) {
        this(new MultiSelectionModel<MarkDTO>(), sailingService, stringMessages, errorReporter);
    }
    
    public CellTable<MarkDTO> getTable() {
        return table;
    }
    
    public SelectionModel<MarkDTO> getSelectionModel() {
        return selectionModel;
    }
    
    public List<MarkDTO> getAllCompetitors() {
        return allMarks;
    }
    
    public void refresh(Collection<MarkDTO> marks) {
        listProvider.getList().clear();
        listProvider.getList().addAll(marks);
        listProvider.flush();
        Collections.sort(listProvider.getList(), new Comparator<MarkDTO>() {
            @Override
            public int compare(MarkDTO o1, MarkDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
    
    public void refresh(String leaderboardName, String raceColumnName, String fleetName) {
        sailingService.getMarksInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Collection<MarkDTO>>() {
            @Override
            public void onSuccess(Collection<MarkDTO> result) {
                refresh(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not load marks: " + caught.getMessage());
            }
        });
    }
    
    public ListDataProvider<MarkDTO> getDataProvider() {
        return listProvider;
    }
}
