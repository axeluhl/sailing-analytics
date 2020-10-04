package com.sap.sse.datamining.ui.client.selection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.DefaultSelectionEventManager.EventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
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
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.controls.AbstractObjectRenderer;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ConfigureQueryParametersDialog extends AbstractDataMiningComponent<SerializableSettings> {
    
    private enum FilterParameterType {
        ValueList, Contains, StartsWith, EndsWith, Regex;
        
        public String getDisplayString(StringMessages stringMessages) {
            switch (this) {
            case ValueList:
                return stringMessages.listOfValues();
            case Contains:
                return stringMessages.containsText();
            case EndsWith:
                return stringMessages.endsWithText();
            case Regex:
                return stringMessages.regularExpression();
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

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;
    private final DataMiningSession session;
    private final DataRetrieverChainDefinitionProvider retrieverChainProvider;
    
    private final DialogBox dialog;
    private final FlowPanel contentContainer;
    private final Panel valuesTextConstraintContainer;
    
    private final Label retrieverLevelLabel;
    private final Label dimensionLabel;
    private final ValueListBox<FilterParameterType> parameterTypeSelectionBox;
    
    private final TextBox valuesTextConstraintInput;
    private final Label filterValuesGridLabel;
    private final ListDataProvider<Serializable> filterValuesDataProvider;
    private final MultiSelectionModel<Serializable> filterValuesSelectionModel;
    private final CellPreviewEvent.Handler<Serializable> selectionEventManager;
    private final DataGrid<Serializable> filterValuesGrid;
    private final Column<Serializable, ?> checkboxColumn;
    private final SimpleBusyIndicator busyIndicator;
    
    private Iterable<? extends Serializable> filterValuesToSelect;
    
    public ConfigureQueryParametersDialog(Component<?> parent, ComponentContext<?> componentContext, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, DataMiningSession session, DataRetrieverChainDefinitionProvider retrieverChainProvider) {
        super(parent, componentContext);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.session = session;
        this.retrieverChainProvider = retrieverChainProvider;

        dialog = new DialogBox(false, true);
        dialog.setText(getLocalizedShortName());
        dialog.setAnimationEnabled(true);
        
        DockPanel dialogPanel = new DockPanel();
        dialogPanel.setWidth("100%");
        dialog.setWidget(dialogPanel);
        
        // Dialog Controls
        FlowPanel controlsPanel = new FlowPanel();
        Style controlsPanelStyle = controlsPanel.getElement().getStyle();
        controlsPanelStyle.setProperty("display", "flex");
        
        FlowPanel dimensionInfoPanel = new FlowPanel();
        Style dimensionInfoPanelStyle = dimensionInfoPanel.getElement().getStyle();
        dimensionInfoPanelStyle.setDisplay(Display.FLEX);
        dimensionInfoPanelStyle.setFontWeight(FontWeight.BOLD);
        dimensionInfoPanelStyle.setMarginRight(1, Unit.EM);
        controlsPanel.add(dimensionInfoPanel);
        
        retrieverLevelLabel = new Label();
        retrieverLevelLabel.getElement().getStyle().setMarginRight(3, Unit.PX);
        dimensionInfoPanel.add(retrieverLevelLabel);
        dimensionInfoPanel.add(new Label(" - "));
        dimensionLabel = new Label();
        dimensionLabel.getElement().getStyle().setMarginLeft(3, Unit.PX);
        dimensionInfoPanel.add(dimensionLabel);
        
        Label parameterTypeSelectionBoxLabel = new Label(getDataMiningStringMessages().parameterType());
        parameterTypeSelectionBoxLabel.getElement().getStyle().setMarginRight(5, Unit.PX);
        controlsPanel.add(parameterTypeSelectionBoxLabel);
        parameterTypeSelectionBox = new ValueListBox<>(new AbstractObjectRenderer<FilterParameterType>() {
            @Override
            protected String convertObjectToString(FilterParameterType type) {
                return type.getDisplayString(getDataMiningStringMessages());
            }
        });
        parameterTypeSelectionBox.addValueChangeHandler(event -> showCreationControlsFor(event.getValue()));
        parameterTypeSelectionBox.setValue(FilterParameterType.Regex);
        parameterTypeSelectionBox.setAcceptableValues(Arrays.asList(FilterParameterType.values()));
        controlsPanel.add(parameterTypeSelectionBox);
        
        // Dialog Content
        contentContainer = new FlowPanel();
        Style contentPanelStyle = contentContainer.getElement().getStyle();
        contentPanelStyle.setMarginTop(1, Unit.EM);
        contentPanelStyle.setMarginBottom(1, Unit.EM);
        
        valuesTextConstraintContainer = new FlowPanel();
        valuesTextConstraintInput = new TextBox();
        valuesTextConstraintInput.setWidth("100%");
        valuesTextConstraintInput.getElement().getStyle().setMarginBottom(10, Unit.PX);
        valuesTextConstraintContainer.add(valuesTextConstraintInput);
        
        busyIndicator = new SimpleBusyIndicator(false, 0.85f);
        busyIndicator.getElement().getStyle().setTextAlign(TextAlign.CENTER);
        busyIndicator.setBusy(false);
        contentContainer.add(busyIndicator);

        filterValuesGridLabel = new Label(getDataMiningStringMessages().selectItemsAvailableForParameter());
        filterValuesGridLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        filterValuesGridLabel.getElement().getStyle().setMarginBottom(5, Unit.PX);
        contentContainer.add(filterValuesGridLabel);
        
        // TODO Add filter input when in value list mode

        filterValuesDataProvider = new ListDataProvider<>(o -> o.toString());
        filterValuesSelectionModel = new MultiSelectionModel<>(o -> o.toString());
        selectionEventManager = DefaultSelectionEventManager.createCustomManager(new CustomCheckboxEventTranslator());
        filterValuesGrid = createFilterValuesGrid();
        checkboxColumn = filterValuesGrid.getColumn(0);
        filterValuesGrid.setSelectionModel(filterValuesSelectionModel, selectionEventManager);
        filterValuesDataProvider.addDataDisplay(filterValuesGrid);
        contentContainer.add(filterValuesGrid);
        
        // Dialog Buttons
        FlowPanel buttonsBar = new FlowPanel();
        Style buttonsBarStyle = buttonsBar.getElement().getStyle();
        buttonsBarStyle.setProperty("display", "flex");
        buttonsBarStyle.setProperty("justifyContent", "flex-end");
        
        Button closeButton = new Button(getDataMiningStringMessages().close());
        closeButton.addClickHandler((e) -> hide());
        buttonsBar.add(closeButton);
        
        // Final Layout
        dialogPanel.add(controlsPanel, DockPanel.NORTH);
        dialogPanel.add(buttonsBar, DockPanel.SOUTH);
        dialogPanel.add(contentContainer, DockPanel.CENTER);
    }
    
    private DataGrid<Serializable> createFilterValuesGrid() {
        DataMiningDataGridResources dataGridResources = GWT.create(DataMiningDataGridResources.class);
        DataGrid<Serializable> filterValues = new DataGrid<>(Integer.MAX_VALUE, dataGridResources);
        filterValues.setAutoHeaderRefreshDisabled(true);
        filterValues.setAutoFooterRefreshDisabled(true);
        filterValues.addStyleName("dataMiningBorderTop");
        filterValues.setHeight("400px");
        
        // TODO Replace with SelectionCheckboxColumn like in TracTracEventManagementPanel
        Column<Serializable, ?> checkboxColumn = new Column<Serializable, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(Serializable object) {
                return filterValuesSelectionModel.isSelected(object);
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
    
    // TODO Parameter Overview
    // TODO Creation/Editing of dimension parameters
    // TODO Select parameter to be used as filter dimension values
    
    public void show(ConfigureFilterParameterEvent data) {
        this.updateCreationControlsAndContent(data);
        dialog.center();
    }
    
    private void updateCreationControlsAndContent(ConfigureFilterParameterEvent data) {
        filterValuesToSelect = data.getSelectedValues();
        retrieverLevelLabel.setText(data.getRetrieverLevel().getRetrievedDataType().getDisplayName());
        dimensionLabel.setText(data.getDimension().getDisplayName());
        parameterTypeSelectionBox.setValue(FilterParameterType.ValueList, true);
        
        filterValuesGrid.setVisible(false);
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
                        filterValuesDataProvider.setList(sortedData);
                        filterValuesToSelect.forEach(v -> filterValuesSelectionModel.setSelected(v, true));
                        
                        busyIndicator.setBusy(false);
                        filterValuesGrid.setVisible(true);
                        contentContainer.add(filterValuesGrid);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching the dimension values of " + data.getDimension() + ": " + caught.getMessage());
                        // TODO Display error message for user
                    }
                });
    }
    
    public void hide() {
        dialog.hide();
    }
    
    private void showCreationControlsFor(FilterParameterType type) {
        if (type.isTextConstrained()) {
            if (valuesTextConstraintContainer.getParent() == null) {                
                contentContainer.insert(valuesTextConstraintContainer, 0);
            }
            valuesTextConstraintInput.setValue(null);
            valuesTextConstraintInput.getElement().setPropertyString("placeholder", type.getDisplayString(getDataMiningStringMessages()));
            
            filterValuesGridLabel.setText(getDataMiningStringMessages().itemsMatchingTextConstraint());
            if (filterValuesGrid.getColumn(0) == checkboxColumn) {                
                filterValuesGrid.removeColumn(checkboxColumn);
                filterValuesGrid.setColumnWidth(0, "100%");
            }
            
            filterValuesToSelect = filterValuesSelectionModel.getSelectedSet();
            filterValuesGrid.setSelectionModel(null);
        } else {
            if (valuesTextConstraintContainer.getParent() != null) {
                contentContainer.remove(valuesTextConstraintContainer);
            }

            filterValuesGridLabel.setText(getDataMiningStringMessages().selectItemsAvailableForParameter());
            if (filterValuesGrid.getColumn(0) != checkboxColumn) {
                filterValuesGrid.insertColumn(0, checkboxColumn);
                filterValuesGrid.clearColumnWidth(0);
            }
            
            filterValuesGrid.setSelectionModel(filterValuesSelectionModel, selectionEventManager);
            filterValuesSelectionModel.clear();
            filterValuesToSelect.forEach(v -> filterValuesSelectionModel.setSelected(v, true));
        }
    }
    
    private FilterParameterType getSelectedParameterType() {
        return parameterTypeSelectionBox.getValue();
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
                    filterValuesSelectionModel.setSelected(value, !filterValuesSelectionModel.isSelected(value));
                    return SelectAction.IGNORE;
                }
                if (!filterValuesSelectionModel.getSelectedSet().isEmpty() && !isCheckboxColumn(event.getColumn())) {
                    return SelectAction.DEFAULT;
                }
            }
            return SelectAction.TOGGLE;
        }

        private boolean isCheckboxColumn(int columnIndex) {
            return columnIndex == filterValuesGrid.getColumnIndex(checkboxColumn);
        }
        
    }

}
