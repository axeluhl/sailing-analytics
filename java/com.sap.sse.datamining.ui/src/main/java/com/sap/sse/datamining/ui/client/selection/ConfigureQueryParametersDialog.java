package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.DataRetrieverChainDefinitionProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.event.ConfigureFilterParameterEvent;
import com.sap.sse.datamining.ui.client.resources.DataMiningDataGridResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.panels.AbstractFilterablePanel;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ConfigureQueryParametersDialog extends AbstractDataMiningComponent<SerializableSettings> {
    
    private enum FilterParameterType {
        ValueList, Contains, StartsWith, EndsWith;
        
        public String getDisplayString(StringMessages stringMessages) {
            switch (this) {
            case ValueList:
                return stringMessages.listOfValues();
            case Contains:
                return stringMessages.containsText();
            case EndsWith:
                return stringMessages.endsWithText();
            case StartsWith:
                return stringMessages.startsWithText();
            default:
                throw new IllegalStateException("Display string for " + this + " not available");
            }
        }
        
        public boolean isTextConstrained() {
            return this != ValueList;
        }
    }

    private static final NaturalComparator NaturalComparator = new NaturalComparator();
    private static final String ContentMinWidth = "550px";
    private static final String FilterValuesGridHeight = "400px";
    private static final int SearchBoxWidth = 250;

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    
    private final DialogBox dialog;
    private final FlowPanel contentContainer;
    private final Panel gridHeaderContainer;
    
    private final Label retrieverLevelLabel;
    private final Label dimensionLabel;
    private final ValueListBox<FilterParameterType> parameterTypeSelectionBox;
    
    private final AbstractFilterablePanel<Serializable> filterPanel;
    private final Label gridLabel;
    private final ListDataProvider<Serializable> filteredData;
    private final MultiSelectionModel<Serializable> selectionModel;
    private final CellPreviewEvent.Handler<Serializable> selectionEventManager;
    private final DataGrid<Serializable> dataGrid;
    private final Column<Serializable, ?> checkboxColumn;
    private final SimpleBusyIndicator busyIndicator;
    
    private String filterInputToApply = null;
    private Iterable<? extends Serializable> filterValuesToSelect = Collections.emptyList();
    
    public ConfigureQueryParametersDialog(Component<?> parent, ComponentContext<?> componentContext, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSession session, DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, componentContext);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverChainProvider = retrieverChainProvider;
        
        // Header Controls
        retrieverLevelLabel = new Label();
        retrieverLevelLabel.getElement().getStyle().setMarginRight(3, Unit.PX);
        dimensionLabel = new Label();
        dimensionLabel.getElement().getStyle().setMarginLeft(3, Unit.PX);
        
        Label parameterTypeSelectionBoxLabel = new Label(getDataMiningStringMessages().parameterType());
        parameterTypeSelectionBoxLabel.getElement().getStyle().setMarginRight(5, Unit.PX);
        parameterTypeSelectionBox = new ValueListBox<>(new AbstractObjectRenderer<FilterParameterType>() {
            @Override
            protected String convertObjectToString(FilterParameterType type) {
                return type.getDisplayString(getDataMiningStringMessages());
            }
        });
        parameterTypeSelectionBox.addValueChangeHandler(event -> showCreationControlsFor(event.getValue()));
        parameterTypeSelectionBox.setValue(FilterParameterType.ValueList);
        parameterTypeSelectionBox.setAcceptableValues(Arrays.asList(FilterParameterType.values()));
        
        // Content Controls
        filteredData = new ListDataProvider<>(o -> o.toString());
        filterPanel = new AbstractFilterablePanel<Serializable>(null, filteredData, getDataMiningStringMessages()) {
            public Iterable<String> getSearchableStrings(Serializable element) {
                return Collections.singleton(element.toString());
            }
            @Override
            public AbstractCellTable<Serializable> getCellTable() {
                return dataGrid;
            };
        };
        filterPanel.addFilter(new TextConstraintFilter());
        filterPanel.setSpacing(0);
        filterPanel.getTextBox().setWidth("100%");
        
        busyIndicator = new SimpleBusyIndicator(false, 0.85f);
        busyIndicator.getElement().getStyle().setTextAlign(TextAlign.CENTER);
        busyIndicator.setBusy(false);

        gridLabel = new Label();
        gridLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        gridLabel.getElement().getStyle().setMarginBottom(5, Unit.PX);

        selectionModel = new MultiSelectionModel<>(o -> o.toString());
        selectionEventManager = DefaultSelectionEventManager.createCustomManager(new CustomCheckboxEventTranslator());
        dataGrid = createFilterValuesGrid();
        checkboxColumn = dataGrid.getColumn(0);
        dataGrid.setSelectionModel(selectionModel, selectionEventManager);
        filteredData.addDataDisplay(dataGrid);
        
        // Dialog Buttons
        Button closeButton = new Button(getDataMiningStringMessages().cancel());
        closeButton.addClickHandler((e) -> hide());
        
        Button applyButton = new Button(getDataMiningStringMessages().apply());
        applyButton.addClickHandler(e -> applyParameter());
        
        // Layout
        FlowPanel controlsPanel = new FlowPanel();
        Style controlsPanelStyle = controlsPanel.getElement().getStyle();
        controlsPanelStyle.setDisplay(Display.FLEX);
        controlsPanelStyle.setProperty("justifyContent", "flex-end");
        controlsPanel.add(createContextInfoPanel());
        controlsPanel.add(parameterTypeSelectionBoxLabel);
        controlsPanel.add(parameterTypeSelectionBox);
        
        FlowPanel buttonsBar = new FlowPanel();
        Style buttonsBarStyle = buttonsBar.getElement().getStyle();
        buttonsBarStyle.setProperty("display", "flex");
        buttonsBarStyle.setProperty("justifyContent", "space-between");
        buttonsBar.add(closeButton);
        buttonsBar.add(applyButton);

        gridHeaderContainer = new FlowPanel();
        gridHeaderContainer.getElement().getStyle().setDisplay(Display.FLEX);
        gridHeaderContainer.add(filterPanel);
        gridHeaderContainer.add(gridLabel);

        contentContainer = new FlowPanel();
        Style contentContainerStyle = contentContainer.getElement().getStyle();
        contentContainerStyle.setProperty("minWidth", ContentMinWidth);
        contentContainerStyle.setMarginTop(1, Unit.EM);
        contentContainerStyle.setMarginBottom(1, Unit.EM);
        contentContainer.add(gridHeaderContainer);
        contentContainer.add(busyIndicator);
        contentContainer.add(dataGrid);

        DockPanel dialogPanel = new DockPanel();
        dialogPanel.setWidth("100%");
        dialogPanel.add(controlsPanel, DockPanel.NORTH);
        dialogPanel.add(buttonsBar, DockPanel.SOUTH);
        dialogPanel.add(contentContainer, DockPanel.CENTER);

        dialog = new DialogBox(false, true);
        dialog.setText(getLocalizedShortName());
        dialog.setAnimationEnabled(true);
        dialog.setGlassEnabled(true);
        dialog.setWidget(dialogPanel);
        dialog.getElement().getStyle().setProperty("minHeight", FilterValuesGridHeight);
        
        showCreationControlsFor(getSelectedParameterType());
    }

    private Panel createContextInfoPanel() {
        Panel dimensionInfoPanel = new FlowPanel();
        Style dimensionInfoPanelStyle = dimensionInfoPanel.getElement().getStyle();
        dimensionInfoPanelStyle.setDisplay(Display.FLEX);
        dimensionInfoPanelStyle.setProperty("flexGrow", "1");
        dimensionInfoPanelStyle.setFontWeight(FontWeight.BOLD);
        dimensionInfoPanelStyle.setMarginRight(1, Unit.EM);
        dimensionInfoPanel.add(retrieverLevelLabel);
        dimensionInfoPanel.add(new Label(" - "));
        dimensionInfoPanel.add(dimensionLabel);
        return dimensionInfoPanel;
    }
    
    private DataGrid<Serializable> createFilterValuesGrid() {
        DataMiningDataGridResources dataGridResources = GWT.create(DataMiningDataGridResources.class);
        DataGrid<Serializable> filterValues = new DataGrid<>(Integer.MAX_VALUE, dataGridResources);
        filterValues.setAutoHeaderRefreshDisabled(true);
        filterValues.setAutoFooterRefreshDisabled(true);
        filterValues.addStyleName("dataMiningBorderTop");
        filterValues.setHeight(FilterValuesGridHeight);
        
        // TODO Replace with SelectionCheckboxColumn like in TracTracEventManagementPanel?
        Column<Serializable, ?> checkboxColumn = new Column<Serializable, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(Serializable object) {
                return selectionModel.isSelected(object);
            }
        };
        filterValues.addColumn(checkboxColumn);
        TextColumn<Serializable> contentColumn = new TextColumn<Serializable>() {
            @Override
            public String getValue(Serializable element) {
                return element.toString();
            }
        };
        contentColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        filterValues.addColumn(contentColumn);
        
        return filterValues;
    }
    
    // TODO Creation/Editing of dimension parameters
    // TODO Select existing parameter to be used as filter dimension values
    
    public void show(ConfigureFilterParameterEvent data) {
        this.updateCreationControlsAndContent(data);
        dialog.center();
    }
    
    private void updateCreationControlsAndContent(ConfigureFilterParameterEvent data) {
        filterValuesToSelect = data.getSelectedValues();
        retrieverLevelLabel.setText(data.getRetrieverLevel().getRetrievedDataType().getDisplayName());
        dimensionLabel.setText(data.getDimension().getDisplayName());
        parameterTypeSelectionBox.setValue(FilterParameterType.ValueList, true);
        
        dataGrid.setVisible(false);
        busyIndicator.setBusy(true);
        HashSet<FunctionDTO> dimensionDTOs = new HashSet<>();
        dimensionDTOs.add(data.getDimension());
        dataMiningService.getDimensionValuesFor(session, retrieverChainProvider.getDataRetrieverChainDefinition(), data.getRetrieverLevel(),
                dimensionDTOs, retrieverChainProvider.getRetrieverSettings(), /*filterSelection*/ new HashMap<>(), LocaleInfo.getCurrentLocale().getLocaleName(),
                new AsyncCallback<QueryResultDTO<HashSet<Object>>>() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onSuccess(QueryResultDTO<HashSet<Object>> result) {
                        Map<GroupKey, HashSet<Object>> results = result.getResults();
                        List<Serializable> sortedData = new ArrayList<>();
                        if (!results.isEmpty()) {
                            GroupKey contentKey = new GenericGroupKey<FunctionDTO>(data.getDimension());
                            sortedData.addAll((Collection<? extends Serializable>) results.get(contentKey));
                            sortedData.sort((o1, o2) -> NaturalComparator.compare(o1.toString(), o2.toString()));
                        }
                        filterPanel.updateAll(sortedData);
                        filterValuesToSelect.forEach(v -> selectionModel.setSelected(v, true));
                        
                        busyIndicator.setBusy(false);
                        dataGrid.setVisible(true);
                        contentContainer.add(dataGrid); // Adding dataGrid again to trigger rendering after filling the data provider
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + data.getDimension() + ": " + caught.getMessage());
                        // TODO Display error message for user
                    }
                });
    }
    
    private void showCreationControlsFor(FilterParameterType type) {
        final StringMessages stringMessages = getDataMiningStringMessages(); 
        if (type.isTextConstrained()) {
            filterPanel.getTextBox().getElement().setPropertyString("placeholder", type.getDisplayString(stringMessages));
            if (filterInputToApply != null) {
                filterPanel.getTextBox().setValue(filterInputToApply);
                filterInputToApply = null;
            }
            filterPanel.search(filterPanel.getTextBox().getValue());
            
            gridLabel.setText(getDataMiningStringMessages().itemsMatchingTextConstraint());
            if (dataGrid.getColumn(0) == checkboxColumn) {                
                dataGrid.removeColumn(checkboxColumn);
                dataGrid.setColumnWidth(0, "100%");
            }
            
            filterValuesToSelect = selectionModel.getSelectedSet();
            dataGrid.setSelectionModel(null);
            
            // Update styles
            final Style headerStyle = gridHeaderContainer.getElement().getStyle();
            headerStyle.setProperty("flexDirection", "column");
            headerStyle.setProperty("justifyContent", null);
            headerStyle.setProperty("alignItems", null);
            headerStyle.setMarginBottom(0, Unit.PX);
            
            final Style filterPanelStyle = filterPanel.getElement().getStyle();
            filterPanelStyle.setMarginBottom(10, Unit.PX);
            filterPanelStyle.setWidth(100, Unit.PCT);
        } else {
            filterPanel.getTextBox().getElement().setPropertyString("placeholder", stringMessages.search());
            filterInputToApply = filterPanel.getTextBox().getValue();
            filterPanel.search(null);
            
            gridLabel.setText(getDataMiningStringMessages().selectItemsAvailableForParameter());
            if (dataGrid.getColumn(0) != checkboxColumn) {
                dataGrid.insertColumn(0, checkboxColumn);
                dataGrid.clearColumnWidth(0);
            }
            
            dataGrid.setSelectionModel(selectionModel, selectionEventManager);
            selectionModel.clear();
            filterValuesToSelect.forEach(v -> selectionModel.setSelected(v, true));
            
            // Update styles
            final Style headerStyle = gridHeaderContainer.getElement().getStyle();
            headerStyle.setProperty("flexDirection", "row-reverse");
            headerStyle.setProperty("justifyContent", "space-between");
            headerStyle.setProperty("alignItems", "center");
            headerStyle.setMarginBottom(2, Unit.PX);
            
            final Style filterPanelStyle = filterPanel.getElement().getStyle();
            filterPanelStyle.setMarginBottom(0, Unit.PX);
            filterPanelStyle.setWidth(SearchBoxWidth, Unit.PX);
        }
    }
    
    private FilterParameterType getSelectedParameterType() {
        return parameterTypeSelectionBox.getValue();
    }
    
    private void applyParameter() {
        // TODO Implement me
        hide();
    }
    
    public void hide() {
        dialog.hide();
    }

    @Override
    public String getId() {
        return "ConfigureQueryParametersDialog";
    }

    @Override
    public String getLocalizedShortName() {
        return this.getDataMiningStringMessages().configureQueryParametersDialog();
    }

    @Override
    public Widget getEntryWidget() {
        return dialog;
    }

    @Override
    public boolean isVisible() {
        return dialog.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        dialog.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<SerializableSettings> getSettingsDialogComponent(
            SerializableSettings useTheseSettings) {
        return null;
    }

    @Override
    public SerializableSettings getSettings() {
        return null;
    }

    @Override
    public void updateSettings(SerializableSettings newSettings) { }

    @Override
    public String getDependentCssClassName() {
        return "configure-query-parameters-dialog";
    }
    
    private class TextConstraintFilter implements Filter<Serializable> {

        @Override
        public boolean matches(Serializable object) {
            final FilterParameterType selectedParameterType = getSelectedParameterType();
            if (!selectedParameterType.isTextConstrained()) {
                return true;
            }
            
            final String text = object.toString().toUpperCase();
            final String constraint = filterPanel.getTextBox().getValue().trim().toUpperCase();
            switch (selectedParameterType) {
            case Contains:
                return text.contains(constraint);
            case EndsWith:
                return text.endsWith(constraint);
            case StartsWith:
                return text.startsWith(constraint);
            default:
                throw new IllegalStateException("Not implemented for '" + selectedParameterType + "'");
            }
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }
        
    }
    
    private class CustomCheckboxEventTranslator implements EventTranslator<Serializable> {
        
        @Override
        public boolean clearCurrentSelection(CellPreviewEvent<Serializable> event) {
            return !isCheckboxColumn(event.getColumn());
        }

        @Override
        public SelectAction translateSelectionEvent(CellPreviewEvent<Serializable> event) {
            NativeEvent nativeEvent = event.getNativeEvent();
            if (BrowserEvents.CLICK.equals(nativeEvent.getType())) {
                if (nativeEvent.getCtrlKey()) {
                    Serializable value = event.getValue();
                    selectionModel.setSelected(value, !selectionModel.isSelected(value));
                    return SelectAction.IGNORE;
                }
                if (!selectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                    return SelectAction.DEFAULT;
                }
            }
            return SelectAction.TOGGLE;
        }

        private boolean isCheckboxColumn(int columnIndex) {
            return columnIndex == dataGrid.getColumnIndex(checkboxColumn);
        }
        
    }

}
