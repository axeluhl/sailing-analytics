package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class QueryResultsPanel extends FlowPanel implements QueryDefinitionChangedListener {

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private QueryDefinitionProvider queryDefinitionProvider;

    private Label queryStatusLabel;
    private ResultsPresenter<Number> presenter;

    private Button runQueryButton;

    public QueryResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider, ResultsPresenter<Number> presenter) {
        super();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.queryDefinitionProvider = queryDefinitionProvider;
        this.presenter = presenter;
        
        add(createFunctionsPanel());
        add(this.presenter.getWidget());
        runQuery(this.queryDefinitionProvider.getQueryDefinition());
    }

    @Override
    public void queryDefinitionChanged(QueryDefinition newQueryDefinition) {
        runQuery(newQueryDefinition);
    }

    private void runQuery(QueryDefinition queryDefinition) {
        Iterable<String> errorMessages = queryDefinitionProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            queryStatusLabel.setText(" | " + stringMessages.running());
            getSailingService().runGPSFixQuery(queryDefinition, new AsyncCallback<QueryResult<Number>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error running the query: " + caught.getMessage());
                }
            
                @Override
                public void onSuccess(QueryResult<Number> result) {
                    queryStatusLabel.setText(" | " + stringMessages.done());
                    presenter.showResult(result);
                }
            });
        } else {
            presenter.showError(stringMessages.queryNotValidBecause(), errorMessages);
        }
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        CheckBox runAutomaticBox = new CheckBox(stringMessages.runAutomatically());
        runAutomaticBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Boolean runAutomatic = event.getValue();
                runQueryButton.setVisible(!runAutomatic);
                if (runAutomatic) {
                    queryDefinitionProvider.addListener(QueryResultsPanel.this);
                } else {
                    queryDefinitionProvider.removeListener(QueryResultsPanel.this);
                }
            }
        });
        functionsPanel.add(runAutomaticBox);
        
        runQueryButton = new Button(stringMessages.run());
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runQuery(null);
            }
        });
        functionsPanel.add(runQueryButton);
        
        queryStatusLabel = new Label();
        functionsPanel.add(queryStatusLabel);
        return functionsPanel;
    }

}
