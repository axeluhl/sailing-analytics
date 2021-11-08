package com.sap.sse.datamining.ui.client.selection;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.ui.client.AbstractDataMiningComponent;
import com.sap.sse.datamining.ui.client.DataMiningServiceAsync;
import com.sap.sse.datamining.ui.client.QueryDefinitionChangedListener;
import com.sap.sse.datamining.ui.client.QueryDefinitionProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractQueryDefinitionProvider<SettingsType extends Settings>
        extends AbstractDataMiningComponent<SettingsType> implements QueryDefinitionProvider<SettingsType> {

    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;

    private final Timer dataminingComponentsChangedTimer;
    private final int timerDelayInMillis = 30 * 1000;
    private Date componentsChangedTimepoint;
    private final DialogBox componentsChangedDialog;

    private boolean blockChangeNotification;
    private final Set<QueryDefinitionChangedListener> listeners;

    public AbstractQueryDefinitionProvider(Component<?> parent, ComponentContext<?> context,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        super(parent, context);
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;

        dataminingComponentsChangedTimer = new Timer() {
            @Override
            public void run() {
                getDataMiningService().getComponentsChangedTimepoint(new AsyncCallback<Date>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        AbstractQueryDefinitionProvider.this.errorReporter.reportError(getDataMiningStringMessages()
                                .errorFetchingComponentsChangedTimepoint(caught.getMessage()));
                        dataminingComponentsChangedTimer.schedule(timerDelayInMillis);
                    }

                    @Override
                    public void onSuccess(Date componentsChangedTimepoint) {
                        if (componentsChangedTimepoint
                                .after(AbstractQueryDefinitionProvider.this.componentsChangedTimepoint)) {
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
                AbstractQueryDefinitionProvider.this.errorReporter.reportError(
                        getDataMiningStringMessages().errorFetchingComponentsChangedTimepoint(caught.getMessage()));
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
        componentsChangedDialog.setText(getDataMiningStringMessages().dataMiningComponentsHaveBeenUpdated());

        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.setSpacing(5);
        contentPanel.add(new HTML(new SafeHtmlBuilder()
                .appendEscapedLines(getDataMiningStringMessages().dataMiningComponentsNeedReloadDialogMessage())
                .toSafeHtml()));

        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("floatRight");
        contentPanel.add(buttonPanel);

        Button reloadButton = new Button(getDataMiningStringMessages().reload());
        reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AbstractQueryDefinitionProvider.this.componentsChangedDialog.hide();
                reloadComponents();
            }
        });
        reloadButton.addStyleName("dataMiningMarginLeft");
        buttonPanel.add(reloadButton);

        Button closeButton = new Button(getDataMiningStringMessages().close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AbstractQueryDefinitionProvider.this.componentsChangedDialog.hide();
            }
        });
        closeButton.addStyleName("dataMiningMarginLeft");
        buttonPanel.add(closeButton);

        componentsChangedDialog.setWidget(contentPanel);
        return componentsChangedDialog;
    }

    @Override
    public Iterable<String> validateQueryDefinition(StatisticQueryDefinitionDTO queryDefinition) {
        Collection<String> errorMessages = new ArrayList<String>();

        if (queryDefinition != null) {
            StringMessages stringMessages = getDataMiningStringMessages();
            if (queryDefinition.getStatisticToCalculate() == null ||
                    queryDefinition.getDataRetrieverChainDefinition() == null) {
                errorMessages.add(stringMessages.noStatisticSelectedError());
            } else {
                // Other components aren't available if no statistic has been selected
                // Reporting an error for something that isn't there would be misleading
                if (queryDefinition.getAggregatorDefinition() == null) {
                    errorMessages.add(stringMessages.noAggregatorSelectedError());
                }
                if (queryDefinition.getDimensionsToGroupBy().isEmpty()) {
                    errorMessages.add(stringMessages.noDimensionToGroupBySelectedError());
                }
            }
        }

        return errorMessages;
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
            for (QueryDefinitionChangedListener listener : listeners) {
                listener.queryDefinitionChanged(queryDefinition);
            }
        }
    }

    protected DataMiningServiceAsync getDataMiningService() {
        return dataMiningService;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}