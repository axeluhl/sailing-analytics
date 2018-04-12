package com.sap.sse.datamining.ui.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.QueryDefinitionChangedListener;
import com.sap.sse.datamining.ui.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractQueryDefinitionProvider<SettingsType extends Settings> extends AbstractComponent<SettingsType> implements QueryDefinitionProvider<SettingsType> {

    private final StringMessages stringMessages;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;

    private final Timer dataminingComponentsChangedTimer;
    private final int timerDelayInMillis = 30 * 1000;
    private Date componentsChangedTimepoint;
    private final DialogBox componentsChangedDialog;
    
    private boolean blockChangeNotification;
    private final Set<QueryDefinitionChangedListener> listeners;

    public AbstractQueryDefinitionProvider(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages, DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(parent, context);
        this.stringMessages = stringMessages;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        
        dataminingComponentsChangedTimer = new Timer() {
            @Override
            public void run() {
                getDataMiningService().getComponentsChangedTimepoint(new AsyncCallback<Date>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        AbstractQueryDefinitionProvider.this.errorReporter.reportError(stringMessages.errorFetchingComponentsChangedTimepoint(caught.getMessage()));
                        dataminingComponentsChangedTimer.schedule(timerDelayInMillis);
                    }
                    @Override
                    public void onSuccess(Date componentsChangedTimepoint) {
                        if (componentsChangedTimepoint.after(AbstractQueryDefinitionProvider.this.componentsChangedTimepoint)) {
                            AbstractQueryDefinitionProvider.this.componentsChangedTimepoint = componentsChangedTimepoint;
                            componentsChangedDialog.center();
                        }
                        dataminingComponentsChangedTimer.schedule(timerDelayInMillis);
                    }
                });
            }
        };
        componentsChangedDialog = createComponentsChangedDialog();

        blockChangeNotification = false;
        listeners = new HashSet<QueryDefinitionChangedListener>();
        
        getDataMiningService().getComponentsChangedTimepoint(new AsyncCallback<Date>() {
            @Override
            public void onFailure(Throwable caught) {
                AbstractQueryDefinitionProvider.this.errorReporter.reportError(stringMessages.errorFetchingComponentsChangedTimepoint(caught.getMessage()));
            }
            @Override
            public void onSuccess(Date componentsChangedTimepoint) {
                AbstractQueryDefinitionProvider.this.componentsChangedTimepoint = componentsChangedTimepoint;
                dataminingComponentsChangedTimer.schedule(timerDelayInMillis);
            }
        });
    }
    
    private DialogBox createComponentsChangedDialog() {
        DialogBox componentsChangedDialog = new DialogBox(false, true);
        componentsChangedDialog.setAnimationEnabled(true);
        componentsChangedDialog.setText(getStringMessages().dataMiningComponentsHaveBeenUpdated());
        
        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setSpacing(5);
        contentPanel.add(new HTML(new SafeHtmlBuilder().appendEscapedLines(getStringMessages().dataMiningComponentsNeedReloadDialogMessage()).toSafeHtml()));
        
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        contentPanel.add(buttonPanel);
        
        Button reloadButton = new Button(getStringMessages().reload());
        reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AbstractQueryDefinitionProvider.this.componentsChangedDialog.hide();
                reloadComponents();
            }
        });
        buttonPanel.add(reloadButton);
        
        Button closeButton = new Button(getStringMessages().close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AbstractQueryDefinitionProvider.this.componentsChangedDialog.hide();
            }
        });
        buttonPanel.add(closeButton);
        
        componentsChangedDialog.setWidget(contentPanel);
        return componentsChangedDialog;
    }

    @Override
    public Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        Collection<String> errorMessages = new ArrayList<String>();
        
        if (queryDefinition != null) {
            String grouperError = validateGrouper(queryDefinition);
            if (grouperError != null && !grouperError.isEmpty()) {
                errorMessages.add(grouperError);
            }
            String statisticError = validateStatisticAndAggregator(queryDefinition);
            if (statisticError != null && !statisticError.isEmpty()) {
                errorMessages.add(statisticError);
            }
            String retrieverChainError = validateDataRetrieverChain(queryDefinition);
            if (retrieverChainError != null && !retrieverChainError.isEmpty()) {
                errorMessages.add(retrieverChainError);
            }
        }
        
        return errorMessages;
    }

    private String validateGrouper(StatisticQueryDefinitionDTO queryDefinition) {
        for (FunctionDTO dimension : queryDefinition.getDimensionsToGroupBy()) {
            if (dimension != null) {
                return null;
            }
        }
        return stringMessages.noDimensionToGroupBySelectedError();
    }

    private String validateStatisticAndAggregator(StatisticQueryDefinitionDTO queryDefinition) {
        return queryDefinition.getStatisticToCalculate() == null || queryDefinition.getAggregatorDefinition() == null ? stringMessages.noStatisticSelectedError() : null;
    }

    private String validateDataRetrieverChain(StatisticQueryDefinitionDTO queryDefinition) {
        return queryDefinition.getDataRetrieverChainDefinition() == null ? stringMessages.noDataRetrieverChainDefinitonSelectedError() : null;
    }

    @Override
    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener) {
        listeners.remove(listener);
    }

    protected void setBlockChangeNotification(boolean block) {
        blockChangeNotification = block;
    }

    protected void notifyQueryDefinitionChanged() {
        if (!blockChangeNotification) {
            StatisticQueryDefinitionDTO queryDefinition = getQueryDefinition();
            if (isQueryDefinitionConsistent(queryDefinition)) {
                for (QueryDefinitionChangedListener listener : listeners) {
                    listener.queryDefinitionChanged(queryDefinition);
                }
            }
        }
    }

    private boolean isQueryDefinitionConsistent(StatisticQueryDefinitionDTO queryDefinition) {
        if (queryDefinition.getStatisticToCalculate() != null) { // The consistency can't be checked, if no statistic is selected
            String sourceTypeName = queryDefinition.getStatisticToCalculate().getSourceTypeName();
            
            if (queryDefinition.getDataRetrieverChainDefinition() != null && 
                !sourceTypeName.equals(queryDefinition.getDataRetrieverChainDefinition().getRetrievedDataTypeName())) {
                return false;
            }
            
            for (FunctionDTO dimensionToGroupBy : queryDefinition.getDimensionsToGroupBy()) {
                if (!sourceTypeName.equals(dimensionToGroupBy.getSourceTypeName())) {
                    return false;
                }
            }
        }
        
        return true;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }
    
    protected DataMiningServiceAsync getDataMiningService() {
        return dataMiningService;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}