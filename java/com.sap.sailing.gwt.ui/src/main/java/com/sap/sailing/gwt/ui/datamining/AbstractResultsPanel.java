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

public abstract class AbstractResultsPanel<DimensionType, ResultType> extends FlowPanel implements QueryComponentsChangedListener<DimensionType> {

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private QueryComponentsProvider<DimensionType> queryComponentsProvider;

    private Label queryStatusLabel;
    private ResultsPresenter<ResultType> presenter;

    private Button runQueryButton;

    public AbstractResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<DimensionType> queryComponentsProvider, ResultsPresenter<ResultType> presenter) {
        super();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.queryComponentsProvider = queryComponentsProvider;
        this.presenter = presenter;
        
        add(createFunctionsPanel());
        add(this.presenter.getWidget());
        runQuery();
    }

    @Override
    public void queryComponentsChanged(QueryComponentsProvider<DimensionType> componentsProvider) {
        runQuery();
    }

    private void runQuery() {
        QueryDefinition<DimensionType> queryDefinition = queryComponentsProvider.getQueryDefinition();
        Iterable<String> errorMessages = queryComponentsProvider.validateQueryDefinition(queryDefinition);
        if (errorMessages == null || !errorMessages.iterator().hasNext()) {
            queryStatusLabel.setText(" | " + stringMessages.running());
            sendServerRequest(queryDefinition, new AsyncCallback<QueryResult<ResultType>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error running the query: " + caught.getMessage());
                }

                @Override
                public void onSuccess(QueryResult<ResultType> result) {
                    queryStatusLabel.setText(" | " + stringMessages.done());
                    presenter.showResult(result);
                }
            });
        } else {
            presenter.showError(stringMessages.queryNotValidBecause(), errorMessages);
        }
    }

    protected abstract void sendServerRequest(QueryDefinition<DimensionType> queryDefinition, AsyncCallback<QueryResult<ResultType>> asyncCallback);
    
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
                    queryComponentsProvider.addListener(AbstractResultsPanel.this);
                } else {
                    queryComponentsProvider.removeListener(AbstractResultsPanel.this);
                }
            }
        });
        functionsPanel.add(runAutomaticBox);
        
        runQueryButton = new Button(stringMessages.run());
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runQuery();
            }
        });
        functionsPanel.add(runQueryButton);
        
        queryStatusLabel = new Label();
        functionsPanel.add(queryStatusLabel);
        return functionsPanel;
    }

}
